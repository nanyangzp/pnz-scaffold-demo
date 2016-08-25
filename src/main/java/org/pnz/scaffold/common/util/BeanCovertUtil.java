package org.pnz.scaffold.common.util;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

public class BeanCovertUtil {
	private static Logger LOGGER = LoggerFactory.getLogger(BeanCovertUtil.class);
	 /**
     * bean����֮���ת��
     * @param <T> Ŀ������
     * @param source ��Դ
     * @param targetClass Ŀ������
     * @return Ŀ��
     */
    public static <T> T beanCovert(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        T target = null;
        try {
            target = targetClass.newInstance();
        } catch (Exception e) {
            LOGGER.error("�����ʼ������", e);
        }
        BeanUtils.copyProperties(source, target);
        return target;
    }
    /**
     * bean�б�֮���ת��
     * @param <T> Ŀ������
     * @param sourceList Դ�б�
     * @param targetClass Ŀ������
     * @return Ŀ���б�
     * @throws Exception �쳣
     */
    public static <T> List<T> listCovert(List<?> sourceList, Class<T> targetClass) {
        List<T> targetList = new ArrayList<T>();
        for (Object source : sourceList) {
            if (source != null) {
                T target = null;
                try {
                    target = targetClass.newInstance();
                } catch (Exception e) {
                    LOGGER.error("�����ʼ������", e);
                }
                BeanUtils.copyProperties(source, target);
                targetList.add(target);
            }
        }
        return targetList;
    }
}
