package org.xblink.core.serial;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.xblink.core.AnalysisObject;
import org.xblink.core.Constant;
import org.xblink.core.TransferInfo;
import org.xblink.core.UnfinishedSetField;
import org.xblink.core.cache.AliasCache;
import org.xblink.core.cache.AnalysisCache;
import org.xblink.core.convert.ConverterWarehouse;
import org.xblink.core.doc.DocReader;
import org.xblink.util.ArrayUtil;
import org.xblink.util.StringUtil;
import org.xblink.util.TypeUtil;

/**
 * 反序列化一个对象。
 * 
 * @author 胖五(pangwu86@gmail.com)
 */
public class Deserializer {

	private Deserializer() {
	}

	public static Object readUnknow(Class<?> objClz, Object obj, Field field, TransferInfo transferInfo)
			throws Exception {
		Object result = null;
		if (null == objClz) {
			if (null != obj) {
				objClz = obj.getClass();
			}
		}
		if (objClz == Object.class) {
			// Object.class没有任何用与null等同
			objClz = null;
		}
		// 数组的特殊处理
		if (transferInfo.isArrayClass()) {
			// 集合类型
			result = readCollection(objClz, obj, field, transferInfo);
		} else if (null == objClz) {
			// 最麻烦的情况了，需要根据名称进行猜测，找到对应的类进行处理
			// 这里怎么处理来，主要是集合类型与Map类型，没有使用泛型的情况下，如何去做
			result = readAnyType(transferInfo);
		}
		// 下面根据传入对象的类型，采用不同的策略
		else if (TypeUtil.isSingleValueType(objClz)) {
			// 单值类型
			result = readSingleValue(objClz, transferInfo);
		} else if (TypeUtil.isEnum(objClz)) {
			// 枚举类型
			result = readEnum(objClz, transferInfo);
		} else {
			// 其他类型都可以算作引用类型
			if (SerialHelper.isReferenceObjectByNode(transferInfo)) {
				// 引用类型
				result = readReference(obj, field, transferInfo);
			} else {
				if (TypeUtil.isCollectionType(objClz)) {
					// 集合类型
					result = readCollection(objClz, obj, field, transferInfo);
				} else if (TypeUtil.isEntryType(objClz)) {
					// Map.Entry类型
					result = readEntry(objClz, obj, field, transferInfo);
				} else if (TypeUtil.isMapType(objClz)) {
					// Map类型
					result = readMap(objClz, obj, field, transferInfo);
				} else {
					// 对象类型
					result = readObject(objClz, transferInfo);
				}
			}
		}
		return result;
	}

	private static Object readAnyType(TransferInfo transferInfo) throws Exception {
		// 只能根据名称去猜测类型
		String nodeName = transferInfo.getDocReader().getNodeName();
		Class<?> classType = tryFindTypeByNodeName(nodeName);
		// 如果是数组类，需要特殊处理
		if (!classType.isArray() && ArrayUtil.tagNameIsArrayClass(nodeName)) {
			transferInfo.setArrayClass(true);
		}
		return readUnknow(classType, null, null, transferInfo);
	}

	private static Class<?> tryFindTypeByNodeName(String nodeName) {
		Class<?> type = AliasCache.getClassByAliasName(nodeName);
		if (null == type) {
			type = TypeUtil.tryFindThisClass(nodeName);
		}
		return type;
	}

	private static Object readSingleValue(Class<?> objClz, TransferInfo transferInfo) throws Exception {
		return ConverterWarehouse.searchConverterForType(objClz, transferInfo).text2Obj(
				transferInfo.getDocReader().getTextValue());
	}

	private static Object readEnum(Class<?> objClz, TransferInfo transferInfo) throws Exception {
		return SerialHelper.getEnumConverter().text2Obj(transferInfo.getDocReader().getTextValue(), objClz);
	}

	private static Object readReference(Object obj, Field field, TransferInfo transferInfo) throws Exception {
		String relativePath = transferInfo.getDocReader().getAttribute(Constant.ATTRIBUTE_REFERENCE);
		String objPath = null;
		if (relativePath.startsWith(Constant.PATH_SEPARATER)) {
			// 如果是采用了绝对路径，则不需要再计算
			objPath = relativePath;
		} else {
			objPath = transferInfo.getPathTracker().getTargetNodeAbsolutePathAsString(relativePath);
		}
		Object result = transferInfo.getPathRefMap().get(objPath);
		if (null == result) {
			// 记录当前状态等全部完成后再次放入
			UnfinishedSetField unfin = new UnfinishedSetField();
			unfin.setField(field);
			unfin.setObj(obj);
			unfin.setObjPath(objPath);
			transferInfo.getUnfins().add(unfin);
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Object readCollection(Class<?> objClz, Object obj, Field field, TransferInfo transferInfo)
			throws Exception {
		DocReader docReader = transferInfo.getDocReader();
		Object result = null;
		if (transferInfo.isArrayClass() || objClz.isArray()) {
			Class<?> arrayItemClz = null;
			if (transferInfo.isArrayClass()) {
				arrayItemClz = objClz;
				transferInfo.setArrayClass(false);
			} else {
				arrayItemClz = objClz.getComponentType();
			}
			List<Object> items = new ArrayList<Object>();
			while (docReader.hasMoreChildren()) {
				docReader.moveDown();
				Object item = readUnknow(arrayItemClz, null, null, transferInfo);
				items.add(item);
				docReader.moveUp();
			}
			// 放入数组中
			Object array = Array.newInstance(arrayItemClz, items.size());
			int i = 0;
			for (Iterator<Object> iterator = items.iterator(); iterator.hasNext();) {
				Array.set(array, i++, iterator.next());
			}
			result = array;
		} else {
			Collection collection = null;
			if (null != obj) {
				// 集合的话可以利用传入的对象，但是需要清空该对象
				collection = (Collection) obj;
				collection.clear();
			} else {
				if (List.class.isAssignableFrom(objClz)) {
					collection = (Collection) transferInfo.getObjectOperator().newInstance(List.class);
				} else if (Set.class.isAssignableFrom(objClz)) {
					collection = (Collection) transferInfo.getObjectOperator().newInstance(Set.class);
				} else {
					throw new RuntimeException(String.format("Can't convert the type [%s] to collection.",
							objClz.getName()));
				}
			}
			// 尝试获得泛型
			Class<?> itemClz = null;
			if (null != field) {
				itemClz = transferInfo.getObjectOperator().getCollectionGenericType(field.getGenericType());
			}
			while (docReader.hasMoreChildren()) {
				docReader.moveDown();
				Object item = readUnknow(itemClz, null, null, transferInfo);
				collection.add(item);
				docReader.moveUp();
			}
			result = collection;
		}
		// 记录引用的对象
		SerialHelper.recordReferenceObjectByPath(result, transferInfo);
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Object readMap(Class<?> objClz, Object obj, Field field, TransferInfo transferInfo) throws Exception {
		DocReader docReader = transferInfo.getDocReader();
		Map map = null;
		if (null != obj) {
			// 集合的话可以利用传入的对象，但是需要清空该对象
			map = (Map) obj;
			map.clear();
		} else {
			if (Map.class.isAssignableFrom(objClz)) {
				map = (Map) transferInfo.getObjectOperator().newInstance(Map.class);
			} else {
				throw new RuntimeException(String.format("Can't convert the type [%s] to map.", objClz.getName()));
			}
		}
		// 尝试获得泛型
		Class<?> keyClz = null;
		Class<?> valueClz = null;
		if (null != field) {
			keyClz = transferInfo.getObjectOperator().getMapKeyGenericType(field.getGenericType());
			valueClz = transferInfo.getObjectOperator().getMapValueGenericType(field.getGenericType());
		}
		while (docReader.hasMoreChildren()) {
			docReader.moveDown(); // 进入entry

			docReader.moveDown();// 进入key
			Object key = readUnknow(keyClz, null, null, transferInfo);
			docReader.moveUp();// 退出key

			docReader.moveDown();// 进入value
			Object value = readUnknow(valueClz, null, null, transferInfo);
			docReader.moveUp();// 退出value

			docReader.moveUp();// 退出entry
			map.put(key, value);
		}
		// 记录引用的对象
		SerialHelper.recordReferenceObjectByPath(map, transferInfo);
		return map;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Object readEntry(Class<?> objClz, Object obj, Field field, TransferInfo transferInfo)
			throws Exception {
		DocReader docReader = transferInfo.getDocReader();
		Map map = new HashMap();
		// 尝试获得泛型
		Class<?> keyClz = null;
		Class<?> valueClz = null;
		if (null != field) {
			keyClz = transferInfo.getObjectOperator().getMapKeyGenericType(field.getGenericType());
			valueClz = transferInfo.getObjectOperator().getMapValueGenericType(field.getGenericType());
		}
		while (docReader.hasMoreChildren()) {
			docReader.moveDown();// 进入key
			Object key = readUnknow(keyClz, null, null, transferInfo);
			docReader.moveUp();// 退出key

			docReader.moveDown();// 进入value
			Object value = readUnknow(valueClz, null, null, transferInfo);
			docReader.moveUp();// 退出value

			map.put(key, value);
		}
		// 这里应该只有一个
		Map.Entry entry = (Entry) map.entrySet().iterator().next();
		// 记录引用的对象
		SerialHelper.recordReferenceObjectByPath(entry, transferInfo);
		return entry;
	}

	private static Object readObject(Class<?> objClz, TransferInfo transferInfo) throws Exception {
		Object result = transferInfo.getObjectOperator().newInstance(objClz);
		DocReader docReader = transferInfo.getDocReader();
		// 记录引用的对象
		SerialHelper.recordReferenceObjectByPath(result, transferInfo);
		// 分析对象，根据分析结果，逐个类型进行序列化
		boolean ignoreTransient = transferInfo.getXbConfig().isIgnoreTransient();
		AnalysisObject analysisObject = AnalysisCache.getAnalysisObject(objClz, ignoreTransient);
		boolean ignoreNull = transferInfo.getXbConfig().isIgnoreNull();
		// attribute类型
		if (!analysisObject.attributeIsEmpty() && docReader.getAttributeCount() > 0) {
			Iterator<String> iter = docReader.getAttributeNames();
			while (iter.hasNext()) {
				String attName = iter.next();
				if (analysisObject.getAttributeFieldMap().containsKey(attName)) {
					Field field = analysisObject.getAttributeFieldMap().get(attName);
					String fieldValueStr = docReader.getAttribute(attName);
					Object fieldValue = null;
					if (!ignoreNull && SerialHelper.getNullConverter().canConvert(fieldValueStr)) {
						fieldValue = null;
					} else if (analysisObject.isFieldHasConverter(field)) {
						fieldValue = analysisObject.getFieldConverter(field).text2Obj(fieldValueStr);
					} else {
						fieldValue = ConverterWarehouse.searchConverterForType(field.getType(), transferInfo).text2Obj(
								fieldValueStr);
					}
					transferInfo.getObjectOperator().setField(result, field, fieldValue);
				}
			}
		}
		// 其他类型
		if (!analysisObject.otherIsEmpty()) {
			while (docReader.hasMoreChildren()) {
				docReader.moveDown();
				String nodeName = docReader.getNodeName();
				Field field = analysisObject.getOtherFieldMap().get(nodeName);
				Class<?> fieldClz = field.getType();
				Object fieldValue = null;
				boolean useNullConverter = false;
				String fieldValueStr = docReader.getTextValue();
				boolean isEmptyStr = StringUtil.isBlankStr(fieldValueStr);
				boolean isStrType = field.getType() == String.class;
				if (!ignoreNull) {
					// 非String类型
					if (isEmptyStr) {
						if (!isStrType) {
							useNullConverter = true;
						}
					} else {
						if (SerialHelper.getNullConverter().canConvert(fieldValueStr)) {
							useNullConverter = true;
						}
					}
				}
				if (analysisObject.isFieldHasConverter(field) || useNullConverter) {
					fieldValue = useNullConverter ? null : analysisObject.getFieldConverter(field).text2Obj(
							fieldValueStr);
				} else if (isEmptyStr && isStrType) {
					fieldValue = fieldValueStr;
				} else {
					fieldValue = readUnknow(fieldClz, null, field, transferInfo);
				}
				transferInfo.getObjectOperator().setField(result, field, fieldValue);
				docReader.moveUp();
			}
		}
		return result;
	}
}
