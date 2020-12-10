package sg.com.nyp.a164936j.physioAssist.models;

public class Patient {

    private String patientId;
    private int stayId;
    private int rank;
    private String codeName;
    private String language;
    private int point;

    public Patient(String patientId, String codeName, String language) {
        this.patientId = patientId;
        this.codeName = codeName;
        this.language = language;
    }

    public Patient(String patientId, int stayId, String codeName, int point) {
        this.patientId = patientId;
        this.stayId = stayId;
        this.codeName = codeName;
        this.point = point;
    }

    public int getStayId() {
        return stayId;
    }

    public void setStayId(int stayId) {
        this.stayId = stayId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getCodeName() {
        return codeName;
    }

    public void setCodeName(String codeName) {
        this.codeName = codeName;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }
}
