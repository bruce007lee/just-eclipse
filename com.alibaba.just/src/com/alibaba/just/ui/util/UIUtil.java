package com.alibaba.just.ui.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.alibaba.just.Activator;

public class UIUtil {
	private static ExecutorService threadPool = Executors.newScheduledThreadPool(4);

	private UIUtil(){}
	
	public static Shell getShell(){

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if(window!=null){
			return window.getShell();
		}
		return null;
	}

	public static IEditorPart getCurrentActiveEditor(){

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if(window!=null){
			IWorkbenchPage page = window.getActivePage();
			if(page!=null){
				return page.getActiveEditor();
			}
		}
		return null;
	}
		
	public static IDecoratorManager getDecoratorManager(){
		return Activator.getDefault().getWorkbench().getDecoratorManager();
	}

	public static ExecutorService getThreadPool() {
		return threadPool;
	}
}
