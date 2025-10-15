package ae.emiratesid.idcard.toolkit.sample.task;

import android.os.AsyncTask;

import ae.emiratesid.idcard.toolkit.Toolkit;
import ae.emiratesid.idcard.toolkit.ToolkitException;
import ae.emiratesid.idcard.toolkit.datamodel.CardPublicData;
import ae.emiratesid.idcard.toolkit.sample.ConnectionController;
import ae.emiratesid.idcard.toolkit.sample.Constants;
import ae.emiratesid.idcard.toolkit.sample.logger.Logger;

public class AuthenticateFacePassportAsync extends AsyncTask<Void, Integer, Integer> {
    private Toolkit toolkit;
    private String passportNumber;
    private String passportCountry;
    private String passportExpiry;
    private String passportDateOfBirth;
    private String imageBase64;
    private int status;
    private String message;
    private AuthenticateFacePassportAsync.AuthenticateFacePassportListener listener;
    private CardPublicData publicData;

    public AuthenticateFacePassportAsync(final String passportNumber,
                                         final String passportCountry,
                                         final String passportExpiry,
                                         final String passportDateOfBirth,
                                         final String imageBase64, AuthenticateFacePassportAsync.AuthenticateFacePassportListener listener) {
        this.passportNumber = passportNumber;
        this.passportCountry = passportCountry;
        this.passportExpiry = passportExpiry;
        this.passportDateOfBirth = passportDateOfBirth;
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

            publicData = toolkit.verifyFaceOnServerUsingPassport(passportNumber, passportCountry, passportExpiry, passportDateOfBirth, imageBase64);

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
            listener.onFaceAuthenticationPassportCompleted(status, message, publicData);
    }

    public interface AuthenticateFacePassportListener {
        void onFaceAuthenticationPassportCompleted(int status, String message, CardPublicData cardPublicData);
    }
}
