package com.ibm.di.test.api.mock;

import static org.easymock.EasyMock.anyBoolean;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.isNull;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.easymock.IAnswer;

import com.ibm.di.api.DIException;
import com.ibm.di.api.local.ConfigInstance;
import com.ibm.di.api.syslog.LogUtils;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.model.descriptor.ConnectorDescriptor;
import com.ibm.di.model.descriptor.FunctionComponentDescriptor;
import com.ibm.di.model.descriptor.ParserDescriptor;
import com.ibm.di.test.utils.EasyMockUtils;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class ServerAPIMockBuilder {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final NullPointerException NPE = new NullPointerException();

	// system: url to connector descriptor
	private Map<String, ConnectorDescriptor> conns = new HashMap<String, ConnectorDescriptor>();

	// system: url to function descriptor
	private Map<String, FunctionComponentDescriptor> fcs = new HashMap<String, FunctionComponentDescriptor>();

	// system: url to parser descriptor
	private Map<String, ParserDescriptor> pss = new HashMap<String, ParserDescriptor>();

	// expected notifications to be sent
	private List<CustomNotification> notifications = new LinkedList<CustomNotification>();

	// config path to config file state
	private Map<String, ConfigState> configs = new HashMap<String, ConfigState>();

	// config dirs
	private Set<String> configDirs = new TreeSet<String>();

	// config path to config to create
	private Map<String, ConfigCreate> createConfigs = new HashMap<String, ConfigCreate>();

	// ci id to ci mock
	private List<CIMock> cis = new LinkedList<CIMock>();

	// called by walk method when walking the path starting from the top. this
	// implementation pushes every path in set of configDirs.
	private PathWalker configDirsCollector = new PathWalker() {
		public void onPath(String pathElem) {
			configDirs.add(pathElem);
		}
	};

	public ServerAPIMockBuilder() {
	}

	public ServerAPIMockBuilder conn(String shortName) {
		return conn(shortName, null);
	}

	public ServerAPIMockBuilder conn(String shortName, ConnectorDescriptor desc) {
		conns.put(shortName, desc);
		return this;
	}

	public ServerAPIMockBuilder fc(String shortName) {
		return fc(shortName, null);
	}

	public ServerAPIMockBuilder fc(String shortName, FunctionComponentDescriptor desc) {
		fcs.put(shortName, desc);
		return this;
	}

	public ServerAPIMockBuilder parser(String shortName) {
		return parser(shortName, null);
	}

	public ServerAPIMockBuilder parser(String shortName, ParserDescriptor desc) {
		pss.put(shortName, desc);
		return this;
	}

	public ServerAPIMock build() throws Exception {
		return build(null);
	}

	public ServerAPIMock build(boolean activate) throws Exception {
		return build(null, activate);
	}

	public ServerAPIMock build(ServerAPIMock mock) throws Exception {
		return build(mock, true);
	}

	public ServerAPIMock build(ServerAPIMock mock, boolean activate) throws Exception {
		ServerAPIMock m = new ServerAPIMock(mock) {

			@Override
			public void activateMocks() throws Exception {
				Set<String> keySet = conns.keySet();
				expect(siMock.getInstalledConnectorsNames()).andStubReturn(keySet.toArray(new String[keySet.size()]));

				keySet = fcs.keySet();
				expect(siMock.getInstalledFunctionComponentsNames()).andStubReturn(keySet.toArray(new String[keySet.size()]));

				keySet = pss.keySet();
				expect(siMock.getInstalledParsersNames()).andStubReturn(keySet.toArray(new String[keySet.size()]));

				for (CustomNotification notify : notifications) {
					sMock.sendCustomNotification(eq(notify.type), eq(notify.id), EasyMockUtils.getEqualsMatcher(notify.payload));
					expectLastCall().times(1);
				}

				expect(sMock.listFolders((String) isNull())).andThrow(NPE).anyTimes();
				expect(sMock.listFolders((String) anyObject())).andAnswer(new ListFolders_String()).anyTimes();

				expect(sMock.listConfigurations((String) isNull())).andThrow(NPE).anyTimes();
				expect(sMock.listConfigurations((String) anyObject())).andAnswer(new ListConfigurations_String()).anyTimes();

				expect(sMock.isConfigurationCheckedOut((String) isNull())).andThrow(NPE).anyTimes();
				expect(sMock.isConfigurationCheckedOut((String) anyObject())).andAnswer(new IsConfigurationCheckedOut_String())
						.anyTimes();

				int loadCount = 0;
				for (ConfigCreate cc : createConfigs.values()) {
					if (cc.mock != null) {
						++loadCount;
					}
				}

				if (loadCount > 0) {
					expect(sMock.createNewConfigurationAndLoad((String) isNull(), anyBoolean())).andThrow(NPE).anyTimes();
					expect(sMock.createNewConfigurationAndLoad((String) anyObject(), anyBoolean())).andAnswer(
							new CreateNewConfigurationAndLoad_String_Boolean()).times(loadCount);
				}

				if (createConfigs.size() - loadCount > 0) {
					expect(sMock.createNewConfiguration((String) isNull(), anyBoolean())).andThrow(NPE).anyTimes();
					expect(sMock.createNewConfiguration((String) anyObject(), anyBoolean())).andAnswer(
							new CreateNewConfiguration_String_Boolean()).times(createConfigs.size() - loadCount);
				}

				expect(sMock.checkOutConfiguration((String) isNull())).andThrow(NPE).anyTimes();
				expect(sMock.checkOutConfiguration((String) anyObject())).andStubAnswer(new CheckOutConfiguration_String());

				expect(sMock.checkOutConfiguration((String) isNull(), (String) anyObject())).andThrow(NPE).anyTimes();
				expect(sMock.checkOutConfiguration((String) anyObject(), (String) isNull())).andThrow(NPE).anyTimes();
				expect(sMock.checkOutConfiguration((String) anyObject(), (String) anyObject())).andStubAnswer(
						new CheckOutConfiguration_String_String());

				sMock.checkInConfiguration((MetamergeConfig) isNull(), (String) anyObject());
				expectLastCall().andThrow(NPE).anyTimes();
				sMock.checkInConfiguration((MetamergeConfig) anyObject(), (String) isNull());
				expectLastCall().andThrow(NPE).anyTimes();
				sMock.checkInConfiguration((MetamergeConfig) anyObject(), (String) anyObject());
				expectLastCall().andAnswer(new CheckInConfiguration_MC_String()).anyTimes();

				sMock.checkInConfiguration((MetamergeConfig) isNull(), (String) anyObject(), anyBoolean());
				expectLastCall().andThrow(NPE).anyTimes();
				sMock.checkInConfiguration((MetamergeConfig) anyObject(), (String) isNull(), anyBoolean());
				expectLastCall().andThrow(NPE).anyTimes();
				sMock.checkInConfiguration((MetamergeConfig) anyObject(), (String) anyObject(), anyBoolean());
				expectLastCall().andAnswer(new CheckInConfiguration_MC_String_Boolean()).anyTimes();

				sMock.checkInAndLeaveCheckedOut((MetamergeConfig) isNull(), (String) anyObject());
				expectLastCall().andThrow(NPE).anyTimes();
				sMock.checkInAndLeaveCheckedOut((MetamergeConfig) anyObject(), (String) isNull());
				expectLastCall().andThrow(NPE).anyTimes();
				sMock.checkInAndLeaveCheckedOut((MetamergeConfig) anyObject(), (String) anyObject());
				expectLastCall().andAnswer(new CheckInAndLeaveCheckedOut_MC_String()).anyTimes();

				sMock.releaseConfigurationLock((String) isNull());
				expectLastCall().andThrow(NPE).anyTimes();
				expect(sMock.releaseConfigurationLock((String) anyObject())).andAnswer(new ReleaseConfigurationLock()).anyTimes();
				
				sMock.undoCheckOut((String) isNull());
				expectLastCall().andThrow(NPE).anyTimes();
				expect(sMock.undoCheckOut((String) anyObject())).andAnswer(new ReleaseConfigurationLock()).anyTimes();

				for (CIMock m : cis) {
					m.activateMocks();
				}

				super.activateMocks();
			}

			@Override
			public AftermathAccessor verifyMockCalls() {
				AftermathAccessor accessor = super.verifyMockCalls();

				for (CIMock m : cis) {
					m.verifyMockCalls();
				}

				accessor.setServerInfoComponents(conns, fcs, pss);
				accessor.setConfigurationFiles(configDirs, configs);
				accessor.setCIs(cis);

				return accessor;
			}

			@Override
			public void resetMockCalls() {
				super.resetMockCalls();

				for (CIMock m : cis) {
					m.resetMockCalls();
				}
			}
		};

		if (activate) {
			m.activateMocks();
		}

		return m;
	}

	public ServerAPIMockBuilder notification(String type, String id, Object payload) {
		notifications.add(new CustomNotification(type, id, payload));
		return this;
	}

	public ServerAPIMockBuilder configDir(String path) {
		walk(path, configDirsCollector);
		return this;
	}

	public ServerAPIMockBuilder configFile(String path) {
		return configFile(path, null);
	}

	public ServerAPIMockBuilder createCfg(String path, boolean overwrite) {
		path = walk(path, null);
		createConfigs.put(path, new ConfigCreate(overwrite, null));
		return this;
	}

	public CIMock createCfgAndLoad(String path, boolean overwrite) {
		path = walk(path, null);
		CIMock mock = new CIMock(LogUtils.getCleanConfigId(path), this);
		createConfigs.put(path, new ConfigCreate(overwrite, mock));
		cis.add(mock);
		return mock;
	}

	public ServerAPIMockBuilder configFile(String path, MetamergeConfig cfg) {
		return configFile(path, cfg, false);
	}

	public ServerAPIMockBuilder configFile(String path, MetamergeConfig cfg, boolean encrypted) {
		return configFile(path, cfg, encrypted, null);
	}

	public ServerAPIMockBuilder configFile(String path, MetamergeConfig cfg, boolean encrypted, String password) {
		int pos = lastSeparator(path);

		if (pos > -1) {
			String correctPath = walk(path.substring(0, pos), configDirsCollector);
			path = correctPath + "/" + path.substring(pos + 1);
		}

		configs.put(path, new ConfigState(cfg, false, encrypted, password));
		return this;
	}

	private int lastSeparator(String path) {
		return Math.max(path.lastIndexOf('\\'), path.lastIndexOf('/'));
	}

	/**
	 * Walks the provided path and returns a normalized version of the same
	 * path.
	 * 
	 * @param path
	 *            the path to walk
	 * @param w
	 *            the object that gets notified on every element from the path.
	 * @return the normalized path walked
	 */
	static String walk(String path, PathWalker w) {
		int lastPos = 0;
		int prevPos = lastPos;
		int backPos;
		int forPos;
		String pathSoFar = "";

		do {
			backPos = path.indexOf('\\', prevPos);
			forPos = path.indexOf('/', prevPos);
			lastPos = Math.min(forPos, backPos);
			if (lastPos == -1) {
				lastPos = forPos == -1 ? backPos : forPos;
			}

			if (lastPos == -1) {
				// reached to the last element
				lastPos = path.length();
			}

			String nextElem = path.substring(prevPos, lastPos).trim();

			if (nextElem.length() > 0 && !".".equals(nextElem)) {
				if (pathSoFar.length() > 0) {
					pathSoFar += "/";
				}
				pathSoFar += nextElem;
			}
			if (w != null) {
				w.onPath(pathSoFar);
			}
			prevPos = lastPos + 1;
		} while (lastPos < path.length());

		return pathSoFar;
	}

	static interface PathWalker {
		public void onPath(String pathElem);
	}

	static class CustomNotification {
		String type;
		String id;
		Object payload;

		public CustomNotification(String type, String id, Object payload) {
			this.type = type;
			this.id = id;
			this.payload = payload;
		}
	}

	static class ConfigState {
		MetamergeConfig cfg;
		boolean checkedOut;
		boolean encrypted;
		String password;

		public ConfigState(MetamergeConfig cfg, boolean checkedOut, boolean encrypted, String password) {
			this.cfg = cfg;
			this.checkedOut = checkedOut;
			this.encrypted = encrypted;
			this.password = password;
		}
	}

	static class ConfigCreate {
		boolean overwrite;
		CIMock mock;

		public ConfigCreate(boolean overwrite, CIMock mock) {
			this.overwrite = overwrite;
			this.mock = mock;
		}
	}

	private class ListFolders_String implements IAnswer<ArrayList<String>> {
		public ArrayList<String> answer() throws Throwable {
			ArrayList<String> l = new ArrayList<String>();
			String dir = (String) getCurrentArguments()[0];

			dir = walk(dir, null);

			if (!"".equals(dir) && !configDirs.contains(dir)) {
				throw new DIException("Missing config dir");
			}

			int sep = -1;
			for (String d : configDirs) {
				if (!d.equals(dir)) {
					sep = lastSeparator(d);
					if (d.startsWith(dir) && (sep == -1 || sep == dir.length())) {
						l.add(d);
					}
				}
			}
			return l;
		}
	}

	private class ListConfigurations_String implements IAnswer<ArrayList<String>> {
		public ArrayList<String> answer() throws Throwable {
			ArrayList<String> l = new ArrayList<String>();
			String dir = (String) getCurrentArguments()[0];

			dir = walk(dir, null);

			if (!"".equals(dir) && !configDirs.contains(dir)) {
				throw new DIException("Missing config dir");
			}

			int sep = -1;
			for (String d : configs.keySet()) {
				sep = lastSeparator(d);
				if (d.startsWith(dir) && (sep == -1 || sep == dir.length())) {
					l.add(d);
				}
			}
			return l;
		}
	}

	private class IsConfigurationCheckedOut_String implements IAnswer<Boolean> {
		public Boolean answer() throws Throwable {
			String file = (String) getCurrentArguments()[0];

			file = walk(file, null);

			if (!"".equals(file) && !configs.containsKey(file)) {
				throw new DIException("Missing config file");
			}

			return configs.get(file).checkedOut;
		}
	}

	private class CreateNewConfigurationAndLoad_String_Boolean implements IAnswer<ConfigInstance> {
		private CreateNewConfiguration_String_Boolean creator = new CreateNewConfiguration_String_Boolean();

		public ConfigInstance answer() throws Throwable {
			creator.answer();
			String path = (String) getCurrentArguments()[0];

			ConfigCreate create = createConfigs.get(path);
			return create.mock.ciMock;
		}
	}

	private class CreateNewConfiguration_String_Boolean implements IAnswer<MetamergeConfig> {
		public MetamergeConfig answer() throws Throwable {
			String path = (String) getCurrentArguments()[0];
			boolean overwrite = (Boolean) getCurrentArguments()[1];

			path = walk(path, null);

			ConfigCreate cfgCreate = createConfigs.get(path);
			assertThat(cfgCreate, is(not(nullValue())));
			assertEquals("Override is not valid", cfgCreate.overwrite, overwrite);

			ConfigState cfgFile = configs.get(path);

			if (cfgFile != null) {
				if (!overwrite) {
					throw new DIException("Config alredy exists!");
				}

				cfgFile.cfg = newConfig(path);
				cfgFile.checkedOut = true;
			} else {
				int lastSep = lastSeparator(path);
				if (lastSep > -1) {
					walk(path.substring(0, lastSep), configDirsCollector);
				}
				cfgFile = new ConfigState(newConfig(path), true, false, null);
				configs.put(path, cfgFile);
			}

			return cfgFile.cfg;
		}
	}

	@SuppressWarnings("unchecked")
	private static MetamergeConfig newConfig(String path) throws Exception {
		Hashtable env = new Hashtable();
		env.put(MetamergeConfigFactory.MC_DRIVER, "com.ibm.di.config.xml.MetamergeConfigXML");
		env.put(MetamergeConfigFactory.MC_CREATE, false);
		env.put(MetamergeConfigFactory.MC_URL, path);
		return MetamergeConfigFactory.getInstance(env);
	}

	private class CheckInConfiguration_MC_String implements IAnswer<Object> {

		public Object answer() throws Throwable {
			MetamergeConfig mc = (MetamergeConfig) getCurrentArguments()[0];
			String path = (String) getCurrentArguments()[1];

			path = walk(path, null);

			ConfigState cfgState = configs.get(path);
			if (!cfgState.checkedOut) {
				throw new DIException("Config not checked out");
			}

			cfgState.cfg = mc;
			cfgState.checkedOut = false;

			return null;
		}
	}

	private class CheckInConfiguration_MC_String_Boolean implements IAnswer<Object> {

		private CheckInConfiguration_MC_String committer = new CheckInConfiguration_MC_String();

		public Object answer() throws Throwable {
			committer.answer();
			String path = (String) getCurrentArguments()[1];
			boolean encrypt = (Boolean) getCurrentArguments()[2];

			path = walk(path, null);

			ConfigState state = configs.get(path);
			state.encrypted = encrypt;

			return null;
		}
	}

	private class CheckInAndLeaveCheckedOut_MC_String implements IAnswer<Object> {

		private CheckInConfiguration_MC_String committer = new CheckInConfiguration_MC_String();

		public Object answer() throws Throwable {
			committer.answer();
			String path = (String) getCurrentArguments()[1];

			path = walk(path, null);

			ConfigState state = configs.get(path);
			state.checkedOut = true;

			return null;
		}
	}

	private class CheckOutConfiguration_String implements IAnswer<MetamergeConfig> {

		public MetamergeConfig answer() throws Throwable {
			String path = (String) getCurrentArguments()[0];
			path = walk(path, null);

			ConfigState cfgState = configs.get(path);
			if (cfgState.checkedOut) {
				throw new DIException("config already checked out");
			}

			if (cfgState.cfg == null) {
				throw new InternalError("MetamergeConfig not provided");
			}

			cfgState.checkedOut = true;
			return cfgState.cfg;
		}
	}

	private class CheckOutConfiguration_String_String implements IAnswer<MetamergeConfig> {
		private CheckOutConfiguration_String leacher = new CheckOutConfiguration_String();

		public MetamergeConfig answer() throws Throwable {
			leacher.answer();

			String path = (String) getCurrentArguments()[0];
			String password = (String) getCurrentArguments()[1];
			path = walk(path, null);

			ConfigState cfgState = configs.get(path);
			assertEquals("Configuration password", cfgState.password, password);

			return cfgState.cfg;
		}
	}

	private class ReleaseConfigurationLock implements IAnswer<Boolean> {

		public Boolean answer() throws Throwable {
			String path = (String) getCurrentArguments()[0];
			path = walk(path, null);
			ConfigState cfgState = configs.get(path);
			if (!cfgState.checkedOut) {
				throw new DIException("config has not been checked out");
			}

			cfgState.checkedOut = false;
			return true;
		}

	}
}
