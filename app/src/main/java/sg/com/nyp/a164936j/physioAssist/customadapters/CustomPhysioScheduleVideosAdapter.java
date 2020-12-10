package sg.com.nyp.a164936j.physioAssist.customadapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.models.Exercise;

public class CustomPhysioScheduleVideosAdapter extends BaseAdapter {

    private Context context;
    private List<Exercise> prescribedExerciseList;

    private static LayoutInflater inflater = null;

    public CustomPhysioScheduleVideosAdapter(Context context, List<Exercise> prescribedExerciseList) {
        this.context = context;
        this.prescribedExerciseList = prescribedExerciseList;

        inflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return prescribedExerciseList.size();
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
        TextView exerciseTitle;
        TextView exerciseType;
        TextView exerciseSet;
        TextView exerciseRepeat;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        ViewHolder holder;
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.layout_physio_schedule_video_item, null);
            holder.exerciseTitle = convertView.findViewById(R.id.physio_schedule_item_title_txt);
            holder.exerciseType = convertView.findViewById(R.id.physio_schedule_item_type_txt);
            holder.exerciseSet = convertView.findViewById(R.id.physio_schedule_item_set_txt);
            holder.exerciseRepeat = convertView.findViewById(R.id.physio_schedule_item_repeat_txt);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        System.out.println("position666: " + position);
        for (int i = 0; i < prescribedExerciseList.size(); i++) {
            System.out.println(prescribedExerciseList.get(i).getName());
        }

        holder.exerciseTitle.setText(prescribedExerciseList.get(position).getName());
        if(prescribedExerciseList.get(position).getExType() == 1) {
            holder.exerciseType.setText(R.string.physio_schedule_item_sitting_up);
        }
        else {
            holder.exerciseType.setText(R.string.physio_schedule_item_lying_down);
        }
        holder.exerciseSet.setText(String.valueOf(prescribedExerciseList.get(position).getExSet()));
        holder.exerciseRepeat.setText(String.valueOf(prescribedExerciseList.get(position).getExRepeat()));

        return convertView;
    }


}
