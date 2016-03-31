package com.gmrodrigues.hookspider.downloadclient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;


class FtpClient implements DownloadClient {

	public String username;
	public String password;
	
	@Override
	public void copy(URI uri, File toFile) throws IOException {
		String server = uri.getHost();
		String filename = uri.getPath();// .substring(1);
		int port = uri.getPort();
		String protocol = uri.getScheme();

		FTPClient ftpclient;
		if ("ftp".equalsIgnoreCase(protocol)) {
			ftpclient = new FTPClient();
		} else if ("ftps".equalsIgnoreCase(protocol)) {
			ftpclient = new FTPSClient();
		} else {
			throw new RuntimeException("Unsuported ftp protocol: " + protocol);
		}

		ftpclient.addProtocolCommandListener(new PrintCommandListener(
				new PrintWriter(System.err)));

		if (port > 0) {
			ftpclient.connect(server, port);
		} else {
			ftpclient.connect(server);
		}

		System.out.println("port " + port);

		int replycode = ftpclient.getReplyCode();
		if (!FTPReply.isPositiveCompletion(replycode)) {
			ftpclient.disconnect();
			throw new RuntimeException("FTP Server '" + server
					+ "' connection refused");
		} else {
			System.out.println("Connected to " + server);
		}

		if (!ftpclient.login(username, password)) {
			throw new RuntimeException("FTP Server '" + server
					+ "' login failed");
		}

		ftpclient.enterLocalPassiveMode();
		ftpclient.setFileType(FTP.BINARY_FILE_TYPE);

		String[] files = ftpclient.listNames();
		System.out.println(files);

		OutputStream out = new FileOutputStream(toFile);
		InputStream  in  = ftpclient.retrieveFileStream(filename);
		long startTime = System.currentTimeMillis();
		IOUtils.copy(in, out);
		IOUtils.closeQuietly(in);
		IOUtils.closeQuietly(out);
		long finishTime = System.currentTimeMillis();
		long bytes = toFile.length();
		ftpclient.logout();
	}

	public String getUsername() {
		return username;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see DownloadClient#setUsername(java.lang.String)
	 */
	@Override
	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see DownloadClient#setPassword(java.lang.String)
	 */
	@Override
	public void setPassword(String password) {
		this.password = password;
	}

}
