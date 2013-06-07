package com.alibaba.just.ui.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ui.editors.text.TextEditor;

import com.alibaba.just.ui.editors.contentassist.JavascriptContentAssistProcessor;

public class JavascriptEditor extends TextEditor {
	
	private static final String STRING_SINGLE_CONTENT = "__js_string_single";
	private static final String STRING_DOUBLE_CONTENT = "__js_string_double";

	public JavascriptEditor() {
		super();
		setSourceViewerConfiguration(new SourceViewerConfiguration(){
			public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
				ContentAssistant assistant = new ContentAssistant();

				JavascriptContentAssistProcessor tagContentAssistProcessor 
			        = new JavascriptContentAssistProcessor();
			    assistant.setContentAssistProcessor(tagContentAssistProcessor,IDocument.DEFAULT_CONTENT_TYPE);
			    assistant.setContentAssistProcessor(tagContentAssistProcessor,STRING_SINGLE_CONTENT);
			    assistant.setContentAssistProcessor(tagContentAssistProcessor,STRING_DOUBLE_CONTENT);
			    assistant.enableAutoActivation(true);
				assistant.setAutoActivationDelay(500);
				assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
				assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
				assistant.addCompletionListener(tagContentAssistProcessor);
			    return assistant;
			}
		});
	}
	
	
	public void dispose() {
		super.dispose();
	}

}
