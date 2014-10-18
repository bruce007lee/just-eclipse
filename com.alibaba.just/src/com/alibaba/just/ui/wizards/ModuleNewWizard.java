package com.alibaba.just.ui.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.alibaba.just.api.bean.Module;
import com.alibaba.just.template.ModuleTemplate;
import com.alibaba.just.ui.templates.TemplateConstants;
import com.alibaba.just.ui.templates.TemplateManager;
import com.alibaba.just.ui.util.LogUtil;
import com.alibaba.just.ui.util.PreferenceUtil;

/**
 * wizard for searching exist modules
 */

public class ModuleNewWizard extends Wizard implements INewWizard {
	private ModuleNewWizardPage page;
	private ISelection selection;

	/**
	 * Constructor for ModuleNewWizard.
	 */
	public ModuleNewWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page = new ModuleNewWizardPage(selection);
		addPage(page);
	}

	public void dispose() {
		page.dispose();
		this.page = null;
		this.selection=null;
		super.dispose();
	}

	/**
	 * After finish click
	 */
	public boolean performFinish() {
		final String containerName = page.getContainerName();
		final String fileName = page.getFileName();
		final String moduleName = page.getModuleName();
		final java.util.List<Module>  moduleList = page.getRequiredModules();
		final boolean isAnonymous = page.isAnonymous();


		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		IProject project = resource.getProject();

		if(!project.exists()){
			MessageDialog.openError(this.getShell(), "Error", "The defined project not exist,please select a valiable path!");
			return false;
		}


		IContainer container = (IContainer) resource;
		IFile file = container.getFile(new Path(fileName));

		if(file.exists()){
			boolean isOverWrite = MessageDialog.openConfirm(this.getShell(), "Confirm", "File already exist,are you want to overwrite it?");

			if(!isOverWrite){
				return false;
			}
		}

		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(containerName, fileName,moduleName,isAnonymous,moduleList, monitor);
				} catch (Exception e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(false, true, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			
			LogUtil.error(e);
			
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * After finish click
	 * @param containerName
	 * @param fileName
	 * @param moduleName
	 * @param isAnonymous
	 * @param moduleList
	 * @param monitor
	 * @throws Exception
	 */
	private void doFinish(
			String containerName,
			String fileName,
			String moduleName,
			boolean isAnonymous,
			java.util.List<Module> moduleList,
			IProgressMonitor monitor)
	throws Exception {
		// create a sample file
		monitor.beginTask("Creating " + fileName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(fileName));
		try {
			InputStream stream = openContentStream(moduleName,isAnonymous,moduleList);
			if (file.exists()) {
				file.setContents(stream, true, true, monitor);
			} else {
				//create folder
				IFolder parent = file.getParent().getFolder(null);
				if(!parent.exists()){
					createFolder(parent, monitor);
				}
				//create file
				file.create(stream, true, monitor);
			}
			stream.close();
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;
		}
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page =
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, file, true);
				} catch (PartInitException e) {
				}
			}
		});
		monitor.worked(1);
	}

	private void createFolder(IFolder folder,IProgressMonitor monitor) throws Exception{
		IContainer parentCt = folder.getParent();		
		if(!(parentCt instanceof IProject)){
			IFolder parent = parentCt.getFolder(null);
			if(!parent.exists()){
				createFolder(parent,monitor);
			}
		}
		if(!folder.exists()){
			folder.create(true, true, monitor);
		}
	}

	private String getTemplate(String templateId) throws Exception{
		return TemplateManager.getTemplate(templateId);
	}

	/**
	 * We will initialize file contents with a sample text.
	 */
	private InputStream openContentStream(String moduleName,boolean isAnonymous,java.util.List<Module> moduleList) {
		//String contents =
		//	"This is the initial file contents for *.js file that should be word-sorted in the Preview page of the multi-page editor";

		byte[] b=null;
		try {
			ModuleTemplate tpl = new ModuleTemplate();
			tpl.setModuleName(moduleName);
			tpl.setRequiredModules(moduleList);

			String charset = PreferenceUtil.getFileCharset();

			//String filePath = "E:/eclipse_3.6/workspace/com.alibaba.just/tpl/module.tpl";
			//String tplStr = FileUtil.getFileContent(new File(filePath),charset);

			String tplStr = null;
			if(isAnonymous){
				tplStr = getTemplate(TemplateConstants.TPL_ANONYMOUS_MODULE);
			}else{
				tplStr = getTemplate(TemplateConstants.TPL_NORMAL_MODULE);
			}

			tpl.setTpl(tplStr);
			b = tpl.getParseContent().getBytes(charset);
			tpl.destroy();
		} catch (Exception e) {
			LogUtil.error(e);
			String error = e.getMessage();
			if(error==null || error.trim().length()==0){
				error = e.toString();
			}
			MessageDialog.openError(
					this.getShell(),
					"Error",
					error);  
			b=new byte[0];
		} 

		return new ByteArrayInputStream(b);
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status =
			new Status(IStatus.ERROR, "justeclipse", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}