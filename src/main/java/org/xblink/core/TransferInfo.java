package org.xblink.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xblink.XBConfig;
import org.xblink.core.doc.DocReader;
import org.xblink.core.doc.DocWriter;
import org.xblink.core.path.PathTracker;
import org.xblink.core.reflect.ObjectOperator;

/**
 * 用来传递在工作过程中的一些需要使用信息。
 * 
 * @author 胖五(pangwu86@gmail.com)
 */
public class TransferInfo {

	private final PathTracker pathTracker;
	private final XBConfig xbConfig;
	private final DocWriter docWriter;
	private final DocReader docReader;
	private final ObjectOperator objectOperator;
	private final Map<Object, ReferenceObject> refMap;
	private final Map<String, Object> pathRefMap;
	private final List<UnfinishedSetField> unfins;

	private boolean isArrayClass;

	public TransferInfo(PathTracker pathTracker, XBConfig xbConfig, DocWriter docWriter, DocReader docReader,
			ObjectOperator objectOperator) {
		this.pathTracker = pathTracker;
		this.xbConfig = xbConfig;
		this.docWriter = docWriter;
		this.docReader = docReader;
		this.objectOperator = objectOperator;
		this.refMap = new HashMap<Object, ReferenceObject>();
		this.pathRefMap = new HashMap<String, Object>();
		this.unfins = new ArrayList<UnfinishedSetField>();
		this.isArrayClass = false;
	}

	public PathTracker getPathTracker() {
		return pathTracker;
	}

	public XBConfig getXbConfig() {
		return xbConfig;
	}

	public DocWriter getDocWriter() {
		return docWriter;
	}

	public DocReader getDocReader() {
		return docReader;
	}

	public Map<Object, ReferenceObject> getRefMap() {
		return refMap;
	}

	public Map<String, Object> getPathRefMap() {
		return pathRefMap;
	}

	public ObjectOperator getObjectOperator() {
		return objectOperator;
	}

	public List<UnfinishedSetField> getUnfins() {
		return unfins;
	}

	public boolean isArrayClass() {
		return isArrayClass;
	}

	public void setArrayClass(boolean isArrayClass) {
		this.isArrayClass = isArrayClass;
	}

}
