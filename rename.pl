#! /usr/bin/perl

$conv = '
metamerge.inf			idi.inf
com.architech.config					remove
com.architech.btree		com.ibm.di.btree
com.architech.jmx		com.ibm.di.jmx
com.architech.connector		com.ibm.di.connector
    HTTPConnector2		HTTPClientConnector
    HTTPConnector		OldHTTPCLient
    HTTPServer			HTTPServerConnector
    Mailbox			MailboxConnector
    connector.SNMP		connector.SNMPConnector
    rscCOMPort			COMPortConnector
    rscCommandLine		CommandLineConnector
    rscConnectorInterface	ConnectorInterface
    rscConnector		Connector
    rscConsumerProducerConnector ConsumerProducer
    rscEmiUcpConnector		EmiUcpConnector
    rscFTPClient		FTPClientConnector
    rscFileConnector		FileConnector
    rscHashtable      		HashtableConnector
    rscHttpServer		OldHTTPServer
    rscJMSConnector		JMSConnector
    rscJNDI   			JNDIConnector
    rscJdbc			JDBCConnector
    rscLdap			LDAPConnector
    rscLotusDomino		DominoConnector
    rscMailStream		MailStreamConnector
    rscNTAccountManager					remove
    rscNetscapeChangelog	LDAPChangelogConnector
    rscNotes						remove
    rscOdbc			ODBCConnector
    rscPPInterface		PPInterface
    rscProxyConnector					remove
    rscRemoteConnect		RemoteConnect
    rscRemoteProtocol		RemoteProtocol
    rscRemote			Remote
    rscRfc822Message		RFC822Message
    rscSMPPConnector					remove
    rscScriptConnector		ScriptConnector
    rscSmtp			SMTPConnector
    rscSoap			SOAPConnector
    rscStreamConnector		StreamConnector
    rscTest						remove
    rscURLConnector		URLConnector
    DominoUsers			DominoUsersConnector
    rscVBD						remove
    rscADChangelog		ADChangelogConnector
    rscExchangeChangelog	ExchangeChangelogConnector
com.architech.entry		com.ibm.di.entry
com.architech.event		com.ibm.di.event
com.architech.exceptions	com.ibm.di.exceptions
    LicenseExpiredException				remove
    LicenseWarningException				remove
    rseAbortAL			AbortALException
    rseReturn			ReturnException
    rseEntryIgnore		IgnoreEntryException
    rseEntrySkipped		SkipEntryException
    rseExceptionHandlerInterface ExceptionHandlerInterface
    rseNonFatalException 	NonFatalException
    rseSkipTo			SkipToException
    rseRestartEntry		RestartEntryException
    rseRetryException					remove
    rseUnsupportedOperation	UnsupportedOperation
    rseUpdateNoChanges		UpdateNoChangesException
com.architech.function		com.ibm.di.function
    eventFunctions		EventFunctions	
    executeCommand		ExecuteCommand
    httpFunctions		HTTPFunctions
    mailMessage			MailMessage
    nho_functions					remove
    systemFunctions		SystemFunctions
    userFunctions2		UserFunctions
com.architech.parser		com.ibm.di.parser
    rspCSVParser		CSVParser
    rspDSML			DSMLParser
    rspEDBFile			EDBFileParser
    rspFixed			FixedRecordParser
    rspHttpParser		HTTPParser
    rspLdif			LDIFParser
    rspOnmail						remove
    rspParserInterface  	ParserInterface
    rspParser			ParserImpl
    rspRigal70			Rigal70Parser
    rspScriptParser		ScriptParser
    rspSimpleParser		SimpleParser
    rspSoapParser		SOAPParser
    rspXml2						remove
    rspXmlSax			XMLSaxParser
    rspXml			XMLParser
com.architech.protocols		com.ibm.di.protocols
com.architech.script.ASPerl				remove
com.architech.script		com.ibm.di.script
    scriptEngine		ScriptEngine
    scriptExitCode		ScriptExitCode
com.architech.security		com.ibm.di.security
    rssEncryptedReader		EncryptedReader
    rssEncryptedWriter		EncryptedWriter
    rssKey			SecurityKey
    rssCrypto			SecurityCrypto
com.architech.switchboard	com.ibm.di.eventhandler
com.architech.trigger		com.ibm.di.trigger
    rstAdminPort					remove
    rstBaseClass		Trigger
    rstGenericThread		GenericTrigger
    rstLDAPListener					remove
    rstMailbox						remove
    rstProxyServer					remove
    rstSnmpTrap						remove
    rstSonicMQ						remove
    rstTimer			TimerTrigger
    rstTriggerInterface		TriggerInterface
    rstTrigger						remove
    rstMBean						remove
com.architech.util    		com.ibm.di.util
    rsuHttpUtil			HTTPUtils
    rsuString			StringUtils
    xmlUtils			XMLUtils
com.architech.webService	com.ibm.di.webService
com.architech			com.ibm.di.server
    dsCompare  			Compare
    dsEntry    						remove
    jobStatus			JobStatus
    rsConstants			ServerConstants
    rsLog			Log
    rsMonitor			Monitor
    rsStats			TaskStatistics
    rscDeltaTaskComponent	DeltaTaskComponent
    rscScriptComponent		ScriptComponent
    rscSearchCriteria		SearchCriteria
    rscTaskComponent		AssemblyLineComponent
    rscTask			AssemblyLine
    rscVersion			Version
    adminServer						remove
    fileConfig			FileConfig
com.metamerge.management	com.ibm.di.management
com.metamergeloader		com.ibm.di.loader
    miloader		IDILoader
com.metamerge.miadmin		com.ibm.di.admin
com.metamerge.config.base	com.ibm.di.config.base
com.metamerge.config.xml	com.ibm.di.config.xml
com.metamerge.config.convert				remove
com.metamerge.config		com.ibm.di.config.interfaces
';

$len = 0;
for $line ( split('\n', $conv) ) {
	next unless $line =~ /(\S+)\s+(\S+)/;
	($o, $n) = ($1, $2);
	if ($o =~ /\./) {
		($old[$len], $new[$len]) = ($o, $n);
		$old[$len] =~ s/\./\//g;
		$new[$len] =~ s/\./\//g;
		$len++;
		$o =~ s/\./\\./g;
	}		
	($old[$len], $new[$len], $len) = ($o, $n, $len+1);
}

$extension = ".orig";

while (<>) {
	changefile() if $ARGV ne $oldargv;
	next if $skip;
	print subst($_);
}
		
sub changefile {
	$oldargv = $ARGV;
	select(STDOUT);
	$backup = $ARGV . $extension;
	rename($ARGV, $backup);
	$filename = subst($ARGV);
	if ($filename =~ /remove/) {
		print "$ARGV -- removed\n";
	   $skip = 1;
	   return;
	}
	print "$ARGV --> $filename\n";
	mkdirs($filename);
	open(ARGVOUT, ">$filename");
	select(ARGVOUT);
	$skip = 0;
}

sub subst {
    $string = $_[0];
    for $i (0..$len-1) {
		$string =~ s/\b$old[$i]\b/$new[$i]/g;
    }
    return $string;
}

sub mkdirs {
	@dirs = split('/', $_[0]);
	return if $#dirs < 2;
	$dir = $dirs[0];
	for $i (1..$#dirs-1) {
		$dir .= '/' . $dirs[$i];
		-d $dir || mkdir $dir;
	}
}
