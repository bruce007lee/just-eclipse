/**
 * 
 */
package com.alibaba.just.ui.viewmodel;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.alibaba.just.ui.util.ImageManager;

/**
 * @author bruce.liz
 *
 */
public class ViewLabelProvider extends LabelProvider {
	
	public String getText(Object obj) {
		if(ViewItem.class.isInstance(obj)){
			String label = ((ViewItem)obj).getLabel();
			if(label==null){
				label = ((ViewItem)obj).getObj().toString();
			}
			return label;
		}else{
		   return obj.toString();
		}
	}
	
	public Image getImage(Object obj) {
		if(ViewItem.class.isInstance(obj)){
			return ImageManager.getImage(((ViewItem)obj).getIconName());
		}else{
			return null;
		}
	}
}
