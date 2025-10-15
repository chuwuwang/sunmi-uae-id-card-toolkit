package ae.emiratesid.idcard.toolkit.sample.fragment.authenticateface.view;

import android.content.Context;

public interface AuthenticateFaceView {
    void onContextNull();
    void onRequestIdNull();
    void onImageBase64Empty();
    void onSuccess(Context mContext, String requestID, int requestCode);
    void onFaceAuthenticationCompleted(String message);
    void showError(String message);
}
