package sg.com.nyp.a164936j.physioAssist.customadapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.configuration.CustomSharedPreference;
import sg.com.nyp.a164936j.physioAssist.fragments.CustomListView;
import sg.com.nyp.a164936j.physioAssist.models.Exercise;

public class CustomPhysioScheduleAdapter extends BaseAdapter {

    private Context context;
    private static LayoutInflater inflater = null;
    private int itemHeight;
    private int itemWidth;
    private List<String> exTimeList;
    private List<Exercise> prescribedExerciseList = new ArrayList<>();

    public CustomPhysioScheduleAdapter(Context context, List<String> exTimeList, int itemHeight, int itemWidth) {
        this.context = context;
        this.exTimeList = exTimeList;
        this.itemHeight = itemHeight;
        this.itemWidth = itemWidth;

        inflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return exTimeList.size();
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
        TextView time;
        ListView exerciseList;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        ViewHolder holder;

        if(convertView == null){
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.layout_physio_schedule, null);
            AbsListView.LayoutParams param = new AbsListView.LayoutParams(itemWidth, itemHeight/2-15);
            convertView.setLayoutParams(param);

            holder.time = convertView.findViewById(R.id.physio_schedule_item_time);
            holder.exerciseList = convertView.findViewById(R.id.physio_schedule_item_listview);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.time.setText(exTimeList.get(position));
        CustomListView.setListViewHeightBasedOnChildren(holder.exerciseList);

        prescribedExerciseList.clear();

        for(int i=0; i<CustomSharedPreference.prescribedExercises.size(); i++) {
            if(CustomSharedPreference.prescribedExercises.get(i).getExTime().equals(exTimeList.get(position))) {
                prescribedExerciseList.add(CustomSharedPreference.prescribedExercises.get(i));
            }
        }

        System.out.println("position777: " + position);

        //TODO: Populate listview for videos
        CustomPhysioScheduleVideosAdapter adapter = new CustomPhysioScheduleVideosAdapter(context, prescribedExerciseList);
        holder.exerciseList.setAdapter(adapter);

        return convertView;
    }


}
