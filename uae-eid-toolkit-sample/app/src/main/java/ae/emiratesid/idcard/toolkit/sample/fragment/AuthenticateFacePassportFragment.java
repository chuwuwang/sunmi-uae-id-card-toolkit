package ae.emiratesid.idcard.toolkit.sample.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ae.emiratesid.idcard.toolkit.Toolkit;
import ae.emiratesid.idcard.toolkit.ToolkitException;
import ae.emiratesid.idcard.toolkit.datamodel.CardPublicData;
import ae.emiratesid.idcard.toolkit.sample.ConnectionController;
import ae.emiratesid.idcard.toolkit.sample.Constants;
import ae.emiratesid.idcard.toolkit.sample.R;
import ae.emiratesid.idcard.toolkit.sample.task.AuthenticateFacePassportAsync;
import ae.emiratesid.idcard.toolkit.sample.utils.RequestGenerator;

public class AuthenticateFacePassportFragment extends BaseFragment implements View.OnClickListener {

    private int requestCode;
    private Button btnCaptureFace;
    private EditText edtPassportNumber;
    private EditText edtPassportCountry;
    private EditText edtPassportExpiry;
    private EditText edtPassportDob;
    private TextView textViewStatus;
    private Context mContext;
    private AuthenticateFacePassportAsync task;

    public AuthenticateFacePassportFragment(int requestCode) {
        this.requestCode = requestCode;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_authenticate_face_passport, container, false);
        textViewStatus = (TextView) view.findViewById(R.id.tv_capture_face);
        btnCaptureFace = (Button) view.findViewById(R.id.btn_capture_face);
        edtPassportNumber = (EditText) view.findViewById(R.id.edt_passport_number);
        edtPassportCountry = (EditText) view.findViewById(R.id.edt_passport_country_code);
        edtPassportExpiry = (EditText) view.findViewById(R.id.edt_passport_expiry);
        edtPassportDob = (EditText) view.findViewById(R.id.edt_passport_dob);
        btnCaptureFace.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        if (view == btnCaptureFace) {
            String sPassportNumber = edtPassportNumber.getText().toString();
            String sPassportCountry = edtPassportCountry.getText().toString();
            String sPassportExpiry = edtPassportExpiry.getText().toString();
            String sPassportDob = edtPassportDob.getText().toString();
            if (sPassportNumber.isEmpty()) {
                edtPassportNumber.setError("required");
                return;
            }
            if (sPassportCountry.isEmpty()) {
                edtPassportNumber.setError("required");
                return;
            }
            if (sPassportExpiry.isEmpty()) {
                edtPassportNumber.setError("required");
                return;
            }
            if (sPassportDob.isEmpty()) {
                edtPassportNumber.setError("required");
                return;
            }
            Toolkit toolkit = null;
            try {
                toolkit = ConnectionController.getToolkit();
                if (toolkit == null) {
                    Toast.makeText(mContext, "Toolkit is not initialized.", Toast.LENGTH_SHORT).show();
                    return;
                }
                String requestId = RequestGenerator.generateRequestID();
                toolkit.CaptureFace(mContext, requestId, requestCode);
            } catch (ToolkitException e) {
                textViewStatus.setText(e.getMessage());
            }
        }//
    }

    public void authenticateFace(String imageBase64) {

        /*Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.download);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteStream);
        byte[] byteArray = byteStream.toByteArray();
        imageBase64 = Base64.encodeToString(byteArray,Base64.DEFAULT);*/

        showProgressDialog("Authenticating Face with Server.");
        task = new AuthenticateFacePassportAsync(edtPassportNumber.getText().toString(),
                edtPassportCountry.getText().toString(),
                edtPassportExpiry.getText().toString(),
                edtPassportDob.getText().toString(), imageBase64, new AuthenticateFacePassportAsync.AuthenticateFacePassportListener() {
            @Override
            public void onFaceAuthenticationPassportCompleted(int status, String message, CardPublicData cardPublicData) {

                if (status == Constants.SUCCESS) {
                    cleanUp();
                    hideProgressDialog();
                    textViewStatus.setText(message);
                } else {
                    cleanUp();
                    hideProgressDialog();
                    textViewStatus.setText(message);
                }
            }
        });
        task.execute();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }//

    @Override
    protected void appendError(String message) {
        textViewStatus.setText(message);
    }

    private void cleanUp() {
        if (task != null && !task.isCancelled()) {
            task.cancel(true);
        }
    }
}