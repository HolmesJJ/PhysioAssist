package sg.com.nyp.a164936j.physioAssist.fragments.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import sg.com.nyp.a164936j.physioAssist.IPAddress;
import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.configuration.CustomSharedPreference;
import sg.com.nyp.a164936j.physioAssist.configuration.GetSetSharedPreferences;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class DownloadFileService extends IntentService {

    private static final int GET_DEMO_VIDEO_TASK_ID = 1;
    private static final int GET_COUNTING_VIDEO_TASK_ID = 2;
    private static final String TAG = DownloadFileService.class.getSimpleName();

    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private Handler handler = new Handler(Looper.getMainLooper());
    private File videoFolder;
    private File demoFolder;
    private File countingFolder;

    private final int progressMax = 100;

    public DownloadFileService() {
        super(TAG);
    }

    protected void onHandleIntent(Intent intent) {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Download File Notification", NotificationManager.IMPORTANCE_NONE);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setPriority(Notification.BADGE_ICON_NONE)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher))
                .setOngoing(false)
                .setContentTitle("Video Download")
                .setContentText("Download is ready...");
        notificationManager.notify(1, notificationBuilder.build());

        // 创建文件夹
        videoFolder = new File(getApplication().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+ "/PhysioAssist/Video");
        if (!videoFolder.exists()) {
            videoFolder.mkdirs();
        }
        demoFolder = new File(getApplication().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+ "/PhysioAssist/Video/Demo");
        if (!demoFolder.exists()) {
            demoFolder.mkdirs();
        }
        countingFolder = new File(getApplication().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+ "/PhysioAssist/Video/Counting");
        if (!countingFolder.exists()) {
            countingFolder.mkdirs();
        }

        // 开始检查下载进度
        handler.post(videoProgress);

        // 开始下载Demo
        if(CustomSharedPreference.videosStatus != null) {
            downloadDemo();
        }
    }

    private void downloadDemo() {
        DownLoadVideoTask downLoadDemoVideoTask = new DownLoadVideoTask(demoFolder, "demo", GET_DEMO_VIDEO_TASK_ID);
        downLoadDemoVideoTask.execute();
    }

    private void downloadCounting() {
        DownLoadVideoTask downLoadCountingVideoTask = new DownLoadVideoTask(countingFolder, "counting", GET_COUNTING_VIDEO_TASK_ID);
        downLoadCountingVideoTask.execute();
    }

    // 检查下载进度
    private Runnable videoProgress = new Runnable() {
        @Override
        public void run() {
            // 如果第一个视频正在下载
            if(CustomSharedPreference.videosStatus.getDownloadDemoStatus().equals("Downloading") &&
                    CustomSharedPreference.videosStatus.getDownloadCountingStatus().equals("Ready")) {
                notificationBuilder.setContentText(CustomSharedPreference.videosStatus.getExerciseName() + " Demo: 1/2")
                        .setProgress(progressMax, CustomSharedPreference.videosStatus.getVideoProgress(), false)
                        .setOngoing(true);
                notificationManager.notify(1, notificationBuilder.build());
            }
            // 如果第二个视频正在下载
            else if(CustomSharedPreference.videosStatus.getDownloadDemoStatus().equals("Finish") &&
                    CustomSharedPreference.videosStatus.getDownloadCountingStatus().equals("Downloading")) {
                notificationBuilder.setContentText(CustomSharedPreference.videosStatus.getExerciseName() + " Counting: 2/2")
                        .setProgress(progressMax, CustomSharedPreference.videosStatus.getVideoProgress(), false)
                        .setOngoing(true);
                notificationManager.notify(1, notificationBuilder.build());
            }
            handler.postDelayed(videoProgress, 500);
        }
    };

    public class DownLoadVideoTask extends AsyncTask<Void, Void, String> {
        private String language;
        private File downloadFolder;
        private String fileType;
        private int exerciseId;
        private String exerciseName;
        private int requestId;
        private double downloadLength = 0;

        public DownLoadVideoTask(File downloadFolder, String fileType, int requestId) {
            this.downloadFolder = downloadFolder;
            this.fileType = fileType;
            this.requestId = requestId;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = "failed";
            // Set Downloading...
            if(requestId == GET_DEMO_VIDEO_TASK_ID) {
                CustomSharedPreference.videosStatus.setDownloadDemoStatus("Downloading");
            }
            else if(requestId == GET_COUNTING_VIDEO_TASK_ID) {
                CustomSharedPreference.videosStatus.setDownloadCountingStatus("Downloading");
            }
            language = GetSetSharedPreferences.getDefaults("language", getApplicationContext());
            exerciseId = CustomSharedPreference.videosStatus.getExerciseId();
            exerciseName = CustomSharedPreference.videosStatus.getExerciseName();
            File file = new File(downloadFolder, exerciseId + "_" + fileType + "_" + language + ".mp4");
            try {
                System.out.println("http://" + IPAddress.ipaddress + "/Videos/" + exerciseId + "_" + fileType + "_" + language + ".mp4");
                URL url = new URL("http://" + IPAddress.ipaddress + "/Videos/" + exerciseId + "_" + fileType + "_" + language + ".mp4");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                FileOutputStream fos = null;
                InputStream inputStream = null;
                try {
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setReadTimeout(10000);
                    urlConnection.setConnectTimeout(3000);
                    urlConnection.setRequestProperty("Accept-Encoding", "identity");
                    double fileLength = urlConnection.getContentLength();
                    int statusCode = urlConnection.getResponseCode();
                    // receive response as inputStream
                    fos = new FileOutputStream(file);
                    inputStream = urlConnection.getInputStream();
                    if (statusCode == 200) {
                        // video大小
                        if(fileLength > 0) {
                            // 建立一个byte数组作为缓冲区，等下把读取到的数据储存在这个数组
                            byte[] buffer = new byte[8192];
                            int len = 0;
                            while ((len = inputStream.read(buffer)) != -1) {
                                fos.write(buffer, 0, len);
                                downloadLength = downloadLength + len;
                                int calculateResult = (int)(downloadLength * 100 / fileLength);
                                if(fileType.equals("demo") && calculateResult == 100) {
                                    CustomSharedPreference.videosStatus.setVideoProgress(0);
                                }
                                else {
                                    CustomSharedPreference.videosStatus.setVideoProgress(calculateResult);
                                }
                            }
                            fos.flush();
                            result = "succeeded";
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (file.exists()) {
                        file.delete();
                    }
                } finally {
                    if(fos!=null)
                        fos.close();
                    if (inputStream!=null)
                        inputStream.close();
                    if (urlConnection!=null)
                        urlConnection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (file.exists()) {
                    file.delete();
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            // 当前Video下载完成
            if(result.equals("succeeded")) {
                if(CustomSharedPreference.videosStatus.getDownloadDemoStatus().equals("Downloading")) {
                    CustomSharedPreference.videosStatus.setDownloadDemoStatus("Finish");
                }
                else if(CustomSharedPreference.videosStatus.getDownloadCountingStatus().equals("Downloading")) {
                    CustomSharedPreference.videosStatus.setDownloadCountingStatus("Finish");
                }
            }
            else {
                if(CustomSharedPreference.videosStatus.getDownloadDemoStatus().equals("Downloading")) {
                    CustomSharedPreference.videosStatus.setDownloadDemoStatus("Failed");
                }
                else if(CustomSharedPreference.videosStatus.getDownloadCountingStatus().equals("Downloading")) {
                    CustomSharedPreference.videosStatus.setDownloadCountingStatus("Failed");
                }
            }

            // 如果全部Video都下载完成
            if(CustomSharedPreference.videosStatus.getDownloadDemoStatus().equals("Finish") &&
                    CustomSharedPreference.videosStatus.getDownloadCountingStatus().equals("Finish")) {
                notificationBuilder.setContentText("Download finished")
                        .setProgress(progressMax, progressMax, false)
                        .setOngoing(false);
                notificationManager.notify(1, notificationBuilder.build());
                // 移除定時器
                handler.removeCallbacksAndMessages(null);
            }
            // 如果第一个video成功第二个video准备开始
            else if(CustomSharedPreference.videosStatus.getDownloadDemoStatus().equals("Finish") &&
                    CustomSharedPreference.videosStatus.getDownloadCountingStatus().equals("Ready")) {
                downloadCounting();
            }
            // 如果第一个video失败
            else if(CustomSharedPreference.videosStatus.getDownloadDemoStatus().equals("Failed")) {
                notificationBuilder.setContentText("Download failed")
                        .setProgress(progressMax, progressMax, false)
                        .setOngoing(false);
                notificationManager.notify(1, notificationBuilder.build());
                // 移除定時器
                handler.removeCallbacksAndMessages(null);
            }
            // 如果第一个video成功第二个video失败
            else if(CustomSharedPreference.videosStatus.getDownloadDemoStatus().equals("Finish") &&
                    CustomSharedPreference.videosStatus.getDownloadCountingStatus().equals("Failed")) {
                notificationBuilder.setContentText("Download failed")
                        .setProgress(progressMax, progressMax, false)
                        .setOngoing(false);
                notificationManager.notify(1, notificationBuilder.build());
                // 移除定時器
                handler.removeCallbacksAndMessages(null);
            }
            else {
                notificationBuilder.setContentText("Download failed")
                        .setProgress(progressMax, progressMax, false)
                        .setOngoing(false);
                notificationManager.notify(1, notificationBuilder.build());
                // 移除定時器
                handler.removeCallbacksAndMessages(null);
            }
        }
    }
}