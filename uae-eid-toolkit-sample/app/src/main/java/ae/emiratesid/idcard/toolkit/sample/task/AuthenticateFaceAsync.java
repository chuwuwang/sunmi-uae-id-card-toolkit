package ae.emiratesid.idcard.toolkit.sample.task;

import android.os.AsyncTask;

import ae.emiratesid.idcard.toolkit.CardReader;
import ae.emiratesid.idcard.toolkit.Toolkit;
import ae.emiratesid.idcard.toolkit.ToolkitException;
import ae.emiratesid.idcard.toolkit.datamodel.ToolkitResponse;
import ae.emiratesid.idcard.toolkit.sample.ConnectionController;
import ae.emiratesid.idcard.toolkit.sample.Constants;
import ae.emiratesid.idcard.toolkit.sample.logger.Logger;
import ae.emiratesid.idcard.toolkit.sample.utils.RequestGenerator;

public class AuthenticateFaceAsync extends AsyncTask<Void, Integer, Integer> {

    private CardReader cardReader;
    private String imageBase64;
    private int status;
    private String message;
    private AuthenticateFaceListener listener;

    public AuthenticateFaceAsync(final String imageBase64, AuthenticateFaceListener listener) {
        this.imageBase64 = imageBase64;
        this.listener = listener;
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        try {
            cardReader = ConnectionController.getConnection();
            if (cardReader == null) {
                Logger.e("EIDAToolkit is null;");
                return Constants.ERROR;
            }//

            ToolkitResponse response = cardReader.authenticateFaceOnServer(imageBase64);

            if (response == null) {
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
            listener.onFaceAuthenticationCompleted(status, message);
    }

    public interface AuthenticateFaceListener {
        void onFaceAuthenticationCompleted(int status, String message);
    }
}
