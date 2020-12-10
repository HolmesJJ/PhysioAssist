package sg.com.nyp.a164936j.physioAssist.tutorials.fragments;


import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.configuration.Config;
import sg.com.nyp.a164936j.physioAssist.configuration.GetSetSharedPreferences;
import sg.com.nyp.a164936j.physioAssist.customadapters.RecyclerViewPatientChallengeAdapter;
import sg.com.nyp.a164936j.physioAssist.fragments.interfaces.CustomOnClickListener;
import sg.com.nyp.a164936j.physioAssist.tutorials.Tutorials;

/**
 * A simple {@link Fragment} subclass.
 */
public class TutorialChallengesTrophy extends Fragment implements CustomOnClickListener, RecyclerViewPatientChallengeAdapter.ItemClickListener{

    private Context context;
    private Resources resources;
    private RecyclerView recyclerView;
    private RecyclerViewPatientChallengeAdapter adapter;
    private TextView challengeHeader;
    private TextView textChallengesItem;
    private Button btnClose;

    private String language;

    //vars
    private String[][] twoDArray;

    public TutorialChallengesTrophy() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_tutorial_challenges_trophy, container, false);
        initView(rootView);

        language = GetSetSharedPreferences.getDefaults("language", getActivity().getApplicationContext());

        Tutorials tutorials = (Tutorials) getActivity();
        resources = tutorials.updateViews(context, language);
        initLanguage();

        //generate dummy data
        generateDummyData();

        //Set up the RecyclerView
        int numOfColumn = 5;
        recyclerView.setLayoutManager(new GridLayoutManager(context,numOfColumn));
        adapter = new RecyclerViewPatientChallengeAdapter(getContext(), twoDArray);

        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        btnClose.setOnClickListener((View view)-> {
            getActivity().finish();
        });

        return rootView;
    }

    private void initView(View rootView) {
        context = getContext();
        recyclerView = rootView.findViewById(R.id.grid_recyclerView);
        challengeHeader = rootView.findViewById(R.id.patient_grid_codename_header);
        textChallengesItem = rootView.findViewById(R.id.patient_tutorial_text_challenges_item);
        btnClose = rootView.findViewById(R.id.patient_tutorial_btn_close);
    }

    private void initLanguage() {
        challengeHeader.setText(resources.getString(R.string.patient_grid_codename_header));
        textChallengesItem.setText(resources.getString(R.string.patient_tutorial_text_challenges_item));
    }

    public void generateDummyData(){

        twoDArray = new String[][]{
                {
                        "Challenge_Desc_1",
                        "Challenge_Desc_2",
                        "Challenge_Desc_3",
                        "Challenge_Desc_4",
                        "Challenge_Desc_5",

                        "Challenge_Desc_6",
                        "Challenge_Desc_7",
                        "Challenge_Desc_8",
                        "Challenge_Desc_9",
                        "Challenge_Desc_10",
                },
                {
                        "checked",
                        "checked",
                        "uncheck",
                        "checked",
                        "checked",

                        "checked",
                        "uncheck",
                        "checked",
                        "checked",
                        "checked",
                }
        };

    }

    @Override
    public void onThumbnailClick(int parentId, int position) {

    }

    @Override
    public void onStartExerciseClick(int parentId, int position) {

    }

    @Override
    public void onAchievementImgClick(int position) {

    }

    @Override
    public void onTrophyImgClick(int position) {

    }

    @Override
    public void onItemClick(View view, int position) {
        Log.d(Config.TAG_BUTTON,"(TutorialChallengesTrophy) Recycler View pos:" + adapter.getItem(position));
        onTrophyImgClick(position);
    }

}
