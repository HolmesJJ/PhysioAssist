package sg.com.nyp.a164936j.physioAssist.customadapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import java.util.List;

import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.configuration.Config;
import sg.com.nyp.a164936j.physioAssist.models.Exercise;

public class CustomPatientProgressHistoryAdapter extends BaseAdapter {

    private Context context;
    private Resources resources;
    private List<Exercise> performedPrescribedExercises;

    private static LayoutInflater inflater = null;

    public CustomPatientProgressHistoryAdapter(Context context, Resources resources, List<Exercise> performedPrescribedExercises) {
        this.context = context;
        this.resources = resources;
        this.performedPrescribedExercises = performedPrescribedExercises;

        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return performedPrescribedExercises.size();
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
        TextView exerciseHeader;
        TextView exerciseSubHeader;
        GridView gridView;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        ViewHolder holder;

        if(convertView == null){
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.layout_progress_history, null);
            holder.exerciseHeader = convertView.findViewById(R.id.progress_history_item_header);
            holder.exerciseSubHeader = convertView.findViewById(R.id.progress_history_item_sub_header);
            holder.gridView = convertView.findViewById(R.id.progress_history_gridview);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.exerciseHeader.setText(performedPrescribedExercises.get(position).getName());
        holder.exerciseSubHeader.setText(resources.getString(R.string.progress_history_item_sub_header));

        //TODO: populate gridview with images

        Log.d(Config.TAG_CUSTOM_ADAPTER, "(CustomPatientProgressHistoryAdapter) in getView");

        //change petals with proper array
        CustomPatientHistoryGridViewAdapter adapter = new CustomPatientHistoryGridViewAdapter
                (
                        context,
                        performedPrescribedExercises.get(position).getCompletedTimes()
                );
        holder.gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        holder.gridView.setAdapter(adapter);


        return convertView;
    }
}
