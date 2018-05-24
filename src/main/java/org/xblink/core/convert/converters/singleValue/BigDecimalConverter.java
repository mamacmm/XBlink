package org.xblink.core.convert.converters.singleValue;

import java.math.BigDecimal;

import org.xblink.core.convert.SingleValueTypeConverter;

/**
 * BigDecimal转换器。
 * 
 * @author 胖五(pangwu86@gmail.com)
 */
public class BigDecimalConverter extends SingleValueTypeConverter {

	public Class<?>[] getTypes() {
		return new Class<?>[] { BigDecimal.class };
	}

	public boolean canConvert(Class<?> type) {
		return BigDecimal.class == type;
	}

	public String obj2Text(Object obj) throws Exception {
		return obj.toString();
	}

	public Object text2Obj(String text) throws Exception {
		return new BigDecimal(text);
	}

}