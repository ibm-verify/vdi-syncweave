/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse;

import java.util.ArrayList;

import org.eclipse.core.runtime.QualifiedName;

import com.ibm.di.config.base.ALMappingConfigImpl;
import com.ibm.di.config.base.BranchingConfigImpl;
import com.ibm.di.config.base.LoopConfigImpl;
import com.ibm.di.config.base.ScriptConfigImpl;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.BranchingConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.tdi.eclipse.log.EclipseAppender;

/**
 * Class to hold various constants used in the TDI eclipse plugin.
 *
 */
public class TDI {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final String DEFAULT_CONTROLFLOW_FOLDER = "ControlFlow";

	public static final String PROJECT_PREF_SERVER = "TDI_PREF_SERVER";
	
	public static final String RUN_OPTIONS = "TDI_RUNOPTIONS";
	
	public static final String PROJECT_TEMP_INSTANCE = "TDI_TEMPINSTANCE";
	
	public static final QualifiedName PROJECT_PREF_SERVER_QNAME = new QualifiedName("http://www.ibm.com", PROJECT_PREF_SERVER);
	
	public static final QualifiedName RUN_OPTIONS_QNAME = new QualifiedName("http://www.ibm.com", RUN_OPTIONS);
	
	public static final QualifiedName PROJECT_LAST_TEMP_INSTANCE = new QualifiedName("http://www.ibm.com", PROJECT_TEMP_INSTANCE);
	/*
	 * Margins - Used with the FormLayout
	 */
	public static final int MARGIN_TOP = 0;
	public static final int MARGIN_BOTTOM = 0;
	public static final int MARGIN_LEFT = 0;
	public static final int MARGIN_RIGHT = 0;
	
	// -- The space between the tabs and contents
	public static final int MARGIN_TAB_ITEM = 0;
	
	// -- Spacing between vertically adjacent components (that belong together as a group)
	public static final int SPACING_VERTICAL = 3;
	
	// -- Spacing between horizontally adjacent components (that belong together as a group)
	public static final int SPACING_HORIZONTAL = 3;
	
	// -- Spacing between horizontally adjacent components (different groups)
	public static final int SPACING_GROUP_HORIZONTAL = 6;
	
	// -- Spacing between vertically adjacent components (different groups)
	public static final int SPACING_GROUP_VERTICAL = 9;
	
	// -- Insert before
	public static final int INSERT_BEFORE = 0;

	// -- Insert after
	public static final int INSERT_AFTER = 1;

	// -- Insert into
	public static final int INSERT_INTO = 2;

	// -- Insertion point for TDI actions etc
	public static final String GROUP_TDI = "group.tdi";

	//
	// Identifiers from the object contributions
	//
	public static final String ID_PARSER_CONFIG = "com.ibm.tdi.rcp.actions.parserconfig";
	public static final String ID_LINKCRITIERIA_CONFIG = "com.ibm.tdi.rcp.actions.linkcriteria";
	public static final String ID_CONNECTION_CONFIG = "com.ibm.tdi.rcp.actions.connectionconfig";
	public static final String ID_DISCOVER_CONFIG = "com.ibm.tdi.rcp.actions.schema";

	private static final String SCRIPT_SAMPLE_LABEL = "TDI.ScriptLabel.";
	
	private static ArrayList<BaseConfiguration> templates = new ArrayList<BaseConfiguration>(); 
	
	public static boolean isTemplate(BaseConfiguration config) {
		return templates.contains(config);
	}
	
	public static void addSystemNameSpaceExtras() {
		
		String[] samples = new String[] { "", 
				"// Dump the work entry\ntask.dumpEntry(work);\n",
				"// Exit current branch\nsystem.exitBranch();\n", 
				"// Go to end of cycle\nsystem.exitFlow();\n"};
		
		MetamergeConfig ns = MetamergeConfigFactory.getNamespace(MetamergeConfigFactory.SYSTEM_NAMESPACE);
		
		for (int i = 0; i < samples.length; i++) {
			try {
				ScriptConfig sc = new ScriptConfigImpl();
				sc.init();
				sc.setEnabled(true);
				sc.setName(Messages.getString(SCRIPT_SAMPLE_LABEL + i));
				sc.setScript(samples[i]);
				templates.add(sc);
				ns.bind(MetamergeConfig.DEFAULT_SCRIPT_FOLDER + "/" + sc.getShortName(), sc);
			} catch (Exception e) {
				EclipseAppender.logerror(e.getMessage(), e);
			}
		}
		try {
			ALMappingConfigImpl sc = new ALMappingConfigImpl();
			sc.init();
			sc.setEnabled(true);
			sc.setName(Messages.getString("Localized.AttributeMap"));
			ns.bind(MetamergeConfig.DEFAULT_ATTRIBUTEMAP_FOLDER + "/" + sc.getShortName(), sc);
			sc.setMetamergeConfig(ns);
			templates.add(sc);
		} catch (Exception e) {
		}
		try {
			ns.createFolder(DEFAULT_CONTROLFLOW_FOLDER);
			
			BranchingConfig bc = new BranchingConfigImpl();
			bc.init();
			bc.setBranchType(BranchingConfig.BRANCH_IF);
			bc.setName(Messages.getString("Localized.IF"));
			ns.bind(DEFAULT_CONTROLFLOW_FOLDER + "/" + bc.getShortName(), bc);
			templates.add(bc);

			bc = new BranchingConfigImpl();
			bc.init();
			bc.setBranchType(BranchingConfig.BRANCH_ELSEIF);
			bc.setName(Messages.getString("Localized.ELSEIF"));
			ns.bind(DEFAULT_CONTROLFLOW_FOLDER + "/" + bc.getShortName(), bc);
			templates.add(bc);

			bc = new BranchingConfigImpl();
			bc.init();
			bc.setBranchType(BranchingConfig.BRANCH_ELSE);
			bc.setName(Messages.getString("Localized.ELSE"));
			ns.bind(DEFAULT_CONTROLFLOW_FOLDER + "/" + bc.getShortName(), bc);
			templates.add(bc);

			bc = new BranchingConfigImpl();
			bc.init();
			bc.setBranchType(BranchingConfig.BRANCH_SWITCH);
			bc.setName(Messages.getString("Localized.Switch"));
			ns.bind(DEFAULT_CONTROLFLOW_FOLDER + "/" + bc.getShortName(), bc);
			templates.add(bc);

			bc = new BranchingConfigImpl();
			bc.init();
			bc.setBranchType(BranchingConfig.BRANCH_CASE);
			bc.setName(Messages.getString("Localized.Case"));
			ns.bind(DEFAULT_CONTROLFLOW_FOLDER + "/" + bc.getShortName(), bc);
			templates.add(bc);

			LoopConfigImpl lc = new LoopConfigImpl();
			lc.init();
			lc.setLoopType(LoopConfig.LOOP_COLLECTION);
			lc.setName(Messages.getString("Localized.AttributeValueLoop"));
			ns.bind(DEFAULT_CONTROLFLOW_FOLDER + "/" + lc.getShortName(), lc);
			templates.add(bc);

			lc = new LoopConfigImpl();
			lc.init();
			lc.setLoopType(LoopConfig.LOOP_CONDITIONS);
			lc.setName(Messages.getString("Localized.ConditionalLoop"));
			ns.bind(DEFAULT_CONTROLFLOW_FOLDER + "/" + lc.getShortName(), lc);
			templates.add(bc);
			
			lc = new LoopConfigImpl();
			lc.init();
			lc.setLoopType(LoopConfig.LOOP_CONNECTOR_FC);
			lc.setName(Messages.getString("Localized.ConnectorLoop"));
			lc.getLoopConnector().setInheritsFromRef("system:/Connectors/ibmdi.FileSystem");
			lc.getLoopConnector().setMode(ConnectorConfig.ITERATOR_MODE);
			ns.bind(DEFAULT_CONTROLFLOW_FOLDER + "/" + lc.getShortName(), lc);
			templates.add(bc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
