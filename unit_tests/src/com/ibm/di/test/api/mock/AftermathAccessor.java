package com.ibm.di.test.api.mock;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.model.descriptor.ConnectorDescriptor;
import com.ibm.di.model.descriptor.FunctionComponentDescriptor;
import com.ibm.di.model.descriptor.ParserDescriptor;
import com.ibm.di.test.api.mock.ServerAPIMockBuilder.ConfigState;

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
public class AftermathAccessor {

	private Map<String, ConnectorDescriptor> conns;
	private Map<String, FunctionComponentDescriptor> fcs;
	private Map<String, ParserDescriptor> pss;
	private Set<String> configDirs;
	private Map<String, ConfigState> configs;
	private List<CIMock> cis;

	void setServerInfoComponents(Map<String, ConnectorDescriptor> conns, Map<String, FunctionComponentDescriptor> fcs,
			Map<String, ParserDescriptor> pss) {
		this.conns = conns;
		this.fcs = fcs;
		this.pss = pss;
	}

	void setConfigurationFiles(Set<String> configDirs, Map<String, ConfigState> configs) {
		this.configDirs = configDirs;
		this.configs = configs;
	}

	void setCIs(List<CIMock> cis) {
		this.cis = cis;
	}

	public boolean isConfigFileChekedOut(String relPath) {
		return getState(relPath).checkedOut;
	}

	private ConfigState getState(String relPath) {
		String path = ServerAPIMockBuilder.walk(relPath, null);

		ConfigState state = configs.get(path);

		if (state == null) {
			throw new IllegalStateException("Missing configuration file: " + relPath);
		}
		return state;
	}

	public MetamergeConfig getConfigurationFile(String relPath) {
		return getState(relPath).cfg;
	}

}
