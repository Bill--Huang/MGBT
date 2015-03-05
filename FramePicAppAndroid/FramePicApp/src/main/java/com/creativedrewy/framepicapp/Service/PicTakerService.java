package com.creativedrewy.framepicapp.service;

import android.app.Activity;

import com.creativedrewy.framepicapp.BuildConfig;

/**
 * Model class for the PicTaker functionality
 */
public class PicTakerService extends ServiceBase {
    public final static String PICTAKER_HOST_IP_PREF = "pictakerHostIPPref";

    /**
     * Constructor
     */
    public PicTakerService(String ipAddress) {
        super(ipAddress);

        _roleString = "picTaker";
        _registerMessage = BuildConfig.pic_register;
    }

    /**
     * Send the message to the server that this app instance should be put in frame order
     */
    public void submitOrder() {
        sendAppDataEmit(BuildConfig.pic_requestFrameOrder);
    }

    /**
     * Send the message to the server that this app instance is ready to take its framepic
     * @param frameNumber This app's frame number
     */
    public void submitReady(int frameNumber) {
        sendAppDataEmit(BuildConfig.pic_picTakingReady, String.valueOf(frameNumber));
    }

    /**
     * Send the message to the server that this app instance is un-registering itself from the PicTakers set
     * @param frameNumber This app's frame number, if it has one
     */
    public void submitUnRegister(int frameNumber) {
        sendAppDataEmit(BuildConfig.pic_unRegisterPicTaker, String.valueOf(frameNumber));
    }

}
