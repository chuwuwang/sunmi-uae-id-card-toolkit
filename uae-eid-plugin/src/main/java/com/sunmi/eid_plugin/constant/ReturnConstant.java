package com.sunmi.eid_plugin.constant;

/**
 * The return Code of Plugin_methods
 */
public class ReturnConstant {
    public static final int ETSTATUS_SUCCESS = 0;
    public static final int ETSTATUS_INVALID_PARAMETER = 2;             // One or more parameters to the function is invalid
    public static final int ETSTATUS_NOT_INITIALIZED = 3;               // Plugin not initialized
    public static final int ETSTATUS_INVALID_HANDLE = 5;                // Invalid handle value
    public static final int ETSTATUS_INVALID_DATA = 7;                  // Invalid data
    public static final int ETSTATUS_INSUFFICIENT_BUFFER_LEN = 20;      // Buffer provided is of insufficient length
    public static final int ETSTATUS_BLUTOOTH_NOT_SUPPORTED = 24;       // Bluetooth not supported
    public static final int ETSTATUS_BLUTOOTH_IS_OFF = 25;              // Bluetooth is off. Please check the settings
    public static final int ETSTATUS_NO_PAIRED_DEVICES = 26;            // No matching paired devices
    public static final int ETSTATUS_SMARTCARD_ERROR = 39;              // Smartcard operation failed
    public static final int ETSTATUS_FP_OPENDEVICE_ERROR = 49;          // Plugin could not connect to the associated fingerprint scanner
    public static final int ETSTATUS_CAPTURE_IMAGE_ERROR = 50;          // Failed to capture fingerprint image
    public static final int ETSTATUS_DEVICE_ENUM_FAILED = 51;           // Failed to enumerate the fingerprint devices
    public static final int ETSTATUS_LIST_READERS_ERROR = 52;           // Error in finding the connected reader names
    public static final int ETSTATUS_SC_CONNECT_FAILED = 53;            // Could not connect to the smartcard
    public static final int ETSTATUS_SC_DISCONNECT_FAILED = 54;         // Failed to disconnect the smartcard
    public static final int ETSTATUS_CONNECTION_TIMEOUT = 59;           // Device connection timed out
    public static final int ETSTATUS_ESTABLISH_RM_CTXT_ERROR = 62;      // Failed to establish smartcard resource manager context
    public static final int ETSTATUS_SEND_SVC_REQ_SC_ERROR = 64;        // Failed to send smartcard command
    public static final int ETSTATUS_SC_ATR_ERROR = 65;                 // Failed to get the ATR from the smartcard
    public static final int ETSTATUS_ERROR_GET_TEMPLATES_COUNT = 81;    // Failed to retrieve fingerprint templates count
    public static final int ETSTATUS_ERROR_RETRIEVING_TEMPLATE = 82;    // Failed to retrieve fingerprint template
    public static final int ETSTATUS_CAPTURE_ISO_ERROR = 236;           // Failed to capture fingerprint template
    public static final int ETSTATUS_NFC_TAG_INVALID = 250;             // Invalid NFC tag
    public static final int ETSTATUS_NFC_CONN_ERROR = 251;              // NFC connection error
    public static final int ETSTATUS_INVALID_PLUGIN_CONTEXT = 252;      // Invalid plugin context
    public static final int ETSTATUS_NO_CONN_USB_DEV = 253;             // No USB device connected
}
