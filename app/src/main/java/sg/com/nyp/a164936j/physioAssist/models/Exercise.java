package sg.com.nyp.a164936j.physioAssist.models;

import java.util.Date;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class Exercise {

    private int exerciseId;
    private int performExId;
    private int prescribeExId;
    private int exType;
    private String name;
    private String exTime;
    private int exSet;
    private int exRepeat;
    private boolean selectedExercise;
    private Date startTime;
    private Date endTime;
    private int avgAngle;
    private int avgHoldDuration;
    private int exTimePerDay;
    private int completedTimes;
    private int score;

    public Exercise(int exerciseId, String name) {
        this.exerciseId = exerciseId;
        this.name = name;
    }

    public Exercise(int prescribeExId, String name, int completedTimes, int exTimePerDay) {
        this.prescribeExId = prescribeExId;
        this.name = name;
        this.completedTimes = completedTimes;
        this.exTimePerDay = exTimePerDay;
    }

    public Exercise(int prescribeExId, String name, String exTime, int completedTimes, int exTimePerDay) {
        this.prescribeExId = prescribeExId;
        this.name = name;
        this.exTime = exTime;
        this.completedTimes = completedTimes;
        this.exTimePerDay = exTimePerDay;

    }

    public Exercise(int performExId, int prescribeExId, String name, Date startTime, Date endTime, int avgAngle, int avgHoldDuration, int score) {
        this.performExId = performExId;
        this.prescribeExId = prescribeExId;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.avgAngle = avgAngle;
        this.avgHoldDuration = avgHoldDuration;
        this.score = score;
    }

    public Exercise(int prescribeExId, String name, boolean selectedExercise) {
        this.prescribeExId = prescribeExId;
        this.name = name;
        this.selectedExercise = selectedExercise;
    }

    public Exercise(int prescribeExId, int exType, String name, String exTime, int exSet, int exRepeat) {
        this.prescribeExId = prescribeExId;
        this.exType = exType;
        this.name = name;
        this.exTime = exTime;
        this.exSet = exSet;
        this.exRepeat = exRepeat;
    }

    public int getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(int exerciseId) {
        this.exerciseId = exerciseId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getPerformExId() {
        return performExId;
    }

    public void setPerformExId(int performExId) {
        this.performExId = performExId;
    }

    public int getPrescribeExId() {
        return prescribeExId;
    }

    public void setPrescribeExId(int prescribeExId) {
        this.prescribeExId = prescribeExId;
    }

    public int getExType() {
        return exType;
    }

    public void setExType(int exType) {
        this.exType = exType;
    }


    public String getName() {
        return name;
    }

    public String getExTime() {
        return exTime;
    }

    public void setExTime(String exTime) {
        this.exTime = exTime;
    }

    public int getExSet() {
        return exSet;
    }

    public void setExSet(int exSet) {
        this.exSet = exSet;
    }

    public int getExRepeat() {
        return exRepeat;
    }

    public void setExRepeat(int exRepeat) {
        this.exRepeat = exRepeat;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelectedExercise() {
        return selectedExercise;
    }

    public void setSelectedExercise(boolean selectedExercise) {
        this.selectedExercise = selectedExercise;
    }

    public int getAvgAngle() {
        return avgAngle;
    }

    public void setAvgAngle(int avgAngle) {
        this.avgAngle = avgAngle;
    }

    public int getAvgHoldDuration() {
        return avgHoldDuration;
    }

    public void setAvgHoldDuration(int avgHoldDuration) {
        this.avgHoldDuration = avgHoldDuration;
    }

    public int getExTimePerDay() {
        return exTimePerDay;
    }

    public void setExTimePerDay(int exTimePerDay) {
        this.exTimePerDay = exTimePerDay;
    }

    public int getCompletedTimes() {
        return completedTimes;
    }

    public void setCompletedTimes(int completedTimes) {
        this.completedTimes = completedTimes;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
