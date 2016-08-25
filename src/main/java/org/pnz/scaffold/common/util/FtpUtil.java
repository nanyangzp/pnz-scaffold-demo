package org.pnz.scaffold.common.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ֧�ֶϵ�������FTPʵ����
 * 
 */
public class FtpUtil {
	public FTPClient ftpClient = new FTPClient();
	/** �ı����͹������ڷ������˵���ʱǰ׺ */
	private String temp_OP;
	/** �ı����͹������ڷ������˵���ʱ��׺ */
	private String temp_ED;
	private static Logger logger = LoggerFactory.getLogger(FtpUtil.class);

	public FtpUtil() {
		this.temp_ED = "";
		this.temp_OP = "";
		// ���ý�������ʹ�õ����������������̨
		this.ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
	}

	/**
	 * ��ǰ׺�ͺ�׺�Ĺ��췽��
	 * 
	 * @param op
	 *            ǰ׺
	 * @param ed
	 *            ��׺
	 */
	public FtpUtil(String op, String ed) {
		this.temp_OP = op;
		this.temp_ED = ed;
		this.ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
	}

	/**
	 * ���ӵ�FTP������
	 * 
	 * @param hostname
	 *            ������
	 * @param port
	 *            �˿�
	 * @param username
	 *            �û���
	 * @param password
	 *            ����
	 * @return boolean �Ƿ����ӳɹ�
	 * @throws IOException
	 */
	public boolean connect(String hostname, int port, String username, String password) throws IOException {
		ftpClient.connect(hostname, port);
		ftpClient.setControlEncoding("UTF-8");
		if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
			if (ftpClient.login(username, password)) {
				return true;
			}
		}
		disconnect();
		return false;
	}

	/**
	 * �Ͽ���Զ�̷�����������
	 * 
	 * @throws IOException
	 */
	public void disconnect() throws IOException {
		if (ftpClient.isConnected()) {
			ftpClient.disconnect();
		}
	}

	/**
	 * ��FTP�������������ļ�,֧�ֶϵ��������ϴ��ٷֱȻ㱨(�����ļ������ļ�����)
	 * 
	 * @param remote
	 *            Զ���ļ���·��
	 * @param local
	 *            �����ļ���·��
	 * @return ���سɹ��ļ��б�
	 * @throws IOException
	 */
	public String[] download(String remote, String local) throws IOException {
		// ���ñ���ģʽ
		ftpClient.enterLocalPassiveMode();
		// �����Զ����Ʒ�ʽ����
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		List<String> results = new ArrayList<String>();

		// ���Զ���ļ��Ƿ����
		FTPFile[] files = ftpClient.listFiles(new String(remote.getBytes("GBK"), "iso-8859-1"));

		for (int i = 0; i < files.length; i++) {
			String remoteFile = files[i].getName();
			String localFile = files[i].getName();
			if ((!"".equals(temp_OP) && remoteFile.startsWith(temp_OP))
					|| (!"".equals(temp_ED) && remoteFile.endsWith(temp_ED))) {
				// ��ʱ�ļ�������
				continue;
			}
			DownloadStatus downloadStatus = downloadFile(remote + remoteFile, local + localFile);
			if (downloadStatus == DownloadStatus.Download_From_Break_Success
					|| downloadStatus == DownloadStatus.Download_New_Success) {
				results.add(remoteFile);
			} else {
				logger.error("�����ļ�" + remoteFile + "״̬:" + downloadStatus.toString());
			}
		}
		return results.toArray(new String[results.size()]);
	}

	/**
	 * ��FTP�������������ļ�,֧�ֶϵ��������ϴ��ٷֱȻ㱨(�����ļ�)
	 * 
	 * @param remote
	 *            Զ���ļ�·��
	 * @param local
	 *            �����ļ�·��
	 * @return DownloadStatus �ϴ���״̬
	 * @throws IOException
	 */
	public DownloadStatus downloadFile(String remote, String local) throws IOException {
		// ���ñ���ģʽ
		ftpClient.enterLocalPassiveMode();
		// �����Զ����Ʒ�ʽ����
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		DownloadStatus result;

		// ���Զ���ļ��Ƿ����
		FTPFile[] files = ftpClient.listFiles(new String(remote.getBytes("GBK"), "iso-8859-1"));
		if (files.length != 1) {
			logger.info("Զ���ļ�������");
			return DownloadStatus.Remote_File_Noexist;
		}

		long lRemoteSize = files[0].getSize();
		File f = new File(local);
		// ���ش����ļ������жϵ�����
		if (f.exists()) {
			long localSize = f.length();
			// �жϱ����ļ���С�Ƿ����Զ���ļ���С
			if (localSize >= lRemoteSize) {
				logger.info("�����ļ�����Զ���ļ���������ֹ");
				return DownloadStatus.Local_Bigger_Remote;
			}

			// ���жϵ�����������¼״̬
			FileOutputStream out = null;
			InputStream in = null;
			try {
				out = new FileOutputStream(f, true);
				ftpClient.setRestartOffset(localSize);
				in = ftpClient.retrieveFileStream(new String(remote.getBytes("GBK"), "iso-8859-1"));
				byte[] bytes = new byte[1024];
				long step = lRemoteSize / 100;
				long process = localSize / step;
				int c;
				while ((c = in.read(bytes)) != -1) {
					out.write(bytes, 0, c);
					localSize += c;
					long nowProcess = localSize / step;
					if (nowProcess > process) {
						process = nowProcess;
						if (process % 50 == 0)
							logger.info("���ؽ��ȣ�" + process);
						// �����ļ����ؽ���,ֵ�����process������
					}
				}
			} catch (Exception e) {
				logger.error("�ļ�����ʧ��", e);
			} finally {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
			}
			boolean isDo = ftpClient.completePendingCommand();
			if (isDo) {
				logger.info("�ϵ������ļ��ɹ�");
				result = DownloadStatus.Download_From_Break_Success;
			} else {
				logger.info("�ϵ������ļ�ʧ��");
				result = DownloadStatus.Download_From_Break_Failed;
			}
		} else {
			OutputStream out = null;
			InputStream in = null;
			try {
				out = new FileOutputStream(f);
				in = ftpClient.retrieveFileStream(new String(remote.getBytes("GBK"), "iso-8859-1"));
				byte[] bytes = new byte[1024];
				long step = lRemoteSize / 100;
				long process = 0;
				long localSize = 0L;
				int c;
				while ((c = in.read(bytes)) != -1) {
					out.write(bytes, 0, c);
					localSize += c;
					long nowProcess = localSize / step;
					if (nowProcess > process) {
						process = nowProcess;
						if (process % 50 == 0)
							logger.info("���ؽ��ȣ�" + process + "%");
						// �����ļ����ؽ���,ֵ�����process������
					}
				}
			} catch (Exception e) {
				logger.error("�ļ�����ʧ��", e);
			} finally {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
			}
			boolean upNewStatus = ftpClient.completePendingCommand();
			if (upNewStatus) {
				logger.info("ȫ�������ļ��ɹ�");
				result = DownloadStatus.Download_New_Success;
			} else {
				logger.info("ȫ�������ļ�ʧ��");
				result = DownloadStatus.Download_New_Failed;
			}
		}
		return result;
	}

	/**
	 * �ϴ��ļ���FTP��������֧�ֶϵ�����
	 * 
	 * @param local
	 *            �����ļ����ƣ�����·��
	 * @param remote
	 *            Զ���ļ�·����ʹ��/home/directory1/subdirectory/file.ext��
	 *            ����Linux�ϵ�·��ָ����ʽ��֧�ֶ༶Ŀ¼Ƕ�ף�֧�ֵݹ鴴�������ڵ�Ŀ¼�ṹ
	 * @return UploadStatus �ϴ����
	 * @throws Exception
	 */
	public UploadStatus upload(String local, String remote) throws Exception {
		// ����PassiveMode����
		ftpClient.enterLocalPassiveMode();
		// �����Զ��������ķ�ʽ����
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		ftpClient.setControlEncoding("UTF-8");
		UploadStatus result;
		// ��Զ��Ŀ¼�Ĵ���
		String remoteFileName = remote;
		if (remote.contains("/")) {
			remoteFileName = remote.substring(remote.lastIndexOf("/") + 1);
			// ����������Զ��Ŀ¼�ṹ������ʧ��ֱ�ӷ���
			if (createDirecroty(remote, ftpClient) == UploadStatus.Create_Directory_Fail) {
				logger.info("Զ�̷�������ӦĿ¼����ʧ��");
				return UploadStatus.Create_Directory_Fail;
			}
		}

		// ���Զ���Ƿ�����ļ�
		FTPFile[] files = ftpClient.listFiles(new String(remoteFileName.getBytes("UTF-8"), "iso-8859-1"));
		if (files.length == 1) {
			long remoteSize = files[0].getSize();
			File f = new File(local);
			long localSize = f.length();
			if (remoteSize == localSize) {
				logger.info("�ļ��Ѿ�����");
				return UploadStatus.File_Exits;
			} else if (remoteSize > localSize) {
				logger.info("Զ���ļ����ڱ����ļ�");
				return UploadStatus.Remote_Bigger_Local;
			}

			// �����ƶ��ļ��ڶ�ȡָ��,ʵ�ֶϵ�����
			result = uploadFile(remoteFileName, f, ftpClient, remoteSize);

			// ����ϵ�����û�гɹ�����ɾ�����������ļ��������ϴ�
			if (result == UploadStatus.Upload_From_Break_Failed) {
				if (!ftpClient.deleteFile(remoteFileName)) {
					logger.info("ɾ��Զ���ļ�ʧ��");
					return UploadStatus.Delete_Remote_Faild;
				}
				result = uploadFile(remoteFileName, f, ftpClient, 0);
			}
		} else {
			result = uploadFile(remoteFileName, new File(local), ftpClient, 0);
		}
		// �������ļ�
		String newFileName = remoteFileName.replace(this.temp_OP, "").replace(this.temp_ED, "");
		if (!remoteFileName.equals(newFileName) && (result.equals(UploadStatus.Upload_New_File_Success)
				|| result.equals(UploadStatus.Upload_From_Break_Success)))
			this.alterFileName(remoteFileName, newFileName);
		return result;
	}

	/**
	 * �ݹ鴴��Զ�̷�����Ŀ¼
	 * 
	 * @param remote
	 *            Զ�̷������ļ�����·��
	 * @param ftpClient
	 *            FTPClient����
	 * @return UploadStatus Ŀ¼�����Ƿ�ɹ�
	 * @throws IOException
	 */
	public UploadStatus createDirecroty(String remote, FTPClient ftpClient) throws IOException {
		UploadStatus status = UploadStatus.Create_Directory_Success;
		String directory = remote.substring(0, remote.lastIndexOf("/") + 1);
		if (!directory.equalsIgnoreCase("/")
				&& !ftpClient.changeWorkingDirectory(new String(directory.getBytes("UTF-8"), "iso-8859-1"))) {
			// ���Զ��Ŀ¼�����ڣ���ݹ鴴��Զ�̷�����Ŀ¼
			int start = 0;
			int end = 0;
			if (directory.startsWith("/")) {
				start = 1;
			} else {
				start = 0;
			}
			end = directory.indexOf("/", start);
			while (true) {
				String subDirectory = new String(remote.substring(start, end).getBytes("UTF-8"), "iso-8859-1");
				if (!ftpClient.changeWorkingDirectory(subDirectory)) {
					if (ftpClient.makeDirectory(subDirectory)) {
						ftpClient.changeWorkingDirectory(subDirectory);
					} else {
						logger.info("����Ŀ¼ʧ��");
						return UploadStatus.Create_Directory_Fail;
					}
				}

				start = end + 1;
				end = directory.indexOf("/", start);

				// �������Ŀ¼�Ƿ񴴽����
				if (end <= start) {
					break;
				}
			}
		}
		return status;
	}

	/**
	 * �ϴ��ļ���������,���ϴ��Ͷϵ�����
	 * 
	 * @param remoteFile
	 *            Զ���ļ��������ϴ�֮ǰ�Ѿ�������������Ŀ¼���˸ı�
	 * @param localFile
	 *            �����ļ�File���������·��
	 * @param processStep
	 *            ��Ҫ��ʾ�Ĵ�����Ȳ���ֵ
	 * @param ftpClient
	 *            FTPClient����
	 * @return UploadStatus
	 * @throws IOException
	 */
	public UploadStatus uploadFile(String remoteFile, File localFile, FTPClient ftpClient, long remoteSize)
			throws IOException {
		UploadStatus status;
		// ��ʾ���ȵ��ϴ�
		double step = localFile.length() / 100.0;
		double process = 0;
		double localreadbytes = 0L;
		RandomAccessFile raf = new RandomAccessFile(localFile, "r");
		OutputStream out = ftpClient.appendFileStream(new String(remoteFile.getBytes("UTF-8"), "iso-8859-1"));
		// �ϵ�����
		if (remoteSize > 0) {
			ftpClient.setRestartOffset(remoteSize);
			process = remoteSize / step;
			raf.seek(remoteSize);
			localreadbytes = remoteSize;
		}
		byte[] bytes = new byte[1024];
		int c;
		while ((c = raf.read(bytes)) != -1) {
			out.write(bytes, 0, c);
			localreadbytes += c;

			if ((localreadbytes / step) != process) {
				process = localreadbytes / step;
				if (process % 50 == 0)
					logger.info("�ϴ�����:" + (int) process + "%"); // �㱨�ϴ�״̬
			}
			// if (!Maths.isEqual(localreadbytes / step, process)) {
			// process = localreadbytes / step;
			// if (process%50 == 0)
			// logger.info("�ϴ�����:" + (int)process + "%"); // �㱨�ϴ�״̬
			// }
		}
		out.flush();
		raf.close();
		out.close();
		boolean result = ftpClient.completePendingCommand();
		if (remoteSize > 0) {
			status = result ? UploadStatus.Upload_From_Break_Success : UploadStatus.Upload_From_Break_Failed;
			if (result) {
				logger.info("�ϵ������ɹ�");
			} else {
				logger.info("�ϵ�����ʧ��");
			}
		} else {
			status = result ? UploadStatus.Upload_New_File_Success : UploadStatus.Upload_New_File_Failed;
			if (result) {
				logger.info("�ϴ����ļ��ɹ�");
			} else {
				logger.info("�ϴ����ļ�ʧ��");
			}
		}
		return status;
	}

	// ö����UploadStatus����
	public enum UploadStatus {
		Fail, Create_Directory_Fail, // Զ�̷�������ӦĿ¼����ʧ��
		Create_Directory_Success, // Զ�̷���������Ŀ¼�ɹ�
		Upload_New_File_Success, // �ϴ����ļ��ɹ�
		Upload_New_File_Failed, // �ϴ����ļ�ʧ��
		File_Exits, // �ļ��Ѿ�����
		Remote_Bigger_Local, // Զ���ļ����ڱ����ļ�
		Upload_From_Break_Success, // �ϵ������ɹ�
		Upload_From_Break_Failed, // �ϵ�����ʧ��
		Delete_Remote_Faild, // ɾ��Զ���ļ�ʧ��
		Empty_File_Upload; // �ϴ����ļ�
	}

	// ö����DownloadStatus����
	public enum DownloadStatus {
		Remote_File_Noexist, // Զ���ļ�������
		Local_Bigger_Remote, // �����ļ�����Զ���ļ�
		Download_From_Break_Success, // �ϵ������ļ��ɹ�
		Download_From_Break_Failed, // �ϵ������ļ�ʧ��
		Download_New_Success, // ȫ�������ļ��ɹ�
		Download_New_Failed; // ȫ�������ļ�ʧ��
	}

	public boolean deleteFile(String pathName) throws IOException {
		return ftpClient.deleteFile(pathName);
	}

	public String alterFileName(String oldName, String newName) throws Exception {
		FTPFile[] files = ftpClient.listFiles(new String(newName.getBytes("UTF-8"), "iso-8859-1"));
		if (files.length == 1) {
			deleteFile(oldName);
			UploadStatus.File_Exits.toString();
		} else if (ftpClient.rename(new String(oldName.getBytes("UTF-8"), "iso-8859-1"),
				new String(newName.getBytes("UTF-8"), "iso-8859-1")))
			return UploadStatus.Upload_New_File_Success.toString();
		return UploadStatus.Fail.toString();
	}

	public boolean changeDirectory(String path) throws IOException {
		return ftpClient.changeWorkingDirectory(path);
	}

	public boolean createDirectory(String pathName) throws IOException {
		return ftpClient.makeDirectory(pathName);
	}

	public boolean removeDirectory(String path) throws IOException {
		return ftpClient.removeDirectory(path);
	}

	public boolean removeDirectory(String path, boolean isAll) throws IOException {
		if (!isAll) {
			return removeDirectory(path);
		}
		FTPFile[] ftpFileArr = ftpClient.listFiles(path);
		if (ftpFileArr == null || ftpFileArr.length == 0) {
			return removeDirectory(path);
		}
		for (FTPFile ftpFile : ftpFileArr) {
			String name = ftpFile.getName();
			if (ftpFile.isDirectory()) {
				logger.info("��ɾ��·����[" + path + "/" + name + "]");
				removeDirectory(path + "/" + name, true);
			} else {
				logger.info("��ɾ���ļ���[" + path + "/" + name + "]");
				deleteFile(path + "/" + name);
			}
		}
		return ftpClient.removeDirectory(path);
	}

	public boolean existDirectory(String path) throws IOException {
		boolean flag = false;
		FTPFile[] ftpFileArr = ftpClient.listFiles(path);
		for (FTPFile ftpFile : ftpFileArr) {
			if (ftpFile.isDirectory() && ftpFile.getName().equalsIgnoreCase(path)) {
				flag = true;
				break;
			}
		}
		return flag;
	}

	public String getTemp_OP() {
		return temp_OP;
	}

	public void setTemp_OP(String temp_OP) {
		this.temp_OP = temp_OP;
	}

	public String getTemp_ED() {
		return temp_ED;
	}

	public void setTemp_ED(String temp_ED) {
		this.temp_ED = temp_ED;
	}

	// ����main����
	// --------------------------------------------------------------------------------------------------
	public static void main(String[] args) {
		FtpUtil myFtp = new FtpUtil("temp_", "");
		try {
//			myFtp.connect("192.168.45.15", 80, "longly", "longltly");
			myFtp.connect(InetAddress.getLocalHost().getHostAddress(), 80, "longly", "longltly");
			// myFtp.ftpClient.makeDirectory(new
			// String("mmm".getBytes("UTF-8"),"iso-8859-1"));
			// myFtp.ftpClient.removeDirectory(new
			// String("test20090806".getBytes("UTF-8"),"iso-8859-1"));
			// myFtp.ftpClient.changeWorkingDirectory(new
			// String("test20090806".getBytes("GBK"),"iso-8859-1"));
			System.out.println(
					myFtp.upload("D:\\XPADS-201-20150731-SpotDeals.deal", "/ftp/XPADS-201-20150828-SpotDeals.deal"));
			// System.out.println(myFtp.download("SystemOut.log","E:\\SystemOut.log"));
			// myFtp.download("/u0/eibc/xpad/WMS/", "G:\\WMSDATAGZ\\XPAD\\");
			myFtp.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("����FTP����" + e.getMessage());
		}
	}

}
