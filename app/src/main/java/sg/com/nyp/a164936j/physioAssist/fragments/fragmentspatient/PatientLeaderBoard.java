package sg.com.nyp.a164936j.physioAssist.fragments.fragmentspatient;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import sg.com.nyp.a164936j.physioAssist.BlankCanvas;
import sg.com.nyp.a164936j.physioAssist.IPAddress;
import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.configuration.GetSetSharedPreferences;
import sg.com.nyp.a164936j.physioAssist.customadapters.CustomPatientLeaderBoardAdapter;
import sg.com.nyp.a164936j.physioAssist.fragments.Comparator.PatientPointComparator;
import sg.com.nyp.a164936j.physioAssist.fragments.interfaces.OnTaskCompleted;
import sg.com.nyp.a164936j.physioAssist.httpasynctask.GetAuth;
import sg.com.nyp.a164936j.physioAssist.httpasynctask.PostForm;
import sg.com.nyp.a164936j.physioAssist.models.Patient;

/**
 * A simple {@link Fragment} subclass.
 */

public class PatientLeaderBoard extends Fragment implements OnTaskCompleted{

    private static final int GET_PATIENT_LEADER_BOARD_TASK_ID = 1;
    private static final int REFRESH_TOKEN_TASK_ID = 2;

    //vars
    private String[] positions;
    private String[] codeNames;
    private int[] points;

    private Context context;
    private Resources resources;
    private ListView mListView;
    private TextView positionHeader;
    private TextView patientCodenameHeader;
    private TextView pointsHeader;
    private ProgressBar mProgressView;

    private List<Patient> mPatients = new ArrayList<>();

    private String language;
    private String stayId;
    private String status = "failed";
    private String access_token;

    public PatientLeaderBoard() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_patient_leaderboard, container, false);
        initView(rootView);

        stayId = GetSetSharedPreferences.getDefaults("stayId", getActivity().getApplicationContext());
        language = GetSetSharedPreferences.getDefaults("language", getActivity().getApplicationContext());

        BlankCanvas blankCanvas = (BlankCanvas) getActivity();
        resources = blankCanvas.updateViews(context, language);
        initLanguage();

        getPatientLeaderBoard();

        showProgress(true);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adpterView, View view, int position, long id) {
                for (int i = 0; i < mListView.getChildCount(); i++) {
                    if(position == i ){
                        mListView.getChildAt(i).setBackground(ContextCompat.getDrawable(context, R.drawable.fragment_layout_round_white_translucent2));
                        GetSetSharedPreferences.setDefaults("sendGiftByStayId", String.valueOf(mPatients.get(position).getStayId()), getActivity().getApplicationContext());
                    }else{
                        mListView.getChildAt(i).setBackground(ContextCompat.getDrawable(context, R.drawable.fragment_layout_round_white_translucent));
                    }
                    int paddingDp = 15;
                    float density = context.getResources().getDisplayMetrics().density;
                    int paddingPixel = (int)(paddingDp * density);
                    mListView.getChildAt(i).setPadding(0, paddingPixel, 0, paddingPixel);
                }
            }
        });

        return rootView;
    }

    private void initView(View rootView) {
        context = getContext();
        mListView = rootView.findViewById(R.id.customListView1);
        positionHeader = rootView.findViewById(R.id.patient_leaderboard_position_header);
        patientCodenameHeader = rootView.findViewById(R.id.patient_leaderboard_patient_codename_header);
        pointsHeader = rootView.findViewById(R.id.patient_leaderboard_points_header);
        mProgressView = rootView.findViewById(R.id.loading_progress);
    }

    private void initLanguage() {
        positionHeader.setText(resources.getString(R.string.patient_leaderboard_position_header));
        patientCodenameHeader.setText(resources.getString(R.string.patient_leaderboard_patient_codename_header));
        pointsHeader.setText(resources.getString(R.string.patient_leaderboard_points_header));
    }

    private void getPatientLeaderBoard(){
        String accessToken = GetSetSharedPreferences.getDefaults("access_token", getActivity().getApplicationContext());
        GetAuth getPatientLeaderBoardTask = new GetAuth(PatientLeaderBoard.this, GET_PATIENT_LEADER_BOARD_TASK_ID);
        getPatientLeaderBoardTask.execute("http://" + IPAddress.ipaddress + "/api/hospitalstay/" + stayId + "/performed", accessToken);
    }

    private void refreshToken() {
        String parameters = getParameters();
        PostForm refreshTokenTask = new PostForm(PatientLeaderBoard.this, REFRESH_TOKEN_TASK_ID);
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
    public void onTaskCompleted(String response, int requestId) {
        if(response.contains("<html")) {
            refreshToken();
        } else {
            if(requestId == GET_PATIENT_LEADER_BOARD_TASK_ID) {
                boolean isJSONArray = false;
                boolean isJSONObject = false;
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    isJSONObject = true;

                    String patientId = jsonObject.getString("PatientId");
                    int getStayId = jsonObject.getInt("StayId");
                    String patientCodeName = jsonObject.getString("PatientCodeName");
                    int score = jsonObject.getInt("Score");

                    mPatients.add(new Patient(patientId, getStayId, patientCodeName, score));
                    status = "succeeded";
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    isJSONArray = true;
                    for(int i=0; i<jsonArray.length(); i++) {
                        JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                        String patientId = jsonObject.getString("PatientId");
                        int getStayId = jsonObject.getInt("StayId");
                        String patientCodeName = jsonObject.getString("PatientCodeName");
                        int score = jsonObject.getInt("Score");

                        boolean isRepeat = false;
                        for (int j = 0; j < mPatients.size(); j++) {
                            if(mPatients.get(j).getStayId() == getStayId) {
                                mPatients.get(j).setPoint(mPatients.get(j).getPoint() + score);
                                isRepeat = true;
                                break;
                            }
                        }

                        if(!isRepeat) {
                            mPatients.add(new Patient(patientId, getStayId, patientCodeName, score));
                        }
                    }
                    Comparator cmp = new PatientPointComparator();
                    Collections.sort(mPatients, cmp);
                    for (int i = 0; i < mPatients.size(); i++) {
                        mPatients.get(i).setRank((i+1));
                    }
                    mListView.setAdapter(new CustomPatientLeaderBoardAdapter(context, resources, mPatients));

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
            }
            else if(requestId == REFRESH_TOKEN_TASK_ID) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    access_token = jsonObject.getString("access_token");
                    GetSetSharedPreferences.setDefaults("access_token", access_token, getActivity().getApplicationContext());
                    getPatientLeaderBoard();
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
