package com.gmrodrigues.hookspider.linkextractor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

public interface LinkExtrator
{

    public String getName();

    public abstract List<URI> getUris(File file, String encode, URI baseUri)
            throws IOException;

}