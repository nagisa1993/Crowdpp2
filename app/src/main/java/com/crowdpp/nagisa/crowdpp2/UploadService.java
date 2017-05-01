package com.crowdpp.nagisa.crowdpp2;

import com.crowdpp.nagisa.crowdpp2.ActivityRecognizedService;
import android.app.Service;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

/**
 * upload activity text file
 * @author Haiyue Ma
 */

public class UploadService extends Service{

    private ActivityRecognizedService activityRecognizedService = new ActivityRecognizedService();

    private boolean uploadflag = true;
    private boolean u_activity_flag = false;
    private String formattedDate;

    private Handler mPeriodicEventHandler;

    private String actionUrl = "http://54.196.39.156/uploadActivity.php";
    private final int PERIODIC_EVENT_TIMEOUT = 60*60*1000;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Runnable doPeriodicTask = new Runnable()
    {
        public void run(){
            //your action here
            try{
                if (uploadActivityFile())
                {
                    uploadflag = true;
                }
                else
                {
                    uploadflag = false;
                }
                mPeriodicEventHandler.postDelayed(doPeriodicTask, PERIODIC_EVENT_TIMEOUT);

            }  catch (Exception ex) {
            }
        }
    };

    private boolean uploadActivityFile()//upload called by  doPeriodicTask first, networkreceiver is called if the first fails.
    {

        Log.i("uploadMfccFile", "The functions is called");
        Thread thread=new Thread(){
            public void run(){
                try {
                    while(!activityRecognizedService.activityFilename.isEmpty()){

                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                        formattedDate = df.format(calendar.getTime());

                        String srcPath = activityRecognizedService.activityFilename.peek();


                        URL url = new URL(actionUrl);
                        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                        httpURLConnection.setDoInput(true);
                        httpURLConnection.setDoOutput(true);
                        httpURLConnection.setUseCaches(false);
                        httpURLConnection.setRequestMethod("POST");
                        httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
                        httpURLConnection.setRequestProperty("Charset", "UTF-8");
                        httpURLConnection.setRequestProperty("Content-Type","multipart/form-data;boundary=" + boundary);

                        //kill http if does not response(>30000ms)
                        httpURLConnection.setReadTimeout(30000);
                        DataOutputStream dos = new DataOutputStream(
                                httpURLConnection.getOutputStream());

                        dos.writeBytes(twoHyphens + boundary + lineend);
                        dos.writeBytes("Content-Disposition: form-data; name=\"fileToUpload\"; filename=\""
                                + srcPath.substring(srcPath.lastIndexOf("/") + 1)
                                + "\""
                                + lineend);
                        dos.writeBytes(lineend);
                        FileInputStream fis = new FileInputStream(srcPath);
                        byte[] buffer = new byte[8192];
                        int count = 0;
                        while ((count = fis.read(buffer)) != -1)
                        {
                            dos.write(buffer, 0, count);
                        }
                        Log.i("uploadMfccResult","step 4 success");
                        fis.close();
                        dos.writeBytes(lineend);
                        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineend);
                        dos.flush();
                        Log.i("uploadMfccResult","step 5 success");


                        InputStream is = httpURLConnection.getInputStream();
                        Log.i("uploadMfccResult","step 6 success");
                        InputStreamReader isr = new InputStreamReader(is, "utf-8");
                        Log.i("uploadMfccResult","step 7 success");
                        BufferedReader br = new BufferedReader(isr);
                        Log.i("uploadMfccResult","step 8 success");

                        String uploadrespond = br.readLine();
                        Log.i("uploadMfccResult","step 9 success");

                        Log.i("uploadMfccResult","uploadrespond: "+ uploadrespond);
                        dos.close();
                        is.close();

                        if (uploadrespond.contains("Success"))
                        {
                            u_activity_flag = true;
                            activityRecognizedService.activityFilename.poll();
                            Log.i("uploadMfccResult","Pop successfully!");
                            //create copy dir for uploaded files
//							File file =new File(Constants.crowdppPath+"/copy/");
//
//							if  (!file.exists()  && !file.isDirectory())
//							{
//								file .mkdir();
//							} else
//							{
                            try{
                                File srcfile = new File(srcPath);
//									copyDirectory(srcfile,file);
                                RecursionDeleteFile(srcfile);
                                Log.i("uploadMfccFile","copy success");
                            }  catch (Exception ex) {
                                System.err.println(ex);
                                Log.i("uploadMfccFile","copy failure");
                            }
//							}


                        }else{
                            u_activity_flag = false;
                        }
                    }
                } catch (Exception e) {
                }

            }
        };

        thread.start();
        try {
            thread.join();

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        thread.interrupt();



        //if upload the file and update the table successfully, return true.
        return u_activity_flag;
    }

    private void copyDirectory(File sourceLocation , File targetLocation)throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();
            for (int i=0; i<children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {

            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }
    private void RecursionDeleteFile(File file){
        if(file.isFile()){
            file.delete();
            return;
        }
        if(file.isDirectory()){
            File[] childFile = file.listFiles();
            if(childFile == null || childFile.length == 0){
                file.delete();
                return;
            }
            for(File f : childFile){
                RecursionDeleteFile(f);
            }
            file.delete();
        }
    }

    //Check if it is connected to Internet
    private boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("www.google.com"); //You can replace it with your name

            if (ipAddr.equals("")) {
                return false;
            } else {
                return true;
            }

        } catch (Exception e) {
            return false;
        }

    }

    private class uploadThread implements Runnable{
        String filename;
        public uploadThread(String filename){
            this.filename = filename;
        }

        public void run(){


            try {

                Log.i("uploadThread", "filename:" + filename);
                File srcfile = new File(filename);
                if(srcfile.exists()){
                    URL url = new URL(caliUrl);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url
                            .openConnection();
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setUseCaches(false);
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
                    httpURLConnection.setRequestProperty("Charset", "UTF-8");
                    httpURLConnection.setRequestProperty("Content-Type",
                            "multipart/form-data;boundary=" + boundary);
                    DataOutputStream dos = new DataOutputStream(
                            httpURLConnection.getOutputStream());

                    dos.writeBytes(twoHyphens + boundary + lineend);
                    dos.writeBytes("Content-Disposition: form-data; name=\"fileToUpload\"; filename=\""
                            + filename.substring(filename.lastIndexOf("/") + 1)
                            + "\""
                            + lineend);
                    dos.writeBytes(lineend);
                    FileInputStream fis = new FileInputStream(filename);
                    byte[] buffer = new byte[8192];
                    int count = 0;
                    while ((count = fis.read(buffer)) != -1)
                    {
                        dos.write(buffer, 0, count);
                    }
                    fis.close();
                    dos.writeBytes(lineend);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineend);
                    dos.flush();
                    InputStream is = httpURLConnection.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is, "utf-8");
                    BufferedReader br = new BufferedReader(isr);
                    uploadresult = br.readLine();

                    Log.i("uploadresult","uploadresult: "+uploadresult);
                    dos.close();
                    is.close();
                    if(uploadresult.contains("Success"))
                    {
                        RecursionDeleteFile(srcfile);
                        Log.i("uploadThread","Delete Success");
                    }

                }

            } catch (Exception e) {
            }
        }
    }
}
