package sg.com.nyp.a164936j.physioAssist.notch;

import android.app.Application;

public class NotchApplication extends Application {

    private static NotchApplication mInst;

    public static NotchApplication getInst() {
        return mInst;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInst = this;
    }
}
