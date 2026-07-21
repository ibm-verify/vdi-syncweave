var opEntry = task.getOpEntry();

task.dumpEntry(opEntry);

var body;

var subject;

var okSubject = opEntry.getString("mail.OKSubject");
if (okSubject == null)
	okSubject = "Everything is fine";

var failSubject = opEntry.getString("mail.failureSubject");
if (failSubject == null)
	failSubject = "There is a problem";

var smtpHost = opEntry.getString("smtp.Host");
if (smtpHost == null)
	smtpHost = java.lang.System.getProperty("mail.smtp.host");

var smtpUser = opEntry.getString("smtp.User");
if (smtpUser == null)
	smtpUser = java.lang.System.getProperty("mail.smtp.user");

var smtpPassword = opEntry.getString("smtp.Password");
if (smtpPassword == null)
	smtpPassword = java.lang.System.getProperty("mail.smtp.password");

var manager = com.ibm.di.api.APIEngine.getTombstoneManager();

if (manager == null) {
	body = "No TomstoneManager, remember to set com.ibm.di.tm.on=true and com.ibm.di.tm.create.all=true in solution.properties";
	subject = failSubject;
} else {

	var afterDate = null; // Last date this RunReport AL was run
	var ts = manager.getAssemblyLineTombstones(task.getName(), main.getName(),
			1);
	if (ts != null && ts.length > 0) {
		afterDate = ts[0].getTombstoneCreateTime();
	}

	var okList = "";
	var failList = "";
	var alList = opEntry.getString("mail.assemblyline").split(",");

	for (i = 0; i < alList.length; i++) {
		var name = alList[i];
		if (name == null)
			continue;

		var alName = name;
		if (alName.startsWith("/"))
			alName = alName.substring(1);
		if (!alName.startsWith("AssemblyLines/"))
			alName = "AssemblyLines/" + alName;

		configName = main.getName();

		ts = manager.getAssemblyLineTombstones(alName, configName, 1);

		if (ts == null || ts.length == 0) {
			failList += name + " has no execution history\n";
			continue;
		}

		var tombStone = ts[0];

		var endDate = tombStone.getTombstoneCreateTime();
		if (afterDate != null && endDate.before(afterDate)) {
			failList += name + " last run " + endDate + "\n";
			continue;
		}
		var statEntry = tombStone.getStatistics();
		okList += name + " ran: " + endDate + ", cycles: "
				+ statEntry.getString("get") + "\n";
	}

	if (failList.length() > 0) {
		subject = failSubject;
		body = "These AssemblyLines did not execute since the last execution of this RunReport\n"
				+ failList + "\n"
	} else if (okList.length() == 0) {
		subject = failSubject;
		body = "No AssemblyLine names were specified\n"
	} else {
		subject = okSubject;
		body = "";
	}

	if (okList.length() > 0) {
		body += "These AssemblyLines did execute since the last execution of this RunReport\n"
				+ okList;
	}
}

task.logmsg("subject = " + subject)
task.logmsg("body = " + body);

var from = opEntry.getString("mail.from");
var recipient = opEntry.getString("mail.recipient");

if (from == null) {
	task.getLog().logwarn("No sender specified, mail will not be sent");
} else if (recipient == null) {
	task.getLog().logwarn("No recipient specified, mail will not be sent");
} else if (smtpHost == null) {
	task
			.getLog()
			.logwarn(
					"The \"mail.smtp.host\" property is not set in solution.properties.");
	task.getLog().logwarn("Mail will not be sent");
} else {
	fc = system.getFunction("ibmdi.SendEMailFC");
	fc.initialize(null);
	entry = system.newEntry();
	entry.setAttribute("from", from);
	entry.setAttribute("subject", subject);
	entry.setAttribute("recipients", recipient);
	entry.setAttribute("body", body);
	entry.setAttribute("smtpServerHost", smtpHost);
	entry.setAttribute("username", smtpUser);
	entry.setAttribute("password", smtpPassword);

	try {
		fc.perform(entry);
	} catch (err) {
		task.getLog().logwarn(
				"Mail could not be sent, the exception caught was: " + err);
	}
	fc.terminate();
}