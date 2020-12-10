package sg.com.nyp.a164936j.physioAssist.fragments.fragmentsphysio;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import sg.com.nyp.a164936j.physioAssist.IPAddress;
import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.configuration.Config;
import sg.com.nyp.a164936j.physioAssist.configuration.CustomSharedPreference;
import sg.com.nyp.a164936j.physioAssist.configuration.GetSetSharedPreferences;
import sg.com.nyp.a164936j.physioAssist.customadapters.CustomPhysioProgressAdapter;
import sg.com.nyp.a164936j.physioAssist.fragments.Comparator.DateComparator;
import sg.com.nyp.a164936j.physioAssist.fragments.interfaces.OnTaskCompleted;
import sg.com.nyp.a164936j.physioAssist.httpasynctask.GetAuth;
import sg.com.nyp.a164936j.physioAssist.httpasynctask.PostForm;
import sg.com.nyp.a164936j.physioAssist.models.Exercise;


/**
 * A simple {@link Fragment} subclass.
 */
public class PhysioProgress extends Fragment implements AdapterView.OnItemSelectedListener, OnDateSelectedListener, OnTaskCompleted {

    private static final int GET_PATIENT_PROGRESS_TASK_ID = 1;
    private static final int REFRESH_TOKEN_TASK_ID = 2;

    private Context context;
    private Dialog calendarDialog;
    private ListView lv;
    private CustomPhysioProgressAdapter adapter;
    private RadioGroup radioGroup;
    private TextView dateFrom, dateTo;
    private Button btnGraph;
    private TextView patientName;
    private TextView calendarTitle;
    private Spinner spinner;
    private MaterialCalendarView calendarView;
    private ItemClickListener mClickListener;
    private ProgressBar mProgressView;

    private List<String> dayList = new ArrayList<>();
    private List<Exercise> performedPrescribedExercises = new ArrayList<>();
    private List<Exercise> listExercises = new ArrayList<>();

    private String stayId;
    private String patientCodeName;
    private String status = "failed";
    private String access_token;
    private String dateFromTxt;
    private String dateToTxt;
    private Date dateFromDate;
    private Date dateToDate;
    private boolean isDateFromSelected = false;
    private boolean isDateToSelected = false;
    private boolean isAutoSelected = false;
    private boolean trueDayFalseDate = true;
    private boolean dayRefresh = true;
    private boolean dateRefresh = false;

    public PhysioProgress() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_physio_progress, container, false);
        initView(rootView);

        stayId = GetSetSharedPreferences.getDefaults("stayId", getActivity().getApplicationContext());
        patientCodeName = GetSetSharedPreferences.getDefaults("patientCodeName", getActivity().getApplicationContext());
        patientName.setText(patientCodeName + getResources().getString(R.string.physio_progress_patient_name));

        generateDummyData();
        initDate();

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, dayList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(this);

        radioGroup.setOnCheckedChangeListener((RadioGroup group, int checkedId)-> {
            RadioButton checkRadio = group.findViewById(checkedId);
            boolean isChecked = checkRadio.isChecked();

            if(isChecked){
                if(checkRadio.getText().equals("Day:")){
                    Log.d(Config.TAG_BUTTON, "(PhysioProgress) radio: "+ checkRadio.getText());
                    trueDayFalseDate = true;
                    initDate();
                    spinner.setEnabled(true);
                    dateFrom.setEnabled(false);
                    dateTo.setEnabled(false);
                    dateFrom.setClickable(false);
                    dateTo.setClickable(false);
                    dateFrom.setText(getResources().getString(R.string.physio_progress_date_from));
                    dateTo.setText(getResources().getString(R.string.physio_progress_date_to));
                    dayRefresh = true;
                    disableWidget();
                    getPatientProgress();
                    showProgress(true);
                }else{
                    Log.d(Config.TAG_BUTTON, "(PhysioProgress) radio: "+ checkRadio.getText());
                    trueDayFalseDate = false;
                    if(spinner.getSelectedItemPosition() != 0) {
                        spinner.setSelection(0);
                        isAutoSelected = true;
                    }
                    spinner.setEnabled(false);
                    dateFrom.setEnabled(true);
                    dateTo.setEnabled(true);
                    dateFrom.setClickable(true);
                    dateTo.setClickable(true);
                    dateFromTxt = null;
                    dateToTxt = null;
                    dateFromDate = null;
                    dateToDate = null;
                }
            }
        });

        dateFrom.setOnClickListener((View view)->{
            isDateFromSelected = true;
            displayCalendar(R.layout.dialog_calendar, "start");
        });

        dateTo.setOnClickListener((View view)->{
            isDateToSelected = true;
            displayCalendar(R.layout.dialog_calendar, "end");
        });

        btnGraph.setOnClickListener((View view)-> {
            if(listExercises.size() > 0) {
                boolean isSelectedExercise = false;
                int selectedPrescribedExerciseId = -1;
                for (int i = 0; i < listExercises.size(); i++) {
                    if(listExercises.get(i).isSelectedExercise()) {
                        isSelectedExercise = true;
                        selectedPrescribedExerciseId = listExercises.get(i).getPrescribeExId();
                        break;
                    }
                }
                if(isSelectedExercise) {
                    if(trueDayFalseDate) {
                        for (int i = performedPrescribedExercises.size()-1; i >= 0; i--) {
                            if(performedPrescribedExercises.get(i).getPrescribeExId() != selectedPrescribedExerciseId) {
                                performedPrescribedExercises.remove(i);
                            }
                        }

                        Comparator cmp = new DateComparator();
                        Collections.sort(performedPrescribedExercises, cmp);

                        CustomSharedPreference.selectedPerformedExercises = performedPrescribedExercises;
                        mClickListener.onItemClick(dateFromTxt, dateToTxt);
                    }
                    else {
                        if(dateFromDate != null && dateToDate != null) {
                            for (int i = performedPrescribedExercises.size()-1; i >= 0; i--) {
                                if(performedPrescribedExercises.get(i).getPrescribeExId() != selectedPrescribedExerciseId) {
                                    performedPrescribedExercises.remove(i);
                                }
                            }

                            Comparator cmp = new DateComparator();
                            Collections.sort(performedPrescribedExercises, cmp);

                            CustomSharedPreference.selectedPerformedExercises = performedPrescribedExercises;
                            mClickListener.onItemClick(dateFromTxt, dateToTxt);
                        }
                        else if(dateFromDate == null && dateToDate != null) {
                            dateFrom.setBackground(ContextCompat.getDrawable(context, R.drawable.fragment_layout_round_white4));
                            float density = context.getResources().getDisplayMetrics().density;
                            int paddingPixel = (int)(10 * density);
                            dateFrom.setPadding(paddingPixel, paddingPixel, paddingPixel, paddingPixel);
                        }
                        else if(dateFromDate != null && dateToDate == null) {
                            dateTo.setBackground(ContextCompat.getDrawable(context, R.drawable.fragment_layout_round_white4));
                            float density = context.getResources().getDisplayMetrics().density;
                            int paddingPixel = (int)(10 * density);
                            dateTo.setPadding(paddingPixel, paddingPixel, paddingPixel, paddingPixel);
                        }
                        else {
                            dateFrom.setBackground(ContextCompat.getDrawable(context, R.drawable.fragment_layout_round_white4));
                            dateTo.setBackground(ContextCompat.getDrawable(context, R.drawable.fragment_layout_round_white4));
                            float density = context.getResources().getDisplayMetrics().density;
                            int paddingPixel = (int)(10 * density);
                            dateFrom.setPadding(paddingPixel, paddingPixel, paddingPixel, paddingPixel);
                            dateTo.setPadding(paddingPixel, paddingPixel, paddingPixel, paddingPixel);
                        }
                    }
                }
                else {
                    lv.setBackground(ContextCompat.getDrawable(context, R.drawable.fragment_layout_round_white4));
                    float density = context.getResources().getDisplayMetrics().density;
                    int paddingPixel = (int)(20 * density);
                    lv.setPadding(paddingPixel, paddingPixel, paddingPixel, paddingPixel);
                }
            }
            else {
                lv.setBackground(ContextCompat.getDrawable(context, R.drawable.fragment_layout_round_white4));
                float density = context.getResources().getDisplayMetrics().density;
                int paddingPixel = (int)(20 * density);
                lv.setPadding(paddingPixel, paddingPixel, paddingPixel, paddingPixel);
            }
        });

        disableWidget();
        getPatientProgress();
        showProgress(true);

        return rootView;
    }

    private void initView(View rootView) {
        context = getContext();
        patientName = rootView.findViewById(R.id.physio_progress_patient_name);
        lv = rootView.findViewById(R.id.physio_progress_video_listview);
        mProgressView = rootView.findViewById(R.id.loading_progress);
        spinner = rootView.findViewById(R.id.physio_progress_day_spinner);
        dateFrom = rootView.findViewById(R.id.physio_progress_date_from);
        dateTo = rootView.findViewById(R.id.physio_progress_date_to);
        radioGroup = rootView.findViewById(R.id.physio_progress_radio_group);
        btnGraph = rootView.findViewById(R.id.physio_progress_btn_graph);
    }

    private void initDate() {
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        String currentYear = String.valueOf(currentCalendar.get(Calendar.YEAR));
        int month = currentCalendar.get(Calendar.MONTH) + 1;
        String currentMonth;
        if(month >= 1 && month <= 9) {
            currentMonth = "0" + String.valueOf(month);
        }
        else {
            currentMonth = String.valueOf(month);
        }
        int date = currentCalendar.get(Calendar.DATE);
        String currentDate;
        if(date >= 1 && date <= 9) {
            currentDate = "0" + String.valueOf(date);
        }
        else {
            currentDate = String.valueOf(date);
        }
        dateFromTxt = currentYear + "-" + currentMonth + "-" + currentDate;
        dateToTxt = currentYear + "-" + currentMonth + "-" + currentDate;

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            dateFromDate = format.parse(dateFromTxt);
            dateToDate = format.parse(dateToTxt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disableWidget() {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            radioGroup.getChildAt(i).setEnabled(false);
        }
        spinner.setEnabled(false);
        dateFrom.setEnabled(false);
        dateTo.setEnabled(false);
        dateFrom.setClickable(false);
        dateTo.setClickable(false);

        lv.setVisibility(View.GONE);
    }

    private String yearMonthDateToDate(int year, int month, int date) {
        String currentYear = String.valueOf(year);
        String currentMonth;
        if(month >= 1 && month <= 9) {
            currentMonth = "0" + String.valueOf(month);
        }
        else {
            currentMonth = String.valueOf(month);
        }
        String currentDate;
        if(date >= 1 && date <= 9) {
            currentDate = "0" + String.valueOf(date);
        }
        else {
            currentDate = String.valueOf(date);
        }
        String dateTxt = currentYear + "-" + currentMonth + "-" + currentDate;
        return dateTxt;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(!isAutoSelected) {
            initDate();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
            int previousYear = calendar.get(Calendar.YEAR);
            int previousMonth = calendar.get(Calendar.MONTH) + 1;

            if(position == 1) {
                int previousDate = calendar.get(Calendar.DATE) - 1;
                dateFromTxt = yearMonthDateToDate(previousYear, previousMonth, previousDate);
            }
            else if(position == 2) {
                int previousDate = calendar.get(Calendar.DATE) - 2;
                dateFromTxt = yearMonthDateToDate(previousYear, previousMonth, previousDate);
            }

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            try {
                dateFromDate = format.parse(dateFromTxt);
                dateToDate = format.parse(dateToTxt);
            } catch (Exception e) {
                e.printStackTrace();
            }

            dayRefresh = true;
            disableWidget();
            getPatientProgress();
            showProgress(true);
        }
        else {
            isAutoSelected = false;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    // allow clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener){
        this.mClickListener = itemClickListener;
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView materialCalendarView, @NonNull CalendarDay calendarDay, boolean b) {
        calendarDay = calendarView.getSelectedDate();
        final String currentDate = calendarDay.getDate().toString();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        new Handler().postDelayed(()-> {
            if(isDateFromSelected) {
                try {
                    dateFromTxt = currentDate;
                    dateFromDate = format.parse(currentDate);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isDateFromSelected = false;
                dateFrom.setText(currentDate);
                calendarDialog.dismiss();
            }
            else if(isDateToSelected) {
                try {
                    dateToTxt = currentDate;
                    dateToDate = format.parse(currentDate);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isDateToSelected = false;
                dateTo.setText(currentDate);
                calendarDialog.dismiss();
            }
            if(dateFromDate != null && dateToDate != null) {
                dateRefresh = true;
                disableWidget();
                getPatientProgress();
                showProgress(true);
            }
        }, 500);
    }

    //parent activity will implement this method to respond to click events
    public interface ItemClickListener{
        //Call stacked bar graph fragment - passing to PhysioDashboard.java
        void onItemClick(String startDate, String endDate);
    }

    public void generateDummyData(){
        dayList.clear();
        dayList.add("Today");
        dayList.add("Yesterday");
        dayList.add("Last 3 days");
    }

    private void getPatientProgress(){
        String accessToken = GetSetSharedPreferences.getDefaults("access_token", getActivity().getApplicationContext());
        GetAuth getPatientProgressTask = new GetAuth(PhysioProgress.this, GET_PATIENT_PROGRESS_TASK_ID);
        getPatientProgressTask.execute("http://" + IPAddress.awsipaddress + "/PhysioWebPortal/api/hospitalstay/" + stayId + "/performed", accessToken);
    }

    private void refreshToken() {
        String parameters = getParameters();
        PostForm refreshTokenTask = new PostForm(PhysioProgress.this, REFRESH_TOKEN_TASK_ID);
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

    private void displayCalendar(int layoutResId, String dTitle){
        calendarDialog = new Dialog(context);
        switch (layoutResId) {
            case R.layout.dialog_calendar:

                calendarDialog.setContentView(layoutResId);
                calendarDialog.setTitle(dTitle);

                calendarTitle = calendarDialog.findViewById(R.id.physio_choose_date);
                calendarTitle.setText(String.format(getResources().getString(R.string.physio_choose_date), dTitle));

                calendarView = calendarDialog.findViewById(R.id.calendarView);
                if(isDateFromSelected) {
                    dateFrom.setBackground(ContextCompat.getDrawable(context, R.drawable.fragment_layout_round_white3));
                    float density = context.getResources().getDisplayMetrics().density;
                    int paddingPixel = (int)(10 * density);
                    dateFrom.setPadding(paddingPixel, paddingPixel, paddingPixel, paddingPixel);
                    if(dateToDate != null) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(dateToDate);
                        calendarView.state().edit().setMaximumDate(CalendarDay.from(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DATE))).commit();
                    }
                }
                else if(isDateToSelected) {
                    dateTo.setBackground(ContextCompat.getDrawable(context, R.drawable.fragment_layout_round_white3));
                    float density = context.getResources().getDisplayMetrics().density;
                    int paddingPixel = (int)(10 * density);
                    dateTo.setPadding(paddingPixel, paddingPixel, paddingPixel, paddingPixel);
                    if(dateFromDate != null) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(dateFromDate);
                        calendarView.state().edit().setMinimumDate(CalendarDay.from(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DATE))).commit();
                    }
                }
                calendarView.setOnDateChangedListener(this);
                break;
        }
        calendarDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        calendarDialog.show();
    }

    @Override
    public void onTaskCompleted(String response, int requestId) {
        if(response.contains("<html")) {
            refreshToken();
        } else {
            if(requestId == GET_PATIENT_PROGRESS_TASK_ID) {

                performedPrescribedExercises.clear();
                listExercises.clear();

                long dateDiff = dateToDate.getTime() - dateFromDate.getTime();
                long selectedDays = dateDiff / (1000 * 60 * 60 * 24);

                boolean isJSONArray = false;
                boolean isJSONObject = false;
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    isJSONObject = true;
                    int performExId = jsonObject.getInt("PerformExId");
                    int prescribeExId = jsonObject.getInt("PEId");
                    String exName = jsonObject.getString("ExName");
                    String StartTime = jsonObject.getString("StartTime");
                    String EndTime = jsonObject.getString("EndTime");
                    int avgAngle = jsonObject.getInt("AvgAngle");
                    int avgHoldDuration = jsonObject.getInt("AvgHoldDuration");
                    int score = jsonObject.getInt("Score");

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    Date exCurrentStartDate = dateFormat.parse(StartTime);
                    Date exCurrentEndDate = dateFormat.parse(EndTime);

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(exCurrentStartDate);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);

                    Date exCurrentStartDateWithoutTime = calendar.getTime();

                    long diff = exCurrentStartDateWithoutTime.getTime() - dateFromDate.getTime();
                    long days = diff / (1000 * 60 * 60 * 24);

                    if(days >= 0 && days <= selectedDays) {
                        Exercise performedPrescribedExercise = new Exercise(performExId, prescribeExId, exName, exCurrentStartDate, exCurrentEndDate, avgAngle, avgHoldDuration, score);
                        performedPrescribedExercises.add(performedPrescribedExercise);

                        Exercise listExercise = new Exercise(prescribeExId, exName, false);
                        listExercises.add(listExercise);
                    }
                    status = "succeeded";
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    JSONArray jsonArray = new JSONArray(response);
                    isJSONArray = true;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                        int performExId = jsonObject.getInt("PerformExId");
                        int prescribeExId = jsonObject.getInt("PEId");
                        String exName = jsonObject.getString("ExName");
                        String StartTime = jsonObject.getString("StartTime");
                        String EndTime = jsonObject.getString("EndTime");
                        int avgAngle = jsonObject.getInt("AvgAngle");
                        int avgHoldDuration = jsonObject.getInt("AvgHoldDuration");
                        int score = jsonObject.getInt("Score");

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        Date exCurrentStartDate = dateFormat.parse(StartTime);
                        Date exCurrentEndDate = dateFormat.parse(EndTime);

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(exCurrentStartDate);
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);

                        Date exCurrentStartDateWithoutTime = calendar.getTime();

                        long diff = exCurrentStartDateWithoutTime.getTime() - dateFromDate.getTime();
                        long days = diff / (1000 * 60 * 60 * 24);

                        if(days >= 0 && days <= selectedDays) {
                            Exercise performedPrescribedExercise = new Exercise(performExId, prescribeExId, exName, exCurrentStartDate, exCurrentEndDate, avgAngle, avgHoldDuration, score);
                            performedPrescribedExercises.add(performedPrescribedExercise);

                            boolean repeatExercise = false;
                            for(int j=0; j<listExercises.size(); j++) {
                                if(listExercises.get(j).getPrescribeExId() == prescribeExId) {
                                    repeatExercise = true;
                                    break;
                                }
                            }

                            if(!repeatExercise) {
                                Exercise listExercise = new Exercise(prescribeExId, exName, false);
                                listExercises.add(listExercise);
                            }
                        }
                    }
                    status = "succeeded";
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (isJSONArray || isJSONObject) {
                    if (status.equals("succeeded")) {
                        showProgress(false);
                    }
                } else if (!isJSONArray && !isJSONObject) {
                    status = "succeeded";
                    if (status.equals("succeeded")) {
                        showProgress(false);
                    }
                }

                adapter = new CustomPhysioProgressAdapter(context, listExercises);
                lv.setVisibility(View.VISIBLE);
                lv.setAdapter(adapter);

                for (int i = 0; i < radioGroup.getChildCount(); i++) {
                    radioGroup.getChildAt(i).setEnabled(true);
                }

                if(dayRefresh) {
                    spinner.setEnabled(true);
                    dayRefresh = false;
                }
                else if(dateRefresh) {
                    dateFrom.setEnabled(true);
                    dateTo.setEnabled(true);
                    dateFrom.setClickable(true);
                    dateTo.setClickable(true);
                    dateRefresh = false;
                }
            }
            else if(requestId == REFRESH_TOKEN_TASK_ID) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    access_token = jsonObject.getString("access_token");
                    GetSetSharedPreferences.setDefaults("access_token", access_token, getActivity().getApplicationContext());
                    getPatientProgress();
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
