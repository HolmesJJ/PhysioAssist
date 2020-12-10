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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.configuration.CustomSharedPreference;
import sg.com.nyp.a164936j.physioAssist.models.Exercise;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class CustomPhysioProgressBarGraphAdapter extends BaseAdapter {

    private Context context;
    private static LayoutInflater inflater = null;

    private List<String> dateList;
    private List<Exercise> selectedPerformedExercisesByDate = new ArrayList<>();
    private int itemHeight;
    private int itemWidth;

    public CustomPhysioProgressBarGraphAdapter(Context context, List<String> dateList, int itemHeight, int itemWidth) {
        this.context = context;
        this.dateList = dateList;
        this.itemHeight = itemHeight;
        this.itemWidth = itemWidth;

        inflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return dateList.size();
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
        TextView headerTxt;
        ListView subListView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if(convertView == null){
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.layout_physio_progress_bar_graph, null);
            holder.headerTxt = convertView.findViewById(R.id.physio_progress_bar_graph_header);
            holder.subListView = convertView.findViewById(R.id.physio_progress_bar_graph_item);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.headerTxt.setText(dateList.get(position));

        AbsListView.LayoutParams param = new AbsListView.LayoutParams(itemWidth, itemHeight/2-10);
        convertView.setLayoutParams(param);

        selectedPerformedExercisesByDate.clear();
        List<Exercise> selectedPerformedExercises = CustomSharedPreference.selectedPerformedExercises;

        for (int i = 0; i < selectedPerformedExercises.size(); i++) {
            Date startDate = selectedPerformedExercises.get(i).getStartTime();

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int date = calendar.get(Calendar.DATE);
            String dateTxt = year + "-" + month + "-" + date;

            if(dateList.get(position).equals(dateTxt)) {
                selectedPerformedExercisesByDate.add(selectedPerformedExercises.get(i));
            }
        }


        CustomPhysioProgressBarGraphItemAdapter adapter = new CustomPhysioProgressBarGraphItemAdapter(context, selectedPerformedExercisesByDate);
        holder.subListView.setAdapter(adapter);

        return convertView;
    }
}
