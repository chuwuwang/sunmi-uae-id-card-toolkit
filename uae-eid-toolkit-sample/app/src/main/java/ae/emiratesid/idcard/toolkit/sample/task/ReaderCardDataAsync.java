package ae.emiratesid.idcard.toolkit.sample.task;

import static ae.emiratesid.idcard.toolkit.sample.ConnectionController.toolkit;

import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import ae.emiratesid.idcard.toolkit.CardReader;
import ae.emiratesid.idcard.toolkit.ToolkitException;
import ae.emiratesid.idcard.toolkit.datamodel.CardPublicData;
import ae.emiratesid.idcard.toolkit.sample.ConnectionController;
import ae.emiratesid.idcard.toolkit.sample.Constants;
import ae.emiratesid.idcard.toolkit.sample.logger.Logger;
import ae.emiratesid.idcard.toolkit.sample.utils.NFCCardParams;
import ae.emiratesid.idcard.toolkit.sample.utils.RequestGenerator;
import ae.emiratesid.idcard.toolkit.sample.utils.XmlUtils;
import sunmi.paylib.SunmiPayKernel;

public class ReaderCardDataAsync extends AsyncTask<Void, Integer, Integer> {

    private ReaderCardDataListener readerCardDataListener;
    private CardReader cardReader;
    private int status;
    private String message;
    private Tag tag;
    private String cardNumber, dob, expiryDate;

    public ReaderCardDataAsync(ReaderCardDataListener readerCardDataListener, Tag tag) {

        this.readerCardDataListener = readerCardDataListener;
        this.tag = tag;
        this.cardNumber = NFCCardParams.CARD_NUMBER;
        this.dob = NFCCardParams.DOB;
        this.expiryDate = NFCCardParams.EXPIRY_DATE;
    }

    public ReaderCardDataAsync(ReaderCardDataListener readerCardDataListener) {
        this.readerCardDataListener = readerCardDataListener;
        this.cardNumber = NFCCardParams.CARD_NUMBER;
        this.dob = NFCCardParams.DOB;
        this.expiryDate = NFCCardParams.EXPIRY_DATE;
    }

    private CardPublicData publicData;

    @Override
    protected Integer doInBackground(Void... voids) {

        boolean bReadNonModifiableData = true;
        boolean bReadModifiableData = true;
        boolean bReadPhotography = true;
        boolean bSignatureImage = true;
        boolean bReadAddress = true;
        try {
//            tag = mockTag();
            Logger.e("ReaderCardDataAsync:" + tag);
            if (tag != null) {
                cardReader = ConnectionController.initConnection(tag);
                Logger.e("cardNumber:" + cardNumber);
                Logger.e("dob:" + dob);
                Logger.e("expiryDate:" + expiryDate);
                ConnectionController.setNFCParams(cardNumber, dob, expiryDate);
                Logger.e("setNFCParams end");
            } else {
//                Logger.e("cardNumber:" + cardNumber);
//                Logger.e("dob:" + dob);
//                Logger.e("expiryDate:" + expiryDate);
//                ConnectionController.setNFCParams(cardNumber, dob, expiryDate);
//                cardReader = ConnectionController.initConnection();
//                Logger.e("mock setNfcMode:" + mockTag());
//                toolkit.setNfcMode(mockTag());
                cardReader = ConnectionController.initConnection();
//                Logger.e("cardNumber:" + cardNumber);
//                Logger.e("dob:" + dob);
//                Logger.e("expiryDate:" + expiryDate);
//                ConnectionController.setNFCParams(cardNumber, dob, expiryDate);
//                Logger.e("setNFCParams end");
            }

            if (cardReader == null) {
                Logger.e("EIDAToolkit is null;");
                return Constants.ERROR;
            }//

            /**
             * Read the public data from card
             * Method will throw error if unable to read data from card
             */

            String requestID = RequestGenerator.generateRequestID();
            Logger.e("requestID:" + requestID);
            publicData = cardReader.readPublicData(requestID,
                    bReadNonModifiableData,
                    bReadModifiableData,
                    bReadPhotography,
                    bSignatureImage,
                    bReadAddress);
            Logger.e("publicData:" + publicData);
            if (publicData == null) {
                Logger.e("Public data is null");
                message = "Public data is null";
                return Constants.ERROR;
            }//
            XmlUtils.validateXML(publicData.toXmlString(), requestID);
            status = Constants.SUCCESS;
            //if()
        } catch (ToolkitException e) {
            status = (int) e.getCode();
            Logger.d("Status Code : ReaderCardDataAsync ::" + status);
            message = e.getMessage();
            Logger.e("Attempts left " + e.getAttemptsLeft());
            Logger.e("Error in reading...." + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
            message = "Unknown error";
        }// catch()
        return status;
    }//

    public Tag mockTag() {
        byte[] mId = new byte[]{5, -120, -47, 102, -17, 82, 0};
        int[] techList = new int[]{3, 1};
        Bundle[] techListExtras = new Bundle[2];
        techListExtras[0] = new Bundle();
        techListExtras[0].putByteArray("histbytes", new byte[]{-1, -1, 0, 0, 48, 80, 35, 0, 0});
        techListExtras[1] = new Bundle();
        techListExtras[1].putShort("sak", (short) 32);
        techListExtras[1].putByteArray("atqa", new byte[]{68, 0});
        Method method = null;
        try {
            Logger.e("method list: " + Arrays.toString(Tag.class.getDeclaredMethods()));
            method = Tag.class.getDeclaredMethod("createMockTag", byte[].class, int[].class, Bundle[].class);

            Logger.e("method " + method);
            Tag value = (Tag) (method.invoke(Tag.class, mId, techList, techListExtras));
            Logger.e("mock Tag " + value);
            return value;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        // call the listener to notify the caller that async has finished.
        // There you can handle all the ui related stuffs.
        readerCardDataListener.onCardReadComplete(this.status, message, publicData);
    }//onPostExecute()
}//end-of-class
