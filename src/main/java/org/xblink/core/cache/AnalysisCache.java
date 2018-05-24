package org.xblink.core.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.xblink.core.AnalysisObject;

/**
 * 分析结果缓存。
 * 
 * @author 胖五(pangwu86@gmail.com)
 */
public class AnalysisCache {

	private AnalysisCache() {
	}

	private static boolean useAnalysisCache = true;

	private static Map<Class<?>, AnalysisObject> anaylsisMap = new ConcurrentHashMap<Class<?>, AnalysisObject>();

	public static void setUseAnalysisCache(boolean use) {
		useAnalysisCache = use;
	}

	/**
	 * 获得分析后的结果对象。
	 * 
	 * @param clz
	 * @param ignoreTransient
	 * @return 分析对象
	 */
	public static AnalysisObject getAnalysisObject(Class<?> clz, boolean ignoreTransient) {
		AnalysisObject analysisObject = null;
		if (useAnalysisCache) {
			analysisObject = anaylsisMap.get(clz);
			if (null == analysisObject) {
				analysisObject = new AnalysisObject(clz, ignoreTransient);
				anaylsisMap.put(clz, analysisObject);
			}
		} else {
			analysisObject = new AnalysisObject(clz, ignoreTransient);
		}
		return analysisObject;
	}
}
