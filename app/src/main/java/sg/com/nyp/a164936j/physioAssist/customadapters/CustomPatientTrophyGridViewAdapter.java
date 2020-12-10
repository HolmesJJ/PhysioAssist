package sg.com.nyp.a164936j.physioAssist.customadapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import sg.com.nyp.a164936j.physioAssist.R;

public class CustomPatientTrophyGridViewAdapter extends BaseAdapter {

    private Context context;
    private final int trophyCollected;

    public CustomPatientTrophyGridViewAdapter(Context context, int mobileValues) {
        this.context = context;
        this.trophyCollected = mobileValues;
    }

    @Override
    public int getCount() {
        return trophyCollected;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;

        if(convertView == null){
            gridView = inflater.inflate(R.layout.patient_progress_trophy_grid_item, null);
            ImageView imgTrophy = gridView.findViewById(R.id.patient_progress_trophy_grid_item_image);
        }else{
            gridView = convertView;
        }

        return gridView;
    }
}
