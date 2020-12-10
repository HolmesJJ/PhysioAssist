package sg.com.nyp.a164936j.physioAssist.customadapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import sg.com.nyp.a164936j.physioAssist.R;
import sg.com.nyp.a164936j.physioAssist.models.Patient;

public class CustomSelectPatientSpinnerAdapter extends ArrayAdapter<Patient> {

    private LayoutInflater flater;

    public CustomSelectPatientSpinnerAdapter(@NonNull Activity context, int resource, @NonNull List<Patient> list) {
        super(context, resource, list);
        flater = context.getLayoutInflater();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Patient patient = getItem(position);

        View rowView = flater.inflate(R.layout.select_patient_spinner_item, null,true);

        TextView tv = rowView.findViewById(R.id.select_patient_codename);
        tv.setText(patient.getCodeName());

        return rowView;
    }

    @Override
    public boolean isEnabled(int position) {
        // Disable the first item "Please select patient codename"
        if (position == 0) {
            return false;
        }
        return true;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null){
            convertView = flater.inflate(R.layout.select_patient_spinner_item, parent, false);
        }
        Patient patient = getItem(position);
        TextView tv = convertView.findViewById(R.id.select_patient_codename);
        tv.setText(patient.getCodeName());

        return convertView;
    }
}
