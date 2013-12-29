package com.alibaba.just.jsdt.editors.contentassist;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.jsdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposalComputer;

import com.alibaba.just.api.bean.Module;
import com.alibaba.just.jsdt.util.ImageManager;
import com.alibaba.just.ui.util.PluginResourceUtil;
import com.alibaba.just.ui.util.PreferenceUtil;

public class ModuleCompletionProposalComputer implements IJavaCompletionProposalComputer{

	private static final String WORD_DELIMITER = "\"\'\t\n\r ";

	private static final String PROPOSALS_ALIAS_TYPE = ModuleCompletionProposal.PROPOSALS_ALIAS_TYPE;
	private static final String PROPOSALS_NORMAL_TYPE = ModuleCompletionProposal.PROPOSALS_NORMAL_TYPE;
	private static final String PROPOSALS_ALIAS_TYPE_LV1 = ModuleCompletionProposal.PROPOSALS_ALIAS_TYPE_LV1;
	private static final String PROPOSALS_NORMAL_TYPE_LV1 = ModuleCompletionProposal.PROPOSALS_NORMAL_TYPE_LV1;
	private static final String PROPOSALS_PACKAGE_TYPE =  ModuleCompletionProposal.PROPOSALS_PACKAGE_TYPE;

	private boolean isStart = false;
	private int startOffset = -1;

	private List<String[]> listCache = null;

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
		IFile ifile = null;

		try{

			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if(window!=null){
				IWorkbenchPage page = window.getActivePage();
				if(page!=null){
					IEditorPart editor  = page.getActiveEditor();
					if(editor!=null){
						IEditorInput input = editor.getEditorInput();
						if (input!=null && input instanceof IFileEditorInput){
							ifile =   ((IFileEditorInput) input).getFile();
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

		String prefixStr = PreferenceUtil.isShowMatchStart()?this.seekForPrefix(document, startOffset):null;		
		
		//System.out.println("currentText:"+currentText);
		//System.out.println("offset:"+offset);
		List<String[]> proposals = new Vector<String[]>();

		//缓存结果
		if(listCache==null){

			//目前path路径提示
			if(ifile!=null){
				IContainer  p = ifile.getParent();
				IContainer root = PreferenceUtil.getCurrentRoot(p);
				if(root!=null && root.getFullPath()!=null){
					String mpath = root.getFullPath().toString();
					mpath = PreferenceUtil.getRelativeRootPath(p.getFullPath().toString(),mpath);
					if(mpath!=null){
						if(mpath.length()>0 && mpath.indexOf('/')==0){
							mpath = mpath.substring(1);
						}
						proposals.add(new String[]{mpath,PROPOSALS_PACKAGE_TYPE});
					}
				}
			}

			List<Module> moduleList = PluginResourceUtil.getAllModulesByProject(project);

			//转化为proposals
			Module tmp = null;
			for (Iterator<Module> iter = moduleList.iterator(); iter.hasNext();) {
				tmp = iter.next();
				proposals.add(new String[]{tmp.getName(),PROPOSALS_NORMAL_TYPE});
				if(tmp.getAlias()!=null){
					proposals.add(new String[]{tmp.getAlias(),PROPOSALS_ALIAS_TYPE});
				}
			}

			listCache = proposals;
		}else{
			proposals = listCache;//如果有cache取cache
		}

		String[] prop = null;
		boolean isPrefixMatch = false;
		int  prefixLen = 0,replacementOffset=0,replacementLength=0;
		String tmp = null;
		for(int i = 0,l=proposals.size();i<l;i++ ){
			prop = proposals.get(i);
			try{
				
				tmp =  prop[0].toLowerCase();
				if(prefixStr!=null){
					prefixLen = prefixStr.length();
					isPrefixMatch = prefixLen>0 &&  tmp.indexOf((currentText==null ? prefixStr : prefixStr + currentText).toLowerCase())>=0;
				}

				if(currentText==null || isMatch(tmp,currentText) || isPrefixMatch){
					
					if(isPrefixMatch){
						replacementOffset = startOffset-prefixLen;
						replacementLength = offset-startOffset+prefixLen;
					}else{
						replacementOffset = startOffset;
						replacementLength = offset-startOffset;
					}
					
					if(prop[1]!=null && prop[1].equals(PROPOSALS_ALIAS_TYPE)){
						completionProposalList.add(new ModuleCompletionProposal(prop[0], replacementOffset, replacementLength , prop[0].length()
								,ImageManager.getImage(ImageManager.IMG_ALIAS_MODULE_ICON),isPrefixMatch?PROPOSALS_ALIAS_TYPE_LV1:prop[1]));
					}else if(prop[1]!=null && prop[1].equals(PROPOSALS_PACKAGE_TYPE)){
						completionProposalList.add(new ModuleCompletionProposal(prop[0], replacementOffset, replacementLength , prop[0].length()
								,ImageManager.getImage(ImageManager.IMG_PACKAGE_OBJ),prop[1]));
					}else{
						completionProposalList.add(new ModuleCompletionProposal(prop[0], replacementOffset, replacementLength, prop[0].length()
								,ImageManager.getImage(ImageManager.IMG_MODULE_ICON),isPrefixMatch?PROPOSALS_NORMAL_TYPE_LV1:prop[1]));
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		return completionProposalList;
	}
	
	/**
	 * 
	 * @param tmp
	 * @param currentText
	 * @return
	 */
	private boolean isMatch(String tmp, String currentText){
		return PreferenceUtil.isShowMatchPartial()?tmp.indexOf(currentText.toLowerCase())>=0:tmp.indexOf(currentText.toLowerCase())==0;
	}

	/**
	 * 
	 * @param document
	 * @param endPos
	 * @return
	 */
	private String seekForPrefix(IDocument document,int endPos){
		int pos = endPos-1;
		char ch;
		while(pos>=0){
			try {
				ch = document.getChar(pos);
				if(WORD_DELIMITER.indexOf(ch)>=0){
					return document.get(pos+1,endPos-pos-1);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			pos--;
		}

		return null;
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
