package org.pnz.scaffold.common.util;

/**
 * ��Ҫ���������������͵�ö��
 * @author zhangGB
 *
 */
/**
 * @author zhangGB
 *
 */
public enum SensitiveDataEnum {

	/**�����˺��������ͱ�ʶ,������SensitiveInfoUtils.bankCardNoHide������sensitiveDataType����*/
	BANKCARDNO_DATA("bankCardNoHide",new EnumObject() {
		@Override
		public String sensitiveData(String value) {
			return SensitiveDataUtils.bankCardNoHide(value);
		}	
	}),
	/**���֤���������ͱ�ʶ,������SensitiveInfoUtils.filterHide������SensitiveDataType����*/
	IDCARDNO_DATA("idCardNoHide",new EnumObject() {
		@Override
		public String sensitiveData(String value) {
			return SensitiveDataUtils.idCardNum(value);
		}	
	}),
	/**�绰�����������ͱ�ʶ,������SensitiveInfoUtils.filterHide������SensitiveDataType����*/
	PHONENO_DATA("phoneOrTelHide",new EnumObject() {
		@Override
		public String sensitiveData(String value) {
			return SensitiveDataUtils.mobilePhone(value);
		}
	}),
	/**EMAIL�������ͱ�ʶ,������SensitiveInfoUtils.filterHide������SensitiveDataType����*/
	EMAIL_DATA("emailHide",new EnumObject() {
		@Override
		public String sensitiveData(String value) {
			return SensitiveDataUtils.email(value);
		}	
	}),
	/**��������ʶ,������SensitiveInfoUtils.filterHide������SensitiveDataType����*/
	CHINESE_NAME("chineseNamelHide",new EnumObject() {
		@Override
		public String sensitiveData(String value) {
			return SensitiveDataUtils.chineseName(value);
		}	
	});

	private String key;
	private EnumObject enumObject;
	
	private SensitiveDataEnum(String key, EnumObject enumObject) {
		this.key = key;
		this.enumObject = enumObject;
	}
	
	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}
	/**
	 * @return the enumObject
	 */
	public EnumObject getEnumObject() {
		return enumObject;
	}
	/**
	 * @param enumObject the enumObject to set
	 */
	public void setEnumObject(EnumObject enumObject) {
		this.enumObject = enumObject;
	}
	
}
