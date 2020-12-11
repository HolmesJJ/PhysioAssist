package sg.com.nyp.a164936j.physioAssist.fragments.fragmentspatient;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import sg.com.nyp.a164936j.physioAssist.BlankCanvas;
import sg.com.nyp.a164936j.physioAssist.IPAddress;
import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.configuration.GetSetSharedPreferences;
import sg.com.nyp.a164936j.physioAssist.customadapters.CustomPatientGiftGridViewAdapter;
import sg.com.nyp.a164936j.physioAssist.customadapters.CustomPatientProgressHistoryAdapter;
import sg.com.nyp.a164936j.physioAssist.customadapters.CustomPatientTrophyGridViewAdapter;
import sg.com.nyp.a164936j.physioAssist.customcomponents.WrappingGridView;
import sg.com.nyp.a164936j.physioAssist.fragments.interfaces.OnTaskCompleted;
import sg.com.nyp.a164936j.physioAssist.httpasynctask.GetAuth;
import sg.com.nyp.a164936j.physioAssist.httpasynctask.PostForm;
import sg.com.nyp.a164936j.physioAssist.models.Exercise;

/**
 * A simple {@link Fragment} subclass.
 */
public class PatientProgress extends Fragment implements OnTaskCompleted {

    private static final int GET_PATIENT_PROGRESS_TASK_ID = 1;
    private static final int GET_GIFT_TASK_ID = 2;
    private static final int REFRESH_TOKEN_TASK_ID = 3;

    private Context context;
    private Resources resources;
    private ListView progressHistoryListView;
    private WrappingGridView progressTrophyGridView, progressGiftGridView;

    private CustomPatientProgressHistoryAdapter adapterHistory;
    private CustomPatientTrophyGridViewAdapter adapterTrophy;
    private CustomPatientGiftGridViewAdapter adapterGift;

    private TextView topHeader;
    private TextView topSubHeader;
    private TextView historyHeader;
    private TextView trophyHeader;
    private TextView giftsHeader;
    private TextView textPoints;
    private ProgressBar mProgressView;

    private String language;
    private String stayId;
    private String patientCodeName;
    private String access_token;
    private String progressStatus = "failed";
    private String giftStatus = "failed";
    private boolean progressRequest = true;
    private boolean giftRequest = true;

    private List<Exercise> performedPrescribedExercises = new ArrayList<>();
    private List<Exercise> performedPrescribedExercisesPerDay = new ArrayList<>();
    private List<Integer> gifts = new ArrayList<>();

    public PatientProgress() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_patient_progress, container, false);
        initView(rootView);

        stayId = GetSetSharedPreferences.getDefaults("stayId", getActivity().getApplicationContext());
        patientCodeName = GetSetSharedPreferences.getDefaults("patientCodeName", getActivity().getApplicationContext());
        language = GetSetSharedPreferences.getDefaults("language", getActivity().getApplicationContext());

        BlankCanvas blankCanvas = (BlankCanvas) getActivity();
        resources = blankCanvas.updateViews(context, language);
        initLanguage();

        progressTrophyGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        progressGiftGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));

        getPatientProgress();
        getGift();

        showProgress(true);

        return rootView;
    }

    private void initView(View rootView) {
        context = getContext();
        topHeader = rootView.findViewById(R.id.progress_top_header);
        topSubHeader = rootView.findViewById(R.id.progress_top_sub_header);
        textPoints = rootView.findViewById(R.id.progress_top_score);
        historyHeader = rootView.findViewById(R.id.progress_history_header);
        trophyHeader = rootView.findViewById(R.id.progress_trophy_header);
        giftsHeader = rootView.findViewById(R.id.progress_gifts_header);
        progressTrophyGridView = rootView.findViewById(R.id.progress_trophy_gridview);
        progressGiftGridView = rootView.findViewById(R.id.progress_gift_gridview);
        progressHistoryListView = rootView.findViewById(R.id.patient_progress_history_listview);
        mProgressView = rootView.findViewById(R.id.loading_progress);
    }

    private void initLanguage() {
        topHeader.setText(patientCodeName.toUpperCase() + resources.getString(R.string.progress_top_header));
        topSubHeader.setText(resources.getString(R.string.progress_top_sub_header));
        historyHeader.setText(resources.getString(R.string.progress_history_header));
        trophyHeader.setText(resources.getString(R.string.progress_trophy_header));
        giftsHeader.setText(resources.getString(R.string.progress_gifts_header));
    }

    private void getPatientProgress(){
        String accessToken = GetSetSharedPreferences.getDefaults("access_token", getActivity().getApplicationContext());
        GetAuth getPatientProgressTask = new GetAuth(PatientProgress.this, GET_PATIENT_PROGRESS_TASK_ID);
        getPatientProgressTask.execute("http://" + IPAddress.ipaddress + "/api/hospitalstay/" + stayId + "/performed", accessToken);
    }

    private void getGift(){
        String accessToken = GetSetSharedPreferences.getDefaults("access_token", getActivity().getApplicationContext());
        GetAuth getGiftTask = new GetAuth(PatientProgress.this, GET_GIFT_TASK_ID);
        getGiftTask.execute("http://" + IPAddress.ipaddress + "/api/hospitalstay/" + stayId + "/gifts", accessToken);
    }

    private void refreshToken() {
        String parameters = getParameters();
        PostForm refreshTokenTask = new PostForm(PatientProgress.this, REFRESH_TOKEN_TASK_ID);
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
            if(requestId == GET_PATIENT_PROGRESS_TASK_ID) {
                progressRequest = false;
            }
            else if(requestId == GET_GIFT_TASK_ID) {
                giftRequest = false;
            }
            refreshToken();
        } else {
            if(requestId == GET_PATIENT_PROGRESS_TASK_ID) {
                boolean isJSONArray = false;
                boolean isJSONObject = false;
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    isJSONObject = true;

                    int prescribeExId = jsonObject.getInt("PEId");
                    String exName = jsonObject.getString("ExName");
                    int score = jsonObject.getInt("Score");
                    int exTimePerDay = jsonObject.getInt("ExTimePerDay");

                    Exercise performedPrescribedExercise = new Exercise(prescribeExId, exName, 1, exTimePerDay);
                    performedPrescribedExercises.add(performedPrescribedExercise);

                    adapterHistory = new CustomPatientProgressHistoryAdapter(context, resources, performedPrescribedExercises);
                    progressHistoryListView.setAdapter(adapterHistory);

                    // Set Point
                    textPoints.setText(String.valueOf(score));

                    // Set trophy
                    int trophyCollected = 0;
                    trophyHeader.setText(resources.getString(R.string.progress_trophy_header) + trophyCollected);
                    adapterTrophy = new CustomPatientTrophyGridViewAdapter(context, trophyCollected);
                    progressTrophyGridView.setAdapter(adapterTrophy);

                    progressStatus = "succeeded";
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    isJSONArray = true;

                    int totalScore = 0;
                    for(int i=0; i<jsonArray.length(); i++) {
                        JSONObject jsonObject = new JSONObject(jsonArray.getString(i));

                        int prescribeExId = jsonObject.getInt("PEId");
                        String exName = jsonObject.getString("ExName");
                        String startTime = jsonObject.getString("StartTime");
                        int score = jsonObject.getInt("Score");
                        int exTimePerDay = jsonObject.getInt("ExTimePerDay");

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        Date startDate = dateFormat.parse(startTime);

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(startDate);
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH) + 1;
                        int date = calendar.get(Calendar.DATE);

                        String startDateTxt = year + "-" + month + "-" + date;

                        // performedPrescribedExercisesPerDay
                        boolean isRepeatPerDay = false;
                        for(int j=0; j<performedPrescribedExercisesPerDay.size(); j++) {
                            if(performedPrescribedExercisesPerDay.get(j).getPrescribeExId() == prescribeExId &&
                                    performedPrescribedExercisesPerDay.get(j).getExTime().equals(startDateTxt)) {
                                performedPrescribedExercisesPerDay.get(j).setCompletedTimes(performedPrescribedExercisesPerDay.get(j).getCompletedTimes() + 1);
                                isRepeatPerDay = true;
                                break;
                            }
                        }

                        if(!isRepeatPerDay) {
                            Exercise performedPrescribedExercisePerDay = new Exercise(prescribeExId, exName, startDateTxt, 1, exTimePerDay);
                            performedPrescribedExercisesPerDay.add(performedPrescribedExercisePerDay);
                        }

                        // performedPrescribedExercises
                        boolean isRepeat = false;
                        for(int j=0; j<performedPrescribedExercises.size(); j++) {
                            if(performedPrescribedExercises.get(j).getPrescribeExId() == prescribeExId) {
                                performedPrescribedExercises.get(j).setCompletedTimes(performedPrescribedExercises.get(j).getCompletedTimes() + 1);
                                isRepeat = true;
                                break;
                            }
                        }

                        if(!isRepeat) {
                            Exercise performedPrescribedExercise = new Exercise(prescribeExId, exName, 1, exTimePerDay);
                            performedPrescribedExercises.add(performedPrescribedExercise);
                        }

                        totalScore = totalScore + score;
                    }

                    adapterHistory = new CustomPatientProgressHistoryAdapter(context, resources, performedPrescribedExercises);
                    progressHistoryListView.setAdapter(adapterHistory);

                    // Set Point
                    textPoints.setText(String.valueOf(totalScore));

                    // Set trophy
                    int trophyCollected = 0;
                    for(int j=0; j<performedPrescribedExercisesPerDay.size(); j++) {
                        if(performedPrescribedExercisesPerDay.get(j).getCompletedTimes() > performedPrescribedExercisesPerDay.get(j).getExTimePerDay())  {
                            trophyCollected++;
                        }
                    }
                    trophyHeader.setText(resources.getString(R.string.progress_trophy_header) + trophyCollected);
                    adapterTrophy = new CustomPatientTrophyGridViewAdapter(context, trophyCollected);
                    progressTrophyGridView.setAdapter(adapterTrophy);

                    progressStatus = "succeeded";
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(isJSONArray || isJSONObject) {
                    if(progressStatus.equals("succeeded") && giftStatus.equals("succeeded")) {
                        showProgress(false);
                    }
                }
                else if(!isJSONArray && !isJSONObject) {
                    progressStatus = "succeeded";
                    if(progressStatus.equals("succeeded") && giftStatus.equals("succeeded")) {
                        showProgress(false);
                    }
                }
            }
            else if(requestId == GET_GIFT_TASK_ID) {
                boolean isJSONArray = false;
                boolean isJSONObject = false;
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    isJSONObject = true;
                    int giftTypeId = jsonObject.getInt("GiftTypeId");

                    if(giftTypeId == 1) {
                        gifts.add(R.drawable.achievement_well_done);
                    }
                    else if(giftTypeId == 2) {
                        gifts.add(R.drawable.achievement_good_job);
                    }
                    else if(giftTypeId == 3) {
                        gifts.add(R.drawable.achievement_great);
                    }
                    else if(giftTypeId == 4) {
                        gifts.add(R.drawable.achievement_nice);
                    }

                    giftStatus = "succeeded";

                    adapterGift = new CustomPatientGiftGridViewAdapter(context, gifts);
                    progressGiftGridView.setAdapter(adapterGift);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    isJSONArray = true;

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                        int giftTypeId = jsonObject.getInt("GiftTypeId");
                        if(giftTypeId == 1) {
                            gifts.add(R.drawable.achievement_well_done);
                        }
                        else if(giftTypeId == 2) {
                            gifts.add(R.drawable.achievement_good_job);
                        }
                        else if(giftTypeId == 3) {
                            gifts.add(R.drawable.achievement_great);
                        }
                        else if(giftTypeId == 4) {
                            gifts.add(R.drawable.achievement_nice);
                        }
                    }

                    giftStatus = "succeeded";

                    adapterGift = new CustomPatientGiftGridViewAdapter(context, gifts);
                    progressGiftGridView.setAdapter(adapterGift);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(isJSONArray || isJSONObject) {
                    if(progressStatus.equals("succeeded") && giftStatus.equals("succeeded")) {
                        showProgress(false);
                    }
                }
                else if(!isJSONArray && !isJSONObject) {
                    giftStatus = "succeeded";
                    if(progressStatus.equals("succeeded") && giftStatus.equals("succeeded")) {
                        showProgress(false);
                    }
                }
            }
            else if(requestId == REFRESH_TOKEN_TASK_ID) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    access_token = jsonObject.getString("access_token");
                    GetSetSharedPreferences.setDefaults("access_token", access_token, getActivity().getApplicationContext());
                    getPatientProgress();
                    if(!progressRequest) {
                        getPatientProgress();
                        progressRequest = true;
                    }
                    else if(!giftRequest) {
                        getGift();
                        giftRequest = true;
                    }
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
