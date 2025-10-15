package ae.emiratesid.idcard.toolkit.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ae.emiratesid.idcard.toolkit.Toolkit;
import ae.emiratesid.idcard.toolkit.ToolkitException;
import ae.emiratesid.idcard.toolkit.sample.logger.Logger;
import ae.emiratesid.idcard.toolkit.sample.task.CardReaderConnectionTask;
import ae.emiratesid.idcard.toolkit.sample.task.CardSerialNumberAsync;
import ae.emiratesid.idcard.toolkit.sample.task.DeviceIdAsync;
import ae.emiratesid.idcard.toolkit.sample.task.InitializeToolkitTask;
import ae.emiratesid.idcard.toolkit.sample.utils.RequestGenerator;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    String[] operation = {
            "Initialize",//1
            "Register Device",//2
            "Connect Reader",//3
            "Card Serial Number",//4
            "Read Public Data", //5
            "Check Card Status", //6
            "Verify Biometric", //7
            "Verify Card and Biometric", //8
            "PKI AUTH", //9
            "PIN Reset", //10
            "Read Certificates", //11
            "Sign Data", //12
            "Read Family Book", //13
            "Disconnect Reader",//14
            "Set NFC Params", //15
            "Device ID",//16
            "Parse MRZ",//17
            "Clean Up",//18
            "Authenticate Face"//19
    };

    private ImageView imgInitialize, imgRegisterDevice, imgConnectReader, imgCardSerialNumber, imgReadPublicData,
            imgCheckCardStatus, imgVerifyBiometric, imgVerifyCardAndBiometric, imgPKIAuth, imgPinReset, imgReadCertificates,
            imgSignData, imgReadFamilyBook, imgDisconnectReader, imgSetNfcParams, imgDeviceId, imgParseMRZ, imgCleanUp,
            imgAuthenticateFace, imgAuthenticateFaceIdn, imgAuthenticateFacePassport;

    private ListView listOperation;
    private ProgressDialog pd;
    private boolean isToolkitInitializeSuccessfull = false;

    private static int variableForOnClick;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ///////////////////////////////////////////////

        imgInitialize = (ImageView) findViewById(R.id.img_initialize);
        imgRegisterDevice = (ImageView) findViewById(R.id.img_register_device);
        imgConnectReader = (ImageView) findViewById(R.id.img_connect_reader);
        imgCardSerialNumber = (ImageView) findViewById(R.id.img_card_serial_number);
        imgReadPublicData = (ImageView) findViewById(R.id.img_read_public_data);
        imgCheckCardStatus = (ImageView) findViewById(R.id.img_check_card_status);
        imgVerifyBiometric = (ImageView) findViewById(R.id.img_verify_biometric);
        imgVerifyCardAndBiometric = (ImageView) findViewById(R.id.img_verify_card_and_biometric);
        imgPKIAuth = (ImageView) findViewById(R.id.img_pki_auth);
        imgPinReset = (ImageView) findViewById(R.id.img_pin_reset);
        imgReadCertificates = (ImageView) findViewById(R.id.img_read_certificate);
        imgSignData = (ImageView) findViewById(R.id.img_sign_data);
        imgReadFamilyBook = (ImageView) findViewById(R.id.img_read_family_book);
        imgDisconnectReader = (ImageView) findViewById(R.id.img_disconnect_reader);
        imgSetNfcParams = (ImageView) findViewById(R.id.img_set_nfc_params);
        imgDeviceId = (ImageView) findViewById(R.id.img_device_id);
        imgParseMRZ = (ImageView) findViewById(R.id.img_parse_mrz);
        imgCleanUp = (ImageView) findViewById(R.id.img_clean_up);
        imgAuthenticateFace = (ImageView) findViewById(R.id.img_authenticate_face);
        imgAuthenticateFaceIdn = (ImageView) findViewById(R.id.img_authenticate_face_idn);
        imgAuthenticateFacePassport = (ImageView) findViewById(R.id.img_authenticate_face_passport);

        imgInitialize.setOnClickListener(this);
        imgRegisterDevice.setOnClickListener(this);
        imgConnectReader.setOnClickListener(this);
        imgCardSerialNumber.setOnClickListener(this);
        imgReadPublicData.setOnClickListener(this);
        imgCheckCardStatus.setOnClickListener(this);
        imgVerifyBiometric.setOnClickListener(this);
        imgVerifyCardAndBiometric.setOnClickListener(this);
        imgPKIAuth.setOnClickListener(this);
        imgPinReset.setOnClickListener(this);
        imgReadCertificates.setOnClickListener(this);
        imgSignData.setOnClickListener(this);
        imgReadFamilyBook.setOnClickListener(this);
        imgDisconnectReader.setOnClickListener(this);
        imgSetNfcParams.setOnClickListener(this);
        imgDeviceId.setOnClickListener(this);
        imgParseMRZ.setOnClickListener(this);
        imgCleanUp.setOnClickListener(this);
        imgAuthenticateFace.setOnClickListener(this);
        imgAuthenticateFaceIdn.setOnClickListener(this);
        imgAuthenticateFacePassport.setOnClickListener(this);


        ///////////////////////////////////////////////


//        listOperation = (ListView) findViewById(R.id.listView);
//        //if the fragment is available then the it is not two plane.
//
//        ArrayAdapter<String> itemsAdapter =
//                new ArrayAdapter<String>(this, R.layout.list_item_view, operation);
//
//        listOperation.addHeaderView(LayoutInflater.from(this).inflate(R.layout.header, null, false));
//        listOperation.setAdapter(itemsAdapter);
//        listOperation.setOnItemClickListener(this);
    }

    private CardReaderConnectionTask.ConnectToolkitListener connectToolkitListener = new CardReaderConnectionTask.ConnectToolkitListener() {
        @Override
        public void onToolkitConnected(int status, boolean isConnectFlag, String message) {

            hideProgressDialog();
            if (isConnectFlag) {
                if (status == Constants.SUCCESS) {
                    Toast.makeText(HomeActivity.this, "Card Reader Connected",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(getApplicationContext(), "Card Reader connection failed::" + message,
                        Toast.LENGTH_LONG).show();
                return;
            }
            if (status == Constants.SUCCESS) {
                Toast.makeText(HomeActivity.this, "Successfully Disconnected",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(HomeActivity.this, "Failed", Toast.LENGTH_SHORT).show();
        }
    };

//    @Override
//    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//
//        Logger.d("onItemClick():: position " + i);
//
//        if (i == 1) {
//            initialize();
//            return;
//        }//
//        if (i == 3) {
//            showProgressDialog("Connecting Card Reader....");
//            CardReaderConnectionTask cardReaderConnectionTask = new CardReaderConnectionTask
//                    (connectToolkitListener, true);
//            cardReaderConnectionTask.execute();
//            return;
//        }
//
////        if (i == 4) {
////            getCardSerialNumber();
////            return;
////        }
//
//        if (i == 14) {
//            showProgressDialog("Disconnecting Reader....");
//            CardReaderConnectionTask cardReaderConnectionTask = new CardReaderConnectionTask
//                    (connectToolkitListener, false);
//            cardReaderConnectionTask.execute();
//            return;
//        }//
//
//        if (i == 16) {
//            getDeviceId();
//            return;
//        }
//        if (i == 18) {
//            ConnectionController.cleanup();
//            Toast.makeText(this, "Clean Up Success", Toast.LENGTH_LONG).show();
//
//            return;
//        }
//        loadActivity(i);
//    }

    @Override
    public void onClick(View v) {

        Logger.d("Value for 111  : "+variableForOnClick);

        if (v.getId() == R.id.img_initialize) {
            variableForOnClick = 1;
            initialize();
            return;
        }
        if (v.getId() == R.id.img_register_device) {
            variableForOnClick = 2;
            Logger.d("Value for variableForOnClick  : "+variableForOnClick);
            loadActivity(variableForOnClick);
            return;
        }
        if (v.getId() == R.id.img_connect_reader) {
            variableForOnClick = 3;
            showProgressDialog("Connecting Card Reader....");
            CardReaderConnectionTask cardReaderConnectionTask = new CardReaderConnectionTask
                    (connectToolkitListener, true);
            cardReaderConnectionTask.execute();
            return;
        }
        if (v.getId() == R.id.img_card_serial_number) {
            variableForOnClick = 4;
            Logger.d("Value for variableForOnClick  : "+variableForOnClick);
            loadActivity(variableForOnClick);
            return;
        }
        if (v.getId() == R.id.img_read_public_data) {
            variableForOnClick = 5;
            Logger.d("Value for variableForOnClick  : "+variableForOnClick);
            loadActivity(variableForOnClick);
            return;
        }
        if (v.getId() == R.id.img_check_card_status) {
            variableForOnClick = 6;
            Logger.d("Value for variableForOnClick  : "+variableForOnClick);
            loadActivity(variableForOnClick);
            return;
        }
        if (v.getId() == R.id.img_verify_biometric) {
            variableForOnClick = 7;
            Logger.d("Value for variableForOnClick  : "+variableForOnClick);
            loadActivity(variableForOnClick);
            return;
        }
        if (v.getId() == R.id.img_verify_card_and_biometric) {
            variableForOnClick = 8;
            Logger.d("Value for variableForOnClick  : "+variableForOnClick);
            loadActivity(variableForOnClick);
            return;
        }
        if (v.getId() == R.id.img_pki_auth) {
            variableForOnClick = 9;
            Logger.d("Value for variableForOnClick  : "+variableForOnClick);
            loadActivity(variableForOnClick);
            return;
        }
        if (v.getId() == R.id.img_pin_reset) {
            variableForOnClick = 10;
            Logger.d("Value for variableForOnClick  : "+variableForOnClick);
            loadActivity(variableForOnClick);
            return;
        }
        if (v.getId() == R.id.img_read_certificate) {
            variableForOnClick = 11;
            Logger.d("Value for variableForOnClick  : "+variableForOnClick);
            loadActivity(variableForOnClick);
            return;
        }
        if (v.getId() == R.id.img_sign_data) {
            variableForOnClick = 12;
            Logger.d("Value for variableForOnClick  : "+variableForOnClick);
            loadActivity(variableForOnClick);
            return;
        }
        if (v.getId() == R.id.img_read_family_book) {
            variableForOnClick = 13;
            Logger.d("Value for variableForOnClick  : "+variableForOnClick);
            loadActivity(variableForOnClick);
            return;
        }
        if (v.getId() == R.id.img_disconnect_reader) {
            variableForOnClick = 14;
            showProgressDialog("Disconnecting Reader....");
            CardReaderConnectionTask cardReaderConnectionTask1 = new CardReaderConnectionTask
                    (connectToolkitListener, false);
            cardReaderConnectionTask1.execute();
            return;
        }
        if (v.getId() == R.id.img_set_nfc_params) {
            variableForOnClick = 15;
            Logger.d("Value for variableForOnClick  : "+variableForOnClick);
            loadActivity(variableForOnClick);
            return;
        }
        if (v.getId() == R.id.img_device_id) {
            variableForOnClick = 16;
            getDeviceId();
            return;
        }
        if (v.getId() == R.id.img_parse_mrz) {
            variableForOnClick = 17;
            Logger.d("Value for variableForOnClick  : "+variableForOnClick);
            loadActivity(variableForOnClick);
            return;
        }
        if (v.getId() == R.id.img_clean_up) {
            variableForOnClick = 18;
            ConnectionController.cleanup();
            Toast.makeText(this, "Clean Up Success", Toast.LENGTH_LONG).show();

            return;
        }
        if (v.getId() == R.id.img_authenticate_face) {
            variableForOnClick = 19;
            Logger.d("Value for variableForOnClick  : "+variableForOnClick);
            loadActivity(variableForOnClick);
            return;
        }
        if (v.getId() == R.id.img_authenticate_face_idn) {
            variableForOnClick = 20;
            Logger.d("Value for variableForOnClick  : "+variableForOnClick);
            loadActivity(variableForOnClick);
            return;
        }
        if (v.getId() == R.id.img_authenticate_face_passport) {
            variableForOnClick = 21;
            Logger.d("Value for variableForOnClick  : "+variableForOnClick);
            loadActivity(variableForOnClick);
            return;
        }



    }

    private void loadActivity(int position) {

        Intent intent = new Intent(this, OperationActivity.class);
        intent.putExtra("TYPE", position);
        startActivity(intent);
    }//loadActivity

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //        //clean up
        Logger.d("___Cleaning up__1.1");

        ConnectionController.cleanup();
    }

    protected void showProgressDialog(String Message) {

        pd = new ProgressDialog(HomeActivity.this);
        pd.requestWindowFeature(Window.FEATURE_NO_TITLE);
        pd.setMessage(Message); //recommended to use String resource...
        pd.setCancelable(false);
        pd.show();
    }//showProgressDialog()

    protected void hideProgressDialog() {

        //check is dialog is showing then dismiss it..
        if (pd != null && (pd.isIndeterminate() || pd.isShowing())) {
            Logger.d("hideProgressDialog():: called");
            pd.dismiss();
            Logger.d("hideProgressDialog():: dismissed");
            pd = null;
        }
    }

    private InitializeToolkitTask.InitializationListener mInitializationListener =
            new InitializeToolkitTask.InitializationListener() {
                @Override
                public void onToolkitInitialized(boolean isSuccessful, String statusMessage) {
                    Logger.d("onToolkitInitialized():: init tookit " + isSuccessful);
                    hideProgressDialog();
                    if (isSuccessful) {
                        Toast.makeText(HomeActivity.this, "Initialization Successful",
                                Toast.LENGTH_SHORT).show();


                        return;
                    }
                    Toast.makeText(getApplicationContext(), "Initialization Failed::" + statusMessage,
                            Toast.LENGTH_LONG).show();
                    return;
                }
            };

    private void initialize() {
        showProgressDialog("Initializing...");
        InitializeToolkitTask initializeToolkitTask = new InitializeToolkitTask
                (mInitializationListener);
        initializeToolkitTask.execute();
    }//initialize()

    private DeviceIdAsync.DeviceIdListener mDeviceIdListener = new DeviceIdAsync.DeviceIdListener() {
        @Override
        public void onGetDeviceId(int status, String message, String deviceid) {
            hideProgressDialog();
            Logger.d("onGetDeviceId:: DeviceId " + deviceid);

            if (status == Constants.SUCCESS) {

                AlertDialog dialog;
                AlertDialog.Builder builder;

                TextView showText = new TextView(HomeActivity.this);
                showText.setPadding(8, 8, 8, 8);
                showText.setTextSize(16f);
                showText.setText(deviceid);
                showText.setOnLongClickListener(new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        // Copy the Text to the clipboard
                        ClipboardManager manager =
                                (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        TextView showTextParam = (TextView) v;
                        manager.setText(showTextParam.getText());
                        // Show a message:
                        Toast.makeText(v.getContext(), "Text in clipboard",
                                Toast.LENGTH_SHORT)
                                .show();
                        return true;
                    }
                });
                builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setView(showText);
                dialog = builder.create();
                dialog.setTitle("Device Id");
                dialog.setIcon(R.drawable.ic_device_id);
                dialog.setCancelable(true);

                dialog.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (mDeviceIdAsync != null && mDeviceIdAsync.isCancelled())
                            mDeviceIdAsync.cancel(true);

                    }
                });

                dialog.show();
                return;
            }
            Toast.makeText(getApplicationContext(), "Unable to get device id::" + message,
                    Toast.LENGTH_LONG).show();
            return;

        }//onGetDeviceId()
    };

    /////////////////////////

    private CardSerialNumberAsync.CardSerialNumberListener mCardSerialNumberListener = new CardSerialNumberAsync.CardSerialNumberListener() {

        @Override
        public void onGetCardSerialNumber(int status, String message, String cardSerialNumber) {

            hideProgressDialog();
            Logger.d("onGetCardSerialNumber:: Card Serial Number " + cardSerialNumber);

            if (status == Constants.SUCCESS) {

                AlertDialog dialog;
                AlertDialog.Builder builder;

                TextView showText = new TextView(HomeActivity.this);
                showText.setPadding(8, 8, 8, 8);
                showText.setTextSize(16f);
                showText.setText(cardSerialNumber);
                showText.setOnLongClickListener(new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        // Copy the Text to the clipboard
                        ClipboardManager manager =
                                (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        TextView showTextParam = (TextView) v;
                        manager.setText(showTextParam.getText());
                        // Show a message:
                        Toast.makeText(v.getContext(), "Text in clipboard",
                                Toast.LENGTH_SHORT)
                                .show();
                        return true;
                    }
                });
                builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setView(showText);
                dialog = builder.create();
                dialog.setTitle("Card Serial Number");
                dialog.setIcon(R.drawable.ic_device_id);
                dialog.setCancelable(true);

                dialog.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (mCardSerialNumberAsync != null && mCardSerialNumberAsync.isCancelled())
                            mCardSerialNumberAsync.cancel(true);

                    }
                });

                dialog.show();
                return;
            }
            Toast.makeText(getApplicationContext(), "Unable to get Card Serial Number ::" + message,
                    Toast.LENGTH_LONG).show();
            return;



        }
    };//onGetDeviceId()


    //////////////////////////


    private DeviceIdAsync mDeviceIdAsync = null;
    private CardSerialNumberAsync mCardSerialNumberAsync = null;

    private void getDeviceId() {
        showProgressDialog("Obtaining Device Id");
        DeviceIdAsync deviceIdAsync = new DeviceIdAsync(mDeviceIdListener);
        deviceIdAsync.execute();
    }//getDeviceId()


//    private void getCardSerialNumber() {
//        showProgressDialog("Obtaining Card Serial Number");
//        CardSerialNumberAsync cardSerialNumberAsync = new CardSerialNumberAsync(mCardSerialNumberListener);
//        cardSerialNumberAsync.execute();
//    }//getDeviceId()


    @Override
    protected void onRestart() {
        super.onRestart();
        Logger.d("onRestart called");
    }


}//HomeActivity
