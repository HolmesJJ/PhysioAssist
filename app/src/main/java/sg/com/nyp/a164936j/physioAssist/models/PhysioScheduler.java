package sg.com.nyp.a164936j.physioAssist.models;

public class PhysioScheduler {

    String timeList = "";
    String[] videoList = new String[]{};

    public String getTimeList() {
        return timeList;
    }

    public void setTimeList(String timeList) {
        this.timeList = timeList;
    }

    public String[] getVideoList() {
        return videoList;
    }

    public void setVideoList(String[] videoList) {
        this.videoList = videoList;
    }
}
