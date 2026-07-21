#include "util.h"
#include <jni.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>

#define JDEBUG          0
#define TO_CHARSET      "ISO8859-1"
#define DEFAULT_CHARSET "IBM-1047"


static char DEF_CHARSET[16] = {"\0"};

void getCharset(JNIEnv* jEnv, jbyteArray jbEnc)
{
    jboolean    isCopy    = JNI_FALSE;
    jbyte       *enc      = NULL;
    jsize       encSz     = -1;
    
    if( jbEnc!=NULL && (encSz=(*jEnv)->GetArrayLength(jEnv, jbEnc))>0 ) {
        enc = (*jEnv)->GetByteArrayElements(jEnv, jbEnc, &isCopy);
        memcpy(DEF_CHARSET, enc, encSz);
        (*jEnv)->ReleaseByteArrayElements(jEnv,jbEnc, enc, 0);
    }
    else {
        strcpy(DEF_CHARSET, DEFAULT_CHARSET);
    }
    
}

char* getOSCharArray(JNIEnv* jenv, jstring jstr) {
    char *retChArray = NULL;
    jint rc = -1 , nativeLen = 0;

    if( jstr!=NULL && ( rc=GetStringPlatformLength( jenv, jstr, &nativeLen, NULL) )==0 ) {
        if( (retChArray = (char*)malloc(sizeof(char)*nativeLen))!=NULL ) {
            if( ( rc=GetStringPlatform( jenv, jstr, retChArray, nativeLen, NULL))!=0 ) {
              free(retChArray);
              retChArray = NULL;
            }
        }
    }
    return retChArray;
}

int StringConvert(char *sourceStr, char *targetStr) {
    iconv_t   source;             /* parameters to instantiate iconv  */
    size_t    sStrLen, tStrLen;   /* local copies of string lengths   */
    iconv_t   ourConverter;       /* the actual conversion descriptor */
    int       iconvRC;            /* return code from the conversion  */
    size_t    originalLen;        /* original length of the sourceStr */
 
    
    originalLen = sStrLen = tStrLen = strlen(sourceStr);    
    memset(&source,0,sizeof(source));    
 
    source = iconv_open( TO_CHARSET, DEF_CHARSET );
 
    /*  Perform the conversion.  */
    iconvRC = iconv(source,
                    (char**) &sourceStr,
                    &sStrLen,
                    &targetStr,
                    &tStrLen);
                    
    /*  If the conversion failed, return a zero.  */
    if ( 0 != iconvRC ) return 0;
 
    /*  Close the conversion descriptor.  */
    iconv_close( source );
    *targetStr = '\0'; 
    
    return originalLen-tStrLen;
 
}
/**********************************************************
 *
 **********************************************************/
void jstrcpy(char *to, jchar *from, int len) {
    int i=0;
    for( i=0;i<len;i++) {
        to[i] = from[i];
    }
}

/********************************************************
 *
 ********************************************************/
char** copyJStringArray2NativeArray(JNIEnv* jEnv, jobjectArray jStrArray)
{ 
   char **nativeParms = 0;
   jsize size = (*jEnv)->GetArrayLength(jEnv, jStrArray);
   int   iCounter = 0;
   nativeParms = (char**)calloc(size,sizeof(char*));
   if (nativeParms==NULL)
       return NULL;

   for (iCounter=0; iCounter<size; iCounter++) {
       nativeParms[iCounter] = getOSCharArray( jEnv, (*jEnv)->GetObjectArrayElement(jEnv, jStrArray, iCounter) );
   }
   
   return nativeParms;
}

/********************************************************
 *
 ********************************************************/
void ReleaseUTFChars(JNIEnv *jEnv, jstring jstr, char* element) 
{
    (*jEnv)->ReleaseStringUTFChars(jEnv, jstr, element);
}

/********************************************************
 *
 ********************************************************/
void ReleaseNativeCharArray(JNIEnv *jEnv, jobjectArray jStrArray, char** array) 
{
    int iCount = 0;
    jsize size = (*jEnv)->GetArrayLength(jEnv, jStrArray);
    
    if( array ) {
        while(iCount<size) {
            if( array[iCount] ) {
              free(array[iCount]);
            }
           iCount++;
        }
        free(array);
    }
}


