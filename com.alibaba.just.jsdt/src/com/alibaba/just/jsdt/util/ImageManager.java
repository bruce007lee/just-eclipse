package com.alibaba.just.jsdt.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class ImageManager {
	
	public static final String IMG_MODULE_ICON = "img_module_icon";
	public static final String IMG_ALIAS_MODULE_ICON = "img_alias_module_icon";
	
	
	private static ImageRegistry imageRegistry = null;
	private static URL baseURL = null;
	private ImageManager(){}
	
	public static void init(AbstractUIPlugin plugin){
		baseURL = plugin.getBundle().getEntry("/");
		imageRegistry = plugin.getImageRegistry();
		ImageManager.putImage(IMG_MODULE_ICON, "icons/module.gif");
		ImageManager.putImage(IMG_ALIAS_MODULE_ICON, "icons/alias_module.gif");
	}
	
	public static void dispose(){
		imageRegistry = null;
	}
	
	public static void putImage(String key,String imagePath){
		try {
			imageRegistry.put(key,ImageDescriptor.createFromURL(new URL(baseURL,imagePath)));
		} catch (MalformedURLException e) {
		}
	}
	
	public static void putImage(String key,Image image){
		imageRegistry.put(key, image);
	}
	
	public static Image getImage(String key){
		return imageRegistry.get(key);
	}
}
