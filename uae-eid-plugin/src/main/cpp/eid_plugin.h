//
// Created by SM2221 on 2022/11/11.
//
//接口定义 ： ID_Card_Toolkit_Plugin_Developer_Guide_v1_2_20190207.pdf

#ifndef EID_USDK_PLUGIN_EID_PLUGIN_H
#define EID_USDK_PLUGIN_EID_PLUGIN_H

extern "C"
int Plugin_Initialize(void *context, void *jenv_obj);

extern "C"
int Plugin_Cleanup(void *jenv_obj);

extern "C"
int Plugin_FreeMemory(void *buffer);

extern "C"
int Plugin_ListReadersEx(char **reader_list, unsigned int *num_bytes, void *jenv_obj);

extern "C"
int Plugin_Connect(char *reader, void **plugin_context, void *jenv_obj);

extern "C"
int Plugin_Disconnect(void *plugin_context, void *jenv_obj);

extern "C"
int Plugin_GetATR(void *plugin_context, unsigned char **atr_bytes, unsigned int *atr_len,
                  void *jenv_obj);
extern "C"
int Plugin_ExecuteCommand(void *plugin_context,
                          unsigned char *isocommand, unsigned int command_length,
                          unsigned char *out_buf, unsigned int *out_length,
                          int interface_type, void *jenv_obj);

extern "C"
int Plugin_SetNfcTag(void *jnfc_tag, void *jenv_obj);


#endif //EID_USDK_PLUGIN_EID_PLUGIN_H
