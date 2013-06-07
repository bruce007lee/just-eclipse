/**
 * 
 */
package com.alibaba.just.ui.view;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author bruce.liz
 *
 */
public class ViewContentProvider<T> implements IStructuredContentProvider {
	List<T> items = null;
	
	public ViewContentProvider(){
		this.initialize();
	}

	private void initialize(){
		this.items = new Vector<T>();
	}

	public void dispose() {
		if(this.items!=null){
			this.items.clear();
		}
		this.items = null;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

	public Object[] getElements(Object parent) {
			return items.toArray();	
	}
	
	public List<T> getItemList(){
		return items;
	}
	
	public T get(int index){
		return items.get(index);
	}
	
	public int indexOf(T item){
		return items.indexOf(item);
	}
	
	public boolean add(T item){
		return items.add(item);
	}
	
	public boolean addAll(Collection<T> c){
		return items.addAll(c);
	}
	
	public boolean addAll(int index , Collection<T> c){
		return items.addAll(index,c);
	}

	public void add(int index ,T item){
		items.add(index,item);
	}
	
	public T remove(int index){
		return items.remove(index);
	}
	
	public boolean remove(T obj){
		return items.remove(obj);
	}
	
	public boolean removeAll(Collection<T> c){
		return items.removeAll(c);
	}
	
	public void clear(){
		items.clear();
	}

}
