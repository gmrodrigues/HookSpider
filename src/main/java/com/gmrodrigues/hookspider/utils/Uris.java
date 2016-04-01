package com.gmrodrigues.hookspider.utils;

import java.net.URI;
import java.net.URISyntaxException;

public class Uris
{
    private Uris()
    {
    }

    public static URI removeFragment(URI uri)
    {
        try {
            return new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), null);
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
