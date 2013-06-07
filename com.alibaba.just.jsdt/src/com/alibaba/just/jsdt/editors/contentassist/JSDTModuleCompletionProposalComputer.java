package com.alibaba.just.jsdt.editors.contentassist;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.jsdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposalComputer;

import com.alibaba.just.PluginConstants;
import com.alibaba.just.api.bean.Module;
import com.alibaba.just.api.parser.ModuleParser;
import com.alibaba.just.jsdt.util.ImageManager;
import com.alibaba.just.ui.util.PluginResourceUtil;
import com.alibaba.just.ui.util.PreferenceUtil;

public class JSDTModuleCompletionProposalComputer implements IJavaCompletionProposalComputer{

	private static final String SEP_REG = "[\n]";

	private boolean isStart = false;
	private int startOffset = -1;

	private List<String> listCache = null;

	public void sessionStarted() {
		//System.out.println("sessionStarted");
		isStart = true;
		this.clearCache();
	}

	private void clearCache(){
		if(listCache!=null){
			listCache.clear();
			listCache=null;
		}
	}

	public List computeCompletionProposals(
			ContentAssistInvocationContext context, IProgressMonitor monitor) {

		IDocument document = context.getDocument();
		int offset = context.getInvocationOffset();

		List<ICompletionProposal> completionProposalList = new ArrayList<ICompletionProposal>();


		IProject project = null;

		try{

			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if(window!=null){
				IWorkbenchPage page = window.getActivePage();
				if(page!=null){
					IEditorPart editor  = page.getActiveEditor();
					if(editor!=null){
						IEditorInput input = editor.getEditorInput();
						if (input!=null && input instanceof IFileEditorInput){
							IFile ifile =   ((IFileEditorInput) input).getFile();
							if(ifile!=null){
								project = ifile.getProject();
							}
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		if(project==null){
			return completionProposalList;
		}

		if(isStart){
			startOffset = offset;
			isStart = false;
		}


		String currentText = null;
		if(startOffset>0 && (offset - startOffset>0)){
			try {
				currentText = document.get(startOffset, offset - startOffset);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}else if(offset - startOffset<0){
			return completionProposalList;
		}

		//System.out.println("currentText:"+currentText);
		//System.out.println("offset:"+offset);
		List<String> proposals = new Vector<String>() ;

		//缓存结果
		if(listCache==null){

			List<String> libs = PreferenceUtil.getProjectLibsList(project);	
			List<Module> moduleList = new ArrayList<Module>();
			ModuleParser parser = new ModuleParser(PreferenceUtil.getFileCharset());
			IWorkspaceRoot  wRoot = ResourcesPlugin.getWorkspace().getRoot();
			for(String lib:libs){
				String lb = lib.trim();
				if(lb.length()>0){

					String folderPath = null;
					String type = PreferenceUtil.getProjectLibType(lb);
					if(PreferenceUtil.LIB_TYPE_WORKSPACE_FOLDER.equals(type) ||
							PreferenceUtil.LIB_TYPE_SELF.equals(type)){
						if(PreferenceUtil.LIB_TYPE_SELF.equals(type)){
							lb = project.getFullPath().toString();
						}else{
							lb = PreferenceUtil.getProjectLibPath(lb);
						}
						IPath rootPath = wRoot.getFullPath();
						IResource res = wRoot.findMember(rootPath.append(lb));
						if(res!=null && res.isAccessible()){
							try{
								PluginResourceUtil.getModulesByResource(res,moduleList,parser);
							}catch(Exception e){
								e.printStackTrace();
							}
						}

					}else if(PreferenceUtil.LIB_TYPE_EXTERNAL_FOLDER.equals(type)){
						lb = PreferenceUtil.getProjectLibPath(lb);
						File f = new File(lb);
						if(f.exists() && f.isDirectory()){
							folderPath = f.getAbsolutePath();
							moduleList.addAll(parser.getAllModules(folderPath));
						}
					}

				}
			}

			//转化为proposals
			for (Iterator<Module> iter = moduleList.iterator(); iter.hasNext();) {
				proposals.add(iter.next().getName());
			}

			listCache = proposals;
		}else{
			proposals = listCache;//如果有cache取cache
		}

		String prop = null;
		for(int i = 0,l=proposals.size();i<l;i++ ){
			prop = proposals.get(i);
			if(currentText==null || prop.indexOf(currentText)==0){
				completionProposalList.add(new CompletionProposal(prop, startOffset, offset-startOffset , prop.length()
						,ImageManager.getImage(ImageManager.IMG_MODULE_ICON),null,null,null));
			}
		}

		return completionProposalList;
	}

	public List computeContextInformation(
			ContentAssistInvocationContext context, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	public void sessionEnded() {
		//System.out.println("sessionEnded");
		isStart = false;
		startOffset = -1;
		this.clearCache();
	}

}
