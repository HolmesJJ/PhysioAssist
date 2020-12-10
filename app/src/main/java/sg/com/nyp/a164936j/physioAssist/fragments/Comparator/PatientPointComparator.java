package sg.com.nyp.a164936j.physioAssist.fragments.Comparator;

import java.util.Comparator;

import sg.com.nyp.a164936j.physioAssist.models.Patient;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class PatientPointComparator implements Comparator<Patient> {
    @Override
    public int compare(Patient p1, Patient p2) {
        if (p1.getPoint() < p2.getPoint()) {
            return 1;
        } else if (p1.getPoint() > p2.getPoint()) {
            return -1;
        } else {
            return 0;
        }
    }
}
