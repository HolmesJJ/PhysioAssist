package sg.com.nyp.a164936j.physioAssist.notch.settings;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wearnotch.db.NotchDataBase;
import com.wearnotch.db.model.Device;
import com.wearnotch.framework.ActionDevice;
import com.wearnotch.framework.Bone;
import com.wearnotch.framework.NotchChannel;
import com.wearnotch.framework.NotchNetwork;
import com.wearnotch.service.common.NotchError;

import java.util.Iterator;
import java.util.Map;

import sg.com.nyp.a164936j.physioAssist.EmptyCallback;
import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.notch.base.BaseFragment;
import sg.com.nyp.a164936j.physioAssist.notch.util.Util;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class SettingsFragment extends BaseFragment implements View.OnClickListener {

    private NotchDataBase mNotchDataBase;
    private NotchChannel mSelectedChannel;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private ProgressBar mCircleProgressView;
    private ProgressBar mHorizontalProgressView;
    private TextView mDeviceList;

    // Buttons
    private Button mButtonPair;
    private Button mButtonSyncPair;
    private Button mButtonRemove;
    private Button mButtonEase;
    private Button mButtonClose;


    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bindNotchService();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNotchDataBase = NotchDataBase.getInst();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        initView(root);

        startProgress();

        // Start Notch
        mHandler.postDelayed(mSetDefaultUser, 1000L);

        return root;
    }

    private void initView(View v) {
        mCircleProgressView = (ProgressBar) v.findViewById(R.id.circle_progress);
        mHorizontalProgressView = (ProgressBar) v.findViewById(R.id.horizontal_progress);
        mDeviceList = (TextView) v.findViewById(R.id.device_list);
        mButtonPair = (Button) v.findViewById(R.id.pair_new_device);
        mButtonSyncPair = (Button) v.findViewById(R.id.sync_pairing);
        mButtonRemove = (Button) v.findViewById(R.id.remove_all_devices);
        mButtonEase = (Button) v.findViewById(R.id.ease_all_devices);
        mButtonClose = (Button) v.findViewById(R.id.settings_btn_close);

        mButtonPair.setOnClickListener(this);
        mButtonSyncPair.setOnClickListener(this);
        mButtonRemove.setOnClickListener(this);
        mButtonEase.setOnClickListener(this);
        mButtonClose.setOnClickListener(this);
    }

    private void disableButton() {
        mButtonPair.setEnabled(false);
        mButtonSyncPair.setEnabled(false);
        mButtonRemove.setEnabled(false);
        mButtonEase.setEnabled(false);
    }

    private void enableButton() {
        mButtonPair.setEnabled(true);
        mButtonSyncPair.setEnabled(true);
        mButtonRemove.setEnabled(true);
        mButtonEase.setEnabled(true);
    }

    Runnable mSetDefaultUser = () -> {
        if (mNotchService != null) {
            mNotchService.setLicense(getString(R.string.default_user_license));
            // 尝试连接
            mNotchService.disconnect(new EmptyCallback<Void>(){
                @Override
                public void onSuccess(Void aVoid) {
                    mHorizontalProgressView.setProgress(30);
                    mNotchService.uncheckedInit(mSelectedChannel, new EmptyCallback<NotchNetwork>() {
                        @Override
                        public void onSuccess(NotchNetwork notchNetwork) {
                            super.onSuccess(notchNetwork);
                            mHorizontalProgressView.setProgress(60);
                            // 连接成功可以根据notchNetwork获取已经连接的设备
                            // 有一个notch打开，而且这个notch是已经匹配，就可以成功连接
                            mNotchService.syncPairedDevices(new EmptyCallback<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    super.onSuccess(aVoid);
                                    // 传入已连接设备
                                    try {
                                        updateUser(mNotchService.getLicense(), notchNetwork.getDevices());
                                        Util.showNotification("Loaded successfully");
                                        finishProgress();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    mHorizontalProgressView.setProgress(100);
                                }
                                @Override
                                public void onFailure(NotchError notchError) {
                                    super.onFailure(notchError);
                                    try {
                                        Util.showNotchError(notchError);
                                        finishProgress();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    mHorizontalProgressView.setProgress(100);
                                }
                            });
                        }
                        @Override
                        public void onFailure(NotchError notchError) {
                            super.onFailure(notchError);
                            mHorizontalProgressView.setProgress(60);
                            // 连接失败
                            mNotchService.syncPairedDevices(new EmptyCallback<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    super.onSuccess(aVoid);
                                    // 没有连接设备，只能传入空值
                                    try {
                                        updateUser(mNotchService.getLicense(), null);
                                        Util.showNotification("Loaded successfully");
                                        finishProgress();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    mHorizontalProgressView.setProgress(100);
                                }
                                @Override
                                public void onFailure(NotchError notchError) {
                                    super.onFailure(notchError);
                                    try {
                                        Util.showNotchError(notchError);
                                        finishProgress();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    mHorizontalProgressView.setProgress(100);
                                }
                            });
                        }
                    });
                }
                @Override
                public void onFailure(NotchError notchError) {
                    super.onFailure(notchError);
                    // 连接失败
                    try {
                        Util.showNotchError(notchError);
                        finishProgress();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mHorizontalProgressView.setProgress(100);
                }
            });
        }
    };

    @Override
    public void onClick(View view) {
        mHorizontalProgressView.setProgress(0);
        switch (view.getId()) {
            case R.id.pair_new_device:
                startProgress();
                mNotchService.pair(new EmptyCallback<Device>() {
                    @Override
                    public void onSuccess(Device device) {
                        mHorizontalProgressView.setProgress(50);
                        updateUser(mNotchService.getLicense(), null);
                        mNotchService.shutDown(new EmptyCallback<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                super.onSuccess(aVoid);
                                Util.showNotification("Pair device successfully");
                                finishProgress();
                                mHorizontalProgressView.setProgress(100);
                            }
                            @Override
                            public void onFailure(NotchError notchError) {
                                super.onFailure(notchError);
                                Util.showNotchError(notchError);
                                finishProgress();
                                mHorizontalProgressView.setProgress(100);
                            }
                        });
                    }
                    @Override
                    public void onFailure(NotchError notchError) {
                        super.onFailure(notchError);
                        Util.showNotchError(notchError);
                        finishProgress();
                        mHorizontalProgressView.setProgress(100);
                    }
                });
                break;
            case R.id.sync_pairing:
                startProgress();
                // 先判断是否已经连接notch
                if(mNotchService.isConnected()) {
                    mNotchService.uncheckedInit(mSelectedChannel, new EmptyCallback<NotchNetwork>() {
                        @Override
                        public void onSuccess(NotchNetwork notchNetwork) {
                            super.onSuccess(notchNetwork);
                            mHorizontalProgressView.setProgress(50);
                            mNotchService.syncPairedDevices(new EmptyCallback<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    super.onSuccess(aVoid);
                                    updateUser(mNotchService.getLicense(), notchNetwork.getDevices());
                                    Util.showNotification("Sync successfully");
                                    finishProgress();
                                    mHorizontalProgressView.setProgress(100);
                                }
                                @Override
                                public void onFailure(NotchError notchError) {
                                    super.onFailure(notchError);
                                    Util.showNotchError(notchError);
                                    finishProgress();
                                    mHorizontalProgressView.setProgress(100);
                                }
                            });
                        }
                        @Override
                        public void onFailure(NotchError notchError) {
                            super.onFailure(notchError);
                            mHorizontalProgressView.setProgress(50);
                            mNotchService.syncPairedDevices(new EmptyCallback<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    super.onSuccess(aVoid);
                                    updateUser(mNotchService.getLicense(), null);
                                    Util.showNotification("Sync successfully");
                                    finishProgress();
                                    mHorizontalProgressView.setProgress(100);
                                }
                                @Override
                                public void onFailure(NotchError notchError) {
                                    super.onFailure(notchError);
                                    Util.showNotchError(notchError);
                                    finishProgress();
                                    mHorizontalProgressView.setProgress(100);
                                }
                            });
                        }
                    });
                }
                else {
                    mNotchService.disconnect(new EmptyCallback<Void>(){
                        @Override
                        public void onSuccess(Void aVoid) {
                            mHorizontalProgressView.setProgress(30);
                            mNotchService.uncheckedInit(mSelectedChannel, new EmptyCallback<NotchNetwork>() {
                                @Override
                                public void onSuccess(NotchNetwork notchNetwork) {
                                    super.onSuccess(notchNetwork);
                                    mHorizontalProgressView.setProgress(60);
                                    mNotchService.syncPairedDevices(new EmptyCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            super.onSuccess(aVoid);
                                            updateUser(mNotchService.getLicense(), notchNetwork.getDevices());
                                            Util.showNotification("Sync successfully");
                                            finishProgress();
                                            mHorizontalProgressView.setProgress(100);
                                        }
                                        @Override
                                        public void onFailure(NotchError notchError) {
                                            super.onFailure(notchError);
                                            Util.showNotchError(notchError);
                                            finishProgress();
                                            mHorizontalProgressView.setProgress(100);
                                        }
                                    });
                                }
                                @Override
                                public void onFailure(NotchError notchError) {
                                    super.onFailure(notchError);
                                    mHorizontalProgressView.setProgress(60);
                                    mNotchService.syncPairedDevices(new EmptyCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            super.onSuccess(aVoid);
                                            updateUser(mNotchService.getLicense(), null);
                                            Util.showNotification("Sync successfully");
                                            finishProgress();
                                            mHorizontalProgressView.setProgress(100);
                                        }
                                        @Override
                                        public void onFailure(NotchError notchError) {
                                            super.onFailure(notchError);
                                            Util.showNotchError(notchError);
                                            finishProgress();
                                            mHorizontalProgressView.setProgress(100);
                                        }
                                    });
                                }
                            });
                        }
                        @Override
                        public void onFailure(NotchError notchError) {
                            super.onFailure(notchError);
                            Util.showNotchError(notchError);
                            finishProgress();
                            mHorizontalProgressView.setProgress(100);
                        }
                    });
                }
                break;
            case R.id.remove_all_devices:
                startProgress();
                // 先判断是否已经连接notch
                if(mNotchService.isConnected()) {
                    mNotchService.deletePairedDevices(null, new EmptyCallback<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            super.onSuccess(aVoid);
                            mHorizontalProgressView.setProgress(50);
                            mNotchService.disconnect(new EmptyCallback<Void>(){
                                @Override
                                public void onSuccess(Void aVoid) {
                                    super.onSuccess(aVoid);
                                    updateUser(mNotchService.getLicense(), null);
                                    Util.showNotification("Delete successfully");
                                    finishProgress();
                                    mHorizontalProgressView.setProgress(100);
                                }
                                @Override
                                public void onFailure(NotchError notchError) {
                                    super.onFailure(notchError);
                                    updateUser(mNotchService.getLicense(), null);
                                    Util.showNotification("Delete successfully");
                                    finishProgress();
                                    mHorizontalProgressView.setProgress(100);
                                }
                            });
                        }
                        @Override
                        public void onFailure(NotchError notchError) {
                            super.onFailure(notchError);
                            Util.showNotchError(notchError);
                            finishProgress();
                            mHorizontalProgressView.setProgress(100);
                        }
                    });
                }
                else {
                    mNotchService.deletePairedDevices(null, new EmptyCallback<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            super.onSuccess(aVoid);
                            updateUser(mNotchService.getLicense(), null);
                            Util.showNotification("Delete successfully");
                            finishProgress();
                            mHorizontalProgressView.setProgress(100);
                        }
                        @Override
                        public void onFailure(NotchError notchError) {
                            super.onFailure(notchError);
                            Util.showNotchError(notchError);
                            finishProgress();
                            mHorizontalProgressView.setProgress(100);
                        }
                    });
                }
                break;
            case R.id.ease_all_devices:
                startProgress();
                // 先判断是否已经连接notch
                if(mNotchService.isConnected()) {
                    mNotchService.uncheckedInit(mSelectedChannel, new EmptyCallback<NotchNetwork>() {
                        @Override
                        public void onSuccess(NotchNetwork notchNetwork) {
                            super.onSuccess(notchNetwork);
                            mHorizontalProgressView.setProgress(50);
                            mNotchService.erase(new EmptyCallback<Void>(){
                                @Override
                                public void onSuccess(Void aVoid) {
                                    super.onSuccess(aVoid);
                                    updateUser(mNotchService.getLicense(), notchNetwork.getDevices());
                                    Util.showNotification("Ease successfully");
                                    finishProgress();
                                    mHorizontalProgressView.setProgress(100);
                                }
                                @Override
                                public void onFailure(NotchError notchError) {
                                    super.onFailure(notchError);
                                    updateUser(mNotchService.getLicense(), notchNetwork.getDevices());
                                    Util.showNotchError(notchError);
                                    finishProgress();
                                    mHorizontalProgressView.setProgress(100);
                                }
                            });
                        }
                        @Override
                        public void onFailure(NotchError notchError) {
                            super.onFailure(notchError);
                            Util.showNotchError(notchError);
                            finishProgress();
                            mHorizontalProgressView.setProgress(100);
                        }
                    });
                }
                else {
                    mNotchService.disconnect(new EmptyCallback<Void>(){
                        @Override
                        public void onSuccess(Void aVoid) {
                            mHorizontalProgressView.setProgress(30);
                            mNotchService.uncheckedInit(mSelectedChannel, new EmptyCallback<NotchNetwork>() {
                                @Override
                                public void onSuccess(NotchNetwork notchNetwork) {
                                    super.onSuccess(notchNetwork);
                                    mHorizontalProgressView.setProgress(60);
                                    mNotchService.erase(new EmptyCallback<Void>(){
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            super.onSuccess(aVoid);
                                            updateUser(mNotchService.getLicense(), notchNetwork.getDevices());
                                            Util.showNotification("Ease successfully");
                                            finishProgress();
                                            mHorizontalProgressView.setProgress(100);
                                        }
                                        @Override
                                        public void onFailure(NotchError notchError) {
                                            super.onFailure(notchError);
                                            updateUser(mNotchService.getLicense(), notchNetwork.getDevices());
                                            Util.showNotchError(notchError);
                                            finishProgress();
                                            mHorizontalProgressView.setProgress(100);
                                        }
                                    });
                                }
                                @Override
                                public void onFailure(NotchError notchError) {
                                    super.onFailure(notchError);
                                    Util.showNotchError(notchError);
                                    finishProgress();
                                    mHorizontalProgressView.setProgress(100);
                                }
                            });
                        }
                        @Override
                        public void onFailure(NotchError notchError) {
                            super.onFailure(notchError);
                            Util.showNotchError(notchError);
                            finishProgress();
                            mHorizontalProgressView.setProgress(100);
                        }
                    });
                }
                break;
            case R.id.settings_btn_close:
                getActivity().finish();
                break;
        }
    }

    private void updateUser(final String user, Map<Bone, ActionDevice> refreshDevices){
        getActivity().runOnUiThread(() -> {
            if (mNotchDataBase == null) {
                mNotchDataBase = NotchDataBase.getInst();
            }
            StringBuilder sb = new StringBuilder();
            // 如果notch数据库有设备（之前匹配过）
            if(mNotchDataBase.findAllDevices(user).size() > 0) {
                // 从notch数据库中获取全部设备
                for (Device device : mNotchDataBase.findAllDevices(user)) {
                    boolean isConnected = false;
                    // 如果有已连接设备
                    if(refreshDevices != null) {
                        Iterator<Bone> iterator = refreshDevices.keySet().iterator();
                        while(iterator.hasNext()){
                            Bone bone = iterator.next();
                            ActionDevice actionDevice = refreshDevices.get(bone);
                            if(actionDevice.getDeviceMac().equals(device.getNotchDevice().getDeviceMac())){
                                sb.append("(Connected)   ");
                                isConnected = true;
                                break;
                            }
                        }
                    }

                    // 无法获取上次匹配时间
                    if(device.getLastSeen() == null || device.getLastSeen() == 0) {
                        sb.append("(Last seen undefined time)   ");
                    }
                    else {
                        // 如果没有已连接设备
                        if(!isConnected) {
                            long diffTimeStamp = (System.currentTimeMillis() - device.getLastSeen()) / 1000L;
                            if((diffTimeStamp / 60) == 0) {
                                sb.append("(Last seen " + diffTimeStamp + " seconds ago)   ");
                            }
                            else if((diffTimeStamp / 60) > 0 && (diffTimeStamp / 3600) == 0) {
                                sb.append("(Last seen " + (diffTimeStamp / 60) + " minutes ago)   ");
                            }
                            else if((diffTimeStamp / 3600) > 0 && (diffTimeStamp / 86400) == 0) {
                                sb.append("(Last seen " + (diffTimeStamp / 3600) + " hours ago)   ");
                            }
                            else {
                                sb.append("(Last seen " + (diffTimeStamp / 86400) + " days ago)   ");
                            }
                        }
                    }

                    sb.append("Notch " + device.getNotchDevice().getNetworkId() + "   ");
                    sb.append("Memory: " + device.getMemoryPercent() + "%   ");
                    sb.append("Battery: " + device.getBatteryPercent() + "%   ");
                    sb.append("\n");
                }
                mDeviceList.setText(sb.toString());
            }
            else {
                mDeviceList.setText("No Device...");
            }
        });
    }

    private void startProgress() {
        disableButton();
        showProgress(true);
    }

    private void finishProgress() {
        getActivity().runOnUiThread(() -> {
            showProgress(false);
            enableButton();
        });
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mCircleProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mCircleProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCircleProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mCircleProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
