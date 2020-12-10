package sg.com.nyp.a164936j.physioAssist.models;

public class ExerciseRoutine {

    private String vidURL;
    private String title;

    public ExerciseRoutine(String vidURL, String title) {
        this.vidURL = vidURL;
        this.title = title;
    }

    public String getVidURL() {
        return vidURL;
    }

    public void setVidURL(String vidURL) {
        this.vidURL = vidURL;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
