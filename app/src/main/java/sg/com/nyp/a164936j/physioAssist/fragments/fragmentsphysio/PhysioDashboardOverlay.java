package sg.com.nyp.a164936j.physioAssist.fragments.fragmentsphysio;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.configuration.GetSetSharedPreferences;

public class PhysioDashboardOverlay extends Fragment {

    private TextView welcomePhysio;

    private String username;

    public PhysioDashboardOverlay() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_physio_dashboard_overlay, container, false);

        if(GetSetSharedPreferences.getDefaults("username", getActivity().getApplicationContext()) != null) {
            username = GetSetSharedPreferences.getDefaults("username", getActivity().getApplicationContext());
        }
        else {
            username = "physiotherapist";
        }

        welcomePhysio = rootView.findViewById(R.id.welcome_physio);
        welcomePhysio.setText(getResources().getString(R.string.welcome_physio) + username);

        return rootView;
    }
}
