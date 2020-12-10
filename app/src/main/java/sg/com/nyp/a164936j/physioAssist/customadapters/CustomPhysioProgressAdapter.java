package sg.com.nyp.a164936j.physioAssist.customadapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.models.Exercise;

public class CustomPhysioProgressAdapter extends BaseAdapter {

    private Context context;
    private ListView listView;
    private List<Exercise> listExercises;
    private Map<Integer, Boolean> map = new HashMap<>();

    private static LayoutInflater inflater = null;

    public CustomPhysioProgressAdapter(Context context, List<Exercise> listExercises) {
        this.context = context;
        this.listExercises = listExercises;

        inflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return listExercises.size();
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
        CheckBox ckBoxExTitle;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        ViewHolder holder;

        if (listView == null) {
            listView = (ListView) parent;
        }

        if(convertView == null){
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.layout_physio_progress_video_check_item, null);
            holder.ckBoxExTitle = convertView.findViewById(R.id.physio_progress_checkbox);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.ckBoxExTitle.setText(listExercises.get(position).getName());
        holder.ckBoxExTitle.setOnCheckedChangeListener((CompoundButton compoundButton, boolean isChecked)-> {

            setBorderStyle(listView);

            if(isChecked){
                map.clear();
                map.put(position, true);
            }else{
                map.remove(position);
            }
        });

        if(map != null && map.containsKey(position)) {
            holder.ckBoxExTitle.setChecked(true);
            listExercises.get(position).setSelectedExercise(true);
        } else {
            holder.ckBoxExTitle.setChecked(false);
            listExercises.get(position).setSelectedExercise(false);
        }

        return convertView;
    }

    private void setBorderStyle(ListView listView) {
        listView.setBackground(ContextCompat.getDrawable(context, R.drawable.fragment_layout_round_white3));
        float density = context.getResources().getDisplayMetrics().density;
        int paddingPixel = (int)(20 * density);
        listView.setPadding(paddingPixel, paddingPixel, paddingPixel, paddingPixel);
    }
}
