/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.naming.CompoundName;
import javax.naming.NameAlreadyBoundException;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.OperationConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.editors.SchemaEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.providers.WorkEntryAttributesProvider;

public class AddAttributeMapItemAction extends BaseAction implements IInputValidator {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String MAP_STAR = Messages.getString("AddAttributeMapItemAction.star");

	private AttributeMapConfig amc;

	public AddAttributeMapItemAction() {
	}

	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		if (selection.isEmpty())
			return;
		if (getFirstSelection() instanceof SchemaItemConfig) {
			SchemaItemConfig sic = (SchemaItemConfig) getFirstSelection();
			SchemaConfig sc = Utils.getParentConfig(sic, SchemaConfig.class);
			if (sc == null ||
					"AssemblyLineInitParams".equals(sc.getShortName()) ||
					(sc.getParent() instanceof ConnectorConfig &&
							"Schema".equals(((ConnectorConfig)sc.getParent()).getMode()))) {
				action.setEnabled(false);
			}
		}
	}

	public void run(IAction action) {
		Object sel = getFirstSelection();
		if(sel instanceof SchemaItemConfig) {
			mapSchemaItem();
		} else if (sel instanceof AttributeMapConfig) {
			amc = (AttributeMapConfig)sel;
			addToMap(true);
		} else if (sel instanceof AttributeMapItem) {
			amc = Utils.getParentConfig(((AttributeMapItem)sel), AttributeMapConfig.class);
			if (amc == null)
				amc = (AttributeMapConfig)((AttributeMapItem)sel).getParameter("%%PLACEHOLDER%%");
			if (amc != null)
				addToMap(true);
		}
	}

	private void mapSchemaItem() {
		SchemaItemConfig sic = (SchemaItemConfig) getFirstSelection();
		SchemaConfig sc = Utils.getParentConfig(sic, SchemaConfig.class);
		if(sc == null)
			return;

		boolean input = false;
		if(ConnectorConfig.SCHEMA_INPUT.equals(sc.getShortName()))
			input = true;

		AttributeMapConfig amc = getMap(sc, input);
		if (amc == null)
			return;

		StringBuilder alreadyMapped = new StringBuilder();
		for ( Object o: getSelectionItems() ) {
			if ( o instanceof SchemaItemConfig )
				sic = (SchemaItemConfig) o;
			else
				continue;

			String str = sic.getAttributeName();
			if (sic.getName() instanceof CompoundName)
				str = createUnquotedString((CompoundName)sic.getName());
			if (str == null)
				str = sic.getShortName();

			if(amc.hasAttributeMapItem(str)) {
				if (alreadyMapped.length() > 0)
					alreadyMapped.append("', '");
				alreadyMapped.append(str);
				continue;
			}

			AttributeMapItem map;
			try {
				map = amc.newAttributeMapItem(str);
				map.setSimple(str);
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, getShell());
			}
		}

		if (alreadyMapped.length() > 0)
			MessageDialog.openInformation(getShell(),
					Messages.getString("action.label.3"),
					Messages.getMessage("attributemap.attribute.already.mapped.err", alreadyMapped.toString()));
	}

	private String createUnquotedString(CompoundName name) {
		StringBuilder ret = new StringBuilder();
		boolean first = true;
		Enumeration<String> elements = name.getAll();
		while (elements.hasMoreElements()) {
			if (first)
				first = false;
			else
				ret.append('.');
			ret.append(elements.nextElement());
		}
		return first ? null : ret.toString();
	}

	private AttributeMapConfig getMap(SchemaConfig sc, boolean input) {
		ConnectorConfig cc = Utils.getParentConfig(sc, ConnectorConfig.class);
		if (cc != null)
			return cc.getAttributeMap(input);
		OperationConfig oc = Utils.getParentConfig(sc, OperationConfig.class);
		if (oc != null)
			return oc.getAttributeMap(input);
		return null;
	}

	private void addToMap(boolean input) {
		if ("Input".equals(amc.getShortName()))
			input = true;
		if ("Output".equals(amc.getShortName()))
			input = false;

		List<String> itemNames = new ArrayList<String>();
		ConnectorConfig cc = Utils.getParentConfig(amc, ConnectorConfig.class);
		OperationConfig oc = null;

		if ( cc != null ) {
			SchemaConfig sc = cc.getSchema(input);
			for (Object obj : sc.getItemNames()) {
				SchemaItemConfig sci = sc.getItem(obj);
				if(!SchemaEditor.SCHEMA_DESIGN_NAME.equals(obj))
					addSchemaItem(itemNames, sci);
			}

			SchemaItemConfig sci = sc.getItem(SchemaEditor.SCHEMA_DESIGN_NAME);
			if(sci != null) {
				SchemaConfig sc2 = SchemaEditor.getDesignSchema(Utils.getProjectFor(sc), sci.getExternalSyntax());
				if(sc2 != null) {
					for (String str : sc2.getItemNames()) {
						if(!itemNames.contains(str))
							itemNames.add(str);
					}
				}
			}
		} else {
			oc = Utils.getParentConfig(amc, OperationConfig.class);
			if (oc != null)
				itemNames = oc.getSchema(input).getItemNames();
		}

		if ( ! input || cc instanceof ALMappingConfig ) {
			AssemblyLineConfig alc = Utils.getParentConfig(amc, AssemblyLineConfig.class);
			if(alc != null) {
				String connName = cc != null ? cc.getShortName() : oc != null ? oc.getShortName() : "";
				WorkEntryAttributesProvider wap = new WorkEntryAttributesProvider(connName);
				wap.inputChanged(null, null, alc);
				for(Object obj : wap.getChildren(alc)) {
					String name = obj.toString();
					if (!itemNames.contains(name))
						itemNames.add(name);
				}
			}
		}

		// -- don't show those already mapped
		for(String str : amc.getAttributeNames()) {
			itemNames.remove(str);
		}

		if (amc.getParent() instanceof ALMappingConfig)
			input = false; // We want to map from work, not conn

		List<String> selectionNames = new ArrayList<String>();
		if (!amc.hasAttributeMapItem("*"))
			selectionNames.add(MAP_STAR);
		Collections.sort(itemNames);
		selectionNames.addAll(itemNames);

		ListSelectionDialog ld = new ListSelectionDialog(getShell(),
				selectionNames,
				new ArrayContentProvider(),
				new LabelProvider(),
				Messages.getString("FormUI.Localized.Select.Attribute")) {

			private Text text;
			private String value = null;

			@Override
			protected Control createDialogArea(Composite parent) {
				Composite c = (Composite) super.createDialogArea(parent);
				Composite c2 = new Composite(c, SWT.NONE);
				c2.setLayout(new GridLayout(2,false));
				new Label(c2, SWT.LEFT).setText(Messages.getString("RenameWorkAttributeItem.1"));
				text = new Text(c2, SWT.BORDER);
				text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				c2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				text.setFocus();
				return c;
			}

			@Override
			protected void okPressed() {
				value = text.getText().trim();
				super.okPressed();
			}

			@Override
			public Object[] getResult() {
				Object[] result = super.getResult();
				if(value.length() > 0) {
					Object[] nresult = new Object[result.length+1];
					for(int i = 0; i < result.length; i++)
						nresult[i] = result[i];
					nresult[result.length] = value;
					return nresult;
				}
				return result;
			}
		};
		ld.setTitle(getTitle());
		if(ld.open() == Window.OK) {
			for(Object obj : ld.getResult())
				addItem(input, obj.toString());
		}
	}

	private void addSchemaItem(List<String> list, SchemaItemConfig sic) {
		list.add(Utils.getScriptName(sic));
		for (BaseConfiguration child : sic.getChildSchemaList().getConfigurations(null)) {
			if (child instanceof SchemaItemConfig)
				addSchemaItem(list, (SchemaItemConfig) child);
		}
	}

	private void addItem(boolean input, String newName) {
		if(newName != null && newName.trim().length() > 0) {
			newName = newName.trim();
			if(newName.equals(MAP_STAR))
				newName = "*";
			try {
				AttributeMapItem ami = amc.newAttributeMapItem(newName);
				// -- for standalone attmaps in the AL we create a work reference
				// -- unless it is a map_star assignment.
				if(amc.getParent() instanceof ALMappingConfig && !newName.equals("*"))
					ami.setScript(Utils.getScript("work", newName));
				else
					ami.setSimple(newName);
			} catch (NameAlreadyBoundException nab) {
				// -- already bound is ok
				SystemFunctions.doNothing();
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, getShell());
			}
		}
	}

	public String isValid(String newText) {
		if(amc.hasAttributeMapItem(newText))
			return Messages.getMessage("attributemap.attribute.already.mapped.err", newText);
		else
			return null;
	}

	public String getTitle() {
		if (getAction() != null) {
			String t = super.getTitle();
			if ( t != null)
				return t;
		}
		return Messages.getString("AddAttributeMapItemAction.title");
	}
}
