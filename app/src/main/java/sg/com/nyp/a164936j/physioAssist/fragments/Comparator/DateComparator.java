package sg.com.nyp.a164936j.physioAssist.fragments.Comparator;

import java.util.Comparator;

import sg.com.nyp.a164936j.physioAssist.models.Exercise;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class DateComparator implements Comparator<Exercise> {
    @Override
    public int compare(Exercise e1, Exercise e2) {
        if (e1.getStartTime().getTime() > e2.getStartTime().getTime()) {
            return 1;
        } else if (e1.getStartTime().getTime() < e2.getStartTime().getTime()) {
            return -1;
        } else {
            return 0;
        }
    }
}
