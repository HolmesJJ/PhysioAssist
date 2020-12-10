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
import sg.com.nyp.a164936j.physioAssist.fragments.interfaces.CustomOnClickListener;

public class CustomPatientLeaderBoardSendGiftAdapter extends BaseAdapter {

    private Context context;
    private final int[] mobileValues;

    private CustomOnClickListener customListener;

    private View previousView;
    private int prevId,currId;

    public void setBtnSendGiftClick(CustomOnClickListener listener){
        this.customListener = listener;
    }

    public CustomPatientLeaderBoardSendGiftAdapter(Context context, int[] mobileValues) {
        this.context = context;
        this.mobileValues = mobileValues;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;

        if(convertView == null){
            gridView = inflater.inflate(R.layout.send_gift_grid_item, null);

            final ImageView imgAchievement = gridView.findViewById(R.id.send_gift_grid_item_image);

            imgAchievement.setBackgroundResource(mobileValues[position]);

            imgAchievement.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO: on selected, change image to achievement with tick, Need to implement select & de-select feature

                    currId = mobileValues[position];
                    switch (mobileValues[position]){
                        case R.drawable.achievement_well_done:
                            customListener.onAchievementImgClick(position);
                            imgAchievement.setBackgroundResource(R.drawable.achievement_well_done_checked);

                            if (previousView!= null){
                                if(prevId != currId){
                                    // set the previous view back
                                    previousView.setBackgroundResource(prevId);
                                }

                            }

                            Log.d(Config.TAG_BUTTON,"(CustomPatientLeaderBoardSendGiftAdapter.java) well_done checked");
                            break;
                        case R.drawable.achievement_good_job:
                            customListener.onAchievementImgClick(position);
                            imgAchievement.setBackgroundResource(R.drawable.achievement_good_job_checked);

                            if (previousView!= null && prevId!=0){
                                if(prevId != currId){
                                    // set the previous view back
                                    previousView.setBackgroundResource(prevId);
                                }
                            }

                            Log.d(Config.TAG_BUTTON,"(CustomPatientLeaderBoardSendGiftAdapter.java) Good_job checked");
                            break;
                        case R.drawable.achievement_nice:
                            customListener.onAchievementImgClick(position);
                            imgAchievement.setBackgroundResource(R.drawable.achievement_nice_checked);

                            if (previousView!= null && prevId!=0){
                                if(prevId != currId){
                                    // set the previous view back
                                    previousView.setBackgroundResource(prevId);
                                }
                            }

                            Log.d(Config.TAG_BUTTON,"(CustomPatientLeaderBoardSendGiftAdapter.java) Nice checked");
                            break;
                        case R.drawable.achievement_great:
                            customListener.onAchievementImgClick(position);
                            imgAchievement.setBackgroundResource(R.drawable.achievement_great_checked);

                            if (previousView!= null && prevId!=0){
                                if(prevId != currId){
                                    // set the previous view back
                                    previousView.setBackgroundResource(prevId);
                                }
                            }

                            Log.d(Config.TAG_BUTTON,"(CustomPatientLeaderBoardSendGiftAdapter.java) Great checked");

                            break;
                    }

                    prevId = mobileValues[position];
                    previousView = view;
                }
            });



        }else{
            gridView = convertView;
        }

        return gridView;
    }

    @Override
    public int getCount() {
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
