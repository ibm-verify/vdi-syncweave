/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.util;

import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.forms.widgets.Form;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.ConfigUtils;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;

public class TDIToolBar extends Composite implements MetamergeConfigChangeListener {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Label titleLabel;
	private Label imageLabel;
	private ArrayList<IAction> actions = new ArrayList<IAction>();

//	private Composite titleComposite;

	private BaseConfiguration imageBC;

	private Composite content;
	
	private ArrayList<BaseConfiguration> registeredListeners = new ArrayList<BaseConfiguration>();

	private Button inherit;
	
	public TDIToolBar(Form form) {
		this(form, SWT.MULTI | SWT.RIGHT | SWT.TITLE);
	}
	
	public TDIToolBar(Form form, int flags) {
		this(form.getHead(), flags);
		form.setHeadClient(this);
	}
	
	public TDIToolBar(Composite parent, int flags) {
		this(parent, flags, (flags & SWT.SINGLE) > 0, (flags & SWT.RIGHT) > 0, (flags & SWT.TITLE) > 0);
	}
	
	public TDIToolBar(Composite parent, int flags, boolean sameRow, boolean flushRight, boolean showTitle) {
		super(parent, flags);
		Utils.setGridLayout(this, 1, false);

		// -- Add the first row
		addRow();

		// Create invisible labels, so they are first no matter what.
		if(showTitle) {
//			titleComposite = new Composite(content, SWT.NONE);
//			Utils.setGridLayout(titleComposite, 2, false);
			imageLabel = new Label(getContent(), SWT.NONE);
			imageLabel.setVisible(false);
			titleLabel = new Label(getContent(), SWT.CENTER);
			titleLabel.setText("");
//			titleLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true));
			if(!sameRow)
				titleLabel.setFont(JFaceResources.getHeaderFont());

			// -- If buttons not on the same row just add a new rowlayout composite
			if(!sameRow) {
				addRow();
			}
		}
	}
	
	public void setText(String title) {
		if(titleLabel == null)
			return;
		
		if (title == null) {
			titleLabel.setText("");
		} else {
			titleLabel.setText(title);
		}
//		titleComposite.layout(true, true);
		getParent().layout(true,true);
	}
	
	/**
	 * Sets the font on the title label.
	 * @param font
	 */
	public void setTitleFont(Font font) {
		if(titleLabel != null)
			titleLabel.setFont(font);
	}
	
	/**
	 * Sets the tooltip on the title label.
	 * 
	 * @param toolTipText
	 */
	public void setTitleToolTipText(String toolTipText) {
		if(titleLabel != null)
			titleLabel.setToolTipText(toolTipText);
	}
	
	/*
	 * This method sets the image and automatically changes icon when the config is enabled/disabled
	 */
	public void setImage(BaseConfiguration bc) {
		if(imageLabel == null || bc == null)
			return;

		setImage(Activator.getImage(bc));
		imageBC = bc;
		imageBC.addListener(this);
		registeredListeners.add(imageBC);
	}
	
	public void configurationChanged(final MetamergeConfigChange mcc) {
		if (isDisposed()) {
			if (imageBC != null)
				imageBC.removeListener(this);
			return;
		}
		
		if (mcc.getSource() == imageBC) {
			Object key = mcc.getKey();
			if (InternalSchema.ENABLED.equals(key) ||
					InternalSchema.HC_ENABLED.equals(key) ||
					InternalSchema.CONNECTOR_STATE.equals(key)) {
				getDisplay().asyncExec(new Runnable() {
					public void run() {
						setImage(Activator.getImage(imageBC));
					}				
				});
			}
		} else if (mcc.getSource() instanceof ScriptConfig){
			getDisplay().asyncExec(new Runnable() {
				public void run() {
					setInheritanceString(inherit, (BaseConfiguration) mcc.getSource());					
				}				
			});
		}
	}

	public void setImage(Image image) {
		if(imageLabel == null)
			return;

		if (image != null) {
			imageLabel.setImage(image);
			imageLabel.setVisible(true);
		} else {
			imageLabel.setVisible(false);
		}
//		titleComposite.layout(true, true);
		getParent().layout(true,true);
	}
	
	/**
	 * Adds a new row to the toolbar that becomes the default composite for new controls
	 */
	public void addRow() {
		content = new Composite(this, SWT.NONE);
		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
		rowLayout.center = true;
		content.setLayout(rowLayout);
		content.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}
	
	/**
	 * Adds a Label control to the current toolbar row
	 * @param flags
	 * @return
	 */
	public Label addLabel(int flags) {
		return new Label(getContent(), flags);
	}
	
	/**
	 * Adds a button control to the current row in the toolbar
	 * @param flags
	 * @return
	 */
	public Button addButton(int flags) {
		return new Button(getContent(), SWT.PUSH);		
	}
	
	/**
	 * Adds a Combo control to the current row in the toolbar
	 * @param flags
	 * @return
	 */
	public Combo addCombo(int flags) {
		return new Combo(getContent(), flags);
	}
	
	public Button add(final IAction action) {
		return add(action, SWT.PUSH);
	}
	
	public Button add(final IAction action, int flags) {
		
		final Button b = new Button(content, flags);
		if (action.getText() != null)
			b.setText(action.getText());
		b.setToolTipText(action.getToolTipText());
		
		if(action.getImageDescriptor() != null) {
			b.setImage(action.getImageDescriptor().createImage(true));
			registerForImageDisposal(b);
		}

		action.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (b.isDisposed()) {
					action.removePropertyChangeListener(this);
					return;
				}
				getDisplay().syncExec(new Runnable() {
					public void run() {
						if (!b.isDisposed()) {
							b.setEnabled( action.isEnabled() );
							b.setSelection(action.isChecked());
							if(action.getText() != null) {
								b.setText(action.getText());
							}
							if(action.getImageDescriptor() != null) {
								Image oldImage = b.getImage();
								b.setImage(action.getImageDescriptor().createImage());
								if(oldImage != null)
									oldImage.dispose();
							}
						}
					}
				});
			}
		});
		b.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				getDisplay().syncExec(new Runnable() {
					public void run() {
						action.setChecked(b.getSelection());
						action.run();
					}
				});
			}			
		});
		b.setEnabled(action.isEnabled());
		
		actions.add(action);
		
		return b;
	}
	
	public void addHelpButton(final String name) {	
		add(new Action() {
			@Override
			public String getText() {
				return Messages.getString("general.help.label");
			}

			@Override
			public String getToolTipText() {
				return Messages.getString("general.help.tooltip");
			}

			@Override
			public void run() {
				ConfigUtils.showHelp(name); 
			}		
		});
	}
	
	public void addHelpButton(final BaseConfiguration bc) {	
		add(new Action() {
			@Override
			public String getText() {
				return Messages.getString("general.help.label");
			}

			@Override
			public String getToolTipText() {
				return Messages.getString("general.help.tooltip");
			}

			@Override
			public void run() {
				ConfigUtils.showHelp(bc); 
			}		
		});
	}
	
	public Button addInheritanceButton(final BaseConfiguration bc) {
		new Label(getContent(), SWT.LEFT).setText(Messages.getString("HooksWidget.0"));
		inherit = new Button(getContent(), SWT.PUSH);
		
		setInheritanceString(inherit, bc);
		
		inherit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				InheritanceUtil.changeInheritance(bc);
				setInheritanceString(inherit, bc);
			}
		});
		
		bc.addListener(this);
		registeredListeners.add(bc);
		
		return inherit;
	}

	private void setInheritanceString(Button inherit, BaseConfiguration bc) {
		if (inherit == null)
			return;

		String str = bc.getInheritsFromRef();
		if(str == null || str.equals("") || str.equals(BaseConfiguration.INHERIT_NONE))
			str = Messages.getString("ConfigChooser.Localized.Inherit.None");
		else if (str.equals(BaseConfiguration.INHERIT_PARENT))
			str = Messages.getString("ConfigChooser.Localized.Inherit.Parent");		
		inherit.setText(str);
		inherit.getParent().layout(true, true);
	}
	
	@Override
	public void dispose() {
		if (isDisposed())
			return;
		for(BaseConfiguration bc : registeredListeners)
			bc.removeListener(this);
		content.dispose();
		super.dispose();
	}

	public PullDownButton addMoreButton(String label, String tooltip, Image image) {
		return new PullDownButton(label, tooltip, image, content);
	}

	public static class PullDownButton {
		private ArrayList<IAction> menuOptions = new ArrayList<IAction>();
		
		public PullDownButton(String label, String tooltip, Image image, Composite parent) {
			Button more = new Button(parent, SWT.PUSH);
			if(label != null)
				more.setText(label);
			if(tooltip != null)
				more.setToolTipText(tooltip);
			if(image != null)
				more.setImage(image);
			
			more.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					Menu menu = createOptionsMenu((Control)e.widget);
					menu.setVisible(true);
				}
			});
		}
		
		public void addMenuOption(IAction action) {
			menuOptions.add(action);
		}
		
		public void removeMenuOption(IAction action) {
			menuOptions.remove(action);
		}
		
		protected Menu createOptionsMenu(Control widget) {
			Menu menu = new Menu(widget.getShell(), SWT.POP_UP);
			for(IAction a : menuOptions) {
				MenuItem item = new MenuItem(menu, SWT.PUSH);
				if(a.getText() != null)
					item.setText(a.getText());
				if(a.getImageDescriptor() != null) {
					item.setImage(a.getImageDescriptor().createImage());
					registerForImageDisposal(item);
				}
				if(!a.isEnabled())
					item.setEnabled(false);
				item.setData("action", a);
				item.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						((IAction)e.widget.getData("action")).run();
					}
				});
			}
			return menu;
		}
	}

	public IAction getItem(int index) {
		return actions.get(index);
	}

	public int getItemCount() {
		return actions.size();
	}

	public int indexOf(IAction item) {
		return actions.indexOf(item);
	}

	public Label getTitleLabel() {
		return titleLabel;
	}

	public Composite getContent() {
		return content;
	}
	
	/**
	 * Register a button which should dispose of its image at the end of the buttons life.
	 * Such images should have been explicitly allocated by application code with new
	 * Image(...), and should not be held under the sway of the ImageRegistry.
	 */
	public static void registerForImageDisposal(final Button button) {
		button.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (!button.isDisposed()) {
					Image image = button.getImage();
					if (image != null) {
						image.dispose();
						button.setImage(null);
					}
				}
			}
		});
	}
	
	/**
	 * Register a MenuItem which should dispose of its image at the end of the MenuItem life.
	 * Such images should have been explicitly allocated by application code with new
	 * Image(...), and should not be held under the sway of the ImageRegistry.
	 */
	public static void registerForImageDisposal(final MenuItem item) {
		item.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (!item.isDisposed()) {
					Image image = item.getImage();
					if (image != null) {
						image.dispose();
						item.setImage(null);
					}
				}
			}
		});
	}

}
