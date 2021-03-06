package com.alibaba.just.ui.editors.contentassist;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.alibaba.just.api.bean.Module;
import com.alibaba.just.ui.util.ImageManager;
import com.alibaba.just.ui.util.PluginResourceUtil;



public class JavascriptContentAssistProcessor implements
IContentAssistProcessor,ICompletionListener {

	private boolean isStart = false;
	private int startOffset = -1;

	private List listCache = null;

	public ICompletionProposal[] computeCompletionProposals(
			ITextViewer viewer, int offset) {

		IDocument document = viewer.getDocument();


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
			return null;
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
			return null;
		}

		//System.out.println("currentText:"+currentText);
		//System.out.println("offset:"+offset);
		List<String> proposals = new Vector<String>() ;

		//缓存结果
		if(listCache==null){

			List<Module> moduleList = PluginResourceUtil.getAllModulesByProject(project);
			
			//转化为proposals
			for (Iterator<Module> iter = moduleList.iterator(); iter.hasNext();) {
				proposals.add(iter.next().getName());
			}

			listCache = proposals;
		}else{
			proposals = listCache;//如果有cache取cache
		}


		List<ICompletionProposal> completionProposalList = new ArrayList<ICompletionProposal>();

		String prop = null;
		for(int i = 0,l=proposals.size();i<l;i++ ){
			prop = (String) proposals.get(i);
			if(currentText==null || prop.indexOf(currentText)==0){
				completionProposalList.add(new CompletionProposal(prop, startOffset, offset-startOffset , prop.length()
						,ImageManager.getImage(ImageManager.IMG_MODULE_ICON),null,null,null));
			}
		}


		return completionProposalList.toArray(new ICompletionProposal[0]);
	}

	public IContextInformation[] computeContextInformation(
			ITextViewer viewer, int offset) {
		return null;
	}

	public char[] getCompletionProposalAutoActivationCharacters() {
		// TODO Auto-generated method stub
		//return new char[] { '.', '/' };
		return null;
	}

	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	public IContextInformationValidator getContextInformationValidator() {
		// TODO Auto-generated method stub
		return null;
	}

	public void assistSessionStarted(ContentAssistEvent event) {
		//System.out.println("assistSessionStarted");
		isStart = true;
		this.clearCache();
	}

	private void clearCache(){
		if(listCache!=null){
			listCache.clear();
			listCache=null;
		}
	}

	public void assistSessionEnded(ContentAssistEvent event) {
		//System.out.println("assistSessionEnded");
		isStart = false;
		startOffset = -1;
		this.clearCache();
	}

	public void selectionChanged(ICompletionProposal proposal,
			boolean smartToggle) {
		// TODO Auto-generated method stub

	}

}
