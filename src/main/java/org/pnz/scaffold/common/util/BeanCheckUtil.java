package org.pnz.scaffold.common.util;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author zhangGB
 *
 */
public class BeanCheckUtil {
	/**
	 * �ж϶����Ƿ�ΪNULL.��������Ǽ������ͻ���Map,����Ҫ�ж��Ƿ����Ԫ��
     * ���ϻ���MapΪNULL���߷���Ϊtrue���򷵻�false
     * �ַ�������ΪNULL���߿��ַ�������true
	 * @param source
	 * @return
	 */
	public static boolean checkNullOrEmpty(Object source) {
		if(source instanceof String) {
			return StringUtils.isBlank((String) source);
		}else if(source instanceof Collection) {
			return source == null || ((Collection<?>)source).isEmpty();
		}else if (source instanceof Map) {
			return source == null || ((Map<?,?>)source).isEmpty();
		}
		return source == null;	
	}
	
}
