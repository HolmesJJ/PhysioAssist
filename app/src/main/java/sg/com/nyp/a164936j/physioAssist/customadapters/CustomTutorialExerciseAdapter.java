package sg.com.nyp.a164936j.physioAssist.customadapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import sg.com.nyp.a164936j.physioAssist.configuration.Config;
import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.fragments.interfaces.CustomOnClickListener;

public class CustomTutorialExerciseAdapter extends BaseAdapter {

    private Context context;
    private List<String> result;
    private List<String> images;

    private CustomOnClickListener customListener;

    private static LayoutInflater inflater = null;

    public void setThumbnailClickListener(CustomOnClickListener listener){
        this.customListener = listener;
    }

    public CustomTutorialExerciseAdapter(Context context, List<String> result, List<String> images) {
        this.context = context;
        this.result = result;
        this.images = images;

        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return result.size();
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
        Button btn;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        ViewHolder holder = new ViewHolder();
        View rowView;

        rowView = inflater.inflate(R.layout.layout_exercise_routine, null);
        holder.tv = rowView.findViewById(R.id.video_title);
        holder.img = rowView.findViewById(R.id.exercise_thumbnail);
        holder.btn = rowView.findViewById(R.id.btnStartExercise);

        if(images.get(position).equals("ankle_pump_lying")) {
            holder.img.setImageResource(R.drawable.ankle_pump_lying);
        }
        else if(images.get(position).equals("hip_side-slides_left")) {
            holder.img.setImageResource(R.drawable.hip_side_slides_left);
        }
        else if(images.get(position).equals("deep_breathing")) {
            holder.img.setImageResource(R.drawable.deep_breathing);
        }
        else if(images.get(position).equals("upper_limb_strengthen")) {
            holder.img.setImageResource(R.drawable.upper_limb_strengthen);
        }

        holder.tv.setText(result.get(position));


        holder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: handle multiple fragments depends on which button is being pressed
                int parentId = parent.getId();
                Log.d(Config.TAG_BUTTON, "(CustomPatientExerciseAdapter) getParent: "+ parentId);

                customListener.onThumbnailClick(parentId, position);

            }
        });

        return rowView;
    }
}