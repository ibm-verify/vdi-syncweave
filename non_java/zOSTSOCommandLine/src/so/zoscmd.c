#include "zoscmd.h"
#include "util.h"
#include <atbpbc.h> /* APPC native Protocol Boundary Interface - C */
#include <atbctc.h> /* Additional, extended interfaces - c */
#include "atbcmc.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define APPC_CONV_ID_LEN      8
#define RECEIVE_BUFFER_SIZE   4096
#define CMD_TRANSACTION_NAME  "        "
#define PARM_PARTNERTP_ID     com_ibm_di_fc_zOSTSOCommandLine_PARM_PARTNERTP_ID /*0*/
#define PARM_DEST_LU_NAME_ID  com_ibm_di_fc_zOSTSOCommandLine_PARM_DEST_LU_NAME_ID /*1*/
#define PARM_SRC_LU_NAME_ID   com_ibm_di_fc_zOSTSOCommandLine_PARM_SRC_LU_NAME_ID /*2*/
#define PARM_MODE_NAME_ID     com_ibm_di_fc_zOSTSOCommandLine_PARM_MODE_NAME_ID /*3*/
#define PARM_USER_ID_ID       com_ibm_di_fc_zOSTSOCommandLine_PARM_USER_ID_ID /*4*/
#define PARM_USER_PSW_ID      com_ibm_di_fc_zOSTSOCommandLine_PARM_USER_PSW_ID /*5*/
#define METHOD_SIGN           "([BII)V"
#define METHOD_NAME           "write"
#define THIS_LOGMSG_SIGN      "(Ljava/lang/String;)V"
#define THIS_LOGMSG           "logmsg"
#define THROWABLE_GET_MESSAGE "getMessage"
#define THROWABLE_GETMSG_SIGN "()Ljava/lang/String;"
#define THROWABLE_PRINTSTACKTRACE       "printStackTrace"
#define THROWABLE_PRINTSTACKTRACE_SIGN  "()V"
#define NATIVE_LIB            "nativelib"

static jmethodID GetJMethodID(JNIEnv *jEnv, jobject aThis, char* aMethodName, char* aMethodSign);
static void AppendToLog(JNIEnv *jEnv, jobject aThis, jmethodID jmID, char *buffer);
void LogMsgStr(JNIEnv *jEnv, jobject aThis, char *str1, char *str2);


/**********************************************************
 * 
 *
 *********************************************************/
char* GetJavaExceptionMessage(JNIEnv *jEnv, jobject _throw) 
{
    jstring     errorMsg      = NULL;
    char        *nativeErrMsg = NULL;
    jmethodID   jmID          = GetJMethodID(jEnv, _throw, 
                                            THROWABLE_GET_MESSAGE, 
                                            THROWABLE_GETMSG_SIGN);
    if (jmID==0) {
        return NULL;
    }
    
    errorMsg = (*jEnv)->CallObjectMethod(jEnv, _throw, jmID);
    return getOSCharArray(jEnv, errorMsg);
}

void DumpJavaException(JNIEnv *jEnv, jobject _throw, char *customMessage)
{
    jstring     errorMsg      = NULL;
    char        *nativeErrMsg = NULL;
    jmethodID   jmID          = NULL;

    nativeErrMsg = GetJavaExceptionMessage(jEnv, _throw);
    printf("Source Error: %s\nError Message: %s\n",customMessage, nativeErrMsg);
    free(nativeErrMsg);

    jmID = GetJMethodID(jEnv, _throw,
                        THROWABLE_PRINTSTACKTRACE, 
                        THROWABLE_PRINTSTACKTRACE_SIGN);
    if (jmID==0) {
        printf("Cannot print stack trace in DumpJavaException: printStackTrace() method id not found.");
        return;
    }
    (*jEnv)->CallVoidMethod(jEnv, _throw, jmID);
}


/**********************************************************
 * 
 *
 *********************************************************/
static jmethodID GetJMethodID(JNIEnv *jEnv, jobject aThis, char* aMethodName, char* aMethodSign) 
{
    char *methodName = NULL;
    char *methodSign = NULL;
    int   convLen    = 0;
    jmethodID jmID   = NULL;   
    jthrowable exc   = NULL;

    jclass thisClass = (*jEnv)->GetObjectClass(jEnv, aThis);
    exc = (*jEnv)->ExceptionOccurred(jEnv);
    if (exc) {
        DumpJavaException(jEnv, exc, "GetJMethodID: Could not get object class");
        (*jEnv)->ExceptionClear(jEnv);
        return 0;
    }

    convLen = strlen(aMethodName);
    methodName = calloc(convLen+1, sizeof(char));
    if (methodName==NULL) {
        LogMsgStr(jEnv, aThis, "GetJMethodID: Could not allocate memory for method name.", NULL);
        return 0;
    }
    StringConvert( aMethodName, methodName );

    convLen = strlen(aMethodSign);
    methodSign = (char*)calloc(convLen+1, sizeof(char));
    if (methodSign==NULL) {
        LogMsgStr(jEnv, aThis, "GetJMethodID: Could not allocate memory for method signature.", NULL);
        free(methodName);
        return 0;
    }
    StringConvert( aMethodSign, methodSign );

    jmID = (*jEnv)->GetMethodID( jEnv, thisClass, methodName, methodSign );
    exc = (*jEnv)->ExceptionOccurred(jEnv);
    if (exc!=NULL) {
        char dumpMsg[1024] = {"\0"};
        sprintf(dumpMsg, "GetJMethodID: Could not get %s method ID with signature %s.", aMethodName, aMethodSign);
        DumpJavaException(jEnv, exc, dumpMsg);
        (*jEnv)->ExceptionClear(jEnv);
        jmID = 0;
    }

    free(methodName);
    free(methodSign);
    return jmID;
}


/**********************************************************
 * 
 *
 *********************************************************/
void LogMsgStr(JNIEnv *jEnv, jobject aThis, char *str1, char *str2) 
{
    char buffer[1024] = {"\0"};
    jmethodID jmID   = NULL;

    if( str2==NULL ) {
        sprintf(buffer,str1);
    }
    else {
        sprintf(buffer,str1,str2);
    }

    jmID = GetJMethodID(jEnv, aThis, THIS_LOGMSG, THIS_LOGMSG_SIGN);
    if ( jmID!=0 ) {
        AppendToLog(jEnv, aThis, jmID, buffer);    
    }
}


/**********************************************************
 * 
 *
 *********************************************************/
static void LogMsgInt(JNIEnv *jEnv, jobject aThis, char *frmtMsg, int errCode)
{
    char buff[1024] = {"\0"};
    sprintf(buff, frmtMsg, errCode);
    LogMsgStr(jEnv, aThis, buff, NULL);
}


/**********************************************************
 * 
 *
 *********************************************************/
static void AppendToLog(JNIEnv *jEnv, jobject aThis, jmethodID jmID, char *buffer) 
{
    char asciiBuffer[1024] = {"\0"};
    jstring jMsg;
    jthrowable exc = NULL;
    
    if ( jmID!=0 ) {
        char _debug[1100] = {"\0"};
        sprintf(_debug,"[%s] %s", NATIVE_LIB, buffer);
        StringConvert(_debug, asciiBuffer);
        jMsg = (*jEnv)->NewStringUTF(jEnv, asciiBuffer);
        exc = (*jEnv)->ExceptionOccurred(jEnv);
        if( !exc ) {
            (*jEnv)->CallVoidMethod( jEnv, aThis, jmID, jMsg );
            exc = (*jEnv)->ExceptionOccurred(jEnv);
            if( exc ) {
                DumpJavaException(jEnv, exc, "Error executing Java method in AppendToLog");
                (*jEnv)->ExceptionClear(jEnv);
            }
        }
        else {
            DumpJavaException(jEnv, exc, "Could not allocate new string in AppendToLog");
            (*jEnv)->ExceptionClear(jEnv);
        }
    }
    else {
        printf(buffer);
    }
}


/**********************************************************
 * 
 *
 *********************************************************/
void logAPPCError(char *conv_ID, jobject aThis, JNIEnv *jEnv) {
  char      service[8+1];
  long int  serv_rs;
  long int  msg_len;
  char      msg[256+1];
  long int  prd_set_len;
  char      prd_set[256+1];
  long int  err_log_len;
  char      err_log[512+1];
  long int  ees_reason;
  long int  ees_rc;
  char buffer[1024] = {"\0"};
  jmethodID jmID    = NULL;

  msg_len = prd_set_len = err_log_len = 0;
  strcpy( service, "        " );

  atbees3( conv_ID,        /* i */
           service,        /* o 8 chars */
           &serv_rs,       /* o long int */
           &msg_len,       /* o long int */
           msg,            /* o char * the message */
           &prd_set_len,   /* o int */
           prd_set,        /* o product set message */
           &err_log_len,   /* o int */
           err_log,        /* o char the message */
           &ees_reason,    /* o reason for ees3 */
           &ees_rc );      /* o rc for ees3 */

  if (ees_rc != 0)  {
    LogMsgInt(jEnv, aThis, "Could not extract APPC error information, rc=%d", ees_rc );
    return;
  }
  
  service[8] = '\0';
  msg[msg_len] = '\0';
  prd_set[prd_set_len] = '\0';
  err_log[err_log_len] = '\0';

  jmID = GetJMethodID(jEnv, aThis, THIS_LOGMSG, THIS_LOGMSG_SIGN);    

  sprintf(buffer, "RC: %d, EES3 RS: %d", ees_rc, ees_reason);
  AppendToLog(jEnv, aThis, jmID, buffer);

  sprintf( buffer, "Service Reason: %d", serv_rs );
  AppendToLog(jEnv, aThis, jmID, buffer);

  sprintf(buffer, "Service: %s", service );
  AppendToLog(jEnv, aThis, jmID, buffer);

  sprintf(buffer, "Message: %s", msg ); 
  AppendToLog(jEnv, aThis, jmID, buffer);

  sprintf(buffer, "Product set: %s", prd_set ); 
  AppendToLog(jEnv, aThis, jmID, buffer);

  sprintf(buffer, "Error log: %s", err_log );    
  AppendToLog(jEnv, aThis, jmID, buffer);    
}


/**********************************************************
 * 
 *
 *********************************************************/
static CM_INT32 InitAllocateConversation(JNIEnv *jEnv, jobject _this, char **convID, char** appcParams) {
    CM_INT32 retCode = CALL_FAIL;
    long int    Conv_SyncLvl;          /* Sync level */
    long int    TP_Name_Len;           /* appc tp name length */
    long int    Sec_Type;              /* Same Security */
    char   Conv_FQN[17+1] = "                 "; /* fully qualified network name */
    char   Conv_Mode[8+1] = "        "; /* mode name */
    char   User_ID[10+1]  = {"\0"};     /* surrogate user id */
    char   User_PWD[10+1] = {"\0"};     /* surrogate user id */
    char   Conv_Correlator[8+1]= {"\0"};/* Conversation correlator   */
    char   TP_Name[64+1] = {"\0"};      /*appc transaction*/
    char   alt_TP_Name[64+1]= {"\0"};
    char   source_lu[17+1]= {"\0"};     /* APPC source LU name */
    char   dest_lu[17+1]= {"\0"};       /* APPC destination LU name */
    char   modename[8+1]= {"\0"};       /* APPC mode name */

    long int    Conv_Type = ATB_MAPPED_CONVERSATION;
    long int    Conv_Notify_Type = ATB_NOTIFY_TYPE_NONE;
    long int    Conv_Return_Control = ATB_WHEN_SESSION_ALLOCATED; /* return control when alloc'd*/
    long int    Sync_Lvl0 = ATB_NONE;    /* Sync level 0 */
    long int    Zero_Timeout = 0;        /* no timeout value */


  /* Get the source LU name */
  memset( source_lu, ' ', sizeof( source_lu )-1 );
  if (appcParams[PARM_SRC_LU_NAME_ID]!=NULL) {
    strcpy(source_lu, appcParams[PARM_SRC_LU_NAME_ID]);
  }    
  if ( ' ' == source_lu[0] )
    LogMsgStr(jEnv, _this, "APPC/MVS default source LU will be used.", NULL );
  else
    LogMsgStr(jEnv, _this, "APPC/MVS source LU: %s", source_lu );
  

  /* Get the destination LU name */
  memset( dest_lu, ' ', sizeof( dest_lu )-1 );
  if (appcParams[PARM_DEST_LU_NAME_ID]!=NULL) {
    strcpy(dest_lu, appcParams[PARM_DEST_LU_NAME_ID]);
  }
  if ( ' ' == dest_lu[0] )
    LogMsgStr(jEnv, _this,"APPC/MVS default destination LU will be used.", NULL);
  else
    LogMsgStr(jEnv, _this, "APPC/MVS destination LU: %s", dest_lu );
 

  /* Get the mode name */
  memset( modename, ' ', sizeof( modename )-1 );
  if (appcParams[PARM_MODE_NAME_ID]!=NULL) {
    strcpy(modename, appcParams[PARM_MODE_NAME_ID]); 
  }
  if ( ' ' == modename[0] )
    LogMsgStr(jEnv, _this, "APPC/MVS default mode will be used.",NULL );
  else
    LogMsgStr(jEnv, _this, "APPC/MVS mode: %s", modename );
  

  /* Get the TP name */
  if (appcParams[PARM_PARTNERTP_ID]==NULL) { 
      LogMsgStr(jEnv, _this, "Cannot allocate conversation: TP name is NULL.", NULL);
      return CM_UNSUCCESSFUL;
  }
  strcpy(TP_Name, appcParams[PARM_PARTNERTP_ID]);
  TP_Name_Len = strlen( TP_Name );
  LogMsgStr(jEnv, _this, "TP name: %s", TP_Name);


  /* Get the User ID */
  memset( User_ID, ' ' , sizeof(User_ID)-1);
  if (appcParams[PARM_USER_ID_ID]!=NULL) { 
      strcpy(User_ID, appcParams[PARM_USER_ID_ID]);
  }
  LogMsgStr(jEnv, _this, "User ID: %s", User_ID);

  memset(User_PWD, ' ' , sizeof(User_PWD)-1);
  if (appcParams[PARM_USER_PSW_ID]!=NULL) {
      strcpy(User_PWD,appcParams[PARM_USER_PSW_ID]);
  }

  Sec_Type = (' ' == User_ID[0])?ATB_SECURITY_SAME:ATB_SECURITY_PROGRAM;
  

  atbalc5( &Conv_Type,                /* I Conversation Type */
           CMD_TRANSACTION_NAME,      /* I Sym Dest name Blank */
           dest_lu,                   /* I Partner LU Blank */
           modename,                  /* I Mode name blank */
           &TP_Name_Len,              /* I Len of tp name */
           TP_Name,                   /* I TP name */
           &Conv_Return_Control,      /* I return when alloc'd */
           &Sync_Lvl0,                /* I Synclevel 0 */
           &Sec_Type,                 /* I security type */
           User_ID,                   /* I Identity of requestor */
           User_PWD,                  /* I no PW */
           "          ",              /* I no Group */
           "\0",                      /* I no token */
           *convID,                   /* O Conversation ID */
    (char*)&Conv_Notify_Type,         /* I Notify Type */
           "\0\0\0\0\0\0\0\0",        /* I 8 bytes zeros */
           source_lu,                 /* I Provided local LU name */
           &Zero_Timeout,             /* I timeout */
           &retCode );                /* O return code */

   if( retCode!=ATB_OK ) {
        LogMsgInt(jEnv, _this, "Could not allocate APPC conversation: error in atbalc5: %d", retCode);
   }
   
  return retCode;
}



/*
 * Class:     com_ibm_di_fc_zOSTSOCommandLine
 * Method:    initConversation
 * Signature: ([C[Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_ibm_di_fc_zOSTSOCommandLine_initConversation
                       (JNIEnv *jEnv, jobject jObj, jbyteArray jConvID, 
                       jobjectArray jParmArray, jbyteArray jEncoding)
{

    char     convID[APPC_CONV_ID_LEN] = {"\0"}; 
    char     *pConvID = convID;
    jbyte    *outArr = NULL;
    jboolean  isCopy = JNI_FALSE;
    CM_INT32  return_code = CALL_FAIL;
    char    **parmArray = NULL;
     
    getCharset(jEnv, jEncoding);

    if( (parmArray = copyJStringArray2NativeArray( jEnv,jParmArray ))!=NULL ) {              
         if( (return_code=InitAllocateConversation(jEnv, jObj, &pConvID, parmArray))==ATB_OK ) {                  
             int i=0;
             outArr = (*jEnv)->GetByteArrayElements(jEnv, jConvID, &isCopy );                  
             memcpy(outArr, convID, APPC_CONV_ID_LEN);
             (*jEnv)->ReleaseByteArrayElements(jEnv, jConvID, outArr, 0);
         } 
         else {
             logAPPCError( (char*)convID , jObj, jEnv);
         }
         ReleaseNativeCharArray(jEnv, jParmArray, parmArray);
    }
    else {
        LogMsgStr(jEnv, jObj, "initConversation: Could not allocate memory for APPC parameters", NULL );
    }

    return return_code;     
}



/*
 * Class:     com_ibm_di_fc_zOSTSOCommandLine
 * Method:    executeCommand
 * Signature: ([CLjava/lang/String;Ljava/lang/StringBuffer;)I
 */
JNIEXPORT jint JNICALL Java_com_ibm_di_fc_zOSTSOCommandLine_executeCommand
  (JNIEnv *jEnv, jobject jObj, jbyteArray jConvID, jstring jcmd, jobject strBuff)
{
     char             convID[APPC_CONV_ID_LEN]= {"\0"}; 
     char             *cmd              = NULL;
     int              iCounter          = 0;
     jboolean         isCopy            = JNI_FALSE;
     CM_INT32         buflen            = 0;
     char             buffer[RECEIVE_BUFFER_SIZE]      = {"\0"};
     char             asciiBuffer[RECEIVE_BUFFER_SIZE] = {"\0"};
     
     long int    Conv_Send_Len     = 0;       /* length of send buffer */
     long int    Conv_Recv_Len     = 0;       /* length of receive buffer */
     long int    Conv_RTS_Rcvd     = 0;       /* Request to transmit received */
     long int    Conv_Data_Rcvd    = 0;       /* status received indicator */
     long int    Conv_Status_Rcvd  = 0;       /* status received indicator */

     long int    Send_Type         = ATB_SEND_AND_PREP_TO_RECEIVE;
     long int    Conv_ALET         = 0;               /* 0 same address space     */
     long int    Conv_Notify_Type  = ATB_NOTIFY_TYPE_NONE;
     long int    Conv_Fill         = ATB_FILL_BUFFER; /* unused but required */
     long int    return_code       = 0;               /* Return code */
     jbyte     *jIDValue           = NULL;
     jclass     strBuffer          = NULL;
     jmethodID  jmID               = 0;
     jbyteArray jBuff              = NULL;     
     int         convDelta         = 0;  
     int         convLen           = 0;
     jbyte     *jArrayValue        = NULL;
     jthrowable exc                = NULL;

     jIDValue=(*jEnv)->GetByteArrayElements(jEnv, jConvID, &isCopy );
     memcpy(convID, jIDValue, APPC_CONV_ID_LEN );
     
     if( (cmd=(char*)getOSCharArray(jEnv,jcmd))!=NULL ) {
          buflen = strlen((char*)cmd);          

        /******************************************************************/
        /* Sending the command to the REXX script for execution                                                    */
        /******************************************************************/
        atbsend( convID,               /* i */
                 &Send_Type,           /* i */
                 &buflen,              /* i/o */
                 &Conv_ALET,           /* i */
                 cmd,                  /* o */
                 &Conv_RTS_Rcvd,       /* o */
          (char*)&Conv_Notify_Type,    /* i */
                 &return_code );       /* o */

         free(cmd);

         if (return_code!=ATB_OK) {
             LogMsgInt(jEnv, jObj, "Error %d when sending data", return_code);
             logAPPCError( convID, jObj, jEnv );
         }
         else {
             Conv_Recv_Len      = RECORD_LENGTH;
             Conv_Status_Rcvd   = ATB_NO_STATUS_RECEIVED;
             strBuffer          = (*jEnv)->GetObjectClass(jEnv, strBuff);
             jmID               = GetJMethodID(jEnv, strBuff, METHOD_NAME, METHOD_SIGN);             
             jBuff              = (*jEnv)->NewByteArray( jEnv, Conv_Recv_Len );             

             /**********************************************************************/
             /* receive the data from the partner                                  */
             /**********************************************************************/
              while (return_code == ATB_OK & Conv_Status_Rcvd == ATB_NO_STATUS_RECEIVED)  {
                     
                     atbrcvw( convID,                   /* i */
                             &Conv_Fill,                /* i */
                             &Conv_Recv_Len,            /* i/o */
                             &Conv_ALET,                /* i */
                              buffer,                   /* o */
                             &Conv_Status_Rcvd,         /* o */
                             &Conv_Data_Rcvd,           /* o */
                             &Conv_RTS_Rcvd,            /* o */
                      (char*)&Conv_Notify_Type,         /* i */
                             &return_code );            /* o */

                    if ( return_code==ATB_OK ) {
                        buffer[Conv_Recv_Len] = '\0';
                        if( jmID!=0 ) {
                            jArrayValue = (*jEnv)->GetByteArrayElements( jEnv, jBuff, NULL );
                            memcpy( jArrayValue,  buffer, Conv_Recv_Len*sizeof(char));  
                            (*jEnv)->ReleaseByteArrayElements(jEnv, jBuff, jArrayValue, 0);
                            (*jEnv)->CallVoidMethod( jEnv, strBuff, jmID, jBuff, 0, Conv_Recv_Len);                            
                            exc = (*jEnv)->ExceptionOccurred(jEnv);
                            if( exc ) {
                                LogMsgStr(jEnv,jObj, "Error writting APPC message to byte array: %s",
                                       GetJavaExceptionMessage(jEnv, exc));
                                (*jEnv)->ExceptionClear(jEnv);
                            }
                        }
                    }
                    else {
                        LogMsgInt(jEnv, jObj, "Error %d when receiving data", return_code );
                        logAPPCError( convID, jObj, jEnv );
                    }
             } /* END: while cycle */
         }
    }    

    (*jEnv)->ReleaseByteArrayElements(jEnv, jConvID, jIDValue, 0);

    return return_code;
}


/*
 * Class:     com_ibm_di_fc_zOSTSOCommandLine
 * Method:    deallocConversation
 * Signature: ([C)I
 */
JNIEXPORT jint JNICALL Java_com_ibm_di_fc_zOSTSOCommandLine_deallocConversation
  (JNIEnv *jEnv, jobject jObj, jbyteArray jConvID) 
{

     jboolean       isCopy           = JNI_FALSE;
     char           convID[APPC_CONV_ID_LEN] = {"\0"}; 
     CM_INT32       return_code      = CALL_FAIL;
     jbyte         *jIDValue         = NULL;
     long int       Conv_Notify_Type = ATB_NOTIFY_TYPE_NONE;
     long int       Deal_Type        = ATB_DEALLOCATE_SYNC_LEVEL;
     
     jIDValue = (*jEnv)->GetByteArrayElements( jEnv, jConvID, &isCopy ); 
     memcpy(convID, jIDValue, APPC_CONV_ID_LEN );
     
     atbdeal(convID,                    /* i */
            &Deal_Type,                 /* i */
     (char*)&Conv_Notify_Type, 
            &return_code );

     if ( return_code!=ATB_OK ) {
        LogMsgInt(jEnv, jObj, "Error %d when deallocating conversation",return_code);
        logAPPCError( convID, jObj, jEnv );
     }

     (*jEnv)->ReleaseByteArrayElements(jEnv, jConvID, jIDValue, 0);

     return return_code;
}


/*
 * Class:     com_ibm_di_fc_zOSTSOCommandLine
 * Method:    getCMOK
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_ibm_di_fc_zOSTSOCommandLine_getCMOK
                                                (JNIEnv *jEnv, jobject jObj) 
{
    return ATB_OK;
}
