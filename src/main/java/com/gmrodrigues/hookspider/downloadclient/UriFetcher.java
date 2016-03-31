package com.gmrodrigues.hookspider.downloadclient;

import org.apache.http.client.ClientProtocolException;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class UriFetcher
{

    private String username;
    private String password;

    public void copy(URI uri, File toFile) throws ClientProtocolException, IOException
    {
        DownloadClient client = null;
        String scheme = uri.getScheme();
        if ("http".equals(scheme) || "https".equals(scheme)) {
            client = new HTTPClient();
        }
        else if ("sftp".equals(scheme) || "scp".equals(scheme)
                || "ssh".endsWith(scheme)) {
            client = new ScpClient();
        }
        else if ("ftp".equals(scheme) || "ftps".equals(scheme)) {
            client = new FtpClient();
        }
        else if ("file".equals(uri.getScheme())) {
            client = new LocalfileClient();
        }
        client.setUsername(username);
        client.setPassword(password);
        client.copy(uri, toFile);
        return;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
}
