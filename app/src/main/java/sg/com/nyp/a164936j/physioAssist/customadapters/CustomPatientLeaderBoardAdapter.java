package sg.com.nyp.a164936j.physioAssist.customadapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.models.Patient;

public class CustomPatientLeaderBoardAdapter extends BaseAdapter {

    private Context context;
    private Resources resources;
    private List<Patient> mPatients;

    private static LayoutInflater inflater = null;

    public CustomPatientLeaderBoardAdapter(Context context, Resources resources, List<Patient> mPatients) {
        this.context = context;
        this.resources = resources;
        this.mPatients = mPatients;

        inflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mPatients.size();
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
        RelativeLayout itemContain;
        TextView position, codeName, points;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        ViewHolder holder = new ViewHolder();
        final View rowView;

        rowView = inflater.inflate(R.layout.layout_patient_leaderboard, null);
        holder.itemContain = rowView.findViewById(R.id.item_contain);
        holder.position = rowView.findViewById(R.id.patient_leaderboard_position);
        holder.codeName = rowView.findViewById(R.id.patient_leaderboard_patient_codename);
        holder.points = rowView.findViewById(R.id.patient_leaderboard_points);

        holder.position.setText(resources.getString(R.string.patient_leaderboard_position, mPatients.get(position).getRank()));
        holder.codeName.setText(mPatients.get(position).getCodeName());
        holder.points.setText(String.valueOf(mPatients.get(position).getPoint()));

        return rowView;
    }
}
