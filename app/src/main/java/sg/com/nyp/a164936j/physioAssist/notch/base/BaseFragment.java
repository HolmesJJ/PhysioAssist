package sg.com.nyp.a164936j.physioAssist.notch.base;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.wearnotch.service.network.NotchService;

import sg.com.nyp.a164936j.physioAssist.notch.interfaces.NotchServiceConnection;

/**
 * Base class for fragments.
 */
public class BaseFragment extends Fragment implements NotchServiceConnection {

    protected Context mApplicationContext;
    protected NotchService mNotchService;

    protected void bindNotchService() {
        BaseActivity activity = (BaseActivity) getActivity();
        if (activity != null) {
            activity.addNotchServiceConnection(this);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplicationContext = getActivity().getApplicationContext();
    }

    public boolean onBackPressed() {
        return false;
    }

    public BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    /**
     * Override point for subclasses.
     */
    @Override
    public void onServiceConnected(NotchService notchService) {
        mNotchService = notchService;
    }

    /**
     * Override point for subclasses.
     */
    @Override
    public void onServiceDisconnected() {
        mNotchService = null;
    }

    protected void fireInvalidateOptionsMenu() {
        if (isAdded()) {
            getActivity().runOnUiThread(mInvalidateOptionsMenu);
        }
    }

    private Runnable mInvalidateOptionsMenu = new Runnable() {
        @Override
        public void run() {
            if (isAdded()) {
                getActivity().invalidateOptionsMenu();
            }
        }
    };

}
