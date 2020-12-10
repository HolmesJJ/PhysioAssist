package sg.com.nyp.a164936j.physioAssist.models;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class VideoStatus {
    private int exerciseId;
    private String exerciseName;
    private int videoProgress = 0;
    private String downloadDemoStatus = "Ready"; // Ready, Downloading, Finish
    private String downloadCountingStatus = "Ready"; // Ready, Downloading, Finish
    private int parentId;
    private int position;

    public VideoStatus(int exerciseId, String exerciseName, int parentId, int position) {
        this.exerciseId = exerciseId;
        this.exerciseName = exerciseName;
        this.parentId = parentId;
        this.position = position;
    }

    public void setExerciseId(int exerciseId) {
        this.exerciseId = exerciseId;
    }

    public int getExerciseId() {
        return exerciseId;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public void setVideoProgress(int videoProgress) {
        this.videoProgress = videoProgress;
    }

    public int getVideoProgress() {
        return videoProgress;
    }

    public String getDownloadDemoStatus() {
        return downloadDemoStatus;
    }

    public void setDownloadDemoStatus(String downloadDemoStatus) {
        this.downloadDemoStatus = downloadDemoStatus;
    }

    public String getDownloadCountingStatus() {
        return downloadCountingStatus;
    }

    public void setDownloadCountingStatus(String downloadCountingStatus) {
        this.downloadCountingStatus = downloadCountingStatus;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public int getParentId() {
        return parentId;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
