package org.pnz.scaffold.common.util;

import java.lang.reflect.Array;  
import java.lang.reflect.Field;  
import java.lang.reflect.Method;  
import java.util.Collection;  
import java.util.HashSet;  
import java.util.Iterator;  
import java.util.Map;  
import java.util.Map.Entry;  
import java.util.Set;  
  
import org.apache.commons.lang3.ArrayUtils;  
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;  
import com.alibaba.fastjson.serializer.SerializerFeature;  
import com.google.gson.Gson;
  

/**
 * ������Ϣ���ι���
 * ��Ϣ�ο��Բ��� http://blog.csdn.net/liuc0317/article/details/48787793
 * @author zhangGB
 *
 */
public class SensitiveDataUtils {

	private static Logger logger = LoggerFactory.getLogger(SensitiveDataUtils.class);

	/**
	 * [��������] ֻ��ʾ��һ�����֣���������Ϊ2���Ǻ�<���ӣ���**>
	 * 
	 * @param name
	 * @return
	 */
	public static String chineseName(String fullName) {
		if (StringUtils.isBlank(fullName)) {
			return "";
		}
		String name = StringUtils.left(fullName, 1);
		return StringUtils.rightPad(name, StringUtils.length(fullName), "*");
	}

	/**
	 * [��������] ֻ��ʾ��һ�����֣���������Ϊ2���Ǻ�<���ӣ���**>
	 * 
	 * @param familyName
	 * @param givenName
	 * @return
	 */
	public static String chineseName(String familyName, String givenName) {
		if (StringUtils.isBlank(familyName) || StringUtils.isBlank(givenName)) {
			return "";
		}
		return chineseName(familyName + givenName);
	}

	/**
	 * [���֤��] ��ʾ�����λ���������ء�����18λ����15λ��<���ӣ�*************5762>
	 * 
	 * @param id
	 * @return
	 */
	public static String idCardNum(String id) {
		if (StringUtils.isBlank(id)) {
			return "";
		}
		String num = StringUtils.right(id, 4);
		return StringUtils.leftPad(num, StringUtils.length(id), "*");
	}

	/**
	 * [�̶��绰] ����λ����������<���ӣ�****1234>
	 * 
	 * @param num
	 * @return
	 */
	public static String fixedPhone(String num) {
		if (StringUtils.isBlank(num)) {
			return "";
		}
		return StringUtils.leftPad(StringUtils.right(num, 4), StringUtils.length(num), "*");
	}

	/**
	 * [�ֻ�����] ǰ��λ������λ����������<����:138******1234>
	 * 
	 * @param num
	 * @return
	 */
	public static String mobilePhone(String num) {
		if (StringUtils.isBlank(num)) {
			return "";
		}
		return StringUtils.left(num, 3).concat(StringUtils
				.removeStart(StringUtils.leftPad(StringUtils.right(num, 4), StringUtils.length(num), "*"), "***"));
	}

	/**
	 * [��ַ] ֻ��ʾ������������ʾ��ϸ��ַ������Ҫ�Ը�����Ϣ��ǿ����<���ӣ������к�����****>
	 * 
	 * @param address
	 * @param sensitiveSize
	 *            ������Ϣ����
	 * @return
	 */
	public static String address(String address, int sensitiveSize) {
		if (StringUtils.isBlank(address)) {
			return "";
		}
		int length = StringUtils.length(address);
		return StringUtils.rightPad(StringUtils.left(address, length - sensitiveSize), length, "*");
	}

	/**
	 * [��������] ����ǰ׺����ʾ��һ����ĸ��ǰ׺�������أ����ǺŴ��棬@������ĵ�ַ��ʾ<����:g**@163.com>
	 * 
	 * @param email
	 * @return
	 */
	public static String email(String email) {
		if (StringUtils.isBlank(email)) {
			return "";
		}
		int index = StringUtils.indexOf(email, "@");
		if (index <= 1)
			return email;
		else
			return StringUtils.rightPad(StringUtils.left(email, 1), index, "*")
					.concat(StringUtils.mid(email, index, StringUtils.length(email)));
	}

	/**
	 * [���п���] ǰ��λ������λ���������Ǻ�����ÿλ1���Ǻ�<����:6222600**********1234>
	 * 
	 * @param cardNum
	 * @return
	 */
	public static String bankCardNoHide(String cardNum) {
		if (StringUtils.isBlank(cardNum)) {
			return "";
		}
		return StringUtils.left(cardNum, 6).concat(StringUtils.removeStart(
				StringUtils.leftPad(StringUtils.right(cardNum, 4), StringUtils.length(cardNum), "*"), "******"));
	}

	/**
	 * [��˾������������] ��˾�����������к�,��ʾǰ��λ���������Ǻ����أ�ÿλ1���Ǻ�<����:12********>
	 * 
	 * @param code
	 * @return
	 */
	public static String cnapsCode(String code) {
		if (StringUtils.isBlank(code)) {
			return "";
		}
		return StringUtils.rightPad(StringUtils.left(code, 2), StringUtils.length(code), "*");
	}

	/**
	 * ��ȡ����json�� <ע�⣺�ݹ����ûᵼ��java.lang.StackOverflowError>
	 * 
	 * @param javaBean
	 * @return
	 */
	public static String getJson(Object javaBean) {
		String json = null;
		if (null != javaBean) {
			Class<? extends Object> raw = javaBean.getClass();
			try {
				if (raw.isInterface())
					return json;
				Gson g = new Gson();
				Object clone = g.fromJson(g.toJson(javaBean, javaBean.getClass()), javaBean.getClass());
				Set<Integer> referenceCounter = new HashSet<Integer>();
				SensitiveDataUtils.replace(SensitiveDataUtils.findAllField(raw), clone, referenceCounter);
				json = JSON.toJSONString(clone, SerializerFeature.WriteMapNullValue,
						SerializerFeature.WriteNullListAsEmpty);
				referenceCounter.clear();
				referenceCounter = null;
			} catch (Throwable e) {
				logger.error("SensitiveInfoUtils.getJson() ERROR", e);
			}
		}
		return json;
	}

	private static Field[] findAllField(Class<?> clazz) {
		Field[] fileds = clazz.getDeclaredFields();
		while (null != clazz.getSuperclass() && !Object.class.equals(clazz.getSuperclass())) {
			fileds = (Field[]) ArrayUtils.addAll(fileds, clazz.getSuperclass().getDeclaredFields());
			clazz = clazz.getSuperclass();
		}
		return fileds;
	}

	private static void replace(Field[] fields, Object javaBean, Set<Integer> referenceCounter)
			throws IllegalArgumentException, IllegalAccessException {
		if (null != fields && fields.length > 0) {
			for (Field field : fields) {
				field.setAccessible(true);
				if (null != field && null != javaBean) {
					Object value = field.get(javaBean);
					if (null != value) {
						Class<?> type = value.getClass();
						// 1.���������ԣ����������е�
						if (type.isArray()) {
							int len = Array.getLength(value);
							for (int i = 0; i < len; i++) {
								Object arrayObject = Array.get(value, i);
								SensitiveDataUtils.replace(SensitiveDataUtils.findAllField(arrayObject.getClass()),
										arrayObject, referenceCounter);
							}
						} else if (value instanceof Collection<?>) {
							Collection<?> c = (Collection<?>) value;
							Iterator<?> it = c.iterator();
							while (it.hasNext()) {
								Object collectionObj = it.next();
								SensitiveDataUtils.replace(SensitiveDataUtils.findAllField(collectionObj.getClass()),
										collectionObj, referenceCounter);
							}
						} else if (value instanceof Map<?, ?>) {
							Map<?, ?> m = (Map<?, ?>) value;
							Set<?> set = m.entrySet();
							for (Object o : set) {
								Entry<?, ?> entry = (Entry<?, ?>) o;
								Object mapVal = entry.getValue();
								SensitiveDataUtils.replace(SensitiveDataUtils.findAllField(mapVal.getClass()), mapVal,
										referenceCounter);
							}
						} else if (!type.isPrimitive() && !StringUtils.startsWith(type.getPackage().getName(), "javax.")
								&& !StringUtils.startsWith(type.getPackage().getName(), "java.")
								&& !StringUtils.startsWith(field.getType().getName(), "javax.")
								&& !StringUtils.startsWith(field.getName(), "java.")
								&& referenceCounter.add(value.hashCode())) {
							SensitiveDataUtils.replace(SensitiveDataUtils.findAllField(type), value, referenceCounter);
						}
					}
					// 2. �������������
					// SensitiveInfo annotation =
					// field.getAnnotation(SensitiveInfo.class);
					// if (field.getType().equals(String.class) && null !=
					// annotation) {
					// String valueStr = (String) value;
					// if (StringUtils.isNotBlank(valueStr)) {
					// switch (annotation.type()) {
					// case CHINESE_NAME: {
					// field.set(javaBean,
					// SensitiveInfoUtils.chineseName(valueStr));
					// break;
					// }
					// case ID_CARD: {
					// field.set(javaBean,
					// SensitiveInfoUtils.idCardNum(valueStr));
					// break;
					// }
					// case FIXED_PHONE: {
					// field.set(javaBean,
					// SensitiveInfoUtils.fixedPhone(valueStr));
					// break;
					// }
					// case MOBILE_PHONE: {
					// field.set(javaBean,
					// SensitiveInfoUtils.mobilePhone(valueStr));
					// break;
					// }
					// case ADDRESS: {
					// field.set(javaBean, SensitiveInfoUtils.address(valueStr,
					// 4));
					// break;
					// }
					// case EMAIL: {
					// field.set(javaBean, SensitiveInfoUtils.email(valueStr));
					// break;
					// }
					// case BANK_CARD: {
					// field.set(javaBean,
					// SensitiveInfoUtils.bankCard(valueStr));
					// break;
					// }
					// case CNAPS_CODE: {
					// field.set(javaBean,
					// SensitiveInfoUtils.cnapsCode(valueStr));
					// break;
					// }
					// }
					// }
					// }
				}
			}
		}
	}

	// ----------------------------------------------------------------------------------------------
	public static Method[] findAllMethod(Class<?> clazz) {
		Method[] methods = clazz.getMethods();
		return methods;
	}

//	// ----------------------------------------------------------------------------------------------
//	public static enum SensitiveType {
//		/**
//		 * ������
//		 */
//		CHINESE_NAME,
//
//		/**
//		 * ���֤��
//		 */
//		ID_CARD,
//		/**
//		 * ������
//		 */
//		FIXED_PHONE,
//		/**
//		 * �ֻ���
//		 */
//		MOBILE_PHONE,
//		/**
//		 * ��ַ
//		 */
//		ADDRESS,
//		/**
//		 * �����ʼ�
//		 */
//		EMAIL,
//		/**
//		 * ���п�
//		 */
//		BANK_CARD,
//		/**
//		 * ��˾������������
//		 */
//		CNAPS_CODE;
//	}

}
