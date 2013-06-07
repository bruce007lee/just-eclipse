package com.alibaba.just.ui.popup.actions;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.FileEditorInput;

import com.alibaba.just.PluginConstants;
import com.alibaba.just.api.bean.Module;
import com.alibaba.just.api.parser.ModuleParser;
import com.alibaba.just.template.ModuleTemplate;
import com.alibaba.just.ui.dialogs.ListSelectDialog;
import com.alibaba.just.ui.templates.TemplateConstants;
import com.alibaba.just.ui.templates.TemplateManager;
import com.alibaba.just.ui.util.PreferenceUtil;
import com.alibaba.just.ui.util.UIUtil;
import com.alibaba.just.util.FileUtil;

/**
 * @author bruce.liz
 */
public class ImportModulesViewAction implements IEditorActionDelegate,IObjectActionDelegate{
	protected static final String VALID_MODULE_EXT = "js";
	protected static final String MODULE_LIBS_CONTENT_REGEX = "/\\*@module_merge_start\\*/([\\s\\S]*)/\\*@module_merge_end\\*/";


	protected IEditorPart targetEditor = null;


	/**
	 * Constructor for Action1.
	 */
	public ImportModulesViewAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if(targetEditor==null){
			return;
		}

		IEditorInput input = targetEditor.getEditorInput();

		if(FileEditorInput.class.isInstance(input)){
			Shell shell = UIUtil.getShell();
			FileEditorInput fileInput = (FileEditorInput)input;
			try {
				IFile ifile = fileInput.getFile();
				String path = ifile.getLocation().toFile().getAbsolutePath();
				if(!VALID_MODULE_EXT.equals(ifile.getFileExtension())){
					return;
				}

				ModuleParser parser = new ModuleParser(PreferenceUtil.getFileCharset());
				Module module = parser.getModule(path,ModuleParser.MODULE_TYPE_ALL);

				if(module==null){
					MessageDialog.openInformation(
							shell,
							"Warnning",
					"Invalid module file!");
					return;
				}

				List<Module> allModule = new ArrayList<Module>();


				String libsStr = ifile.getProject().getPersistentProperty(
						new QualifiedName(PluginConstants.QUALIFIED_NAME, PluginConstants.LIBS_PROPERTY_KEY));

				if(libsStr!=null){

					String[] libs = libsStr.split("[\n]");

					for(String lib:libs){
						String lb = lib.trim();
						if(lb.length()>0){

							String folderPath = null;

							if(lb.indexOf("@")==0){
								File f = null;								
								IPath ipath = ResourcesPlugin.getWorkspace().getRoot().getLocation();								
								if(ipath!=null){
									f = ipath.toFile();
								}

								f = new File(f.getAbsolutePath()+lb.substring(1));
								if(f.exists() && f.isDirectory()){
									folderPath = f.getAbsolutePath();
								}

							}else{

								File f = new File(lb);
								if(f.exists() && f.isDirectory()){
									folderPath = f.getAbsolutePath();
								}
							}

							if(folderPath==null){
								continue;
							}

							allModule.addAll(parser.getAllModules(folderPath));

						}
					}

					List<Module> requires =  parser.getAllRequiredModules(module, allModule);

					//add self
					requires.add(module);

					//IFile file = ifile.getParent().getFile(new Path("merge.js"));

					List<IFile>  fList = this.getMergeFileList(ifile.getParent());

					if(fList.size()==0){
						showCreateMergeFileDlg(shell,ifile.getParent(), requires);
					}else if(fList.size()==1){
						createMergeFile(fList.get(0), requires);
					}else{
						showSelectMergeFileDlg(shell,fList,requires);
					}

					//this.createMergeFile(file, requires);
				}

			} catch (Exception e) {
				String error = e.getMessage();
				if(error==null || error.trim().length()==0){
					error = e.toString();
				}
				MessageDialog.openInformation(
						shell,
						"Error",
						error);  
			} 
		}
	}

	protected void showSelectMergeFileDlg(Shell shell,final List<IFile> mergeFileList,final List<Module> requires){

		List<String> fileNames = new ArrayList<String>();

		for(IFile f:mergeFileList){
			fileNames.add(f.getName());
		}
		ListSelectDialog selectDlg =  new ListSelectDialog(shell,"Select merge file","Please select a merge file:",fileNames){
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.jface.dialogs.InputDialog#buttonPressed(int)
			 */
			protected void buttonPressed(int buttonId) {
				super.buttonPressed(buttonId);
				if (buttonId == IDialogConstants.OK_ID) {
					String name = this.getValue();
					if(name==null){
						name="";
					}
					if(name.length()>0){
						IFile file = null;
						for(IFile f:mergeFileList){
							if(f.getName().equals(name)){
								file = f;
								break;
							}
						}

						if(file!=null){
							ImportModulesViewAction.this.createMergeFile(file, requires);
						}else{
							return;
						}
					}else{
						return;
					}
				}
				this.close();
			}
		};

		selectDlg.open();
	}

	protected void showCreateMergeFileDlg(Shell shell,final IContainer parent,final List<Module> requires){
		InputDialog mergefileDlg = new InputDialog(shell,
				"Create merge file",
				"Please enter a merge file name:",
				"merge.js",
				new IInputValidator(){
			public String isValid(String newText) {
				if(!newText.matches("[\\w-]+.js")){
					return "please enter a valid file name!";
				}
				return null;
			}
		}){

			/*
			 * (non-Javadoc)
			 * @see org.eclipse.jface.dialogs.InputDialog#buttonPressed(int)
			 */
			protected void buttonPressed(int buttonId) {
				super.buttonPressed(buttonId);
				if (buttonId == IDialogConstants.OK_ID) {
					String name = this.getValue();
					if(name==null){
						name="";
					}
					name = name.trim();
					if(name.length()>0){
						IFile file = parent.getFile(new Path(name));
						ImportModulesViewAction.this.createMergeFile(file, requires);
					}else{
						return;
					}
				}
				this.close();
			}

		};
		mergefileDlg.open();
	}

	private boolean isMergeFile(String content){
		if(content==null){
			return false;
		}
		Pattern p = Pattern.compile(MODULE_LIBS_CONTENT_REGEX,Pattern.MULTILINE);
		Matcher m = p.matcher(content);
		if (m.find()) {
			return true;
		}

		return false;
	}

	protected List<IFile> getMergeFileList(IContainer folder) throws Exception{
		List<IFile> list = new ArrayList<IFile>();
		try {
			IResource[] resources = folder.members();
			IFile ifile = null;
			String content = null;
			for(IResource res:resources){
				if(IFile.class.isInstance(res)){
					ifile = (IFile)res;
					content = FileUtil.getFileContent(ifile.getContents(), ifile.getCharset());
					if(isMergeFile(content)){
						list.add(ifile);
					}
				}
			}
		} catch (Exception e) {
			list.clear();
			throw e;
		}
		return list;
	}

	private void sortModule(List<Module> moduleList){
		Collections.sort(moduleList, new Comparator<Module>(){
			public int compare(Module o1, Module o2) {
				if(o1.getName()!=null && o2.getName()!=null){
					return o1.getName().compareTo(o2.getName());
				}
				if(o1.getName()==null){
					return 1;
				}
				return 0;
			}

		});
	}

	private String getTemplate(String templateId) throws Exception{
		return TemplateManager.getTemplate(templateId);
	}

	protected void createMergeFile(IFile ifile , List<Module> requires){
		try {

			//sort
			this.sortModule(requires);

			ModuleTemplate  mtpl = new ModuleTemplate();
			mtpl.setTpl(getTemplate(TemplateConstants.TPL_MERGE_CONTENT));
			mtpl.setRequiredModules(requires);

			String libsStr = mtpl.getParseContent();
			libsStr = "/*@module_merge_start*/\n" + libsStr + "/*@module_merge_end*/" ;
			mtpl.destroy();
			if (ifile.exists()) {
				//如果存在直接merge
				String ori = FileUtil.getFileContent(ifile.getContents(),ifile.getCharset());
				ori = ori.replaceAll(MODULE_LIBS_CONTENT_REGEX, libsStr);

				InputStream stream = new ByteArrayInputStream(ori.getBytes());
				ifile.setContents(stream, true, true, null);
			} else {
				//如果不存在通过模板创建merge文件
				String tpl = this.getTemplate(TemplateConstants.TPL_MERGE_FILE);
				ModuleTemplate mtp = new ModuleTemplate();
				mtp.setTpl(tpl);
				tpl = mtp.getParseContent();
				mtp.destroy();
				tpl = tpl.replaceAll(MODULE_LIBS_CONTENT_REGEX, libsStr);
				InputStream stream = new ByteArrayInputStream(tpl.getBytes());
				ifile.create(stream, true, null);
			}

		} catch (Exception e) {
			String error = e.getMessage();
			if(error==null || error.trim().length()==0){
				error = e.toString();
			}
			MessageDialog.openError(
					UIUtil.getShell(),
					"Error",
					error);  
		}
	}


	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {

	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		this.targetEditor = targetEditor;
	}

}
