package sg.com.nyp.a164936j.physioAssist.tutorials.fragments;


import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.configuration.GetSetSharedPreferences;
import sg.com.nyp.a164936j.physioAssist.tutorials.Tutorials;

/**
 * A simple {@link Fragment} subclass.
 */
public class TutorialExercise extends Fragment {

    private Context context;
    private Resources resources;
    private TextView welcomePatient, textExercise;
    private Button btnExercise, btnProgress, btnChallenges, btnLeaderBoard, btnTutorial;

    private String language;
    private String patientCodeName;

    public TutorialExercise() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_tutorial_exercise, container, false);
        initView(rootView);

        patientCodeName = GetSetSharedPreferences.getDefaults("patientCodeName", getActivity().getApplicationContext());
        language = GetSetSharedPreferences.getDefaults("language", getActivity().getApplicationContext());

        Tutorials tutorials = (Tutorials) getActivity();
        resources = tutorials.updateViews(context, language);
        initLanguage();

        return rootView;
    }

    private void initView(View rootView) {
        context = getContext();
        welcomePatient = rootView.findViewById(R.id.patient_activity_welcome_patient);
        textExercise = rootView.findViewById(R.id.patient_tutorial_text_exercise);
        btnExercise = rootView.findViewById(R.id.patient_activity_btnExercise);
        btnChallenges = rootView.findViewById(R.id.patient_activity_btnChallenges);
        btnLeaderBoard = rootView.findViewById(R.id.patient_activity_btnLeaderboard);
        btnProgress = rootView.findViewById(R.id.patient_activity_btnProgress);
        btnTutorial = rootView.findViewById(R.id.patient_activity_btnTutorial);
    }

    private void initLanguage() {
        welcomePatient.setText(resources.getString(R.string.patient_activity_welcome_patient) + patientCodeName);
        textExercise.setText(resources.getString(R.string.patient_tutorial_text_exercise));
        btnExercise.setText(resources.getString(R.string.patient_activity_btnExercise));
        btnChallenges.setText(resources.getString(R.string.patient_activity_btnChallenges));
        btnLeaderBoard.setText(resources.getString(R.string.patient_activity_btnLeaderboard));
        btnProgress.setText(resources.getString(R.string.patient_activity_btnProgress));
        btnTutorial.setText(resources.getString(R.string.patient_activity_btnTutorial));
    }
}
