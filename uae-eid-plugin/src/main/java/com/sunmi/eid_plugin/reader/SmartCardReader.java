package com.sunmi.eid_plugin.reader;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

import com.sunmi.eid_plugin.BuildConfig;
import com.sunmi.eid_plugin.CardResult;
import com.sunmi.eid_plugin.constant.Constant;
import com.sunmi.eid_plugin.constant.ReturnConstant;
import com.sunmi.eid_plugin.utils.BytesUtil;
import com.sunmi.eid_plugin.utils.LogFileUtil;
import com.sunmi.eid_plugin.utils.LogUtil;
import com.sunmi.pay.hardware.aidl.AidlConstants;
import com.sunmi.pay.hardware.aidl.AidlErrorCode;
import com.sunmi.pay.hardware.aidl.SPErrorCode;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;
import com.sunmi.pay.hardware.aidlv2.readcard.ReadCardOptV2;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import sunmi.paylib.SunmiPayKernel;

/**
 * @Time : On 2022/11/15 10:38
 * @Description : SmartCardReader
 */
public class SmartCardReader {
    private static final String TAG = "SmartCardReader";
    public static final String READER_NAME = "SmartCardReader";
    private ReadCardOptV2 mReadCardOptV2;
    private int mCheckCardType = AidlConstants.CardType.IC.getValue() | AidlConstants.CardType.NFC.getValue();
    private Context mContext;
    //IC的ATR
    private String ATR = null;
    // 服务是否已连接
    public AtomicBoolean isServiceConnected = new AtomicBoolean(false);
    // 是否已经调用检卡接口 checkCard(mCheckCardType, mCheckCardCallbackV2, 60);
    private AtomicBoolean isCheckCardCalled = new AtomicBoolean(false);
    // 是否已寻到卡（mCheckCardCallbackV2回调寻卡成功）
    public AtomicBoolean isCardFound = new AtomicBoolean(false);
    //如果回调onDisconnectPaySDK()，尝试重新连接的次数
    private int reConnectionNum;
    private final int maxNum = 3;
    private Handler handler = null;
    // 将检卡回调异步转同步
    private CountDownLatch checkCardLatch;
    // 检卡回调onError()的错误码
    private int checkCardErrorCode = 0;

    public static SmartCardReader getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        private static SmartCardReader INSTANCE = new SmartCardReader();
    }

    private SunmiPayKernel.ConnectCallback mConnectCallback = new SunmiPayKernel.ConnectCallback() {
        @Override
        public void onConnectPaySDK() {
            mReadCardOptV2 = SunmiPayKernel.getInstance().mReadCardOptV2;
            isServiceConnected.set(true);
            LogUtil.i(TAG, "onConnectPaySDK");
        }

        @Override
        public void onDisconnectPaySDK() {
            LogUtil.i(TAG, "onDisconnectPaySDK");
            isServiceConnected.set(false);
            mReadCardOptV2 = null;
            if (handler == null) {
                handler = new Handler();
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (mContext != null) {
                        boolean result = SunmiPayKernel.getInstance().initPaySDK(mContext, mConnectCallback);
                        LogUtil.i(TAG, "reconnect initPaySDK:" + result + ", reConnectionNum:" + reConnectionNum);
                        reConnectionNum--;
                    }
                }
            });

        }
    };

    public int init(Object context) {
        LogUtil.LEVEL = BuildConfig.DEBUG ? Log.VERBOSE : Log.INFO;
        LogUtil.i(TAG, "java init - " + context);
        //每次init将重连次数重新赋值为3
        reConnectionNum = maxNum;
        if (!(context instanceof Context)) {
            LogUtil.e(TAG, "context error");
            return ReturnConstant.ETSTATUS_INVALID_PLUGIN_CONTEXT;
        }
        mContext = (Context) context;
        LogFileUtil.setContext(mContext);
        if (checkInit()) {
            return ReturnConstant.ETSTATUS_SUCCESS;
        }
        boolean initResult = SunmiPayKernel.getInstance().initPaySDK(mContext, mConnectCallback);
        LogUtil.i(TAG, "initResult:" + initResult);
        if (!initResult) {
            LogUtil.e(TAG, "initPaySDK failed");
            return ReturnConstant.ETSTATUS_INVALID_PARAMETER;
        }
        return ReturnConstant.ETSTATUS_SUCCESS;
    }

    public int cleanUp() {
        LogUtil.i(TAG, "call java cleanUp");
        // 检卡线程是否处于阻塞状态
        if (checkCardLatch != null) {
            isCardFound.set(false);
            checkCardLatch.countDown();
            checkCardLatch = null;
        }
        if (!checkInit()) {
            return ReturnConstant.ETSTATUS_SUCCESS;
        }
        if (isCheckCardCalled.get()) {
            disConnect();
        }
        SunmiPayKernel.getInstance().destroyPaySDK();
        isServiceConnected.set(false);
        isCheckCardCalled.set(false);
        isCardFound.set(false);
        checkCardErrorCode = 0;
        mReadCardOptV2 = null;
        ATR = null;
        LogUtil.i(TAG, "call java cleanUp end");
        return ReturnConstant.ETSTATUS_SUCCESS;
    }

    private final CheckCardCallbackV2 mCheckCardCallbackV2 = new CheckCardCallbackV2.Stub() {
        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {
            LogUtil.i("find Mag Card ");
        }

        @Override
        public void findICCard(String atr) throws RemoteException {
            ATR = atr;
            isCardFound.set(true);
            LogUtil.i("find ic card -- atr: " + atr);
            if (checkCardLatch != null) {
                checkCardLatch.countDown();
                checkCardLatch = null;
            }
        }

        @Override
        public void findRFCard(String uuid) throws RemoteException {
            // 多次调用检卡需要清空之前的数据
            byte[] eid_ATR = new byte[]{-128, 101, -94, 1, 49, 1, 61, 114, -42, 65};
            ATR = BytesUtil.bytesToHex(eid_ATR);
            isCardFound.set(true);
            LogUtil.i("find nfc card -- uuid : " + uuid);
            if (checkCardLatch != null) {
                checkCardLatch.countDown();
                checkCardLatch = null;
            }
        }

        @Override
        public void onError(int code, String msg) throws RemoteException {
            ATR = null;
            isCardFound.set(false);
            LogUtil.e("onError: ErrorCode:" + code + "  ErrorMessage: " + msg);
            checkCardErrorCode = code;
            if (checkCardLatch != null) {
                checkCardLatch.countDown();
                checkCardLatch = null;
            }
        }

        @Override
        public void findICCardEx(Bundle bundle) throws RemoteException {
            LogUtil.i("findICCardEx");
        }

        @Override
        public void findRFCardEx(Bundle bundle) throws RemoteException {
            LogUtil.i("findRFCardEx" + bundle2string(bundle));

        }

        @Override
        public void onErrorEx(Bundle bundle) throws RemoteException {
            LogUtil.e("onErrorEx:" + "cardType:" + bundle.getInt("cardType"));
            LogUtil.e("onErrorEx:" + "code:" + bundle.getInt("code"));
            LogUtil.e("onErrorEx:" + "message:" + bundle.getString("message"));
        }
    };

    public int connect() {
        LogUtil.i(TAG, "call java connect");
        if (!checkInit()) {
            LogUtil.e(TAG, "It has not been initialized successfully");
            return ReturnConstant.ETSTATUS_SC_CONNECT_FAILED;
        }
        if (checkCardLatch != null) {
            LogUtil.e(TAG, "Repeated call function");
            return ReturnConstant.ETSTATUS_SC_CONNECT_FAILED;
        }
        checkCardLatch = new CountDownLatch(1);
        try {
            LogUtil.i(TAG, "mReadCardOptV2 : " + mReadCardOptV2);
            mReadCardOptV2.checkCardEx(mCheckCardType, 8, 1, mCheckCardCallbackV2, 60);
            isCheckCardCalled.set(true);
            LogUtil.i(TAG, "await");
            boolean awaitResult = true;
            if (checkCardLatch != null) {
                //由于当前机制 这里可能出现NPE，场景为另一线程调用了disconnect ，通过 try catch 解决，
                awaitResult = checkCardLatch.await(61, TimeUnit.SECONDS);
            }
            if (isCardFound.get()) {
                return ReturnConstant.ETSTATUS_SUCCESS;
            }
            if (!awaitResult || checkCardErrorCode == AidlErrorCode.READ_CARD_TIMEOUT.getCode()) {
                LogUtil.e(TAG, "read card time out");
                return ReturnConstant.ETSTATUS_CONNECTION_TIMEOUT;
            }
            // NFC检卡失败
            if (checkCardErrorCode == AidlErrorCode.READ_CARD_FAIL_NFC.getCode()) {
                return ReturnConstant.ETSTATUS_NFC_CONN_ERROR;
            }
        } catch (Exception e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));
        }
        return ReturnConstant.ETSTATUS_SC_CONNECT_FAILED;
    }

    public static String bundle2string(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        String string = "Bundle{";
        for (String key : bundle.keySet()) {
            string += " " + key + " => " + bundle.get(key) + ";";
        }
        string += " }Bundle";
        return string;
    }

    public void disConnect() {
        LogUtil.i(TAG, "call java disconnect");
        // 检卡线程是否处于阻塞状态
        if (checkCardLatch != null) {
            isCardFound.set(false);
            checkCardLatch.countDown();
            checkCardLatch = null;
        }
        if (!checkInit()) {
            LogUtil.e(TAG, "It has not been initialized successfully");
            return;
        }
        if (!isCheckCardCalled.get()) {
            LogUtil.e(TAG, "connect has not been called");
            return;
        }
        try {
            mReadCardOptV2.cardOff(AidlConstants.CardType.IC.getValue());
            mReadCardOptV2.cardOff(AidlConstants.CardType.NFC.getValue());
            mReadCardOptV2.cancelCheckCard();
        } catch (Exception e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));
        }
        isCheckCardCalled.set(false);
        ATR = null;
        isCardFound.set(false);
        checkCardErrorCode = 0;
    }

    /**
     * TransmitApdu
     *
     * @param sendBuff      The data send to the card
     * @param interfaceType Smart card connection interface type
     *                      CONTACT_INTERFACE (1)  NFC_INTERFACE (2)
     * @return
     */
    public CardResult sendAPDU(byte[] sendBuff, int interfaceType) {
        LogUtil.i(TAG, "java sendAPDU --- APDU: ***" + " interfaceType: " + interfaceType);
        CardResult cardResult = new CardResult();
        // APDU交互前需要先寻到卡
        if (!isCardFound.get() || !checkInit()) {
            LogUtil.e(TAG, "isCardFound ： " + isCardFound.get());
            cardResult.setErrorCode(ReturnConstant.ETSTATUS_SMARTCARD_ERROR);
            return cardResult;
        }
        byte[] recvBuff = new byte[1024];
        int cardType = getCardType(interfaceType);
        if (cardType < 0) {
            cardResult.setErrorCode(ReturnConstant.ETSTATUS_INVALID_PARAMETER);
            return cardResult;
        }
        LogUtil.i(TAG, "cardType : " + (cardType == AidlConstants.CardType.IC.getValue() ? "IC" : "NFC"));
        try {
            // 该方法不建议使用timeout处理超时，原因：1. transmitApdu接口没有取消方法；2. 该接口正常情况下非常快，不存在超时情况；
            int result = mReadCardOptV2.transmitApdu(cardType, sendBuff, recvBuff);
            if (result >= 0) {
                // result >= 0 时表示 recvBuff 中有效数据的长度
                cardResult.setResultBytesArray(getResultBytesArray(result, recvBuff));
            }
            cardResult.setErrorCode(getErrorCode(result));
            LogUtil.i(TAG, "result = " + result + " resultByteArray = ****");
        } catch (Exception e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));
            cardResult.setErrorCode(ReturnConstant.ETSTATUS_SMARTCARD_ERROR);
        }
        return cardResult;
    }

    /**
     * Get cardType by interfaceType
     *
     * @param interfaceType Smart card connection interface type
     *                      CONTACT_INTERFACE (1)  NFC_INTERFACE (2)
     * @return CardType.IC or
     * CardType.NFC or
     * -1 : invalid interfaceType
     */
    private int getCardType(int interfaceType) {
        if (interfaceType == Constant.CONTACT_INTERFACE) {
            return AidlConstants.CardType.IC.getValue();
        } else if (interfaceType == Constant.NFC_INTERFACE) {
            return AidlConstants.CardType.NFC.getValue();
        }
        return -1;
    }

    /**
     * get ErrorCode by the result of transmitApdu
     *
     * @param result The result of transmitApdu
     * @return
     */
    private int getErrorCode(int result) {
        if (result >= 0) {
            return ReturnConstant.ETSTATUS_SUCCESS;
        }
        LogUtil.e(TAG, "The ErrorCode of transmitApdu : " + result);
        // L1错误码
        SPErrorCode errorCode = SPErrorCode.valueOf(result);
        switch (errorCode) {
            case CARD_ERR_PARAM:
            case SCI_ERR_T0_PARAM:
            case SCI_ERR_T1_PARAM:
                return ReturnConstant.ETSTATUS_INVALID_PARAMETER;
            case SMC_HAL_ERR_PARITY:
                return ReturnConstant.ETSTATUS_INVALID_DATA;
            case SCI_ERR_COMMU:
            case CLS_HAL_ERR_TIMEOUT:
            case CLS_HAL_ERR_FRAME:
            case CLS_HAL_ERR_PARITY:
            case CLS_HAL_ERR_COLL:
            case CLS_HAL_ERR_PROT:
            case CLS_HAL_ERR_CRC:
                return ReturnConstant.ETSTATUS_SEND_SVC_REQ_SC_ERROR;
            case CLS_ERR_IBLOCK_PROTOCOL:
            case CLS_ERR_RBLOCK_PROTOCOL:
            case CLS_ERR_SBLOCK_PROTOCOL:
                return ReturnConstant.ETSTATUS_INSUFFICIENT_BUFFER_LEN;
            default:
                return ReturnConstant.ETSTATUS_SMARTCARD_ERROR;
        }
    }

    /**
     * Cut out bytesArray of result length
     *
     * @param length   the effective length of recvBuff*
     * @param recvBuff
     * @return
     */
    private byte[] getResultBytesArray(int length, byte[] recvBuff) {
        byte[] resultBytesArray = new byte[length];
        System.arraycopy(recvBuff, 0, resultBytesArray, 0, length);
        return resultBytesArray;
    }

    /**
     * Get the Answer to Reset (ATR) value of a smartcard.
     *
     * @return
     */
    public byte[] getATRBytesArray() {
        LogUtil.i(TAG, "java getATR");
        if (ATR != null) {
            return BytesUtil.hexStr2Bytes(ATR);
        }
        return new byte[]{};
    }

    /**
     * 判断服务是否连接
     *
     * @return
     */
    private boolean checkInit() {
        return isServiceConnected.get() && mReadCardOptV2 != null;
    }
}
