package sg.com.nyp.a164936j.physioAssist.customadapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import sg.com.nyp.a164936j.physioAssist.configuration.Config;
import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.fragments.interfaces.CustomOnClickListener;

public class CustomPatientChallengeAdapter extends BaseAdapter {

    private Context context;
    private final int[] mobileValues;
    private final String[] mobileValueDesc;

    private CustomOnClickListener customListener;

    public CustomPatientChallengeAdapter(Context context, int[] mobileValues, String[] mobileValueDesc) {
        this.context = context;
        this.mobileValues = mobileValues;
        this. mobileValueDesc = mobileValueDesc;
    }

    public void setImgTrophyClick(CustomOnClickListener listener){
        this.customListener = listener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;

        if(convertView == null){
            gridView = inflater.inflate(R.layout.patient_challenge_grid_item, null);

            ImageView imgTrophy = gridView.findViewById(R.id.patient_challenge_grid_item_image);
            TextView tvTrophy = gridView.findViewById(R.id.patient_challenge_grid_item_desc);

            imgTrophy.setBackground(context.getDrawable(mobileValues[position]));
            tvTrophy.setText(mobileValueDesc[position]);

            Log.d(Config.TAG_BUTTON, "(CustomPatientChallengeAdapter) pos: "+ position);

            imgTrophy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    customListener.onTrophyImgClick(position);
                }
            });

        }else{
            gridView = convertView;
        }

        return gridView;
    }

    @Override
    public int getCount() {
        Log.d(Config.TAG_BUTTON, "(CustomPatientChallengeAdapter) mobileValues.length:"+mobileValues.length);
        return mobileValues.length;
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
