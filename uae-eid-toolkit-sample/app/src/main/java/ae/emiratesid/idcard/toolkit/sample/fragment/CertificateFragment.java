package ae.emiratesid.idcard.toolkit.sample.fragment;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.security.cert.Certificate;

import ae.emiratesid.idcard.toolkit.internal.ErrorCode;
import ae.emiratesid.idcard.toolkit.sample.Constants;
import ae.emiratesid.idcard.toolkit.sample.R;
import ae.emiratesid.idcard.toolkit.sample.logger.Logger;
import ae.emiratesid.idcard.toolkit.sample.task.ReadCertificateAsync;
import ae.emiratesid.idcard.toolkit.sample.widget.LogTextView;

import static ae.emiratesid.idcard.toolkit.sample.AppController.isReading;

public class CertificateFragment extends BaseFragment {


    public CertificateFragment() {
        // Required empty public constructor
    }

    //Using a formatted Custom  TextView  to show the result and logs;
    private LogTextView txtStatus;
    private Button btnRefresh;
    private EditText edtPin;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_certificate, container, false);
        txtStatus = (LogTextView) view.findViewById(R.id.txtReadData);
        // make the text view scrollable.
        txtStatus.setMovementMethod(new ScrollingMovementMethod());
        btnRefresh = (Button) view.findViewById(R.id.btn_refresh);
        edtPin = (EditText) view.findViewById(R.id.edtPin);
        btnRefresh.setOnClickListener(this);
        return view;
    }

    // create a listener
    private ReadCertificateAsync.ReadCertificateListener readCertificateListener =  new ReadCertificateAsync.ReadCertificateListener() {
        @Override
        public void onCertificateReadComplete(int status,String message , Certificate certificateAuth, Certificate certificateSign) {
            hideProgressDialog();
            isReading = false;
            if(status == Constants.SUCCESS){
                Logger.d("Auth certificate");
                if(certificateAuth != null){
                    txtStatus.appendLog("*********************AUTH CERTIFICATE*********\n" ,
                            LogTextView.LOG_TYPE.SUCCESS);
                    txtStatus.appendLog(certificateAuth.toString().trim() ,
                            LogTextView.LOG_TYPE.SUCCESS);
                }//
                Logger.d("Signed certificate");
                if(certificateSign != null){
                    txtStatus.appendLog("\n*********************SIGN CERTIFICATE*********\n" ,
                            LogTextView.LOG_TYPE.INFO);
                    txtStatus.appendLog(certificateSign.toString().trim() , LogTextView.LOG_TYPE.INFO);
                }//if()

            }//if()
            else{
                if(status != ErrorCode.UNSPECIFIED.getCode()) {
                    txtStatus.setLog("Error Code : " + status + "\nError in Reading Certificate. \n" + message,
                            LogTextView.LOG_TYPE.ERROR);
                }
                else
                {
                    txtStatus.setLog("Error in Reading Certificate. \n" + message,
                            LogTextView.LOG_TYPE.ERROR);
                }
            }//
        }//onCertificateReadComplete()
    };
    @Override
    public void onClick(View view) {
        if(!isReading){
            txtStatus.setText("");
            //set the reading flag..
            final String PIN =  edtPin.getText().toString();
            //validate for empty
            if(PIN.isEmpty()){
                txtStatus.setLog("PIN is Required" , LogTextView.LOG_TYPE.ERROR);
                return;
            }//if()

            isReading =  true;

            //show the dialog to provide user interaction...
            showProgressDialog("Reading certificates...");

            //create the object of ReaderCardDataAsync
            ReadCertificateAsync readCertificateAsync =  new ReadCertificateAsync(PIN ,
                    readCertificateListener);
            readCertificateAsync.execute();
            return;
        }//
        txtStatus.setLog("Reading already in process" , LogTextView.LOG_TYPE.ERROR);

    }//onClick()

    @Override
    public void onDetach() {
        super.onDetach();
        readCertificateListener = null;
    }//onDetach()

    @Override
    protected void appendError(String message) {
        txtStatus.appendLog(message , LogTextView.LOG_TYPE.ERROR);
    }
}
