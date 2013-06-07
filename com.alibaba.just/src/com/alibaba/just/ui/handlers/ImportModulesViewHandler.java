package com.alibaba.just.ui.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.alibaba.just.ui.popup.actions.ImportModulesViewAction;

public class ImportModulesViewHandler extends ImportModulesViewAction implements
		IHandler {
	
	private static final String IMPORT_MOUDLES_ACTION = "import_modules_action";

	public void addHandlerListener(IHandlerListener handlerListener) {

	}

	public void dispose() {

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		IWorkbenchPart  part = HandlerUtil.getActivePart(event);
		if(editor!=null && part!=null){
			IAction action = new Action(IMPORT_MOUDLES_ACTION){};
			this.setActiveEditor(action, editor);
			this.setActivePart(action, part);
		    this.run(action);
		    this.targetEditor = null;//clear
		}		
		return null;
	}

	public boolean isEnabled(){
		return true;
	}

	public boolean isHandled() {
		return true;
	}
	public void removeHandlerListener(IHandlerListener handlerListener) {

	}

}
