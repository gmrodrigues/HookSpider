package com.gmrodrigues.hookspider.downloadclient;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

import java.io.*;
import java.net.URI;


class HTTPClient implements DownloadClient
{

    private static ThreadSafeClientConnManager cm;

    {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(
                new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        schemeRegistry.register(
                new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
        cm = new ThreadSafeClientConnManager(schemeRegistry);
    }

    @Override
    public void copy(URI uri, File toFile) throws ClientProtocolException, IOException
    {


//		HttpClient httpclient = new DefaultHttpClient(cm);
//		HttpGet httpget = new HttpGet(uri);
//		HttpResponse response = httpclient.execute(httpget);
//
//		HttpEntity entity = response.getEntity();
//		
//		if (entity != null) {
//			InputStream in = entity.getContent();
        InputStream in = uri.toURL().openStream();
        OutputStream out = new FileOutputStream(toFile);
        IOUtils.copy(in, out);
        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);
//		}
    }

    @Override
    public void setUsername(String username)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setPassword(String password)
    {
        // TODO Auto-generated method stub

    }
}