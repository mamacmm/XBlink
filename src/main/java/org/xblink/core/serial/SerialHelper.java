package org.xblink.core.serial;

import org.xblink.core.Constant;
import org.xblink.core.ReferenceObject;
import org.xblink.core.TransferInfo;
import org.xblink.core.convert.converters.singleValue.EnumConverter;
import org.xblink.core.convert.converters.singleValue.NullConverter;

public class SerialHelper {

	private SerialHelper() {
	}

	private static EnumConverter enumConverter;
	private static NullConverter nullConverter;

	static {
		nullConverter = new NullConverter();
		enumConverter = new EnumConverter();
	}

	public static void setUseStringNull(boolean useStringNull) {
		nullConverter.useStringNull(useStringNull);
	}

	/**
	 * 获得NullConverter。
	 * 
	 * @return
	 */
	protected static NullConverter getNullConverter() {
		return nullConverter;
	}

	/**
	 * 获得EnumConverter。
	 * 
	 * @return
	 */
	protected static EnumConverter getEnumConverter() {
		return enumConverter;
	}

	/**
	 * 判断对象是否属于引用对象。
	 * 
	 * @param obj
	 * @param transferInfo
	 * @return
	 * @throws Exception
	 */
	protected static boolean isReferenceObjectByObj(Object obj, TransferInfo transferInfo) throws Exception {
		return transferInfo.getRefMap().containsKey(obj);
	}

	/**
	 * 记录当前对象，放入到引用对象缓存中。
	 * 
	 * @param obj
	 * @param transferInfo
	 * @return
	 * @throws Exception
	 */
	protected static void recordReferenceObjectByObj(Object obj, TransferInfo transferInfo) throws Exception {
		String[] currentPath = transferInfo.getPathTracker().getCurrentPath();
		transferInfo.getRefMap().put(obj, new ReferenceObject(obj, currentPath));
	}

	/**
	 * 判断当前节点是否是引用节点。
	 * 
	 * @param transferInfo
	 * @return
	 * @throws Exception
	 */
	protected static boolean isReferenceObjectByNode(TransferInfo transferInfo) throws Exception {
		return 1 == transferInfo.getDocReader().getAttributeCount()
				&& null != transferInfo.getDocReader().getAttribute(Constant.ATTRIBUTE_REFERENCE);
	}

	/**
	 * 记录当前对象，放入到引用对象缓存中。
	 * 
	 * @param obj
	 * @param transferInfo
	 * @return
	 * @throws Exception
	 */
	protected static void recordReferenceObjectByPath(Object obj, TransferInfo transferInfo) throws Exception {
		String currentPath = transferInfo.getPathTracker().getCurrentPathAsString();
		transferInfo.getPathRefMap().put(currentPath, obj);
	}

}
