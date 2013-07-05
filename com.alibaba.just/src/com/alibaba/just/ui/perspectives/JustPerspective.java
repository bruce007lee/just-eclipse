/**
 * 
 */
package com.alibaba.just.ui.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.jdt.ui.JavaUI;

/**
 * 
 * @author bruce.liz
 *
 */
public class JustPerspective implements IPerspectiveFactory {

	private IPageLayout factory;

	public JustPerspective() {
		super();
	}

	public void createInitialLayout(IPageLayout factory) {
		this.factory = factory;
		addViews();
	}

	private void addViews() {
		// Creates the overall folder layout. 
		// Note that each new Folder uses a percentage of the remaining EditorArea.
		
		IFolderLayout bottom =
			factory.createFolder(
				"bottomRight", //NON-NLS-1
				IPageLayout.BOTTOM,
				0.75f,
				factory.getEditorArea());
		bottom.addView(IPageLayout.ID_PROBLEM_VIEW);		
		bottom.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);

		IFolderLayout topLeft =
			factory.createFolder(
				"topLeft", //NON-NLS-1
				IPageLayout.LEFT,
				0.25f,
				factory.getEditorArea());
		topLeft.addView(IPageLayout.ID_PROJECT_EXPLORER);

		IFolderLayout topRight =
			factory.createFolder(
				"topRight", //NON-NLS-1
				IPageLayout.RIGHT,
				0.7f,
				factory.getEditorArea());
		topRight.addView("com.alibaba.just.ui.views.ModuleView"); //NON-NLS-1
	}

}
