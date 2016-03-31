package com.gmrodrigues.hookspider.linkextractor;

import com.gmrodrigues.js.sandbox.JsSandboxConcurrentEvaluator;
import com.gmrodrigues.js.sandbox.JsSandboxEvaluator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LinkExtratorJavascript implements LinkExtrator
{

    private final String scriptName;
    private final String javascriptSourceCode;

    private final JsSandboxEvaluator jsSandbox;


    public LinkExtratorJavascript(String scriptName, String javascriptSourceCode)
    {
        this.scriptName = scriptName;
        this.javascriptSourceCode = javascriptSourceCode;
        jsSandbox = JsSandboxConcurrentEvaluator.newInstance();
        jsSandbox.setScriptName(scriptName);
        jsSandbox.setSource(javascriptSourceCode);
    }


    @Override
    public List<URI> getUris(File file, String encode, URI baseUri) throws IOException
    {
        List links = new ArrayList<String>();
        Document doc = Jsoup.parse(file, encode, baseUri.toString());
        jsSandbox.putVar("doc", doc);
        jsSandbox.putVar("links", links);
        jsSandbox.exec();
        links = jsSandbox.<List<String>>getVar("links");
//		if(result instanceof List){
//			links =  (List)result;
//		}else if (result instanceof Iterable){
//			Iterator it = ((Iterable)result).iterator();
//			links = new ArrayList();
//			while(it.hasNext()){
//				links.add(it.next());
//			}
//		}else{
//			links = new ArrayList();
//			links.add(result.toString());
//		}

        List<URI> uris = new ArrayList<URI>();
        for (Object link : links) {
            URI uri;
            try {
                uri = new URL(baseUri.toURL(), link.toString()).toURI();
            }
            catch (URISyntaxException e) {
                System.err.println(e.getMessage());
                continue;
            }
            uris.add(uri);
        }
        return uris;
    }


    @Override
    public String getName()
    {
        return scriptName;
    }
}
