/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.providers;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.BranchCondition;
import com.ibm.di.config.interfaces.BranchingConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FormConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.HooksConfig;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.config.xml.BranchingFactory;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.util.HookTree;
import com.ibm.di.util.HookTree.Phase;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;

/**
 * Standard image and label provider for TDI configuration objects
 */
public class ConfigLabelProvider extends ColumnLabelProvider {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	boolean consultingInfFiles;
	boolean simpleConnectorIcon = false;
	boolean provideIcons = true;
	boolean simpleInlineIcons = false;
	boolean includeHooks = false;
	private Image inputAttributMapItem;
	private Image outputAttributMapItem;
	private Image greenBullet;

	public ConfigLabelProvider() {
		super();

		Display display = Display.getDefault();
		inputAttributMapItem = new Image(display, 16, 16);
		GC gc = new GC(inputAttributMapItem);
		gc.setBackground(display.getSystemColor(SWT.COLOR_DARK_GREEN));
		gc.fillPolygon(new int[] { 8, 4, 4, 8, 8, 12, 8, 4 });
		gc.dispose();

		outputAttributMapItem = new Image(display, 16, 16);
		gc = new GC(outputAttributMapItem);
		gc.setBackground(display.getSystemColor(SWT.COLOR_DARK_RED));
		gc.fillPolygon(new int[] { 6, 4, 6, 12, 10, 8, 6, 4 });
		gc.dispose();

		greenBullet = new Image(display, 16, 16);
		gc = new GC(greenBullet);
		gc.setBackground(display.getSystemColor(SWT.COLOR_GRAY));
		gc.fillRectangle(6, 6, 10, 8);
		gc.dispose();
	}

	public boolean isProvideIcons() {
		return provideIcons;
	}

	public void setProvideIcons(boolean provideIcons) {
		this.provideIcons = provideIcons;
	}

	public boolean isSimpleConnectorIcon() {
		return simpleConnectorIcon;
	}

	public void setSimpleConnectorIcon(boolean simpleConnectorIcon) {
		this.simpleConnectorIcon = simpleConnectorIcon;
	}

	@Override
	public Image getImage(Object element) {
		Image image = getTDIImage(element);
		IDecoratorManager dm = (IDecoratorManager) PlatformUI.getWorkbench().getDecoratorManager();
		if (dm != null && dm.getLabelDecorator() != null) {
			Image img = dm.getLabelDecorator().decorateImage(image, element);
			if (img != null)
				return img;
		}
		return image;
	}

	public Image getTDIImage(Object element) {
		if (!isProvideIcons())
			return null;

		// return small bullet for sub configurations
		if (isSimpleInlineIcons()) {
			if (element instanceof HooksConfig)
				return greenBullet;

			if (element instanceof AttributeMapItem) {
				if (!((AttributeMapItem) element).getEnabled())
					return null;
				AttributeMapConfig amc = Utils.getParentConfig(element, AttributeMapConfig.class);
				if (Utils.isInputMap(amc))
					return inputAttributMapItem;
				else
					return outputAttributMapItem;
			}
		}

		if (element instanceof HookTree) {
			element = ((HookTree) element).getConfig();
			if (element == null)
				return Activator.getImage("Script");
		}

		if (element instanceof BaseConfiguration) {
			Image img = Activator.getImage((BaseConfiguration) element);
			if (img != null)
				return img;
		}

		if (element instanceof Phase)
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);

		if (element instanceof FunctionConfig || element instanceof ALMappingConfig)
			return null;
		if (element instanceof ConnectorConfig && isSimpleConnectorIcon())
			return Activator.getImage("Connector_16");

		return null;
	}

	@Override
	public String getText(Object element) {
		String str = null;

		if (element instanceof ConnectorConfig && isConsultingInfFiles())
			str = getInfTitle((BaseConfiguration) element);
		else if (element instanceof RawConnectorConfig && isConsultingInfFiles())
			str = getInfTitle((BaseConfiguration) element);
		else if (element instanceof ParserConfig && isConsultingInfFiles())
			str = getInfTitle((BaseConfiguration) element);
		else if (element instanceof HookConfig)
			str = HookTree.getHookLabel((HookConfig) element);
		else if (element instanceof MetamergeConfig)
			str = "" + MetamergeConfigFactory.getNamespaceFor((BaseConfiguration) element);
		else if (element instanceof BaseConfiguration)
			str = ((BaseConfiguration) element).getShortName();
		else if (element instanceof String)
			str = Messages.getString((String) element);
		else if (element instanceof Phase)
			str = Messages.getString("Phase." + element.toString());
		else
			str = super.getText(element);

		if ("ibmdi.LogConnector".equals(str))
			str = Messages.getString("ConfigLabelProvider.1");

		if (str == null && element instanceof BaseConfiguration)
			str = ((BaseConfiguration) element).getShortName();

		if ("DataFlowContainer".equals(str))
			str = Messages.getString("ConfigLabelProvider.2");
		else if ("EntryFeedContainer".equals(str))
			str = Messages.getString("ConfigLabelProvider.3");
		else if (str == null && element instanceof HooksConfig)
			str = Messages.getString("ConfigLabelProvider.4");
		else if (str == null && element instanceof MetamergeConfig)
			str = element.toString();
		else if (element == null)
			str = Messages.getString("ConfigLabelProvider.5");
		else if (str == null)
			str = Messages.getMessage("ConfigLabelProvider.6", element.getClass().getName());

		if (str != null && str.startsWith("SyncWeave"))
			str = str.substring(9);

		if (element instanceof LoopConfig) {
			LoopConfig lc = (LoopConfig) element;
			str = Messages.getMessage("ConfigLabelProvider.Loop." + lc.getLoopType(), str);

		} else if (element instanceof BranchingConfig) {
			BranchingConfig bc = (BranchingConfig) element;
			BranchCondition cond = (BranchCondition) bc.getConditions().getConfig(0);
			switch (bc.getBranchType()) {
			case BranchingConfig.BRANCH_SWITCH:
				if (cond != null) {
					String val = cond.getRightHand();
					if (val == null)
						val = ""; // Or use another template?
					str = Messages.getMessage("ConfigLabelProvider.Branch.3", val);
				}
				break;
			case BranchingConfig.BRANCH_CASE:
				String val = null;
				if (cond != null)
					val = cond.getRightHand();
				if (val == null)
					str = Messages.getString("ConfigLabelProvider.7");
				else
					str = Messages.getMessage("ConfigLabelProvider.Branch.4", val);
				break;
			case BranchingConfig.BRANCH_IF:
				String script = (bc.getBooleanParameter(BranchingFactory.SCRIPT_DELETED, false) ? null : bc.getScript());
				if (str == null || str.length() == 0 || cond != null ||
						(script != null && script.length() > 0 && ! script.equals("return true")))
					str = Messages.getMessage("ConfigLabelProvider.Branch.0", str);
				break;
			case BranchingConfig.BRANCH_ELSEIF:
				str = Messages.getMessage("ConfigLabelProvider.Branch.1", str);
				break;
			case BranchingConfig.BRANCH_ELSE:
				str = Messages.getMessage("ConfigLabelProvider.Branch.2", str);
				break;
			}

		} else if (element instanceof ConnectorConfig) {
			ConnectorConfig cc = (ConnectorConfig) element;

			if (isIncludeHooks()) {
				StringBuffer hooks = new StringBuffer();
				for (Object obj : cc.getHooks().getActiveHooks()) {
					if (hooks.length() > 0)
						hooks.append(", ");
					hooks.append(HooksContentProvider.hookLabel((HookConfig) obj));
				}
				if (hooks.length() > 0)
					str += " [" + hooks.toString() + "]";
			}

		} else if (element instanceof HooksConfig) {
			ConnectorConfig cc = Utils.getParentConfig(element, ConnectorConfig.class);
			str = Messages.getMessage("ConfigLabelProvider.8", cc.getShortName());
		} else if (element instanceof AttributeMapConfig) {
			ConnectorConfig cc = Utils.getParentConfig(element, ConnectorConfig.class);
			if ("Input".equals(str))
				str = Messages.getMessage("ConfigLabelProvider.9", cc.getShortName());
			else if ("Output".equals(str))
				str = Messages.getMessage("ConfigLabelProvider.10", cc.getShortName());
			else
				str = Messages.getMessage("ConfigLabelProvider.11", cc.getShortName());
		}

		return str;
	}

	private String getInfTitle(BaseConfiguration element) {
		String javaclass = null;
		if (element instanceof FunctionConfig)
			javaclass = ((FunctionConfig) element).getJavaClass();
		else if (element instanceof ConnectorConfig)
			javaclass = ((ConnectorConfig) element).getConnectionConfig().getJavaClass();
		else if (element instanceof ParserConfig)
			javaclass = ((ParserConfig) element).getJavaClass();
		else if (element instanceof RawConnectorConfig)
			javaclass = ((RawConnectorConfig) element).getJavaClass();

		if (javaclass == null)
			return element.getShortName();

		if (javaclass.startsWith("@"))
			return Messages.getMessage("ConfigLabelPriver.reusing", javaclass.substring(1));

		try {
			MetamergeConfig system = MetamergeConfigFactory.getNamespace(MetamergeConfigFactory.SYSTEM_NAMESPACE);
			FormConfig inf = null;
			for (BaseConfiguration bc = element; bc != null && inf == null; bc = bc.getInheritsFrom()) {
				try {
					inf = (FormConfig) system.lookup("Forms/" + javaclass + "." + bc.getShortName());
				} catch (Exception e) {
					// Continue searching.
					SystemFunctions.doNothing();
				}
			}
			if (inf == null) {
				try {
					inf = (FormConfig) system.lookup("Forms/" + javaclass);
				} catch (Exception e) {
					// Continue searching.
					SystemFunctions.doNothing();
				}
			}

			if (inf == null) {
				// 7.2 With OSGi IntegrationComponents introduction we define
				// default config's short name matches its corresponding form
				// id. This way a more direct association can be performed
				// without the need to parse the default config to get the
				// component class.
				BaseConfiguration it = element;
				while (it != null) {
					try {
						inf = (FormConfig) system.lookup("/Forms/" + it.getShortName());
						break;
					} catch (Exception notFound) {
						it = it.getInheritsFrom();
					}
				}
			}

			String s = null;
			if (inf != null)
				s = inf.getTitle();
			if (s != null && s.length() > 0)
				return s;
			return element.getShortName();
		} catch (Exception e) {
			// EclipseAppender.logerror(e.toString(), e);
			return element.getShortName();
		}

	}

	public boolean isConsultingInfFiles() {
		return consultingInfFiles;
	}

	public void setConsultingInfFiles(boolean consultingInfFiles) {
		this.consultingInfFiles = consultingInfFiles;
	}

	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex == 0)
			return getImage(element);
		else
			return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (columnIndex == 0)
			return getText(element);
		else
			return className(element);
	}

	private String className(Object element) {
		if (element instanceof FunctionConfig)
			return Messages.getString("ConfigLabelProvider.12");
		else if (element instanceof ParserConfig)
			return Messages.getString("ConfigLabelProvider.13");
		else if (element instanceof AttributeMapConfig)
			return Messages.getString("ConfigLabelProvider.14");
		else if (element instanceof ConnectorConfig)
			return Messages.getString("ConfigLabelProvider.15");
		return null;
	}

	@Override
	public void dispose() {
		if (inputAttributMapItem != null)
			inputAttributMapItem.dispose();
		if (outputAttributMapItem != null)
			outputAttributMapItem.dispose();
		super.dispose();
	}

	public boolean isSimpleInlineIcons() {
		return simpleInlineIcons;
	}

	public void setSimpleInlineIcons(boolean simpleInlineIcons) {
		this.simpleInlineIcons = simpleInlineIcons;
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		if ("cell".equals(property))
			return true;
		else
			return super.isLabelProperty(element, property);
	}

	public boolean isIncludeHooks() {
		return includeHooks;
	}

	public void setIncludeHooks(boolean includeHooks) {
		this.includeHooks = includeHooks;
	}

	@Override
	public String getToolTipText(Object element) {
		String inherit = Utils.getInheritsFromExt(element);
		String tooltip = null;
		if (inherit != null)
			tooltip = Messages.getString("HooksWidget.0") + ": " + inherit;
		else
			tooltip = super.getToolTipText(element);

		if (element instanceof BaseConfiguration) {
			String comment = ((BaseConfiguration) element).getUserComment();
			if (comment != null && comment.length() > 0) {
				if (tooltip == null)
					tooltip = comment;
				else
					tooltip += "***\n" + comment;
			}
		}

		return tooltip;
	}

	@Override
	public void update(ViewerCell cell) {
		cell.setText(getColumnText(cell.getElement(), cell.getColumnIndex()));
		cell.setImage(getImage(cell.getElement()));
		cell.setForeground(getForeground(cell.getElement()));
	}

	@Override
	public Color getForeground(Object element) {
		if (Utils.getInheritsFromExt(element) != null)
			return Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
		else
			return super.getForeground(element);
	}
}
