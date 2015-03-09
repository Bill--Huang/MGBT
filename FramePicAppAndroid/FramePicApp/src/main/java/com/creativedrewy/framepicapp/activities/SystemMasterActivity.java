package com.creativedrewy.framepicapp.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.creativedrewy.framepicapp.BuildConfig;
import com.creativedrewy.framepicapp.R;
import com.creativedrewy.framepicapp.service.IServerMessageHandler;
import com.creativedrewy.framepicapp.service.SystemMasterService;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Activity/view for the app that will act as the MGBT master
 */
public class SystemMasterActivity extends Activity implements IServerMessageHandler {
    @InjectView(R.id.masterRegisterButton) protected Button _masterRegisterButton;
    @InjectView(R.id.initOrderingButton) protected Button _initOrderingButton;
    @InjectView(R.id.freezeTimeButton) protected Button _freezeTimeButton;
    @InjectView(R.id.resetSystemButton) protected Button _resetSystemButton;
    @InjectView(R.id.serverAddrEditText) protected EditText _serverAddrEditText;
    @InjectView(R.id.devicesReadyLabel) protected TextView _devicesReadyLabel;
    @InjectView(R.id.devicesOrderedLabel) protected TextView _devicesOrderedLabel;

    private SystemMasterService _masterModel;
    private int _orderedDevices = 0;
    private int _readyDevices = 0;
    private SharedPreferences _appPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.system_master_layout);
        ButterKnife.inject(this);

        _appPrefs = getPreferences(MODE_PRIVATE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        String ipString = _appPrefs.getString(SystemMasterService.SYSTEM_HOST_IP_PREF, "");
        if (!ipString.equals("")) {
            _serverAddrEditText.setText(ipString);
        }
    }

    @OnClick(R.id.masterRegisterButton)
    void onMasterRegisterClick() {
        String ipAddr = _serverAddrEditText.getText().toString();
        _appPrefs.edit().putString(SystemMasterService.SYSTEM_HOST_IP_PREF, ipAddr).commit();

        _masterModel = new SystemMasterService(ipAddr);
        _masterModel.subscribeConnection()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> handleServerMessage(pair.first, pair.second),
                           err -> Toast.makeText(this, getString(R.string.server_connect_error_message), Toast.LENGTH_LONG).show());

        InputMethodManager inputMethodManager = (InputMethodManager)  SystemMasterActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(SystemMasterActivity.this.getCurrentFocus().getWindowToken(), 0);
    }

    @OnClick(R.id.initOrderingButton)
    void onInitOrderingClick() {
        _masterModel.sendInitOrder();

        _initOrderingButton.setText("Ordering...");
        _initOrderingButton.setEnabled(false);
    }

    @OnClick(R.id.freezeTimeButton)
    void onFreezeTimeClick() {
        _masterModel.sendFreezeTime();

        _freezeTimeButton.setText("Boom!");
        _freezeTimeButton.setEnabled(false);
    }

    @OnClick(R.id.resetSystemButton)
    void onResetSystemClick() {
        resetFreezeOperation();
    }

    /**
     * Reset the system for the next round
     */
    public void resetFreezeOperation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to reset?");
        builder.setPositiveButton("Yes", (dialogInterface, i) -> {
            _masterModel.sendResetSystem();

            _initOrderingButton.setText(getString(R.string.init_ordering_label));
            _initOrderingButton.setEnabled(true);

            _freezeTimeButton.setText(getString(R.string.freeze_time_button_text));
            _freezeTimeButton.setEnabled(false);

            _devicesOrderedLabel.setText("");
            _devicesReadyLabel.setText("");

            _orderedDevices = 0;
            _readyDevices = 0;
        });
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> { });

        builder.show();
    }

    /**
     * Handle message/payload data from the MGBT server; implemented from the interface
     */
    @Override
    public void handleServerMessage(String message, String payload) {
        if (message.equals(BuildConfig.master_registerResponse)) {
            _masterRegisterButton.setEnabled(false);
            _masterRegisterButton.setText("Master Device");

            _resetSystemButton.setEnabled(true);
            _initOrderingButton.setEnabled(true);
        } else if (message.equals(BuildConfig.master_picTakerOrderUpdate)) {
            _orderedDevices++;
            _devicesOrderedLabel.setText(_orderedDevices + " device(s) ordered");
        } else if (message.equals(BuildConfig.master_picTakerFrameReadyUpdate)) {
            _readyDevices++;
            _devicesReadyLabel.setText(_readyDevices + " device(s) ready");

            if (_readyDevices == _orderedDevices) {
                _freezeTimeButton.setEnabled(true);
            } else {
                _freezeTimeButton.setEnabled(false);
            }
        } else if (message.equals(BuildConfig.master_serverProcessedPicResponse)) {
            // Server got all pic, and convert them into video
            // Re active "Freeze Time" Button
            _freezeTimeButton.setText(getString(R.string.freeze_time_button_text));
            _freezeTimeButton.setEnabled(true);
        }
        //else if (message.equals("PicTakerHasUnRegistered")) {
        //    //TODO: Check the payload to see if the PicTaker was ordered and/or ready
        //    //TODO: But note, right now, nothing is getting sent to the master app when a PicTaker un-registers
        //}
    }
}
