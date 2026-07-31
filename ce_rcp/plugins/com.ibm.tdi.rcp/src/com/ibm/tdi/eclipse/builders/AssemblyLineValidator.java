/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.builders;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.BranchingConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.LinkCriteriaConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.script.ScriptEngineOptions;
import com.ibm.di.server.ResourceHash;
import com.ibm.jscript.ParserResult;
import com.ibm.jscript.ScriptError;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.marker.MarkerResolutionGenerator;

public class AssemblyLineValidator extends ComponentValidator {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Used for orphaned branches
	 */
	public final static String ORPHANED_BRANCH  = "branch.orphaned";
	
	private List<BaseConfiguration> assemblylineItems;
	private AssemblyLineConfig alc;
	private ArrayList<String> componentNameList;
	private boolean errors;

	public AssemblyLineValidator() {
		super();
	}

	public boolean validate(BaseConfiguration configuration) throws Exception {
		errors = false;

		alc = (AssemblyLineConfig) configuration;

		// Get all AssemblyLine components
		assemblylineItems = alc.getEntryFeedComponents().getConfigurations(null);
		alc.getDataFlowComponents().getConfigurations(assemblylineItems);

		Hashtable<String, String> current = new Hashtable<String, String>();

		componentNameList = new ArrayList<String>();

		boolean hasServerMode = false;

		for (BaseConfiguration b : assemblylineItems) {
			componentNameList.add(b.getShortName());

			if (b instanceof FunctionConfig) {
				FunctionConfig fc = (FunctionConfig) b;
				if (!fc.getEnabled())
					continue;

				verifyConfigAndLogProblem(fc, fc);
			} else if (b instanceof ConnectorConfig) {
				ConnectorConfig cc = (ConnectorConfig) b;
				if (!cc.getEnabled())
					continue;

				if (cc.getMode().equals(ConnectorConfig.SERVER_MODE)) {
					if (hasServerMode)
						addProblem(IMarker.SEVERITY_WARNING, MULTIPLE_SERVERS, cc,
								Messages.getMessage("AssemblyLineValidator.multiple_servers", cc.getShortName())); //$NON-NLS-1$
					else
						hasServerMode = true;
				}
				if (Utils.isInputConnector(cc))
					verifyConnectorConfig(cc, true, current);
				if (Utils.isOutputConnector(cc))
					verifyConnectorConfig(cc, false, current);
				if (Utils.hasLinkRequirements(cc))
					verifyLinkCriteria(cc);
			} else if (b instanceof BranchingConfig) {
				BranchingConfig branch = (BranchingConfig) b;
				if (! branch.getEnabled())
					continue;
				int branchType = branch.getBranchType();
				if ( branchType == BranchingConfig.BRANCH_ELSE || 
						branchType == BranchingConfig.BRANCH_ELSEIF)
					verifyElse(branch);
				else if ( branchType == BranchingConfig.BRANCH_CASE )
					verifyCase(branch);
			}
		}

		//
		// -- Verify property references
		//
		verifyProperties(configuration);

		//
		// -- Verify script syntax
		//
		verifyScripts(configuration);
		
		return errors;
	}

	private void verifyElse(BranchingConfig b) {
		ContainerConfig cc;
		if(b.getParent() instanceof ContainerConfig)
			cc = (ContainerConfig) b.getParent();
		else
			return; // Impossible, or an error?

		for (int i = cc.indexOf(b) - 1; i>=0; i--) {
			BaseConfiguration previous = cc.getConfig(i);
			if(previous instanceof BranchingConfig) {
				BranchingConfig prev = (BranchingConfig)previous;
				if (!prev.getEnabled())
					continue;
				int prevType = prev.getBranchType();
				if (prevType != BranchingConfig.BRANCH_ELSEIF && 
						prevType != BranchingConfig.BRANCH_IF) {
					addElseBranchProblem(b);
				}
				return;
			} else if (previous.getEnabled() ){	
				addElseBranchProblem(b);
				return;				
			}
		}

		addElseBranchProblem(b);
	}

	private void addElseBranchProblem(BranchingConfig b) {
		addProblem(IMarker.SEVERITY_ERROR, ORPHANED_BRANCH, b,
				Messages.getMessage("AssemblyLineValidator.orphaned.branch", b.getShortName())); //$NON-NLS-1$		
	}
	
	private void verifyCase(BranchingConfig b) {
		BranchingConfig parent = null;
		if(b.getParent() instanceof BranchingConfig)
			parent = (BranchingConfig) b.getParent();

		if (parent == null || parent.getBranchType() != BranchingConfig.BRANCH_SWITCH)
			addProblem(IMarker.SEVERITY_ERROR, ORPHANED_BRANCH, b,
					Messages.getMessage("AssemblyLineValidator.orphaned.case", b.getShortName())); //$NON-NLS-1$		
	}

	
	/**
	 * Check all "@property{xxx}" references
	 * 
	 * @param config
	 */
	private void verifyProperties(BaseConfiguration config) {
		if (config == null)
			return;

		for (String key: config.getKeys(BaseConfiguration.ONE_LEVEL)) {
			String pps = config.getParameterPropertySource(key);
			if (pps == null)
				continue;

			if (pps.startsWith("{property") && config.getParameter(key) == null) { //$NON-NLS-1$
				String propref = pps.substring(10, pps.length() - 1);
				IMarker marker = addProblem(IMarker.SEVERITY_WARNING, PROPERTY_NOT_DEFINED, config, 
						Messages.getMessage("AssemblyLineValidator.property_undefined", new Object[]{key, propref})); //$NON-NLS-1$
				try {
					marker.setAttribute(MarkerResolutionGenerator.TDI_MARKER_TYPE_ATTRIBUTE,
							MarkerResolutionGenerator.TDI_MARKER_MISSING_PROPERTY);
					marker.setAttribute(MarkerResolutionGenerator.TDI_MARKER_MISSING_PROPERTY, propref);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
		for (Object b : config.getChildNames()) {
			verifyProperties(config.getChild(b));
		}
	}

	/**
	 * This method check whether specified configuration inherits its data from
	 * a valid and existing configuration. If one of the parent configurations
	 * is not valid an InvalidConfigRefException exception is thrown.
	 * 
	 * @param bc
	 *            configuration to check
	 * @throws Exception
	 */
	private void verifyConfigInheritance(BaseConfiguration bc) throws InvalidConfigRefException {
		String parentConfigRef = Utils.getInheritsFromExt(bc);
		if (parentConfigRef == null)
			return;

		// Still has parents
		
		// Some special code to check for re-used connector
		if (parentConfigRef.startsWith("@")) {	
			if (bc.getInheritsFrom() == null) {
				try {
					bc.setupInheritanceChain();
				} catch (Exception ignore) {
					SystemFunctions.doNothing();
				}
			}
			if (bc.getInheritsFrom() == null)
				throw new InvalidConfigRefException(parentConfigRef);
			return;
		}
		
		MetamergeConfig mc = bc.getMetamergeConfig();
		if (mc != null) {
			BaseConfiguration parent = null;
			try {
				parent = (BaseConfiguration) mc.lookup(parentConfigRef);
			} catch (Exception e) {
				throw new InvalidConfigRefException(parentConfigRef);
			}

			if (parent != null) {
				// Make sure all parents above are also valid and existing
				verifyConfigInheritance(parent);
			}
		}
	}
	
	/**
	 * Verify that all parent configurations exist. If some of the parents does
	 * not exist log an error in the Problems view.
	 * 
	 * @param configToLogProblem
	 *            initial configuration used to log the error
	 * @param configToCheck
	 *            configuration to check its inheritance
	 * @throws Exception
	 */
	private void verifyConfigAndLogProblem(BaseConfiguration configToLogProblem, BaseConfiguration configToCheck) {
		try {
			verifyConfigInheritance(configToCheck);
		} catch (InvalidConfigRefException icre) {
			String msg = Messages.getMessage("TDIConfigurationFile.refmissing", new Object[] { icre.getInvalidConfigRef() }); //$NON-NLS-1$
			Utils.logProblem(IMarker.SEVERITY_ERROR, SCHEMA_NOT_DEFINED, configToLogProblem, msg);
		}
	}

	/**
	 * Exception thrown if a configuration references non-existing parent
	 * configuration.
	 */
	private static class InvalidConfigRefException extends RuntimeException {
		private static final long serialVersionUID = 866948989075167118L;

		/**
		 * Name of the invalid configuration (e.g '/Connectors/myConn')
		 */
		private String invalidConfigRef;

		public InvalidConfigRefException(String config) {
			this.invalidConfigRef = config;
		}

		public String getInvalidConfigRef() {
			return invalidConfigRef;
		}
	}

	public void verifyConnectorConfig(ConnectorConfig cc, boolean input, Hashtable<String, String> current) {
		AttributeMapConfig map = cc.getAttributeMap(input);
		SchemaConfig schema = cc.getSchema(input);

		// Verify that all parent connector and parser configurations exist
		verifyConfigAndLogProblem(cc, cc);

		if (Utils.hasParserRequirements(cc)) {
			verifyConfigAndLogProblem(cc, cc.getParserConfig());
		}
		
		AssemblyLineConfig alc = Utils.getParentConfig(cc, AssemblyLineConfig.class);
		boolean autoMap = false;
		if(alc != null)
			autoMap = alc.getSettings().getBooleanParameter(InternalSchema.AL_AUTOMAP_ATTRIBUTES, false);

		if(!autoMap) {
			// -- Empty input map?
			String msg = Messages.getMessage("AssemblyLineValidator.empty.map", new Object[] {cc.getShortName()});
			if(Utils.isInputConnector(cc) && cc.getAttributeMap(true).getAttributeNames().size() == 0) {
				Utils.logProblem(IMarker.SEVERITY_WARNING, SCHEMA_NOT_DEFINED, cc.getAttributeMap(true), msg);
			}

			// -- Empty output map?
			if(Utils.isOutputConnector(cc) && cc.getAttributeMap(false).getAttributeNames().size() == 0) {
				Utils.logProblem(IMarker.SEVERITY_WARNING, SCHEMA_NOT_DEFINED, cc.getAttributeMap(false), msg);
			}
		}
		
		for (String name : map.getAttributeNames()) {
			AttributeMapItem ami = map.getAttributeMapItem(name);

			if ((!input) && (!name.equals("*")) && (schema.getItem(name) == null)) { //$NON-NLS-1$
				String msg = Messages.getMessage("AssemblyLineValidator.schema_undefined", cc.getShortName(), name); //$NON-NLS-1$
				Utils.logProblem(IMarker.SEVERITY_WARNING, SCHEMA_NOT_DEFINED, ami, msg);
			}

			if (! ami.isSimple()) {
				String script = ami.isSubstitution() ? ami.getSubstitution() : ami.getScript();
				for (String str : Utils.getScriptReferences(input, script)) {
					if (input && schema.getItem(str) == null && !str.startsWith("getAttribute(")) { //$NON-NLS-1$
						Utils.logProblem(IMarker.SEVERITY_WARNING, SCHEMA_NOT_DEFINED, ami, Messages.getMessage("AssemblyLineValidator.input_schema_undefined",
								new Object[] { cc.getShortName(), "conn." + str }));
					} else if (!input && !current.containsKey(str)) {
						String msg = Messages.getMessage("AssemblyLineValidator.work_undefined", name, str ); //$NON-NLS-1$
						Utils.logProblem(IMarker.SEVERITY_WARNING, WORK_NOT_DEFINED, ami, msg);
					} else {
						continue;
					}

					errors = true;
				}
			}

			if (input) {
				current.put(name, cc.getShortName());
			}
		}

	}

	/**
	 * Check syntax on all scripts
	 * 
	 * @param config Configuration to search and validate
	 */
	private void verifyScripts(BaseConfiguration config) {
		if (config == null)
			return;
		/*L3 code, if condition added for defect 13363*/
		if (config instanceof AttributeMapItem && 
				!AttributeMapItem.ADVANCED_MAPPING.equals(((AttributeMapItem)config).getType()))
			return;
		String script = config.getScript();
		if(script != null && script.length() > 0) {
			ParserResult result = ScriptEngineOptions.get().parseScript(script, true);
			for(int i = 0; i < result.getErrorCount(); i++) {
				ScriptError jserr = result.getError(i);
				IMarker marker = addProblem(IMarker.SEVERITY_ERROR, SCRIPT_SYNTAX_ERROR, config, jserr.getMessage());
				try {
					marker.setAttribute(IMarker.LINE_NUMBER, jserr.getErrorLine());
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}

		for (Object b : config.getChildNames()) {
			verifyScripts(config.getChild(b));
		}
		
		if(config instanceof AssemblyLineConfig) {
			for(Object obj : ((AssemblyLineConfig)config).getHooks().getActiveHooks()) {
				verifyScripts((BaseConfiguration) obj);
			}
		}
	}
	
	private void verifyLinkCriteria(ConnectorConfig cc) {
		LinkCriteriaConfig lcc = cc.getLinkCriteria();
		if (lcc.getAdvancedLinkMode()) {
			String s = lcc.getAdvancedLinkCriteria();
			if (s != null && s.length() > 0)
				return;
		} else {
			List<String> list = lcc.getCriteriaNames();
			if (list.size() > 0)
				return;
		}
		// Nothing configured, create a message
		String msg = ResourceHash.getHash("miserver").getString("no.link.criteria");
		addProblem(IMarker.SEVERITY_WARNING, PROPERTY_NOT_DEFINED, cc, msg);
	}
}
