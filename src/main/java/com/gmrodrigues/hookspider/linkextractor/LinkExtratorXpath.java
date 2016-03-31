package com.gmrodrigues.hookspider.linkextractor;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LinkExtratorXpath implements LinkExtrator
{

    private final String xpathString;
    private final String name;

    public LinkExtratorXpath(String name, String xpathString)
    {
        this.xpathString = xpathString;
        this.name = name;
    }

    @Override
    public List<URI> getUris(File file, String encode, URI baseUri) throws IOException
    {
        List<URI> results = new ArrayList<URI>();
        try {
            HtmlCleaner cleaner = new HtmlCleaner();
            TagNode node;
            if (encode != null) {
                node = cleaner.clean(file, encode);
            }
            else {
                node = cleaner.clean(file);
            }
            Object[] nodes;
            nodes = node.evaluateXPath(xpathString);
            for (Object o : nodes) {
                try {
                    URL url = new URL(baseUri.toURL(), o.toString().trim());
                    results.add(url.toURI());
                }
                catch (URISyntaxException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        catch (XPatherException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return results;
    }

    @Override
    public String getName()
    {
        return name;
    }
}
