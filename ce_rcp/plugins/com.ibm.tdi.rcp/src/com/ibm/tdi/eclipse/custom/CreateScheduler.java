/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.custom;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.di.config.interfaces.SchedulerConfig;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.util.Schedule;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.GregorianCalendar;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.widget.FormWidget2;

public class CreateScheduler extends Canvas implements MetamergeConfigChangeListener {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Composite schedule;

	private Composite monthComp; // For choosing the month
	private Composite mdayComp; // For choosing the day in the month
	private Composite wdayComp; // For choosing the day in the week
	private Composite hourComp; // For choosing the hour
	private Combo minuteComp; // For choosing the minute
	private Combo secondComp; // For choosing seconds...

	private Composite warningComp;
	private Label warning;

	private SchedulerConfig config; // The config we are editing
	
	private FormWidget2 form;
	private String paramName;

	private final static String[] DEFAULT_COMBO_VALUES = {
		"0","0,30","0,20,40", "0,15,30,45", "0,10,20,30,40,50", "0,5,10,15,20,25,30,35,40,45,50,55", "*"};

	private final static String ALL = "*";

	private final static String SEP = ",";

	private final static String DEFAULT_START_TIMES = "* * * * 0 0";
	
	private boolean isUpdating;
	
	// Save the schedule every time the user presses a button
	private final SelectionAdapter sel = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			saveSchedule();
		}			
	};

	/**
	 * COnstructor as needed by FormWidget2.
	 * @param form The parent FormWidget2
	 * @param parent
	 * @param editingConfig
	 * @param name
	 */
	public CreateScheduler (FormWidget2 form, Composite parent, BaseConfiguration editingConfig, String name) {
		super(parent, SWT.BORDER);
		this.form = form;
		this.config = (SchedulerConfig) editingConfig;
		this.paramName = name;
		setLayout(new FillLayout());	
		createSchedule();
		config.addListener(this);
	}

	/**
	 * Creates a schedule, where the user can choose month, day, hour, minute and even seconds
	 */
	private void createSchedule() {
		schedule = new Composite(this, SWT.NONE);
		schedule.setLayout(new GridLayout(1, false));
		schedule.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

		String s = config.getStartTimes();
		String err = null;
		if (s == null || s.length() < 9) {
			s = DEFAULT_START_TIMES; // Default value //$NON-NLS-1$
			isUpdating = true;
			config.setStartTimes(s);
			isUpdating = false;
		}
		
		String[] st = s.split(" "); //$NON-NLS-1$
		if (st.length < 6) {
			err = ResourceHash.getHash("miserver").getString("Schedule.number.of.fields", s);
			st = DEFAULT_START_TIMES.split(" "); //Temporary
		}

		createMonth(st[0]);
		createDay(st[1], st[2]);
		createHour(st[3]);
		createMinute(st[4]);
		createSeconds(st[5]);
		addTestButton();
		if (err != null)
			setWarning(err);
	}

	/**
	 * Creates a hour group
	 * @param hour Current value of the hour parameter
	 */
	private void createHour(String hour) {
		Group g = createScheduleGroup("CreateScheduler.Hour");

		hourComp = new Composite(schedule, SWT.NONE);
		hourComp.setLayout(new GridLayout(6, true));

		String[] match = hour.split(SEP); //$NON-NLS-1$
		int k = 0;
		for (int i = 0; i < 24; i++) {
			Button b = new Button(hourComp, SWT.CHECK);
			String si = String.valueOf(i);
			b.setText(si);
			if (k < match.length && si.equals(match[k])) {
				b.setSelection(true);
				k++;
			}
			b.addSelectionListener(sel);
		}

		addListeners(g, hourComp, hour);
	}

	/**
	 * Creates a day group, with option for days in Week or days in Month.
	 * @param mday Current value for the MonthDay parameter
	 * @param wday Current value for the WeekDay parameter
	 */
	private void createDay(String mday, String wday) {
		//Day /Weekday
		Group g = createScheduleGroup("CreateScheduler.Day");

		Button all = createRadioButton(g, "CreateScheduler.All");		
		final Button dim = createRadioButton(g, "CreateScheduler.DIM");
		final Button diw = createRadioButton(g, "CreateScheduler.DIW");

		mdayComp = new Composite(schedule, SWT.NONE);
		mdayComp.setLayout(new GridLayout(7, true));

		String[] match = mday.split(SEP); //$NON-NLS-1$
		int k = 0;
		for (int i = 0; i < 31; i++) {
			Button b = new Button(mdayComp, SWT.CHECK);
			String si = String.valueOf(i+1);
			b.setText(si);
			if (k < match.length && si.equals(match[k])) {
				b.setSelection(true);
				k++;
			}
			b.addSelectionListener(sel);
		}

		wdayComp = new Composite(schedule, SWT.NONE);
		wdayComp.setLayout(new GridLayout(4, true));

		SimpleDateFormat sd = new SimpleDateFormat("EEEE");  //$NON-NLS-1$ // Weekday
		GregorianCalendar date = new GregorianCalendar(2000, 0, 2); // A random Sunday
		match = wday.split(SEP); //$NON-NLS-1$
		k = 0;
		for (int i = 0; i < 7; i++) {
			Button b = new Button(wdayComp, SWT.CHECK);
			b.setText(sd.format(date));
			date.add(Calendar.DATE, 1);
			if (k < match.length && String.valueOf(i+1).equals(match[k])) {
				b.setSelection(true);
				k++;
			}
			b.addSelectionListener(sel);
		}

		if (! ALL.equals(mday)) { //$NON-NLS-1$
			dim.setSelection(true);
			setVisible(wdayComp, false);
		} else if (! ALL.equals(wday)) { //$NON-NLS-1$
			diw.setSelection(true);
			setVisible(mdayComp, false);
		} else {
			all.setSelection(true);
			setVisible(mdayComp, false);
			setVisible(wdayComp, false);
		}

		SelectionAdapter sa = new SelectionAdapter () {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource() instanceof Button) {
					Button b = (Button) e.getSource();
					if (! b.getSelection())
						return;
					if ( b == dim ) {
						setVisible(mdayComp, true);
						setVisible(wdayComp, false);					
					} else if (b == diw) {
						setVisible(wdayComp, true);
						setVisible(mdayComp, false);										
					} else {
						setVisible(wdayComp, false);
						setVisible(mdayComp, false);															
					}
					saveSchedule();
				}
			}		
		};
		all.addSelectionListener(sa);
		dim.addSelectionListener(sa);
		diw.addSelectionListener(sa);
	}

	/**
	 * Creates a month group
	 * @param month
	 */
	private void createMonth(String month) {
		// Month
		Group g = createScheduleGroup("CreateScheduler.Month");

		monthComp = new Composite(schedule, SWT.NONE);
		monthComp.setLayout(new GridLayout(6, true));

		SimpleDateFormat sd = new SimpleDateFormat("MMMM"); // Month //$NON-NLS-1$
		GregorianCalendar date = new GregorianCalendar(2000, 0, 1);
		String[] match = month.split(SEP); //$NON-NLS-1$
		int k = 0;
		for (int i = 0; i < 12; i++) {
			Button b = new Button(monthComp, SWT.CHECK);
			date.set(Calendar.MONTH, i);
			b.setText(sd.format(date));
			if (k < match.length && String.valueOf(i).equals(match[k])) {
				b.setSelection(true);
				k++;
			}
			b.addSelectionListener(sel);
		}

		addListeners(g, monthComp, month);
	}

	/**
	 * Creates a minute component, we use a dropdown here.
	 * @param minute
	 */
	private void createMinute(String minute) {
		Group g = createScheduleGroup("CreateScheduler.Minute");
		g.setToolTipText(Messages.getString("CreateScheduler.Minute.tooltip"));

		minuteComp = new Combo(g, SWT.DROP_DOWN);

		setupDropdown(g, minuteComp, minute);
	}

	/**
	 * Add default values and listeners to a drop down.
	 * @param g
	 * @param dropdown
	 * @param value
	 */
	private void setupDropdown(Group g, Combo dropdown, String value) {
		dropdown.setItems(DEFAULT_COMBO_VALUES);
		int i = dropdown.indexOf(value);
		if (i >= 0) {
			dropdown.select(i);
		} else {
			dropdown.add(value, 0);
			dropdown.select(0);		
		}

		dropdown.addSelectionListener(sel);
		dropdown.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				saveSchedule();
			}	
		});
	}

	/**
	 * Creates a second component, to allow the user to specify seconds
	 * @param seconds The nitial value for the dropdown
	 */
	private void createSeconds(String seconds) {
		Group g = createScheduleGroup("CreateScheduler.Second");

		secondComp = new Combo(g, SWT.DROP_DOWN);
		setupDropdown(g, secondComp, seconds);
	}

	/**
	 * Creates one group in the scheduler. which will contain 2-2 buttons
	 * @param key
	 * @return
	 */
	private Group createScheduleGroup(String key) {
		Group g = new Group(schedule, SWT.SHADOW_NONE);
		g.setLayout(new GridLayout(3, false));
		g.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		g.setText(Messages.getString(key));
		return g;
	}

	/**
	 * Creates buttons and adds Listeners to a group.
	 * @param group The gtoup to create buttons in.
	 * @param comp The component that will be shown/hidden
	 * @param current If a star, the all button is default selected.
	 */
	private void addListeners(Group g, final Composite comp, String current) {
		addListeners(g, comp, current, 
				createRadioButton(g, "CreateScheduler.All"),
				createRadioButton(g, "CreateScheduler.Specific"));
	}

	/**
	 * Adds Listeners to a group.
	 * The specific button will cause all options to be visible,
	 * the all button will hide them.
	 */
	private void addListeners(Group g, final Composite comp, String current, Button all, final Button specific) {
		if (ALL.equals(current)) { //$NON-NLS-1$
			all.setSelection(true);
			setVisible(comp, false);
		} else {
			specific.setSelection(true);
		}

		SelectionAdapter sa = new SelectionAdapter () {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource() instanceof Button) {
					Button b = (Button) e.getSource();
					if (! b.getSelection())
						return;
					setVisible(comp, b == specific);
					saveSchedule();
				}
			}		
		};
		all.addSelectionListener(sa);
		specific.addSelectionListener(sa);
	}

	private Button createRadioButton(Group g, String key) {
		Button b = new Button(g, SWT.RADIO);
		b.setText(Messages.getString(key));
		return b;
	}

	/**
	 * Gets all the values the user has ticked off, and saves them.
	 * This method is called every time the user checks or unchecks a box.
	 */
	private void saveSchedule() {
		setWarning(null);
		isUpdating = true;
		config.setStartTimes(getMonth() + " " + getMDay() + " " + getWDay() + 
				" " + getHour() + " " + getMinute() + " " + getSeconds());
		form.setLabelColor(paramName);
		isUpdating = false;
	}

	private String getMonth() {
		return getValues(monthComp, 0);
	}

	private String getHour() {
		return getValues(hourComp, 0);
	}

	private String getWDay() {
		return getValues(wdayComp, 1);
	}

	private String getMDay() {
		String s = getValues(mdayComp, 1);
		if (!s.equals(ALL))
			validateDay(s, getMonth());
		return s;
	}

	/**
	 * Gets all the values from a Component, comma separated.
	 * If it is not visible, will return a * to indicate all.
	 * @param comp
	 * @param offset An offset to add, because sometimes the numbers are 1-based.
	 * @return
	 */
	private String getValues(Composite comp, int offset) {
		StringBuilder ret = new StringBuilder();
		if (comp.isVisible()) {
			Control[] children = comp.getChildren();
			for (int i = 0; i < children.length; i++) {
				if (children[i] instanceof Button && ((Button)children[i]).getSelection()) {
					if (ret.length() > 0)
						ret.append(SEP); //$NON-NLS-1$
					ret.append(i + offset);
				}
			}
		}
		if (ret.length() == 0)
			return ALL; //$NON-NLS-1$
		return ret.toString();
	}

	private String getMinute() {
		return validateList(minuteComp.getText(), "CreateScheduler.Minute");
	}

	private String getSeconds() {
		return validateList(secondComp.getText(), "CreateScheduler.Second");
	}

	/**
	 * Validate that we have a list of increasing numbers
	 * @param range
	 * @param key
	 * @return
	 */
	private String validateList(String range, String key) {
		range = range.replaceAll("\\s", "");
		if (range.equals(ALL))
			return range;
		boolean err = false;
		if (range.length() == 0) {
			err = true;
		} else {
			try {
				int prev = -1;
				for (String s:range.split(SEP)) {
					int i=Integer.valueOf(s);
					if (i<= prev || i > 59)
						err = true;
					prev = i;
				}
			} catch (NumberFormatException nfe) {
				err = true;
			}
		}
		if (err) {
			setWarning(Messages.getMessage("CreateScheduler.illegal.list", Messages.getString(key)));
			return "0";
		}
		return range;		
	}

	/**
	 * Validate the day/month combination
	 * @param days
	 * @param months
	 */
	private void validateDay(String days, String months) {
		int i = days.lastIndexOf(',');
		int last = Integer.parseInt(days.substring(i+1));
		if (last < 29)
			return;

		boolean more = i > 0;
		boolean specific = false;
		boolean err = false;

		if (months.equals("1")) { //February has max 29 days.
			specific = last > 29;
			err = true;
		} else if (last <= 30 ) {
			err = contains(months, "1");
		} else if (contains(months,  "1", "3", "5", "8", "10")){
			err = true;
			specific = !months.equals(ALL) && months.indexOf(',') == -1;
		}

		if (err) {
			if (specific)
				setWarning(Messages.getString("CreateScheduler.day.fewer"));
			else if (more)
				setWarning(Messages.getString("CreateScheduler.day.combinations"));
			else 
				setWarning(Messages.getString("CreateScheduler.day.month"));
		}
	}

	/**
	 * Return true if months contains one of the months in check.
	 * @param months
	 * @param check
	 * @return
	 */
	private boolean contains(String months, String... check) {
		if (months.equals(ALL))
			return true;
		List<String> list = Arrays.asList(months.split(SEP));
		for (String s:check)
			if(list.indexOf(s)>=0)
				return true;
		return false;
	}

	/**
	 * Sets a component visible or invisible
	 * @param comp The Component
	 * @param b If false, the component will become invisible.
	 */
	private void setVisible(Composite comp, boolean b) {
		if (comp.getVisible() == b)
			return;
		comp.setVisible(b);
		GridData gd = (GridData) comp.getLayoutData();
		if (gd == null) {
			gd = new GridData();
			comp.setLayoutData(gd);
		}
		gd.exclude = !b;
		updateSize();
	}

	private void updateSize() {
		schedule.layout(true);

		// If we are inside a ScrolledComposite, we need to update it.
		Composite c = getParent();
		while (c != null) {
			c.layout(true);
			if (c instanceof ScrolledComposite) {
				ScrolledComposite sc = (ScrolledComposite) c;
				try {
					sc.setMinSize(sc.getContent().computeSize(SWT.DEFAULT, SWT.DEFAULT));
				} catch (Exception e) {
					return; // Not ready yet
				}
				sc.setExpandHorizontal(true);
				sc.setExpandVertical(true);
				return;
			}
			c = c.getParent();
		}
		
	}
	
	private void setWarning(String s) {
		if (s == null) {
			if (warningComp != null)
				setVisible(warningComp, false);
			return;
		}		
		if (warningComp == null) {
			warningComp = new Composite(schedule, SWT.NONE);
			warningComp.setLayout(new FillLayout());
			warningComp.setVisible(false);
			warning = new Label(warningComp, SWT.NONE);
			warning.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));			
		}
		warning.setText(s);
		setVisible(warningComp, true);
	}

	/**
	 * Adds a test button
	 */
	private void addTestButton() {
		Button b = new Button(schedule, SWT.PUSH);
		b.setText(Messages.getString("CreateScheduler.Test.label"));
		b.setToolTipText(Messages.getString("CreateScheduler.Test.tooltip"));
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				new ScheduleDialog(getShell(), config.getStartTimes()).open();
			}
		});
	}

	/**
	 * A Dialog class to show dates
	 */
	private static class ScheduleDialog extends Dialog {

		Date current; // The last date shown
		Schedule schedule; // The schedule used to compute the next date
		Text text; // a Text to show the dates
		String err;

		protected ScheduleDialog(Shell parentShell, String schedule) {
			super(parentShell);
			try {
				this.schedule = new Schedule(schedule);
			} catch (Exception e) {
				err = e.getLocalizedMessage() + "\n";
			}
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite c = (Composite) super.createDialogArea(parent);
			text = new Text(c, SWT.MULTI);
			showDates();
			getShell().setText(Messages.getString("CreateScheduler.Test.title"));
			return c;
		}


		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.PROCEED_ID,
					Messages.getString("DiscoverSchemaWidget.next"), false);
			createButton(parent, IDialogConstants.OK_ID,
					Messages.getString("DiscoverSchemaWidget.close"), true);
		}

		@Override
		protected void buttonPressed(int buttonId) {
			if (buttonId == IDialogConstants.PROCEED_ID)
				showDates();
			else
				super.buttonPressed(buttonId);
		}

/**
 * puts the 10 first/next dates in the text window
 */
		private void showDates() {
			if (err != null) {
				text.setText(err);
				return;
			}
			StringBuilder buf = new StringBuilder();
			DateFormat df = DateFormat.getDateTimeInstance();
			try {
				for (int i = 0; i < 10; i++) {
					current = schedule.getNext(current);
					buf.append(df.format(current));
					buf.append("\n");
				}
			} catch (Exception e) {
				buf.append(e.getLocalizedMessage());
				buf.append("\n");
			}
			text.setText(buf.toString());
		}
	}

	public void configurationChanged(MetamergeConfigChange changeEvent) {
		if (isUpdating)
			return;
		if (paramName.equals(changeEvent.getKey())) {
			if (isDisposed())
				return;
			getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (isDisposed())
						return;
					schedule.dispose();
					warningComp = null;
					createSchedule();
					updateSize();
				}
			});

		}
	}

	@Override
	public void dispose() {
		if (config != null)
			config.removeListener(this);
		super.dispose();
	}

}
