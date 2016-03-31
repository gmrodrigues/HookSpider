package com.gmrodrigues.hookspider.downloadclient;

import org.apache.http.client.ClientProtocolException;

import java.io.File;
import java.io.IOException;
import java.net.URI;

interface DownloadClient
{

    public abstract void setUsername(String username);

    public abstract void setPassword(String password);

    public void copy(URI uri, File toFile) throws ClientProtocolException, IOException;
}