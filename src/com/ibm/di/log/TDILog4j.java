/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.log;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.SyslogAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.HtmlLayout;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.layout.XmlLayout;
import org.apache.logging.log4j.core.net.Facility;
import org.apache.logging.log4j.core.net.Protocol;
import org.apache.logging.log4j.core.config.AbstractConfiguration;

import com.ibm.di.api.DIException;
import com.ibm.di.api.syslog.SystemLogAppender;
import com.ibm.di.config.interfaces.LogConfigItem;
import com.ibm.di.config.interfaces.SolutionInterface;
import com.ibm.di.function.SystemFunctions;

/**
 * Implements LogInterface for org.apache.log4j
 */

public class TDILog4j implements LogInterface {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String CONVERSION_PATTERN = "Pattern.ConversionPattern";
	private static final String SIMPLE_LAYOUT = "%level - %m%n";

	private static final Object systemLogLock = new Object();
	private static final Object fileRollerLock = new Object();
	public Logger myLogger;
	public LoggerConfig loggerConfig;
	public String LOGGING_CLOSE = "com.ibm.di.logging.close"; // ISDISUP-93

	//	private static final String PROPERTIES_FILE = "miserver";

	//	private static ResourceHash sResHash = ResourceHash
	//			.getHash(PROPERTIES_FILE);

	private boolean mustClose;

	/**
	 * Currently we can only add loggers and appenders, not remove them again.
	 * If we are ever able to remove them, these are the names that should be removed.
	 */
	private String loggerName;
	private String appenderName;
	
	public LoggerContext ctx;
	public Configuration config;
	
	/**
	 * Public constructor
	 */
	public TDILog4j() {
		myLogger = null;
	}

	public TDILog4j(Logger logger) {
		myLogger = logger;
	}

	public void setCategory(String category) {
		myLogger = LogManager.getLogger(category);
	}
	
	public void addAppender(LogConfigItem config, Map<String,Object> map) throws Exception {
		if (config == null)
			return;

		String strAppender = config.getStringParameter(LogUtils.APPENDER);
		if (strAppender == null)
			return;

		switch (strAppender) {
		case "DailyRollingFile":
			addDailyRollingFile(config, map);
			return;
		case "IDIFileRoller":
			addIDIFileRoller(config, map);
			return;
		case "File":
			addFile(config, map);
			return;
		case "Syslog":
			addSyslog(config, map);
			return;
		case "Console":
			addConsole(config, map);
			return;
		default:
			System.out.println("Unable to add appender of type " + strAppender);
		}
	}

	public SystemLogAppender addSystemLogAppender(LogConfigItem lci, Map<String, Object> map) throws Exception {

		final String name = (String) map.get(LogInterface.NAME);
		final SystemLogAppender appender = new SystemLogAppender();
		
		synchronized (systemLogLock) {
			try {
				appender.setComponentName(name);
				appender.setMaxGenerations(lci.getStringParameter("SystemLog.MaxGenerations"));

				// Use solution ID if possible
				String id = null;
				SolutionInterface sol = lci.getMetamergeConfig().getSolutionInterface();
				if (sol != null)
					id = sol.getInstanceID();
				if (id != null && id.length() > 0)
					appender.setConfigId(id);
				else
					appender.setConfigInstance(map.get(LogInterface.CONFIG_INSTANCE));

				appender.generateFileName();

			} catch (Exception e) {
				return null;
			}

			appenderName = "SystemLog." + getId();
			loggerName = appenderName;
			String pattern = lci.getStringParameter("SystemLog.LogPattern");
			if (pattern == null)
				pattern = "%d{DEFAULT} %-5p [%c] - %m%n";

			Charset charSet = null;
			String encoding = lci.getStringParameter("SystemLog.Encoding");
			if (encoding != null && !encoding.isEmpty() && Charset.isSupported(encoding))
				charSet = Charset.forName(encoding);

	           // Initialize context and config if not already initialized
	           if (ctx == null) {
	               ctx = (LoggerContext) LogManager.getContext(false);
	           }
	           if (config == null) {
	               config = ctx.getConfiguration();
	           }

	           /*
	           ISDIDEV-567
	           Removing old logger config, context & appender in order to add new logger/appender.
	           The issue was the older config was holding lock on older log files due to that -
	           deletion was breaking and the "SystemLog.MaxGenerations" param was not working.
	           Removal of older config resoles deletion issue
	           */
	           LoggerConfig oldLoggerConfig = config.getLoggerConfig(loggerName);

            if (oldLoggerConfig != null) {
                if (oldLoggerConfig.getAppenders().containsKey(appenderName)) {
                    oldLoggerConfig.removeAppender(appenderName);
                }
                /*
                ISDIDEV-567
                This will remove logger only with name "loggerName/appendername" and 
                not root logger hence not breaking any other configured loggers
                */
                if (oldLoggerConfig.getName().equals(loggerName)) {
                    config.removeLogger(loggerName);
                }
            }

            // ISDIDEV-567 : Stopping & removing old appender
            Appender oldAppender = config.getAppender(appenderName);
            if (oldAppender != null) {
                oldAppender.stop();
                config.getAppenders().remove(appenderName);
            }

            /*
            ISDIDEV-567
            Moving this block here after stopping old appenders/loggers
            This block handles deletion of extra files
            if greater than "SystemLog.MaxGenerations" value
            */
            if (appender.getMaxGenerations() > 0) {
                try {
                    com.ibm.di.api.syslog.LogUtils.cleanOldALLogs(appender.getConfigId(), 
                                                                  name, 
                                                                  appender.getMaxGenerations() - 1);
                } catch (DIException die) {
                    SystemFunctions.doNothing();
                }
            }

            // ISDIDEV-567 : using .withAppend(false) so that logger always writes to new file
            FileAppender fileAppender = FileAppender.newBuilder()
                                                    .setName(appenderName)
                                                    .setLayout(PatternLayout
                                                               .newBuilder()
                                                               .withPattern(pattern)
                                                               .withCharset(charSet)
                                                               .build())
                                                    .withFileName(appender.getFile())
                                                    .build();

			finishAdd(fileAppender, lci);
		}
		return appender;
	}

	public void addDailyRollingFile(LogConfigItem lci, Map<String, Object> map) throws Exception {

		ctx = (LoggerContext) LogManager.getContext(false);
		config = ctx.getConfiguration();

		String fileName = lci.getStringParameter("DailyRollingFile.File");
		String datePattern = lci.getStringParameter("DailyRollingFile.DatePattern");
		String filePattern = fileName + "%d{" + datePattern + "}";
		String encoding = lci.getStringParameter("DailyRollingFile.Encoding");
		
		appenderName = "DailyRollingFile." + getId();
		loggerName = appenderName;
		
		RollingFileAppender fileAppender = RollingFileAppender.newBuilder()
				.setName(appenderName)
				.setLayout(getLayout(lci, encoding))
				.withFileName(fileName)
				.withFilePattern(filePattern)
				.withPolicy(TimeBasedTriggeringPolicy.newBuilder().build())
				.withStrategy(DefaultRolloverStrategy.newBuilder().build())
				.build();
		
		finishAdd(fileAppender, lci);
	}

    public void addIDIFileRoller(LogConfigItem lci, Map<String, Object> map) throws Exception {

        ctx = (LoggerContext) LogManager.getContext(false);
        config = ctx.getConfiguration();

        String fileName = substitute(lci.getStringParameter("IDIFileRoller.File"), map);
        // ISDIDEV-548 : using variables for encoding & rollCount
        String encoding = lci.getStringParameter("IDIFileRoller.Encoding");
        String rollCount = lci.getStringParameter("IDIFileRoller.RollCount");
        /*
        ISDIDEV-548 ->
        the rotation of log files was broken
        Removed the manual rotation : new FileRollerAppender(fileName)
        As there is lock by JVM on the files hence deletion logic was not working
        Fix Approach ->
        Adding dummy TriggeringPolicy which is size-based and then rolling file 
        manually using .rollover() method provided by log4j
        DefaultRolloverStrategy - 
        1. withFileIndex("min") : the newest log file will have index min and 
                                  the oldest one will have index max
        2. withMax(rollCount) : max no of files to keep
        attaching the new policy and strategy to RollingFileAppender
        */
        SizeBasedTriggeringPolicy policy = SizeBasedTriggeringPolicy.createPolicy("1GB");

        DefaultRolloverStrategy strategy = DefaultRolloverStrategy
                                            .newBuilder()
                                            .withMax(rollCount)
                                            .withFileIndex("min")
                                            .withConfig(config)
                                            .build();

        synchronized (fileRollerLock) {

            appenderName = "IDIFileRoller." + getId();
            loggerName = appenderName;

            RollingFileAppender fileAppender = RollingFileAppender.newBuilder()
                                            .setName(appenderName)
                                            .setLayout(getLayout(lci, encoding))
                                            .withFileName(fileName)
                                            .withFilePattern(fileName + ".%i")
                                            .withPolicy(policy)
                                            .withStrategy(strategy)
                                            .withAppend(true)
                                            .setConfiguration(config)
                                            .build();

            finishAdd(fileAppender, lci);

            // ISDIDEV-548 : manually rolling the log file
            // after the logging has been done
            fileAppender.getManager().rollover();
        }
    }
	
	public void addFile(LogConfigItem lci, Map<String, Object> map) throws Exception {
		ctx = (LoggerContext) LogManager.getContext(false);
		config = ctx.getConfiguration();
		String fileName = substitute(lci.getStringParameter("File.File"), map);
		String encoding = lci.getStringParameter("File.Encoding");
		boolean append = Boolean.valueOf(lci.getStringParameter("File.Append"));

		appenderName = "File." + getId();
		loggerName = appenderName;
		
		FileAppender fileAppender = FileAppender.newBuilder()
				.setName(appenderName)
				.setLayout(getLayout(lci, encoding))
				.withFileName(fileName)
				.withAppend(append)
				.build();
		
		finishAdd(fileAppender, lci);
	}

	public void addSyslog(LogConfigItem lci, Map<String, Object> map) throws Exception {
		ctx = (LoggerContext) LogManager.getContext(false);
		config = ctx.getConfiguration();
		appenderName = "Syslog." + getId();
		loggerName = appenderName;
		String facility = lci.getStringParameter("Syslog.Facility");
		String syslogHost = lci.getStringParameter("Syslog.SyslogHost");

		SyslogAppender appender = SyslogAppender.newSyslogAppenderBuilder()
				.setName(appenderName)
				.setLayout(getLayout(lci, null))
				.setFacility(Facility.toFacility(facility, Facility.LOCAL7))
				.withHost(syslogHost)
				.withPort(514)
				.withProtocol(Protocol.UDP)
				.build();
		
		finishAdd(appender, lci);
	}

	private void addConsole(LogConfigItem lci, Map<String, Object> map) {
		ctx = (LoggerContext) LogManager.getContext(false);
		config = ctx.getConfiguration();
		appenderName = "Console." + getId();
		loggerName = appenderName;
		ConsoleAppender appender = ConsoleAppender.newBuilder()
				.setName(appenderName)
				.setLayout(getLayout(lci, null))
				.build();
		
		finishAdd(appender, lci);
	}

    private String getId() {
        String al = Thread.currentThread().getName();

        if (al.contains("/"))
            al = al.substring(al.indexOf("/") + 1);

        return al + "." + Thread.currentThread().hashCode();
    }
	

	private void finishAdd(Appender appender, LogConfigItem lci) {
		appender.start();
		if (config == null){
			this.ctx = (LoggerContext)LogManager.getContext(false);
			this.config = this.ctx.getConfiguration();
		}
		config.addAppender(appender);

		AppenderRef[] refs = new AppenderRef[] { AppenderRef.createAppenderRef(appender.getName(), null, null) };
		String strLog = lci.getLogLevel();
		if(strLog == null) //added validation: if the log level is not set by the user, set it to Level.INFO.
			strLog = "Level.INFO";
		Level level = Level.getLevel(strLog);
		LoggerConfig loggerConfig = LoggerConfig.createLogger(false, level, loggerName,"false", refs, null, config, null );
		loggerConfig.addAppender(appender, null, null);
		config.addLogger(loggerName, loggerConfig);
		ctx.updateLoggers();

		myLogger = LogManager.getLogger(loggerName);
				 
	}

	public String substitute(String s, Map<String, Object> map) {
		if (s == null)
			return s;
		if ( ! (s.contains("{0") || s.contains("{1")))
			return s;

		Object[] substitute = new Object[] { map.get(LogInterface.NAME),
				map.get(LogInterface.TIME) };
		try {
			return MessageFormat.format(s, substitute);
		} catch (Exception ignore) {
			return s;
		}
	}

	private Layout<? extends Serializable> getLayout(LogConfigItem lci, String encoding) {
		Charset charSet = null;
		if (encoding != null && !encoding.isEmpty() && Charset.isSupported(encoding))
			charSet = Charset.forName(encoding);

		String layout = lci.getStringParameter(LogUtils.LAYOUT);
		if (layout == null)
			return null;
		switch (layout) {
		case "Simple":
			return PatternLayout.newBuilder().withPattern(SIMPLE_LAYOUT).withCharset(charSet).build();
		case "HTML":
			return HtmlLayout.newBuilder().withCharset(charSet).build();
		case "XML":
			return XmlLayout.newBuilder().build();
		default:
		return PatternLayout.newBuilder().withCharset(charSet).
				withPattern(lci.getStringParameter(CONVERSION_PATTERN)).build();

		}
	}
	
	/**
	 * Log a message with level debug.
	 * 
	 * @param str
	 *            The string to be logged
	 */

	public void debug(String str) {
		if (myLogger != null)
			myLogger.debug(str);
	}

	/**
	 * Log a message with level info.
	 * 
	 * @param str
	 *            The string to be logged
	 */

	public void info(String str) {
		if (myLogger != null)
			myLogger.info(str);
	}

	/**
	 * Log a message with level warning.
	 * 
	 * @param str
	 *            The string to be logged
	 */

	public void warn(String str) {
		if (myLogger != null)
			myLogger.warn(str);
	}

	/**
	 * Log a message with level error.
	 * 
	 * @param str
	 *            The string to be logged
	 */

	public void error(String str) {
		if (myLogger != null)
			myLogger.error(str);
	}

	/**
	 * Log a message with level error, and an additional Throwable.
	 * 
	 * @param str
	 *            The string to be logged
	 * @param error
	 *            The Throwable to be logged
	 */

	public void error(String str, Throwable error) {
		if (myLogger != null)
			myLogger.error(str, error);
	}

	/**
	 * Log a message with level fatal.
	 * 
	 * @param str
	 *            The string to be logged
	 */

	public void fatal(String str) {
		if (myLogger != null)
			myLogger.fatal(str);
	}

	/**
	 * Log a message with level fatal, and an additional Throwable.
	 * 
	 * @param str
	 *            The string to be logged
	 * @param error
	 *            The Throwable to be logged
	 */

	public void fatal(String str, Throwable error) {
		if (myLogger != null)
			myLogger.fatal(str, error);
	}

	/**
	 * Log a message with the specified level.
	 * 
	 * @param level
	 *            The level to use when logging.
	 * @param str
	 *            The string to be logged
	 */

	public void log(String level, String str) {
		if (myLogger != null)
			myLogger.log(Level.toLevel(level), str);
	}

	/**
	 * Check if a debug message would be logged.
	 * 
	 * @return true if a debug message might be logged
	 */

	public boolean isDebugEnabled() {
		if (myLogger != null)
			return myLogger.isDebugEnabled();
		else
			return false;
	}

	public void setLevel(String level) {
		if (level == null || level.isEmpty())
			return;
			
		if (myLogger instanceof org.apache.logging.log4j.core.Logger) {
			((org.apache.logging.log4j.core.Logger)myLogger).setLevel(
					Level.toLevel(level, Level.INFO));
		}
	}

	/**
	 * Free up all resources this logger uses. The logger will not be called
	 * anymore.
	 */
	public void close() {
		if ((loggerName == null) || (appenderName == null)) {
			try {
				ctx = (LoggerContext) LogManager.getContext(false);
		        config = ctx.getConfiguration();
		        // Remove the logger
		        config.removeLogger(loggerName);
		        // Remove the appender
		        ((AbstractConfiguration) config).removeAppender(appenderName);	
		        ctx.updateLoggers();
				}catch(Exception e) {				
			}
		}       		
	}
}