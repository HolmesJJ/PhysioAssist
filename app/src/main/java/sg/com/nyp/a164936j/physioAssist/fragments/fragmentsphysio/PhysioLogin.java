package sg.com.nyp.a164936j.physioAssist.fragments.fragmentsphysio;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONObject;

import java.net.URLEncoder;

import sg.com.nyp.a164936j.physioAssist.httpasynctask.PostForm;
import sg.com.nyp.a164936j.physioAssist.IPAddress;
import sg.com.nyp.a164936j.physioAssist.PhysioDashboard;
import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.configuration.Config;
import sg.com.nyp.a164936j.physioAssist.configuration.GetSetSharedPreferences;
import sg.com.nyp.a164936j.physioAssist.fragments.UserSelection;
import sg.com.nyp.a164936j.physioAssist.fragments.interfaces.OnTaskCompleted;

/**
 * A simple {@link Fragment} subclass.
 */
public class PhysioLogin extends Fragment implements OnTaskCompleted {

    private static final int SKIPLOGIN = 100;

    private static final int LOGIN_TASK_ID = 1;

    private Button btnPhysioLogin, backClick;
    private EditText edt_physio_email, edt_physio_pass;
    private ProgressBar mProgressView;

    private String status;
    private String username;
    private String password;
    private String access_token;

    public PhysioLogin() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_physio_login, container, false);

        mProgressView = rootView.findViewById(R.id.login_progress);
        edt_physio_email = rootView.findViewById(R.id.edt_physio_email);
        edt_physio_pass = rootView.findViewById(R.id.edt_physio_pass);

        btnPhysioLogin = rootView.findViewById(R.id.btnPhysioLogin);
        btnPhysioLogin.setOnClickListener((View view) -> {
            username = edt_physio_email.getText().toString();
            password = edt_physio_pass.getText().toString();

            if(TextUtils.isEmpty(username)) {
                edt_physio_email.setError("Please fill in your username");
            }
            else if(TextUtils.isEmpty(password)) {
                edt_physio_pass.setError("Please fill in your password");
            }
            else {
                String parameters = getParameters();
                PostForm loginTask = new PostForm(PhysioLogin.this, LOGIN_TASK_ID);
                loginTask.execute("http://" + IPAddress.ipaddress + "/token", parameters);
                showProgress(true);
            }
        });

        backClick = rootView.findViewById(R.id.backBtn);
        backClick.setOnClickListener((View view) -> {
            Log.d(Config.TAG_BUTTON, "(PhysioLogin) Physio back btn pressed");
            getActivity().onBackPressed();
        });

        return rootView;
    }

    // Convert information to JSON string
    public String getParameters() {
        String parameters = "";
        try {
            String firstParameter = "username=" + URLEncoder.encode(username, "UTF-8");
            String secondParameter = "password=" + URLEncoder.encode(password, "UTF-8");
            String thirdParameter = "grant_type=" + URLEncoder.encode("password", "UTF-8");
            parameters = firstParameter + "&&" + secondParameter + "&&" + thirdParameter;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parameters;
    }

    @Override
    public void onTaskCompleted(String response, int requestId) {
        if(requestId == LOGIN_TASK_ID) {
            retrieveFromJSON(response);
            showProgress(false);

            // if response is from upload request
            if (status.equals("succeeded")){
                Log.d(Config.TAG_BUTTON, "(PhysioLogin) Physio logged in");
                GetSetSharedPreferences.setDefaults("username", username, getActivity().getApplicationContext());
                GetSetSharedPreferences.setDefaults("password", password, getActivity().getApplicationContext());
                GetSetSharedPreferences.setDefaults("access_token", access_token, getActivity().getApplicationContext());
                startActivityForResult(new Intent(getContext(), PhysioDashboard.class), SKIPLOGIN);
            }
            else {
                String temp = "Failed!";
                Toast.makeText(getActivity(), temp, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Retrieve information from JSON string
    private void retrieveFromJSON(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            access_token = jsonObject.getString("access_token");
            status = "succeeded";
        } catch (Exception e) {
            e.printStackTrace();
            status = "failed";
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (SKIPLOGIN) : {
                if (resultCode == Activity.RESULT_OK) {
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    UserSelection userSelection = new UserSelection();
                    manager.beginTransaction()
                            .replace(R.id.fragment_container, userSelection).addToBackStack("tag")
                            .commit();
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
