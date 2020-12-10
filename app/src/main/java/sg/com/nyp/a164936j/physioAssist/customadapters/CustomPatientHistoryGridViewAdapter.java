package sg.com.nyp.a164936j.physioAssist.customadapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import sg.com.nyp.a164936j.physioAssist.configuration.Config;
import sg.com.nyp.a164936j.physioAssist.R;

public class CustomPatientHistoryGridViewAdapter extends BaseAdapter {

    private Context context;
    private final int noOfPetals;

    public CustomPatientHistoryGridViewAdapter(Context context, int noOfPetals) {
        this.context = context;
        this.noOfPetals = noOfPetals;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;

        if(convertView == null){
            gridView = inflater.inflate(R.layout.patient_progress_history_grid_item, null);

            ImageView imgPetal = gridView.findViewById(R.id.patient_progress_history_grid_item_image);

            Log.d(Config.TAG_CUSTOM_ADAPTER, "(CustomPatientHistoryGridViewAdapter) in getView");
            Log.d(Config.TAG_CUSTOM_ADAPTER, "(CustomPatientHistoryGridViewAdapter) " + noOfPetals);
            imgPetal.setBackground(context.getDrawable(R.drawable.petal));


        }else{
            gridView = convertView;
        }

        return gridView;
    }

    @Override
    public int getCount() {
        return noOfPetals;

    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}
