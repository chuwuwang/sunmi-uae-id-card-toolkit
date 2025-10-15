package ae.emiratesid.idcard.toolkit.sample.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import ae.emiratesid.idcard.toolkit.internal.ErrorCode;
import ae.emiratesid.idcard.toolkit.sample.Constants;
import ae.emiratesid.idcard.toolkit.sample.R;
import ae.emiratesid.idcard.toolkit.sample.logger.Logger;
import ae.emiratesid.idcard.toolkit.sample.task.PKIAuthAsync;
import ae.emiratesid.idcard.toolkit.sample.widget.LogTextView;

import static ae.emiratesid.idcard.toolkit.sample.AppController.isReading;

public class PKIAuthFragment extends BaseFragment {


    public PKIAuthFragment() {
        // Required empty public constructor
    }

    @Override
    protected void appendError(String message) {
        txtStatus.appendLog(message , LogTextView.LOG_TYPE.ERROR);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    private LogTextView txtStatus;
    private Button btnResfersh;
    private EditText edtPin;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_pkiauth, container, false);
        txtStatus = (LogTextView) view.findViewById(R.id.txtReadData);
        edtPin = (EditText) view.findViewById(R.id.edtPin);
        btnResfersh = (Button) view.findViewById(R.id.btn_refresh);
        btnResfersh.setOnClickListener(this);
        return view;
    }

    //Create PKIAuthFragment
    private PKIAuthAsync pkiAuthAsync;

    //create
    PKIAuthAsync.PKIAuthListener pkiAuthListener =  new PKIAuthAsync.PKIAuthListener() {
        @Override
        public void onPKIAuth(int status ,String message,  String vgResponse) {

            isReading =  false;
            hideProgressDialog();
            Logger.d("PKI Auth status " + status);
            switch(status) {
                case Constants.SUCCESS:
                    txtStatus.setLog(message, LogTextView.LOG_TYPE.SUCCESS);
                    txtStatus.appendLog("\n VG Response ::" + vgResponse , LogTextView.LOG_TYPE.INFO);
                    break;
                default:
                    //invalid pin case and shoe the attempts.
                    if(status != ErrorCode.UNSPECIFIED.getCode()) {
                        txtStatus.setLog("Error Code : " + status +"\n" + message,
                                LogTextView.LOG_TYPE.ERROR);
                    }
                    else
                    {
                        txtStatus.setLog( message,
                                LogTextView.LOG_TYPE.ERROR);
                    }
                    break;
            }//switch()
        }//onPKIAuth
    };//
    @Override
    public void onClick(View view) {
        txtStatus.setText("");
        if(!isReading){
            final String PIN =  edtPin.getText().toString();
            //validate for empty
            if(PIN.isEmpty()){
                txtStatus.setLog("PIN is Required" , LogTextView.LOG_TYPE.ERROR);
                return;
            }//if()

            isReading =  true;
            showProgressDialog("Authenticating PKI....");
            //Execute the PKIAuthListener
            pkiAuthAsync =  new PKIAuthAsync( PIN , pkiAuthListener);
            pkiAuthAsync.execute();
        }//
    }//onClick()...
}
