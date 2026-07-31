/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.providers;

import java.util.ArrayList;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;

public class MapLabelProvider extends ColumnLabelProvider implements ITableLabelProvider {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String INPUT = "InputImage";
	private static final String OUTPUT = "OutputImage";
	private static final String NVB_IMAGE = "tdi.nvb.image";

	private Image inputImage;
	private Image outputImage;
	private Image nvbImage;
	private Image missingImage;

	public MapLabelProvider(Display display) {
		createImages(display);
	}

	public Image getImage(Object element) {
		return getColumnImage(element, 0);
	}

	public String getText(Object element) {
		return getColumnText(element, 0);
	}

	public Image getColumnImage(Object element, int columnIndex) {
		if ( columnIndex != 0 || ! (element instanceof BaseConfiguration))
			return null;
		BaseConfiguration b = (BaseConfiguration) element;
		if (b instanceof AttributeMapItem) {
			if (!b.getEnabled())
				return Activator.getImage(b);
			
			if(hasNVB(b))
				return nvbImage;
			else if (b.getParameter("%%PLACEHOLDER%%") != null)
				return missingImage;
			else
				return null;
		}
		
		if (b instanceof AttributeMapConfig ) {
			Image image = Activator.getImage(b.getParent());
			if (image != null)
				return image;
		}
		
		// We should never get here.
		return Activator.getImage(b);
	}

	public String getColumnText(Object element, int columnIndex) {

		if (element instanceof AttributeMapConfig) {
			AttributeMapConfig amc = (AttributeMapConfig)element;
			switch(columnIndex) {
			case 0:
				return amc.getParent().getShortName();
			case 1:
				if(Utils.isInputMap(amc))
					return "<----------------"; //$NON-NLS-1$
				else
					return "---------------->"; //$NON-NLS-1$
			case 2:
				if(Utils.isInputMap(amc))
					return Messages.getString("WorkEntryWidget.source"); //$NON-NLS-1$
				else
					return Messages.getString("WorkEntryWidget.target"); //$NON-NLS-1$
			}
		}

		// Attribute map item
		if (element instanceof AttributeMapItem) {
			AttributeMapItem ami = (AttributeMapItem)element;
			AttributeMapConfig map = Utils.getParentConfig(ami, AttributeMapConfig.class);
			switch (columnIndex) {
			case 0:
				return Utils.isInputMap(map) ? ami.getShortName() : getReferences(ami, false);
			case 1:
				if ("*".equals(ami.getShortName()))
					return Messages.getString("MapLabelProvider.star");
				if (ami.isSimple())
					return Utils.getScript(Utils.isInputMap(map) && !isALMap(map) ? "conn":"work", ami.getSimple());
				if (ami.isSubstitution())
					return ami.getSubstitution();
				return ami.getScript();
			case 2:
				if(Utils.isInputMap(map))
					return getReferences(ami, !isALMap(map));

				ConnectorConfig cc = Utils.getParentConfig(ami, ConnectorConfig.class);
				if(cc == null || !ConnectorConfig.UPDATE_MODE.equals(cc.getMode()) || (ami.getAdd() && ami.getModify()))
					return ami.getShortName();
				else if (ami.getAdd())
					return ami.getShortName() + " [" + Messages.getString("AttributeMap.1.label") + "]";
				else
					return ami.getShortName() + " [" + Messages.getString("AttributeMap.2.label") + "]";
			}

		} 

		return null;
	}

	private String getReferences(AttributeMapItem map, boolean input) {
		if(map.isSimple())
			return oneLiner(map.getSimple());
		String script = map.isSubstitution() ? map.getSubstitution() : map.getScript();
		ArrayList<String> list = Utils.getScriptReferences(input, script);
		StringBuilder buf = new StringBuilder();
		for(String str : list) {
			if(buf.length() > 0)
				buf.append(", "); //$NON-NLS-1$
			buf.append(str);
		}
		return buf.toString();
	}

	private void createImages(Display display) {
		inputImage = JFaceResources.getImageRegistry().get(INPUT);
		if ( inputImage == null) {
			inputImage = new Image(display, 16, 16);
			GC gc = new GC(inputImage);
			gc.setBackground(display.getSystemColor(SWT.COLOR_DARK_GREEN));
			gc.fillPolygon(new int[] { 8, 4, 4, 8, 8, 12 });
			gc.dispose();
			JFaceResources.getImageRegistry().put(INPUT, inputImage);
		}
		outputImage = JFaceResources.getImageRegistry().get(OUTPUT);
		if ( outputImage == null) {
			outputImage = new Image(display, 16, 16);
			GC gc = new GC(outputImage);
			gc.setBackground(display.getSystemColor(SWT.COLOR_DARK_RED));
			gc.fillPolygon(new int[] { 6, 4, 6, 12, 10, 8});
			gc.dispose();
			JFaceResources.getImageRegistry().put(OUTPUT, outputImage);
		}
		nvbImage = JFaceResources.getImageRegistry().get(NVB_IMAGE);
		if ( nvbImage == null) {
			nvbImage = new Image(display, 16, 16);
			GC gc = new GC(nvbImage);
			gc.setBackground(display.getSystemColor(SWT.COLOR_BLUE));
			gc.fillRoundRectangle(6, 6, 6, 6, 2, 2);
			gc.dispose();
			JFaceResources.getImageRegistry().put(NVB_IMAGE, nvbImage);
		}
		missingImage = Activator.getImage("Stop");
	}

	private String oneLiner(String s) {
		if (s == null)
			return "";
		return s.replaceAll("\n", ",");
	}

	@Override
	public Color getForeground(Object element) {
		if(Utils.getInheritsFromExt(element) != null || isInherited(element))
			return Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
		else
			return super.getForeground(element);
	}

	private boolean isInherited(Object element) {
		if(element instanceof HookConfig) {
			return Utils.getInheritsFromExt(((HookConfig)element).getParent()) != null;
		} else if (element instanceof AttributeMapItem) {
			AttributeMapItem ami = (AttributeMapItem) element;
			AttributeMapConfig amc = Utils.getParentConfig(element, AttributeMapConfig.class);
			if(amc != null) {
				return !amc.hasParameter(ami.getShortName());
			}
		}
		return false;
	}

	private boolean isALMap(AttributeMapConfig map) {
		return map != null && map.getParent() instanceof ALMappingConfig;
	}
	
	@Override
	public String getToolTipText(Object element) {
		String str = Utils.getInheritsFromExt(element);
		if(str != null) {
			str = Messages.getString("HooksWidget.0") + ": " + str;
		}
		if(hasNVB(element)) {
			if(str != null)
				str += "\n" + Messages.getString("NullBehavior.popup.Title");
			else
				str = Messages.getString("NullBehavior.popup.Title");
		} else if (isInherited(element)) {
			// Inherits via one of its parents
			str = Messages.getString("HooksWidget.0") + ": " + BaseConfiguration.INHERIT_PARENT;
		}
		return str;
	}

	/**
	 * Returns true if the element is a BaseConfiguration object with a NullValue behavior
	 * other than Default.
	 * 
	 * @param element
	 * @return
	 */
	public boolean hasNVB(Object element) {
		if(element instanceof BaseConfiguration) {
			String def = ((BaseConfiguration)element).getNullDefinition();
			String beh = ((BaseConfiguration)element).getNullBehavior();
			if(def != null && !(def.startsWith("Default")))
				return true;
			if(beh != null && !(beh.startsWith("Default")))
				return true;
		}
		return false;
	}

	@Override
	public void update(ViewerCell cell) {
		// Must override update to account for columns
		Object element = cell.getElement();
		cell.setText(getColumnText(element,cell.getColumnIndex()));
		Image image = getColumnImage(element,cell.getColumnIndex());
		cell.setImage(image);
		cell.setBackground(getBackground(element));
		cell.setForeground(getForeground(element));
		cell.setFont(getFont(element));
	}
}
