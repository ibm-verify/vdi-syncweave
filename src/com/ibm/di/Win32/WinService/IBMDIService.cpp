#include "NTServApp.h"
#include "IBMDIService.h"

#define ITDI_SERVICE_NAME "ibmdisrv"
#define ITDI_DISPLAY_NAME "IBM Security Verify Directory Integrator"

#define ITDI_SERVICE_NAME_TEMPLATE "ibmdisrv-%s"
#define ITDI_DISPLAY_NAME_TEMPLATE "IBM Security Verify Directory Integrator (%s)"

#define DLL_PATH_TEMPLATE "%s\\jvm\\jre\\bin\\default\\jvm.dll"
#define CLASS_PATH_TEMPLATE "-Djava.class.path=%s\\IDILoader.jar"
#define USER_DIR_TEMPLATE "-Duser.dir=%s"

#define CLASS_NAME "com/ibm/di/loader/ServerLauncher"
// Incorrect, but ServerLauncher will fix it since it starts with file:etc
#define LOG4J_OPTION "-Dlog4j2.configurationFile=file:etc/log4j2.xml"
#define SHUTDOWN_TIMEOUT 20000
#define OPTIONS_NUMBER 128

#define PROP_SEPARATOR " "

typedef jint (JNICALL *CreateJavaVM)(JavaVM **pvm, void **penv, void *args);

CIBMDIService::CIBMDIService()
:CNTService(ITDI_SERVICE_NAME, ITDI_DISPLAY_NAME)
{
	configFile = NULL;
	solutionFolder = NULL;

	propPath = NULL;
	propItdiRoot = NULL;
	propJvmRoot = NULL;
	propItdiConfigFile = NULL;
	propAssemblyLines = NULL;
	propEventHandlers = NULL;
	propCmdOptions = NULL;
	propJvmCmdOptions = NULL;
	propServiceName = NULL;
	propAutostart = NULL;
	propContrShutdown = NULL;
	propDebug = NULL;

	debugFlag = false;
	contrShutdown = false;

	// service is started automatically by default
	isAutostart = true;
	readAttempted = false;
	configFileRead = false;

	SetFilePaths();
	if (ReadConfigFile() == 0) {
		configFileRead = true;
	}
	
	char buf[STRSIZE];
	_snprintf(buf, sizeof(buf) - 1, DLL_PATH_TEMPLATE, propItdiRoot);
	buf[sizeof(buf) - 1] = '\0';
	SetRuntimeDLL(buf);

	_snprintf(buf, sizeof(buf) - 1, CLASS_PATH_TEMPLATE, propItdiRoot);
	buf[sizeof(buf) - 1] = '\0';
	SetJvmClassPath(buf);

	_snprintf(buf, sizeof(buf) - 1, USER_DIR_TEMPLATE, solutionFolder);
	buf[sizeof(buf) - 1] = '\0';
	SetUserDir(buf);
}


BOOL CIBMDIService::OnInit()
{
	return true;
}


JNIEnv* CIBMDIService::CreateJVM()
{
	JNIEnv* env = NULL;
	jint res = NULL;
	JavaVMInitArgs vm_args;
	JavaVMOption options[OPTIONS_NUMBER];
	int argNumber = 0;

	//Load the JVM DLL
	HINSTANCE dllInstance = LoadLibrary(runtimeDLL);
	if( dllInstance == 0) {
		LogError("Could not load JVM DLL");
	}

	// Resolve the function pointer JNI_CreateJVM
	CreateJavaVM createJVM = (CreateJavaVM)GetProcAddress(dllInstance, "JNI_CreateJavaVM");
	
	// Initialize an array of options for the JVM
	options[argNumber++].optionString = jvmClassPath;		// Application's class path
	options[argNumber++].optionString = LOG4J_OPTION;
	options[argNumber++].optionString = userDir;
	options[argNumber++].optionString = "-Xrs";
	
	char* temp = NULL;
	// Split the options in propJvmCmdOptions property
	if ((propJvmCmdOptions != NULL) && (strlen(propJvmCmdOptions) > 0)) {
		char* next_token = NULL;
		temp = _strdup(propJvmCmdOptions);
		char* token = strtok_s(temp, PROP_SEPARATOR, &next_token);
		
		while (token != NULL)
		{
			if (token != NULL)
			{
				options[argNumber++].optionString = token;
				token = strtok_s( NULL, PROP_SEPARATOR, &next_token);
			}
		}
	}
	
	//JNI Version 1.4 and above
	vm_args.version = JNI_VERSION_1_4;
	vm_args.options = options;
	vm_args.nOptions = argNumber;
	vm_args.ignoreUnrecognized = JNI_FALSE;

	//Create the JVM
	res = createJVM(&pJVM, (void **)&env, &vm_args);
	if (res < 0)  {
		LogError("Could not create JVM");
	}
	free(temp);

	return env;
}


void CIBMDIService::InvokeMain(JNIEnv* env)
{
	jclass cls = NULL;
	jmethodID methodID = NULL;
	int i=0;

	// Find the java class
    cls = env->FindClass(CLASS_NAME);

	// Find the main method
    methodID = env->GetStaticMethodID(cls, "main", "([Ljava/lang/String;)V");

	// Initialize an array of options for the main method
	int argNumber = 0;
	char *arguments [OPTIONS_NUMBER];

	if ((propItdiConfigFile != NULL) && (strlen(propItdiConfigFile) > 0)) {
		arguments[argNumber++] = "-c";
		arguments[argNumber++] = propItdiConfigFile;
	}
	
	if ((propAssemblyLines != NULL) && (strlen(propAssemblyLines) > 0)) {
		arguments[argNumber++] = "-r";
		arguments[argNumber++] = propAssemblyLines;
	}

	if ((propEventHandlers != NULL) && (strlen(propEventHandlers) > 0)) {
		arguments[argNumber++] = "-t";
		arguments[argNumber++] = propEventHandlers;
	}

	char* temp = NULL;
	// Split the options in propCmdOptions property
	if ((propCmdOptions != NULL) && (strlen(propCmdOptions) > 0)) {
		char* next_token = NULL;
		temp = _strdup(propCmdOptions);
		char* token = strtok_s(temp, PROP_SEPARATOR, &next_token);
		
		while (token != NULL)
		{
			if (token != NULL)
			{
				arguments[argNumber++] = token;
				token = strtok_s( NULL, PROP_SEPARATOR, &next_token);
			}
		}
	}

	jclass stringClass = env->FindClass("java/lang/String");
	jobjectArray args = env->NewObjectArray(argNumber, stringClass, NULL);
	for(i=0; i < argNumber; i++)
	{
		env->SetObjectArrayElement(args, i, env->NewStringUTF(arguments[i]));
	}

	// Invoke main method
	env->CallStaticVoidMethod(cls, methodID, args);

	// Log error if there is any exception occured
	if(env->ExceptionCheck()) {
		LogError("Exception occurred while starting the server");
	}

	cls = NULL;
	methodID = NULL;
	free(temp);
}


///////////////////////////////////////////////////////////////////////////////////////////
// Set PATH env (from properties file)
//
void CIBMDIService::SetPathEnv()
{
	char* currentPathEnv = getenv("PATH");
	char* newPathEnv = (char*)calloc(STRSIZE, 1);

	if (currentPathEnv != NULL) {
		_snprintf(newPathEnv,STRSIZE-1,"PATH=%s;%s;%s\\bin;%s;",currentPathEnv,propItdiRoot,propItdiRoot,propPath);
	} 
	else {
		_snprintf(newPathEnv,STRSIZE-1,"PATH=%s;%s\\bin;%s;",propItdiRoot,propItdiRoot,propPath);
	}
	_putenv(newPathEnv);
	Debug("new process path: %1", newPathEnv);
	
	// Free used memory
	free(newPathEnv);
}


///////////////////////////////////////////////////////////////////////////////////////////
// Set current directory
// 
void CIBMDIService::SetCurDir()
{
	BOOL isOk = SetCurrentDirectory(solutionFolder);
	if(isOk != 0) {
		Debug("current directory: %1", solutionFolder);
	}
}


void CIBMDIService::Run()
{
	JNIEnv* env = NULL;
	
	// Make sure we read the configuration file
	if (!configFileRead) {
		return;
	}

	SetCurDir();

	SetPathEnv();
	
	env = CreateJVM();

	InvokeMain(env);

	env = NULL;
}

int CIBMDIService::ReadConfigFile()
{
	if (readAttempted) {
		return 0;
	}

	readAttempted = true;

	int rc = 0;

	FILE *fp = NULL;
	char line[BUFFSIZE];
	char *eolPtr = NULL;

	char *checkRepString = NULL;
	
	if ((fp = fopen(configFile, "r")) == NULL) {
		LogError("Could not open configuration file: %1", configFile);
		return ( -1);
	}

	debugFlag = false;
	contrShutdown = false;

	while (fgets(line, BUFFSIZE, fp) != NULL) {
		if ((eolPtr = strpbrk(line, "\r\n")) != NULL) {
			*eolPtr = 0;
			eolPtr = NULL;
		}

		if (strncmp(line, PROP_PATH, strlen(PROP_PATH)) == 0) {
			propPath = strdup(line + strlen(PROP_PATH) + 1);
			TrimString(propPath);
		}
		else if (strncmp(line, PROP_IBMDI_ROOT, strlen(PROP_IBMDI_ROOT)) == 0) {
			propItdiRoot = strdup(line + strlen(PROP_IBMDI_ROOT) + 1);
			TrimString(propItdiRoot);

			if ((propItdiRoot == NULL) || (strlen(propItdiRoot) == 0)) {
				LogError("Missing required property: %1", PROP_IBMDI_ROOT);
				rc = -1;
				goto finalize;
			}
			else {
				if (propItdiRoot[strlen(propItdiRoot)-1] == '\\') {
					propItdiRoot[strlen(propItdiRoot)-1] = '\0';
				}
			}
		}
		else if (strncmp(line, PROP_JVM_ROOT, strlen(PROP_JVM_ROOT)) == 0) {
			propJvmRoot = strdup(line + strlen(PROP_JVM_ROOT) + 1);
			TrimString(propJvmRoot);

			if ((propJvmRoot == NULL) || (strlen(propJvmRoot) == 0)) {
				LogError("Missing required property: %1", PROP_JVM_ROOT);
				rc = -1;
				goto finalize;
			}
			else {
				if (propJvmRoot[strlen(propJvmRoot)-1] == '\\') {
					propJvmRoot[strlen(propJvmRoot)-1] = '\0';
				}
			}
		}
		else if (strncmp(line, PROP_ITDI_CONFIG_FILE, strlen(PROP_ITDI_CONFIG_FILE)) == 0) {
			propItdiConfigFile = strdup(line + strlen(PROP_ITDI_CONFIG_FILE) + 1);
			TrimString(propItdiConfigFile);
		}
		else if (strncmp(line, PROP_ASSEMBLY_LINES, strlen(PROP_ASSEMBLY_LINES)) == 0) {
			propAssemblyLines = strdup(line + strlen(PROP_ASSEMBLY_LINES) + 1);
			TrimString(propAssemblyLines);
		}
		else if (strncmp(line, PROP_EVENT_HANDLERS, strlen(PROP_EVENT_HANDLERS)) == 0) {
			propEventHandlers = strdup(line + strlen(PROP_EVENT_HANDLERS) + 1);
			TrimString(propEventHandlers);
		}
		else if (strncmp(line, PROP_CMD_OPTIONS, strlen(PROP_CMD_OPTIONS)) == 0) {
			propCmdOptions = strdup(line + strlen(PROP_CMD_OPTIONS) + 1);
			TrimString(propCmdOptions);
		} 		
		else if (strncmp(line, PROP_JVM_CMD_OPTIONS, strlen(PROP_JVM_CMD_OPTIONS)) == 0) {
			propJvmCmdOptions = strdup(line + strlen(PROP_JVM_CMD_OPTIONS) + 1);
			TrimString(propJvmCmdOptions);
		}
		else if (strncmp(line, PROP_DEBUG, strlen(PROP_DEBUG)) == 0) {
			propDebug = strdup(line + strlen(PROP_DEBUG) + 1);
			TrimString(propDebug);

			if ((propDebug != NULL) && (strlen(propDebug) > 0)) {
				if (_stricmp("true", propDebug) == 0) {
					debugFlag = true;
					LogInfo("Debug information will be displayed.");
				}
			}
		}
		else if (strncmp(line, PROP_SERVICE_NAME, strlen(PROP_SERVICE_NAME)) == 0) {
			propServiceName = strdup(line + strlen(PROP_SERVICE_NAME) + 1);
			TrimString(propServiceName);
			SetServiceAndDisplayNames();
		}
		else if (strncmp(line, PROP_AUTOSTART, strlen(PROP_AUTOSTART)) == 0) {
			propAutostart = strdup(line + strlen(PROP_AUTOSTART) + 1);
			TrimString(propAutostart);

			if ((propAutostart != NULL) && (strlen(propAutostart) > 0)) {
				if (_stricmp("false", propAutostart) == 0) {
					isAutostart = false;
				}
			}
		}
		else if (strncmp(line, PROP_CONTR_SHUTDOWN, strlen(PROP_CONTR_SHUTDOWN)) == 0) {
			propContrShutdown = strdup(line + strlen(PROP_CONTR_SHUTDOWN) + 1);
			TrimString(propContrShutdown);

			if ((propContrShutdown != NULL) && (strlen(propContrShutdown) > 0)) {
				if (_stricmp("true", propContrShutdown) == 0) {
					contrShutdown = true;
				}
			}
		}

		memset(line, 0, BUFFSIZE);
	}
	
	// debug property values and check for required properties
	if (propPath != NULL) {
		Debug("%1: %2", PROP_PATH, propPath);
	}

	if (propItdiRoot != NULL) {
		Debug("%1: %2", PROP_IBMDI_ROOT, propItdiRoot);
	}
	else {
		LogError("Missing required property: %1", PROP_IBMDI_ROOT);
		rc = -1;
		goto finalize;
	}

	if (propJvmRoot != NULL) {
		Debug("%1: %2", PROP_JVM_ROOT, propJvmRoot);
	}
	else {
		LogError("Missing required property: %1", PROP_JVM_ROOT);
		rc = -1;
		goto finalize;
	}

	if (propItdiConfigFile != NULL) {
		Debug("%1: %2", PROP_ITDI_CONFIG_FILE, propItdiConfigFile);
	}
	else {
		LogError("Missing required property: %1", PROP_ITDI_CONFIG_FILE);
		rc = -1;
		goto finalize;
	}

	if (propAssemblyLines != NULL) {
		Debug("%1: %2", PROP_ASSEMBLY_LINES, propAssemblyLines);
	}

	if (propEventHandlers != NULL) {
		Debug("%1: %2", PROP_EVENT_HANDLERS, propEventHandlers);
	}

	if (propCmdOptions != NULL) {
		Debug("%1: %2", PROP_CMD_OPTIONS, propCmdOptions);
	}

	if (propJvmCmdOptions != NULL) {
		Debug("%1: %2", PROP_JVM_CMD_OPTIONS, propJvmCmdOptions);
	}

	if (propDebug != NULL) {
		Debug("%1: %2", PROP_DEBUG, propDebug);
	}

	if (propServiceName != NULL) {
		Debug("%1: %2", PROP_SERVICE_NAME, propServiceName);
	}

	if (propAutostart != NULL) {
		Debug("%1: %2", PROP_AUTOSTART, propAutostart);
	}

	if (propContrShutdown != NULL) {
		Debug("%1: %2", PROP_CONTR_SHUTDOWN, propContrShutdown);
	}

finalize:
	if (fp != NULL) {
		fclose(fp);
	}

	return(rc);
}

void CIBMDIService::FreeMemory()
{
	if (solutionFolder != NULL) {
		free(solutionFolder);
		solutionFolder = NULL;
	}

	if (configFile != NULL) {
		free(configFile);
		configFile = NULL;
	}

	if (propPath != NULL) {
		free(propPath);
		propPath = NULL;
	}

	if (propItdiRoot != NULL) {
		free(propItdiRoot);
		propItdiRoot = NULL;
	}

	if (propJvmRoot != NULL) {
		free(propJvmRoot);
		propJvmRoot = NULL;
	}

	if (propItdiConfigFile != NULL) {
		free(propItdiConfigFile);
		propItdiConfigFile = NULL;
	}
	
	if (propAssemblyLines != NULL) {
		free(propAssemblyLines);
		propAssemblyLines = NULL;
	}

	if (propEventHandlers != NULL) {
		free(propEventHandlers);
		propEventHandlers = NULL;
	}

	if (propCmdOptions != NULL) {
		free(propCmdOptions);
		propCmdOptions = NULL;
	}

	if (propJvmCmdOptions != NULL) {
		free(propJvmCmdOptions);
		propJvmCmdOptions = NULL;
	}

	if (propDebug != NULL) {
		free(propDebug);
		propDebug = NULL;
	}

	if (propServiceName != NULL) {
		free(propServiceName);
		propServiceName = NULL;
	}

	if (propAutostart != NULL) {
		free(propAutostart);
		propAutostart = NULL;
	}

	if (propContrShutdown != NULL) {
		free(propContrShutdown);
		propContrShutdown = NULL;
	}
}

void CIBMDIService::OnStop()
{
	// Cleanup memory first
	FreeMemory();

	// Try to perform controlled shutdown
	if (contrShutdown){
		ControlledShutdown();
	}
}

void CIBMDIService::ControlledShutdown()
{
	LogInfo("Try controlled shutdown first.");
		
	// Create a separate shutdown thread just to be 
	// sure that the shutdown method will be invoked
	HANDLE hThread = CreateThread( 
			NULL,							// default security attributes
			0,								// use default stack size  
			CIBMDIService::InvokeShutdown,	// thread function 
			pJVM,							// argument to thread function 
			0,								// use default creation flags 
			0);								// returns the thread identifier 

	// Wait for the thread to gracefully shutdown the server 
	// within a timeout of 20 seconds
	DWORD r = WaitForSingleObject(hThread, SHUTDOWN_TIMEOUT); 
		
	if(r != WAIT_OBJECT_0) {
		LogError("Controlled shutdown timeout.");
	}

	CloseHandle(hThread);
}

DWORD CIBMDIService::InvokeShutdown(void * lp){
	JNIEnv *env;
	jclass cls;
	jmethodID methodId;
	
	// This can be only of type JavaVM*
	JavaVM* pJVM = (JavaVM*)lp;
	
	// Attach the thread creator of the JVM to the JVM, to make JNI calls
	pJVM->AttachCurrentThread((void **)&env, 0); 
	
	// Find class IDILoader
	cls = env->FindClass(CLASS_NAME);

	// Find static void shutdown() method
	methodId = env->GetStaticMethodID(cls, "shutdown", "()V");

	env->CallStaticVoidMethod(cls, methodId);
	pJVM->DetachCurrentThread();

	return 0;
}

void CIBMDIService::Debug(char* debugMsg, ...)
{
	if (debugFlag != true) {
		return;
	}

	va_list msgAgrs;
	va_start(msgAgrs, debugMsg);

	LPVOID lpMsgBuf;
	FormatMessage( 
	    FORMAT_MESSAGE_ALLOCATE_BUFFER | 
		FORMAT_MESSAGE_FROM_STRING,
		debugMsg,
		0,
		0,
		(LPTSTR) &lpMsgBuf,
		0,
		&msgAgrs 
	);

	va_end(msgAgrs);

	LogEvent(EVENTLOG_INFORMATION_TYPE, EVMSG_DEBUG, (char*)lpMsgBuf);

	LocalFree(lpMsgBuf);
}


void CIBMDIService::LogInfo(char* logMsg, ...)
{
	va_list msgAgrs;
	va_start(msgAgrs, logMsg);

	LPVOID lpMsgBuf;
	FormatMessage( 
	    FORMAT_MESSAGE_ALLOCATE_BUFFER | 
		FORMAT_MESSAGE_FROM_STRING,
		logMsg,
		0,
		0,
		(LPTSTR) &lpMsgBuf,
		0,
		&msgAgrs 
	);

	va_end(msgAgrs);

	LogEvent(EVENTLOG_INFORMATION_TYPE, EVMSG_INFO, (char*)lpMsgBuf);

	LocalFree(lpMsgBuf);
}


void CIBMDIService::LogError(char* errorMsg, ...)
{
	va_list msgAgrs;
	va_start(msgAgrs, errorMsg);

	LPVOID lpMsgBuf;
	FormatMessage( 
	    FORMAT_MESSAGE_ALLOCATE_BUFFER | 
		FORMAT_MESSAGE_FROM_STRING,
		errorMsg,
		0,
		0,
		(LPTSTR) &lpMsgBuf,
		0,
		&msgAgrs 
	);

	va_end(msgAgrs);

	LogEvent(EVENTLOG_ERROR_TYPE, EVMSG_ERROR, (char*)lpMsgBuf);

	LocalFree(lpMsgBuf);
}


void CIBMDIService::LogSystemError(char* helpErrorMsg)
{
	DWORD lastError = GetLastError();

	LPVOID lpMsgBuf;
	FormatMessage( 
	    FORMAT_MESSAGE_ALLOCATE_BUFFER | 
	    FORMAT_MESSAGE_FROM_SYSTEM | 
		FORMAT_MESSAGE_IGNORE_INSERTS,
		NULL,
		lastError,
		0,
		(LPTSTR) &lpMsgBuf,
		0,
		NULL 
	);
	
	LogError("%1: error code:%2!u!, %3", helpErrorMsg, lastError, lpMsgBuf);
	
	LocalFree(lpMsgBuf);
}


void CIBMDIService::TrimString (char* text)
{
	if (text == NULL || strlen(text) == 0) {
		return;
	}

	// trim right
	while ((strlen(text) > 0) && (text[strlen(text)-1] == ' ' || text[strlen(text)-1] == '\t')) {
		text[strlen(text)-1] = '\0';
	}

	// trim left
	char* left = text;
	while (left[0] == ' ' || left[0] == '\t') {
		left++;
	}
	if (left != text) {
		memmove(text, left, strlen(left)+1);
	}
}


////////////////////////////////////////////////////////////////////////////////
// Sets the path of the solution folder and the config file according to the
// location of the executable file (ibmdiservice.exe)
////////////////////////////////////////////////////////////////////////////////
void CIBMDIService::SetFilePaths()
{
	configFile = (char*)calloc(STRSIZE, 1);
	GetModuleFileName(NULL, configFile, STRSIZE);

	char *slash = strrchr(configFile, '\\');
	strcpy(slash + 1, PROP_FILE);

	solutionFolder = (char*)calloc(STRSIZE, 1);
	memmove(solutionFolder, configFile, slash - configFile);
}


////////////////////////////////////////////////////////////////////////////////
// Sets the service and display names accoring to the servicename property
////////////////////////////////////////////////////////////////////////////////
void CIBMDIService::SetServiceAndDisplayNames()
{
	if (propServiceName != NULL) {
		char name[SERVICE_NAMES];

		_snprintf(name, sizeof(name) - 1, ITDI_SERVICE_NAME_TEMPLATE, propServiceName);
		name[sizeof(name) - 1] = '\0';
		SetServiceName(name);

		_snprintf(name, sizeof(name) - 1, ITDI_DISPLAY_NAME_TEMPLATE, propServiceName);
		name[sizeof(name) - 1] = '\0';
		SetDisplayName(name);
	}
}

void CIBMDIService::SetRuntimeDLL(const char *szRuntimeDLL)
{
	strncpy(runtimeDLL, szRuntimeDLL, sizeof(runtimeDLL) - 1);
}

void CIBMDIService::SetJvmClassPath(const char *szJvmClassPath)
{
	strncpy(jvmClassPath, szJvmClassPath, sizeof(jvmClassPath) - 1);
}

void CIBMDIService::SetUserDir(const char *szUserDir)
{
	strncpy(userDir, szUserDir, sizeof(userDir) - 1);
}
