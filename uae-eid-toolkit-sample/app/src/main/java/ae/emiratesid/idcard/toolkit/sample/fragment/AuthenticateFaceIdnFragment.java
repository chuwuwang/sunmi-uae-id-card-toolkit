package ae.emiratesid.idcard.toolkit.sample.fragment;

import static ae.emiratesid.idcard.toolkit.sample.AppController.isReading;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

import ae.emiratesid.idcard.toolkit.Toolkit;
import ae.emiratesid.idcard.toolkit.ToolkitException;
import ae.emiratesid.idcard.toolkit.datamodel.CardPublicData;
import ae.emiratesid.idcard.toolkit.sample.AppController;
import ae.emiratesid.idcard.toolkit.sample.ConnectionController;
import ae.emiratesid.idcard.toolkit.sample.Constants;
import ae.emiratesid.idcard.toolkit.sample.R;
import ae.emiratesid.idcard.toolkit.sample.task.AuthenticateFaceAsync;
import ae.emiratesid.idcard.toolkit.sample.task.AuthenticateFaceIdnAsync;
import ae.emiratesid.idcard.toolkit.sample.task.NfcReaderAsync;
import ae.emiratesid.idcard.toolkit.sample.utils.RequestGenerator;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AuthenticateFaceIdnFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AuthenticateFaceIdnFragment extends BaseFragment implements View.OnClickListener {

    private int requestCode;
    private Button btnCaptureFace;
    private EditText edtIdn;
    private TextView textViewStatus;
    private Context mContext;
    private AuthenticateFaceIdnAsync task;

    public AuthenticateFaceIdnFragment(int requestCode) {
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
        View view = inflater.inflate(R.layout.fragment_authenticate_face_idn, container, false);
        textViewStatus = (TextView) view.findViewById(R.id.tv_capture_face);
        btnCaptureFace = (Button) view.findViewById(R.id.btn_capture_face);
        edtIdn = (EditText) view.findViewById(R.id.edt_idn);
        btnCaptureFace.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        if (view == btnCaptureFace) {
            String sIdn = edtIdn.getText().toString();
            if (!sIdn.isEmpty()) {
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
            } else {
                edtIdn.setError("required");
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
        task = new AuthenticateFaceIdnAsync(edtIdn.getText().toString(), imageBase64, new AuthenticateFaceIdnAsync.AuthenticateFaceIdnListener() {
            @Override
            public void onFaceAuthenticationIdnCompleted(int status, String message, CardPublicData cardPublicData) {

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