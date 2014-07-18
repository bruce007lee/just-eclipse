package com.alibaba.just.jsdt;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.wst.jsdt.internal.ui.text.java.CompletionProposalComputerRegistry;
import org.eclipse.wst.jsdt.ui.PreferenceConstants;

/**
 * 
 * @author bruce.liz
 *
 */
public class Startup implements IStartup {

	public void earlyStartup() {
		this.fixJSDTProposal();
	}

	/**
	 * fix JSDT 默认提示设置的问题
	 */
	private void fixJSDTProposal(){
		try{
			//IPreferenceStore store= JavaScriptPlugin.getDefault().getPreferenceStore();			
			IPreferenceStore store = new ScopedPreferenceStore(new InstanceScope(), "org.eclipse.wst.jsdt.ui");			
			String preference= store.getString(PreferenceConstants.CODEASSIST_CATEGORY_ORDER);
			if(preference!=null){
				preference = "";
				int rank = 65535;
				String[] ids = new String[]{
						"com.alibaba.just.jsdt.justWordsProposals",
						"com.alibaba.just.jsdt.justModuleProposals",
						"com.alibaba.just.jsdt.justJSDTProposals",
						"org.eclipse.wst.jsdt.ui.spellingProposalCategory",
						"org.eclipse.wst.jsdt.ui.javaTypeProposalCategory",
						"org.eclipse.wst.jsdt.ui.javaNoTypeProposalCategory",
						"org.eclipse.wst.jsdt.ui.textProposalCategory",
						"org.eclipse.wst.jsdt.ui.templateProposalCategory"
				};
				for(String id:ids) {
					preference += ( id + ":" + (rank++) + "\0" );
				}
				store.setDefault(PreferenceConstants.CODEASSIST_CATEGORY_ORDER, preference);
				store.setValue(PreferenceConstants.CODEASSIST_CATEGORY_ORDER, preference);

				//FIXME 现在只能用internal方法
				CompletionProposalComputerRegistry registry = CompletionProposalComputerRegistry.getDefault();
				registry.reload();
			}

		}catch(Exception e){

		}
	}


}
