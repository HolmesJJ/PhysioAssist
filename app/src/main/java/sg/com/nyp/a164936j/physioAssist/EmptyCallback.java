package sg.com.nyp.a164936j.physioAssist;

import com.wearnotch.service.common.NotchCallback;
import com.wearnotch.service.common.NotchError;
import com.wearnotch.service.common.NotchProgress;

import sg.com.nyp.a164936j.physioAssist.notch.util.Util;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class EmptyCallback<T> implements NotchCallback<T> {

    @Override
    public void onProgress(NotchProgress notchProgress) {
        System.out.println("onProgress");
    }

    @Override
    public void onSuccess(T t) {
        System.out.println("onSuccess");
    }

    @Override
    public void onFailure(NotchError notchError) {
        System.out.println("onFailure");
    }

    @Override
    public void onCancelled() {
        System.out.println("onCancelled");
    }
}

