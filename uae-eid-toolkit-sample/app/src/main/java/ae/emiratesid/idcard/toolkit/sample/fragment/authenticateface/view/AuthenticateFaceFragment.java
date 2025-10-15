package ae.emiratesid.idcard.toolkit.sample.fragment.authenticateface.view;

import static ae.emiratesid.idcard.toolkit.sample.AppController.isReading;

import android.content.Context;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import ae.emiratesid.idcard.toolkit.CardReader;
import ae.emiratesid.idcard.toolkit.Toolkit;
import ae.emiratesid.idcard.toolkit.ToolkitException;
import ae.emiratesid.idcard.toolkit.internal.ErrorCode;
import ae.emiratesid.idcard.toolkit.sample.ConnectionController;
import ae.emiratesid.idcard.toolkit.sample.Constants;
import ae.emiratesid.idcard.toolkit.sample.R;
import ae.emiratesid.idcard.toolkit.sample.fragment.BaseFragment;
import ae.emiratesid.idcard.toolkit.sample.fragment.authenticateface.presenter.AuthenticateFacePresenter;
import ae.emiratesid.idcard.toolkit.sample.logger.Logger;
import ae.emiratesid.idcard.toolkit.sample.task.NfcReaderAsync;
import ae.emiratesid.idcard.toolkit.sample.task.ReaderConnectionTask;
import ae.emiratesid.idcard.toolkit.sample.task.ValidateCardCaptureFaceAsync;

public class AuthenticateFaceFragment extends BaseFragment implements AuthenticateFaceView {
    private Context mContext;
    private Button btnAuthenticate;
    private AuthenticateFacePresenter presenter;
    private int requestCode;
    /////////////////////////////////////
    private static boolean isNFCMode;
    private Tag tag;
    private TextView textViewStatus;
    private NfcReaderAsync nfcReaderAsync;
    private ValidateCardCaptureFaceAsync task;


    public AuthenticateFaceFragment() {
        // Required empty public constructor
    }


    public AuthenticateFaceFragment(int requestCode) {
        this.requestCode = requestCode;
    }

    public void setNfcMode(Tag tag) {
        this.tag = tag;
        isNFCMode = true;
        Logger.e("setNfcMode :: called");

        if (!isReading) {
            Logger.d("onResume :: enableForegroundDispatch called");
            textViewStatus.setText("");
            //set the reading flag..
            isReading = true;

            //show the dialog to provide user interaction...
            showProgressDialog("Reading");

            //create the object of ReaderCardDataAsync
            if (tag == null) {
                Logger.e("setNfcMode :: tag is null");
                return;
            } else {
                nfcReaderAsync = new NfcReaderAsync(nfcReaderListener, tag);
            }
            nfcReaderAsync.execute();
        }//
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_authenticate_face, container, false);
        textViewStatus = (TextView) view.findViewById(R.id.tv_authenticate_face_status);
        btnAuthenticate = (Button) view.findViewById(R.id.btn_authenticate);
        btnAuthenticate.setOnClickListener(this);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter = new AuthenticateFacePresenter(this);

        /*btnAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/

    }

    public void authenticateFace(String imageBase64) {
        showProgressDialog("Authenticating Face with Server.");
        presenter.authenticateFace(imageBase64);
    }

    @Override
    public void onClick(View view) {
        if (view == btnAuthenticate) {
            if (!isReading) {
                textViewStatus.setText("");
                //set the reading flag..
                isReading = true;

                //show the dialog to provide user interaction...
                showProgressDialog("Reading");

                //create the object of ReaderCardDataAsync
                if (tag == null) {
                    nfcReaderAsync = new NfcReaderAsync(nfcReaderListener);
                } else {
                    nfcReaderAsync = new NfcReaderAsync(nfcReaderListener, tag);
                }
                nfcReaderAsync.execute();
            }//

        }//
    }//

    private NfcReaderAsync.NfcReaderListener nfcReaderListener = new NfcReaderAsync.NfcReaderListener() {
        @Override
        public void onReadComplete(int status, String message, String requestID) {
            //dismiss the dialog...
            hideProgressDialog();

            Logger.d("Reading finished with status " + status);
            isReading = false;
            if (status == Constants.SUCCESS && requestID != null) {

                textViewStatus.setText("");
                showProgressDialog("Authenticating Face with Server.");
                presenter.openAuthenticateFace(mContext, requestID, requestCode);

            } else {
                if (status == ErrorCode.UNSPECIFIED.getCode()) {
                    textViewStatus.setText("Error in reading data from card. \n" + message);
                } else {
                    textViewStatus.setText("Error Code : " + status + "\nError in reading data from card. \n" + message);
                }

            }
            /*if (isNFCMode) {
                ReaderConnectionTask readerConnectionTask =
                        new ReaderConnectionTask(connectToolkitListener, false);
                readerConnectionTask.execute();
            }//if()*/
        }//onCardReadComplete

    };

    private ReaderConnectionTask.ConnectToolkitListener connectToolkitListener = new
            ReaderConnectionTask.ConnectToolkitListener() {
                @Override
                public void onToolkitConnected(int status, boolean isConnectFlag, String message) {
                    if (!isConnectFlag) {
                        Toast.makeText(getActivity(), "Disconnected", Toast.LENGTH_SHORT).show();
                    }//
                }//onToolkitConnected()
            };//ConnectToolkitListener

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
        connectToolkitListener = null;
        nfcReaderListener = null;
        isNFCMode = false;
    }//

    @Override
    protected void appendError(String message) {
        textViewStatus.setText(message);
    }


    @Override
    public void onContextNull() {
        hideProgressDialog();
        textViewStatus.setText("Something went wrong!");
    }

    @Override
    public void onRequestIdNull() {
        hideProgressDialog();
        textViewStatus.setText("Something went wrong!");
    }


    @Override
    public void onImageBase64Empty() {
        hideProgressDialog();
        textViewStatus.setText("Something went wrong!");
    }

    @Override
    public void onSuccess(Context mContext, String requestId, int requestCode) {
        hideProgressDialog();
        try {
          /* task = new ValidateCardCaptureFaceAsync(mContext, requestID, requestCode, new ValidateCardCaptureFaceAsync.ValidateCardCaptureFaceListener() {
               @Override
               public void onValidateCardCaptureFaceCompleted(int status, String message) {
                   cleanUp();
                   textViewStatus.setText(message);
               }
           });
           task.execute();*/

            CardReader cardReader = ConnectionController.getConnection();
            if (cardReader == null) {
                Logger.e("EIDAToolkit is null;");
                textViewStatus.setText("EIDAToolkit is null");
            } else {
                try {
                    cardReader.validateCardAndCaptureFace(mContext, requestId, requestCode);
                } catch (ToolkitException e) {
                    textViewStatus.setText(e.getMessage());
                } catch (Exception e) {
                    textViewStatus.setText(e.getMessage());
                }
            }
        } catch (Exception e) {
            textViewStatus.setText(e.getMessage());
        }
    }

    /*private void cleanUp() {
        if (task != null && !task.isCancelled()) {
            task.cancel(true);
        }
    }*/

    @Override
    public void onFaceAuthenticationCompleted(String message) {
        hideProgressDialog();
        textViewStatus.setText(message);
    }

    @Override
    public void showError(String message) {
        textViewStatus.setText(message);
    }
}
