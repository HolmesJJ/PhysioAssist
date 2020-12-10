package sg.com.nyp.a164936j.physioAssist.fragments;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import sg.com.nyp.a164936j.physioAssist.CustomToast;
import sg.com.nyp.a164936j.physioAssist.MainActivity;
import sg.com.nyp.a164936j.physioAssist.PhysioDashboard;
import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.configuration.Config;
import sg.com.nyp.a164936j.physioAssist.configuration.GetSetSharedPreferences;
import sg.com.nyp.a164936j.physioAssist.fragments.fragmentspatient.PatientActivities;
import sg.com.nyp.a164936j.physioAssist.fragments.fragmentsphysio.PhysioLogin;
import sg.com.nyp.a164936j.physioAssist.fragments.interfaces.CustomOnClickListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserSelection extends Fragment implements View.OnClickListener, CustomOnClickListener {

    private Context context;
    private Resources resources;
    private TextView fullAppName;
    private Button btnPatient;
    private Button btnPhysio;

    private String language;

    public UserSelection() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_user_selection, container, false);
        initView(rootView);

        language = GetSetSharedPreferences.getDefaults("language", getActivity().getApplicationContext());

        MainActivity mainActivity = (MainActivity) getActivity();
        resources = mainActivity.updateViews(context, language);
        initLanguage();

        btnPatient.setOnClickListener(this);
        btnPhysio.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(GetSetSharedPreferences.getDefaults("isNewPatient", getActivity().getApplicationContext()) != null) {
            language = GetSetSharedPreferences.getDefaults("language", getActivity().getApplicationContext());
            MainActivity mainActivity = (MainActivity) getActivity();
            resources = mainActivity.updateViews(context, language);
            initLanguage();
            GetSetSharedPreferences.removeDefaults("isNewPatient", getActivity().getApplicationContext());
        }
    }

    private void initView(View rootView) {
        context = getContext();
        fullAppName = rootView.findViewById(R.id.fullAppName);
        btnPatient = rootView.findViewById(R.id.btnPatient);
        btnPhysio = rootView.findViewById(R.id.btnPhysio);
    }

    private void initLanguage() {
        fullAppName.setText(resources.getString(R.string.full_app_name));
        btnPatient.setText(resources.getString(R.string.btn_patient));
        btnPhysio.setText(resources.getString(R.string.btn_Physio));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnPatient:
                Log.d(Config.TAG_BUTTON, "(UserSelection) Patient button are being pressed");

                if(GetSetSharedPreferences.getDefaults("patientId", getActivity().getApplicationContext()) != null) {
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    PatientActivities patientActivities = new PatientActivities();
                    manager.beginTransaction()
                            .replace(R.id.fragment_container, patientActivities)
                            .addToBackStack("tag")
                            .commit();
                } else {
                    CustomToast.show(getActivity(), "Please register patient first");
                }
                break;
            case R.id.btnPhysio:
                Log.d(Config.TAG_BUTTON, "(UserSelection) Physiotherapist button are being pressed");

                if(GetSetSharedPreferences.getDefaults("access_token", getActivity().getApplicationContext()) != null) {
                    Log.d(Config.TAG_BUTTON, "(PhysioLogin) Physio logged in");
                    startActivity(new Intent(getContext(), PhysioDashboard.class));
                }
                else {
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    PhysioLogin physioLogin = new PhysioLogin();
                    manager.beginTransaction()
                            .replace(R.id.fragment_container, physioLogin).addToBackStack("tag")
                            .commit();
                }
                break;
        }
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

    @Override
    public void onTrophyImgClick(int position) {

    }
}
