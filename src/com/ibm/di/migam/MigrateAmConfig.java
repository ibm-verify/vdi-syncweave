/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.migam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ibm.di.migration.BaseMigrationUtility;
import com.ibm.di.migration.ChangeDescription;
import com.ibm.di.server.ResourceHash;

/**
 * The MigrateAmConfig class is a small program that will migrate any
 * am.properties file from 6.0, 6.1, 6.1.1, or 7.0 to 7.1. See the main method
 * for information of valid parameters that can be passed into the command. The
 * program relies on the icu4j library for globalization. It relies on log4j for
 * logging.
 */
public class MigrateAmConfig extends BaseMigrationUtility {

	/**
	 * Resource bundle (Locale specific) Filename: migrategblprops.properties.
	 * Generated from TMS XML file migrategblprops.xml
	 */
	private static ResourceHash resHash = ResourceHash.getHash("migrateamprops");

	/**
	 * Constant specifies the command completed successfully.
	 */
	private static final int RC_OK = 0;

	/**
	 * Constant specifies the command failed.
	 */
	private static final int RC_FAIL = -1;

	// SSL properties - since TDI 7.0
	private static final String AM_SSL_COMMENT = " Action Manager SSL properties";
	private static final String AM_SSL_PROP_TRUSTSTORE = "javax.net.ssl.trustStore";
	private static final String AM_SSL_PROP_TRUSTSTORE_PASS = "javax.net.ssl.trustStorePassword";
	private static final String AM_SSL_PROP_TRUSTSTORE_TYPE = "javax.net.ssl.trustStoreType";
	private static final String AM_SSL_PROP_KEYSTORE = "javax.net.ssl.keyStore";
	private static final String AM_SSL_PROP_KEYSTORE_PASS = "javax.net.ssl.keyStorePassword";
	private static final String AM_SSL_PROP_KEYSTORE_TYPE = "javax.net.ssl.keyStoreType";

	private static final String AM_SSL_DEFAULT_TRUSTSTORE = "testadmin.jks";
	private static final String AM_SSL_DEFAULT_KEYSTORE = "testadmin.jks";

	// SMTP properties - since TDI 7.0
	private static final String AM_SMTP_COMMENT = " SMTP server properties";
	private static final String AM_SMTP_HOST = "smtp.host";
	private static final String AM_SMTP_PORT = "smtp.port";
	private static final String AM_SMTP_USER = "smtp.user";
	private static final String AM_SMTP_PASS = "smtp.password";

	// Encryption properties - since TDI 7.1
	private static final String AM_ENC_COMMENT = " Action Manager encryption properties";
	private static final String AM_ENC_PROP_KEYSTORE = "com.ibm.di.amc.am.encryption.keystore";
	private static final String AM_ENC_PROP_ALIAS = "com.ibm.di.amc.am.encryption.key.alias";
	private static final String AM_ENC_PROP_KEYSTORE_TYPE = "com.ibm.di.amc.am.encryption.keystoretype";
	private static final String AM_ENC_PROP_TRANS = "com.ibm.di.amc.am.encryption.transformation";
	private static final String AM_ENC_PROP_STASH_FILE = "com.ibm.di.amc.am.stash.file";

	private static final String AM_ENC_DEFAULT_KEYSTORE = "testserver.jks";
	private static final String AM_ENC_DEFAULT_STASH_FILE = "idisrv.sth";

	// Interval properties
	private static final String AM_STATUSUPDATE_INTERVAL_COMMENT = " Controls the frequency of triggering AM update thread";
	private static final String AM_STATUSUPDATE_INTERVAL = "com.ibm.di.amc.am.statusUpdate.interval.time";

	private static final String AM_FAIL_INTERVAL = "com.ibm.di.amc.am.serverapi.fail.interval.time";
	private static final String AM_QUERY_AL_INTERVAL = "com.ibm.di.amc.am.queryAL.interval.time";
	private static final String AM_HEALTH_AL_INTERVAL = "com.ibm.di.amc.am.healthAL.interval.time";
	private static final String AM_QUERY_PROP_INTERVAL = "com.ibm.di.amc.am.queryProperty.interval.time";

	// AM DB type - since TDI 7.0
	private static final String AM_DB_TYPE_COMMENT = " Specifies the database being used by the Action Manager";
	private static final String AM_DB_TYPE = "am.db.type";

	// JDBC properties - since TDI 6.1 at least
	private static final String AM_JDBC_NWSERVER_PORT = "com.ibm.di.amc.am.jdbc.networkserver.por";
	private static final String AM_JDBC_USER = "com.ibm.di.amc.am.jdbc.user";
	private static final String AM_JDBC_PASS = "com.ibm.di.amc.am.jdbc.password";

	// AMC props location - commented in TDI 7.1
	private static final String AM_AMC_PROPS = "com.ibm.di.amc.properties.file.location";

	// Protect prefix - since TDI 7.0 for three passwords in
	// am_config.properties
	private static final String PROTECT_PREFIX = "{protect}-";

	/**
	 * Holds the install directory of TDI. This is passed as environment
	 * variable.
	 */
	private String targetDir;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * @param args
	 * @param log
	 */
	public MigrateAmConfig(String[] args, Logger log) {
		super(args, log);
	}

	/**
	 * Sets TDI installation folder.
	 * 
	 * @param tdiDir
	 */
	public void setTdiDir(String tdiDir) {
		tdiDir = tdiDir.replace('\\', '/');
		if (!tdiDir.endsWith("/")) {
			tdiDir = tdiDir + "/";
		}
		this.targetDir = tdiDir;
	}

	@Override
	protected void parseArgs(String[] args) {
		if (isVerboseMode()) {
			message(resHash.getString("COMMAND.PARSING.OPTIONS"));
		}
		
		super.parseArgs(args);
		
		if (isVerboseMode()) {
			String[] switches = { SWITCH_MIG_FILE, SWITCH_MIG_FILE_BAKUP, SWITCH_MIG_FILE_NEW, SWITCH_VERBOSE };
			StringBuilder genOptions = new StringBuilder("{");
			for (int i = 0; i < switches.length; i++) {
				String value = getCommandValueBySwitch(switches[i]);
				if (value != null && value.length() > 0) {
					genOptions.append(switches[i] + "=" + value + " ");
				}
			}
			genOptions.replace(genOptions.length() - 1, genOptions.length(), "}");
			message(resHash.getString("COMMAND.GENERAL.OPTIONS", genOptions.toString()));
		}
	}
	
	@Override
	protected void interpretCommandLineOptions() {
		super.interpretCommandLineOptions();
		setTdiDir(System.getProperty("tdi.install.dir"));
	}

	@Override
	protected List<ChangeDescription> defineChanges(Map<String, String> props) {
		List<ChangeDescription> changes = new ArrayList<ChangeDescription>();

		if (!props.containsKey(AM_DB_TYPE)) {
			changes.add(new ChangeDescription(AM_DB_TYPE, ChangeDescription.TYPE_ADD, "derby", AM_JDBC_NWSERVER_PORT,
					new String[] { AM_DB_TYPE_COMMENT }, 1, 0));
		}

		if (!props.containsKey(AM_STATUSUPDATE_INTERVAL)) {
			changes.add(new ChangeDescription(AM_STATUSUPDATE_INTERVAL, ChangeDescription.TYPE_ADD, "60", AM_HEALTH_AL_INTERVAL,
					new String[] { AM_STATUSUPDATE_INTERVAL_COMMENT }, 1, 0));
		}

		if (!props.containsKey(AM_SMTP_HOST)) {
			changes.add(new ChangeDescription(AM_SMTP_HOST, ChangeDescription.TYPE_ADD, "", AM_STATUSUPDATE_INTERVAL,
					new String[] { AM_SMTP_COMMENT }, 1, 0));
			changes.add(new ChangeDescription(AM_SMTP_PORT, ChangeDescription.TYPE_ADD, "", AM_SMTP_HOST));
			changes.add(new ChangeDescription(AM_SMTP_USER, ChangeDescription.TYPE_ADD, "", AM_SMTP_PORT));
			changes.add(new ChangeDescription(AM_SMTP_PASS, ChangeDescription.TYPE_ADD, "", AM_SMTP_USER));
		}

		// Modified properties

		// pre TDI 7.0 - no SSL properties
		if (!props.containsKey(AM_SSL_PROP_TRUSTSTORE) && !props.containsKey(AM_SSL_PROP_KEYSTORE)) {

			changes.add(new ChangeDescription(AM_SSL_PROP_TRUSTSTORE, ChangeDescription.TYPE_ADD, targetDir + "serverapi/"
					+ AM_SSL_DEFAULT_TRUSTSTORE, AM_DB_TYPE, new String[] { AM_SSL_COMMENT }, 1, 0));
			changes.add(new ChangeDescription(PROTECT_PREFIX + AM_SSL_PROP_TRUSTSTORE_PASS, ChangeDescription.TYPE_ADD,
					"administrator", AM_SSL_PROP_TRUSTSTORE));
			changes.add(new ChangeDescription(AM_SSL_PROP_TRUSTSTORE_TYPE, ChangeDescription.TYPE_ADD, "jks", PROTECT_PREFIX
					+ AM_SSL_PROP_TRUSTSTORE_PASS));

			changes.add(new ChangeDescription(AM_SSL_PROP_KEYSTORE, ChangeDescription.TYPE_ADD, targetDir + "serverapi/"
					+ AM_SSL_DEFAULT_KEYSTORE, AM_SSL_PROP_TRUSTSTORE_TYPE));
			changes.add(new ChangeDescription(PROTECT_PREFIX + AM_SSL_PROP_KEYSTORE_PASS, ChangeDescription.TYPE_ADD,
					"administrator", AM_SSL_PROP_KEYSTORE));
			changes.add(new ChangeDescription(AM_SSL_PROP_KEYSTORE_TYPE, ChangeDescription.TYPE_ADD, "jks", PROTECT_PREFIX
					+ AM_SSL_PROP_KEYSTORE_PASS, null, 0, 1));

		} else if (props.containsKey(AM_SSL_PROP_TRUSTSTORE) && props.containsKey(AM_SSL_PROP_KEYSTORE)) {

			// Mark truststore pass as protected
			if (props.containsKey(AM_SSL_PROP_TRUSTSTORE_PASS)) {
				String trustPass = props.get(AM_SSL_PROP_TRUSTSTORE_PASS);
				changes.add(new ChangeDescription(AM_SSL_PROP_TRUSTSTORE_PASS, ChangeDescription.TYPE_DELETE));
				changes.add(new ChangeDescription(PROTECT_PREFIX + AM_SSL_PROP_TRUSTSTORE_PASS, ChangeDescription.TYPE_ADD,
						trustPass, AM_SSL_PROP_TRUSTSTORE));
			}

			// mark keystore pass as protected
			if (props.containsKey(AM_SSL_PROP_KEYSTORE_PASS)) {
				String keyPass = props.get(AM_SSL_PROP_KEYSTORE_PASS);
				changes.add(new ChangeDescription(AM_SSL_PROP_KEYSTORE_PASS, ChangeDescription.TYPE_DELETE));
				changes.add(new ChangeDescription(PROTECT_PREFIX + AM_SSL_PROP_KEYSTORE_PASS, ChangeDescription.TYPE_ADD,
						keyPass, AM_SSL_PROP_KEYSTORE));
			}

			String trustStore = props.get(AM_SSL_PROP_TRUSTSTORE);
			String keyStore = props.get(AM_SSL_PROP_KEYSTORE);

			// change default truststore location for TDI 7.1
			if (trustStore.equals(targetDir + "bin/amc/ActionManager/" + AM_SSL_DEFAULT_TRUSTSTORE)
					|| trustStore.equals("ActionManager/" + AM_SSL_DEFAULT_TRUSTSTORE) 
					|| trustStore.equals("$change$/bin/amc/ActionManager/" + AM_SSL_DEFAULT_TRUSTSTORE)) {
				changes.add(new ChangeDescription(AM_SSL_PROP_TRUSTSTORE, ChangeDescription.TYPE_MODIFY, targetDir + "serverapi/"
						+ AM_SSL_DEFAULT_TRUSTSTORE));

				// set this just in case
				changes.add(new ChangeDescription(PROTECT_PREFIX + AM_SSL_PROP_TRUSTSTORE_PASS, ChangeDescription.TYPE_MODIFY,
						"administrator"));
				changes.add(new ChangeDescription(AM_SSL_PROP_TRUSTSTORE_TYPE, ChangeDescription.TYPE_MODIFY, "jks"));
			}

			// change default keystore location for TDI 7.1
			if (keyStore.equals(targetDir + "bin/amc/ActionManager/" + AM_SSL_DEFAULT_KEYSTORE)
					|| keyStore.equals("ActionManager/" + AM_SSL_DEFAULT_KEYSTORE)
					|| keyStore.equals("$change$/bin/amc/ActionManager/" + AM_SSL_DEFAULT_KEYSTORE)) {
				changes.add(new ChangeDescription(AM_SSL_PROP_KEYSTORE, ChangeDescription.TYPE_MODIFY, targetDir + "serverapi/"
						+ AM_SSL_DEFAULT_KEYSTORE));

				// set this just in case
				changes.add(new ChangeDescription(PROTECT_PREFIX + AM_SSL_PROP_KEYSTORE_PASS, ChangeDescription.TYPE_MODIFY,
						"administrator"));
				changes.add(new ChangeDescription(AM_SSL_PROP_KEYSTORE_TYPE, ChangeDescription.TYPE_MODIFY, "jks"));
			}

		}

		// Mark jdbc pass as protected
		if (props.containsKey(AM_JDBC_PASS)) {
			String jdbcPass = props.get(AM_JDBC_PASS);
			changes.add(new ChangeDescription(AM_JDBC_PASS, ChangeDescription.TYPE_DELETE));
			changes.add(new ChangeDescription(PROTECT_PREFIX + AM_JDBC_PASS, ChangeDescription.TYPE_ADD, jdbcPass, AM_JDBC_USER));
		}

		// New properties for encryption - since TDI 7.1
		changes.add(new ChangeDescription(AM_ENC_PROP_KEYSTORE, ChangeDescription.TYPE_ADD, targetDir + AM_ENC_DEFAULT_KEYSTORE,
				AM_SSL_PROP_KEYSTORE_TYPE, new String[] { AM_ENC_COMMENT }, 1, 0));
		changes.add(new ChangeDescription(AM_ENC_PROP_ALIAS, ChangeDescription.TYPE_ADD, "server", AM_ENC_PROP_KEYSTORE));
		changes.add(new ChangeDescription(AM_ENC_PROP_KEYSTORE_TYPE, ChangeDescription.TYPE_ADD, "jks", AM_ENC_PROP_ALIAS));
		changes.add(new ChangeDescription(AM_ENC_PROP_TRANS, ChangeDescription.TYPE_ADD, "RSA", AM_ENC_PROP_KEYSTORE_TYPE));
		changes.add(new ChangeDescription(AM_ENC_PROP_STASH_FILE, ChangeDescription.TYPE_ADD,
				targetDir + AM_ENC_DEFAULT_STASH_FILE, AM_ENC_PROP_TRANS));

		// Commented properties
		if (props.containsKey(AM_AMC_PROPS)) {
			changes.add(new ChangeDescription(AM_AMC_PROPS, ChangeDescription.TYPE_COMMENT));
		}

		if (props.containsKey(AM_HEALTH_AL_INTERVAL)) {
			changes.add(new ChangeDescription(AM_HEALTH_AL_INTERVAL, ChangeDescription.TYPE_COMMENT));
		}

		if (props.containsKey(AM_FAIL_INTERVAL)) {
			changes.add(new ChangeDescription(AM_FAIL_INTERVAL, ChangeDescription.TYPE_COMMENT));
		}

		if (props.containsKey(AM_QUERY_AL_INTERVAL)) {
			changes.add(new ChangeDescription(AM_QUERY_AL_INTERVAL, ChangeDescription.TYPE_COMMENT));
		}

		if (props.containsKey(AM_QUERY_PROP_INTERVAL)) {
			changes.add(new ChangeDescription(AM_QUERY_PROP_INTERVAL, ChangeDescription.TYPE_COMMENT));
		}

		return changes;
	}

	private static String getStackTrace(Exception e) {
		StackTraceElement[] stElements = e.getStackTrace();
		StringBuffer trace = new StringBuffer();
		String tmp = e.getMessage();
		trace.append(tmp);
		for (int i = 0; i < stElements.length; i++) {
			trace.append("\n\t").append(stElements[i].toString());
		}
		return trace.toString();
	}

	/**
	 * Used to send messages out to user on the console.
	 * 
	 * @param string
	 *            The message to be sent. Should be localized.
	 */
	private static void message(String string) {
		System.out.println(string);
	}

	@Override
	protected void printHelpInformation() {
		message(resHash.getString("QUERY_USAGE"));
	}

	@Override
	protected void validateCommandLineOptions() {
		super.validateCommandLineOptions();

		if (!isHelpRequested() && getCommandValueBySwitch(SWITCH_MIG_FILE) == null) {
			throw new IllegalArgumentException(resHash.getString("FILE_OPTION_ABSENT", SWITCH_MIG_FILE));
		}

		List<String> opt = getCommandStandaloneValuesList();
		for (int i = 0; i < opt.size(); i++) {
			String key = opt.get(i++);
			String value = getCommandValueBySwitch(key);

			// check if already provided with value
			if (value != null
					&& (key.equals(SWITCH_MIG_FILE) || key.equals(SWITCH_MIG_FILE_BAKUP) || key.equals(SWITCH_MIG_FILE_NEW))) {
				throw new IllegalArgumentException(resHash.getString("OPT_OCCUR_TWICE", key));
			} else if (!key.equals(SWITCH_VERBOSE) || !key.equals(SWITCH_HELP)) {
				throw new IllegalArgumentException(resHash.getString("UNKNOWN_OPT", key));
			}
		}

	}

	/**
	 * The main method of the Migrate Global Properties Command. The command
	 * exits with a 0 if it completes successfully. If the command failes it
	 * exits with a -1.
	 * 
	 * @param args
	 *            The arguments passed into the command. Valid arguments are:
	 * 
	 */
	public static void main(String args[]) {
		int retCode = RC_FAIL;

		// Initialize the migration utility and logging...
		MigrateAmConfig migUtil = new MigrateAmConfig(args, Logger.getLogger("com.ibm.di.miggbl.tdimigam"));
		try {
			if (migUtil.isVerboseMode()) {
				message(resHash.getString("COMMAND.START.MIGRATION"));
			}
			migUtil.migrateFile();
			retCode = RC_OK;
		} catch (Exception ex) {
			migUtil.getLog().error(getStackTrace(ex));
		}

		if (retCode != RC_OK) {
			// The command did not finish successfully. Print WARNING message.
			message(resHash.getString("COMMAND_ERROR_occurred"));
		}

		if (migUtil.isVerboseMode()) {
			message(resHash.getString("COMMAND.RETURN.CODE", "" + retCode));
		}

		// Exit with the specified return code.
		System.exit(retCode);
	}

}
