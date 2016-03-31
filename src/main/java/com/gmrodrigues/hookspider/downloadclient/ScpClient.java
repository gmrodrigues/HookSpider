package com.gmrodrigues.hookspider.downloadclient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.apache.commons.io.IOUtils;

import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.sftp.SftpFile;
import com.sshtools.j2ssh.sftp.SftpFileInputStream;
import com.sshtools.j2ssh.sftp.SftpSubsystemClient;
import com.sshtools.j2ssh.transport.ConsoleKnownHostsKeyVerification;
import com.sshtools.j2ssh.transport.InvalidHostFileException;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;


class ScpClient implements DownloadClient {

	private boolean isUnknowHostKeyAllowed = true;
	private boolean isMismatchKeyAllowed = false;

	public String username;
	public String password;

	@Override
	public void copy(URI uri, File toFile) throws IOException {
		String server = uri.getHost();
		String filename = uri.getPath();
		int port = uri.getPort();
		if (port < 1) {
			port = 22;
		}

		SshClient sshclient = new SshClient();
		sshclient.connect(server, port, new MyHostsKeyVerification());
		
		PasswordAuthenticationClient pwdclient = new PasswordAuthenticationClient();
		pwdclient.setUsername(username);
		pwdclient.setPassword(password);

		int result = sshclient.authenticate(pwdclient);
		if (result != AuthenticationProtocolState.COMPLETE) {
			throw new RuntimeException("SSH Server '" + server
					+ "' auth failed");
		}

		SftpSubsystemClient sftpSubsystemClient = sshclient.openSftpChannel();
		SftpFile file = sftpSubsystemClient.openFile(filename, SftpSubsystemClient.OPEN_READ);
		
		InputStream in = new SftpFileInputStream(file);
		OutputStream out = new FileOutputStream(toFile);
		long startTime = System.currentTimeMillis();
		IOUtils.copy(in, out);
		IOUtils.closeQuietly(in);
		IOUtils.closeQuietly(out);
		long finishTime = System.currentTimeMillis();
		long bytes = toFile.length();

		
//		
//		final int bufferSize = 1024*1024;
//		BufferedInputStream bufIn = new BufferedInputStream(in,bufferSize);
//		BufferedWriter buffOut = new BufferedWriter(new OutputStreamWriter(out), bufferSize);
//		
//		int c;            
//        while ((c = bufIn.read()) != -1) {
//        	buffOut.write(c);
//        }
	
//		SftpClient sftpclient = sshclient.openSftpClient();	
//		sftpclient.get(filename,out);

		sshclient.disconnect();
	}

	public String getUsername() {
		return username;
	}

	@Override
	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public void setPassword(String password) {
		this.password = password;
	}

	class MyHostsKeyVerification extends ConsoleKnownHostsKeyVerification {

		public MyHostsKeyVerification() throws InvalidHostFileException {
			super();
		}

		@Override
		public void onHostKeyMismatch(String s, SshPublicKey sshpublickey,
				SshPublicKey sshpublickey1) {
			if (isMismatchKeyAllowed) {
				try {
					allowHost(s, sshpublickey, false);
				} catch (InvalidHostFileException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				throw new RuntimeException("Host Key mismatch for host"+s);
			}
		}

		@Override
		public void onUnknownHost(String s, SshPublicKey sshpublickey) {
			if (isUnknowHostKeyAllowed) {
				try {
					allowHost(s, sshpublickey, false);
				} catch (InvalidHostFileException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				throw new RuntimeException("Host Key unknown for host "+s);
			}
		}

	}

}
