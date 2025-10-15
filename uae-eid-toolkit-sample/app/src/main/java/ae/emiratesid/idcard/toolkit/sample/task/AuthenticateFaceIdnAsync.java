package ae.emiratesid.idcard.toolkit.sample.task;

import android.os.AsyncTask;

import ae.emiratesid.idcard.toolkit.CardReader;
import ae.emiratesid.idcard.toolkit.Toolkit;
import ae.emiratesid.idcard.toolkit.ToolkitException;
import ae.emiratesid.idcard.toolkit.datamodel.CardPublicData;
import ae.emiratesid.idcard.toolkit.datamodel.ToolkitResponse;
import ae.emiratesid.idcard.toolkit.sample.ConnectionController;
import ae.emiratesid.idcard.toolkit.sample.Constants;
import ae.emiratesid.idcard.toolkit.sample.logger.Logger;

public class AuthenticateFaceIdnAsync extends AsyncTask<Void, Integer, Integer> {
    private Toolkit toolkit;
    private String idn;
    private String imageBase64;
    private int status;
    private String message;
    private AuthenticateFaceIdnAsync.AuthenticateFaceIdnListener listener;
    private CardPublicData publicData;

    public AuthenticateFaceIdnAsync(final String idn, final String imageBase64, AuthenticateFaceIdnAsync.AuthenticateFaceIdnListener listener) {
        this.idn = idn;
        this.imageBase64 = imageBase64;
        this.listener = listener;
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        try {
            toolkit = ConnectionController.getToolkit();
            if (toolkit == null) {
                Logger.e("Toolkit is not initialized.");
                return Constants.ERROR;
            }//

            publicData = toolkit.verifyFaceOnServerUsingID(idn, imageBase64);

            if (publicData == null) {
                message = "Response from Toolkit is null";
                status = Constants.ERROR;
                return status;
            }
            //call the function
            status = Constants.SUCCESS;
            return status;
        } catch (ToolkitException e) {
            status = (int) e.getCode();
            message = e.getMessage();
            Logger.e("Exception occurred :::" + e.getMessage() + " Status = " + status);
        }//catch()
        ConnectionController.closeConnection();
        return status;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        if (listener != null)
            listener.onFaceAuthenticationIdnCompleted(status, message, publicData);
    }

    public interface AuthenticateFaceIdnListener {
        void onFaceAuthenticationIdnCompleted(int status, String message, CardPublicData cardPublicData);
    }
}
