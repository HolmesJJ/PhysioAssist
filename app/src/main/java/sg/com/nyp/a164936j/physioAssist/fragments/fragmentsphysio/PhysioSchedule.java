package sg.com.nyp.a164936j.physioAssist.fragments.fragmentsphysio;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sg.com.nyp.a164936j.physioAssist.IPAddress;
import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.configuration.Config;
import sg.com.nyp.a164936j.physioAssist.configuration.CustomSharedPreference;
import sg.com.nyp.a164936j.physioAssist.configuration.GetSetSharedPreferences;
import sg.com.nyp.a164936j.physioAssist.customadapters.CustomPhysioScheduleAdapter;
import sg.com.nyp.a164936j.physioAssist.customadapters.CustomSelectExerciseSpinnerAdapter;
import sg.com.nyp.a164936j.physioAssist.fragments.interfaces.OnTaskCompleted;
import sg.com.nyp.a164936j.physioAssist.httpasynctask.GetAuth;
import sg.com.nyp.a164936j.physioAssist.httpasynctask.PostForm;
import sg.com.nyp.a164936j.physioAssist.models.Exercise;

/**
 * A simple {@link Fragment} subclass.
 */
public class PhysioSchedule extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener, OnTaskCompleted {

    private static final int GET_EXERCISES_TASK_ID = 1;
    private static final int REFRESH_TOKEN_TASK_ID = 2;
    private static final int GET_PRESCRIBED_EXERCISES_TASK_ID = 3;

    private static final String exTime1 = "9:00 AM";
    private static final String exTime2 = "1:00 PM";
    private static final String exTime3 = "5:00 PM";
    private static final String exTime4 = "9:00 PM";

    //vars - Listview
    private List<String> frequentList = new ArrayList<>();
    private List<Exercise> exerciseList = new ArrayList<>();
    private List<Exercise> prescribedExerciseList = new ArrayList<>();
    private List<String> exTimeList = new ArrayList<>();

    private Context context;
    private CustomPhysioScheduleAdapter adapter;
    private ProgressBar mProgressView;
    private ListView listView;
    private Spinner frequencySpinner;
    private Spinner exerciseSpinner;
    private Button btnAdd;

    private String stayId;
    private String scheduleStatus = "failed";
    private String exerciseStatus = "failed";
    private String access_token;
    private int selected;
    private int listViewHeight = 0;
    private int listViewWidth = 0;
    private int exTimeCount = 0;
    private boolean scheduleRequest = true;
    private boolean exerciseRequest = true;

    public PhysioSchedule() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_physio_schedule, container, false);
        initView(rootView);

        stayId = GetSetSharedPreferences.getDefaults("stayId", getActivity().getApplicationContext());

        getPrescribedExercises();
        generateDummyData();

        rootView.post(()-> {
            listViewHeight = listView.getMeasuredHeight();
            listViewWidth = listView.getMeasuredWidth();
        });

        exerciseSpinner.setOnItemSelectedListener(this);
        Exercise selectTitle = new Exercise(-1, "Select Exercise");
        exerciseList.add(selectTitle);
        CustomSelectExerciseSpinnerAdapter customSelectExerciseSpinnerAdapter = new CustomSelectExerciseSpinnerAdapter(getActivity(), R.layout.select_exercise_spinner_item, exerciseList);
        exerciseSpinner.setAdapter(customSelectExerciseSpinnerAdapter);
        exerciseSpinner.setEnabled(false);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, frequentList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        frequencySpinner.setAdapter(dataAdapter);
        frequencySpinner.setEnabled(false);

        btnAdd.setOnClickListener(this);
        btnAdd.setEnabled(false);
        getExercises();

        showProgress(true);

        return rootView;
    }

    private void initView(View rootView) {
        context = getContext();
        listView = rootView.findViewById(R.id.physio_schedule_listview);
        exerciseSpinner = rootView.findViewById(R.id.physio_schedule_exercise_spinner);
        frequencySpinner = rootView.findViewById(R.id.physio_schedule_frequency_spinner);
        mProgressView = rootView.findViewById(R.id.spinner_progress);
        btnAdd = rootView.findViewById(R.id.physio_schedule_btn_add);
    }

    private void getExercises(){
        String accessToken = GetSetSharedPreferences.getDefaults("access_token", getActivity().getApplicationContext());
        GetAuth getAuthSpinnerTask = new GetAuth(PhysioSchedule.this, GET_EXERCISES_TASK_ID);
        getAuthSpinnerTask.execute("http://" + IPAddress.awsipaddress + "/PhysioWebPortal/api/exercises", accessToken);
    }

    private void getPrescribedExercises(){
        String accessToken = GetSetSharedPreferences.getDefaults("access_token", getActivity().getApplicationContext());
        GetAuth getAuthSpinnerTask = new GetAuth(PhysioSchedule.this, GET_PRESCRIBED_EXERCISES_TASK_ID);
        getAuthSpinnerTask.execute("http://" + IPAddress.awsipaddress + "/PhysioWebPortal/api/hospitalstay/" + stayId + "/prescribed", accessToken);
    }

    private void refreshToken() {
        String parameters = getParameters();
        PostForm refreshTokenTask = new PostForm(PhysioSchedule.this, REFRESH_TOKEN_TASK_ID);
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
        frequentList.clear();
        frequentList.add("One Session");
        frequentList.add("Two Session");
        frequentList.add("Three Session");
        frequentList.add("Four Session");
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if(exerciseList.get(i).getExerciseId() > 0) {
            selected = exerciseList.get(i).getExerciseId();
        }
        else {
            selected = -1;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onClick(View view) {
        String selectedSession = String.valueOf(frequencySpinner.getSelectedItem());
        Log.d(Config.TAG_BUTTON, "(PhysioSchedule) add button pressed");
    }

    @Override
    public void onTaskCompleted(String response, int requestId) {
        if(response.contains("<html")) {
            if(requestId == GET_EXERCISES_TASK_ID) {
                exerciseRequest = false;
            }
            else if(requestId == GET_PRESCRIBED_EXERCISES_TASK_ID) {
                scheduleRequest = false;
            }
            refreshToken();
        } else {
            if(requestId == GET_EXERCISES_TASK_ID) {
                exerciseList.clear();
                boolean isJSONArray = false;
                boolean isJSONObject = false;
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    isJSONObject = true;
                    int exerciseId = jsonObject.getInt("ExId");
                    String exerciseName = jsonObject.getString("ExName");
                    exerciseList.add(new Exercise(exerciseId, exerciseName));
                    exerciseStatus = "succeeded";
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    isJSONArray = true;
                    for(int i=0; i<jsonArray.length(); i++) {
                        JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                        int exerciseId = jsonObject.getInt("ExId");
                        String exerciseName = jsonObject.getString("ExName");
                        exerciseList.add(new Exercise(exerciseId, exerciseName));
                    }
                    exerciseStatus = "succeeded";
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(isJSONArray || isJSONObject) {
                    if(scheduleStatus.equals("succeeded") && exerciseStatus.equals("succeeded")) {
                        showProgress(false);
                        // 加载完才允许点击
                        exerciseSpinner.setEnabled(true);
                        frequencySpinner.setEnabled(true);
                        btnAdd.setEnabled(true);
                    }
                }
                else if(!isJSONArray && !isJSONObject) {
                    exerciseStatus = "succeeded";
                    if(scheduleStatus.equals("succeeded") && exerciseStatus.equals("succeeded")) {
                        showProgress(false);
                        // 加载完才允许点击
                        exerciseSpinner.setEnabled(true);
                        frequencySpinner.setEnabled(true);
                        btnAdd.setEnabled(true);
                    }
                }
            }
            else if(requestId == GET_PRESCRIBED_EXERCISES_TASK_ID) {
                prescribedExerciseList.clear();
                boolean isJSONArray = false;
                boolean isJSONObject = false;
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    isJSONObject = true;
                    String assignedDateTxt = jsonObject.getString("AssignedDate");
                    String endDateTxt = jsonObject.getString("EndDate");
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    Date assignedDate = dateFormat.parse(assignedDateTxt);
                    long assignedTimeStamp = assignedDate.getTime();
                    Date endDate = dateFormat.parse(endDateTxt);
                    long endTimeStamp = endDate.getTime();

                    if(assignedTimeStamp <= System.currentTimeMillis() && endTimeStamp >= System.currentTimeMillis()) {

                        int prescribedExId = jsonObject.getInt("PEId");
                        int exTimePerDay = jsonObject.getInt("ExTimePerDay");
                        int exType = jsonObject.getInt("ExType");
                        String exName = jsonObject.getString("ExName");
                        int exSet = jsonObject.getInt("ExSetNo");
                        int exRepeat = jsonObject.getInt("ExRepNo");
                        if(exTimePerDay == 1) {
                            exTimeCount = 1;
                            prescribedExerciseList.add(new Exercise(prescribedExId, exType, exName, exTime1, exSet, exRepeat));
                        }
                        else if(exTimePerDay == 2) {
                            if(exTimeCount < 2) {
                                exTimeCount = 2;
                            }
                            prescribedExerciseList.add(new Exercise(prescribedExId, exType, exName, exTime1, exSet, exRepeat));
                            prescribedExerciseList.add(new Exercise(prescribedExId, exType, exName, exTime2, exSet, exRepeat));
                        }
                        else if(exTimePerDay == 3) {
                            if(exTimeCount < 3) {
                                exTimeCount = 3;
                            }
                            prescribedExerciseList.add(new Exercise(prescribedExId, exType, exName, exTime1, exSet, exRepeat));
                            prescribedExerciseList.add(new Exercise(prescribedExId, exType, exName, exTime2, exSet, exRepeat));
                            prescribedExerciseList.add(new Exercise(prescribedExId, exType, exName, exTime3, exSet, exRepeat));
                        }
                        else if(exTimePerDay == 4) {
                            if(exTimeCount < 4) {
                                exTimeCount = 4;
                            }
                            prescribedExerciseList.add(new Exercise(prescribedExId, exType, exName, exTime1, exSet, exRepeat));
                            prescribedExerciseList.add(new Exercise(prescribedExId, exType, exName, exTime2, exSet, exRepeat));
                            prescribedExerciseList.add(new Exercise(prescribedExId, exType, exName, exTime3, exSet, exRepeat));
                            prescribedExerciseList.add(new Exercise(prescribedExId, exType, exName, exTime4, exSet, exRepeat));
                        }
                    }

                    if(exTimeCount == 1) {
                        exTimeList.add(exTime1);
                    }
                    else if(exTimeCount == 2) {
                        exTimeList.add(exTime1);
                        exTimeList.add(exTime2);
                    }
                    else if(exTimeCount == 3) {
                        exTimeList.add(exTime1);
                        exTimeList.add(exTime2);
                        exTimeList.add(exTime3);
                    }
                    else if(exTimeCount == 4) {
                        exTimeList.add(exTime1);
                        exTimeList.add(exTime2);
                        exTimeList.add(exTime3);
                        exTimeList.add(exTime4);
                    }

                    CustomSharedPreference.prescribedExercises = prescribedExerciseList;

                    adapter = new CustomPhysioScheduleAdapter(context, exTimeList, listViewHeight, listViewWidth);
                    listView.setAdapter(adapter);

                    scheduleStatus = "succeeded";
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    isJSONArray = true;
                    for(int i=0; i<jsonArray.length(); i++) {
                        JSONObject jsonObject = new JSONObject(jsonArray.getString(i));

                        String assignedDateTxt = jsonObject.getString("AssignedDate");
                        String endDateTxt = jsonObject.getString("EndDate");
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        Date assignedDate = dateFormat.parse(assignedDateTxt);
                        long assignedTimeStamp = assignedDate.getTime();
                        Date endDate = dateFormat.parse(endDateTxt);
                        long endTimeStamp = endDate.getTime();

                        if(assignedTimeStamp <= System.currentTimeMillis() && endTimeStamp >= System.currentTimeMillis()) {

                            int prescribedExId = jsonObject.getInt("PEId");
                            int exTimePerDay = jsonObject.getInt("ExTimePerDay");
                            int exType = jsonObject.getInt("ExType");
                            String exName = jsonObject.getString("ExName");
                            int exSet = jsonObject.getInt("ExSetNo");
                            int exRepeat = jsonObject.getInt("ExRepNo");

                            if(exTimePerDay == 1) {
                                if(exTimeCount < 1) {
                                    exTimeCount = 1;
                                }
                                prescribedExerciseList.add(new Exercise(prescribedExId, exType, exName, exTime1, exSet, exRepeat));
                            }
                            else if(exTimePerDay == 2) {
                                if(exTimeCount < 2) {
                                    exTimeCount = 2;
                                }
                                prescribedExerciseList.add(new Exercise(prescribedExId, exType, exName, exTime1, exSet, exRepeat));
                                prescribedExerciseList.add(new Exercise(prescribedExId, exType, exName, exTime2, exSet, exRepeat));
                            }
                            else if(exTimePerDay == 3) {
                                if(exTimeCount < 3) {
                                    exTimeCount = 3;
                                }
                                prescribedExerciseList.add(new Exercise(prescribedExId, exType, exName, exTime1, exSet, exRepeat));
                                prescribedExerciseList.add(new Exercise(prescribedExId, exType, exName, exTime2, exSet, exRepeat));
                                prescribedExerciseList.add(new Exercise(prescribedExId, exType, exName, exTime3, exSet, exRepeat));
                            }
                            else if(exTimePerDay == 4) {
                                if(exTimeCount < 4) {
                                    exTimeCount = 4;
                                }
                                prescribedExerciseList.add(new Exercise(prescribedExId, exType, exName, exTime1, exSet, exRepeat));
                                prescribedExerciseList.add(new Exercise(prescribedExId, exType, exName, exTime2, exSet, exRepeat));
                                prescribedExerciseList.add(new Exercise(prescribedExId, exType, exName, exTime3, exSet, exRepeat));
                                prescribedExerciseList.add(new Exercise(prescribedExId, exType, exName, exTime4, exSet, exRepeat));
                            }

                            System.out.println("exTimeCount");
                            System.out.println(exTimeCount);
                        }
                    }

                    if(exTimeCount == 1) {
                        exTimeList.add(exTime1);
                    }
                    else if(exTimeCount == 2) {
                        exTimeList.add(exTime1);
                        exTimeList.add(exTime2);
                    }
                    else if(exTimeCount == 3) {
                        exTimeList.add(exTime1);
                        exTimeList.add(exTime2);
                        exTimeList.add(exTime3);
                    }
                    else if(exTimeCount == 4) {
                        exTimeList.add(exTime1);
                        exTimeList.add(exTime2);
                        exTimeList.add(exTime3);
                        exTimeList.add(exTime4);
                    }

                    System.out.println("888888888888888888");
                    System.out.println(exTimeCount);

                    for (int i = 0; i < exTimeList.size(); i++) {
                        System.out.println(exTimeList.get(i));
                    }

                    CustomSharedPreference.prescribedExercises = prescribedExerciseList;

                    adapter = new CustomPhysioScheduleAdapter(context, exTimeList, listViewHeight, listViewWidth);
                    listView.setAdapter(adapter);

                    scheduleStatus = "succeeded";
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(isJSONArray || isJSONObject) {
                    if(scheduleStatus.equals("succeeded") && exerciseStatus.equals("succeeded")) {
                        showProgress(false);
                        // 加载完才允许点击
                        exerciseSpinner.setEnabled(true);
                        frequencySpinner.setEnabled(true);
                        btnAdd.setEnabled(true);
                    }
                }
                else if(!isJSONArray && !isJSONObject) {
                    scheduleStatus = "succeeded";
                    if(scheduleStatus.equals("succeeded") && exerciseStatus.equals("succeeded")) {
                        showProgress(false);
                        // 加载完才允许点击
                        exerciseSpinner.setEnabled(true);
                        frequencySpinner.setEnabled(true);
                        btnAdd.setEnabled(true);
                    }
                }
            }
            else if(requestId == REFRESH_TOKEN_TASK_ID) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    access_token = jsonObject.getString("access_token");
                    GetSetSharedPreferences.setDefaults("access_token", access_token, getActivity().getApplicationContext());
                    if(!exerciseRequest) {
                        getExercises();
                        exerciseRequest = true;
                    }
                    else if(!scheduleRequest) {
                        getPrescribedExercises();
                        scheduleRequest = true;
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
}
