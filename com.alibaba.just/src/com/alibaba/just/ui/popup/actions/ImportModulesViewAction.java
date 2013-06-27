package com.alibaba.just.ui.popup.actions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
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

import com.alibaba.just.api.bean.Module;
import com.alibaba.just.api.parser.ModuleParser;
import com.alibaba.just.api.parser.ParserFactory;
import com.alibaba.just.template.ModuleTemplate;
import com.alibaba.just.ui.dialogs.ListSelectDialog;
import com.alibaba.just.ui.templates.TemplateConstants;
import com.alibaba.just.ui.templates.TemplateManager;
import com.alibaba.just.ui.util.PluginResourceUtil;
import com.alibaba.just.ui.util.PreferenceUtil;
import com.alibaba.just.ui.util.UIUtil;
import com.alibaba.just.util.FileUtil;

/**
 * @author bruce.liz
 */
public class ImportModulesViewAction implements IEditorActionDelegate,IObjectActionDelegate{
	protected static final String VALID_MODULE_EXT = "js";
	protected static final String MODULE_LIBS_CONTENT_REGEX = "/\\*@module_merge_start\\*/([\\s\\S]*)/\\*@module_merge_end\\*/";
	protected static final String SELECT_FOLDER_FILE_REGEX = "/\\*@module_main_start\\*/([\\s\\S]*)/\\*@module_main_end\\*/";
	protected static final String NL = "\n";

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
				if(!VALID_MODULE_EXT.equalsIgnoreCase(ifile.getFileExtension())){
					return;
				}

				ModuleParser parser = ParserFactory.getModuleParser(PreferenceUtil.getFileCharset());
				parser.setThreadPool(UIUtil.getThreadPool());
				Module module = parser.getModule(path,ModuleParser.MODULE_TYPE_ALL);

				if(module==null){
					MessageDialog.openInformation(
							shell,
							"Warnning",
					"Invalid module file!");
					return;
				}

				List<Module> moduleList = PluginResourceUtil.getAllModulesByProject(ifile.getProject());	
				List<Module> requires =  parser.getAllRequiredModules(module, moduleList);

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
		showSelectMergeFileDlg(shell,mergeFileList,requires,null);
	}

	protected void showSelectMergeFileDlg(Shell shell,final List<IFile> mergeFileList,final List<Module> requires,final List<String> mainFiles){

		List<String> fileNames = new ArrayList<String>();

		for(IFile f:mergeFileList){
			fileNames.add(f.getName());
		}
		ListSelectDialog selectDlg =  new ListSelectDialog(shell,"Select merge file","Please select a merge file:",fileNames){
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
			 */
			protected void okPressed() {
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
						ImportModulesViewAction.this.createMergeFile(file, requires,mainFiles);
					}else{
						return;
					}
				}else{
					return;
				}

				super.okPressed();
			}
		};

		selectDlg.open();
	}

	protected void showCreateMergeFileDlg(final Shell shell,final IContainer parent,final List<Module> requires){
		showCreateMergeFileDlg(shell,parent,requires,null);
	}

	protected void showCreateMergeFileDlg(final Shell shell,final IContainer parent,final List<Module> requires,final List<String> mainFiles){
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
			 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
			 */
			protected void okPressed() {
				String name = this.getValue();
				if(name==null){
					name="";
				}
				name = name.trim();
				if(name.length()>0){
					IFile file = parent.getFile(new Path(name));
					ImportModulesViewAction.this.createMergeFile(file, requires, mainFiles);
				}else{
					return;
				}
				super.okPressed();
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

	private boolean hasMainFiles(String content){
		if(content==null){
			return false;
		}
		Pattern p = Pattern.compile(SELECT_FOLDER_FILE_REGEX,Pattern.MULTILINE);
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
		createMergeFile(ifile,requires,null);
	}

	private String appendMainFiles(String tpl,List<String> mainFiles){
		String mainStr = null;
		if(mainFiles!=null){
			StringBuffer sb = new StringBuffer();
			for(String fstr : mainFiles){
				sb.append("/* "+fstr+" */\n");
			}
			mainStr = sb.toString();
		}

		if(mainStr!=null){
			if(hasMainFiles(tpl)){
				mainStr ="/*@module_main_start*/" + NL + mainStr + "/*@module_main_end*/";
				tpl = tpl.replaceAll(SELECT_FOLDER_FILE_REGEX, Matcher.quoteReplacement(mainStr));
			}else{
				mainStr =NL + "/*@module_main_start*/" + NL + mainStr + "/*@module_main_end*/\n" + NL;
				String flag = "/*@module_merge_start*/";
				int index = tpl.indexOf(flag);
				if(index>=0){
					tpl = tpl.substring(0,index) + mainStr + tpl.substring(index,tpl.length());
				}else{
					tpl = tpl + mainStr;
				}
			}
		}

		return tpl;
	}

	protected void createMergeFile(IFile ifile , List<Module> requires , List<String> mainFiles){
		try {

			//sort
			this.sortModule(requires);

			ModuleTemplate  mtpl = new ModuleTemplate();
			mtpl.setTpl(getTemplate(TemplateConstants.TPL_MERGE_CONTENT));
			mtpl.setRequiredModules(requires);

			Map map = new HashMap();
			String path = null;
			IPath p = PreferenceUtil.getCurrentRootPath(ifile.getParent());
			if(p!=null){
				path = p.toString();
			}
			map.put("rootPath", path);
			String libsStr = mtpl.getParseContent(map);
			libsStr = "/*@module_merge_start*/\n" + libsStr + "/*@module_merge_end*/" ;

			mtpl.destroy();
			if (ifile.exists()) {
				//如果存在直接merge
				String ori = FileUtil.getFileContent(ifile.getContents(),ifile.getCharset());
				ori = ori.replaceAll(MODULE_LIBS_CONTENT_REGEX,Matcher.quoteReplacement(libsStr));				
				ori = this.appendMainFiles(ori, mainFiles);
				InputStream stream = new ByteArrayInputStream(ori.getBytes());
				ifile.setContents(stream, true, true, null);
			} else {
				//如果不存在通过模板创建merge文件
				String tpl = this.getTemplate(TemplateConstants.TPL_MERGE_FILE);
				ModuleTemplate mtp = new ModuleTemplate();
				mtp.setTpl(tpl);
				tpl = mtp.getParseContent();
				mtp.destroy();
				tpl = tpl.replaceAll(MODULE_LIBS_CONTENT_REGEX, Matcher.quoteReplacement(libsStr));
				tpl = this.appendMainFiles(tpl, mainFiles);
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
