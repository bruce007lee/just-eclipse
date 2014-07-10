package com.alibaba.just.jsdt.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.eclipse.wst.jsdt.core.IFunction;
import org.eclipse.wst.jsdt.core.IJavaScriptElement;
import org.eclipse.wst.jsdt.core.IMember;
import org.eclipse.wst.jsdt.core.JavaScriptModelException;
import org.eclipse.wst.jsdt.ui.JSdocContentAccess;

import com.alibaba.just.jsdt.text.html.HTMLPrinter;

public class ProposalUtil {
	
	private static final String SEP = ",";

	/**
	 * create method label
	 * @param fun
	 * @return
	 */
	public static String createMethodProposalLabel(IFunction fun) {
		StringBuffer nameBuffer= new StringBuffer();

		// method name
		nameBuffer.append(fun.getElementName());

		// parameters
		nameBuffer.append('(');
		appendParameterList(nameBuffer, fun);
		nameBuffer.append(')');

		return nameBuffer.toString();
	}

	private static void appendParameterList(StringBuffer nameBuffer, IFunction fun) {
		try{
			String[] names = fun.getParameterNames();

			for(int i=0;i<names.length;i++){
				if(i>0){
					nameBuffer.append(SEP);
				}
				nameBuffer.append(names[i]);
			}

		}catch(Exception e){

		}
	}

	/**
	 * Returns the Javadoc in HTML format.
	 *
	 * @param result the Java elements for which to get the Javadoc
	 * @return a string with the Javadoc in HTML format.
	 */
	public static String getJavadocHtml(IJavaScriptElement curr) {
		StringBuffer buffer= new StringBuffer();
		Reader reader = null;
		if (curr instanceof IMember) {
			IMember member= (IMember) curr;
			//HTMLPrinter.addSmallHeader(buffer, getInfoText(member));
			try {
				reader= JSdocContentAccess.getHTMLContentReader(member, true, true);

				// Provide hint why there's no Javadoc
				/*if (reader == null && member.isBinary()) {
						boolean hasAttachedJavadoc= JavaDocLocations.getJavadocBaseLocation(member) != null;
						IPackageFragmentRoot root= (IPackageFragmentRoot)member.getAncestor(IJavaScriptElement.PACKAGE_FRAGMENT_ROOT);
						boolean hasAttachedSource= root != null && root.getSourceAttachmentPath() != null;
						IOpenable openable= member.getOpenable();
						boolean hasSource= openable.getBuffer() != null;

						if (!hasAttachedSource && !hasAttachedJavadoc)
							reader= new StringReader(InfoViewMessages.JavadocView_noAttachments);
						else if (!hasAttachedJavadoc && !hasSource)
							reader= new StringReader(InfoViewMessages.JavadocView_noAttachedJavadoc);
						else if (!hasAttachedSource)
							reader= new StringReader(InfoViewMessages.JavadocView_noAttachedSource);
						else if (!hasSource)
							reader= new StringReader(InfoViewMessages.JavadocView_noInformation);
					}*/

			} catch (JavaScriptModelException ex) {
				reader= new StringReader("error_gettingJavadoc");
			}
			if (reader != null) {
				HTMLPrinter.addParagraph(buffer, reader);
			}
		}

		if(reader!=null){
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (buffer.length() > 0) {
			HTMLPrinter.addPageEpilog(buffer);
			return buffer.toString();
		}

		return null;
	}

}
