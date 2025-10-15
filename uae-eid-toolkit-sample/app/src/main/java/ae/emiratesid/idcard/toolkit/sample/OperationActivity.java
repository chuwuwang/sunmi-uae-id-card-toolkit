package ae.emiratesid.idcard.toolkit.sample;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


import com.dtt.face.MyFaceKey;

import ae.emiratesid.idcard.toolkit.sample.fragment.AuthenticateFaceIdnFragment;
import ae.emiratesid.idcard.toolkit.sample.fragment.AuthenticateFacePassportFragment;
import ae.emiratesid.idcard.toolkit.sample.fragment.authenticateface.view.AuthenticateFaceFragment;
import ae.emiratesid.idcard.toolkit.sample.fragment.CardSerialNumberFragment;
import ae.emiratesid.idcard.toolkit.sample.fragment.CertificateFragment;
import ae.emiratesid.idcard.toolkit.sample.fragment.CheckCardFragment;
import ae.emiratesid.idcard.toolkit.sample.fragment.FamilyBookFragment;
import ae.emiratesid.idcard.toolkit.sample.fragment.HomeFragment;
import ae.emiratesid.idcard.toolkit.sample.fragment.PKIAuthFragment;
import ae.emiratesid.idcard.toolkit.sample.fragment.ParseMRZ;
import ae.emiratesid.idcard.toolkit.sample.fragment.PinResetFragment;
import ae.emiratesid.idcard.toolkit.sample.fragment.PublicDataReadingFragment;
import ae.emiratesid.idcard.toolkit.sample.fragment.VerifyCardAndBiometric;
import ae.emiratesid.idcard.toolkit.sample.fragment.register.view.RegisterDeviceFragment;
import ae.emiratesid.idcard.toolkit.sample.fragment.SetNFCParamsFragment;
import ae.emiratesid.idcard.toolkit.sample.fragment.SignDataFragment;
import ae.emiratesid.idcard.toolkit.sample.fragment.VerifyFragment;
import ae.emiratesid.idcard.toolkit.sample.logger.Logger;

public class OperationActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_AUTHENTICATE_FACE = 100;
    private static final int REQUEST_CODE_AUTHENTICATE_FACE_IDN = 101;
    private static final int REQUEST_CODE_AUTHENTICATE_FACE_PASSPORT = 102;

    private NfcAdapter adapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mIntentFilters;
    private String[][] techListsArray;
    private Fragment fragment = null;
    private boolean isNFCRequired;
    private int type;

    private static boolean isNFCMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operation);

        type = getIntent().getIntExtra("TYPE", 0);
        Logger.d("Value for type  : " + type);

        isNFCRequired = (type == 5 || type == 7 || type == 4 || type == 19);
        if (isNFCRequired) {
            adapter = NfcAdapter.getDefaultAdapter(this);
            if (null != adapter) {
                mPendingIntent = PendingIntent.getActivity(
                        this, 0, new Intent(this, getClass()).
                                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
                IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
                IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
                mIntentFilters = new IntentFilter[]{ndef, tech};
                techListsArray = new String[][]{new String[]{NfcF.class.getName()}};
            }//if()
        }
        loadFragment();
    }//

    private Tag tag;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Logger.d("Calling OnNewIntent");
        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Logger.d("Calling OnNewIntent tag  : "+tag);
        setTagToFragment();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null && isNFCRequired) {
            Logger.d("onResume :: enableForegroundDispatch called");
            adapter.enableForegroundDispatch(this, mPendingIntent, mIntentFilters, techListsArray);
        }//if()
    }//

    private void loadFragment() {

        switch (type) {
            case 0:
                fragment = new HomeFragment();
                break;
            case 2:
                fragment = new RegisterDeviceFragment();
                break;
            case 4:
                fragment = new CardSerialNumberFragment();
                break;
            case 5:
                fragment = new PublicDataReadingFragment();
                break;
            case 6:
                fragment = new CheckCardFragment();
                break;
            case 7:
                fragment = new VerifyFragment();
                break;
            case 8:
                fragment = new VerifyCardAndBiometric();
                break;
            case 9:
                fragment = new PKIAuthFragment();
                break;
            case 10:
                fragment = new PinResetFragment();
                break;
            case 11:
                fragment = new CertificateFragment();
                break;
            case 12:
                fragment = new SignDataFragment();
                break;
            case 13:
                fragment = new FamilyBookFragment();
                break;
            case 15:
                fragment = new SetNFCParamsFragment();
                break;
            case 17:
                fragment = new ParseMRZ();
                break;
            case 19:
                fragment = new AuthenticateFaceFragment(REQUEST_CODE_AUTHENTICATE_FACE);
                break;
            case 20:
                fragment = new AuthenticateFaceIdnFragment(REQUEST_CODE_AUTHENTICATE_FACE_IDN);
                break;
            case 21:
                fragment = new AuthenticateFacePassportFragment(REQUEST_CODE_AUTHENTICATE_FACE_PASSPORT);
                break;
            default:
                Toast.makeText(this, "Not yet Implemented", Toast.LENGTH_SHORT).show();
                return;
        }//switch()
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.root, fragment)
                .commit();
    }//

    private void setTagToFragment() {

        switch (type) {

            case 4:
                Logger.d("Tag found setting new tag to read public data");
                ((CardSerialNumberFragment) fragment).setNfcMode(tag);
                break;

            case 5:
                Logger.d("Tag found setting new tag to read public data");
                ((PublicDataReadingFragment) fragment).setNfcMode(tag);
                break;
            case 7:
                Logger.d("Tag found setting new tag to read verify data");
                ((VerifyFragment) fragment).setNfcMode(tag);
                break;
            case 19:
                Logger.d("Tag found setting new tag to read verify data");
                ((AuthenticateFaceFragment) fragment).setNfcMode(tag);
                break;
            default:
                Toast.makeText(this, "This functionality is not supported in NFC mode",
                        Toast.LENGTH_SHORT).show();
                return;
        }//switch()
    }


    @Override
    protected void onPause() {

        super.onPause();
        if (adapter != null) {
            adapter.disableForegroundDispatch(this);
        }//if()
    }//onPause()

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_AUTHENTICATE_FACE) {
            if (resultCode == Activity.RESULT_OK) {

                String result = data.getStringExtra(MyFaceKey.INTENT_RESULT);

                if (MyFaceKey.RESULT_SUCCESS.equals(result)) {

                    String message = data.getStringExtra(MyFaceKey.INTENT_MESSAGE);
                    String imageBase64 = data.getStringExtra(MyFaceKey.IMAGE_BASE64);

                    ((AuthenticateFaceFragment) fragment).authenticateFace(imageBase64);

                } else if (MyFaceKey.RESULT_FAIL.equals(result)) {

                    String message = data.getStringExtra(MyFaceKey.INTENT_MESSAGE);

                    ((AuthenticateFaceFragment) fragment).showError(message);
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {

                Toast.makeText(this, "Cancelled.", Toast.LENGTH_SHORT).show();

            } else {

                Toast.makeText(this, "Unknown result.", Toast.LENGTH_SHORT).show();

            }
        }

        if (requestCode == REQUEST_CODE_AUTHENTICATE_FACE_IDN) {
            if (resultCode == Activity.RESULT_OK) {

                String result = data.getStringExtra(MyFaceKey.INTENT_RESULT);

                if (MyFaceKey.RESULT_SUCCESS.equals(result)) {

                    String message = data.getStringExtra(MyFaceKey.INTENT_MESSAGE);
                    String imageBase64 = data.getStringExtra(MyFaceKey.IMAGE_BASE64);

                    ((AuthenticateFaceIdnFragment) fragment).authenticateFace(imageBase64);

                } else if (MyFaceKey.RESULT_FAIL.equals(result)) {

                    String message = data.getStringExtra(MyFaceKey.INTENT_MESSAGE);

                    ((AuthenticateFaceFragment) fragment).showError(message);
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {

                Toast.makeText(this, "Cancelled.", Toast.LENGTH_SHORT).show();

            } else {

                Toast.makeText(this, "Unknown result.", Toast.LENGTH_SHORT).show();

            }
        }
        if (requestCode == REQUEST_CODE_AUTHENTICATE_FACE_PASSPORT) {
            if (resultCode == Activity.RESULT_OK) {

                String result = data.getStringExtra(MyFaceKey.INTENT_RESULT);

                if (MyFaceKey.RESULT_SUCCESS.equals(result)) {

                    String message = data.getStringExtra(MyFaceKey.INTENT_MESSAGE);
                    String imageBase64 = data.getStringExtra(MyFaceKey.IMAGE_BASE64);

                    ((AuthenticateFacePassportFragment) fragment).authenticateFace(imageBase64);

                } else if (MyFaceKey.RESULT_FAIL.equals(result)) {

                    String message = data.getStringExtra(MyFaceKey.INTENT_MESSAGE);

                    ((AuthenticateFaceFragment) fragment).showError(message);
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {

                Toast.makeText(this, "Cancelled.", Toast.LENGTH_SHORT).show();

            } else {

                Toast.makeText(this, "Unknown result.", Toast.LENGTH_SHORT).show();

            }
        }
    }

//    @Override
//    public void onSetData( String cardNumber, String dateOfBirth, String cardExpDate ) {
//
//        setTagToFragment();
//    }
//    @Override
//    public void onFinish() {
//
//        if ( NFCCardParams.isNFCParamSet ) {
//            setTagToFragment();
//        } else {
//            Snackbar.make( findViewById( R.id.root ), "Can't proceed without NFC params",
//                    Snackbar.LENGTH_SHORT ).show();
//        }
//    }
//
//    private void createDialogForInput() {
//
//        NfcFieldsDialogFragment fragment = new NfcFieldsDialogFragment();
//        FragmentManager fm = getSupportFragmentManager();
//        fragment.show( fm, "InputDialog" );
//    }
}
