package com.alibaba.just.jsdt.editors.contentassist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

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
import org.eclipse.wst.jsdt.core.IJavaScriptElement;
import org.eclipse.wst.jsdt.core.IJavaScriptProject;
import org.eclipse.wst.jsdt.core.IJavaScriptUnit;
import org.eclipse.wst.jsdt.core.IPackageFragment;
import org.eclipse.wst.jsdt.core.IType;
import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.eclipse.wst.jsdt.core.JavaScriptModelException;
import org.eclipse.wst.jsdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposalComputer;

import com.alibaba.just.jsdt.PluginConstants;

public class JSDTJavascriptCompletionProposalComputer implements IJavaCompletionProposalComputer{

	private static final String PROPOSALS_NORMAL_TYPE = ModuleCompletionProposal.PROPOSALS_NORMAL_TYPE;
	private static final String PROPOSALS_NORMAL_TYPE_LV1 = ModuleCompletionProposal.PROPOSALS_NORMAL_TYPE_LV1;

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

	@SuppressWarnings("static-access")
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

		String prefixStr = this.seekForPrefix(document, startOffset);


		//System.out.println("currentText:"+currentText + " prefixStr:"+prefixStr);

		List<String[]> proposals = null;


		//缓存结果
		if(listCache==null){

			proposals = new Vector<String[]>();

			IJavaScriptProject p = JavaScriptCore.create(project);

			/*
            TypeNameMatchRequestor requestor= new TypeNameMatchRequestor(){
                    @Override
                    public void acceptTypeNameMatch(TypeNameMatch match) {
                            try {
                                    //System.out.println("[find]:"+match.getType().getTypes());

                                    Object[] types = match.getType().getChildren();
                                    for(Object t:types){
                                            System.out.println("[TYPE]"+t);
                                    }
                            } catch (JavaScriptModelException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                            }

                    }                        
            };
            IJavaScriptSearchScope searchScope= SearchEngine.createJavaSearchScope(new IJavaScriptElement[] { p });
            SearchEngine engine = new SearchEngine();
            try {
                    engine.searchAllTypeNames(null, 
                                    0, 
                                    "jqueryObject".toCharArray(), 
                                    SearchPattern.R_PREFIX_MATCH, 
                                    IJavaScriptSearchConstants.TYPE, 
                                    searchScope, 
                                    requestor, 
                                    IJavaScriptSearchConstants.FORCE_IMMEDIATE_SEARCH, 
                                    monitor);
            } catch (JavaScriptModelException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
            }*/

			IPackageFragment[] pels = null;
			String name = null;
			try {
				pels = p.getPackageFragments();
				for(IPackageFragment pel:pels){
					IJavaScriptUnit[] uels = pel.getJavaScriptUnits();
					for(IJavaScriptUnit uel:uels){
						IType[] tels = uel.getAllTypes();
						for(IType tel:tels){
							IJavaScriptElement[] jels = tel.getChildren();
							for(IJavaScriptElement jel:jels){
								name = jel.getElementName() + "()";
								proposals.add(new String[]{name,name+" - "+tel.getElementName()});
							}
						}
					}
				}
			} catch (JavaScriptModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			listCache = proposals;
		}else{
			proposals = listCache;//如果有cache取cache
		}

		String[] prop = null;
		boolean isPrefixMatch = false;
		int  prefixLen = 0,replacementOffset=0,replacementLength=0;
		String tmp = null;
		String tmp2 = null;
		int size = 0;
		for(int i = 0,l=proposals.size();i<l;i++ ){
			//限制下一次显示的提示个数
			if(size>=PluginConstants.PROPOSAL_MAX_SIZE){
				break;
			}
			prop = proposals.get(i);
			try{

				tmp =  prop[0].toLowerCase();
				if(prefixStr!=null){
					prefixLen = prefixStr.length();
					tmp2 = (currentText==null ? prefixStr : prefixStr + currentText);
					isPrefixMatch = prefixLen>0 &&  !prop.equals(tmp2) && tmp.indexOf(tmp2.toLowerCase())==0;
				}
				//if(  ( (prefixStr==null || prefixStr.equals("")) && (currentText==null || tmp.indexOf(currentText.toLowerCase())==0) ) || isPrefixMatch){

				if(currentText==null || tmp.indexOf(currentText.toLowerCase())==0 || isPrefixMatch){

					if(isPrefixMatch){
						replacementOffset = startOffset-prefixLen;
						replacementLength = offset-startOffset+prefixLen;
					}else{
						replacementOffset = startOffset;
						replacementLength = offset-startOffset;
					}
					completionProposalList.add(new ModuleCompletionProposal(prop[0], replacementOffset, replacementLength , prop[0].length()
							,null,prop[1],null,null," test2"));
					size++;
				}
			}catch(Exception e){
				e.printStackTrace();

			}
		}

		return completionProposalList;
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
				if(!Character.isJavaIdentifierPart(ch)){
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
		return  Collections.EMPTY_LIST;
	}

	public String getErrorMessage() {
		return null;
	}

	public void sessionEnded() {
		//System.out.println("sessionEnded");
		isStart = false;
		startOffset = -1;
		this.clearCache();
	}

}
