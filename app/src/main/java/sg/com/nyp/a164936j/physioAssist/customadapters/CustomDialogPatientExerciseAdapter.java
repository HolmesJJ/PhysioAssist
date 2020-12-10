package sg.com.nyp.a164936j.physioAssist.customadapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.fragments.interfaces.CustomOnClickListener;

public class CustomDialogPatientExerciseAdapter extends BaseAdapter {

    private Context context;
    private ListView listView;
    private List<String> header;
    private List<String> headerAndTitle;
    private List<String> images;

    private CustomOnClickListener customListener;
    private static LayoutInflater inflater = null;
    private ViewHolder holder;
    private View rowView;
    private LruCache<String, BitmapDrawable> mImageCache;
    private int parentId;

    public void setDialogThumbnailClickListener(CustomOnClickListener listener){
        this.customListener = listener;
    }

    public CustomDialogPatientExerciseAdapter(Context context, List<String> header, List<String> headerAndTitle, List<String> images) {
        this.context = context;
        this.header = header;
        this.headerAndTitle = headerAndTitle;
        this.images = images;

        int maxCache = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxCache / 5;

        mImageCache = new LruCache<String, BitmapDrawable>(cacheSize) {
            @Override
            protected int sizeOf(String key, BitmapDrawable value) {
                return value.getBitmap().getByteCount();
            }
        };

        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return headerAndTitle.size();
    }

    @Override
    public Object getItem(int position) {
        return headerAndTitle.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        if(header.contains(getItem(position))){
            return false;
        }
        return super.isEnabled(position);
    }

    public class ViewHolder{
        TextView tv;
        ImageView img;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        if (listView == null) {
            listView = (ListView) parent;
        }

        holder = new ViewHolder();
        parentId = listView.getId();

        if(header.contains(getItem(position))){
            rowView = inflater.inflate(R.layout.dialog_exercise_list_header, null);
            holder.tv = rowView.findViewById(R.id.dialog_exercise_header);
            holder.tv.setText(headerAndTitle.get(position));
        }
        else {
            rowView = inflater.inflate(R.layout.dialog_exercise_list_item, null);
            holder.tv = rowView.findViewById(R.id.dialog_exercise_title);
            holder.img = rowView.findViewById(R.id.dialog_exercise_thumbnail);
            holder.tv.setText(headerAndTitle.get(position));
            holder.img.setTag(images.get(position));

            // 如果本地已有缓存，就从本地读取，否则从网络请求数据
            if (mImageCache.get(images.get(position)) != null) {
                holder.img.setImageDrawable(mImageCache.get(images.get(position)));
            } else {
                ImageTask it = new ImageTask();
                it.execute(images.get(position));
            }

            holder.tv.setOnClickListener((View view) -> {
                customListener.onStartExerciseClick(parentId, position);
            });
            holder.img.setOnClickListener((View view) -> {
                customListener.onStartExerciseClick(parentId, position);
            });
        }

        return rowView;
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
}