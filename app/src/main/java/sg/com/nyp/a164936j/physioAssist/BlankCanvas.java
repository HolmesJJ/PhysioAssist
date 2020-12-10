package sg.com.nyp.a164936j.physioAssist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;
import org.json.JSONStringer;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import sg.com.nyp.a164936j.physioAssist.configuration.Config;
import sg.com.nyp.a164936j.physioAssist.configuration.GetSetSharedPreferences;
import sg.com.nyp.a164936j.physioAssist.customadapters.CustomPatientLeaderBoardSendGiftAdapter;
import sg.com.nyp.a164936j.physioAssist.fragments.fragmentspatient.PatientChallenge;
import sg.com.nyp.a164936j.physioAssist.fragments.fragmentspatient.PatientExercise;
import sg.com.nyp.a164936j.physioAssist.fragments.fragmentspatient.PatientLeaderBoard;
import sg.com.nyp.a164936j.physioAssist.fragments.fragmentspatient.PatientProgress;
import sg.com.nyp.a164936j.physioAssist.fragments.interfaces.CustomOnClickListener;
import sg.com.nyp.a164936j.physioAssist.fragments.interfaces.OnTaskCompleted;
import sg.com.nyp.a164936j.physioAssist.httpasynctask.PostForm;
import sg.com.nyp.a164936j.physioAssist.httpasynctask.PostFormAuth;
import sg.com.nyp.a164936j.physioAssist.language.LocaleHelper;

public class BlankCanvas extends AppCompatActivity implements CustomOnClickListener, OnTaskCompleted {

    private static final int SEND_GIFT_TASK_ID = 1;
    private static final int REFRESH_TOKEN_TASK_ID = 2;

    private int[] achievementImages;

    private Resources resources;
    private RelativeLayout frameLayout;
    private CustomPatientLeaderBoardSendGiftAdapter adapter;
    private Dialog dialog;
    private TextView giftTitle;
    private TextView giftSubTitle;
    private GridView gridView;
    private Button btnSendGift;
    private Button btn_back, btn_send_gift;
    private ProgressBar mProgressView;

    private String language;
    private String btnType;
    private int imgSelected;

    private int giftTypeId;
    private String stayId;
    private String status = "failed";
    private String access_token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank_canvas);
        initView();

        Intent intent = getIntent();
        btnType = intent.getExtras().getString("BTNTYPE");

        stayId = GetSetSharedPreferences.getDefaults("stayId", getApplicationContext());
        language = GetSetSharedPreferences.getDefaults("language", getApplicationContext());

        resources = updateViews(BlankCanvas.this, language);
        initLanguage();

        btn_send_gift.setVisibility(View.GONE);
        btn_back.setOnClickListener((View view) -> {
            Log.d(Config.TAG_BUTTON, "(BlankCanvas) btnBack");
            onBackPressed();
        });

        FragmentManager manager = getSupportFragmentManager();

        switch (btnType){
            case "exercise":
                Log.d(Config.TAG_BUTTON, "(BlankCanvas) btnExercise");

                PatientExercise patientExercise = new PatientExercise();
                manager.beginTransaction()
                        .replace(R.id.fragment_container_blankcanvas, patientExercise)
                        .addToBackStack("tag")
                        .commit();
                break;
            case "challenge":
                Log.d(Config.TAG_BUTTON, "(BlankCanvas) btnChallenge");

                PatientChallenge patientChallenge = new PatientChallenge();
                manager.beginTransaction()
                        .replace(R.id.fragment_container_blankcanvas, patientChallenge)
                        .addToBackStack("tag")
                        .commit();
                break;
            case "leaderboard":
                Log.d(Config.TAG_BUTTON, "(BlankCanvas) btnLeaderboard");

                PatientLeaderBoard patientLeaderBoard = new PatientLeaderBoard();
                frameLayout.setBackground(getDrawable(R.drawable.background2));
                btn_send_gift.setVisibility(View.VISIBLE);
                btn_send_gift.setOnClickListener((View view) -> {
                    generateDummyData();
                    displayDialog(R.layout.dialog_send_gift, achievementImages, resources.getString(R.string.patient_btn_send_gift));
                    imgSelected = -1;
                    Log.d(Config.TAG_BUTTON,"(BlankCanvas) Send Gift Btn clicked");
                });
                manager.beginTransaction()
                        .replace(R.id.fragment_container_blankcanvas, patientLeaderBoard)
                        .addToBackStack("tag")
                        .commit();
                break;
            case "progress":
                Log.d(Config.TAG_BUTTON, "(BlankCanvas) btnProgress");

                PatientProgress patientProgress = new PatientProgress();
                frameLayout.setBackground(getDrawable(R.drawable.background2));
                manager.beginTransaction()
                        .replace(R.id.fragment_container_blankcanvas, patientProgress)
                        .addToBackStack("tag")
                        .commit();
                break;
        }

    }

    private void initView() {
        frameLayout = findViewById(R.id.blank_canvas);
        btn_back = findViewById(R.id.blank_canvas_btn_back);
        btn_send_gift = findViewById(R.id.patient_btn_send_gift);
    }

    private void initLanguage() {
        btn_send_gift.setText(resources.getString(R.string.patient_btn_send_gift));
    }

    @Override
    public void onBackPressed() {

        finish();
    }

    public void generateDummyData(){
        achievementImages = new int[]{
                R.drawable.achievement_well_done,
                R.drawable.achievement_good_job,
                R.drawable.achievement_nice,
                R.drawable.achievement_great,
        };
    }

    @Override
    public void onThumbnailClick(int parentId, int position) {
        //Not used
    }

    @Override
    public void onStartExerciseClick(int parentId, int position) {

    }

    @Override
    public void onAchievementImgClick(int position) {
        imgSelected = position;
    }

    @Override
    public void onTrophyImgClick(int position) {
        Log.d(Config.TAG_BUTTON, "(BlankCanvas) onTrophyImgClick");

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

    private void displayDialog(int layoutResId, int[] data, String dTitle){
        dialog = new Dialog(BlankCanvas.this);
        switch (layoutResId){
            case R.layout.dialog_send_gift:

                dialog.setContentView(layoutResId);
                dialog.setTitle(dTitle);

                giftTitle = dialog.findViewById(R.id.patient_gift_title);
                giftTitle.setText(resources.getString(R.string.patient_gift_title));

                giftSubTitle = dialog.findViewById(R.id.patient_gift_subTitle);
                giftSubTitle.setText(resources.getString(R.string.patient_gift_subTitle));

                gridView = dialog.findViewById(R.id.send_gift_grid_item_image);

                mProgressView = dialog.findViewById(R.id.loading_progress);

                adapter = new CustomPatientLeaderBoardSendGiftAdapter(BlankCanvas.this, data);
                adapter.setBtnSendGiftClick(this);

                btnSendGift = dialog.findViewById(R.id.btn_send_gift);
                btnSendGift.setText(resources.getString(R.string.btn_send_gift));
                btnSendGift.setOnClickListener((View view)-> {
                    if(GetSetSharedPreferences.getDefaults("sendGiftByStayId", getApplicationContext()) != null) {
                        //TODO: Send data to server
                        switch (imgSelected){
                            case 0:
                                giftTypeId = 1;
                                Log.d(Config.TAG_BUTTON, "(BlankCanvas) send 'Well Done Achievement' to server");
                                break;
                            case 1:
                                giftTypeId = 2;
                                Log.d(Config.TAG_BUTTON, "(BlankCanvas) send 'Good Job Achievement' to server");
                                break;
                            case 2:
                                giftTypeId = 5;
                                Log.d(Config.TAG_BUTTON, "(BlankCanvas) send 'Nice Achievement' to server");
                                break;
                            case 3:
                                giftTypeId = 4;
                                Log.d(Config.TAG_BUTTON, "(BlankCanvas) send 'Great Achievement' to server");
                                break;
                        }
                        sendGift();
                        showProgress(true);
                    }
                    else {
                        CustomToast.show(getApplicationContext(), "Please choose one patient");
                    }
                });
                break;
        }
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        gridView.setAdapter(adapter);
        dialog.show();
    }

    public Resources updateViews(Context context, String languageCode) {
        context = LocaleHelper.setLocale(this, languageCode);
        return context.getResources();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    private void sendGift(){
        String accessToken = GetSetSharedPreferences.getDefaults("access_token", getApplicationContext());
        PostFormAuth getPatientLeaderBoardTask = new PostFormAuth(BlankCanvas.this, SEND_GIFT_TASK_ID);
        getPatientLeaderBoardTask.execute("http://" + IPAddress.awsipaddress + "/PhysioWebPortal/api/giftreceived", convertToJSON(), accessToken);
    }

    public String convertToJSON() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date currentTime = new Date(System.currentTimeMillis());
        String ReceivedDateTime = dateFormat.format(currentTime);

        int stayIdInteger = Integer.valueOf(GetSetSharedPreferences.getDefaults("sendGiftByStayId", getApplicationContext()));

        JSONStringer jsonText = new JSONStringer();
        try {
            jsonText.object();
            jsonText.key("StayId");
            jsonText.value(stayIdInteger);
            jsonText.key("GiftTypeId");
            jsonText.value(giftTypeId);
            jsonText.key("ReceivedDateTime");
            jsonText.value(ReceivedDateTime);
            jsonText.key("GivenById");
            jsonText.value("whoever is kind enough to send a gift");
            jsonText.endObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonText.toString();
    }

    private void refreshToken() {
        String parameters = getParameters();
        PostForm refreshTokenTask = new PostForm(BlankCanvas.this, REFRESH_TOKEN_TASK_ID);
        refreshTokenTask.execute("http://" + IPAddress.awsipaddress + "/PhysioWebPortal/token", parameters);
    }

    // Convert information to JSON string
    public String getParameters() {
        String parameters = "";
        try {
            String username = GetSetSharedPreferences.getDefaults("username", getApplicationContext());
            String password = GetSetSharedPreferences.getDefaults("password", getApplicationContext());
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
    public void onTaskCompleted(String response, int requestId) {
        if(response.contains("<html")) {
            refreshToken();
        } else {
            if(requestId == SEND_GIFT_TASK_ID) {
                GetSetSharedPreferences.removeDefaults("sendGiftByStayId", getApplicationContext());
                CustomToast.show(getApplicationContext(), "Sent!");
                showProgress(false);
            }
            else if(requestId == REFRESH_TOKEN_TASK_ID) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    access_token = jsonObject.getString("access_token");
                    GetSetSharedPreferences.setDefaults("access_token", access_token, getApplicationContext());
                    sendGift();
                } catch (Exception e) {
                    e.printStackTrace();
                    GetSetSharedPreferences.removeDefaults("username", getApplicationContext());
                    GetSetSharedPreferences.removeDefaults("password", getApplicationContext());
                    GetSetSharedPreferences.removeDefaults("access_token", getApplicationContext());
                    showProgress(false);
                    finish();
                }
            }
        }
    }
}
