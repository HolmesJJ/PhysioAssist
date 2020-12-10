package sg.com.nyp.a164936j.physioAssist.notch.init;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wearnotch.db.NotchDataBase;
import com.wearnotch.db.model.Device;
import com.wearnotch.framework.ActionDevice;
import com.wearnotch.framework.Bone;
import com.wearnotch.framework.Measurement;
import com.wearnotch.framework.MeasurementType;
import com.wearnotch.framework.NotchChannel;
import com.wearnotch.framework.NotchNetwork;
import com.wearnotch.framework.Skeleton;
import com.wearnotch.framework.Workout;
import com.wearnotch.framework.visualiser.VisualiserData;
import com.wearnotch.internal.util.IOUtil;
import com.wearnotch.service.common.Cancellable;
import com.wearnotch.service.common.NotchCallback;
import com.wearnotch.service.common.NotchError;
import com.wearnotch.service.common.NotchProgress;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import sg.com.nyp.a164936j.physioAssist.CustomToast;
import sg.com.nyp.a164936j.physioAssist.EmptyCallback;
import sg.com.nyp.a164936j.physioAssist.IPAddress;
import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.configuration.GetSetSharedPreferences;
import sg.com.nyp.a164936j.physioAssist.customadapters.CustomDialogPatientExerciseAdapter;
import sg.com.nyp.a164936j.physioAssist.fragments.interfaces.CustomOnClickListener;
import sg.com.nyp.a164936j.physioAssist.fragments.interfaces.OnTaskCompleted;
import sg.com.nyp.a164936j.physioAssist.httpasynctask.GetAuth;
import sg.com.nyp.a164936j.physioAssist.httpasynctask.PostForm;
import sg.com.nyp.a164936j.physioAssist.notch.NotchActivity;
import sg.com.nyp.a164936j.physioAssist.notch.base.BaseFragment;
import sg.com.nyp.a164936j.physioAssist.notch.util.Util;
import sg.com.nyp.a164936j.physioAssist.notch.visualiser.VisualiserActivity;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class InitFragment extends BaseFragment implements View.OnClickListener, OnTaskCompleted, CustomOnClickListener {

    private static final int THUMBNAIL_TASK_ID = 1;
    private static final int UPLOAD_PERFORMED_EXERCISE_ID = 2;
    private static final int REFRESH_TOKEN_TASK_ID = 3;

    private static final long DEMO_TIME = 7000L;
    private static final int IS_CANCELLABLE = 100;
    private static final String IS_FIRST_TIME = "isFirstTime";
    private static final String IS_Show_Other_Exercise_Dialog = "isShowOtherExerciseDialog";
    private static final String EXERCISE_ID = "exerciseId";
    private static final String PRESCRIBED_EX_ID = "prescribedExId";
    private static final String EXERCISE_NAME = "exerciseName";
    private static final String EXERCISE_TYPE = "exerciseType";
    private static final String EXERCISE_SET_NO = "exerciseSetNo";
    private static final String EXERCISE_REP_NO = "exerciseRepNo";
    private static final String EXERCISE_TIME_PER_DAY = "exerciseTimePerDay";

    private Cancellable mCancellable;
    private Measurement mCurrentMeasurement;
    private NotchDataBase mNotchDataBase;
    private NotchChannel mSelectedChannel;
    private VisualiserActivity mVisualiserActivity;
    private VisualiserData mRealTimeData;
    private AnimationDrawable mDockAnimation;
    private Workout mWorkout;
    private Skeleton skeleton;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private View initContainer;
    private View initStatusContainer;
    private View initButtonContainer;
    private ProgressBar mCircleProgressView;
    private ProgressBar mHorizontalProgressView;
    private ImageView mDockImg;
    private Button backButton;
    private Button otherExerciseBtn;
    private ImageButton calibrationHelpButton;
    private Button calibrateButton;
    private Button configSteady1Button;
    private Button configSteady2Button;
    private Button startSteady1Button;
    private Button startSteady2Button;
    private Button captureButton;
    private TextView mCounterText;
    private AlertDialog cDialog, chDialog, pocDialog, crDialog, cnDialog, oeDialog;

    private enum State {CALIBRATION,STEADY,CAPTURE}
    private enum calibrationState {FIRST_TIME,OVER_TIME}

    private long defaultDiffTimeStamp = -1L;
    private State mState;

    private String stayId;
    private int exId;
    private int peId;
    private int exType;
    private String exName;
    private int exSetNo;
    private int exRepNo;
    private int exTimePerDay;
    private File recordFolder;
    private File recordFile;
    private String language;
    private String access_token;
    private String getThumbnailStatus = "failed";
    private String uploadExerciseStatus = "failed";

    private boolean isUploaded = false;
    private boolean getThumbnailRequest = true;
    private boolean uploadExerciseRequest = true;

    // Json
    private String NewStartTime;
    private String NewEndTime;
    private int NewExSetNo = 0;
    private int NewExRepNo = 0;
    private int NewAvgAngle = 0;
    private int NewAvgHoldDuration = 10;
    private String NewExVisualFile = "0Uu1cRRTTr6ZAsoqi3l2zA";
    private String NewLastUpdated;
    private String NewLastUpdatedBy;
    private int NewScore = 0;

    //vars
    private CustomDialogPatientExerciseAdapter adapter;
    private ListView mListView;
    private List<String> header = new ArrayList<>();

    private List<Integer> ExId1 = new ArrayList<>();
    private List<Integer> PeExId1 = new ArrayList<>();
    private List<String> ExName1 = new ArrayList<>();
    private List<Integer> ExType1 = new ArrayList<>();
    private List<Integer> ExSetNo1 = new ArrayList<>();
    private List<Integer> ExRepNo1 = new ArrayList<>();
    private List<String> ExThumbnail1 = new ArrayList<>();

    private List<Integer> ExId2 = new ArrayList<>();
    private List<Integer> PeExId2 = new ArrayList<>();
    private List<String> ExName2 = new ArrayList<>();
    private List<Integer> ExType2 = new ArrayList<>();
    private List<Integer> ExSetNo2 = new ArrayList<>();
    private List<Integer> ExRepNo2 = new ArrayList<>();
    private List<String> ExThumbnail2 = new ArrayList<>();

    private List<Integer> ExId = new ArrayList<>();
    private List<Integer> PeExId = new ArrayList<>();
    private List<String> ExName = new ArrayList<>();
    private List<Integer> ExType = new ArrayList<>();
    private List<Integer> ExSetNo = new ArrayList<>();
    private List<Integer> ExRepNo = new ArrayList<>();
    private List<String> ExThumbnail = new ArrayList<>();

    public static InitFragment newInstance(int exId, int peId, String exName, int exType, int exSetNo, int exRepNo, int exTimePerDay) {
        InitFragment fragment = new InitFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(EXERCISE_ID, exId);
        bundle.putInt(PRESCRIBED_EX_ID, peId);
        bundle.putString(EXERCISE_NAME, exName);
        bundle.putInt(EXERCISE_TYPE, exType);
        bundle.putInt(EXERCISE_REP_NO, exSetNo);
        bundle.putInt(EXERCISE_TIME_PER_DAY, exTimePerDay);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bindNotchService();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            exId = bundle.getInt(EXERCISE_ID);
            peId = bundle.getInt(PRESCRIBED_EX_ID);
            exName = bundle.getString(EXERCISE_NAME);
            exType = bundle.getInt(EXERCISE_TYPE);
            exSetNo = bundle.getInt(EXERCISE_REP_NO);
            exRepNo = bundle.getInt(EXERCISE_SET_NO);
            exTimePerDay = bundle.getInt(EXERCISE_TIME_PER_DAY);

            System.out.println("77777777777777777777");
            System.out.println(exId);
            System.out.println(peId);
            System.out.println(exName);
            System.out.println(exType);
            System.out.println(exSetNo);
            System.out.println(exRepNo);
            System.out.println(exTimePerDay);
        }
        mNotchDataBase = NotchDataBase.getInst();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_init, container, false);
        initView(root);

        stayId = GetSetSharedPreferences.getDefaults("stayId", getActivity().getApplicationContext());

        // Animation
        mDockImg.setBackgroundResource(R.drawable.sensor_anim);
        mDockAnimation = (AnimationDrawable) mDockImg.getBackground();
        mDockImg.setVisibility(View.INVISIBLE);

        startProgress();

        // Start Notch
        mHandler.postDelayed(mSetDefaultUser, 1000L);

        return root;
    }

    private void initView(View view) {
        initContainer = (View) view.findViewById(R.id.init_container);
        initStatusContainer = (View) view.findViewById(R.id.init_statusContainer);
        initButtonContainer = (View) view.findViewById(R.id.init_btnContainer);
        mCircleProgressView = (ProgressBar) view.findViewById(R.id.circle_progress);
        mHorizontalProgressView = (ProgressBar) view.findViewById(R.id.horizontal_progress);
        mCounterText = (TextView) view.findViewById(R.id.counter_text);
        backButton = (Button) view.findViewById(R.id.blank_canvas_btn_back);
        otherExerciseBtn = (Button) view.findViewById(R.id.other_exercise_btn);
        calibrationHelpButton = (ImageButton) view.findViewById(R.id.calibration_help);
        calibrateButton = (Button) view.findViewById(R.id.start_calibration);
        configSteady1Button = (Button) view.findViewById(R.id.config_steady1);
        configSteady2Button = (Button) view.findViewById(R.id.config_steady2);
        startSteady1Button = (Button) view.findViewById(R.id.start_steady1);
        startSteady2Button = (Button) view.findViewById(R.id.start_steady2);
        captureButton = (Button) view.findViewById(R.id.init_capture);
        mDockImg = (ImageView) view.findViewById(R.id.dock_image);

        backButton.setOnClickListener(this);
        otherExerciseBtn.setOnClickListener(this);
        calibrateButton.setOnClickListener(this);
        calibrationHelpButton.setOnClickListener(this);
        configSteady1Button.setOnClickListener(this);
        configSteady2Button.setOnClickListener(this);
        startSteady1Button.setOnClickListener(this);
        startSteady2Button.setOnClickListener(this);
        captureButton.setOnClickListener(this);
    }

    private void disableButton() {
        calibrateButton.setEnabled(false);
        calibrationHelpButton.setEnabled(false);
        configSteady1Button.setEnabled(false);
        configSteady2Button.setEnabled(false);
        startSteady1Button.setEnabled(false);
        startSteady2Button.setEnabled(false);
        captureButton.setEnabled(false);
    }

    private void enableButton() {
        calibrateButton.setEnabled(true);
        calibrationHelpButton.setEnabled(true);
        configSteady1Button.setEnabled(true);
        configSteady2Button.setEnabled(true);
        startSteady1Button.setEnabled(true);
        startSteady2Button.setEnabled(true);
        captureButton.setEnabled(true);
    }

    private void getThumbnail(){
        String accessToken = GetSetSharedPreferences.getDefaults("access_token", getActivity().getApplicationContext());
        GetAuth getAuthThumbnailTask = new GetAuth(InitFragment.this, THUMBNAIL_TASK_ID);
        getAuthThumbnailTask.execute("http://" + IPAddress.awsipaddress + "/PhysioWebPortal/api/hospitalstay/" + stayId + "/prescribed", accessToken);
    }

    private void uploadPerformedExercise(){
        String parameters = uploadPerformedExerciseParameters();
        String accessToken = GetSetSharedPreferences.getDefaults("access_token", getActivity().getApplicationContext());
        GetAuth getAuthThumbnailTask = new GetAuth(InitFragment.this, UPLOAD_PERFORMED_EXERCISE_ID);
        getAuthThumbnailTask.execute("http://" + IPAddress.ipaddress + "/test/Thumbnail.json", accessToken);
        // PostFormAuth uploadPerformedExerciseTask = new PostFormAuth(InitFragment.this, UPLOAD_PERFORMED_EXERCISE_ID);
        // uploadPerformedExerciseTask.execute("http://" + IPAddress.awsipaddress + "/PhysioWebPortal/token", parameters, accessToken);
    }

    private void refreshToken() {
        String parameters = getParameters();
        PostForm refreshTokenTask = new PostForm(InitFragment.this, REFRESH_TOKEN_TASK_ID);
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

    private String uploadPerformedExerciseParameters() {
        String parameters = "";
        JSONStringer jsonText = new JSONStringer();
        try {
            jsonText.object();
            jsonText.key("PEId");
            jsonText.value(peId);
            jsonText.key("StartTime");
            jsonText.value(NewStartTime);
            jsonText.key("EndTime");
            jsonText.value(NewEndTime);
            jsonText.key("ExSetNo");
            jsonText.value(NewExSetNo);
            jsonText.key("ExRepNo");
            jsonText.value(NewExRepNo);
            jsonText.key("ExType");
            jsonText.value(exType);
            jsonText.key("AvgAngle");
            jsonText.value(NewAvgAngle);
            jsonText.key("AvgHoldDuration");
            jsonText.value(NewAvgHoldDuration);
            jsonText.key("ExVisualFile");
            jsonText.value(NewExVisualFile);
            jsonText.key("LastUpdated");
            jsonText.value(NewLastUpdated);
            jsonText.key("LastUpdatedBy");
            jsonText.value(NewLastUpdatedBy);
            jsonText.key("Score");
            jsonText.value(NewScore);
            jsonText.endObject();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return parameters;
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
                            // 连接成功可以根据notchNetwork获取已经连接的设备
                            // 有一个notch打开，而且这个notch是已经匹配，就可以成功连接
                            finishProgress();
                            // 检查是否6个notch都打开了
                            if(notchNetwork.getDevices().keySet().size() != 6) {
                                checkNotchDialog();
                                cnDialog.show();
                                mHorizontalProgressView.setProgress(100);
                            }
                            else {
                                // 判断是否第一次使用
                                if(GetSetSharedPreferences.getDefaults(IS_FIRST_TIME, getActivity().getApplicationContext()) == null) {
                                    GetSetSharedPreferences.setDefaults(IS_FIRST_TIME, "false", getActivity().getApplicationContext());
                                    // 第一次使用没有时间间隔
                                    calibrateReminderDialog(calibrationState.FIRST_TIME, defaultDiffTimeStamp);
                                    crDialog.show();
                                    mHorizontalProgressView.setProgress(100);
                                }
                                else {
                                    // 不是第一次使用需要检查Calibration时间间隔
                                    checkCalibration(mNotchService.getLicense(), notchNetwork.getDevices());
                                }
                            }
                        }
                        @Override
                        public void onFailure(NotchError notchError) {
                            super.onFailure(notchError);
                            // 连接失败
                            finishProgress();
                            checkNotchDialog();
                            cnDialog.show();
                            mHorizontalProgressView.setProgress(100);
                        }
                    });
                }
                @Override
                public void onFailure(NotchError notchError) {
                    super.onFailure(notchError);
                    // 连接失败
                    finishProgress();
                    checkNotchDialog();
                    cnDialog.show();
                    mHorizontalProgressView.setProgress(100);
                }
            });
        }
    };

    @Override
    public void onClick(View view) {
        mHorizontalProgressView.setProgress(0);
        switch (view.getId()) {
            case R.id.calibration_help:
                calibrationHelpDialog();
                chDialog.show();
                break;
            case R.id.start_calibration:
                startProgress();
                mNotchService.uncheckedInit(mSelectedChannel, new EmptyCallback<NotchNetwork>() {
                    @Override
                    public void onSuccess(NotchNetwork notchNetwork) {
                        super.onSuccess(notchNetwork);
                        mHorizontalProgressView.setProgress(50);
                        mNotchService.configureCalibration(true, new EmptyCallback<Void>(){
                            @Override
                            public void onSuccess(Void aVoid) {
                                super.onSuccess(aVoid);
                                try {
                                    Thread.sleep(2000);
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                                finishProgress();
                                hideViews();
                                calibrateDialog();
                                cDialog.show();
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
            case R.id.config_steady1:
                startProgress();
                try {
                    configSteady();
                    mNotchService.init(mSelectedChannel, mWorkout, new EmptyCallback<NotchNetwork>() {
                        @Override
                        public void onSuccess(NotchNetwork notchNetwork) {
                            super.onSuccess(notchNetwork);
                            mHorizontalProgressView.setProgress(50);
                            mNotchService.configureSteady(MeasurementType.STEADY_SIMPLE, true, new EmptyCallback<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    super.onSuccess(aVoid);
                                    hideButtons();
                                    getActivity().runOnUiThread(() -> {
                                        startSteady1Button.setVisibility(View.VISIBLE);
                                    });
                                    finishProgress();
                                    putOnNotchDialog();
                                    pocDialog.show();
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
                } catch (Exception e) {
                    finishProgress();
                    Util.showNotification(R.string.error_skeleton);
                    mHorizontalProgressView.setProgress(100);
                }
                break;
            case R.id.config_steady2:
                startProgress();
                try {
                    configSteady();
                    mNotchService.init(mSelectedChannel, mWorkout, new EmptyCallback<NotchNetwork>() {
                        @Override
                        public void onSuccess(NotchNetwork notchNetwork) {
                            super.onSuccess(notchNetwork);
                            mHorizontalProgressView.setProgress(50);
                            mNotchService.configureSteady(MeasurementType.STEADY_SIMPLE, true, new EmptyCallback<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    super.onSuccess(aVoid);
                                    hideButtons();
                                    getActivity().runOnUiThread(() -> {
                                        startSteady2Button.setVisibility(View.VISIBLE);
                                    });
                                    finishProgress();
                                    putOnNotchDialog();
                                    pocDialog.show();
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
                } catch (Exception e) {
                    finishProgress();
                    Util.showNotification(R.string.error_skeleton);
                    mHorizontalProgressView.setProgress(100);
                }
                break;
            case R.id.start_steady1:
                hideViews();
                mState = State.STEADY;
                mCountDown.start();
                break;
            case R.id.start_steady2:
                hideViews();
                mState = State.STEADY;
                mCountDown.start();
                break;
            case R.id.init_capture:
                startProgress();
                try {
                    configSteady();
                    mNotchService.init(mSelectedChannel, mWorkout, new EmptyCallback<NotchNetwork>() {
                        @Override
                        public void onSuccess(NotchNetwork notchNetwork) {
                            super.onSuccess(notchNetwork);
                            mNotchService.configureCapture(false, new EmptyCallback<Void>(){
                                @Override
                                public void onSuccess(Void aVoid) {
                                    super.onSuccess(aVoid);
                                    finishProgress();
                                    hideViews();
                                    mState = State.CAPTURE;
                                    mCountDown.start();
                                }
                                @Override
                                public void onFailure(NotchError notchError) {
                                    super.onFailure(notchError);
                                    Util.showNotchError(notchError);
                                    finishProgress();
                                }
                            });
                        }
                        @Override
                        public void onFailure(NotchError notchError) {
                            super.onFailure(notchError);
                            Util.showNotchError(notchError);
                            finishProgress();
                        }
                    });
                } catch (Exception e) {
                    Util.showNotification(R.string.error_skeleton);
                }
                break;
            case R.id.other_exercise_btn:
                otherExerciseDialog();
                oeDialog.show();
                break;
            case R.id.blank_canvas_btn_back:
                getActivity().finish();
                break;
        }
    }

    private void configSteady() throws Exception {
        if(exType == 1) {
            skeleton = Skeleton.from(new InputStreamReader(mApplicationContext.getResources().openRawResource(R.raw.skeleton_male), "UTF-8"));
        }
        else {
            skeleton = Skeleton.from(new InputStreamReader(mApplicationContext.getResources().openRawResource(R.raw.skeleton_male), "UTF-8"));
        }
        Workout workout = Workout.from("Demo_config", skeleton, IOUtil.readAll(new InputStreamReader(mApplicationContext.getResources().openRawResource(R.raw.config_6_lower_body_real_time))));
        workout = workout.withRealTime(true);
        workout = workout.withMeasurementType(MeasurementType.STEADY_SKIP);
        mWorkout = workout;
    }

    private CountDownTimer mCountDown = new CountDownTimer(3250, 500) {
        public void onTick(long millisUntilFinished) {
            //update the UI with the new count
            setCounterText(mCounterText, millisUntilFinished);
        }
        public void onFinish() {
            mHorizontalProgressView.setProgress(0);
            //start the activity
            switch (mState) {
                case CALIBRATION:
                    mNotchService.calibration(new EmptyCallback<Measurement>());
                    mHandler.post(() -> {
                        mDockImg.setVisibility(View.VISIBLE);
                        mDockAnimation.setVisible(false, true);
                        mDockAnimation.start();
                    });
                    mHandler.postDelayed(() -> {
                        mDockImg.setVisibility(View.GONE);
                        mDockAnimation.stop();
                        showViews();
                        startProgress();
                        try {
                            Thread.sleep(3000);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        mNotchService.getCalibrationData(new EmptyCallback<Boolean>(){
                            @Override
                            public void onSuccess(Boolean bool) {
                                super.onSuccess(bool);
                                mHorizontalProgressView.setProgress(30);
                                try {
                                    configSteady();
                                    mNotchService.init(mSelectedChannel, mWorkout, new EmptyCallback<NotchNetwork>() {
                                        @Override
                                        public void onSuccess(NotchNetwork notchNetwork) {
                                            super.onSuccess(notchNetwork);
                                            mHorizontalProgressView.setProgress(60);
                                            mNotchService.configureSteady(MeasurementType.STEADY_SIMPLE, true, new EmptyCallback<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    super.onSuccess(aVoid);
                                                    hideButtons();
                                                    getActivity().runOnUiThread(() -> {
                                                        startSteady1Button.setVisibility(View.VISIBLE);
                                                    });
                                                    finishProgress();
                                                    putOnNotchDialog();
                                                    pocDialog.show();
                                                    mHorizontalProgressView.setProgress(100);
                                                }
                                                @Override
                                                public void onFailure(NotchError notchError) {
                                                    super.onFailure(notchError);
                                                    hideButtons();
                                                    Util.showNotchError(notchError);
                                                    finishProgress();
                                                    mHorizontalProgressView.setProgress(100);
                                                }
                                            });
                                        }
                                        @Override
                                        public void onFailure(NotchError notchError) {
                                            super.onFailure(notchError);
                                            hideButtons();
                                            Util.showNotchError(notchError);
                                            finishProgress();
                                            mHorizontalProgressView.setProgress(100);
                                        }
                                    });
                                } catch (Exception e) {
                                    hideButtons();
                                    Util.showNotification(R.string.error_skeleton);
                                    finishProgress();
                                    mHorizontalProgressView.setProgress(100);
                                }
                            }
                            @Override
                            public void onFailure(NotchError notchError) {
                                super.onFailure(notchError);
                                hideButtons();
                                Util.showNotchError(notchError);
                                finishProgress();
                                mHorizontalProgressView.setProgress(100);
                            }
                        });
                    }, DEMO_TIME);
                    break;
                case STEADY:
                    showViews();
                    startProgress();
                    mNotchService.steady(new EmptyCallback<Measurement>(){
                        @Override
                        public void onSuccess(Measurement measurement) {
                            super.onSuccess(measurement);
                            mHorizontalProgressView.setProgress(50);
                            mNotchService.getSteadyData(new EmptyCallback<Void>(){
                                @Override
                                public void onSuccess(Void aVoid) {
                                    super.onSuccess(aVoid);
                                    hideButtons();
                                    getActivity().runOnUiThread(() -> {
                                        captureButton.setVisibility(View.VISIBLE);
                                        configSteady2Button.setVisibility(View.VISIBLE);
                                    });
                                    Util.showNotification("Steady successfully");
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
                case CAPTURE:
                    showViews();
                    capture();
                    break;
            }
            setCounterText(mCounterText, "");
        }
    };

    private void capture() {
        mVisualiserActivity = null;
        startProgress();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date startTimeDate = new Date(System.currentTimeMillis());
        NewStartTime = dateFormat.format(startTimeDate);

        recordFolder = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+ "/PhysioAssist/Record/" + peId);
        recordFile = new File(recordFolder, NewStartTime + ".zip");
        mCancellable = mNotchService.capture(recordFile, new NotchCallback<Void>() {
            @Override
            public void onProgress(NotchProgress progress) {
                if (progress.getState() == NotchProgress.State.REALTIME_UPDATE) {
                    mRealTimeData = (VisualiserData) progress.getObject();
                    updateRealTime();
                }
            }

            @Override
            public void onSuccess(Void nothing) {

            }

            @Override
            public void onFailure(NotchError notchError) {
                Util.showNotification(Util.getNotchErrorStr(notchError));
            }

            @Override
            public void onCancelled() {

            }
        });
    }

    private void updateRealTime() {
        if (mVisualiserActivity == null) {
            mVisualiserActivity = new VisualiserActivity();
            Intent i = VisualiserActivity.createIntent(getActivity(), mRealTimeData, true, exId, peId, exName, exType, exSetNo, exRepNo, exTimePerDay);
            startActivityForResult(i, IS_CANCELLABLE);
        }
        else {
            EventBus.getDefault().post(mRealTimeData);
        }
    }

    private void hideButtons() {
        getActivity().runOnUiThread(() -> {
            configSteady1Button.setVisibility(View.GONE);
            configSteady2Button.setVisibility(View.GONE);
            startSteady1Button.setVisibility(View.GONE);
            startSteady2Button.setVisibility(View.GONE);
            captureButton.setVisibility(View.GONE);
        });
    }

    private void hideViews() {
        getActivity().runOnUiThread(() -> {
            initContainer.setBackgroundColor(Color.parseColor("#CC000000"));
            initStatusContainer.setVisibility(View.GONE);
            initButtonContainer.setVisibility(View.GONE);
            backButton.setVisibility(View.GONE);
            otherExerciseBtn.setVisibility(View.GONE);
        });
    }

    private void showViews() {
        getActivity().runOnUiThread(() -> {
            initContainer.setBackgroundColor(0x00000000);
            initStatusContainer.setVisibility(View.VISIBLE);
            initButtonContainer.setVisibility(View.VISIBLE);
            backButton.setVisibility(View.VISIBLE);
            otherExerciseBtn.setVisibility(View.VISIBLE);
        });
    }

    private void startProgress() {
        getActivity().runOnUiThread(() -> {
            disableButton();
            showProgress(true);
        });
    }

    private void finishProgress() {
        getActivity().runOnUiThread(() -> {
            showProgress(false);
            enableButton();
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
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
            mCircleProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    // 检查Calibration
    private void checkCalibration(final String user, Map<Bone, ActionDevice> connectedDevices){
        if (mNotchDataBase == null) {
            mNotchDataBase = NotchDataBase.getInst();
        }
        // 如果notch数据库有设备（之前匹配过）
        if(mNotchDataBase.findAllDevices(user).size() > 0) {

            boolean needToCalibrate = false;
            boolean isFirstTime = false;
            long diffTimeStamp = -1L;
            // 从notch数据库中获取全部设备
            for (Device device : mNotchDataBase.findAllDevices(user)) {
                // 如果有已连接设备
                if(connectedDevices != null) {
                    Iterator<Bone> iterator = connectedDevices.keySet().iterator();
                    while(iterator.hasNext()){
                        Bone bone = iterator.next();
                        ActionDevice actionDevice = connectedDevices.get(bone);
                        if(actionDevice.getDeviceMac().equals(device.getNotchDevice().getDeviceMac())){
                            // 无法获取上次Calibration时间（之前有连接但是没有进行Calibration，因此属于第一次进行Calibration）
                            if(device.getLastCalibration() == null || device.getLastCalibration() == 0) {
                                needToCalibrate = true;
                                isFirstTime = true;
                                break;
                            }
                            // 之前进行过Calibration
                            else {
                                diffTimeStamp = (System.currentTimeMillis() - device.getLastCalibration()) / 1000L;
                                // 如果超过3天都没进行Calibration
                                if((diffTimeStamp / 86400) > 3) {
                                    needToCalibrate = true;
                                    break;
                                }
                            }
                        }
                    }
                    if(needToCalibrate) {
                        break;
                    }
                }
            }

            // 如果需要Calibration
            if(needToCalibrate) {
                if(isFirstTime) {
                    calibrateReminderDialog(calibrationState.FIRST_TIME, diffTimeStamp);
                }
                else {
                    calibrateReminderDialog(calibrationState.OVER_TIME, diffTimeStamp);
                }
                crDialog.show();
                mHorizontalProgressView.setProgress(100);
            }
            // 继续Steady
            else {
                startProgress();
                try {
                    configSteady();
                    mNotchService.init(mSelectedChannel, mWorkout, new EmptyCallback<NotchNetwork>() {
                        @Override
                        public void onSuccess(NotchNetwork notchNetwork) {
                            super.onSuccess(notchNetwork);
                            mHorizontalProgressView.setProgress(60);
                            mNotchService.configureSteady(MeasurementType.STEADY_SIMPLE, true, new EmptyCallback<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    super.onSuccess(aVoid);
                                    hideButtons();
                                    getActivity().runOnUiThread(() -> {
                                        startSteady1Button.setVisibility(View.VISIBLE);
                                    });
                                    finishProgress();
                                    putOnNotchDialog();
                                    pocDialog.show();
                                    mHorizontalProgressView.setProgress(100);
                                }
                                @Override
                                public void onFailure(NotchError notchError) {
                                    super.onFailure(notchError);
                                    hideButtons();
                                    getActivity().runOnUiThread(() -> {
                                        configSteady1Button.setVisibility(View.VISIBLE);
                                    });
                                    Util.showNotchError(notchError);
                                    finishProgress();
                                    mHorizontalProgressView.setProgress(100);
                                }
                            });
                        }
                        @Override
                        public void onFailure(NotchError notchError) {
                            super.onFailure(notchError);
                            hideButtons();
                            getActivity().runOnUiThread(() -> {
                                configSteady1Button.setVisibility(View.VISIBLE);
                            });
                            Util.showNotchError(notchError);
                            finishProgress();
                            mHorizontalProgressView.setProgress(100);
                        }
                    });
                }
                catch (Exception e) {
                    hideButtons();
                    getActivity().runOnUiThread(() -> {
                        configSteady1Button.setVisibility(View.VISIBLE);
                    });
                    Util.showNotification(R.string.error_skeleton);
                    finishProgress();
                    mHorizontalProgressView.setProgress(100);
                }
            }
        }
        // 如果之前数据库没记录（正常来到这里是不可能存在没有记录的可能），需要先进行连接或者匹配
        else {
            checkNotchDialog();
            cnDialog.show();
            mHorizontalProgressView.setProgress(100);
        }
    }

    private void calibrateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Notch:").setMessage("Please click \"OK\" to start calibrate");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mState = State.CALIBRATION;
                mCountDown.start();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showViews();
            }
        });

        cDialog = builder.create();
    }

    private void calibrationHelpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Notch Calibration:")
                .setMessage("Calibrate your notches at the beginning of each recording session " +
                        "for optimal performance. Re-calibrate if you move to a location with significantly " +
                        "different environment. For example: from an outdoor track to an indoor gym.");

        builder.setPositiveButton("OK", null);
        builder.setNegativeButton("Cancel", null);

        chDialog = builder.create();
    }

    private void calibrateReminderDialog(calibrationState cs, long diffTimeStamp) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Notch:");

        // 如果是第一次使用
        if(cs == calibrationState.FIRST_TIME) {
            builder.setMessage("Please calibrate your notches");

            builder.setPositiveButton("OK", null);
            builder.setNegativeButton("Cancel", null);
        }
        // 如果不是第一次使用
        else if(cs == calibrationState.OVER_TIME) {
            builder.setMessage("Your last successful calibration was " + (diffTimeStamp / 86400) + " days ago.\n" +
                    "Would you like to calibrate now?");

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mHorizontalProgressView.setProgress(0);
                    startProgress();
                    mNotchService.uncheckedInit(mSelectedChannel, new EmptyCallback<NotchNetwork>() {
                        @Override
                        public void onSuccess(NotchNetwork notchNetwork) {
                            super.onSuccess(notchNetwork);
                            mHorizontalProgressView.setProgress(50);
                            mNotchService.configureCalibration(true, new EmptyCallback<Void>(){
                                @Override
                                public void onSuccess(Void aVoid) {
                                    super.onSuccess(aVoid);
                                    try {
                                        Thread.sleep(2000);
                                    }
                                    catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    finishProgress();
                                    hideViews();
                                    calibrateDialog();
                                    cDialog.show();
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
                }
            });

            builder.setNegativeButton("Not Now", null);
        }
        crDialog = builder.create();
    }

    private void putOnNotchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Notch:").setMessage("Please put on your notches according to the diagram");

        builder.setPositiveButton("OK", null);
        builder.setNegativeButton("Cancel", null);

        pocDialog = builder.create();
    }

    private void checkNotchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Notch:").setMessage("Please check if the notches are paired and their connection");

        builder.setPositiveButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
            }
        });

        builder.setNegativeButton("Go to settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NotchActivity notchActivity = (NotchActivity) getActivity();
                notchActivity.gotoSettingsFragment();
            }
        });

        DialogInterface.OnKeyListener onKeyListener = (DialogInterface dialog, int keyCode, KeyEvent KEvent) -> {
            if(keyCode == KeyEvent.KEYCODE_BACK) {
                getActivity().finish();
            }
            return false;
        };

        cnDialog = builder.create();
        cnDialog.setOnKeyListener(onKeyListener);
    }

    private void otherExerciseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        View mDialogView = getLayoutInflater().inflate(R.layout.dialog_patient_exercise, null);
        adapter = new CustomDialogPatientExerciseAdapter(getContext(), header, ExName, ExThumbnail);
        mListView = mDialogView.findViewById(R.id.dialog_exercise_list);
        mListView.setAdapter(adapter);
        adapter.setDialogThumbnailClickListener(this);

        builder.setTitle("Select Exercises:");

        builder.setView(mDialogView);

        builder.setPositiveButton("Cancel", null);

        if(GetSetSharedPreferences.getDefaults(IS_Show_Other_Exercise_Dialog, getActivity().getApplicationContext()) == null ||
                GetSetSharedPreferences.getDefaults(IS_Show_Other_Exercise_Dialog, getActivity().getApplicationContext()).equals("true")) {
            builder.setNegativeButton("Don't show anymore", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    GetSetSharedPreferences.setDefaults(IS_Show_Other_Exercise_Dialog, "false", getActivity().getApplicationContext());
                }
            });
        }
        else {
            builder.setNegativeButton("Always show", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    GetSetSharedPreferences.setDefaults(IS_Show_Other_Exercise_Dialog, "true", getActivity().getApplicationContext());
                }
            });
        }


        oeDialog = builder.create();
    }

    private void setCounterText(final TextView text, final String str){
        setCounterText(text, str, 50);
    }

    private void setCounterText(final TextView text, final long millisec){
        if(isAdded()) {
            getActivity().runOnUiThread(() -> {
                text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 120);
                text.setText("" + Math.round((float) millisec / 1000.0f));
            });
        }
    }

    private void setCounterText(final TextView text, final String str, final float size){
        if(isAdded()) {
            getActivity().runOnUiThread(() -> {
                text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
                text.setText(str);
            });
        }
    }

    public void UnZip(File zipFile, File targetDirectory) throws IOException {
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
        try {
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs()) {
                    throw new FileNotFoundException("Failed to ensure directory: " + dir.getAbsolutePath());
                }
                if (ze.isDirectory()) {
                    continue;
                }
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1) {
                        fout.write(buffer, 0, count);
                    }
                } finally {
                    fout.close();
                }
            }
        } finally {
            zis.close();
        }
    }

    public void Zip(File[] files, String zipFileName) {
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(zipFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            byte data[] = new byte[1024];

            for (int i = 0; i < files.length; i++) {
                FileInputStream fi = new FileInputStream(files[i].getAbsolutePath());
                origin = new BufferedInputStream(fi, 1024);

                ZipEntry entry = new ZipEntry(files[i].getAbsolutePath().substring(files[i].getAbsolutePath().lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;

                while ((count = origin.read(data, 0, 1024)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void moveFile(File inputFile, String outputPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            //create output directory if it doesn't exist
            File dir = new File (outputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            in = new FileInputStream(inputFile.getAbsolutePath());
            out = new FileOutputStream(outputPath + "/" + inputFile.getName());

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;

            // delete the original file
            inputFile.delete();
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (IS_CANCELLABLE) : {
                if (resultCode == Activity.RESULT_OK) {
                    boolean isCancellable = data.getBooleanExtra("isCancellable", true);
                    int exerciseTimes = data.getIntExtra("exerciseTimes", 0);
                    if(isCancellable && mCancellable != null) {
                        mCancellable.cancel();
                        mCancellable = null;
                        getActivity().runOnUiThread(() -> {
                            CustomToast.show(getActivity(), "Processing File...");
                        });
                        new CountDownTimer(5000, 1000) {
                            public void onTick(long millisUntilFinished) {
                                if(millisUntilFinished / 1000 == 1) {

                                }
                                System.out.println(millisUntilFinished);
                            }
                            public void onFinish() {
                                if (data.hasExtra("isCancelExercise")) {
                                    recordFile.delete();
                                    CustomToast.show(getActivity(), "File Deleted");
                                }
                                else {
                                    try {
                                        String tempFolderPath = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+ "/PhysioAssist/Record/" + peId + "/Temp";

                                        // Create a temp folder
                                        File tempFolder = new File(tempFolderPath);
                                        tempFolder.mkdirs();

                                        // UpZip file to the temp folder
                                        UnZip(new File(recordFile.getAbsolutePath()), tempFolder);

                                        // Zip file
                                        File[] files = tempFolder.listFiles()[0].listFiles();
                                        Zip(files, tempFolderPath + "/" + recordFile.getName());

                                        // Delete the original zip file
                                        recordFile.delete();

                                        // Move the file to original place
                                        moveFile(new File(tempFolderPath + "/" + recordFile.getName()), recordFolder.getAbsolutePath());

                                        // Delete the temp folder
                                        File[] tempChildren = tempFolder.listFiles();
                                        for (int i = 0; i < tempChildren.length; i++)  {
                                            File[] tempSubChildren = tempChildren[i].listFiles();
                                            for (int j = 0; j < tempSubChildren.length; j++)  {
                                                tempSubChildren[j].delete();
                                            }
                                            tempChildren[i].delete();
                                        }
                                        tempFolder.delete();

                                        // Keep 10 files
                                        File[] recordFolderChildren = recordFolder.listFiles();
                                        if(recordFolderChildren.length > 10) {
                                            for (int i = 10; i < recordFolderChildren.length; i++)  {
                                                recordFolderChildren[i].delete();
                                            }
                                        }
                                    }
                                    catch (Exception e) {
                                        e.getStackTrace();
                                    }

                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    Date endTimeDate = new Date(System.currentTimeMillis());
                                    NewEndTime = dateFormat.format(endTimeDate);
                                    NewLastUpdated = dateFormat.format(endTimeDate);

                                    CustomToast.show(getActivity(), "File Saved");
                                }

                                isUploaded = true;

                                if(uploadExerciseStatus.equals("succeeded")) {
                                    if(GetSetSharedPreferences.getDefaults(IS_Show_Other_Exercise_Dialog, getActivity().getApplicationContext()) == null ||
                                            GetSetSharedPreferences.getDefaults(IS_Show_Other_Exercise_Dialog, getActivity().getApplicationContext()).equals("true")) {
                                        getThumbnail();
                                    }
                                }
                            }
                        }.start();
                    }
                }
                break;
            }
        }
    }

    @Override
    public void onTaskCompleted(String response, int requestId) {
        if(response.contains("<html")) {
            if(requestId == THUMBNAIL_TASK_ID) {
                getThumbnailRequest = false;
            }
            else if(requestId == UPLOAD_PERFORMED_EXERCISE_ID) {
                uploadExerciseRequest = false;
            }
            refreshToken();
        }
        else {
            if(requestId == THUMBNAIL_TASK_ID) {
                boolean isJSONArray = false;
                boolean isJSONObject = false;
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    isJSONObject = true;

                    // 清空数组
                    ExId1.clear();
                    ExId2.clear();
                    ExId.clear();
                    PeExId1.clear();
                    PeExId2.clear();
                    PeExId.clear();
                    ExName1.clear();
                    ExName2.clear();
                    ExName.clear();
                    ExType1.clear();
                    ExType2.clear();
                    ExSetNo1.clear();
                    ExSetNo2.clear();
                    ExRepNo1.clear();
                    ExRepNo2.clear();
                    ExThumbnail1.clear();
                    ExThumbnail2.clear();
                    ExThumbnail.clear();

                    // 刷新数据库
                    if(jsonObject.getInt("ExType") == 1) {
                        ExId1.add(jsonObject.getInt("ExId"));
                        PeExId1.add(jsonObject.getInt("PEId"));
                        ExName1.add(jsonObject.getString("ExName"));
                        ExType1.add(jsonObject.getInt("ExType"));
                        ExSetNo1.add(jsonObject.getInt("ExSetNo"));
                        ExRepNo1.add(jsonObject.getInt("ExRepNo"));
                        String imageURL = "http://" + IPAddress.awsipaddress + "/PhysioWebPortal/Images/" + jsonObject.getString("VThumbnail") +".jpg";
                        ExThumbnail1.add(imageURL);
                    }
                    else if(jsonObject.getInt("ExType") == 2) {
                        ExId2.add(jsonObject.getInt("ExId"));
                        PeExId2.add(jsonObject.getInt("PEId"));
                        ExName2.add(jsonObject.getString("ExName"));
                        ExType2.add(jsonObject.getInt("ExType"));
                        ExSetNo2.add(jsonObject.getInt("ExSetNo"));
                        ExRepNo2.add(jsonObject.getInt("ExRepNo"));
                        String imageURL = "http://" + IPAddress.awsipaddress + "/PhysioWebPortal/Images/" + jsonObject.getString("VThumbnail") +".jpg";
                        ExThumbnail1.add(imageURL);
                    }

                    header.add("Lying Down");
                    header.add("Sitting Up");

                    ExName.add("Lying Down");
                    ExName.addAll(ExName1);
                    ExName.add("Sitting Up");
                    ExName.addAll(ExName2);

                    // 保证长度一致防止数组越界
                    ExThumbnail.add("");
                    ExThumbnail.addAll(ExThumbnail1);
                    ExThumbnail.add("");
                    ExThumbnail.addAll(ExThumbnail2);

                    otherExerciseDialog();
                    oeDialog.show();
                    getThumbnailStatus = "succeeded";
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    isJSONArray = true;

                    // 清空数组
                    ExId1.clear();
                    ExId2.clear();
                    ExId.clear();
                    PeExId1.clear();
                    PeExId2.clear();
                    PeExId.clear();
                    ExName1.clear();
                    ExName2.clear();
                    ExName.clear();
                    ExType1.clear();
                    ExType2.clear();
                    ExSetNo1.clear();
                    ExSetNo2.clear();
                    ExRepNo1.clear();
                    ExRepNo2.clear();
                    ExThumbnail1.clear();
                    ExThumbnail2.clear();
                    ExThumbnail.clear();

                    // 刷新数据库
                    for(int i=0; i<jsonArray.length(); i++) {
                        JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                        // 刷新数据库
                        if(jsonObject.getInt("ExId") == 1 || jsonObject.getInt("ExId") == 3 || jsonObject.getInt("ExId") == 8 || jsonObject.getInt("ExId") == 9) {
                            ExId1.add(jsonObject.getInt("ExId"));
                            PeExId1.add(jsonObject.getInt("PEId"));
                            ExName1.add(jsonObject.getString("ExName"));
                            ExType1.add(1);
                            ExSetNo1.add(jsonObject.getInt("ExSetNo"));
                            ExRepNo1.add(jsonObject.getInt("ExRepNo"));
                            String imageURL = "http://" + IPAddress.awsipaddress + "/PhysioWebPortal/Images/" + jsonObject.getString("VThumbnail") +".jpg";
                            ExThumbnail1.add(imageURL);
                        }
                        else {
                            ExId2.add(jsonObject.getInt("ExId"));
                            PeExId2.add(jsonObject.getInt("PEId"));
                            ExName2.add(jsonObject.getString("ExName"));
                            ExType2.add(2);
                            ExSetNo2.add(jsonObject.getInt("ExSetNo"));
                            ExRepNo2.add(jsonObject.getInt("ExRepNo"));
                            String imageURL = "http://" + IPAddress.awsipaddress + "/PhysioWebPortal/Images/" + jsonObject.getString("VThumbnail") +".jpg";
                            ExThumbnail2.add(imageURL);
                        }
                    }

                    header.add("Lying Down");
                    header.add("Sitting Up");

                    ExName.add("Lying Down");
                    ExName.addAll(ExName1);
                    ExName.add("Sitting Up");
                    ExName.addAll(ExName2);

                    // 保证长度一致防止数组越界
                    ExThumbnail.add("");
                    ExThumbnail.addAll(ExThumbnail);
                    ExThumbnail.add("");
                    ExThumbnail.addAll(ExThumbnail2);

                    otherExerciseDialog();
                    oeDialog.show();
                    getThumbnailStatus = "succeeded";
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(isJSONArray || isJSONObject) {
                    if(getThumbnailStatus.equals("succeeded") && uploadExerciseStatus.equals("succeeded")) {
                        // 加载完才允许点击
                        finishProgress();
                    }
                }
                else if(!isJSONArray && !isJSONObject) {
                    getThumbnailStatus = "succeeded";
                    if(getThumbnailStatus.equals("succeeded") && uploadExerciseStatus.equals("succeeded")) {
                        // 加载完才允许点击
                        finishProgress();
                    }
                }
            }
            else if(requestId == UPLOAD_PERFORMED_EXERCISE_ID) {
                boolean isJSONArray = false;
                boolean isJSONObject = false;
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    isJSONObject = true;
                    uploadExerciseStatus = "succeeded";
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    isJSONArray = true;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                    }
                    uploadExerciseStatus = "succeeded";
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(isJSONArray || isJSONObject) {
                    if(getThumbnailStatus.equals("succeeded") && uploadExerciseStatus.equals("succeeded")) {
                        if(isUploaded) {
                            if(GetSetSharedPreferences.getDefaults(IS_Show_Other_Exercise_Dialog, getActivity().getApplicationContext()) == null ||
                                    GetSetSharedPreferences.getDefaults(IS_Show_Other_Exercise_Dialog, getActivity().getApplicationContext()).equals("true")) {
                                getThumbnail();
                            }
                        }
                    }
                }
                else if(!isJSONArray && !isJSONObject) {
                    uploadExerciseStatus = "succeeded";
                    if(getThumbnailStatus.equals("succeeded") && uploadExerciseStatus.equals("succeeded")) {
                        if(isUploaded) {
                            if(GetSetSharedPreferences.getDefaults(IS_Show_Other_Exercise_Dialog, getActivity().getApplicationContext()) == null ||
                                    GetSetSharedPreferences.getDefaults(IS_Show_Other_Exercise_Dialog, getActivity().getApplicationContext()).equals("true")) {
                                getThumbnail();
                            }
                        }
                    }
                }
            }
            else if(requestId == REFRESH_TOKEN_TASK_ID) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    access_token = jsonObject.getString("access_token");
                    GetSetSharedPreferences.setDefaults("access_token", access_token, getActivity().getApplicationContext());
                    if(!getThumbnailRequest) {
                        getThumbnail();
                        getThumbnailRequest = true;
                    }
                    else if(!uploadExerciseRequest) {
                        uploadPerformedExercise();
                        uploadExerciseRequest = true;
                    }
                    getThumbnail();
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

        if(isUploaded) {
            finishProgress();
        }
    }

    @Override
    public void onThumbnailClick(int parentId, int position) {

    }

    @Override
    public void onStartExerciseClick(int parentId, int position) {
        startProgress();

        exId = ExId.get(position);
        exName = ExName.get(position);
        exType = ExType.get(position);
        exSetNo = ExSetNo.get(position);
        exRepNo = ExRepNo.get(position);

        File recordFolder = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+ "/PhysioAssist/Record/" + peId);
        if (!recordFolder.exists()) {
            recordFolder.mkdirs();
        }
        oeDialog.dismiss();
        finishProgress();
    }

    @Override
    public void onAchievementImgClick(int position) {

    }

    @Override
    public void onTrophyImgClick(int position) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
