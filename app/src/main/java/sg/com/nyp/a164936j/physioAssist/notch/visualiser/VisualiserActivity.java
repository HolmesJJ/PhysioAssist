package sg.com.nyp.a164936j.physioAssist.notch.visualiser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.opengl.GLES20;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.wearnotch.framework.Bone;
import com.wearnotch.framework.Skeleton;
import com.wearnotch.framework.visualiser.VisualiserData;
import com.wearnotch.notchmaths.fvec3;
import com.wearnotch.visualiser.NotchSkeletonRenderer;
import com.wearnotch.visualiser.RigidBody;
import com.wearnotch.visualiser.shader.ColorShader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;
import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.configuration.GetSetSharedPreferences;
import sg.com.nyp.a164936j.physioAssist.notch.util.Util;

public class VisualiserActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener, OnChartValueSelectedListener {
    private static final String TAG = "Visualiser";
    private static final float SECONDARY_TRANSPARENCY = 0.8f;
    private static final int REQUEST_OPEN = 1;
    private static final String NOTCH_DIR = "notch_tutorial";
    private static float BASELINE = 45f;

    private static final String PARAM_INPUT_ZIP = "INPUT_ZIP";
    private static final String PARAM_INPUT_DATA = "INPUT_DATA";
    private static final String PARAM_REALTIME = "REALTIME";

    private static final String OBJ_ASSET = "droid_obj.dat";
    private static final String MTL_ASSET = "droid_mtl.dat";

    private static final String EXERCISE_ID = "exerciseId";
    private static final String PRESCRIBED_EX_ID = "prescribedExId";
    private static final String EXERCISE_NAME = "exerciseName";
    private static final String EXERCISE_TYPE = "exerciseType";
    private static final String EXERCISE_SET_NO = "exerciseSetNo";
    private static final String EXERCISE_REP_NO = "exerciseRepNo";
    private static final String EXERCISE_TIME_PER_DAY = "exerciseTimePerDay";

    private Parcelable[] mZipUri;
    private VisualiserData mData;
    private Skeleton mSkeleton;

    private volatile boolean mPaused;
    private volatile int mFrameIndex;
    private volatile float mSpeed = 1f;
    private volatile boolean mSeeking;
    private volatile boolean mShowPath, mPinToCentre, mHighlightBones;
    private volatile List<Bone> mBonesToShow = new ArrayList<>();

    private volatile boolean[] mCheckedBones;
    private int mFrameCount;
    private boolean mRealTime, isGroundDrawn, mShowAngles;
    private float mFrequency;
    private String[] mBoneNames;
    private DecimalFormat decimalFormat;

    private String language;
    private int exId;
    private int peId;
    private String exName;
    private int exType;
    private int exSetNo;
    private int exRepNo;
    private int exTimePerDay;

    private ExtendedRenderer mRenderer;
    private AlertDialog mBoneSelectorDialog, cDialog, fDialog, sDialog;

    private Toolbar mToolbar;
    private JzvdStd mJzvdStd;
    private ProgressBar mProgress;
    private TouchGLView mSurfaceView;
    private SeekBar mSeekBar;
    private TextView mSpeedText;
    private TextView mAnglesText;
    private TextView mRepeatText;
    private TextView mElapsedTimeText;
    private ImageButton cancelExerciseButton;
    private ImageButton finishExerciseButton;
    private ImageButton playPauseButton;
    private ImageButton startButton;
    private ImageButton endButton;
    private ImageButton speedButton;
    private ImageButton frontViewButton;
    private ImageButton topViewButton;
    private ImageButton sideViewButton;
    private ImageButton showViewButton;

    private LineChart chart;
    private int countAboveBaseline = 0;
    private int countUnderBaseline = 0;
    float oldValue = 0;
    float newValue = 0;
    private Entry oldEntry;
    private Entry newEntry;
    private Entry lowestEntry;
    private Entry highestEntry;

    private int exerciseTimes = 0;
    private int countFrame = 0;
    private boolean exerciseUp = false;
    private boolean exerciseDown = false;
    private boolean isSucceedDialogShow = false;

    public static Intent createIntent(Context context, Uri zipUri, String exName) {
        Intent i = new Intent(context, VisualiserActivity.class);
        i.putExtra(PARAM_INPUT_ZIP, new Parcelable[] { zipUri });
        i.putExtra(EXERCISE_NAME, exName);
        return i;
    }

    public static Intent createIntent(Context context, VisualiserData data, boolean realtime, int exId, int peId, String exName, int exType, int exSetNo, int exRepNo, int exTimePerDay) {
        Intent i = new Intent(context, VisualiserActivity.class);
        i.putExtra(PARAM_INPUT_DATA, data);
        i.putExtra(PARAM_REALTIME, realtime);

        i.putExtra(EXERCISE_ID, exId);
        i.putExtra(PRESCRIBED_EX_ID, peId);
        i.putExtra(EXERCISE_NAME, exName);
        i.putExtra(EXERCISE_TYPE, exType);
        i.putExtra(EXERCISE_SET_NO, exSetNo);
        i.putExtra(EXERCISE_REP_NO, exRepNo);
        i.putExtra(EXERCISE_TIME_PER_DAY, exTimePerDay);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualiser);
        initView();
        setSupportActionBar(mToolbar);

        language = GetSetSharedPreferences.getDefaults("language", getApplicationContext());

        exId = getIntent().getIntExtra(EXERCISE_ID, 1);
        peId = getIntent().getIntExtra(PRESCRIBED_EX_ID, 1);
        exName = getIntent().getStringExtra(EXERCISE_NAME);
        exType = getIntent().getIntExtra(EXERCISE_TYPE, 1);
        exSetNo = getIntent().getIntExtra(EXERCISE_SET_NO, 1);
        exRepNo = getIntent().getIntExtra(EXERCISE_REP_NO, 1);
        exTimePerDay = getIntent().getIntExtra(EXERCISE_TIME_PER_DAY, 1);

        System.out.println("888888888888888");
        System.out.println(peId);
        System.out.println(exId);
        System.out.println(exName);
        System.out.println(exType);
        System.out.println(exSetNo);
        System.out.println(exRepNo);
        System.out.println(exTimePerDay);

        mZipUri = getIntent().getParcelableArrayExtra(PARAM_INPUT_ZIP);
        mData = (VisualiserData) getIntent().getSerializableExtra(PARAM_INPUT_DATA);
        mRealTime = getIntent().getBooleanExtra(PARAM_REALTIME,false);

        if(!mRealTime) {
            cancelExerciseButton.setVisibility(View.GONE);
        }

        mSeekBar.setOnSeekBarChangeListener(this);
        initRenderer();
        mShowAngles = true;

        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
        decimalFormat = new DecimalFormat("0.000", otherSymbols);

        String videoUri = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+ "/PhysioAssist/Video/Counting/" + exId + "_counting_" + language + ".mp4";
        mJzvdStd.setUp(videoUri, exName, Jzvd.SCREEN_WINDOW_NORMAL);
        Jzvd.FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        Jzvd.NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        Jzvd.SAVE_PROGRESS = false;

        Bitmap videoThumbnail = getLocalVideoThumbnail(videoUri);
        mJzvdStd.thumbImageView.setImageBitmap(videoThumbnail);
        mJzvdStd.startVideo();

        initLineChart();
    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mJzvdStd = (JzvdStd) findViewById(R.id.exercise_video_view);
        mProgress = (ProgressBar) findViewById(R.id.progress_animation);
        mSurfaceView = (TouchGLView) findViewById(R.id.surface_view);
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        mSpeedText = (TextView) findViewById(R.id.speed_text);
        mAnglesText = (TextView) findViewById(R.id.angles_text);
        mRepeatText = (TextView) findViewById(R.id.repeat_text);
        mElapsedTimeText = (TextView) findViewById(R.id.elapsed_time_txt);
        cancelExerciseButton = (ImageButton) findViewById(R.id.cancel_exercise_btn);
        finishExerciseButton = (ImageButton) findViewById(R.id.finish_exercise_btn);
        playPauseButton = (ImageButton) findViewById(R.id.button_play_pause);
        startButton = (ImageButton) findViewById(R.id.button_start);
        endButton = (ImageButton) findViewById(R.id.button_end);
        speedButton = (ImageButton) findViewById(R.id.button_speed);
        frontViewButton = (ImageButton) findViewById(R.id.button_front_view);
        topViewButton = (ImageButton) findViewById(R.id.button_top_view);
        sideViewButton = (ImageButton) findViewById(R.id.button_side_view);
        showViewButton = (ImageButton) findViewById(R.id.button_show_path);
        chart = findViewById(R.id.line_chart);

        finishExerciseButton.setOnClickListener(this);
        playPauseButton.setOnClickListener(this);
        startButton.setOnClickListener(this);
        endButton.setOnClickListener(this);
        speedButton.setOnClickListener(this);
        frontViewButton.setOnClickListener(this);
        topViewButton.setOnClickListener(this);
        sideViewButton.setOnClickListener(this);
        showViewButton.setOnClickListener(this);
    }

    private void initLineChart() {
        chart.setOnChartValueSelectedListener(this);
        // enable description text
        chart.getDescription().setEnabled(true);
        // enable touch gestures
        chart.setTouchEnabled(true);
        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true);
        // set an alternative background color
        chart.setBackgroundColor(Color.LTGRAY);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);
        // add empty data
        chart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();
        // modify the legend ...
        l.setForm(LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        XAxis xl = chart.getXAxis();
        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private LineDataSet createNormalSet() {
        LineDataSet set = new LineDataSet(null, "Real Time Angles");
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(Color.BLUE);
        set.setLineWidth(3f);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        return set;
    }

    private LineDataSet createHighestSet() {
        LineDataSet set = new LineDataSet(null, null);
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(Color.TRANSPARENT);
        set.setLineWidth(3f);
        set.setCircleColor(Color.BLUE);
        set.setCircleHoleColor(Color.WHITE);
        set.setCircleRadius(4f);
        set.setValueTextColor(Color.GREEN);
        set.setValueTextSize(15f);
        return set;
    }

    private LineDataSet createLowestSet() {
        LineDataSet set = new LineDataSet(null, null);
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(Color.TRANSPARENT);
        set.setLineWidth(3f);
        set.setCircleColor(Color.BLUE);
        set.setCircleHoleColor(Color.WHITE);
        set.setCircleRadius(4f);
        set.setValueTextColor(Color.RED);
        set.setValueTextSize(15f);
        return set;
    }

    private void addEntry(float getNewValue) {
        LineData data = chart.getData();
        if (data != null) {
            ILineDataSet normalSet = data.getDataSetByIndex(1);
            ILineDataSet highestSet = data.getDataSetByIndex(2);
            ILineDataSet lowestSet = data.getDataSetByIndex(3);
            if (normalSet == null) {
                normalSet = createNormalSet();
                data.addDataSet(normalSet);
            }
            if (highestSet == null) {
                highestSet = createHighestSet();
                data.addDataSet(highestSet);
            }
            if (lowestSet == null) {
                lowestSet = createLowestSet();
                data.addDataSet(lowestSet);
            }

            newValue = getNewValue;
            newEntry = new Entry(normalSet.getEntryCount(), newValue);
            Entry fakeHighestEntry = new Entry(normalSet.getEntryCount(), -1f);
            Entry fakeLowestEntry = new Entry(normalSet.getEntryCount(), 101f);

            data.addEntry(newEntry, 1);
            if(newValue >= 50) {
                if(countAboveBaseline > 0) {
                    data.addEntry(newEntry, 2);
                    int countHighestSet = highestSet.getEntryCount();
                    int highestIndex = highestSet.getEntryCount()-countAboveBaseline-1;
                    highestEntry = highestSet.getEntryForIndex(highestIndex);
                    for(int i=countHighestSet-countAboveBaseline; i<countHighestSet; i++) {
                        if(highestSet.getEntryForIndex(i).getY() >= highestSet.getEntryForIndex(highestIndex).getY()) {
                            highestIndex = i;
                            highestEntry = highestSet.getEntryForIndex(i);
                        }
                    }
                    for(int i=countHighestSet-1; i>=countHighestSet-countAboveBaseline-1; i--) {
                        data.removeEntry(highestSet.getEntryForIndex(i), 2);
                    }
                    for(int i=countHighestSet-countAboveBaseline-1; i<countHighestSet; i++) {
                        if(i == highestIndex) {
                            data.addEntry(new Entry(highestSet.getEntryCount(), highestEntry.getY()), 2);
                        }
                        else {
                            data.addEntry(new Entry(highestSet.getEntryCount(), -1f), 2);
                        }
                    }
                }
                else {
                    data.addEntry(newEntry, 2);
                    highestEntry = newEntry;
                }
                data.addEntry(fakeHighestEntry, 3);
                countAboveBaseline++;
                if(countUnderBaseline > 0) {
                    exerciseUp = true;
                    countUnderBaseline = 0;
                }
            }
            else {
                if(countUnderBaseline > 0) {
                    data.addEntry(newEntry, 3);
                    int countLowestSet = lowestSet.getEntryCount();
                    int lowestIndex = lowestSet.getEntryCount()-countUnderBaseline-1;
                    lowestEntry = lowestSet.getEntryForIndex(lowestIndex);
                    for(int i=countLowestSet-countUnderBaseline; i<countLowestSet; i++) {
                        if(lowestSet.getEntryForIndex(i).getY() <= lowestSet.getEntryForIndex(lowestIndex).getY()) {
                            lowestIndex = i;
                            lowestEntry = lowestSet.getEntryForIndex(i);
                        }
                    }
                    for(int i=countLowestSet-1; i>=countLowestSet-countUnderBaseline-1; i--) {
                        data.removeEntry(lowestSet.getEntryForIndex(i), 3);
                    }
                    for(int i=countLowestSet-countUnderBaseline-1; i<countLowestSet; i++) {
                        if(i == lowestIndex) {
                            data.addEntry(new Entry(lowestSet.getEntryCount(), lowestEntry.getY()), 3);
                        }
                        else {
                            data.addEntry(new Entry(lowestSet.getEntryCount(), 101f), 3);
                        }
                    }
                }
                else {
                    data.addEntry(newEntry, 3);
                    lowestEntry = newEntry;
                }
                data.addEntry(fakeLowestEntry, 2);
                countUnderBaseline++;
                if(countAboveBaseline > 0) {
                    if(exerciseUp) {
                        exerciseDown = true;
                    }
                    countAboveBaseline = 0;

                    if(exerciseUp && exerciseDown) {
                        exerciseTimes++;
                        exerciseUp = false;
                        exerciseDown = false;
                    }
                }
            }
            data.notifyDataChanged();
            // let the chart know it's data has changed
            chart.notifyDataSetChanged();
            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(20);
            // move to the latest entry
            chart.moveViewToX(data.getEntryCount());

            oldValue = newValue;
            oldEntry = newEntry;
        }
    }

    private LineDataSet createBaselineSet() {
        LineDataSet set = new LineDataSet(null, "Baseline");
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(Color.YELLOW);
        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        return set;
    }

    private void addBaselineEntry() {

        LineData data = chart.getData();
        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            if (set == null) {
                set = createBaselineSet();
                data.addDataSet(set);
            }
            data.addEntry(new Entry(set.getEntryCount(), BASELINE), 0);
            data.notifyDataChanged();
            // let the chart know it's data has changed
            chart.notifyDataSetChanged();
            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(20);
            // move to the latest entry
            chart.moveViewToX(data.getEntryCount());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_play_pause:
                if (mRenderer != null) {
                    if (mPaused && mFrameIndex == mFrameCount - 1) {
                        mFrameIndex = 0;
                    }
                    mPaused = !mPaused;
                    refreshUI();
                    mSurfaceView.postOnAnimation(mRefreshPlayback);
                }
                break;
            case R.id.button_start:
                if (mRenderer != null && !mRealTime) {
                    mRenderer.setAutoPlayback(false);
                    mRenderer.setAllFrameIndex(0);
                    mSurfaceView.postOnAnimation(mRefreshPlayback);
                    mRenderer.setAutoPlayback(true);
                }
                refreshUI();
                break;
            case R.id.button_end:
                if (mRenderer != null && !mRealTime) {
                    mPaused = true;
                    mRenderer.setAutoPlayback(false);
                    mRenderer.setAllFrameIndex(mFrameCount);
                    mSurfaceView.postOnAnimation(mRefreshPlayback);
                }
                refreshUI();
                break;
            case R.id.button_speed:
                if (mRenderer != null && !mRealTime) {
                    mSpeed = mSpeed * 2f;
                    if (mSpeed > 1f) {
                        mSpeed = 0.25f;
                    }
                    if (mSpeed >= 1f) {
                        mSpeedText.setText((int)mSpeed + "x");
                    }
                    else {
                        mSpeedText.setText("1/" + (int)(1f/mSpeed) + "x");
                    }
                    mRenderer.setAllPlaybackSpeed(mSpeed);
                }
                refreshUI();
                break;
            case R.id.button_front_view:
                if (mRenderer != null) {
                    mRenderer.setCameraBeta((float) Math.PI/2.0f);
                    mRenderer.setCameraAlpha((float) Math.PI / 2.0f);
                }
                refreshUI();
                break;
            case R.id.button_top_view:
                if (mRenderer != null) {
                    mRenderer.setCameraBeta(0.0f);
                    mRenderer.setCameraAlpha((float) Math.PI/2.0f);
                }
                refreshUI();
                break;
            case R.id.button_side_view:
                if (mRenderer != null) {
                    mRenderer.setCameraBeta((float) Math.PI/2.0f);
                    mRenderer.setCameraAlpha((float) Math.PI);
                }
                refreshUI();
                break;
            case R.id.button_show_path:
                mBoneSelectorDialog.show();
                break;
            case R.id.cancel_exercise_btn:
                cancelDialog(mRealTime);
                cDialog.show();
                break;
            case R.id.finish_exercise_btn:
                finishDialog(mRealTime);
                fDialog.show();
                break;
        }
    }

    @Subscribe
    public void onDataUpdate(VisualiserData data) {
        if (data != null && mRenderer != null) {
            mData = data;
            NotchSkeletonRenderer.RendererContext rc = mRenderer.getRendererContext(0);
            if (rc == null) {
                rc = mRenderer.createRendererContext(data);
                mRenderer.setRendererContext(0, rc);
            } else {
                rc.setData(data);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.visualiser, menu);

        VisualizerSettings settings = VisualizerSettings.getInstance();

        if (settings != null) {
            MenuItem highlightBones = menu.findItem(R.id.highlight_bones);
            mHighlightBones = settings.isVisualizerShowFullBody();
            highlightBones.setChecked(mHighlightBones);

            MenuItem pinToCenter = menu.findItem(R.id.pin_to_center);
            mPinToCentre = settings.isVisualizerPinToCentre();
            pinToCenter.setChecked(mPinToCentre);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_open:
                openMeasurement();
                return true;
            case R.id.highlight_bones:
                mHighlightBones = !mHighlightBones;
                mRenderer.setHighlightBones(mHighlightBones);
                item.setChecked(mHighlightBones);
                VisualizerSettings.getInstance().putVisualizerShowFullBody(mHighlightBones);
                refreshUI();
                return true;
            case R.id.pin_to_center:
                mPinToCentre = !mPinToCentre;
                mRenderer.setRootMovement(!mPinToCentre);
                item.setChecked(mPinToCentre);
                VisualizerSettings.getInstance().putVisualizerPinToCenter(mPinToCentre);
                refreshUI();
                return true;
            case R.id.show_angles:
                mShowAngles = !mShowAngles;
                if (mShowAngles) mAnglesText.setVisibility(View.VISIBLE);
                else mAnglesText.setVisibility(View.GONE);
                return true;
            case R.id.export_angles:
                exportAngles();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_OPEN:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    setData(data.getData());
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void initRenderer() {
        new AsyncTask<Void, Void, ExtendedRenderer>() {
            @Override
            protected ExtendedRenderer doInBackground(Void... params) {
                try {
                    ExtendedRenderer renderer = new ExtendedRenderer(getApplicationContext());
                    renderer.setAutoPlayback(true);

                    if (mZipUri != null) {
                        for (Parcelable uri : mZipUri) {
                            VisualiserData data = VisualiserData.fromStream(new FileInputStream(new File(((Uri) uri).getPath())));
                            int dataIndex = renderer.addRendererContext(renderer.createRendererContext(data));
                            if (dataIndex > 0) {
                                renderer.setAlpha(dataIndex, SECONDARY_TRANSPARENCY);
                            }
                        }
                    } else if (mData != null) {
                        int dataIndex = renderer.addRendererContext(renderer.createRendererContext(mData));
                        if (dataIndex > 0) {
                            renderer.setAlpha(dataIndex, SECONDARY_TRANSPARENCY);
                        }
                    }

                    return renderer;
                } catch (Exception e) {
                    Log.e(TAG, "NotchSkeletonRenderer exception", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(ExtendedRenderer renderer) {
                super.onPostExecute(renderer);
                if (renderer != null) {
                    mRenderer = renderer;
                    mSurfaceView.setRenderer(renderer);
                    mProgress.setVisibility(View.GONE);
                    mSurfaceView.setVisibility(View.VISIBLE);
                    mShowPath = true;

                    mRenderer.setHighlightBones(mHighlightBones);
                    mRenderer.setRootMovement(!mPinToCentre);

                    if (mData == null){
                        mData = renderer.getRendererContext(0).getData();
                    }
                    mFrameCount = mData.getFrameCount();
                    mFrequency = mData.getFrequency();
                    mSeekBar.setMax(mFrameCount);
                    if (mRealTime) {
                        mSeekBar.setMax(0);
                        mSeekBar.setEnabled(false);
                        playPauseButton.setEnabled(false);
                    }
                    mRenderer.setRealTime(mRealTime);
                    mSkeleton = mData.getSkeleton();

                    mBoneNames = new String[mSkeleton.getBoneOrder().size()];
                    mCheckedBones = new boolean[mSkeleton.getBoneOrder().size()];
                    int i = 0;
                    for (Bone b : mSkeleton.getBoneOrder()) {
                        mBoneNames[i] = b.getName();
                        if (b.getName().equals("RightHand")) {
                            mCheckedBones[i] = true;
                            mBonesToShow.add(b);
                        } else {
                            mCheckedBones[i] = false;
                        }
                        i++;
                    }

                    buildBoneSelectorDialog();

                } else {
                    showNotification(R.string.error_invalid_measurement);
                    finish();
                }
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    public void setData(final Uri zipUri) {
        new AsyncTask<Void, Void, VisualiserData>() {

            @Override
            protected VisualiserData doInBackground(Void... params) {
                try {
                    return VisualiserData.fromStream(new FileInputStream(new File(zipUri.getPath())));
                } catch (Exception e) {
                    Log.e(TAG, "NotchSkeletonRenderer exception", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(final VisualiserData data) {
                super.onPostExecute(data);
                if (data != null) {
                    mZipUri = Arrays.copyOf(mZipUri, mZipUri.length + 1);
                    mZipUri[mZipUri.length - 1] = zipUri;
                    getIntent().putExtra(PARAM_INPUT_ZIP, mZipUri);

                    mSurfaceView.postOnAnimation(() -> {
                        int dataIndex = mRenderer.setRendererContext(mRenderer.createRendererContext(data));
                        mFrameCount = mRenderer.getRendererContext(dataIndex).getFrameCount();
                        mSeekBar.setMax(mFrameCount);
                        mData = data;
                        refreshUI();
                    });

                } else {
                    showNotification(R.string.error_invalid_measurement);
                }
            }
        }.execute();
    }

    public void refreshUI() {
        mSeekBar.setProgress(mFrameIndex);

        if (mPaused) {
            playPauseButton.setImageResource(R.drawable.ic_play);
        } else {
            playPauseButton.setImageResource(R.drawable.ic_pause);
        }

        refreshAngles();
    }

    private Runnable mRefreshPlayback = new Runnable() {
        @Override
        public void run() {
            if (mRenderer != null) {
                for (int i=0; i<mRenderer.getContextSize();i++) {
                    if (mRenderer.rendererContextIndexExists(i)) mRenderer.setFrameIndex(i,mFrameIndex);
                }

                if (!mSeeking) {
                    mRenderer.setAutoPlayback(!mPaused);
                }
            }
        }
    };

    private Runnable mStartSeeking = new Runnable() {
        @Override
        public void run() {
            if (mRenderer != null && mSeeking) {
                mRenderer.setAutoPlayback(false);
            }
        }
    };

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mRenderer != null && fromUser && mFrameIndex != progress) {
            mFrameIndex = progress;
            mSurfaceView.postOnAnimation(mRefreshPlayback);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (mRenderer != null) {
            mSeeking = true;
            mSurfaceView.postOnAnimation(mStartSeeking);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mSeeking = false;
        mSurfaceView.postOnAnimation(mRefreshPlayback);
    }

    private void openMeasurement() {
        Intent chooser = new Intent(Intent.ACTION_GET_CONTENT);
        chooser.setType("application/zip");
        chooser.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(chooser, REQUEST_OPEN);
    }

    // Show angles
    public void refreshAngles() {

        if(mData == null) {
            return;
        }
        this.runOnUiThread(() -> {
            calculateAngles(mFrameIndex);
        });
    }

    // Calculate angles
    private void calculateAngles(int frameIndex) {
        Bone root = mSkeleton.getRoot();

        Bone chestBottom = mSkeleton.getBone("ChestBottom");
        Bone hip = mSkeleton.getBone("Hip");

        Bone leftThigh = mSkeleton.getBone("LeftThigh");
        Bone leftLowerLeg = mSkeleton.getBone("LeftLowerLeg");

        Bone rightThigh = mSkeleton.getBone("RightThigh");
        Bone rightLowerLeg = mSkeleton.getBone("RightLowerLeg");

        fvec3 chestBottomAngles = new fvec3();
        fvec3 hipAngles = new fvec3();
        fvec3 leftKneeAngles = new fvec3();
        fvec3 rightKneeAngles = new fvec3();

        // Calculate forearm angles with respect to upper arm (determine elbow joint angles).
        // Angles correspond to rotations around X,Y and Z axis of the paren bone's coordinate system, respectively.
        // The coordinate system is X-left, Y-up, Z-front aligned.
        // Default orientations are defined in the steady pose (in the skeleton file)
        // Usage: calculateRelativeAngle(Bone child, Bone parent, int frameIndex, fvec3 output)
        mData.calculateRelativeAngle(leftLowerLeg, leftThigh, frameIndex, leftKneeAngles);
        mData.calculateRelativeAngle(rightLowerLeg, rightThigh, frameIndex, rightKneeAngles);

        // Calculate chest angles with respect root, i.e. absolute angles
        // The root orientation is the always the same as in the steady pose.
        mData.calculateRelativeAngle(chestBottom, root, frameIndex, chestBottomAngles);
        mData.calculateRelativeAngle(hip, root, frameIndex, hipAngles);

        // Show angles
        StringBuilder sb = new StringBuilder();

        sb.append("Chest Bottom Angles:\n")
                // Anterior/posterior tilt (forward/backward bend) is rotation around global X axis
                .append("Anterior(+)/posterior(-) tilt: ").append((int)chestBottomAngles.get(0)).append("°\n")
                // Rotation to left/right is rotation around the global Y axis
                .append("Rotation left(+)/right(-): ").append((int)chestBottomAngles.get(1)).append("°\n")
                // Lateral tilt (side bend) is rotation around global Z axis
                .append("Lateral tilt left(-)/right(+): ").append((int)chestBottomAngles.get(2)).append("°\n");

        sb.append("\nHip Angles:\n")
                // Anterior/posterior tilt (forward/backward bend) is rotation around global X axis
                .append("Anterior(+)/posterior(-) tilt: ").append((int)hipAngles.get(0)).append("°\n")
                // Rotation to left/right is rotation around the global Y axis
                .append("Rotation left(+)/right(-): ").append((int)hipAngles.get(1)).append("°\n")
                // Lateral tilt (side bend) is rotation around global Z axis
                .append("Lateral tilt left(-)/right(+): ").append((int)hipAngles.get(2)).append("°\n");

        sb.append("\nLeft Knee Angles:\n")
                // Extension/flexion is rotation around the upperarm's X-axis
                .append("Extension(+)/flexion(-): ").append((int)leftKneeAngles.get(0)).append("°\n")
                // Supination/pronation is rotation around the upperarm's Y-axis
                .append("Supination(+)/pronation(-): ").append((int)leftKneeAngles.get(1)).append("°\n");

        sb.append("\nRight Knee Angles:\n")
                // Extension/flexion is rotation around the upperarm's X-axis
                .append("Extension(+)/flexion(-): ").append((int)rightKneeAngles.get(0)).append("°\n")
                // Supination/pronation is rotation around the upperarm's Y-axis
                .append("Supination(+)/pronation(-): ").append((int)rightKneeAngles.get(1)).append("°\n");

        mAnglesText.setText(sb.toString());

        if(countFrame % 30 == 0) {
            addBaselineEntry();
            addEntry((float)leftKneeAngles.get(0));
            mRepeatText.setText("Repeat: " + exerciseTimes);
        }
    }

    public void showNotification(final int stringId) {
        try {
            Toast.makeText(getApplicationContext(), stringId, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Toast exception", e);
        }
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    class ExtendedRenderer extends NotchSkeletonRenderer {
        private final float cameraBetaMax = (float) Math.PI - 0.01f;
        private final float cameraBetaMin = 0.01f;
        private final float cameraDistanceMin = 0.5f;

        ColorShader mVisualisationShader;

        public ExtendedRenderer(Context context) {
            super(context);
            mContext = context;
        }

        @Override
        protected void createAdditionalShaders() {
            super.createAdditionalShaders();
            mVisualisationShader = new ColorShader(getApplicationContext());
            addShader(mVisualisationShader);
        }

        @Override
        protected void onSurfaceCreatedGL(GL10 unused, EGLConfig config) {
            super.onSurfaceCreatedGL(unused, config);
            GLES20.glClearColor(0.87f, 0.87f, 0.87f, 0.35f);
        }

        public RendererContext createRendererContext(VisualiserData data) {
            return createRendererContext(data, OBJ_ASSET, MTL_ASSET, null);
        }

        public RendererContext createRendererContext(VisualiserData data, String obj, String mtl, RigidBody.StickCustomizer stick) {
            final PlotDemo plotDemo = new PlotDemo(data, isGroundDrawn);
            if (!isGroundDrawn) isGroundDrawn = true;
            plotDemo.init();

            return new RendererContext(data, obj, mtl, stick) {

                @Override
                public void prepare() {
                    plotDemo.prepare(mVisualisationShader);
                    super.prepare();
                }

                @Override
                public void draw() {
                    super.draw();
                    plotDemo.setShowPath(mShowPath ? mBonesToShow : null);
                    plotDemo.draw(getFrameIndex());
                }
            };
        }


        @Override
        protected void onFrameIndexChanged(int dataIndex, int frameIndex) {
            super.onFrameIndexChanged(dataIndex, frameIndex);
            mFrameIndex = frameIndex;
            mSeekBar.setProgress(mFrameIndex);
            refreshAngles();
            countFrame++;
            if(exerciseTimes == 10 && !isSucceedDialogShow) {
                isSucceedDialogShow = true;
                runOnUiThread(() -> {
                    succeedDialog();
                    sDialog.show();
                });
            }
        }

        @Override
        protected void onPlaybackFinished(int dataIndex, int frameIndex) {
            super.onPlaybackFinished(dataIndex, frameIndex);
            mPaused = true;
            runOnUiThread(() -> {
                refreshUI();
            });
            mSurfaceView.postOnAnimation(mRefreshPlayback);
        }


        @Override
        public void setCameraBeta(float cameraBeta) {
            if (cameraBeta > cameraBetaMax) {
                cameraBeta = cameraBetaMax;
            } else if (cameraBeta < cameraBetaMin) {
                cameraBeta = cameraBetaMin;
            }

            super.setCameraBeta(cameraBeta);
        }

        @Override
        public void setCameraDistance(float cameraDistance) {
            if (cameraDistance < cameraDistanceMin) {
                cameraDistance = cameraDistanceMin;
            }

            super.setCameraDistance(cameraDistance);
        }
    }


    private void buildBoneSelectorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select bones!");

        builder.setMultiChoiceItems(mBoneNames, mCheckedBones, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                Bone b = mSkeleton.getBone(mBoneNames[which]);
                mCheckedBones[which] = isChecked;
                if (isChecked) {
                    if (!mBonesToShow.contains(b)) mBonesToShow.add(b);
                } else {
                    if (mBonesToShow.contains(b)) mBonesToShow.remove(b);
                }
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton("Clear all", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mBonesToShow.removeAll(mBonesToShow);
                for (int i=0; i<mCheckedBones.length; i++) {
                    Bone b = mSkeleton.getBone(mBoneNames[i]);
                    mBonesToShow.remove(b);
                    mCheckedBones[i]= false;
                    ((AlertDialog) dialog).getListView().setItemChecked(i, false);
                }
            }
        });

        mBoneSelectorDialog = builder.create();
    }

    private void exportAngles() {
        File directory = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+ "/PhysioAssist/Record/", "exported_angles");
        if (!directory.isDirectory()) directory.mkdirs();
        BufferedWriter writer = null;

        Bone root = mSkeleton.getRoot();

        Bone chestBottom = mSkeleton.getBone("ChestBottom");
        Bone hip = mSkeleton.getBone("Hip");

        Bone leftThigh = mSkeleton.getBone("LeftThigh");
        Bone leftLowerLeg = mSkeleton.getBone("LeftLowerLeg");

        Bone rightThigh = mSkeleton.getBone("RightThigh");
        Bone rightLowerLeg = mSkeleton.getBone("RightLowerLeg");

        fvec3 chestBottomAngles = new fvec3();
        fvec3 hipAngles = new fvec3();
        fvec3 leftKneeAngles = new fvec3();
        fvec3 rightKneeAngles = new fvec3();

        String[] mNames = {"ChestBottom", "Hip", "LeftKnee", "RightKnee"};

        for (int i = 0; i < mNames.length; i++) {
            String mName = mNames[i];

            File file = new File(directory, mName.toLowerCase() + ".csv");
            try {
                writer = new BufferedWriter(new FileWriter(file));
                StringBuilder sb = new StringBuilder();

                for (int frame = 0; frame < mFrameCount; frame++) {
                    // Header
                    if (frame == 0) {
                        if (i==0 || i==1) {
                            sb.append("Time [sec], Anterior(+)/posterior(-) tilt, Rotation left(+)/right(-), Lateral tilt left(-)/right(+),\n");
                        } else {
                            sb.append("Time [sec], Extension(+)/flexion(-), Supination(+)/pronation(-),\n");
                        }
                    }

                    // Angles
                    if (i==0) {
                        mData.calculateRelativeAngle(chestBottom, root, frame, chestBottomAngles);

                        sb.append(String.valueOf(decimalFormat.format(frame / mFrequency))).append(",")
                                .append(chestBottomAngles.get(0)).append(",")
                                .append(chestBottomAngles.get(1)).append(",")
                                .append(chestBottomAngles.get(2)).append(",")
                                .append("\n");
                    } else if(i==1) {
                        mData.calculateRelativeAngle(chestBottom, root, frame, hipAngles);

                        sb.append(String.valueOf(decimalFormat.format(frame / mFrequency))).append(",")
                                .append(hipAngles.get(0)).append(",")
                                .append(hipAngles.get(1)).append(",")
                                .append(hipAngles.get(2)).append(",")
                                .append("\n");

                    } else if(i==2) {
                        mData.calculateRelativeAngle(leftLowerLeg, leftThigh, frame, leftKneeAngles);

                        sb.append(String.valueOf(decimalFormat.format(frame / mFrequency))).append(",")
                                .append(leftKneeAngles.get(0)).append(",")
                                .append(leftKneeAngles.get(1)).append(",")
                                .append("\n");
                    } else {
                        mData.calculateRelativeAngle(rightLowerLeg, rightThigh, frame, rightKneeAngles);

                        sb.append(String.valueOf(decimalFormat.format(frame / mFrequency))).append(",")
                                .append(rightKneeAngles.get(0)).append(",")
                                .append(rightKneeAngles.get(1)).append(",")
                                .append("\n");
                    }
                }
                writer.write(sb.toString());

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (writer != null) {
                        writer.close();
                    }
                    Util.showNotification("Angles are exported to : '" + directory);

                } catch (Exception ignored) {
                }
            }
        }
    }

    private void finishDialog(boolean realTime) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Notch:");

        if(realTime) {
            builder.setMessage("Have you done your exercise?");
        }
        else {
            builder.setMessage("Are you sure to exit play back?");
        }

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("isCancellable", true);
                resultIntent.putExtra("exerciseTimes", exerciseTimes);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        builder.setNegativeButton("Cancel", null);

        fDialog = builder.create();
    }

    private void cancelDialog(boolean realTime) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Notch:");

        builder.setMessage("Are you sure to cancel your exercise?");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("isCancelExercise", true);
                resultIntent.putExtra("isCancellable", true);
                resultIntent.putExtra("exerciseTimes", exerciseTimes);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        builder.setNegativeButton("Cancel", null);

        fDialog = builder.create();
    }

    private void succeedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Notch:");

        builder.setMessage("Congratulations! you have completed your exercise successfully, Do you want to continue?");

        builder.setPositiveButton("OK", null);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("isCancellable", true);
                resultIntent.putExtra("exerciseTimes", exerciseTimes);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        sDialog = builder.create();
    }

    private Bitmap getLocalVideoThumbnail(String filePath) {
        Bitmap bitmap = null;
        //MediaMetadataRetriever 是android中定义好的一个类，提供了统一
        //的接口，用于从输入的媒体文件中取得帧和元数据；
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            //根据文件路径获取缩略图
            retriever.setDataSource(filePath);
            //获得第一帧图片
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }
        return bitmap;
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Jzvd.releaseAllVideos();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (Jzvd.backPress()) {
            return;
        }
        cancelDialog(mRealTime);
        cDialog.show();
    }
}