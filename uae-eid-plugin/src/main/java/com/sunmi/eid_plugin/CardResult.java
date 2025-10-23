package com.sunmi.eid_plugin;

public class CardResult {
    private int errorCode;
    private byte[] resultBytesArray;

    public CardResult() {
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public byte[] getResultBytesArray() {
        return this.resultBytesArray;
    }

    public void setResultBytesArray(byte[] resultBytesArrray) {
        this.resultBytesArray = resultBytesArrray;
    }
}
