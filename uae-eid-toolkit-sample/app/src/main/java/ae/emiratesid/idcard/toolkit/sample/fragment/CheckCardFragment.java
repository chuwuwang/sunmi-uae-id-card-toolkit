package ae.emiratesid.idcard.toolkit.sample.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ae.emiratesid.idcard.toolkit.internal.ErrorCode;
import ae.emiratesid.idcard.toolkit.sample.Constants;
import ae.emiratesid.idcard.toolkit.sample.R;
import ae.emiratesid.idcard.toolkit.sample.logger.Logger;
import ae.emiratesid.idcard.toolkit.sample.task.CheckCardStatusAsync;
import ae.emiratesid.idcard.toolkit.sample.widget.LogTextView;

import static ae.emiratesid.idcard.toolkit.sample.AppController.isReading;

public class CheckCardFragment extends BaseFragment implements View.OnClickListener{

    public CheckCardFragment() {
        // Required empty public constructor
    }

    private LogTextView txtStatus;
    private Button btnCheckCardStatus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_check_card  , container, false);
        txtStatus = (LogTextView) view.findViewById(R.id.txtReadData);
        // make the text view scrollable.
        txtStatus.setMovementMethod(new ScrollingMovementMethod());
        btnCheckCardStatus = (Button) view.findViewById(R.id.btn_refresh);
        btnCheckCardStatus.setOnClickListener(this);
        return view;
    }
    //Create  a listener object for card status.
    private CheckCardStatusAsync.CheckCardStatusListener checkCardStatusListener =
            new CheckCardStatusAsync.CheckCardStatusListener() {
                @Override
                public void onCheckCardStatus(int status , String message , String xmlString) {
                    hideProgressDialog();
                    isReading = false;
                    Logger.d("CheckCardStatusAsync::onCheckCardStatus() Status = "+ status);
                    if(status == Constants.SUCCESS){
                        txtStatus.setLog( message , LogTextView.LOG_TYPE.SUCCESS);
                        txtStatus.appendLog("\n" + xmlString ,
                                LogTextView.LOG_TYPE.INFO);
                    }//if()
                    else{

                        if (status != ErrorCode.UNSPECIFIED.getCode()) {
                            txtStatus.setLog("Error Code : " + status + "\n" + message,
                                    LogTextView.LOG_TYPE.ERROR);
                        } else {
                            txtStatus.setLog(message,
                                    LogTextView.LOG_TYPE.ERROR);
                        }
                    }//else

                }//onCheckCardStatus
            };//checkCardStatusListener


    //Create a AsyncTask
    private CheckCardStatusAsync checkCardStatusAsync;
    @Override
    public void onClick(View view) {
        if( view == btnCheckCardStatus){
            if(!isReading){
                txtStatus.setText("");
                //set the reading flag..
                isReading =  true;

                //show the dialog to provide user interaction...
                showProgressDialog("Checking card status with server...");

                //create the object of ReaderCardDataAsync
                checkCardStatusAsync  = new CheckCardStatusAsync(checkCardStatusListener);
                checkCardStatusAsync.execute();

            }//
            else{
                txtStatus.setLog( "Reading in process" , LogTextView.LOG_TYPE.ERROR);

            }//else
        }//
    }//

    @Override
    public void onDetach() {
        super.onDetach();

        //set isReading false.
        isReading = false;

        //stop the AsyncTask is running.
        if(checkCardStatusAsync != null &&
                checkCardStatusAsync.getStatus() == AsyncTask.Status.RUNNING ){
            // cancel the async task
            try {
                checkCardStatusAsync.cancel(true);
            }//
            catch(Exception e){
            }//catch()
        }//
        checkCardStatusAsync = null;
        checkCardStatusListener = null;
    }//onDetach()

    @Override
    protected void appendError(String message) {
        txtStatus.appendLog(message , LogTextView.LOG_TYPE.ERROR);
    }
}//**end of class**
