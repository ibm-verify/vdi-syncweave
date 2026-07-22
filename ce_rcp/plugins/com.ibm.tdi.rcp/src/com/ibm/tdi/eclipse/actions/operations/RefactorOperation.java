/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions.operations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.log.EclipseAppender;

/**
 * Changes inheritance for a list of components after a rename operation.
 */
public class RefactorOperation extends AbstractOperation {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private IFile oldReference;
	private IFile newReference;
	private List<IFile> components;
	private boolean hasRefactored = false;
	private ArrayList<IFile> errorFiles;
	private static DocumentBuilder docbuilder = null;

	private String oldName;
	private String folder;
	private String oldRef;
	private String newName;
	private String newRef;
	
	public RefactorOperation(String label, IFile oldReference, IFile newReference, List<IFile> components) throws Exception {
		super(label);
		this.oldReference = oldReference;
		this.newReference = newReference;
		this.components = components;
		if (docbuilder == null)
			docbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	}

	@Override
	public boolean canUndo() {
		return hasRefactored;
	}

	@Override
	public boolean canExecute() {
		return !hasRefactored;
	}

	@Override
	public boolean canRedo() {
		return canExecute();
	}
	
	private IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	public IStatus execute(IProgressMonitor monitor, final IAdaptable uiInfo) throws ExecutionException {
		try {
			getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					doExecute(monitor, uiInfo);
				}
			}, getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, monitor);
		} catch (final CoreException e) {
			throw new ExecutionException(e.getLocalizedMessage(), e);
		}
		return Status.OK_STATUS;
	}

	public IStatus redo(IProgressMonitor monitor, final IAdaptable uiInfo) throws ExecutionException {
		IFile tmp = oldReference;
		oldReference = newReference;
		newReference = tmp;
		return execute(monitor, uiInfo);
	}
	
	public IStatus undo(IProgressMonitor monitor, final IAdaptable uiInfo) throws ExecutionException {
		try {
			getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					doUndo(monitor, uiInfo);
				}
			}, getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, monitor);
		} catch (final CoreException e) {
			throw new ExecutionException(e.getLocalizedMessage(), e);
		}
		return Status.OK_STATUS;
	}
	
	public void doExecute(IProgressMonitor monitor, IAdaptable info) throws CoreException {

		oldName = oldReference.getName().substring(0, oldReference.getName().lastIndexOf("."));
		folder = TDIConfigurationFile.getFolderForExtension(oldReference.getFileExtension());
		oldRef = "/" + folder + "/" + oldName;

		newName = newReference.getName().substring(0, newReference.getName().lastIndexOf("."));
		newRef = "/" + folder + "/" + newName;

		//
		// First rename the file
		//
		oldReference.move(newReference.getFullPath(), IResource.SHALLOW|IResource.KEEP_HISTORY, monitor);

		errorFiles = new ArrayList<IFile>();

		//
		// Update the XML DOM structure directly to avoid annoying
		// "Cannot setup inheritance ..." error messages
		//
		for (IFile file : components) {
			changeInFile(file);
		}

		hasRefactored = !hasRefactored;

		if (errorFiles.size() > 0)
			throw new CoreException(EclipseAppender.statusException(new Exception(
					Messages.getString("RefactorOperation.not.refactored"))));
	}

	
	private void changeInFile(IFile file) {
		try {
			docbuilder.reset();
			Document xmldoc = docbuilder.parse(file.getContents());

			XPath xpath = XPathFactory.newInstance().newXPath();
			NodeList nodes = (NodeList) xpath.evaluate("//InheritFrom", xmldoc, XPathConstants.NODESET);
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node instanceof Element) {
					Element elem = (Element) node;
					String cur = elem.getTextContent();
					if (oldRef.equals(cur)) {
						elem.setTextContent(newRef);
					}
				}
			}

			if (MetamergeConfig.DEFAULT_SCRIPT_FOLDER.equals(folder)) {
				Node node = (Node) xpath.evaluate("//parameter[@name='includePrologs']", xmldoc, XPathConstants.NODE);
				if (node instanceof Element) {
					Element elem = (Element) node;
					String cur = elem.getTextContent();
					if (cur != null && cur.length() > 0) {
						elem.setTextContent(fixIncludeProlog(cur));
					}
				}				
			}

			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			t.transform(new DOMSource(xmldoc.getDocumentElement()), new StreamResult(bos));
			file.setContents(new ByteArrayInputStream(bos.toByteArray()), 0, null);

		} catch (Exception e) {
			EclipseAppender.logerror(e.getLocalizedMessage(), e);
			errorFiles.add(file);
		}
	}

	private String fixIncludeProlog(String cur) {
		StringBuilder buf = new StringBuilder();
		for (String s : cur.split("\n")) {
			if (oldName.equals(s.trim()))
				buf.append(newName);
			else
				buf.append(s);
			buf.append("\n");
		}
		return buf.toString();
	}

	public void doUndo(IProgressMonitor monitor, IAdaptable info) throws CoreException {
		IFile tmp = oldReference;
		oldReference = newReference;
		newReference = tmp;
		doExecute(monitor, info);
	}

	/**
	 * Returns true if the file contains InheritFrom references to
	 * <i>reference</i>.
	 * 
	 * @param source
	 * @param reference
	 * @return
	 * @throws Exception
	 */
	public static boolean hasReferenceTo(IFile source, String reference, String scriptRef) throws Exception {
		if (docbuilder == null)
			docbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

		docbuilder.reset();

		InputStream input = source.getContents();

		try {
			Document xmldoc = docbuilder.parse(input);

			XPath xpath = XPathFactory.newInstance().newXPath();
			NodeList nodes = (NodeList) xpath.evaluate("//InheritFrom", xmldoc, XPathConstants.NODESET);
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node instanceof Element) {
					Element elem = (Element) node;
					String cur = elem.getTextContent();
					if (reference.equals(cur)) {
						return true;
					}
				}
			}
			if (scriptRef != null) {
				Node node = (Node) xpath.evaluate("//parameter[@name='includePrologs']", xmldoc, XPathConstants.NODE);
				if (node instanceof Element) {
					Element elem = (Element) node;
					String cur = elem.getTextContent();
					if (cur != null && cur.length() > 0) {
						for (String s : cur.split("\n")) {
							if (scriptRef.equals(s.trim()))
								return true;
						}
					}
				}
			}
		} finally {
			if (input != null)
				input.close();
		}

		return false;
	}
}
