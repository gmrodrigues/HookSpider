package com.gmrodrigues.hookspider.downloadclient;


import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URI;

class LocalfileClient implements DownloadClient {

	

	@Override
	public void setUsername(String username) {
	}

	@Override
	public void setPassword(String password) {
	}

	

	@Override
	public void copy(URI uri, File toFile) throws IOException
	{
		InputStream in  = new FileInputStream(new File(uri.getPath()));
		OutputStream out = new FileOutputStream(toFile);
		long startTime = System.currentTimeMillis();
		IOUtils.copy(in, out);
		IOUtils.closeQuietly(in);
		IOUtils.closeQuietly(out);
		long finishTime = System.currentTimeMillis();
		long bytes = toFile.length();
		
		}

	

}
