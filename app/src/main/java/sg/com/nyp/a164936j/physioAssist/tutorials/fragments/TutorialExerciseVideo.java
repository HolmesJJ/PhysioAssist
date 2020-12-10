package sg.com.nyp.a164936j.physioAssist.tutorials.fragments;


import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.configuration.GetSetSharedPreferences;
import sg.com.nyp.a164936j.physioAssist.customadapters.CustomTutorialExerciseAdapter;
import sg.com.nyp.a164936j.physioAssist.fragments.interfaces.CustomOnClickListener;
import sg.com.nyp.a164936j.physioAssist.tutorials.Tutorials;

/**
 * A simple {@link Fragment} subclass.
 */
public class TutorialExerciseVideo extends Fragment implements CustomOnClickListener {


    private Context context;
    private Resources resources;
    private ListView lv1, lv2;
    private CustomTutorialExerciseAdapter adapter;
    private TextView exerciseHeader;
    private TextView exerciseSubHeader;
    private TextView exerciseTypeHeader1;
    private TextView exerciseTypeHeader2;

    //vars
    private List<String> exerciseList1 = new ArrayList<>();
    private List<String> exerciseThumbnail1 = new ArrayList<>();
    private List<String> exerciseList2 = new ArrayList<>();
    private List<String> exerciseThumbnail2 = new ArrayList<>();

    private String language;

    public TutorialExerciseVideo() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_tutorial_exercise_video, container, false);
        initView(rootView);

        language = GetSetSharedPreferences.getDefaults("language", getActivity().getApplicationContext());

        Tutorials tutorials = (Tutorials) getActivity();
        resources = tutorials.updateViews(context, language);
        initLanguage();

        clearDummyData();
        generateDummyData();

        adapter = new CustomTutorialExerciseAdapter(context, exerciseList1, exerciseThumbnail1);
        adapter.setThumbnailClickListener(this);
        lv1.setAdapter(adapter);

        adapter = new CustomTutorialExerciseAdapter(context, exerciseList2, exerciseThumbnail2);
        adapter.setThumbnailClickListener(this);
        lv2.setAdapter(adapter);

        return  rootView;
    }

    private void initView(View rootView) {
        context = getContext();
        exerciseHeader = rootView.findViewById(R.id.patient_exercise_header);
        exerciseSubHeader = rootView.findViewById(R.id.patient_exercise_sub_header);
        exerciseTypeHeader1 = rootView.findViewById(R.id.patient_exercise_type_header1);
        exerciseTypeHeader2 = rootView.findViewById(R.id.patient_exercise_type_header2);
        lv1 = rootView.findViewById(R.id.patient_customListView1);
        lv2 = rootView.findViewById(R.id.patient_customListView2);
    }

    private void initLanguage() {
        exerciseHeader.setText(resources.getString(R.string.patient_exercise_header));
        exerciseSubHeader.setText(resources.getString(R.string.patient_exercise_sub_header));
        exerciseTypeHeader1.setText(resources.getString(R.string.patient_exercise_type_header1));
        exerciseTypeHeader2.setText(resources.getString(R.string.patient_exercise_type_header2));
    }

    private void clearDummyData() {
        exerciseThumbnail1.clear();
        exerciseList1.clear();
        exerciseThumbnail2.clear();
        exerciseList2.clear();
    }

    private void generateDummyData() {
        exerciseThumbnail1.add("ankle_pump_lying");
        exerciseThumbnail1.add("hip_side-slides_left");

        exerciseList1.add("Ankle Pump (lying)");
        exerciseList1.add("Hip Side-Slides (left)");

        exerciseThumbnail2.add("deep_breathing");
        exerciseThumbnail2.add("upper_limb_strengthen");

        exerciseList2.add("Deep Breathing");
        exerciseList2.add("Upper Limb Strengthen");
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
}
