package org.pnz.scaffold.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * �ļ��Ŀ���Util
 * 
 * @author zhangGB
 */
public class FileUtility extends FileUtils {
	protected static final Log log = LogFactory.getLog(FileUtility.class);

	/**
	 * ��Դ�ļ�������ָ��Ŀ¼����������
	 * @param srcFile
	 * @param destDir
	 * @param newFileName
	 * @return
	 */
	@SuppressWarnings("resource")
	public static long copyFile(File srcFile, File destDir, String newFileName) {
		long copySizes = 0;
		if (!srcFile.exists())//���Դ�ļ�������
		{
			copySizes = -1;
			log.error("Դ�ļ���" + "[->]" + srcFile.getName() + "������");
		}
		else if (!destDir.exists())//���Ŀ��Ŀ¼������
		{
			log.error("Ŀ��Ŀ¼��" + "[->]" + destDir.getName() + "������");
			try {
				log.error("Ŀ��Ŀ¼��" + "[->]" + destDir.getName() + "������......");
				destDir.createNewFile();
				log.error("Ŀ��Ŀ¼��" + "[->]" + destDir.getName() + "�����ɹ�!");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				copySizes = -1;
				log.error("Ŀ��Ŀ¼��" + "[->]" + destDir.getName() + "����ʧ��!" + e.getMessage());
			}
		}
		else if (newFileName == null)//���Դ�ļ�Ϊ�� 
		{
			copySizes = -1;
			log.error("���ļ���Ϊ��" + "[->]" + newFileName);
		}
		else 
		{
			try 
			{
				//ʹ��nio����ʽ�����ļ��Ŀ���
				FileChannel fcin = new FileInputStream(srcFile).getChannel();
				FileChannel fcout = new FileOutputStream(new File(destDir, newFileName)).getChannel();
				long size = fcin.size();
				fcin.transferTo(0, fcin.size(), fcout);
				fcin.close();
				fcout.close();
				copySizes = size;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				copySizes = -1;
				log.error("�ļ�û���ҵ���" + "[->]" + e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
				copySizes = -1;
				log.error("IO�쳣��" + "[->]" + e.getMessage());
			}
		}
		return copySizes;
	}
}
