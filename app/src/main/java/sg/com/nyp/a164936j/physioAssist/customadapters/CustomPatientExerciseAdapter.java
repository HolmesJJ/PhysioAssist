package sg.com.nyp.a164936j.physioAssist.customadapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import sg.com.nyp.a164936j.physioAssist.IPAddress;
import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.configuration.GetSetSharedPreferences;
import sg.com.nyp.a164936j.physioAssist.customview.CircleProgressBar;
import sg.com.nyp.a164936j.physioAssist.fragments.interfaces.CustomOnClickListener;
import sg.com.nyp.a164936j.physioAssist.fragments.interfaces.OnTaskCompleted;
import sg.com.nyp.a164936j.physioAssist.httpasynctask.GetAuth;

public class CustomPatientExerciseAdapter extends BaseAdapter implements OnTaskCompleted {

    private static final int GET_PERFORM_TASK_ID = 1;
    private static final int REFRESH_TOKEN_TASK_ID = 2;

    private Context context;
    private Resources resources;
    private ListView listView;
    private List<Integer> exId;
    private List<Integer> peId;
    private List<String> exName;
    private List<String> images;
    private List<Integer> exTimePerDays;

    private CustomOnClickListener customListener;
    private static LayoutInflater inflater = null;
    private int parentId;
    private String stayId;
    private String language;
    private String status = "failed";
    private String access_token;

    private File demoFolder;
    private File countingFolder;

    private LruCache<String, BitmapDrawable> mImageCache;
    private LruCache<Integer, Integer> mProgressCache;

    public void setThumbnailClickListener(CustomOnClickListener listener){
        this.customListener = listener;
    }

    public CustomPatientExerciseAdapter(Context context, Resources resources, List<Integer> exId, List<Integer> peId, List<String> exName, List<String> images, List<Integer> exTimePerDays) {
        this.context = context;
        this.resources = resources;
        this.exId = exId;
        this.peId = peId;
        this.exName = exName;
        this.images = images;
        this.exTimePerDays = exTimePerDays;

        int maxCache = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxCache / 5;

        mImageCache = new LruCache<String, BitmapDrawable>(cacheSize) {
            @Override
            protected int sizeOf(String key, BitmapDrawable value) {
                return value.getBitmap().getByteCount();
            }
        };

        mProgressCache = new LruCache<Integer, Integer>(cacheSize) {
            @Override
            protected int sizeOf(Integer key, Integer value) {
                return value;
            }
        };

        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        language = GetSetSharedPreferences.getDefaults("language", context);
        stayId = GetSetSharedPreferences.getDefaults("stayId", context);
    }

    @Override
    public int getCount() {
        return exId.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder{
        TextView tv;
        ImageView img;
        Button startBtn;
        CircleProgressBar mCircleProgressBar;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        ViewHolder viewHolder;

        if (listView == null) {
            listView = (ListView) parent;
        }
        parentId = listView.getId();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.layout_exercise_routine, null);
            viewHolder = new ViewHolder();

            viewHolder.tv = convertView.findViewById(R.id.video_title);
            viewHolder.img = convertView.findViewById(R.id.exercise_thumbnail);
            viewHolder.startBtn = convertView.findViewById(R.id.btnStartExercise);
            viewHolder.mCircleProgressBar = convertView.findViewById(R.id.tasks_view);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.img.setTag(images.get(position));
        viewHolder.tv.setText(exName.get(position));
        viewHolder.startBtn.setText(resources.getString(R.string.btnStartExercise));
        viewHolder.mCircleProgressBar.setTag(peId.get(position));
        viewHolder.mCircleProgressBar.setProgress(0, exTimePerDays.get(position));

        // 如果本地已有缓存，就从本地读取，否则从网络请求数据
        if (mImageCache.get(images.get(position)) != null) {
            viewHolder.img.setImageDrawable(mImageCache.get(images.get(position)));
        } else {
            ImageTask it = new ImageTask();
            it.execute(images.get(position));
        }

        // 如果本地已有缓存，就从本地读取，否则从网络请求数据
        if (mProgressCache.get(peId.get(position)) != null) {
            viewHolder.mCircleProgressBar.setProgress(mProgressCache.get(peId.get(position)), exTimePerDays.get(position));
        } else {
            getPatientProgress(peId.get(position));
        }

        demoFolder = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+ "/PhysioAssist/Video/Demo");
        countingFolder = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+ "/PhysioAssist/Video/Counting");

        viewHolder.img.setOnClickListener((View view) -> {
            customListener.onThumbnailClick(parentId, position);
        });

        viewHolder.startBtn.setOnClickListener((View view) -> {
            customListener.onStartExerciseClick(parentId, position);
        });

        if(findVideoByExerciseId(exId.get(position))) {
            viewHolder.img.setColorFilter(Color.argb(0, 255, 255, 255));
        }

        return convertView;
    }

    private void getPatientProgress(int peId){
        String accessToken = GetSetSharedPreferences.getDefaults("access_token", context);
        GetAuth getPatientProgressTask = new GetAuth(CustomPatientExerciseAdapter.this, GET_PERFORM_TASK_ID);
        getPatientProgressTask.execute("http://" + IPAddress.awsipaddress + "/PhysioWebPortal/api/hospitalstay/" + stayId + "/performed", accessToken);
    }

    // findVideoByName
    private boolean findVideoByExerciseId(int exerciseId) {
        boolean isDemoFileExist = false;
        File[] demoFolderChildren = demoFolder.listFiles();
        for (int i = 0; i < demoFolderChildren.length; i++)  {
            if(demoFolderChildren[i].getName().equals(exerciseId + "_demo_" + language + ".mp4")) {
                isDemoFileExist = true;
                break;
            }
        }

        boolean isCountingFileExist = false;
        File[] countingFolderChildren = countingFolder.listFiles();
        for (int i = 0; i < countingFolderChildren.length; i++)  {
            if(countingFolderChildren[i].getName().equals(exerciseId + "_counting_" + language + ".mp4")) {
                isCountingFileExist = true;
                break;
            }
        }

        // 如果video不存在
        if(!isDemoFileExist || !isCountingFileExist) {
            return false;
        }
        // 如果video存在
        else {
            return true;
        }
    }

    public class ImageTask extends AsyncTask<String, Void, BitmapDrawable> {
        private String imageUrl;

        @Override
        protected BitmapDrawable doInBackground(String... params) {
            imageUrl = params[0];
            Bitmap bitmap = null;
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = null;
                try {
                    urlConnection.setRequestMethod("GET");
                    // receive response as inputStream
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    bitmap = BitmapFactory.decodeStream(inputStream);
                } finally {
                    if (inputStream!=null)
                        inputStream.close();
                    if (urlConnection!=null)
                        urlConnection.disconnect();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            BitmapDrawable db = new BitmapDrawable(listView.getResources(), bitmap);

            // 如果本地还没缓存该图片，就缓存
            if (mImageCache.get(imageUrl) == null) {
                mImageCache.put(imageUrl, db);
            }
            return db;
        }

        @Override
        protected void onPostExecute(BitmapDrawable result) {
            // 通过Tag找到我们需要的ImageView，如果该ImageView所在的item已被移出页面，就会直接返回null
            ImageView iv = (ImageView) listView.findViewWithTag(imageUrl);
            if (iv != null && result != null) {
                iv.setImageDrawable(result);
            }
        }
    }

    @Override
    public void onTaskCompleted(String response, int requestId) {
        if(response.contains("<html")) {
            // refreshToken();
        } else {
            if(requestId == GET_PERFORM_TASK_ID) {

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date currentDateTime = new Date(System.currentTimeMillis());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(currentDateTime);
                int currentYear = calendar.get(Calendar.YEAR);
                int currentMonth = calendar.get(Calendar.MONTH) + 1;
                int currentDate = calendar.get(Calendar.DATE);
                String currentDateTxt = currentYear + "-" + currentMonth + "-" + currentDate;

                int countPerformedExercised = 0;
                int prescribeExId = 0;
                int exTimePerDay = 0;

                boolean isJSONArray = false;
                boolean isJSONObject = false;
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    isJSONObject = true;

                    prescribeExId = jsonObject.getInt("PEId");
                    String startTime = jsonObject.getString("StartTime");
                    exTimePerDay = jsonObject.getInt("ExTimePerDay");

                    Date startDateDate = dateFormat.parse(startTime);
                    calendar.setTime(startDateDate);
                    int startYear = calendar.get(Calendar.YEAR);
                    int startMonth = calendar.get(Calendar.MONTH) + 1;
                    int startDate = calendar.get(Calendar.DATE);
                    String startDateTxt = startYear + "-" + startMonth + "-" + startDate;

                    if(currentDateTxt.equals(startDateTxt)) {
                        countPerformedExercised++;
                    }

                    status = "succeeded";
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    isJSONArray = true;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = new JSONObject(jsonArray.getString(i));

                        prescribeExId = jsonObject.getInt("PEId");
                        String startTime = jsonObject.getString("StartTime");
                        exTimePerDay = jsonObject.getInt("ExTimePerDay");

                        Date startDateDate = dateFormat.parse(startTime);
                        calendar.setTime(startDateDate);
                        int startYear = calendar.get(Calendar.YEAR);
                        int startMonth = calendar.get(Calendar.MONTH) + 1;
                        int startDate = calendar.get(Calendar.DATE);
                        String startDateTxt = startYear + "-" + startMonth + "-" + startDate;

                        if(currentDateTxt.equals(startDateTxt)) {
                            countPerformedExercised++;
                        }

                        System.out.println("6666666666666666666");
                        System.out.println(prescribeExId);
                        System.out.println(startDateTxt);
                        System.out.println(currentDateTxt);
                    }

                    status = "succeeded";
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(isJSONArray || isJSONObject) {
                    if(status.equals("succeeded")) {
                        CircleProgressBar circleProgressBar = (CircleProgressBar) listView.findViewWithTag(prescribeExId);
                        if (circleProgressBar != null && prescribeExId != 0 && exTimePerDay != 0) {
                            circleProgressBar.setProgress(countPerformedExercised, exTimePerDay);
                            // 如果本地还没缓存该图片，就缓存
                            if (mProgressCache.get(prescribeExId) == null) {
                                mProgressCache.put(prescribeExId, countPerformedExercised);
                            }
                        }
                    }
                }
                else if(!isJSONArray && !isJSONObject) {
                    status = "succeeded";
                    if(status.equals("succeeded")) {
                        CircleProgressBar circleProgressBar = (CircleProgressBar) listView.findViewWithTag(prescribeExId);
                        if (circleProgressBar != null && prescribeExId != 0 && exTimePerDay != 0) {
                            circleProgressBar.setProgress(countPerformedExercised, exTimePerDay);
                            // 如果本地还没缓存该图片，就缓存
                            if (mProgressCache.get(prescribeExId) == null) {
                                mProgressCache.put(prescribeExId, countPerformedExercised);
                            }
                        }
                    }
                }
            }
        }
    }
}