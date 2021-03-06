package com.alibaba.just.ui.popup.actions;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import com.alibaba.just.api.bean.Module;
import com.alibaba.just.ui.dialogs.ModuleSelectionDialog;
import com.alibaba.just.ui.util.PreferenceUtil;
import com.alibaba.just.ui.util.UIUtil;

/**
 * 
 * @author bruce.liz
 *
 */
public class AddModuleViewAction implements IEditorActionDelegate {
	private ModuleSelectionDialog moduleSelectionDialog = null;

	private IEditorPart targetEditor = null;

	private static final String VALID_MODULE_EXT = "js";

	public AddModuleViewAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	public void run(IAction action) {

		if(targetEditor==null){
			return;
		}

		IEditorInput input = targetEditor.getEditorInput();

		if(input instanceof FileEditorInput){
			FileEditorInput fileInput = (FileEditorInput)input;
			try {
				IFile ifile = fileInput.getFile();
				if(!VALID_MODULE_EXT.equals(ifile.getFileExtension())){
					return;
				}

				IProject project = ifile.getProject();
				if(targetEditor instanceof ITextEditor){
					handleShowModuleSelectionDlg(project,(ITextEditor)targetEditor,ifile);
				}

			} catch (Exception e) {
				String error = e.getMessage();
				if(error==null || error.trim().length()==0){
					error = e.toString();
				}
				MessageDialog.openInformation(
						UIUtil.getShell(),
						"Error",
						error);  
			} 		
		}

	}


	private void insertEditorText(ITextEditor editor,String text) throws Exception{
		IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		ISelection selection = editor.getSelectionProvider().getSelection();
		if(selection instanceof ITextSelection){
			ITextSelection textSeleciton = (ITextSelection)selection;
			document.replace(textSeleciton.getOffset(), textSeleciton.getLength() , text);
			editor.selectAndReveal(textSeleciton.getOffset()+text.length(), 0);
		}
	}

	private ITextSelection getEditSelection(ITextEditor editor){
		ISelection selection = editor.getSelectionProvider().getSelection();
		if(selection instanceof ITextSelection){
			return (ITextSelection)selection;
		}
		return null;
	}

	/**
	 * 
	 */
	private void handleShowModuleSelectionDlg(IProject project,final ITextEditor editor,IFile ifile){
		moduleSelectionDialog = new ModuleSelectionDialog(UIUtil.getShell()){
			protected void okPressed() {
				this.computeResult();
				try{
					String str = moduleSelectionDialog.getFirstResultString();
					if(str!=null){
						AddModuleViewAction.this.insertEditorText(editor,str);
					}
				}catch(Exception e1){

				}
				super.okPressed();
			}

			/**
			 * close and clear
			 */
			public boolean close() {
				boolean rs = super.close();
				moduleSelectionDialog = null;//clear
				return rs;
			}
		};

		ITextSelection sel = this.getEditSelection(editor);
		if(sel!=null && sel.getText()!=null){
			moduleSelectionDialog.setInitialPattern(sel.getText());
		}
		moduleSelectionDialog.setProject(project);

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
					moduleSelectionDialog.setPackagePath(mpath);
				}
			}
		}

		moduleSelectionDialog.create();
		moduleSelectionDialog.getSelectedModBtn().addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				try{
					Object rs  = moduleSelectionDialog.getSelectedResult();
					//System.out.println(rs);
					if(rs instanceof Module){
						String t = ((Module)rs).getName();
						AddModuleViewAction.this.insertEditorText(editor,t);					
					}
				}catch(Exception e1){

				}
			}
		});

		moduleSelectionDialog.getTextModBtn().addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				try{
					Control c = moduleSelectionDialog.getPatternControl();
					if(c instanceof Text){
						Text  text = (Text) c;
						String t = text.getText().trim();
						AddModuleViewAction.this.insertEditorText(editor,t);
					}
				}catch(Exception e1){

				}
			}
		});
		moduleSelectionDialog.open();		
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		this.targetEditor = targetEditor;
	}

}
