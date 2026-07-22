/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.systemqueue.driver;

import com.ibm.di.api.security.CryptoUtils;
import com.ibm.di.script.ScriptEngine;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.RS;

import java.util.Hashtable;

import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;

/**
 * The Script JMS Driver implementation. This driver will act as a bridge
 * between the System Queue and user-specified script It initialize the JMS
 * Driver from script and provides specific way for obtaining JMS
 * QueueConnectionFactory.
 */
public class JMSScriptDriver implements JMSDriver {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/*
	 * JMS Driver property name used in Script Driver
	 */
	private static final String PROP_JS_FILE = "js.jsfile";

	/*
	 * JMS Driver property name used in Script Driver
	 */
	public static final String PROP_JS_SCRIPT = "js.jsscript";

	/*
	 * Script engine that runs provided script
	 */
	private ScriptEngine mScriptEngine = null;

	/*
	 * Object used for locking script engine, while obtaining Ret object
	 */
	private Object mEngineLock = new Object();

	/*
	 * QueueConnectionFactory object obtained from the Script
	 */
	private QueueConnectionFactory queueConnectionFactory = null;

	/*
	 * TopicConnectionFactory object obtained from the Script
	 */
	private TopicConnectionFactory topicConnectionFactory = null;

	protected JMSDriverLog log = null;

	private static ResourceHash sResHash = ResourceHash
			.getHash(JMSDriver.JMS_DRIVER_TMS_FILE);

	/**
	 * The initialize(Hashtable env) method is passed a java.util.Hashtable
	 * object which stores provider-specific parameters, which can be used for
	 * connecting to a specific instance of the JMS server. Normally this method
	 * would use the supplied parameters to connect to the JMS server and obtain
	 * a javax.jms.TopicConnectionFactory object and/or a
	 * javax.jms.QueueConnectionFactory object. Then the method would store the
	 * object(s) in member variables so that it/they can be later retrieved via
	 * the getQueueFactory() and/or the getTopicFactory() method.
	 * 
	 * @param env
	 *            Hashtable that holds Driver properties
	 * 
	 * @throws Exception
	 *             if JMS Driver cannot be initialized
	 */
	public void initialize(Hashtable env) throws Exception {

		Object obj = env.get(ENVIRONMENT_LOG);
		if (obj instanceof JMSDriverLog)
			log = (JMSDriverLog) obj;
		else
			log = new NullLogger();

		String customDriverScript = (String) env.get(PROP_JS_SCRIPT);

		if (customDriverScript == null
				|| customDriverScript.trim().length() == 0) {
			String jsFileName = (String) env.get(PROP_JS_FILE);
			if (jsFileName == null || jsFileName.trim().length() == 0) {
				log.logErrorAndThrowException(sResHash
						.getString("prop.jms.javascript.file.wasnot.provided"));
			} else {
				try {
					jsFileName = jsFileName.trim();
					customDriverScript = new String(CryptoUtils
							.readFile(jsFileName));
				} catch (java.io.IOException e) {
					log.logErrorAndThrowException(sResHash.getString(
							"unable.to.read.custom.jsscript.fromfile",
							jsFileName), e);
				}
			}
		}

		customDriverScript = customDriverScript.trim();
		try {
			mScriptEngine = new ScriptEngine("javascript", RS.getServer());
		} catch (Exception e) {
			log.logErrorAndThrowException(sResHash
					.getString("unable.to.initialize.script.engine"), e);
		}

		Ret ret = new Ret();
		try {
			synchronized (mEngineLock) {
				mScriptEngine.declareBean("ret", ret);
				mScriptEngine.declareBean("env", env);
				mScriptEngine.declareStaticBean("main", RS.gRS,
						com.ibm.di.server.RSInterface.class);
				mScriptEngine.exec(customDriverScript);
			}
		} catch (Exception e) {
			log
					.logErrorAndThrowException(sResHash
							.getString("script.error"), e);
		}

		if (ret != null) {
			if (ret.errorcode != null) {
				throw new Exception(sResHash.getString(
						"exception.script.driver.initialization",
						ret.errordescr));
			}

			queueConnectionFactory = ret.queueConnectionFactory;
			topicConnectionFactory = ret.topicConnectionFactory;
		}
	}

	/**
	 * This method retrieves the provider-specific
	 * javax.jms.QueueConnectionFactory object
	 * 
	 * @return QueueConnectionFactory object of the JMS Driver
	 * @throws Exception
	 *             if QueueConnectionFactory cannot be created
	 */
	public QueueConnectionFactory getQueueFactory() throws Exception {
		return queueConnectionFactory;
	}

	/**
	 * This method retrieves the provider-specific
	 * javax.jms.TopicConnectionFactory object
	 * 
	 * @return TopicConnectionFactory object of the JMS Driver
	 * @throws Exception
	 *             if TopicConnectionFactory cannot be created
	 */
	public TopicConnectionFactory getTopicFactory() throws Exception {
		return topicConnectionFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	public void terminate() throws Exception {
	}

	/**
	 * Class used for returning object that holds QueueConnectionFactory,
	 * TopicConnectionFactory or error message from the script.
	 */
	public static class Ret {

		/*
		 * Error description if an error occur in the script
		 */
		public String errordescr = "";

		/*
		 * Error code if an error occur in the script
		 */
		public Object errorcode = null;

		/*
		 * QueueConnectionFactory object obtained from the Script
		 */
		public QueueConnectionFactory queueConnectionFactory = null;

		/*
		 * TopicConnectionFactory object obtained from the Script
		 */
		public TopicConnectionFactory topicConnectionFactory = null;
	}
}
