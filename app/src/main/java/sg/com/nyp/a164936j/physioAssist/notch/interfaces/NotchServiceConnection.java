package sg.com.nyp.a164936j.physioAssist.notch.interfaces;

import com.wearnotch.service.network.NotchService;

public interface NotchServiceConnection {
    void onServiceConnected(NotchService notchService);
    void onServiceDisconnected();
}
