#include <global.h>
#include <intl.h>
#include <nif.h>
#include <nsfdb.h>
#include <nsfsearc.h>
#include <osfile.h>
#include <osmisc.h>
#include <kfm.h>
#include <osenv.h>
#include <stdlib.h>

#include "com_ibm_di_connector_DominoChangeDetectionConnector.h"


// it is assumed that no documents are older than January 1, 1 AD
#define BEGINNING_OF_TIME           "0001-01-01 00:00:00"

#define NOTES_ERR_MSG_LENGTH        200
#define ERR_MSG_LENGTH              NOTES_ERR_MSG_LENGTH + 150

#define NOTE_STATE_NORMAL           "NOR"
#define NOTE_STATE_DELETED          "DEL"

// a sample millisecond suffix is ".270"
#define MILLIS_SUFFIX_LEN           4

// Name of the item in the notes.ini file identifying the idfile path
#define IDFILE_PATH_ITEM			"KeyFilename"


typedef struct _JNI_Params
{
    JNIEnv * env;
    jobject vector;
    jmethodID addMethodID;
} JNI_Params;


DBHANDLE hDB = NULL;



void getNotesErrorString(STATUS error, char * functionName, char * msg)
{
    char notesErrMsg[NOTES_ERR_MSG_LENGTH];

    OSLoadString(NULLHANDLE, ERR(error), notesErrMsg, NOTES_ERR_MSG_LENGTH);
    sprintf(msg, "Native call %s failed with error: code %d, '%s'", functionName, error, notesErrMsg);
}


void debugDumpString(const char * msg, const char * param)
{
#ifdef DOMCHDET_DLL_DEBUG
    if (param != NULL) {
        printf(msg, param);
    }s
    else {
        printf(msg);
    }
#endif
}


void debugDumpDWORD(const char * msg, DWORD param)
{
#ifdef DOMCHDET_DLL_DEBUG
    printf(msg, param);
#endif
}


JNIEXPORT void JNICALL Java_com_ibm_di_connector_DominoChangeDetectionConnector_initNotes
  (JNIEnv * env, jobject jobj, jstring jstrDominoServerName, jstring jstrDbName, jstring jstrUserPassword)
{
    STATUS error = NOERROR;
	int error2 = 1;
    const char * dbName = NULL;
    const char * dbDominoServerName = NULL;
    char remoteDbName[MAXPATH];

	const char * userPassword = NULL;
	const char * userName = NULL;
	char filePath[MAXUSERNAME+1];

	debugDumpString("Java_com_ibm_di_connector_DominoChangeDetectionConnector_initNotes()\n", NULL);

    error = NotesInitExtended (0, NULL);

	if (error)
    {
        char msg[ERR_MSG_LENGTH];
        getNotesErrorString(error, "NotesInitExtended", msg);
        debugDumpString("Error: %s\n", msg);
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/Exception"), msg);
        return;
    }

	error2 = OSGetEnvironmentString(IDFILE_PATH_ITEM, filePath, MAXUSERNAME);

	if (!error2)
    { 
		char msg[ERR_MSG_LENGTH];
		strcpy(msg,"Could not determine the path to the id file. Verify the KeyFilename item in the notes.ini file.");
		NotesTerm();
		hDB = NULL;
		debugDumpString("Error: %s\n", msg);
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/Exception"), msg);
        return;
    }

    dbName = (*env)->GetStringUTFChars(env, jstrDbName, NULL);

	if(jstrDominoServerName != NULL)
	{
		dbDominoServerName = (*env)->GetStringUTFChars(env, jstrDominoServerName, NULL);
	}

	userPassword = (*env)->GetStringUTFChars(env, jstrUserPassword, NULL);

    OSPathNetConstruct(NULL, dbDominoServerName, dbName, remoteDbName);
    
    if(jstrDominoServerName != NULL)
    	(*env)->ReleaseStringUTFChars(env, jstrDominoServerName, dbDominoServerName);
    	
    (*env)->ReleaseStringUTFChars(env, jstrDbName, dbName);
	
    debugDumpString("remoteDbName: %s\n", remoteDbName);
	
	error = SECKFMSwitchToIDFile(filePath, (char *)userPassword, (char *)userName, MAXUSERNAME, 0, NULL);

	(*env)->ReleaseStringUTFChars(env, jstrUserPassword, userPassword);

	if (error)
	{
		char msg[ERR_MSG_LENGTH];
		getNotesErrorString(error, "SECKFMSwitchToIDFile", msg);
		NotesTerm();
		hDB = NULL;
		debugDumpString("Error: %s\n", msg);
		(*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/Exception"), msg);
		return;
	}

	error = NSFDbOpen (remoteDbName, &hDB);

	if (error)
	{
		char msg[ERR_MSG_LENGTH];
		getNotesErrorString(error, "NSFDbOpen", msg);
		NotesTerm();
		hDB = NULL;
		debugDumpString("Error: %s\n", msg);
		(*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/Exception"), msg);
		return;
	}

	debugDumpString("End of Java_com_ibm_di_connector_DominoChangeDetectionConnector_initNotes()\n", NULL);
}


void LNPUBLIC getTimeDateString(TIMEDATE * ptdModified, char * szTimedate, INTLFORMAT * pIntlFormat)
{
    WORD wLen;

    ConvertTIMEDATEToText(pIntlFormat, NULL, 
                            ptdModified, 
                            szTimedate, 
                            MAXALPHATIMEDATE,
                            &wLen);
    szTimedate[wLen] = '\0';
    return;
}


void UnIDToString(char * buf, ORIGINATORID * pNoteUnID)
{
    DWORD d1 = pNoteUnID->File.Innards[1];
    DWORD d2 = pNoteUnID->File.Innards[0];
    DWORD d3 = pNoteUnID->Note.Innards[1];
    DWORD d4 = pNoteUnID->Note.Innards[0];

    sprintf(buf, "%08X", d1);
    sprintf(buf + 8, "%08X", d2);
    sprintf(buf + 16, "%08X", d3);
    sprintf(buf + 24, "%08X", d4);
    buf[32] = '\0';
}


STATUS LNPUBLIC BuildJavaVector(void far *pParam, SEARCH_MATCH far *pSearchMatch, ITEM_TABLE far *pSummaryBuffer)
{   
    SEARCH_MATCH SearchMatch;    
    JNI_Params * pJNIParams = NULL;
    jstring jstrReturnData = NULL;
    char noteUnID[64];
    char returnData[128];
    char * noteState = NULL;

    memcpy( (char*)&SearchMatch, (char*)pSearchMatch, sizeof(SEARCH_MATCH) );

    UnIDToString(noteUnID, &(SearchMatch.OriginatorID));
    if(SearchMatch.SERetFlags & SE_FMATCH) {
        noteState = NOTE_STATE_NORMAL;
    }
    else {
        noteState = NOTE_STATE_DELETED;
    }

    sprintf(returnData, "%s:%X:%s", noteUnID, SearchMatch.ID.NoteID, noteState);
    debugDumpString("returnData: %s\n", returnData);

    pJNIParams = (JNI_Params *)pParam;
    jstrReturnData = (*(pJNIParams->env))->NewStringUTF(pJNIParams->env, returnData);
    (*(pJNIParams->env))->CallBooleanMethod(pJNIParams->env, pJNIParams->vector, pJNIParams->addMethodID, jstrReturnData);
    (*(pJNIParams->env))->DeleteLocalRef(pJNIParams->env, jstrReturnData);

    return (NOERROR);
}


DWORD removeMillis(char * strDate)
{
    int millisIndex = 0;
    int decimalPointIndex = 0;
    char millisString[MILLIS_SUFFIX_LEN];
    int millis = 0;

    decimalPointIndex = strlen(strDate) - MILLIS_SUFFIX_LEN;
    millisIndex = decimalPointIndex + 1;
    strcpy(millisString, strDate + millisIndex);
    strDate[decimalPointIndex] = '\0';
    millis = atoi(millisString);

    return millis;
}


void addHundredthsToTIMEDATE(TIMEDATE * pTimeDate, DWORD hundredths)
{
    TIME time;

    if (hundredths > 0) {
        time.GM = *pTimeDate;
        TimeGMToLocal(&time);
        time.hundredth = hundredths;
        TimeLocalToGM(&time);
        *pTimeDate = time.GM;
    }
}


JNIEXPORT jobject JNICALL Java_com_ibm_di_connector_DominoChangeDetectionConnector_getModifiedNotes
  (JNIEnv * env, jobject jobj, jstring jstrStartDate, jobject jobjEndDate)
{
    JNI_Params jniParams;
    TIMEDATE timeSince;
    TIMEDATE timeUntil;
    STATUS error;
    jobject vector;
    jclass javaClass;
    jmethodID constructorID;
    jmethodID methodID;
    jmethodID appendMethodID;
    char * strTimeSince = NULL;
    char * copyOfStrTimeSince = NULL;
    INTLFORMAT intl_format;
    jstring jstrEndDate;
    char strEndDateToSec[MAXALPHATIMEDATE + 1];
    char strEndDate[MAXALPHATIMEDATE + 1 + MILLIS_SUFFIX_LEN];
    DWORD hundredthsOfSec = 0;
    
    debugDumpString("Java_com_ibm_di_connector_DominoChangeDetectionConnector_getModifiedNotes()\n", NULL);

    javaClass = (*env)->FindClass(env, "java/util/Vector");
    constructorID = (*env)->GetMethodID(env, javaClass, "<init>", "()V");
    vector = (*env)->NewObject(env, javaClass, constructorID);

    if (jstrStartDate != NULL) {
        // date format is "yyyy-mm-dd hh:mm:ss.SSS"
        strTimeSince = (char *)(*env)->GetStringUTFChars(env, jstrStartDate, NULL);
        hundredthsOfSec = removeMillis(strTimeSince) / 10;
    }
    else {
        strTimeSince = BEGINNING_OF_TIME;
        hundredthsOfSec = 0;
    }
    debugDumpString("strTimeSince: %s\n", strTimeSince);
    debugDumpDWORD("Since time hundredthsOfSec: %lu\n", hundredthsOfSec);

    OSGetIntlSettings(&intl_format, sizeof(intl_format));
    intl_format.Flags = DATE_YMD | DATE_4DIGIT_YEAR | CLOCK_24_HOUR | NUMBER_LEADING_ZERO | DAYLIGHT_SAVINGS;
    strcpy(intl_format.DateString, "-");
    strcpy(intl_format.TimeString, ":");

	copyOfStrTimeSince = strTimeSince;
    error = ConvertTextToTIMEDATE(&intl_format, NULL, &strTimeSince, MAXALPHATIMEDATE, &timeSince);
    
    if (jstrStartDate != NULL)
    	(*env)->ReleaseStringUTFChars(env, jstrStartDate, copyOfStrTimeSince);
    	
    if (error != 0)
    {
        char msg[ERR_MSG_LENGTH];
        getNotesErrorString(error, "ConvertTextToTIMEDATE", msg);
        debugDumpString("Error: %s\n", msg);
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/Exception"), msg);
        return vector;
    }
    addHundredthsToTIMEDATE(&timeSince, hundredthsOfSec);

    methodID = (*env)->GetMethodID(env, javaClass, "add", "(Ljava/lang/Object;)Z");
    jniParams.env = env;
    jniParams.vector = vector;
    jniParams.addMethodID = methodID;
	
    error = NSFSearch(hDB, NULLHANDLE, NULL, 
                        SEARCH_ALL_VERSIONS, 
                        NOTE_CLASS_ALL,
                        &timeSince, 
                        BuildJavaVector,
                        &jniParams, &timeUntil);
    if (error)
    {
        char msg[ERR_MSG_LENGTH];
        getNotesErrorString(error, "NSFSearch", msg);
        debugDumpString("Error: %s\n", msg);
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/Exception"), msg);
        return vector;
    }

    getTimeDateString(&timeUntil, strEndDateToSec, &intl_format);
    hundredthsOfSec = TimeExtractTicks(&timeUntil) % 100;
    // date format is "yyyy-mm-dd hh:mm:ss.SSS"
    sprintf(strEndDate, "%s.%02d0", strEndDateToSec, hundredthsOfSec);
    debugDumpString("strEndDate: %s\n", strEndDate);

    jstrEndDate = (*env)->NewStringUTF(env, strEndDate);
    javaClass = (*env)->FindClass(env, "java/lang/StringBuffer");
    appendMethodID = (*env)->GetMethodID(env, javaClass, "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
    (*env)->CallObjectMethod(env, jobjEndDate, appendMethodID, jstrEndDate);

    debugDumpString("End of Java_com_ibm_di_connector_DominoChangeDetectionConnector_getModifiedNotes()\n", NULL);

    return vector;
}


JNIEXPORT void JNICALL Java_com_ibm_di_connector_DominoChangeDetectionConnector_termNotes
  (JNIEnv * env, jobject jobj)
{
    debugDumpString("Java_com_ibm_di_connector_DominoChangeDetectionConnector_termNotes()\n", NULL);
    if (hDB != NULL) {
        NSFDbClose(hDB);
        NotesTerm();
    }
    debugDumpString("End of Java_com_ibm_di_connector_DominoChangeDetectionConnector_termNotes()\n", NULL);
}
