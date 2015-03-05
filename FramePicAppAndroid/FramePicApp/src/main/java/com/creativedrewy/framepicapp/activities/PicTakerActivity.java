package com.creativedrewy.framepicapp.activities;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.creativedrewy.framepicapp.BuildConfig;
import com.creativedrewy.framepicapp.R;
import com.creativedrewy.framepicapp.camera.CameraPreview;
import com.creativedrewy.framepicapp.service.IServerMessageHandler;
import com.creativedrewy.framepicapp.service.PicTakerService;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpPost;
import com.koushikdutta.async.http.MultipartFormDataBody;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Activity/view for apps that will operate as PicTakers
 */
public class PicTakerActivity extends Activity implements IServerMessageHandler {

    @InjectView(R.id.picTakerMainLinearLayout) protected LinearLayout _mainLayout;
    @InjectView(R.id.picRegisterButton) protected Button _picRegisterButton;
    @InjectView(R.id.submitPicOrderButton) protected Button _submitPicOrderButton;
    @InjectView(R.id.picReadyButton) protected Button _picReadyButton;

    @InjectView(R.id.serverAddrEditText) protected EditText _serverAddrEditText;

    @InjectView(R.id.registerStepContainer) protected RelativeLayout _registerStepContainer;
    @InjectView(R.id.submitOrderStepContainer) protected RelativeLayout _submitOrderStepContainer;

    @InjectView(R.id.readyStepContainer) protected RelativeLayout _readyStepContainer;

    @InjectView(R.id.framePreviewImageView) protected ImageView _framePreviewImageView;

    private PicTakerService _picTakerService;
    private int _picFrameNumber = -1;
    private SharedPreferences _appPrefs;
    private Camera _systemCamera = null;
    private CameraPreview _cameraPreviewWindow;
    private ProgressDialog _uploadingDialog;
    private byte[] _capturedImageBytes;

//    private RelativeLayout _cameraAttrButtonContainer;
//    private Button _toggleCameraAttrsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pictaker_layout);
        ButterKnife.inject(this);

        _framePreviewImageView.setVisibility(View.GONE);
        _appPrefs = getPreferences(MODE_PRIVATE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        String ipString = _appPrefs.getString(PicTakerService.PICTAKER_HOST_IP_PREF, "");
        if (!ipString.equals("")) {
            _serverAddrEditText.setText(ipString);
        }

//        initializeCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (_picTakerService != null) {
            //TODO: If we want the master app to accurately update ordered and ready PicTaker counts, we
            //TODO: would need to send along a "isReady" boolean value along with this call
            _picTakerService.submitUnRegister(_picFrameNumber);
        }

        if (_systemCamera != null) {
            _systemCamera.release();
        }
    }

    @OnClick(R.id.picRegisterButton)
    void startPicRegister() {
        String ipAddr = _serverAddrEditText.getText().toString();
        _appPrefs.edit().putString(PicTakerService.PICTAKER_HOST_IP_PREF, ipAddr).apply();

        _picTakerService = new PicTakerService(ipAddr);
        _picTakerService.subscribeConnection()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> handleServerMessage(pair.first, pair.second),
                           err -> Toast.makeText(this, getString(R.string.server_connect_error_message), Toast.LENGTH_LONG).show());

        InputMethodManager inputMethodManager = (InputMethodManager)  PicTakerActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(PicTakerActivity.this.getCurrentFocus().getWindowToken(), 0);
    }

    @OnClick(R.id.submitPicOrderButton)
    void submitPicOrder() {

        _picTakerService.submitOrder();
    }

    @OnClick(R.id.picReadyButton)
    void picTakerReady() {
        _picTakerService.submitReady(_picFrameNumber);

        _registerStepContainer.setVisibility(View.GONE);
        _submitOrderStepContainer.setVisibility(View.GONE);
        _readyStepContainer.setVisibility(View.GONE);

        initializeCamera();
    }

    /**
     * Setup the camera and preview that will show while grabbing the frame
     */
    public void initializeCamera() {
        try {
            _systemCamera = Camera.open();
            _cameraPreviewWindow = new CameraPreview(this, _systemCamera);

            //LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            _cameraPreviewWindow.setLayoutParams(layoutParams);

            _mainLayout.addView(_cameraPreviewWindow);

            // Add Button View
//            View controlView = View.inflate(this, R.layout.takepicture_activity_layout, null);
//            _mainLayout.addView(controlView);
//            Log.d("TTT", "ddddddddddddddddd");


        } catch (Exception ex) {
            Toast.makeText(this, "Could not init camera. Will not capture frame.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * After the picture has been taken, save it to user's device, upload to MGBT server, and
     * update the UI this particular app instance's frame
     */
    private Camera.PictureCallback _pictureCallback = new Camera.PictureCallback() {
        @Overrideœ
        public void onPictureTaken(byte[] bytes, Camera camera) {
            _capturedImageBytes = bytes;
            String fileName = "MGBT_" + _picFrameNumber + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()).toString() + ".jpg";

            File sdRoot = Environment.getExternalStorageDirectory();
            String dir = "/MGBT/";
            File mkDir = new File(sdRoot, dir);
            mkDir.mkdirs();
            File pictureFile = new File(sdRoot, dir + fileName);

            try {
                FileOutputStream purge = new FileOutputStream(pictureFile);
                purge.write(bytes);
                purge.close();
            } catch (FileNotFoundException e) {
                Log.d("DG_DEBUG", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("DG_DEBUG", "Error accessing file: " + e.getMessage());
            }

            AsyncHttpPost reqPost = new AsyncHttpPost("http://" + _picTakerService.getServerIP() + ":7373/fileUpload");
            MultipartFormDataBody body = new MultipartFormDataBody();
            body.addFilePart("framePic", pictureFile);
            body.addStringPart("frameNumber", String.valueOf(_picFrameNumber));
            reqPost.setBody(body);

            Future<String> uploadReturn = AsyncHttpClient.getDefaultInstance().executeString(reqPost);
            uploadReturn.setCallback(new FutureCallback<String>() {
                @Override
                public void onCompleted(Exception e, String s) {
                    if (_uploadingDialog != null) {
                        _uploadingDialog.cancel();
                    }

                    if (_systemCamera != null) {
                        _systemCamera.release();
                    }

                    _mainLayout.removeView(_cameraPreviewWindow);

                    _registerStepContainer.setVisibility(View.VISIBLE);
                    _submitOrderStepContainer.setVisibility(View.VISIBLE);
                    _readyStepContainer.setVisibility(View.VISIBLE);

                    _framePreviewImageView.setImageBitmap(BitmapFactory.decodeByteArray(_capturedImageBytes, 0, _capturedImageBytes.length));
                    _framePreviewImageView.setVisibility(View.VISIBLE);
                    _picReadyButton.setVisibility(View.GONE);
                }
            });

            try {
                _uploadingDialog = ProgressDialog.show(PicTakerActivity.this, "Uploading Frame", "Uploading your frame to MGBT server.");
                uploadReturn.get();
            } catch (Exception e) {
                //Do we need to handle the specific timeout exception?
                e.printStackTrace();
            }
        }
    };

    /**
     * Reset this PicTaker instance for the next Freeze Time operation
     */
    public void resetPicTaker() {
        _picFrameNumber = -1;

        if (_systemCamera != null) {
            _systemCamera.release();
        }

        if (_cameraPreviewWindow != null) {
            _mainLayout.removeView(_cameraPreviewWindow);
        }

        _registerStepContainer.setVisibility(View.VISIBLE);
        _submitOrderStepContainer.setVisibility(View.VISIBLE);
        _readyStepContainer.setVisibility(View.VISIBLE);

        _submitPicOrderButton.setEnabled(false);
        _submitPicOrderButton.setText(getString(R.string.submit_frame_order_button_text));

        _picReadyButton.setVisibility(View.VISIBLE);
        _picReadyButton.setEnabled(false);

        _framePreviewImageView.setImageDrawable(null);
    }

    /**
     * Handle message/payload data from the MGBT server; implemented from the interface
     */
    @Override
    public void handleServerMessage(String message, String payload) {
        if (message.equals(BuildConfig.pic_registerResponse)) {
            _picRegisterButton.setText("Registered!");
            _picRegisterButton.setEnabled(false);

            _submitPicOrderButton.setText("Waiting for master...");
        } else if (message.equals(BuildConfig.pic_serverOrderingStart)) {
            _submitPicOrderButton.setEnabled(true);
            _submitPicOrderButton.setText("Submit Order");
        } else if (message.equals(BuildConfig.pic_frameOrderResponse)) {
            _picFrameNumber = Integer.valueOf(payload);
            _picReadyButton.setEnabled(true);

            _submitPicOrderButton.setText("Frame Number: " + _picFrameNumber);
            _submitPicOrderButton.setEnabled(false);
        } else if (message.equals(BuildConfig.pic_takeFramePic)) {
            if (_systemCamera != null) {
                _systemCamera.takePicture(null, null, _pictureCallback);
            }
        } else if (message.equals(BuildConfig.pic_resetPicTaker)) {
            resetPicTaker();
        }
    }
}
