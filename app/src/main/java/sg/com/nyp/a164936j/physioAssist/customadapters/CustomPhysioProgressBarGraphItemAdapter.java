package sg.com.nyp.a164936j.physioAssist.customadapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.models.Exercise;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class CustomPhysioProgressBarGraphItemAdapter extends BaseAdapter {


    private Context context;
    private static LayoutInflater inflater = null;

    private List<Exercise> selectedPerformedExercises;

    public CustomPhysioProgressBarGraphItemAdapter(Context context, List<Exercise> selectedPerformedExercises) {
        this.context = context;
        this.selectedPerformedExercises = selectedPerformedExercises;

        inflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return selectedPerformedExercises.size();
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
        TextView startTimeTxt;
        TextView endTimeTxt;
        TextView avgAngleTxt;
        TextView avgHoldDurationTxt;
        Button playBackBtn;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if(convertView == null){
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.layout_physio_progress_bar_graph_item, null);
            holder.startTimeTxt = convertView.findViewById(R.id.startTimeTxt);
            holder.endTimeTxt = convertView.findViewById(R.id.endTimeTxt);
            holder.avgAngleTxt = convertView.findViewById(R.id.avgAngleTxt);
            holder.avgHoldDurationTxt = convertView.findViewById(R.id.avgHoldDurationTxt);
            holder.playBackBtn = convertView.findViewById(R.id.playBackBtn);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        Calendar calendar = Calendar.getInstance();

        Date startDate = selectedPerformedExercises.get(position).getStartTime();
        calendar.setTime(startDate);
        int startDateHour = calendar.get(Calendar.HOUR_OF_DAY);
        int startDateMinute = calendar.get(Calendar.MINUTE);
        String startTimeTxt = startDateHour + ":" + startDateMinute;

        Date endDate = selectedPerformedExercises.get(position).getEndTime();
        calendar.setTime(endDate);
        int endDateHour = calendar.get(Calendar.HOUR_OF_DAY);
        int endDateMinute = calendar.get(Calendar.MINUTE);
        String endTimeTxt = endDateHour + ":" + endDateMinute;

        holder.startTimeTxt.setText(startTimeTxt);
        holder.endTimeTxt.setText(endTimeTxt);
        holder.avgAngleTxt.setText(String.valueOf(selectedPerformedExercises.get(position).getAvgAngle()));
        holder.avgHoldDurationTxt.setText(String.valueOf(selectedPerformedExercises.get(position).getAvgHoldDuration()));

        holder.playBackBtn.setOnClickListener((View view)-> {
            Toast.makeText(context, "6666", Toast.LENGTH_SHORT).show();
        });

        return convertView;
    }
}
