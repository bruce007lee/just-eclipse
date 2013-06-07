package com.alibaba.just.ui.decorators;

import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.widgets.Display;

import com.alibaba.just.ui.util.ImageManager;
import com.alibaba.just.ui.util.PreferenceUtil;
import com.alibaba.just.ui.util.UIUtil;

/**
 * Root path folder tree item decorator.
 * @see ILightweightLabelDecorator
 */
public class RootPathDecorator extends LabelProvider implements ILightweightLabelDecorator {

	public static final String ROOT_PATH_DECORATORS_ID = RootPathDecorator.class.getName();

	/**
	 * String constants for the various icon placement options from the template
	 * wizard.
	 */
	public static final String TOP_RIGHT = "TOP_RIGHT";

	public static final String TOP_LEFT = "TOP_LEFT";

	public static final String BOTTOM_RIGHT = "BOTTOM_RIGHT";

	public static final String BOTTOM_LEFT = "BOTTOM_LEFT";

	public static final String UNDERLAY = "UNDERLAY";
	
	ImageDescriptor rootPathImageDescriptor = null;

	/** The integer value representing the placement options */
	private int quadrant;

	/**
	 * The image description used in
	 * <code>addOverlay(ImageDescriptor, int)</code>
	 */
	private ImageDescriptor descriptor;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object, org.eclipse.jface.viewers.IDecoration)
	 */
	public void decorate(Object element, IDecoration decoration) {
		
		this.createImageDescriptors();

		IResource resource = (IResource) element;
		if(IFolder.class.isInstance(element)){
			IFolder ifolder = (IFolder) element;	
			String path = ifolder.getFullPath().toString();
			IProject project = ifolder.getProject();
			path = PreferenceUtil.getRootPathByIPath(project, path);
			List<String> list = PreferenceUtil.getProjectRootPathList(resource.getProject());
			if(list.contains(path)){
				descriptor = rootPathImageDescriptor;
				quadrant = IDecoration.TOP_RIGHT;
				decoration.addOverlay(descriptor,quadrant);
			}
		}

	}
	
	/**
	 * create image descriptors
	 */
	private void createImageDescriptors(){
		if(rootPathImageDescriptor==null){
		  rootPathImageDescriptor = ImageDescriptor.createFromImage(ImageManager.getImage(ImageManager.IMG_ROOT_PATH));
		}
	}


	/**
	 * 
	 * @param updateList
	 */
	public static void refresh(){
		UIUtil.getDecoratorManager().update(ROOT_PATH_DECORATORS_ID);
	}

	public static RootPathDecorator getRootPathDecorator(){
		return (RootPathDecorator)UIUtil.getDecoratorManager().getBaseLabelProvider(ROOT_PATH_DECORATORS_ID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
	}
}