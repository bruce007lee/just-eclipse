/**
 * 
 */
package com.alibaba.just.ui.dialogs;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.IEncodingSupport;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;

import com.alibaba.just.api.bean.Module;
import com.alibaba.just.ui.util.PreferenceUtil;

/**
 * @author bruce.liz
 *
 */
public class OpenModuleDialog extends ModuleSelectionDialog {
	private static final String DLG_TITLE = "Open Module";
	/**
	 * @param shell
	 */
	public OpenModuleDialog(Shell shell) {
		super(shell);
	}

	/**
	 * @param shell
	 * @param project
	 */
	public OpenModuleDialog(Shell shell, IProject project) {
		super(shell, project);
	}



	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		addSelectedModBtn.setVisible(false);
		addTextModBtn.setVisible(false);
	}

	protected Control createDialogArea(Composite parent) {
		Control c = super.createDialogArea(parent);
		this.getShell().setText(DLG_TITLE);
		return c;
	}

	protected void okPressed() {
		Object module = this.getSelectedResult();
		if(Module.class.isInstance(module)){
			final String filePath = ((Module)module).getFilePath();
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {

					IWorkbenchPage page =
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

					File fileToOpen = new File(filePath);

					if (fileToOpen.exists() && fileToOpen.isFile()) {
						IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
						try {									
							IEditorPart  editPart = IDE.openEditorOnFileStore(page, fileStore);
							
							//fix 显示非workspace文件编码问题,目前先使用justeclipse中设置的编码
							//TODO 需要对lib库添加编码定义配置
							if(editPart !=null && FileStoreEditorInput.class.isInstance(editPart.getEditorInput())){
								IEncodingSupport encodingSupport= 
									(IEncodingSupport)editPart.getAdapter(IEncodingSupport.class);
								if(encodingSupport!=null){
									encodingSupport.setEncoding(PreferenceUtil.getFileCharset());
								}
							}
						} catch (Exception e ) {
							//e.printStackTrace();
						}
					} else {
						MessageDialog.openWarning(page.getWorkbenchWindow().getShell(), "Warning", "File not exist!");
					}

				}
			});
		}
		super.okPressed();
	}


}
