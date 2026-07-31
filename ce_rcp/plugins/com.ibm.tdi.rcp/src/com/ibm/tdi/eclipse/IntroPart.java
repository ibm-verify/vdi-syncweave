/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse;

import java.io.File;
import java.util.Hashtable;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.IIntroSite;
import org.w3c.dom.Node;

import com.ibm.tdi.eclipse.actions.TDIHelpMenuAction;
import com.ibm.tdi.eclipse.widget.BaseWidget;
import com.ibm.tdi.eclipse.wizards.ImportConfigWizard;
import com.ibm.tdi.eclipse.wizards.NewProject;

/*
 *
 *
 */
public class IntroPart implements IIntroPart {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String GOTO_EXAMPLES = "gotoExamples"; //$NON-NLS-1$

	private static final String CREATE_PROJECT = "createProject"; //$NON-NLS-1$

	private static final String GOTO_WORKBENCH = "gotoWorkbench"; //$NON-NLS-1$

	private static final String GOTO_DOCUMENTATION = "welcome"; //$NON-NLS-1$

	private static final String GOTO_JAVADOCS = "goto_javadocs"; //$NON-NLS-1$

	private static final String GOTO_GETTING_STARTED = "started-introducing-verify-directory-integrator"; //$NON-NLS-1$

	private static final String IMPORT_PROJECT = "import_project"; //$NON-NLS-1$

	private static final String GOTO_VIDEOS = "goto_videos"; //$NON-NLS-1$
	
	private static final String LAUNCH_ETL = "ETLView"; //$NON-NLS-1$

	private static final String DEFAULT_VIDEO_URL = "http://pic.dhe.ibm.com/infocenter/ieduasst/secv1r0/index.jsp?topic=/com.ibm.iea.tdi/plugin_coverpage.html";
	
	private static final String GOTO_JAVASCRIPT = "goto_javascript";

	private IIntroSite site;

	private String[] _sections = new String[] {
			"intro.section.actions", //$NON-NLS-1$
			"intro.section.learning", //$NON-NLS-1$
			"intro.section.videos", //$NON-NLS-1$
	};

	private String[] _images = new String[] {
			"/intro/images/task_icon.gif", //$NON-NLS-1$
			"/intro/images/train_icon.gif", //$NON-NLS-1$
			"/intro/images/train_icon.gif", //$NON-NLS-1$
	};

	private Hashtable<String, String> _urls = new Hashtable<String, String>();

	private ScrolledComposite scroller;

//	private Label videoStatus = null;

//	private Document videoRSS;

//	private Composite videoContainer = null;

	private BaseWidget wid;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.intro.IIntroPart#addPropertyListener(org.eclipse.ui.IPropertyListener)
	 */
	public void addPropertyListener(IPropertyListener listener) {
	}

	public void createPartControl(Composite parent) {

		_urls.put("intro.section.actions.1", LAUNCH_ETL); //$NON-NLS-1$
		_urls.put("intro.section.actions.2", CREATE_PROJECT); //$NON-NLS-1$
		_urls.put("intro.section.actions.3", IMPORT_PROJECT); //$NON-NLS-1$
		_urls.put("intro.section.actions.4", GOTO_WORKBENCH); //$NON-NLS-1$
		_urls.put("intro.section.learning.1", GOTO_GETTING_STARTED); //$NON-NLS-1$
		_urls.put("intro.section.learning.2", GOTO_DOCUMENTATION); //$NON-NLS-1$
		_urls.put("intro.section.learning.3", GOTO_JAVADOCS); //$NON-NLS-1$
		_urls.put("intro.section.learning.4", GOTO_EXAMPLES); //$NON-NLS-1$
		_urls.put("intro.section.learning.5", GOTO_JAVASCRIPT); //$NON-NLS-1$
		_urls.put("intro.section.videos.1", GOTO_VIDEOS); //$NON-NLS-1$

		scroller = new ScrolledComposite(parent, SWT.H_SCROLL|SWT.V_SCROLL);
		Composite contents = createContents(scroller);

		scroller.setLayout(new FillLayout());
		scroller.setExpandVertical(true);
		scroller.setExpandHorizontal(true);
		scroller.setContent(contents);

		updateMinSize();

		scroller.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				updateMinSize();
			}
		});

	}

	/**
	 * Update the minimum size so the parent container can refresh scroll bars.
	 */
	private void updateMinSize() {
		Point size = scroller.getContent().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		scroller.setMinSize(size);
		scroller.layout(true, true);
	}

	private Composite createContents(Composite parent) {
		GridData gd;

		wid = new BaseWidget(parent, SWT.NONE);
		wid.setLayout(new FillLayout());
		wid.createForm(wid, null);

		Composite body = wid.getForm().getBody();
		GridLayout layout = new GridLayout(3,false);
		layout.marginTop = 30;
		layout.marginLeft = 30;
		layout.horizontalSpacing = 10;
		body.setLayout(layout);
		int index = 0;

		// -- Welcome title (spans 3 cols)
		Label title = wid.getFormToolKit().createLabel(body, Messages.getString("miadmin.frametitle")); //$NON-NLS-1$
		title.setFont(JFaceResources.getHeaderFont());
		gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
		gd.horizontalSpan = 3;
		title.setLayoutData(gd);

		//
		// -- For each section: Image,	Title
		// -- 				  : 		HyperLink	Description
		// --							etc ....
		//
		for(String str : _sections) {
			Image img = Activator.getImage(_images[index++]);
			Label label = wid.getFormToolKit().createLabel(body, ""); //$NON-NLS-1$
			label.setImage(img);
			gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false);
			label.setLayoutData(gd);

			title = wid.getFormToolKit().createLabel(body, Messages.getString(str));
			title.setFont(JFaceResources.getBannerFont());
			gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
			gd.horizontalSpan = 2;
			title.setLayoutData(gd);

			int bullets = addBullets(wid.getFormToolKit(), body, str);

			// -- Update vertical span for image
			((GridData)label.getLayoutData()).verticalSpan = bullets;

			// -- Add an empty row between sections
			Label gap = wid.getFormToolKit().createLabel(body, "", SWT.NONE); //$NON-NLS-1$
			gd = new GridData();
			gd.horizontalSpan = 3;
			gap.setLayoutData(gd);

		}

		try {
			IViewPart view = site.getWorkbenchWindow().getActivePage().findView("org.eclipse.ui.views.ResourceNavigator"); //$NON-NLS-1$
			if (view != null)
				site.getWorkbenchWindow().getActivePage().hideView(view);

		} catch (Exception e) {
		}

		return wid;
	}

	private int addVideoBullets(FormToolkit tk, Composite section) {

		Composite row = new Composite(section, SWT.NONE);
		row.setBackground(section.getBackground());
		row.setForeground(section.getForeground());
		Utils.setGridLayout(row, 2, false);

		// -- Link
		Hyperlink link = tk.createHyperlink(row, Messages.getString("intro.section.videos"), SWT.NONE);
		link.setUnderlined(true);
		link.setData("intro.section.videos.1"); //$NON-NLS-1$
		link.addHyperlinkListener(new IHyperlinkListener() {
			public void linkActivated(HyperlinkEvent e) {
				openURL(""+((Hyperlink)e.getSource()).getData()); //$NON-NLS-1$
			}
			public void linkEntered(HyperlinkEvent e) {}
			public void linkExited(HyperlinkEvent e) {}
		});

		GridData gd = new GridData(SWT.DEFAULT, SWT.TOP, false, false);
		gd.horizontalSpan = 2;
		row.setLayoutData(gd);

		return 2;
		/*

		if(videoContainer == null) {
			videoContainer = new Composite(section, SWT.NONE);
			videoContainer.setBackground(section.getBackground());
			Utils.setGridLayout(videoContainer, 1, false);
		}

		if(videoStatus != null) {
			videoStatus.dispose();
			videoStatus = null;
		}

		if(videoRSS == null) {
			String url = System.getProperty("com.ibm.tdi.videos.rss");
			if(url == null || url.length() == 0) {
				videoStatus = tk.createLabel(videoContainer, Messages.getString("IntroPart.loading.empty"));
			} else {
				videoStatus = tk.createLabel(videoContainer, Messages.getString("IntroPart.loading.rss"));
				loadVideoRSS();
			}
			return 2;
		}

		int count = 1;
		try {
			NodeList list = XPathAPI.selectNodeList(videoRSS.getDocumentElement(), "//item");
			for(int i = 0; i < list.getLength(); i++) {
				Element item = (Element) list.item(i);
				Node etitle = XPathAPI.selectSingleNode(item, "title");
				Node edescription = XPathAPI.selectSingleNode(item, "description");
				Node elink = XPathAPI.selectSingleNode(item, "link");
				Node edate = XPathAPI.selectSingleNode(item, "date");

				String label = "()";
				if(etitle != null) {
					label = getNodeText(etitle);
				}

				String description = "()";
				if(edescription != null) {
					description = getNodeText(edescription);
				}

				String date = "";
				if(edate != null) {
					date = getNodeText(edate);
				}

				String url = "";
				if(elink != null) {
					url = getNodeText(elink);
				}

				Hyperlink link = tk.createHyperlink(section, label + "("+date+")", SWT.NONE);
				link.setUnderlined(true);
				link.setHref(url);
				link.addHyperlinkListener(new IHyperlinkListener() {
					public void linkActivated(HyperlinkEvent e) {
						openURL(""+e.getHref()); //$NON-NLS-1$
					}
					public void linkEntered(HyperlinkEvent e) {}
					public void linkExited(HyperlinkEvent e) {}
				});

				GridData gd = new GridData(SWT.DEFAULT, SWT.TOP, false, false);
				gd.horizontalSpan = 2;
				link.setLayoutData(gd);

				Label desc = tk.createLabel(section, description, SWT.WRAP);
				gd = new GridData(SWT.DEFAULT, SWT.TOP, false, false);
				gd.horizontalSpan = 2;
				desc.setLayoutData(gd);

				count++;
			}
		} catch (Exception e) {
		}
		return 2;
//		return count * 2;

		*/
	}

//	private void loadVideoRSS() {
//		Job job = new Job(System.getProperty("com.ibm.tdi.videos.rss")) {
//			@Override
//			protected IStatus run(IProgressMonitor monitor) {
//				try {
//					videoRSS = null;
//					URL url = new URL(System.getProperty("com.ibm.tdi.videos.rss"));
//					videoRSS = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(url.openStream()));
//				} catch (Exception e) {
//					return EclipseAppender.statusException(e);
//				}
//				return Status.OK_STATUS;
//			}
//		};
//		job.addJobChangeListener(new IJobChangeListener() {
//			public void aboutToRun(IJobChangeEvent event) {
//			}
//			public void awake(IJobChangeEvent event) {
//			}
//			public void done(IJobChangeEvent event) {
//				// Both videoStatus and videoContainer are null, so no place to show anything.
////				if(!event.getResult().equals(Status.OK_STATUS) && videoStatus != null) {
////					final String msg = event.getResult().getException().toString();
////					UIJob ui = new UIJob("") {
////						@Override
////						public IStatus runInUIThread(IProgressMonitor monitor) {
////							videoStatus.setText(msg);
////							updateMinSize();
////							return Status.OK_STATUS;
////						}
////					};
////					ui.schedule();
////				} else if(videoRSS != null) {
////					UIJob ui = new UIJob("") {
////						@Override
////						public IStatus runInUIThread(IProgressMonitor monitor) {
////							addVideoBullets(wid.getFormToolKit(), videoContainer);
////							updateMinSize();
////							return Status.OK_STATUS;
////						}
////					};
////					ui.schedule();
////				}
//			}
//			public void running(IJobChangeEvent event) {
//			}
//			public void scheduled(IJobChangeEvent event) {
//			}
//			public void sleeping(IJobChangeEvent event) {
//			}
//		});
//
//		job.schedule();
//	}
//
	/**
	 * @param node
	 *            Node object
	 * @return text from a node's children
	 * @throws Exception
	 */
	public String getNodeText(Node node) throws Exception {
		StringBuffer buf = new StringBuffer();
		Node n = node.getFirstChild();
		while (n != null) {
			switch (n.getNodeType()) {
			case Node.TEXT_NODE:
			case Node.CDATA_SECTION_NODE:
				buf.append(n.getNodeValue());
				break;
			case Node.ELEMENT_NODE:
				buf.append(getNodeText(n));
				break;
			default:
				break;
			}
			n = n.getNextSibling();
		}

		return buf.toString();
	}

	private int addBullets(FormToolkit tk, Composite section, String str) {
		int i = 1;

		while(true) {
			String label = Messages.getString(str + "." + i + ".label"); //$NON-NLS-1$ //$NON-NLS-2$
			if(label == null || label.length() == 0)
				return i;

			String description = Messages.getString(str + "." + i + ".description"); //$NON-NLS-1$ //$NON-NLS-2$
			if(description == null || description.length() == 0)
				return i;


			Composite row = new Composite(section, SWT.NONE);
			row.setBackground(section.getBackground());
			row.setForeground(section.getForeground());
			Utils.setGridLayout(row, 2, false);

			// -- Link
			Hyperlink link = tk.createHyperlink(row, label, SWT.NONE);
			link.setUnderlined(true);
			link.setData(str + "." + i); //$NON-NLS-1$
			link.addHyperlinkListener(new IHyperlinkListener() {
				public void linkActivated(HyperlinkEvent e) {
					openURL(""+((Hyperlink)e.getSource()).getData()); //$NON-NLS-1$
				}
				public void linkEntered(HyperlinkEvent e) {}
				public void linkExited(HyperlinkEvent e) {}
			});

			// -- additional text
			tk.createLabel(row, " - " + description, SWT.WRAP);

			GridData gd = new GridData(SWT.DEFAULT, SWT.TOP, false, false);
			gd.horizontalSpan = 2;
			row.setLayoutData(gd);

			i++;
		}
	}

	protected void openURL(String href) {
		String url = _urls.get(href);

		if(GOTO_EXAMPLES.equals(url))
			openExamples();
		else if(LAUNCH_ETL.equals(url))
			launchETL();
		else if(CREATE_PROJECT.equals(url))
			createProject();
		else if(IMPORT_PROJECT.equals(url))
			importProject();
		else if(GOTO_WORKBENCH.equals(url))
			closeIntro();
		else if(GOTO_GETTING_STARTED.equals(url))
			openDocumentation(url);
		else if(GOTO_DOCUMENTATION.equals(url))
			openDocumentation(url);
		else if(GOTO_JAVADOCS.equals(url))
			openJavaDocs();
		else if(GOTO_VIDEOS.equals(url))
			openVideos();
		else if(GOTO_JAVASCRIPT.equals(url))
			openJavaScript();
		else if(url != null)
			ConfigUtils.showURL(url);
		else
			MessageDialog.openError(site.getShell(), "Error", href + " has no URL configured"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void launchETL() {
		PlatformUI.getWorkbench().getIntroManager().closeIntro(this);
		setPerspective("com.ibm.tdi.rcp.perspective.etl");
	}

	private void importProject() {
		ImportConfigWizard newwiz = new ImportConfigWizard();
		newwiz.init(site.getWorkbenchWindow().getWorkbench(), StructuredSelection.EMPTY);
		WizardDialog wiz = new WizardDialog(site.getShell(), newwiz);
		if (wiz.open() == Window.OK) {
			closeIntro();
		}
	}

	private void openVideos() {
		String str = System.getProperty("com.ibm.tdi.videos.rss");
		if(str == null || str.equals(""))
			str = DEFAULT_VIDEO_URL;
		ConfigUtils.showURL(str);	}

	private void openJavaDocs() {
		String str = "file://" + Activator.getInstallPath() + File.separator + "docs/api/index.html"; //$NON-NLS-1$ //$NON-NLS-2$
		ConfigUtils.showURL(str);
	}

	private void openJavaScript() {
		new TDIHelpMenuAction().showJavaScriptHelp();
	}

	private void openExamples() {
		String str = "file://" + Activator.getInstallPath() + File.separator + "examples"; //$NON-NLS-1$ //$NON-NLS-2$
		ConfigUtils.showURL(str);
	}

	private void openDocumentation(String url) {

		String helpHost = System.getProperty("com.ibm.di.helpHost");

		if (helpHost != null && helpHost.length() > 0) {
			String helpFolder = "";
			int i = helpHost.indexOf('/');
			if (i > 0) {
				helpFolder = helpHost.substring(i);
				helpHost = helpHost.substring(0, i);
			}
			String helpPort = System.getProperty("com.ibm.di.helpPort");
			if (helpPort != null && helpPort.length() > 0)
				helpPort = ":" + helpPort;
			else
				helpPort = "";

			ConfigUtils.showURL("http://" + helpHost + helpPort + helpFolder + "?topic=" + url);
		}
	}

	private void createProject() {
		NewProject newwiz = new NewProject();
		newwiz.init(site.getWorkbenchWindow().getWorkbench(), StructuredSelection.EMPTY);
		WizardDialog wiz = new WizardDialog(site.getShell(), newwiz);
		if (wiz.open() == Window.OK) {
			closeIntro();
		}
	}

	private void closeIntro() {
		PlatformUI.getWorkbench().getIntroManager().closeIntro(this);
	}

	public void dispose() {
		setPerspective("com.ibm.tdi.rcp.perspective");
	}

	private static void setPerspective(final String id) {
		final IWorkbenchWindow wb = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (wb != null) {
			wb.getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					IPerspectiveRegistry registry = PlatformUI.getWorkbench().getPerspectiveRegistry();
					registry.setDefaultPerspective("com.ibm.tdi.rcp.perspective");
					IWorkbenchPage page = wb.getActivePage();
					if (page != null && registry != null)
						page.setPerspective(registry.findPerspectiveWithId(id));
				}
			});
		}
	}
	
	public IIntroSite getIntroSite() {
		return site;
	}

	public String getTitle() {
		return "SyncWeave"; //$NON-NLS-1$
	}

	public Image getTitleImage() {
		return Activator.getImage("Neo"); //$NON-NLS-1$
	}

	public void init(IIntroSite site, IMemento memento) throws PartInitException {
		this.site = site;
	}

	public void removePropertyListener(IPropertyListener listener) {
	}

	public void saveState(IMemento memento) {
	}

	public void setFocus() {
	}

	public void standbyStateChanged(boolean standby) {
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return null;
	}

}
