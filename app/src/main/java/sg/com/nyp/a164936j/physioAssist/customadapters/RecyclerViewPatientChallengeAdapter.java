package sg.com.nyp.a164936j.physioAssist.customadapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import sg.com.nyp.a164936j.physioAssist.R;

public class RecyclerViewPatientChallengeAdapter extends RecyclerView.Adapter<RecyclerViewPatientChallengeAdapter.ViewHolder> {

    private String[][] twoDArray;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    //data is passed into the constructor
    public RecyclerViewPatientChallengeAdapter(Context context, String[][] twoDArray) {
        this.mInflater = LayoutInflater.from(context);
        this.twoDArray = twoDArray;
    }

    //inflate the cell layout from xm when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.patient_challenge_grid_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.grid_item_desc.setText(twoDArray[0][position]);

        if(twoDArray[1][position].equals("checked"))
            holder.patient_challenge_grid_item_image.setImageResource(R.drawable.trophy_checked);
    }

    @Override
    public int getItemCount() {
        return twoDArray[0].length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView grid_item_desc;
        ImageView patient_challenge_grid_item_image;

        ViewHolder(View itemView){
            super(itemView);
            grid_item_desc = itemView.findViewById(R.id.patient_challenge_grid_item_desc);
            patient_challenge_grid_item_image = itemView.findViewById(R.id.patient_challenge_grid_item_image);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    //convenience method for getting data at click position
    public String getItem(int id){
        return twoDArray[0][id];
    }

    // allow clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener){
        this.mClickListener = itemClickListener;
    }

    //parent activity will implement this method to respond to click events
    public interface ItemClickListener{
        void onItemClick(View view, int position);
    }
}
