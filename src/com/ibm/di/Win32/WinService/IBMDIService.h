#include "ntservice.h"

#define BUFFSIZE 5000
#define STRSIZE 5000
#define PROP_FILE "ibmdiservice.props"

#define PROP_PATH "path"
#define PROP_IBMDI_ROOT "ibmdiroot"
#define PROP_JVM_ROOT "jvmRoot"
#define PROP_ITDI_CONFIG_FILE "configfile"
#define PROP_ASSEMBLY_LINES "assemblylines"
#define PROP_EVENT_HANDLERS "eventhandlers"
#define PROP_CMD_OPTIONS "cmdoptions"
#define PROP_JVM_CMD_OPTIONS "jvmcmdoptions"
#define PROP_DEBUG "debug"
#define PROP_SERVICE_NAME "servicename"
#define PROP_AUTOSTART "autostart"
#define PROP_CONTR_SHUTDOWN "controlledshutdown"

class CIBMDIService : public CNTService
{
public:
	CIBMDIService();

	virtual BOOL OnInit();
	virtual void Run();
	virtual void OnStop();

	virtual int ReadConfigFile();

	virtual void Debug(char* debugMsg, ...);
	virtual void LogInfo(char* logMsg, ...);
	virtual void LogError(char* errorMsg, ...);
	virtual void LogSystemError(char* helpErrorMsg);

	virtual void TrimString (char* text);
	static DWORD WINAPI InvokeShutdown(LPVOID);

private:
	void SetRuntimeDLL(const char *);
	void SetJvmClassPath(const char *);
	void SetUserDir(const char *);

	void SetServiceAndDisplayNames();
	void ControlledShutdown();
	void SetFilePaths();
	void FreeMemory();
	void SetPathEnv();
	void SetCurDir();

	void InvokeMain(JNIEnv*);
	JNIEnv* CreateJVM();

private:
	char* solutionFolder;
	char* configFile;

	char* propPath;
	char* propItdiRoot;
	char* propJvmRoot;
	char* propItdiConfigFile;
	char* propAssemblyLines;
	char* propEventHandlers;
	char* propCmdOptions;
	char* propJvmCmdOptions;
	char* propDebug;
	char* propServiceName;
	char* propAutostart;
	char* propContrShutdown;

	char runtimeDLL[STRSIZE];
	char jvmClassPath[STRSIZE];
	char userDir[STRSIZE];

	bool debugFlag;
	bool contrShutdown;
	bool readAttempted;
	bool configFileRead;

	JavaVM* pJVM;
	HANDLE  hStopEvent;
	
private:
	STARTUPINFO startupInfo;
	PROCESS_INFORMATION processInfo;
};

