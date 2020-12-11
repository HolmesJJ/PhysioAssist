package sg.com.nyp.a164936j.physioAssist.fragments.fragmentspatient;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import sg.com.nyp.a164936j.physioAssist.BlankCanvas;
import sg.com.nyp.a164936j.physioAssist.CustomToast;
import sg.com.nyp.a164936j.physioAssist.IPAddress;
import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.configuration.Config;
import sg.com.nyp.a164936j.physioAssist.configuration.CustomSharedPreference;
import sg.com.nyp.a164936j.physioAssist.configuration.GetSetSharedPreferences;
import sg.com.nyp.a164936j.physioAssist.customadapters.CustomPatientExerciseAdapter;
import sg.com.nyp.a164936j.physioAssist.customview.CustomCircleLoading;
import sg.com.nyp.a164936j.physioAssist.fragments.interfaces.CustomOnClickListener;
import sg.com.nyp.a164936j.physioAssist.fragments.interfaces.OnTaskCompleted;
import sg.com.nyp.a164936j.physioAssist.fragments.service.DownloadFileService;
import sg.com.nyp.a164936j.physioAssist.httpasynctask.GetAuth;
import sg.com.nyp.a164936j.physioAssist.httpasynctask.PostForm;
import sg.com.nyp.a164936j.physioAssist.models.VideoStatus;
import sg.com.nyp.a164936j.physioAssist.notch.NotchActivity;
import sg.com.nyp.a164936j.physioAssist.notch.visualiser.VisualiserActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class PatientExercise extends Fragment implements CustomOnClickListener, OnTaskCompleted {

    private static final int THUMBNAIL_TASK_ID = 1;
    private static final int REFRESH_TOKEN_TASK_ID = 2;

    private Context context;
    private Resources resources;
    private ListView lv1, lv2;
    private CustomCircleLoading circleLoading;
    private TextView exerciseHeader;
    private TextView exerciseSubHeader;
    private TextView exerciseTypeHeader1;
    private TextView exerciseTypeHeader2;
    private ProgressBar mProgressView;
    private ImageView thumbnail;
    private CustomPatientExerciseAdapter adapter;
    private AlertDialog rncDialog, pbvDialog;
    private VisualiserActivity mVisualiserActivity;
    private Intent downloadIntent;
    private File videoFolder;
    private File demoFolder;
    private File countingFolder;
    private File recordFolder;

    //vars
    private List<Integer> exerciseId1 = new ArrayList<>();
    private List<Integer> prescribedExId1 = new ArrayList<>();
    private List<String> exerciseName1 = new ArrayList<>();
    private List<Integer> ExType1 = new ArrayList<>();
    private List<Integer> ExSetNo1 = new ArrayList<>();
    private List<Integer> ExRepNo1 = new ArrayList<>();
    private List<Integer> ExTimePerDay1 = new ArrayList<>();
    private List<String> exerciseThumbnail1 = new ArrayList<>();
    private List<Integer> exerciseId2 = new ArrayList<>();
    private List<Integer> prescribedExId2 = new ArrayList<>();
    private List<String> exerciseName2 = new ArrayList<>();
    private List<Integer> ExType2 = new ArrayList<>();
    private List<Integer> ExSetNo2 = new ArrayList<>();
    private List<Integer> ExRepNo2 = new ArrayList<>();
    private List<Integer> ExTimePerDay2 = new ArrayList<>();
    private List<String> exerciseThumbnail2 = new ArrayList<>();

    //Video
    private VideoView videoView;
    private static Dialog dialog;
    private Button btnQuit, btnSkip;
    private File selectedVideo;

    private String stayId;
    private String language;
    private String status = "failed";
    private String access_token;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            // 如果有视频在下载
            if(CustomSharedPreference.videosStatus != null) {

                int videoProgress = CustomSharedPreference.videosStatus.getVideoProgress();
                int parentId = CustomSharedPreference.videosStatus.getParentId();
                int position = CustomSharedPreference.videosStatus.getPosition();

                if(parentId == 2131296504){
                    circleLoading = getViewByPosition(position, lv1).findViewById(R.id.circleLoading);
                    thumbnail = getViewByPosition(position, lv1).findViewById(R.id.exercise_thumbnail);
                } else {
                    circleLoading = getViewByPosition(position, lv2).findViewById(R.id.circleLoading);
                    thumbnail = getViewByPosition(position, lv2).findViewById(R.id.exercise_thumbnail);
                }
                // 如果第一个视频在下载
                if(CustomSharedPreference.videosStatus.getDownloadDemoStatus().equals("Downloading") &&
                        CustomSharedPreference.videosStatus.getDownloadCountingStatus().equals("Ready")) {
                    if(circleLoading.getStatus() == CustomCircleLoading.Status.End) {
                        circleLoading.setStatus(CustomCircleLoading.Status.Starting);
                    }
                    circleLoading.animatorAngle((float)(videoProgress*0.01*0.5));
                }
                // 如果第二个视频在下载
                else if(CustomSharedPreference.videosStatus.getDownloadDemoStatus().equals("Finish") &&
                        CustomSharedPreference.videosStatus.getDownloadCountingStatus().equals("Ready")) {
                    if(circleLoading.getStatus() == CustomCircleLoading.Status.End) {
                        circleLoading.setStatus(CustomCircleLoading.Status.Starting);
                    }
                    circleLoading.animatorAngle((float)(0.5 + videoProgress*0.01*0.5));
                }
                // 如果第二个视频在下载
                else if(CustomSharedPreference.videosStatus.getDownloadDemoStatus().equals("Finish") &&
                        CustomSharedPreference.videosStatus.getDownloadCountingStatus().equals("Downloading")) {
                    if(circleLoading.getStatus() == CustomCircleLoading.Status.End) {
                        circleLoading.setStatus(CustomCircleLoading.Status.Starting);
                    }
                    circleLoading.animatorAngle((float)(0.5 + videoProgress*0.01*0.5));
                }
                // 如果第一个视频失败
                else if(CustomSharedPreference.videosStatus.getDownloadDemoStatus().equals("Failed")){
                    if(circleLoading.getStatus() != CustomCircleLoading.Status.End) {
                        circleLoading.setStatus(CustomCircleLoading.Status.End);
                        circleLoading.init();
                    }
                    CustomSharedPreference.videosStatus = null;
                    // 停止下载
                    getActivity().stopService(downloadIntent);
                    getActivity().runOnUiThread(() -> {
                        CustomToast.show(getActivity(), "Download failed");
                    });
                }
                // 如果第二个视频失败
                else if(CustomSharedPreference.videosStatus.getDownloadDemoStatus().equals("Finish") &&
                        CustomSharedPreference.videosStatus.getDownloadCountingStatus().equals("Failed")) {
                    if(circleLoading.getStatus() != CustomCircleLoading.Status.End) {
                        circleLoading.setStatus(CustomCircleLoading.Status.End);
                        circleLoading.init();
                    }
                    CustomSharedPreference.videosStatus = null;
                    // 停止下载
                    getActivity().stopService(downloadIntent);
                    getActivity().runOnUiThread(() -> {
                        CustomToast.show(getActivity(), "Download failed");
                    });
                }
                // 如果下载成功
                else if(CustomSharedPreference.videosStatus.getDownloadDemoStatus().equals("Finish") &&
                        CustomSharedPreference.videosStatus.getDownloadCountingStatus().equals("Finish")) {
                    if(circleLoading.getStatus() != CustomCircleLoading.Status.End) {
                        circleLoading.setStatus(CustomCircleLoading.Status.End);
                        circleLoading.init();
                    }
                    thumbnail.setColorFilter(Color.argb(0, 255, 255, 255));
                    CustomSharedPreference.videosStatus = null;
                    // 停止下载
                    getActivity().stopService(downloadIntent);
                    getActivity().runOnUiThread(() -> {
                        CustomToast.show(getActivity(), "Download succeeded");
                    });
                }
            }
            handler.postDelayed(progressRunnable, 500);
        }
    };

    public PatientExercise() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_patient_exercise, container, false);
        initView(rootView);

        language = GetSetSharedPreferences.getDefaults("language", getActivity().getApplicationContext());
        stayId = GetSetSharedPreferences.getDefaults("stayId", getActivity().getApplicationContext());

        BlankCanvas blankCanvas = (BlankCanvas) getActivity();
        resources = blankCanvas.updateViews(context, language);
        initLanguage();

        // 创建文件夹
        videoFolder = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+ "/PhysioAssist/Video");
        if (!videoFolder.exists()) {
            videoFolder.mkdirs();
        }
        demoFolder = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+ "/PhysioAssist/Video/Demo");
        if (!demoFolder.exists()) {
            demoFolder.mkdirs();
        }
        countingFolder = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+ "/PhysioAssist/Video/Counting");
        if (!countingFolder.exists()) {
            countingFolder.mkdirs();
        }
        recordFolder = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+ "/PhysioAssist/Record");
        if (!recordFolder.exists()) {
            recordFolder.mkdirs();
        }

        showProgress(true);

        getThumbnail();

        return rootView;
    }

    private void initView(View rootView) {
        context = getContext();
        exerciseHeader = rootView.findViewById(R.id.patient_exercise_header);
        exerciseSubHeader = rootView.findViewById(R.id.patient_exercise_sub_header);
        exerciseTypeHeader1 = rootView.findViewById(R.id.patient_exercise_type_header1);
        exerciseTypeHeader2 = rootView.findViewById(R.id.patient_exercise_type_header2);
        lv1 = rootView.findViewById(R.id.patient_customListView1);
        lv2 = rootView.findViewById(R.id.patient_customListView2);
        mProgressView = rootView.findViewById(R.id.exercise_progress);
    }

    private void initLanguage() {
        exerciseHeader.setText(resources.getString(R.string.patient_exercise_header));
        exerciseSubHeader.setText(resources.getString(R.string.patient_exercise_sub_header));
        exerciseTypeHeader1.setText(resources.getString(R.string.patient_exercise_type_header1));
        exerciseTypeHeader2.setText(resources.getString(R.string.patient_exercise_type_header2));
    }

    private void getThumbnail(){
        String accessToken = GetSetSharedPreferences.getDefaults("access_token", getActivity().getApplicationContext());
        GetAuth getAuthThumbnailTask = new GetAuth(PatientExercise.this, THUMBNAIL_TASK_ID);
        getAuthThumbnailTask.execute("http://" + IPAddress.ipaddress + "/api/hospitalstay/" + stayId + "/prescribed", accessToken);
    }

    private void refreshToken() {
        String parameters = getParameters();
        PostForm refreshTokenTask = new PostForm(PatientExercise.this, REFRESH_TOKEN_TASK_ID);
        refreshTokenTask.execute("http://" + IPAddress.ipaddress + "/token", parameters);
    }

    // Convert information to JSON string
    public String getParameters() {
        String parameters = "";
        try {
            String username = GetSetSharedPreferences.getDefaults("username", getActivity().getApplicationContext());
            String password = GetSetSharedPreferences.getDefaults("password", getActivity().getApplicationContext());
            String firstParameter = "username=" + URLEncoder.encode(username, "UTF-8");
            String secondParameter = "password=" + URLEncoder.encode(password, "UTF-8");
            String thirdParameter = "grant_type=" + URLEncoder.encode("password", "UTF-8");
            parameters = firstParameter + "&&" + secondParameter + "&&" + thirdParameter;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parameters;
    }

    @Override
    public void onThumbnailClick(int parentId, int position) {
        System.out.println("position: " + position);
        System.out.println("parentId: " + parentId);

        int exerciseId;
        String exerciseName;
        // 如果是第一列
        if(parentId == 2131296504) {
            exerciseId = exerciseId1.get(position);
            exerciseName = exerciseName1.get(position);
        }
        // 如果是第二列
        else {
            exerciseId = exerciseId2.get(position);
            exerciseName = exerciseName2.get(position);
        }

        // 判断你所点击的video是否已经下载
        boolean isDemoFileExist = false;
        File[] demoFolderChildren = demoFolder.listFiles();
        for (int i = 0; i < demoFolderChildren.length; i++)  {
            if(demoFolderChildren[i].getName().equals(exerciseId + "_demo_" +  language + ".mp4")) {
                isDemoFileExist = true;
                break;
            }
        }

        boolean isCountingFileExist = false;
        File[] countingFolderChildren = countingFolder.listFiles();
        for (int i = 0; i < countingFolderChildren.length; i++)  {
            if(countingFolderChildren[i].getName().equals(exerciseId + "_counting_" +  language + ".mp4")) {
                isCountingFileExist = true;
                break;
            }
        }

        // 如果video不存在
        if(!isDemoFileExist || !isCountingFileExist) {
            boolean isVideoDownloading = false;
            if(CustomSharedPreference.videosStatus != null) {
                isVideoDownloading = true;
            }

            // 如果video当前正在下载
            if(isVideoDownloading) {
                CustomToast.show(getActivity(), "Video is downloading...");
            }
            // 如果video开始下载
            else {
                CustomSharedPreference.videosStatus = new VideoStatus(exerciseId, exerciseName, parentId, position);
                downloadIntent = new Intent(getActivity(), DownloadFileService.class);
                getActivity().startService(downloadIntent);
                CustomToast.show(getActivity(), "Video is added to the queue...");
            }
        }
        // 如果video存在
        else {
            String videoURL = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+ "/PhysioAssist/Video/Demo/" + exerciseId + "_demo_" +  language + ".mp4";
            videoScreen(videoURL);
        }
    }

    @Override
    public void onStartExerciseClick(int parentId, int position) {
        System.out.println("position: " + position);
        System.out.println("parentId: " + parentId);

        int exerciseId;
        int prescribedExId;
        String exerciseName;
        int exerciseType;
        int exerciseSetNo;
        int exerciseRepNo;
        int exTimePerDay;

        // 如果是第一列
        if(parentId == 2131296504) {
            exerciseId = exerciseId1.get(position);
            prescribedExId = prescribedExId1.get(position);
            exerciseName = exerciseName1.get(position);
            exerciseType = ExType1.get(position);
            exerciseSetNo = ExSetNo1.get(position);
            exerciseRepNo = ExRepNo1.get(position);
            exTimePerDay = ExTimePerDay1.get(position);
        }
        // 如果是第二列
        else {
            exerciseId = exerciseId2.get(position);
            prescribedExId = prescribedExId2.get(position);
            exerciseName = exerciseName2.get(position);
            exerciseType = ExType2.get(position);
            exerciseSetNo = ExSetNo2.get(position);
            exerciseRepNo = ExRepNo2.get(position);
            exTimePerDay = ExTimePerDay2.get(position);
        }

        // 创建文件夹保存记录
        File recordSubFolder = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+ "/PhysioAssist/Record/" + prescribedExId);
        if (!recordSubFolder.exists()) {
            recordSubFolder.mkdirs();
        }

        // 判断你所点击的video是否已经下载
        boolean isDemoFileExist = false;
        File[] demoFolderChildren = demoFolder.listFiles();
        for (int i = 0; i < demoFolderChildren.length; i++)  {
            if(demoFolderChildren[i].getName().equals(exerciseId + "_demo_" +  language + ".mp4")) {
                isDemoFileExist = true;
                break;
            }
        }
        boolean isCountingFileExist = false;
        File[] countingFolderChildren = countingFolder.listFiles();
        for (int i = 0; i < countingFolderChildren.length; i++)  {
            if(countingFolderChildren[i].getName().equals(exerciseId + "_counting_" +  language + ".mp4")) {
                isCountingFileExist = true;
                break;
            }
        }

        // 如果video不存在
        if(!isDemoFileExist || !isCountingFileExist) {
            CustomToast.show(getActivity(), "Please download the video first");
        }
        else {
            remindNotchConnectionDialog(exerciseId, prescribedExId, exerciseName, exerciseType, exerciseSetNo, exerciseRepNo, exTimePerDay);
            rncDialog.show();
        }
    }

    @Override
    public void onAchievementImgClick(int position) {

    }

    @Override
    public void onTrophyImgClick(int position) {

    }

    private void remindNotchConnectionDialog(int exerciseId, int prescribedExId, String exerciseName, int exerciseType, int exerciseSetNo, int exerciseRepNo, int exTimePerDay) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Notch:").setMessage("Please turn on all the notches");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getActivity(), NotchActivity.class);
                intent.putExtra("exerciseId", exerciseId);
                intent.putExtra("prescribedExId", prescribedExId);
                intent.putExtra("exerciseName", exerciseName);
                intent.putExtra("exerciseType", exerciseType);
                intent.putExtra("exerciseSetNo", exerciseSetNo);
                intent.putExtra("exerciseRepNo", exerciseRepNo);
                intent.putExtra("exTimePerDay", exTimePerDay);
                startActivity(intent);
            }
        });

        builder.setNegativeButton("Cancel", null);

        rncDialog = builder.create();
    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void videoScreen(String videoUri){
        //Create custom dialog object
        dialog = new Dialog(getActivity());

        //Include dialog.xml
        dialog.setContentView(R.layout.dialog_video_screen);
        //Set dialog title
        dialog.setTitle("Exercise Demo");

        //Set values for custom dialog components
        videoView = dialog.findViewById(R.id.videoView1);
        new fetchVideo().execute(videoUri);

        btnSkip = dialog.findViewById(R.id.btnSkipVideo);
        btnQuit = dialog.findViewById(R.id.btnQuitVideo);

        btnSkip.setOnClickListener((View v) -> {
            //Close dialog
            dialog.dismiss();

            //Start new fragment
            Log.d(Config.TAG_BUTTON, "(PatientExercise) Skip Exercise");
            //TODO: Start fragment to start exercise
        });

        btnQuit.setOnClickListener((View v) -> {
            //close dialog
            Log.d(Config.TAG_BUTTON, "(PatientExercise) Quit Exercise");
            dialog.dismiss();
        });

    }

    private class fetchVideo extends AsyncTask<String, Void, Uri> {

        ProgressDialog progressDialog = new ProgressDialog(getActivity());

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Loading.. Please wait");
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Uri result) {
            //dismiss loading screen
            progressDialog.dismiss();

            //Play video
            videoView.setVideoURI(result);
            videoView.setZOrderOnTop(true);
            videoView.requestFocus();
            videoView.start();

            //Adjust dialogWindow (width x Height)
            Window dialogWindow = dialog.getWindow();
            WindowManager wm = getActivity().getWindowManager();
            Display d = wm.getDefaultDisplay();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();

            lp.height = (int) (d.getHeight() * 0.8f);
            lp.width = (int) (d.getWidth() * 0.9f);
            dialogWindow.setAttributes(lp);

            dialog.show();
        }

        @Override
        protected Uri doInBackground(String... params) {
            return Uri.parse(params[0]);
        }
    }

    @Override
    public void onTaskCompleted(String response, int requestId) {
        if(response.contains("<html")) {
            refreshToken();
        } else {
            if(requestId == THUMBNAIL_TASK_ID) {
                boolean isJSONArray = false;
                boolean isJSONObject = false;

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    isJSONObject = true;

                    // 清空数组
                    exerciseId1.clear();
                    exerciseId2.clear();
                    prescribedExId1.clear();
                    prescribedExId2.clear();
                    exerciseName1.clear();
                    exerciseName2.clear();
                    ExType1.clear();
                    ExType2.clear();
                    ExSetNo1.clear();
                    ExSetNo2.clear();
                    ExRepNo1.clear();
                    ExRepNo2.clear();
                    ExTimePerDay1.clear();
                    ExTimePerDay2.clear();
                    exerciseThumbnail1.clear();
                    exerciseThumbnail2.clear();

                    // 刷新数据库
                    if(jsonObject.getInt("ExType") == 1) {
                        exerciseId1.add(jsonObject.getInt("ExId"));
                        prescribedExId1.add(jsonObject.getInt("PEId"));
                        exerciseName1.add(jsonObject.getString("ExName"));
                        ExType1.add(jsonObject.getInt("ExType"));
                        ExSetNo1.add(jsonObject.getInt("ExSetNo"));
                        ExRepNo1.add(jsonObject.getInt("ExRepNo"));
                        ExTimePerDay1.add(jsonObject.getInt("ExTimePerDay"));
                        String imageURL = "http://" + IPAddress.ipaddress + "/Images/" + jsonObject.getString("VThumbnail") +".jpg";
                        exerciseThumbnail1.add(imageURL);
                    }
                    else if(jsonObject.getInt("ExType") == 2) {
                        exerciseId2.add(jsonObject.getInt("ExId"));
                        prescribedExId2.add(jsonObject.getInt("PEId"));
                        exerciseName2.add(jsonObject.getString("ExName"));
                        ExType2.add(jsonObject.getInt("ExType"));
                        ExSetNo2.add(jsonObject.getInt("ExSetNo"));
                        ExRepNo2.add(jsonObject.getInt("ExRepNo"));
                        ExTimePerDay2.add(jsonObject.getInt("ExTimePerDay"));
                        String imageURL = "http://" + IPAddress.ipaddress + "/Images/" + jsonObject.getString("VThumbnail") +".jpg";
                        exerciseThumbnail2.add(imageURL);
                    }

                    status = "succeeded";
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    JSONArray jsonArray = new JSONArray(response);
                    isJSONArray = true;

                    // 清空数组
                    exerciseId1.clear();
                    exerciseId2.clear();
                    prescribedExId1.clear();
                    prescribedExId2.clear();
                    exerciseName1.clear();
                    exerciseName2.clear();
                    ExType1.clear();
                    ExType2.clear();
                    ExSetNo1.clear();
                    ExSetNo2.clear();
                    ExRepNo1.clear();
                    ExRepNo2.clear();
                    ExTimePerDay1.clear();
                    ExTimePerDay2.clear();
                    exerciseThumbnail1.clear();
                    exerciseThumbnail2.clear();

                    // 刷新数据库
                    for(int i=0; i<jsonArray.length(); i++) {
                        JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                        // 刷新数据库
                        if(jsonObject.getInt("ExId") == 1 || jsonObject.getInt("ExId") == 3 || jsonObject.getInt("ExId") == 8 || jsonObject.getInt("ExId") == 9) {
                            exerciseId1.add(jsonObject.getInt("ExId"));
                            prescribedExId1.add(jsonObject.getInt("PEId"));
                            exerciseName1.add(jsonObject.getString("ExName"));
                            ExType1.add(1);
                            ExSetNo1.add(jsonObject.getInt("ExSetNo"));
                            ExRepNo1.add(jsonObject.getInt("ExRepNo"));
                            ExTimePerDay1.add(jsonObject.getInt("ExTimePerDay"));
                            String imageURL = "http://" + IPAddress.ipaddress + "/Images/" + jsonObject.getString("VThumbnail") +".jpg";
                            exerciseThumbnail1.add(imageURL);
                        }
                        else {
                            exerciseId2.add(jsonObject.getInt("ExId"));
                            prescribedExId2.add(jsonObject.getInt("PEId"));
                            exerciseName2.add(jsonObject.getString("ExName"));
                            ExType2.add(2);
                            ExSetNo2.add(jsonObject.getInt("ExSetNo"));
                            ExRepNo2.add(jsonObject.getInt("ExRepNo"));
                            ExTimePerDay2.add(jsonObject.getInt("ExTimePerDay"));
                            String imageURL = "http://" + IPAddress.ipaddress + "/Images/" + jsonObject.getString("VThumbnail") +".jpg";
                            exerciseThumbnail2.add(imageURL);
                        }
                    }

                    status = "succeeded";
                } catch (Exception e){
                    e.printStackTrace();
                }

                if(isJSONArray || isJSONObject) {
                    if(status.equals("succeeded")) {
                        showProgress(false);
                    }
                }
                else if(!isJSONArray && !isJSONObject) {
                    status = "succeeded";
                    if(status.equals("succeeded")) {
                        showProgress(false);
                    }
                }

                adapter = new CustomPatientExerciseAdapter(context, resources, exerciseId1, prescribedExId1, exerciseName1, exerciseThumbnail1, ExTimePerDay1);
                lv1.setAdapter(adapter);
                adapter.setThumbnailClickListener(this);
                adapter = new CustomPatientExerciseAdapter(context, resources, exerciseId2, prescribedExId2, exerciseName2, exerciseThumbnail2, ExTimePerDay2);
                lv2.setAdapter(adapter);
                adapter.setThumbnailClickListener(this);

                handler.post(progressRunnable);
            }
            else if(requestId == REFRESH_TOKEN_TASK_ID) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    access_token = jsonObject.getString("access_token");
                    GetSetSharedPreferences.setDefaults("access_token", access_token, getActivity().getApplicationContext());
                    getThumbnail();
                } catch (Exception e) {
                    e.printStackTrace();
                    GetSetSharedPreferences.removeDefaults("username", getActivity().getApplicationContext());
                    GetSetSharedPreferences.removeDefaults("password", getActivity().getApplicationContext());
                    GetSetSharedPreferences.removeDefaults("access_token", getActivity().getApplicationContext());
                    showProgress(false);
                    getActivity().finish();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 移除定時器
        handler.removeCallbacksAndMessages(null);
    }
}
