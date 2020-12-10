package sg.com.nyp.a164936j.physioAssist;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import sg.com.nyp.a164936j.physioAssist.configuration.GetSetSharedPreferences;
import sg.com.nyp.a164936j.physioAssist.fragments.UserSelection;
import sg.com.nyp.a164936j.physioAssist.language.LocaleHelper;


public class MainActivity extends AppCompatActivity{

    private static final int PERMISSION_ALL = 100;
    String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(GetSetSharedPreferences.getDefaults("language", getApplicationContext()) == null) {
            GetSetSharedPreferences.setDefaults("language", "en", getApplicationContext());
        }

        UserSelection userSelection = new UserSelection();

        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(
                R.id.fragment_container,
                userSelection,
                userSelection.getTag())
                .commit();

        if(!hasPermissions(MainActivity.this, PERMISSIONS)) {
            MainActivity.this.requestPermissions(PERMISSIONS, PERMISSION_ALL);
        }
    }

    public boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode == PERMISSION_ALL) {
            if (grantResults.length > 0) {
                int j = 0;
                for(int i=0; i<grantResults.length; i++) {
                    if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        j++;
                        break;
                    }
                }
                if(j != 0) {
                    Toast.makeText(MainActivity.this, "Please turn on your permissions", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            else {
                Toast.makeText(MainActivity.this, "Please turn on your permissions manually", Toast.LENGTH_SHORT).show();
                finish();
            }
            return;
        }
    }

    public Resources updateViews(Context context, String languageCode) {
        context = LocaleHelper.setLocale(this, languageCode);
        return context.getResources();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }
}
