package sg.com.nyp.a164936j.physioAssist.fragments.fragmentspatient;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import sg.com.nyp.a164936j.physioAssist.BlankCanvas;
import sg.com.nyp.a164936j.physioAssist.MainActivity;
import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.configuration.Config;
import sg.com.nyp.a164936j.physioAssist.configuration.GetSetSharedPreferences;
import sg.com.nyp.a164936j.physioAssist.notch.NotchActivity;
import sg.com.nyp.a164936j.physioAssist.tutorials.Tutorials;

/**
 * A simple {@link Fragment} subclass.
 */
public class PatientActivities extends Fragment implements View.OnClickListener{

    private Context context;
    private Resources resources;
    private TextView welcomePatient;
    private Button btnExercise, btnProgress, btnChallenges, btnLeaderBoard, btnHome, btnTutorial, btnSettings;

    private String language;
    private String patientCodeName;

    public PatientActivities() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_patient_activities, container, false);
        initView(rootView);

        patientCodeName = GetSetSharedPreferences.getDefaults("patientCodeName", getActivity().getApplicationContext());
        language = GetSetSharedPreferences.getDefaults("language", getActivity().getApplicationContext());

        MainActivity mainActivity = (MainActivity) getActivity();
        resources = mainActivity.updateViews(context, language);
        initLanguage();

        btnHome.setOnClickListener((View view) -> {
            Log.d(Config.TAG_BUTTON, "(PatientActivities) btnHome");
            getActivity().onBackPressed();
        });

        btnTutorial.setOnClickListener((View view) -> {
            Log.d(Config.TAG_BUTTON, "(PatientActivities) btnTutorial");
            //TODO: Create Guide Step Tutorial
            startActivity(new Intent(getContext(), Tutorials.class));
        });

        btnSettings.setOnClickListener((View view) -> {
            Log.d(Config.TAG_BUTTON, "(PatientActivities) btnSettings");
            //TODO: Create Guide Step Tutorial
            Intent intent = new Intent(getContext(), NotchActivity.class);
            intent.putExtra("settings", "settings");
            startActivity(intent);
        });

        btnExercise.setOnClickListener(this);
        btnProgress.setOnClickListener(this);
        btnLeaderBoard.setOnClickListener(this);
        btnChallenges.setOnClickListener(this);

        return rootView;
    }

    private void initView(View rootView) {
        context = getContext();
        welcomePatient = rootView.findViewById(R.id.patient_activity_welcome_patient);
        btnExercise = rootView.findViewById(R.id.patient_activity_btnExercise);
        btnChallenges = rootView.findViewById(R.id.patient_activity_btnChallenges);
        btnLeaderBoard = rootView.findViewById(R.id.patient_activity_btnLeaderboard);
        btnProgress = rootView.findViewById(R.id.patient_activity_btnProgress);
        btnHome = rootView.findViewById(R.id.patient_activity_btnBack);
        btnTutorial = rootView.findViewById(R.id.patient_activity_btnTutorial);
        btnSettings = rootView.findViewById(R.id.patient_activity_btnSettings);
    }

    private void initLanguage() {
        welcomePatient.setText(resources.getString(R.string.patient_activity_welcome_patient) + patientCodeName);
        btnExercise.setText(resources.getString(R.string.patient_activity_btnExercise));
        btnChallenges.setText(resources.getString(R.string.patient_activity_btnChallenges));
        btnLeaderBoard.setText(resources.getString(R.string.patient_activity_btnLeaderboard));
        btnProgress.setText(resources.getString(R.string.patient_activity_btnProgress));
        btnTutorial.setText(resources.getString(R.string.patient_activity_btnTutorial));
        btnSettings.setText(resources.getString(R.string.patient_activity_btnSettings));
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(getContext(), BlankCanvas.class);
        switch(view.getId()){
            case R.id.patient_activity_btnExercise:
                intent.putExtra("BTNTYPE", "exercise");
                startActivity(intent);
                break;
            case R.id.patient_activity_btnChallenges:
                intent.putExtra("BTNTYPE", "challenge");
                startActivity(intent);
                break;
            case R.id.patient_activity_btnLeaderboard:
                intent.putExtra("BTNTYPE", "leaderboard");
                startActivity(intent);
                break;
            case R.id.patient_activity_btnProgress:
                intent.putExtra("BTNTYPE", "progress");
                startActivity(intent);
                break;
        }
    }
}
