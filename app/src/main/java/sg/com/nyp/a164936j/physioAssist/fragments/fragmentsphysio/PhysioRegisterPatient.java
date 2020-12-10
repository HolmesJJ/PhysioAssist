package sg.com.nyp.a164936j.physioAssist.fragments.fragmentsphysio;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import sg.com.nyp.a164936j.physioAssist.CustomToast;
import sg.com.nyp.a164936j.physioAssist.IPAddress;
import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.configuration.Config;
import sg.com.nyp.a164936j.physioAssist.configuration.GetSetSharedPreferences;
import sg.com.nyp.a164936j.physioAssist.customadapters.CustomSelectPatientSpinnerAdapter;
import sg.com.nyp.a164936j.physioAssist.fragments.interfaces.OnTaskCompleted;
import sg.com.nyp.a164936j.physioAssist.httpasynctask.GetAuth;
import sg.com.nyp.a164936j.physioAssist.httpasynctask.PostForm;
import sg.com.nyp.a164936j.physioAssist.models.Patient;


public class PhysioRegisterPatient extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener, OnTaskCompleted {

    private static final int GET_SPINNER_TASK_ID = 1;
    private static final int GET_STAY_TASK_ID = 2;
    private static final int REFRESH_TOKEN_TASK_ID = 3;

    private List<Patient> mPatients = new ArrayList<>();

    private Spinner customSpinner;
    private Button btn_register;
    private ProgressBar mProgressView;
    private ImageView icSucceeded;

    private String selectedPatientId;
    private String selectedPatientCodeName;
    private String selectedStayId;
    private String selectedPhysiotherapistId;
    private String selectedLanguage;
    private String spinnerStatus = "failed";
    private String stayStatus = "failed";
    private String access_token;

    public PhysioRegisterPatient(){
        //Empty Constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_physio_register_patient, container, false);

        mProgressView = (ProgressBar) rootView.findViewById(R.id.spinner_progress);
        icSucceeded = (ImageView) rootView.findViewById(R.id.ic_succeeded);

        // Attach views
        customSpinner = rootView.findViewById(R.id.patient_codename);
        customSpinner.setOnItemSelectedListener(this);
        btn_register = rootView.findViewById(R.id.btnPatientRegister);
        btn_register.setOnClickListener(this);

        showProgress(true);

        // 禁止点击
        customSpinner.setEnabled(false);
        btn_register.setEnabled(false);

        Patient selectTitle = new Patient(null, 0, "Please select patient codename", -1);
        mPatients.add(selectTitle);

        CustomSelectPatientSpinnerAdapter customSelectPatientSpinnerAdapter = new CustomSelectPatientSpinnerAdapter(getActivity(), R.layout.select_patient_spinner_item, mPatients);
        customSpinner.setAdapter(customSelectPatientSpinnerAdapter);

        // GetAuth Patient CodeName
        getPatientCodeName();

        return rootView;
    }

    // Spinner Drop down elements
    private void getPatientCodeName(){
        String accessToken = GetSetSharedPreferences.getDefaults("access_token", getActivity().getApplicationContext());
        GetAuth getAuthSpinnerTask = new GetAuth(PhysioRegisterPatient.this, GET_SPINNER_TASK_ID);
        getAuthSpinnerTask.execute("http://" + IPAddress.awsipaddress + "/PhysioWebPortal/api/patients", accessToken);
    }

    // Spinner Drop down elements
    private void getStay(){
        String accessToken = GetSetSharedPreferences.getDefaults("access_token", getActivity().getApplicationContext());
        GetAuth getStayTask = new GetAuth(PhysioRegisterPatient.this, GET_STAY_TASK_ID);
        getStayTask.execute("http://" + IPAddress.awsipaddress + "/PhysioWebPortal/api/patients/" + selectedPatientId + "/hospitalstay", accessToken);
    }

    private void refreshToken() {
        String parameters = getParameters();
        PostForm refreshTokenTask = new PostForm(PhysioRegisterPatient.this, REFRESH_TOKEN_TASK_ID);
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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        icSucceeded.setVisibility(View.GONE);
        if(!TextUtils.isEmpty(mPatients.get(i).getPatientId())) {
            selectedPatientId = mPatients.get(i).getPatientId();
            selectedPatientCodeName = mPatients.get(i).getCodeName();
            selectedLanguage = mPatients.get(i).getLanguage();
        }
        else {
            selectedPatientId = null;
            selectedPatientCodeName = null;
            selectedPhysiotherapistId = null;
            selectedLanguage = null;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onClick(View view) {
        if(!TextUtils.isEmpty(selectedPatientId)) {
            if(GetSetSharedPreferences.getDefaults("patientId", getActivity().getApplicationContext()) != null) {
                String oldPatientId = GetSetSharedPreferences.getDefaults("patientId", getActivity().getApplicationContext());
                if(!selectedPatientId.equals(oldPatientId)) {
                    File videoFolder = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+ "/PhysioAssist/Video");
                    if (videoFolder.exists()) {
                        File[] videoSubFolder = videoFolder.listFiles();
                        for (int i = 0; i < videoSubFolder.length; i++)  {
                            File[] videoFile = videoSubFolder[i].listFiles();
                            for (int j = 0; j < videoFile.length; j++)  {
                                videoFile[j].delete();
                            }
                            videoSubFolder[i].delete();
                        }
                        videoFolder.delete();
                    }
                    File recordFolder = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+ "/PhysioAssist/Record");
                    if (recordFolder.exists()) {
                        File[] recordSubFolder = recordFolder.listFiles();
                        for (int i = 0; i < recordSubFolder.length; i++)  {
                            File[] recordFile = recordSubFolder[i].listFiles();
                            for (int j = 0; j < recordFile.length; j++)  {
                                recordFile[j].delete();
                            }
                            recordSubFolder[i].delete();
                        }
                        recordFolder.delete();
                    }
                }
            }
            GetSetSharedPreferences.setDefaults("patientId", selectedPatientId, getActivity().getApplicationContext());
            GetSetSharedPreferences.setDefaults("patientCodeName", selectedPatientCodeName, getActivity().getApplicationContext());
            GetSetSharedPreferences.setDefaults("language", selectedLanguage, getActivity().getApplicationContext());
            GetSetSharedPreferences.setDefaults("isNewPatient", "true", getActivity().getApplicationContext());
            getStay();
            showProgress(true);
        }
        else {
            Log.d(Config.TAG_BUTTON, "(PhysioRegisterPatient) Register patient: No patient selected");
        }
    }

    @Override
    public void onTaskCompleted(String response, int requestId) {
        if(response.contains("<html")) {
            refreshToken();
        } else {
            if(requestId == GET_SPINNER_TASK_ID) {
                boolean isJSONArray = false;
                boolean isJSONObject = false;
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    isJSONObject = true;
                    String patientId = jsonObject.getString("Id");
                    String patientCodeName = jsonObject.getString("PatientCodeName");
                    int language = jsonObject.getInt("PreferredLanguage");
                    if(language == 1) {
                        mPatients.add(new Patient(patientId, patientCodeName, "en"));
                    }
                    else if(language == 2) {
                        mPatients.add(new Patient(patientId, patientCodeName, "zh"));
                    }
                    else {
                        mPatients.add(new Patient(patientId, patientCodeName, "en"));
                    }
                    spinnerStatus = "succeeded";
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    isJSONArray = true;
                    for(int i=0; i<jsonArray.length(); i++) {
                        JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                        String patientId = jsonObject.getString("Id");
                        String patientCodeName = jsonObject.getString("PatientCodeName");
                        int language = jsonObject.getInt("PreferredLanguage");
                        if(language == 1) {
                            mPatients.add(new Patient(patientId, patientCodeName, "en"));
                        }
                        else if(language == 2) {
                            mPatients.add(new Patient(patientId, patientCodeName, "zh"));
                        }
                        else {
                            mPatients.add(new Patient(patientId, patientCodeName, "en"));
                        }
                    }
                    spinnerStatus = "succeeded";
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(isJSONArray || isJSONObject) {
                    if(spinnerStatus.equals("succeeded")) {
                        showProgress(false);
                        // 加载完才允许点击
                        customSpinner.setEnabled(true);
                        btn_register.setEnabled(true);
                    }
                }
                else if(!isJSONArray && !isJSONObject) {
                    spinnerStatus = "succeeded";
                    if(spinnerStatus.equals("succeeded")) {
                        showProgress(false);
                        // 加载完才允许点击
                        customSpinner.setEnabled(true);
                        btn_register.setEnabled(true);
                    }
                }
            }
            else if(requestId == GET_STAY_TASK_ID) {
                boolean isJSONArray = false;
                boolean isJSONObject = false;
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    isJSONObject = true;
                    selectedStayId = String.valueOf(jsonObject.getInt("StayId"));
                    selectedPhysiotherapistId = String.valueOf(jsonObject.getString("PhysiotherapistId"));
                    GetSetSharedPreferences.setDefaults("stayId", selectedStayId, getActivity().getApplicationContext());
                    GetSetSharedPreferences.setDefaults("physiotherapistId", selectedPhysiotherapistId, getActivity().getApplicationContext());
                    stayStatus = "succeeded";
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    isJSONArray = true;
                    JSONObject jsonObject = new JSONObject(jsonArray.getString(jsonArray.length()-1));
                    selectedStayId = String.valueOf(jsonObject.getInt("StayId"));
                    selectedPhysiotherapistId = String.valueOf(jsonObject.getString("PhysiotherapistId"));
                    GetSetSharedPreferences.setDefaults("stayId", selectedStayId, getActivity().getApplicationContext());
                    GetSetSharedPreferences.setDefaults("physiotherapistId", selectedPhysiotherapistId, getActivity().getApplicationContext());
                    stayStatus = "succeeded";
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(isJSONArray || isJSONObject) {
                    if(stayStatus.equals("succeeded")) {
                        showProgress(false);
                        icSucceeded.setVisibility(View.VISIBLE);
                        CustomToast.show(getActivity(), "Patient registered successfully!");
                    }
                }
                else if(!isJSONArray && !isJSONObject) {
                    stayStatus = "succeeded";
                    if(stayStatus.equals("succeeded")) {
                        showProgress(false);
                        icSucceeded.setVisibility(View.VISIBLE);
                        CustomToast.show(getActivity(), "Patient registered successfully!");
                    }
                }
            }
            else if(requestId == REFRESH_TOKEN_TASK_ID) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    access_token = jsonObject.getString("access_token");
                    GetSetSharedPreferences.setDefaults("access_token", access_token, getActivity().getApplicationContext());
                    getPatientCodeName();
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
