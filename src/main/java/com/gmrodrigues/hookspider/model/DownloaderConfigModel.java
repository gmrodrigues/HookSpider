package com.gmrodrigues.hookspider.model;

import com.gmrodrigues.hookspider.downloader.Auth;
import com.gmrodrigues.hookspider.downloader.NamedUriTestListTester;
import com.gmrodrigues.hookspider.linkextractor.LinkExtrator;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DownloaderConfigModel
{
    private NamedUriTestListTester crawlTester;
    private NamedUriTestListTester downloadTester;
    private Map<String, LinkExtrator> filterToScriptMap;
    private Map<URI, LinkExtrator> uriCrawlScripts;
    private Auth auth;

    public DownloaderConfigModel()
    {
        crawlTester = new NamedUriTestListTester();
        downloadTester = new NamedUriTestListTester();
        filterToScriptMap = new HashMap<String, LinkExtrator>();
        uriCrawlScripts = new HashMap<URI, LinkExtrator>();
        auth = new Auth();
    }


    public LinkExtrator findLinkExtractorForUri(URI uri, DownloaderConfigModel config)
    {
        List<String> passedCrawlTestNames = config.getCrawlTester()
                .getPassedTestsNames(uri);

        LinkExtrator extractor = null;
        if (passedCrawlTestNames.isEmpty()) {
            if (config.getUriCrawlScripts() != null) {
                extractor = config.getUriCrawlScripts().get(uri);
            }
        }
        else {
            extractor = config.getFilterToScriptMap().get(passedCrawlTestNames.get(0));
        }
        return extractor;
    }


    // Getters & Setters

    public NamedUriTestListTester getCrawlTester()
    {
        return crawlTester;
    }

    public void setCrawlTester(NamedUriTestListTester crawlTester)
    {
        this.crawlTester = crawlTester;
    }

    public NamedUriTestListTester getDownloadTester()
    {
        return downloadTester;
    }

    public void setDownloadTester(NamedUriTestListTester downloadTester)
    {
        this.downloadTester = downloadTester;
    }

    public Map<String, LinkExtrator> getFilterToScriptMap()
    {
        return filterToScriptMap;
    }

    public void setFilterToScriptMap(Map<String, LinkExtrator> filterToScriptMap)
    {
        this.filterToScriptMap = filterToScriptMap;
    }

    public Map<URI, LinkExtrator> getUriCrawlScripts()
    {
        return uriCrawlScripts;
    }

    public void setUriCrawlScripts(Map<URI, LinkExtrator> uriCrawlScripts)
    {
        this.uriCrawlScripts = uriCrawlScripts;
    }

    public Auth getAuth()
    {
        return auth;
    }

    public void setAuth(Auth auth)
    {
        this.auth = auth;
    }

    @Override
    public String toString()
    {
        return "DownloaderConfigModel [getCrawlTester()=" + getCrawlTester()
                + ", getDownloadTester()=" + getDownloadTester()
                + ", getFilterToScriptMap()=" + getFilterToScriptMap()
                + ", getUriCrawlScripts()=" + getUriCrawlScripts()
                + ", getAuth()=" + getAuth() + "]";
    }

}