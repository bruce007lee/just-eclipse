package com.alibaba.just.jsdt.editors.contentassist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.wst.jsdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposalComputer;

public class WordsCompletionProposalComputer implements IJavaCompletionProposalComputer{

	private static final String PROPOSALS_NORMAL_TYPE = ModuleCompletionProposal.PROPOSALS_NORMAL_TYPE;
	private static final String PROPOSALS_NORMAL_TYPE_LV1 = ModuleCompletionProposal.PROPOSALS_NORMAL_TYPE_LV1;

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

		List<String> proposals = null;


		//缓存结果
		if(listCache==null){

			proposals = new Vector<String>();

			String doc = document.get();

			Pattern p = Pattern.compile("([0-9a-zA-Z_\\$]+)");
			Pattern digt = Pattern.compile("^[0-9]+$");
			Matcher m = p.matcher(doc);
			String str = null;
			while (m.find()) {
				str = m.group(1);
				//排除纯数字
				if(!proposals.contains(str) && !digt.matcher(str).find()){
					proposals.add(str);
				}
			}


			listCache = proposals;
		}else{
			proposals = listCache;//如果有cache取cache
		}

		String prop = null;
		boolean isPrefixMatch = false;
		int  prefixLen = 0,replacementOffset=0,replacementLength=0;
		String tmp = null;
		String tmp2 = null;
		for(int i = 0,l=proposals.size();i<l;i++ ){
			prop = proposals.get(i);
			try{

				tmp =  prop.toLowerCase();
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
					completionProposalList.add(new ModuleCompletionProposal(prop, replacementOffset, replacementLength , prop.length()
							,null,isPrefixMatch?PROPOSALS_NORMAL_TYPE_LV1:PROPOSALS_NORMAL_TYPE));
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
