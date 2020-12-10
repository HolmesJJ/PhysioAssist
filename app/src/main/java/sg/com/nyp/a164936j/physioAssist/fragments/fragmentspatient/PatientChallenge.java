package sg.com.nyp.a164936j.physioAssist.fragments.fragmentspatient;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;

import sg.com.nyp.a164936j.physioAssist.BlankCanvas;
import sg.com.nyp.a164936j.physioAssist.IPAddress;
import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.configuration.Config;
import sg.com.nyp.a164936j.physioAssist.configuration.GetSetSharedPreferences;
import sg.com.nyp.a164936j.physioAssist.customadapters.RecyclerViewPatientChallengeAdapter;
import sg.com.nyp.a164936j.physioAssist.fragments.interfaces.CustomOnClickListener;
import sg.com.nyp.a164936j.physioAssist.fragments.interfaces.OnTaskCompleted;
import sg.com.nyp.a164936j.physioAssist.httpasynctask.GetAuth;
import sg.com.nyp.a164936j.physioAssist.httpasynctask.PostForm;

/**
 * A simple {@link Fragment} subclass.
 */
public class PatientChallenge extends Fragment implements CustomOnClickListener, RecyclerViewPatientChallengeAdapter.ItemClickListener, OnTaskCompleted {

    private static final int GET_PATIENT_CHALLENGE_TASK_ID = 1;
    private static final int REFRESH_TOKEN_TASK_ID = 2;

    private Context context;
    private Resources resources;
    private TextView challengeHeader;
    private RecyclerView recyclerView;
    private RecyclerViewPatientChallengeAdapter adapter;
    private Dialog dialog;
    private TextView achievementDesc;
    private ProgressBar mProgressView;

    private String language;
    private String patientId;
    private String status = "failed";
    private String access_token;

    //vars
    private String[][] twoDArray;

    public PatientChallenge() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.patient_challenge_gridlayout, container, false);
        initView(rootView);

        patientId = GetSetSharedPreferences.getDefaults("patientId", getActivity().getApplicationContext());
        language = GetSetSharedPreferences.getDefaults("language", getActivity().getApplicationContext());

        BlankCanvas blankCanvas = (BlankCanvas) getActivity();
        resources = blankCanvas.updateViews(context, language);
        initLanguage();

        // Set up the RecyclerView
        int numOfColumn = 5;
        recyclerView.setLayoutManager(new GridLayoutManager(context, numOfColumn));

        getPatientChallenge();
        showProgress(true);

        return rootView;
    }

    private void initView(View rootView) {
        context = getContext();
        recyclerView = rootView.findViewById(R.id.grid_recyclerView);
        challengeHeader = rootView.findViewById(R.id.patient_grid_codename_header);
        mProgressView = rootView.findViewById(R.id.loading_progress);
    }

    private void initLanguage() {
        challengeHeader.setText(resources.getString(R.string.patient_grid_codename_header));
    }

    private void getPatientChallenge(){
        String accessToken = GetSetSharedPreferences.getDefaults("access_token", getActivity().getApplicationContext());
        GetAuth getPatientChallengeTask = new GetAuth(PatientChallenge.this, GET_PATIENT_CHALLENGE_TASK_ID);
        getPatientChallengeTask.execute("http://" + IPAddress.awsipaddress + "/PhysioWebPortal/api/patients/" + patientId + "/prescribed", accessToken);
    }

    private void refreshToken() {
        String parameters = getParameters();
        PostForm refreshTokenTask = new PostForm(PatientChallenge.this, REFRESH_TOKEN_TASK_ID);
        refreshTokenTask.execute("http://" + IPAddress.awsipaddress + "/PhysioWebPortal/token", parameters);
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

    public void generateDummyData(){

        twoDArray = new String[][]{
                {
                        "Challenge_Desc_1",
                        "Challenge_Desc_2",
                        "Challenge_Desc_3",
                        "Challenge_Desc_4",
                        "Challenge_Desc_5",

                        "Challenge_Desc_6",
                        "Challenge_Desc_7",
                        "Challenge_Desc_8",
                        "Challenge_Desc_9",
                        "Challenge_Desc_10",

                        "Challenge_Desc_11",
                        "Challenge_Desc_12",
                        "Challenge_Desc_13",
                        "Challenge_Desc_14",
                        "Challenge_Desc_15",

                        "Challenge_Desc_16",
                        "Challenge_Desc_17",
                        "Challenge_Desc_18",
                        "Challenge_Desc_19",
                        "Challenge_Desc_20",
                },
                {
                        "checked",
                        "checked",
                        "uncheck",
                        "checked",
                        "checked",

                        "checked",
                        "uncheck",
                        "checked",
                        "checked",
                        "checked",

                        "uncheck",
                        "uncheck",
                        "uncheck",
                        "uncheck",
                        "uncheck",

                        "uncheck",
                        "uncheck",
                        "uncheck",
                        "uncheck",
                        "uncheck",
                }
        };

    }

    @Override
    public void onThumbnailClick(int parentId, int position) {

    }

    @Override
    public void onStartExerciseClick(int parentId, int position) {

    }

    @Override
    public void onAchievementImgClick(int position) {

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

    @Override
    public void onTrophyImgClick(int position) {
        Log.d(Config.TAG_BUTTON, "(PatientChallenge) onTrophyImgClick:" + position + " [listen from parent]");

        //Create custom dialog object
        dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_achievement_info);
        dialog.setTitle("Achievement Info");
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        achievementDesc = dialog.findViewById(R.id.achievement_desc);
        achievementDesc.setText(twoDArray[0][position]);

        dialog.show();
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.d(Config.TAG_BUTTON,"(PatientChallenge) Recycler View pos:" + adapter.getItem(position));
        onTrophyImgClick(position);
    }

    @Override
    public void onTaskCompleted(String response, int requestId) {
        if(response.contains("<html")) {
            refreshToken();
        } else {
            if(requestId == GET_PATIENT_CHALLENGE_TASK_ID) {
                boolean isJSONArray = false;
                boolean isJSONObject = false;
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    isJSONObject = true;
                    status = "succeeded";
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    isJSONArray = true;
                    for(int i=0; i<jsonArray.length(); i++) {
                        JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                    }
                    status = "succeeded";
                } catch (Exception e) {
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

                System.out.println(response);

                // Generate dummy data
                generateDummyData();

                adapter = new RecyclerViewPatientChallengeAdapter(getContext(), twoDArray);
                adapter.setClickListener(this);
                recyclerView.setAdapter(adapter);
            }
            else if(requestId == REFRESH_TOKEN_TASK_ID) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    access_token = jsonObject.getString("access_token");
                    GetSetSharedPreferences.setDefaults("access_token", access_token, getActivity().getApplicationContext());
                    getPatientChallenge();
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
}
