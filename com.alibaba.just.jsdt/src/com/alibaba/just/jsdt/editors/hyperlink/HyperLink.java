package com.alibaba.just.jsdt.editors.hyperlink;

/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.jsdt.core.IJavaScriptElement;
import org.eclipse.wst.jsdt.core.dom.ASTNode;
import org.eclipse.wst.jsdt.core.dom.ASTVisitor;
import org.eclipse.wst.jsdt.core.dom.JavaScriptUnit;
import org.eclipse.wst.jsdt.internal.ui.JavaScriptPlugin;
import org.eclipse.wst.jsdt.internal.ui.javaeditor.ASTProvider;
import org.eclipse.wst.jsdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.wst.jsdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.wst.jsdt.internal.ui.javaeditor.JavaElementHyperlink;
import org.eclipse.wst.jsdt.internal.ui.text.JavaWordFinder;
import org.eclipse.wst.jsdt.ui.actions.OpenAction;

import com.alibaba.just.jsdt.actions.OpenLinkAction;


/**
 * Java element hyperlink detector.
 *
 * 
 */
public class HyperLink extends AbstractHyperlinkDetector {
	
	/*
	 * @see org.eclipse.jface.text.hyperlink.IHyperlinkDetector#detectHyperlinks(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion, boolean)
	 */
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		ITextEditor textEditor= (ITextEditor)getAdapter(ITextEditor.class);
		if (region == null  || !(textEditor instanceof JavaEditor))
			return null;

		int offset= region.getOffset();

		IJavaScriptElement input= EditorUtility.getEditorInputJavaElement(textEditor, false);
		if (input == null)
			return null;
		
			//get the possibly hyperlink word region
			IDocument document= textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
			IRegion wordRegion= JavaWordFinder.findWord(document, offset);
			if (wordRegion == null)
				return null;
			

			IAction openAction= new OpenLinkAction((JavaEditor)textEditor,wordRegion.getOffset(),wordRegion.getLength());

			
			//search the AST for the word region to determine if it is a candidate for a link
			JavaScriptUnit ast = JavaScriptPlugin.getDefault().getASTProvider().getAST(
							input, ASTProvider.WAIT_NO, null);
			if(ast != null) {
				int start = wordRegion.getOffset();
				int end = start + wordRegion.getLength();
				HyperlinkCandidateVisitor visitor = new HyperlinkCandidateVisitor(start, end);
				try {
					ast.accept(visitor);
				} catch(HyperlinkCandidateVisitor.HyperlinkCandidateFoundException e) {
					//just means the visiting has been cut off early
				}
				if(visitor.fFoundCandidate) {
					return new IHyperlink[] {new JavaElementHyperlink(wordRegion, openAction){
						public String getHyperlinkText() {
							return "View Reference";
						}						
					}};
				}
			}
			
		return null;
	}

	/**
	 * <p>Visits an AST looking for a node between the given start and end offsets
	 * that could be a hyperlink candidate.</p>
	 * 
	 * @throws HyperlinkCandidateFoundException once a candidate has been found this is thrown to
	 * short cut any further visiting
	 */
	private class HyperlinkCandidateVisitor extends ASTVisitor {
		/** <code>true</code> if a hyperlink candidate is found, <code>false</code> otherwise.</p> */
		protected boolean fFoundCandidate;
		
		/** Start offset where the candidate should start after. */
		private int fStartOffset;
		
		/** End offset where the candidate should end before. */
		private int fEndOffset;
		
		/**
		 * @param start Start offset where the candidate should start after 
		 * @param end End offset where the candidate should end before
		 */
		protected HyperlinkCandidateVisitor(int start, int end) {
			this.fFoundCandidate = false;
			this.fStartOffset = start;
			this.fEndOffset = end;
		}
		
		/**
		 * @see org.eclipse.wst.jsdt.core.dom.ASTVisitor#preVisit(org.eclipse.wst.jsdt.core.dom.ASTNode)
		 */
		public void preVisit(ASTNode node) {
			switch(node.getNodeType()) {
				case ASTNode.SIMPLE_NAME:
				case ASTNode.QUALIFIED_NAME:
					isCandidate(node);
					break;
			}
		}
		
		/**
		 * <p>Determines if the given node is a hyperlink candidate based on the start
		 * and end offsets. </p>
		 * 
		 * @param n Determine if this {@link ASTNode} is a candidate for a hyperlink based
		 * on the start and end offsets
		 * 
		 * @throws HyperlinkCandidateFoundException
		 */
		private void isCandidate(ASTNode n) {
			if(!this.fFoundCandidate) {
				int start = n.getStartPosition();
				int end = start + n.getLength();
				if(start >= this.fStartOffset && end <= this.fEndOffset) {
					this.fFoundCandidate = true;
					throw new HyperlinkCandidateFoundException();
				}
			}
		}
		
		/**
		 * <p>Used to cancel visiting after a candidate has been found.</p>
		 */
		private class HyperlinkCandidateFoundException extends RuntimeException {
			private static final long serialVersionUID = 1L;
		}
	}
}