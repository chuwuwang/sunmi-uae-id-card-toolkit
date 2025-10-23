package com.sunmi.eid_plugin;

import android.text.TextUtils;

import com.sunmi.eid_plugin.constant.ReturnConstant;
import com.sunmi.eid_plugin.reader.SmartCardReader;
import com.sunmi.eid_plugin.utils.LogUtil;

public class SunmiReaderDispatch {
    private static final String TAG = "SunmiReaderDispatch";

    /**
     * Initialize the plugin.
     * This function is invoked by the Toolkit immediately after the module shared library is loaded into the process.
     *
     * @param context
     * @return
     */
    public int initPlugin(Object context) {
        return SmartCardReader.getInstance().init(context);
    }

    /**
     * Clean up the plugin module.
     * This function is invoked before the module is unloaded from the application address space.
     *
     * @return
     */
    public int cleanUp() {
        return SmartCardReader.getInstance().cleanUp();
    }

    /**
     * Establish connection to the smartcard in the specific reader identified by the reader name.
     *
     * @return
     */
    public int connect(String readerName) {
        if (!TextUtils.equals(readerName, SmartCardReader.READER_NAME)) {
            return ReturnConstant.ETSTATUS_INVALID_PARAMETER;
        }
        return SmartCardReader.getInstance().connect();
    }

    /**
     * Disconnect an already established connection to smartcard from a previous call to Plugin_Connect.
     */
    public void disconnect() {
        SmartCardReader.getInstance().disConnect();
    }

    /**
     * Execute APDU commands for accessing data from smartcard.
     *
     * @param APDU
     * @param interfaceType Smart card connection interface type
     *                      CONTACT_INTERFACE (1)  NFC_INTERFACE (2)
     * @return
     */
    public CardResult sendAPDU(byte[] APDU, int interfaceType) {
        return SmartCardReader.getInstance().sendAPDU(APDU, interfaceType);
    }

    /**
     * Get the Answer to Reset (ATR) value of a smartcard.
     *
     * @return
     */
    public byte[] getATR() {
        return SmartCardReader.getInstance().getATRBytesArray();
    }

    /**
     * Get the list of Smart Card readers connected.
     *
     * @return
     */
    public String[] getListReader() {
        LogUtil.i(TAG, "java getListReader");
        return new String[]{SmartCardReader.READER_NAME};
    }

    /**
     * Set the NFC tag received by an Android application upon the tapping of ID card on the NFC reader.
     *
     * @param tag
     * @return
     */
    public int setNfcTag(Object tag) {
        LogUtil.i(TAG, "java setNfcTag - tag:" + (tag == null ? "tag is null" : tag.toString()));
        if (tag == null) {
            return ReturnConstant.ETSTATUS_NFC_TAG_INVALID;
        } else {
            return ReturnConstant.ETSTATUS_SUCCESS;
        }
    }

}
