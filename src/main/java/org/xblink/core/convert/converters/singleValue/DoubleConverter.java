package org.xblink.core.convert.converters.singleValue;

import org.xblink.core.convert.SingleValueTypeConverter;

/**
 * double与其包装类转换器。
 * 
 * @author 胖五(pangwu86@gmail.com)
 */
public class DoubleConverter extends SingleValueTypeConverter {

	public Class<?>[] getTypes() {
		return new Class<?>[] { Double.class, double.class };
	}

	public boolean canConvert(Class<?> type) {
		return double.class == type || Double.class == type;
	}

	public String obj2Text(Object obj) throws Exception {
		return obj.toString();
	}

	public Object text2Obj(String text) throws Exception {
		return Double.valueOf(text);
	}

}
