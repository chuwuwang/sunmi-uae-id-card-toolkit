package ae.emiratesid.idcard.toolkit.sample.fragment.authenticateface.presenter;

import android.content.Context;

import ae.emiratesid.idcard.toolkit.sample.Constants;
import ae.emiratesid.idcard.toolkit.sample.fragment.authenticateface.view.AuthenticateFaceView;
import ae.emiratesid.idcard.toolkit.sample.task.AuthenticateFaceAsync;
import ae.emiratesid.idcard.toolkit.sample.task.RegisterDeviceAsync;

public class AuthenticateFacePresenter {

    private AuthenticateFaceAsync task;

    private AuthenticateFaceView view;

    public AuthenticateFacePresenter(AuthenticateFaceView view) {
        this.view = view;
    }

    public void openAuthenticateFace(Context mContext, String requestID, int requestCode) {
        if (mContext == null) {
            view.onContextNull();
        } else if (requestID == null || requestID.equals("")) {
            view.onRequestIdNull();
        } else {
            view.onSuccess(mContext, requestID, requestCode);
        }
    }

    public void authenticateFace(String imageBase64) {
        if (imageBase64 == null || imageBase64.equals("")) {
            view.onImageBase64Empty();
        } else {
            task = new AuthenticateFaceAsync(imageBase64, new AuthenticateFaceAsync.AuthenticateFaceListener() {
                @Override
                public void onFaceAuthenticationCompleted(int status, String message) {

                    if (status == Constants.SUCCESS) {
                        cleanUp();
                        view.onFaceAuthenticationCompleted("Face Authentication successful");

                    } else {
                        cleanUp();
                        view.onFaceAuthenticationCompleted("Face Authentication failed.\n" + message);
                    }
                }
            });
            task.execute();
        }
    }

    private void cleanUp()
    {
        if (task != null && !task.isCancelled()){
            task.cancel(true);
        }
    }
}
