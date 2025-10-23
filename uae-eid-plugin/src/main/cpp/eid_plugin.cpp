//
// Created by SM2221 on 2022/11/11.
//

#include "eid_plugin.h"
#include <jni.h>
#include <android/log.h>
#include <cstdlib>
#include <cstring>

#define TAG "native_eid"

//如果存在宏 则输出debug级别log
#ifdef LOG_DEBUG
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__) // 定义LOGD类型
#else
#define LOGD(...)
#endif

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__) // 定义LOGI类型
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__) // 定义LOGW类型
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__) // 定义LOGE类型
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,TAG ,__VA_ARGS__) // 定义LOGF类型

#define ETSTATUS_SUCCESS 0 // Successfully
#define ETSTATUS_INVALID_PARAMETER 2 // One or more parameters to the function is invalid
#define ETSTATUS_LIST_READERS_ERROR 52 // Error in finding the connected reader names
#define ETSTATUS_ESTABLISH_RM_CTXT_ERROR 62 // Failed to establish smartcard resource manager context
#define ETSTATUS_INVALID_PLUGIN_CONTEXT 252 // Invalid eid_plugin context

static jobject readerInstanceRef = NULL;
static jmethodID methodID_connect = NULL;
static jmethodID methodID_disconnect = NULL;
static jmethodID methodID_sendAPDU = NULL;
static jmethodID methodID_getATR = NULL;
static jmethodID methodID_getListReader = NULL;
static jmethodID methodID_cleanup = NULL;
static jmethodID methodID_setNfcTag = NULL;

//CardResult errorCode
static jfieldID jfieldID_CardResult_error_code = NULL;
//CardResult resultBytesArray
static jfieldID jfieldID_CardResult_data_byte = NULL;


/**
 * The Plugin_Initialize method is called by the Toolkit to initialize the eid_plugin.
 * This function is invoked by the Toolkit immediately after the module shared library is loaded into the process.
 * @param context
 * @param jenv_obj
 * @return
 */
extern "C"
int Plugin_Initialize(void *context, void *jenv_obj) {
    LOGD("Plugin_Initialize");
    JNIEnv *env = static_cast<JNIEnv *>(jenv_obj);
    jobject jcontext = static_cast<jobject>(context);
    //context 不合法
    if (context == NULL) {
        LOGE("ETSTATUS_INVALID_PLUGIN_CONTEXT");
        return ETSTATUS_INVALID_PLUGIN_CONTEXT;
    }
    //获取 SunmiReader class
    jclass readClazz = env->FindClass("com/sunmi/eid_plugin/SunmiReaderDispatch");
    jclass card_result_class = env->FindClass("com/sunmi/eid_plugin/CardResult");
    if (readClazz == NULL || card_result_class == NULL) {
        LOGE("find reader or cardResult error");
        return ETSTATUS_INVALID_PARAMETER;
    }
    //获取 initPlugin method id
    jmethodID methodID_initPlugin = env->GetMethodID(readClazz, "initPlugin",
                                                     "(Ljava/lang/Object;)I");
    if (methodID_initPlugin == NULL) {
        LOGE("find reader methodID_initPlugin error");
        return ETSTATUS_INVALID_PARAMETER;
    }
    //获取 SunmiReader 实例
    jmethodID readConstructor = env->GetMethodID(readClazz, "<init>", "()V");

    jobject readerInstance = env->NewObject(readClazz, readConstructor);

    // 创建一个全局引用
    readerInstanceRef = env->NewGlobalRef(readerInstance);

    //调用 SunmiReader initPlugin 方法
    jint initResult = env->CallIntMethod(readerInstanceRef, methodID_initPlugin, jcontext);
    if (initResult == ETSTATUS_SUCCESS) {
        methodID_connect = env->GetMethodID(readClazz, "connect", "(Ljava/lang/String;)I");
        methodID_disconnect = env->GetMethodID(readClazz, "disconnect", "()V");
        methodID_sendAPDU = env->GetMethodID(readClazz, "sendAPDU",
                                             "([BI)Lcom/sunmi/eid_plugin/CardResult;");
        methodID_getATR = env->GetMethodID(readClazz, "getATR", "()[B");
        methodID_getListReader = env->GetMethodID(readClazz, "getListReader",
                                                  "()[Ljava/lang/String;");
        methodID_cleanup = env->GetMethodID(readClazz, "cleanUp", "()I");
        methodID_setNfcTag = env->GetMethodID(readClazz, "setNfcTag", "(Ljava/lang/Object;)I");

        jfieldID_CardResult_error_code = env->GetFieldID(card_result_class, "errorCode", "I");
        jfieldID_CardResult_data_byte = env->GetFieldID(card_result_class, "resultBytesArray",
                                                        "[B");
    } else {
        env->DeleteGlobalRef(readerInstanceRef);
        readerInstanceRef = NULL;
    }
    return initResult;
}

/**
 * The Plugin_Cleanup method is called by the ID Card Toolkit to clean up the eid_plugin module.
 * This function is invoked before the module is unloaded from the application address space.
 * @param jenv_obj
 * @return
 */
extern "C"
int Plugin_Cleanup(void *jenv_obj) {
    LOGE("Plugin_Cleanup");
    if (jenv_obj == NULL || readerInstanceRef == NULL) {
        return ETSTATUS_INVALID_PARAMETER;
    }
    JNIEnv *env = static_cast<JNIEnv *>(jenv_obj);
    jint cleanUpResult = env->CallIntMethod(readerInstanceRef, methodID_cleanup);
    LOGE("Plugin_Cleanup:%d", cleanUpResult);
//    env->DeleteGlobalRef(readerInstanceRef);
//    readerInstanceRef = NULL;
    return cleanUpResult;
}


/**
 * The Plugin_FreeMemory method is called to free memory buffers allocated by the eid_plugin
 * and returned to the Toolkit.
 * @param buffer Pointer to the buffer returned by a previous call to a eid_plugin method.
 *      Buffers allocated by a eid_plugin are expected to be freed by the eid_plugin itself.
 * @return
 */
extern "C"
int Plugin_FreeMemory(void *buffer) {
    LOGD("Plugin_FreeMemory");
    free(buffer);
    return ETSTATUS_SUCCESS;
}

/**
 * The Plugin_ListReaders method is called to get the list of Smart Card readers connected.
 * @param reader_list If it’s a single reader, append this buffer with two null terminations, which indicates it’s the only reader in the list.
 * @param num_bytes This parameter indicates the actual length of the multi string returned in the reader_list.
 * @param env JNIEnv
 * @return
 */
extern "C"
int Plugin_ListReadersEx(char **reader_list, unsigned int *num_bytes, void *jenv_obj) {
    LOGD("Plugin_ListReadersEx");
    if (jenv_obj == NULL || readerInstanceRef == NULL) {
        return ETSTATUS_INVALID_PARAMETER;
    }
    JNIEnv *env = static_cast<JNIEnv *>(jenv_obj);
    //调用 SunmiReader getListReader 方法 返回 string array类型
    jobjectArray javaReaderList = (jobjectArray) env->CallObjectMethod(readerInstanceRef,
                                                                       methodID_getListReader);
    //获取 array 长度
    jsize listSize = env->GetArrayLength(javaReaderList);

    int total = 0;
    //遍历计算需要申请的内存大小
    for (jint i = 0; i < listSize; i++) {
        jstring readerString = (jstring) env->GetObjectArrayElement(javaReaderList, i);
        const char *str = env->GetStringUTFChars(readerString, 0);
        total += strlen(str);
        env->ReleaseStringUTFChars(readerString, str);
    }
    //创建指针
    char *tmp_reader = (char *) malloc(total * sizeof(char) + listSize + 1);
    memset(tmp_reader, 0, total * sizeof(char) + listSize + 1);
    int offset = 0;
    for (jint i = 0; i < listSize; i++) {
        jstring readerString = (jstring) env->GetObjectArrayElement(javaReaderList, i);
        const char *str = env->GetStringUTFChars(readerString, 0);
        memcpy(tmp_reader + offset, str, strlen(str));
        offset = strlen(str) + 1;
        env->ReleaseStringUTFChars(readerString, str);
    }

    // todo native层可以直接返回,无须调用java？
//    tmp_reader = "SunmiReader\0adadada\0\0";

    *reader_list = tmp_reader;
    *num_bytes = total;
    return ETSTATUS_SUCCESS;
}

/**
 * The Plugin_Connect method is called by the ID Card Toolkit to establish connection to
 * the smartcard in the specific reader identified by the reader name.
 * @param reader
 * @param plugin_context
 * @param jenv_obj
 * @return
 */
extern "C"
int Plugin_Connect(char *reader, void **plugin_context, void *jenv_obj) {
    LOGD("Plugin_Connect - %s", reader);
    if (jenv_obj == NULL || readerInstanceRef == NULL) {
        return ETSTATUS_INVALID_PARAMETER;
    }
    JNIEnv *env = static_cast<JNIEnv *>(jenv_obj);
    jstring readerString = env->NewStringUTF(reader);
    jint connectResult = env->CallIntMethod(readerInstanceRef, methodID_connect, readerString);
    env->DeleteLocalRef(readerString);
    return connectResult;
}

/**
 * The Plugin_Disconnect method is called by the ID Card Toolkit to disconnect an already established
 * connection to smartcard from a previous call to Plugin_Connect.
 * @param plugin_context
 * @param jenv_obj
 * @return
 */
extern "C"
int Plugin_Disconnect(void *plugin_context, void *jenv_obj) {
    LOGD("Plugin_Disconnect");
    if (jenv_obj == NULL || readerInstanceRef == NULL) {
        return ETSTATUS_INVALID_PARAMETER;
    }
    JNIEnv *env = static_cast<JNIEnv *>(jenv_obj);
    env->CallVoidMethod(readerInstanceRef, methodID_disconnect);
    return ETSTATUS_SUCCESS;
}

/**
 * The Plugin_GetATR method is called by the ID Card Toolkit to get the Answer to Reset (ATR) value of a smartcard.
 * @param plugin_context
 * @param atr_bytes [out] Buffer containing the ATR bytes. This buffer should be allocated by the plugin and the ID Card Toolkit will release the buffer with a call to Plugin_FreeMemory.
 * @param atr_len [out] Number of bytes returned in atr_bytes.
 * @param jenv_obj
 * @return
 */
extern "C"
int Plugin_GetATR(void *plugin_context, unsigned char **atr_bytes, unsigned int *atr_len,
                  void *jenv_obj) {
    LOGD("Plugin_GetATR");
    if (jenv_obj == NULL || readerInstanceRef == NULL) {
        return ETSTATUS_INVALID_PARAMETER;
    }
    JNIEnv *env = static_cast<JNIEnv *>(jenv_obj);

    jbyteArray atr = (jbyteArray) env->CallObjectMethod(readerInstanceRef, methodID_getATR);
    int atrSize = env->GetArrayLength(atr);
    LOGD("atrSize:%d", atrSize);
    jbyte *bytes = env->GetByteArrayElements(atr, 0);

    unsigned char *result = (unsigned char *) malloc(atrSize * sizeof(unsigned char) + 1);
    memset(result, 0, atrSize + 1);
    memcpy(result, bytes, atrSize);
    LOGD("atr:%s", result);
    *atr_bytes = result;
    *atr_len = atrSize;
    env->ReleaseByteArrayElements(atr, bytes, 0);
    return ETSTATUS_SUCCESS;
}

/**
 * The Plugin_ExecuteCommand method is called by the ID Card Toolkit to
 * execute APDU commands for accessing data from smartcard.
 * @param plugin_context
 * @param isocommand [in] Smartcard command in ISO 7816 format
 * @param command_length [in] Length of the smartcard command
 * @param out_buf [out] Response buffer is provided by the Toolkit. Plugins need to copy the smartcard response to this buffer.
 * @param out_length [out] Length of the smartcard response copied to the out buffer.
 * @param interface_type  [in] Smart card connection interface type. One of the following vales
 *          - CONTACT_INTERFACE (1)
 *          - NFC_INTERFACE (2)
 *
 * @param jenv_obj
 * @return
 */
extern "C"
int Plugin_ExecuteCommand(void *plugin_context,
                          unsigned char *isocommand, unsigned int command_length,
                          unsigned char *out_buf, unsigned int *out_length,
                          int interface_type, void *jenv_obj) {
    LOGD("Plugin_ExecuteCommand:isocommand:%s,command_length:%d", isocommand, command_length);
    if (jenv_obj == NULL || readerInstanceRef == NULL) {
        return ETSTATUS_INVALID_PARAMETER;
    }
    JNIEnv *env = static_cast<JNIEnv *>(jenv_obj);
    //将apdu指令转换成jbyteArray类型
    jbyte *cmd = (jbyte *) isocommand;
    jbyteArray command = env->NewByteArray(command_length);
    env->SetByteArrayRegion(command, 0, command_length, cmd);

    //将apdu指令透传到java层并获取返回结果
    jobject apduData = env->CallObjectMethod(readerInstanceRef, methodID_sendAPDU, command,
                                             interface_type);

    //解析errorCode
    jint errorCode = env->GetIntField(apduData, jfieldID_CardResult_error_code);
    LOGD("errorCode:%d", errorCode);
    //code不为0，直接返回失败
    if (errorCode != ETSTATUS_SUCCESS) {
        LOGE("error:%d", errorCode);
        return errorCode;
    }
    //解析获取到的结果数据
    jbyteArray resultData = (jbyteArray) env->GetObjectField(apduData,
                                                             jfieldID_CardResult_data_byte);
    int resultSize = env->GetArrayLength(resultData);
    LOGD("resultSize:%d", resultSize);
    jbyte *bytes = env->GetByteArrayElements(resultData, 0);

    //将从java层获取到的byte数组复制到out_buf中、长度复制到out_length中
    memcpy(out_buf, bytes, resultSize);
    LOGD("result:%s", out_buf);
    *out_length = resultSize;
    //释放 jbyteArray
    env->ReleaseByteArrayElements(resultData, bytes, 0);

    return ETSTATUS_SUCCESS;
}

/**
 * The Plugin_SetNfcTag method is called by the ID Card Toolkit to set the NFC tag
 * received by an Android application upon the tapping of ID card on the NFC reader.
 * @param jnfc_tag
 * @param jenv_obj
 * @return
 */
extern "C"
int Plugin_SetNfcTag(void *jnfc_tag, void *jenv_obj) {
    if (jenv_obj == NULL || readerInstanceRef == NULL) {
        return ETSTATUS_INVALID_PARAMETER;
    }
    JNIEnv *env = static_cast<JNIEnv *>(jenv_obj);
    jobject tag = static_cast<jobject>(jnfc_tag);
    return env->CallIntMethod(readerInstanceRef, methodID_setNfcTag, tag);
}

