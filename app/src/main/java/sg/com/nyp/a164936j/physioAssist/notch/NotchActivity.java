package sg.com.nyp.a164936j.physioAssist.notch;

import android.content.Intent;
import android.os.Bundle;

import com.wearnotch.service.NotchAndroidService;

import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.notch.base.BaseActivity;
import sg.com.nyp.a164936j.physioAssist.notch.init.InitFragment;
import sg.com.nyp.a164936j.physioAssist.notch.settings.SettingsFragment;

public class NotchActivity extends BaseActivity {

    private int exerciseId;
    private int prescribedExId;
    private String exerciseName;
    private int exerciseType;
    private int exerciseSetNo;
    private int exerciseRepNo;
    private int exTimePerDay;
    private String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notch);
        if (getIntent().hasExtra("exerciseId")) {
            exerciseId = getIntent().getIntExtra("exerciseId", 1);
        }
        if (getIntent().hasExtra("prescribedExId")) {
            prescribedExId = getIntent().getIntExtra("prescribedExId", 1);
        }
        if (getIntent().hasExtra("exerciseName")) {
            exerciseName = getIntent().getStringExtra("exerciseName");
        }
        if (getIntent().hasExtra("exerciseType")) {
            exerciseType = getIntent().getIntExtra("exerciseType", 1);
        }
        if (getIntent().hasExtra("exerciseSetNo")) {
            exerciseSetNo = getIntent().getIntExtra("exerciseSetNo", 1);
        }
        if (getIntent().hasExtra("exerciseRepNo")) {
            exerciseRepNo = getIntent().getIntExtra("exerciseRepNo", 1);
        }
        if (getIntent().hasExtra("exTimePerDay")) {
            exTimePerDay = getIntent().getIntExtra("exTimePerDay", 1);
        }

        if (savedInstanceState == null) {
            if(getIntent().hasExtra("settings")) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.container, SettingsFragment.newInstance())
                        .commit();
            }
            else {
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.container, InitFragment.newInstance(exerciseId, prescribedExId, exerciseName, exerciseType, exerciseSetNo, exerciseRepNo, exTimePerDay))
                        .commit();
            }
        }

        Intent controlServiceIntent = new Intent(this, NotchAndroidService.class);
        startService(controlServiceIntent);

        // to develop app UI without notches you can use a 'mock' version of the SDK
        // it returns success for all SDK calls, to use it uncomment this line
        // controlServiceIntent.putExtra("MOCK", true);

        bindService(controlServiceIntent, this, BIND_AUTO_CREATE);
    }

    public void gotoSettingsFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, SettingsFragment.newInstance())
                .commit();
    }
}