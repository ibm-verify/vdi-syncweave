/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.ibm.di.config.base.LinkCriteriaConfigImpl;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.LinkCriteriaConfig;
import com.ibm.di.config.interfaces.LinkCriteriaItem;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.osgi.ConnectorDelegate;
import com.ibm.di.server.SearchCriteria;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.log.EclipseAppender;

public class LinkCriteriaDialog {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private enum FilterType {
		notes, notesft, ldap, jdbc, mql
	};

	/**
	 * Generates notes search filter based on the user's link criteria
	 * 
	 * @param bc
	 *            Initial contents of the dialog
	 * @return
	 */
	public static String generateNotesSearchFilter(Shell parentShell, BaseConfiguration bc) {
		return generateSearchFilter(parentShell, bc, FilterType.notes);
	}

	/**
	 * Generates notes Full Text search filter based on the user's link criteria
	 * 
	 * @param bc
	 *            Initial contents of the dialog
	 * @return
	 */
	public static String generateNotesFTSearchFilter(Shell parentShell, BaseConfiguration bc) {
		return generateSearchFilter(parentShell, bc, FilterType.notesft);
	}

	/**
	 * Generates LDAP search filter based on the user's link criteria
	 * 
	 * @param bc
	 *            Initial contents of the dialog
	 * @return
	 */
	public static String generateLdapSearchFilter(Shell parentShell, BaseConfiguration bc) {
		return generateSearchFilter(parentShell, bc, FilterType.ldap);
	}

	/**
	 * Generates JDBC search filter based on the user's link criteria
	 * 
	 * @param bc
	 *            Initial contents of the dialog
	 * @return
	 */
	public static String generateJdbcSearchFilter(Shell parentShell, BaseConfiguration bc) {
		return generateSearchFilter(parentShell, bc, FilterType.jdbc);
	}

	/**
	 * Generates MQL search filter based on the user's link criteria
	 * 
	 * @param parentShell
	 *            the parent shell.
	 * @param bc
	 *            Initial contents of the dialog
	 * @return Generated MQL WHERE clause form the provided Search Criteria in
	 *         Configuration.
	 */
	public static String generateMQLSearchFilter(Shell parentShell, BaseConfiguration bc) {
		return generateSearchFilter(parentShell, bc, FilterType.mql);
	}

	private static String generateSearchFilter(Shell parentShell, BaseConfiguration bc, FilterType type) {
		if (bc instanceof RawConnectorConfig)
			bc = bc.getParent();

		LinkCriteriaConfig lcc = null;
		if (bc instanceof LinkCriteriaConfig)
			lcc = (LinkCriteriaConfig) bc;
		else if (bc instanceof ConnectorConfig)
			lcc = ((ConnectorConfig) bc).getLinkCriteria();

		LinkCriteriaDlg dlg = new LinkCriteriaDlg(parentShell, lcc);
		if (dlg.open() == Window.OK) {
			// As the dialog does not allow the user to specify advanced
			// link criteria, do not use it.
			// Also it is better to use the normal parameter dialog for
			// specifying javascript.
			// if(dlg.getConfig().getAdvancedLinkMode())
			// return dlg.getConfig().getAdvancedLinkCriteria();

			SearchCriteria ss = new SearchCriteria();
			boolean hasValue = false;
			for (String str : dlg.getConfig().getCriteriaNames()) {
				LinkCriteriaItem item = dlg.getConfig().getCriteria(str);
				if (item.getAttribute() != null) {
					Object v = item.getValue();
					if (v instanceof String && ((String)v).startsWith("$"))
						v = "{work." + ((String)v).substring(1) + "}";

					ss.addCriteria((String) item.getAttribute(), item.getMatch(), v);
					hasValue = true;
				}
			}
			if (!hasValue)
				return null;

			if (dlg.getConfig().getMatchAny())
				ss.setType(SearchCriteria.SEARCH_OR);
			else
				ss.setType(SearchCriteria.SEARCH_AND);

			if (type == FilterType.jdbc)
				return ss.getSQLFilter();
			else if (type == FilterType.ldap)
				return ss.getLDAPFilter();
			else if (type == FilterType.notes)
				return ss.getNotesFilter();
			else if (type == FilterType.notesft)
				return ss.getNotesFTFilter();
			else if (type == FilterType.mql) {
				ConnectorInterface conn;
				try {
					conn = SystemFunctions.loadConnector((ConnectorConfig) lcc.getParent());
					if (conn instanceof ConnectorDelegate) {
						ConnectorInterface delegate = ((ConnectorDelegate) conn).getDelegate();
						return (String) delegate.getClass().getMethod("createWhereClause", new Class[] { SearchCriteria.class }).invoke(delegate,
								new Object[] { ss });
					}
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e);
				}
			}
		}
		return null;
	}

	private static class LinkCriteriaDlg extends Dialog {

		private LinkCriteriaConfig config;

		protected LinkCriteriaDlg(Shell parentShell, LinkCriteriaConfig lcc) {
			super(parentShell);
			/*
			 * TODO: Cloning a LinkCriteraConfigImpl will return a
			 * BaseConfigurationImpl, giving us a ClassCastException, so we
			 * don't do that. We could add a getClone() method to
			 * LinkCriteriaConfigImpl, though.
			 * 
			 * Why do we want to clone anyway? Probably best to not clone, then
			 * it is easier for the user to change the link criteria later. Or
			 * we could parse the constructed search filter, but it is easier to
			 * just leave the LinkCriteriaConfig we used the last time.
			 */
			// if(lcc != null) {
			// try {
			// config = (LinkCriteriaConfig) lcc.getClone();
			// } catch (Exception e) {
			// EclipseAppender.logerror(e.toString(), e);
			// }
			// }
			//			
			config = lcc;

			if (config == null) {
				config = new LinkCriteriaConfigImpl();
				try {
					config.init();
				} catch (Exception e) {
					SystemFunctions.doNothing();
				}
			}
		}

		@Override
		protected Point getInitialSize() {
			return new Point(700, 300);
		}

		@Override
		protected int getShellStyle() {
			return super.getShellStyle() | SWT.RESIZE;
		}

		public LinkCriteriaConfig getConfig() {
			return config;
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite c = (Composite) super.createDialogArea(parent);
			c.setLayout(new FillLayout());
			new LinkCriteriaWidget(config, c, 0, false);

			getShell().setText(Messages.getString("ConnectorUI.LinkCriteria.label"));

			return c;
		}

	}

}
