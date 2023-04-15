package org.uengine.modeling.resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.Id;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class DefaultResource implements IResource {

	private String path;
		public String getPath() {
			return this.path;
		}
		public void setPath(String path) {
			this.path = path;
		}

	private IContainer parent;


	@Autowired
	public ResourceManager resourceManager;




	public DefaultResource() {
	}

	public DefaultResource(String path) {
		this();
		setPath(path);
	}


	/**
	 *
	 * @return Returns only the file name **WITH** extension name
	 */
	public String getName() {

		if(path==null)
			return null;

		if (path.indexOf("/") == StringUtils.INDEX_NOT_FOUND) {
			return path;
		}
		return StringUtils.substringAfterLast(path, "/");
	}


	String displayName;

	
	/**
	 *
	 * @return Returns only the file name **WITHOUT** the extension name
	 */
	public String getDisplayName() {
		// File.separatorChar로 파일경로에서의 마지막 파일이나 폴더의 경로에서의 위치를 가져온다.

		if(displayName!=null)
			return displayName;


		return getName();

//		if(path==null)
//			return null;
//
//		int index = this.path.lastIndexOf("/") + 1;
//		// 경로에서 마지막 파일이나 폴더의 포인트 위치를 가져온다.
//		int pos = this.path.substring(index).indexOf(".");
//		if (pos == -1) {
//			return this.path.substring(index);
//		} else {
//			return this.path.substring(index, index + pos);
//		}
	}

	public void setDisplayName(String displayName){
		this.displayName = displayName;
	}


	/**
	 * type is filename extension if the file is folder String "folder" will be
	 * returned * for example,
	 * <p>
	 * folder : folder
	 * </p>
	 * <p>
	 * file : practice
	 * </p>
	 *
	 * @return filename extension
	 */
	public String getType() {

		if(path==null)
			return null;

		if (getName().indexOf(".") == StringUtils.INDEX_NOT_FOUND) {
			return TYPE_FOLDER;
		}
		return StringUtils.substringAfter(getName(), ".");
	}


	public IContainer getParent() {
		if (this.parent == null && "/".equals(this.getPath())) {
			return null;
		}
		return parent;
	}

	@Override
	public void setParent(IContainer parent) {
		this.parent = parent;
	}




	

	@Override
	public void accept(IResourceVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public void accept(IResourceVisitor visitor, boolean admin) {
		visitor.visit(this);
	}

	@Override
	public boolean isContainer() {
		return false;
	}


	@Override
	public void rename(String newName) {

		resourceManager.rename(this, newName);
	}


	public static IResource createResource(String path) throws Exception {
		
		

			DefaultResource defaultResource = new DefaultResource();
			defaultResource.setPath(path);

			return defaultResource;

		
	}


	@Override
	public void save(Object editingObject) throws Exception {
		resourceManager.save(this, editingObject);
	}

	public void copy(String desPath) throws Exception {
		resourceManager.copy(this, desPath);
	}

	public void move(IContainer container) throws IOException {
		resourceManager.move(this, container);
	}

	@Override
	public int compareTo(IResource resource) {
		if(!(this instanceof IContainer) && resource instanceof IContainer){
			return 1;
		}else if(this instanceof IContainer && !(resource instanceof IContainer)){
			return -1;
		}else if(this.getName() == null && resource.getName() == null){
			return 0;
		}else if(this.getName() == null && resource.getName() != null){
			return -1;
		}

		return this.getName().compareTo(resource.getName());
	}

	public Object load() throws Exception {
		Object object = resourceManager.getObject(this);

		return object;
	}

	public void delete() throws IOException {
		resourceManager.delete(this);
	}

	
}

