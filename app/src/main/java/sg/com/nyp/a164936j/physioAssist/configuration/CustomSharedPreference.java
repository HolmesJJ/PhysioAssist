package sg.com.nyp.a164936j.physioAssist.configuration;

import java.util.ArrayList;
import java.util.List;

import sg.com.nyp.a164936j.physioAssist.models.Exercise;
import sg.com.nyp.a164936j.physioAssist.models.VideoStatus;

public class CustomSharedPreference {
    // Patient Exercise
    public static VideoStatus videosStatus = null;
    // Patient Progress
    public static List <Exercise> performedPrescribedExercises  = new ArrayList<>();
    // Physio Progress
    public static List <Exercise> selectedPerformedExercises  = new ArrayList<>();
    // Physio Schedule
    public static List <Exercise> prescribedExercises  = new ArrayList<>();
}
