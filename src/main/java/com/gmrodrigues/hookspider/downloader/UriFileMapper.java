package com.gmrodrigues.hookspider.downloader;

import org.apache.commons.lang.StringEscapeUtils;

import java.io.File;
import java.net.URI;

public class UriFileMapper
{

    public static File uriToFile(File basedir, URI uri)
    {
        String protocol = uri.getScheme();
        String host = uri.getHost();
        int port = uri.getPort();
        if (port > 0) {
            host += ":" + port;
        }
        String path = uri.getPath();
        String query = uri.getQuery();
        if (query != null && query.length() > 0) {
            path += "?" + query;
        }
        if (path == null || "".equals(path)) {
            path = "#";
        }
        path += ".local";
        path = StringEscapeUtils.escapeXml(path);

        File file = new File(basedir,
                new File(host, new File(protocol, new File(path).getPath()).getPath())
                        .getPath());
        return file;
    }
}
