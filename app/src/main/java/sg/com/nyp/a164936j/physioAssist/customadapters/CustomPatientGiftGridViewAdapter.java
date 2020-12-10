package sg.com.nyp.a164936j.physioAssist.customadapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.List;

import sg.com.nyp.a164936j.physioAssist.R;

public class CustomPatientGiftGridViewAdapter extends BaseAdapter {

    private Context context;
    private List<Integer> gifts;

    public CustomPatientGiftGridViewAdapter(Context context, List<Integer> gifts) {
        this.context = context;
        this.gifts = gifts;
    }

    @Override
    public int getCount() {
        return gifts.size();
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
            gridView = inflater.inflate(R.layout.patient_progress_gift_grid_item, null);
            ImageView imgPetal = gridView.findViewById(R.id.patient_progress_gift_grid_item_image);
            imgPetal.setBackground(context.getDrawable(gifts.get(position)));
        }
        else{
            gridView = convertView;
        }

        return gridView;
    }
}
