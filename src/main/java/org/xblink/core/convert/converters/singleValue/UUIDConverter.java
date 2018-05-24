package org.xblink.core.convert.converters.singleValue;

import java.util.UUID;

import org.xblink.core.convert.SingleValueTypeConverter;

/**
 * UUID类型转换器。
 * 
 * @author 胖五(pangwu86@gmail.com)
 */
public class UUIDConverter extends SingleValueTypeConverter {

	public Class<?>[] getTypes() {
		return new Class<?>[] { UUID.class };
	}

	public boolean canConvert(Class<?> type) {
		return UUID.class == type;
	}

	public String obj2Text(Object obj) throws Exception {
		return ((UUID) obj).toString();
	}

	public Object text2Obj(String text) throws Exception {
		return UUID.fromString(text);
	}

}
