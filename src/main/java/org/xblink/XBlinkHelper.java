package org.xblink;

import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.xblink.core.TransferInfo;
import org.xblink.core.UnfinishedSetField;
import org.xblink.core.cache.CacheInit;
import org.xblink.core.doc.DocReader;
import org.xblink.core.doc.DocWorkerFactory;
import org.xblink.core.doc.DocWriter;
import org.xblink.core.path.PathTracker;
import org.xblink.core.path.PathTrackingReader;
import org.xblink.core.path.PathTrackingWriter;
import org.xblink.core.reflect.ObjectOperator;
import org.xblink.core.reflect.ObjectOperatorFactory;
import org.xblink.core.serial.Deserializer;
import org.xblink.core.serial.SerialHelper;
import org.xblink.core.serial.Serializer;
import org.xblink.util.StringUtil;

/**
 * XBlink小助手，算是一个辅助类吧，其实是为了XBlink这个类看起来更加简洁，只保留对外的API，而把几个不想公开的方法放在这个类中了。
 * 
 * @author 胖五(pangwu86@gmail.com)
 */
class XBlinkHelper {

	private XBlinkHelper() {
	}

	private static void missDocTypeName(String docTypeName) {
		// 格式名称
		if (StringUtil.isBlankStr(docTypeName)) {
			throw new IllegalArgumentException("File format name not given.");
		}
	}

	private static void serializeNull(Object object) {
		if (null == object) {
			throw new IllegalArgumentException("Can't operate on an empty object.");
		}
	}

	// ***************************************序列化****************************************

	protected static String toAny(Object object, String docTypeName, Writer writer) {
		missDocTypeName(docTypeName);
		serializeNull(object);
		return serializing(object, DocWorkerFactory.createAnyWriter(writer, docTypeName));
	}

	protected static String toXml(Object object, Writer writer) {
		serializeNull(object);
		return serializing(object, DocWorkerFactory.createXmlWriter(writer));
	}

	/**
	 * 序列化。如果不生成文件，则直接返回生成的字符串。
	 * 
	 * @param object
	 *            被序列化的对象
	 * @param docWriter
	 *            文档输出器
	 * @return 字符串
	 */
	private static String serializing(Object object, DocWriter docWriter) {
		// 准备工作
		XBConfig xbConfig = XBConfigHelper.getXbConfig();
		docWriter.setXBConfig(xbConfig);
		PathTracker pathTracker = new PathTracker(xbConfig.isUseRelativePath());
		DocWriter realDocWriter = new PathTrackingWriter(docWriter, pathTracker);
		TransferInfo transferInfo = new TransferInfo(pathTracker, xbConfig, realDocWriter, null, null);
		// 关于Null的格式问题，暂时这里处理下
		SerialHelper.setUseStringNull(xbConfig.isUseStringNull());
		try {
			// 开始序列化
			realDocWriter.writeStartDocument();
			Serializer.writeUnknow(object, transferInfo, null);
			realDocWriter.writeEndDocument();
		} catch (Exception e) {
			throw new RuntimeException("Serialization failed.", e);
		} finally {
			if (null != realDocWriter) {
				try {
					realDocWriter.close();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				if (null != realDocWriter.getWriter()) {
					try {
						realDocWriter.getWriter().close();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		return docWriter.getString();
	}

	// ***************************************反序列化****************************************

	protected static Object fromAny(Reader reader, String docTypeName) {
		return fromAny(reader, docTypeName, null, null);
	}

	protected static Object fromAny(Reader reader, String docTypeName, Class<?> clz, Object object) {
		missDocTypeName(docTypeName);
		return deserializing(DocWorkerFactory.createAnyReader(reader, docTypeName), clz, object);
	}

	protected static Object fromXml(Reader reader) {
		return fromXml(reader, null, null);
	}

	protected static Object fromXml(Reader reader, Class<?> clz, Object object) {
		return deserializing(DocWorkerFactory.createXmlReader(reader), clz, object);
	}

	/**
	 * 反序列化。
	 * 
	 * @param DocReader
	 *            文档读取器
	 * @param clz
	 *            参考类
	 * @param object
	 *            参考对象
	 * @return 对象
	 */
	private static Object deserializing(DocReader docReader, Class<?> clz, Object object) {
		// 准备工作
		XBConfig xbConfig = XBConfigHelper.getXbConfig();
		PathTracker pathTracker = new PathTracker(xbConfig.isUseRelativePath());
		DocReader realDocReader = new PathTrackingReader(docReader, pathTracker);
		ObjectOperator objectOperator = ObjectOperatorFactory.createObjectOperator();
		TransferInfo transferInfo = new TransferInfo(pathTracker, xbConfig, null, realDocReader, objectOperator);
		Object result = null;
		// 关于Null的格式问题，暂时这里处理下
		SerialHelper.setUseStringNull(xbConfig.isUseStringNull());
		try {
			realDocReader.moveDown();
			result = Deserializer.readUnknow(clz, object, null, transferInfo);
			// 处理引用失败的例子
			handleUnfin(transferInfo);
		} catch (Exception e) {
			throw new RuntimeException("Deserialization failed.", e);
		} finally {
			if (null != realDocReader) {
				try {
					realDocReader.close();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				if (null != realDocReader.getReader()) {
					try {
						realDocReader.getReader().close();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		return result;
	}

	private static void handleUnfin(TransferInfo transferInfo) {
		List<UnfinishedSetField> unfins = transferInfo.getUnfins();
		if (null != unfins && unfins.size() > 0) {
			ObjectOperator objectOperator = transferInfo.getObjectOperator();
			Map<String, Object> pathRefMap = transferInfo.getPathRefMap();
			for (UnfinishedSetField unfin : unfins) {
				// 重新赋值
				Object fiedValue = pathRefMap.get(unfin.getObjPath());
				if (null == fiedValue) {
					throw new RuntimeException(String.format("Can't find the reference object by Path [%s]",
							unfin.getObjPath()));
				}
				objectOperator.setField(unfin.getObj(), unfin.getField(), fiedValue);
			}
		}
	}

	// *******************************

	protected static void registerClassesToBeUsed(Class<?>[] clzs) {
		for (Class<?> clz : clzs) {
			registerClassToBeUsed(clz);
		}
	}

	protected static void registerClassToBeUsed(Class<?> clz) {
		CacheInit.registerClassToBeUsedAndSetClassNameInAliasCache(clz);
	}
}
