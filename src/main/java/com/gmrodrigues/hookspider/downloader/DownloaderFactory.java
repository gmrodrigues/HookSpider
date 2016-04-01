package com.gmrodrigues.hookspider.downloader;

import com.gmrodrigues.hookspider.linkextractor.LinkExtrator;
import com.gmrodrigues.hookspider.linkextractor.LinkExtratorJavascript;
import com.gmrodrigues.hookspider.linkextractor.LinkExtratorXpath;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloaderFactory
{

    private Downloader downloader;
    private File confFile;
    private File baseDir;

    Map<String, LinkExtrator> scriptsMap = new HashMap<String, LinkExtrator>();

    public Downloader newInstance()
    {
        if (downloader == null) {
            downloader = new Downloader(baseDir);
        }
        return downloader;
    }

    public void load() throws IOException, ConfigurationException
    {
        if (baseDir == null) {
            baseDir = confFile.getCanonicalFile().getParentFile();
        }
        downloader = new Downloader(baseDir);

        XMLConfiguration config = new XMLConfiguration();
        config.setDelimiterParsingDisabled(true);
        config.load(confFile);

        loadScripts(config);
        loadFilters(config);
        loadAuth(config);
    }

    private void loadFilters(XMLConfiguration config)
    {
        Map<String, NamedUriTester> uriFilters = new HashMap<String, NamedUriTester>();
        String filterTag = "url_filter";
        String extractLinksTag = "extract_links";
        String copyContentsTag = "copy_contents";

        List urlFiltersConf = config.getList(filterTag);

        if (!urlFiltersConf.isEmpty()) {
            for (int i = 0; i < urlFiltersConf.size(); i++) {
                String name = config.getString(filterTag + "(" + i + ")[@id]")
                        .trim();
                String testerLines = urlFiltersConf.get(i).toString().trim();
                List<String> tests = new ArrayList<String>();
                for (String line : testerLines.split("\n")) {
                    line = line.trim();
                    if (line.isEmpty()) {
                        continue;
                    }
                    tests.add(line);
                }
                if (tests.isEmpty()) {
                    continue;
                }

                NamedUriTester nt = new NamedUriTester();
                nt.setName(name);
                nt.setTestStrings(tests);
                uriFilters.put(nt.getName(), nt);
            }
        }

        NamedUriTestListTester crawlListTester = downloader.getConfig().getCrawlTester();
        int crawlConfs = config.getMaxIndex(extractLinksTag) + 1;
        for (int i = 0; i < crawlConfs; i++) {
            String index = extractLinksTag + "(" + i + ")";
            String urlFilterId = config.getString(index + "[@if_url_filter]");
            if (urlFilterId != null) {
                NamedUriTester uriTester = uriFilters.get(urlFilterId);
                if (uriTester == null) {
                    throw new RuntimeException(extractLinksTag
                            + ": unknown url_filter id '" + urlFilterId + "'");
                }
                crawlListTester.getUriTestList().add(uriTester);
            }
            String linkExtractorId = config.getString(index + "[@use]");
            if (linkExtractorId != null && !linkExtractorId.isEmpty()) {
                LinkExtrator extractor = scriptsMap.get(linkExtractorId);
                if (extractor == null) {
                    throw new RuntimeException(extractLinksTag
                            + ": unknown link_extractor id '" + linkExtractorId
                            + "'");
                }
                downloader.getConfig().getFilterToScriptMap().put(urlFilterId, extractor);
            }

            String from_href = config.getString(index + "[@from_href]");
            if (from_href != null && !from_href.isEmpty()) {
                addToExtractLinksQueue(from_href, linkExtractorId);
            }
        }

        NamedUriTestListTester copylListTester = downloader.getConfig().getDownloadTester();
        int copyConfs = config.getMaxIndex(copyContentsTag) + 1;
        for (int i = 0; i < copyConfs; i++) {
            String index = copyContentsTag + "(" + i + ")";
            String urlFilterId = config.getString(index + "[@url_filter]");
            if (urlFilterId != null) {
                NamedUriTester uriTester = uriFilters.get(urlFilterId);
                if (uriTester == null) {
                    throw new RuntimeException(copyContentsTag
                            + ": unknown url_filter id '" + urlFilterId + "'");
                }
                copylListTester.getUriTestList().add(uriTester);
            }
            String href = config.getString(index + "[@from_href]");
            if (href != null && !href.isEmpty()) {
                addToCopyContentsQueue(href);
            }
        }
    }

    private void loadAuth(XMLConfiguration config)
    {
        String username = config.getString("auth[@user]");
        String password = config.getString("auth[@pwd]");
        if (username == null || password == null) {
            return;
        }
        downloader.getConfig().getAuth().setUsername(username);
        downloader.getConfig().getAuth().setPassword(password);
    }

    private void loadScripts(XMLConfiguration config)
    {
        scriptsMap.clear();
        String extractorTag = "link_extractor";
        int extratorConfs = config.getMaxIndex(extractorTag) + 1;
        for (int i = 0; i < extratorConfs; i++) {
            String index = extractorTag + "(" + i + ")";
            String type = config.getString(index + "[@type]");
            String name = config.getString(index + "[@id]");
            String content = config.getString(index);
            if (content != null) {
                content = content.trim();
            }

            if (type == null || type.isEmpty()) {
                throw new RuntimeException("Script #" + i
                        + " must have a 'id' attribute");
            }
            if (name == null || name.isEmpty()) {
                throw new RuntimeException("Script #" + i + " with id='" + name
                        + "' must have a 'id' attribute");
            }
            if (content == null || content.isEmpty()) {
                throw new RuntimeException("Script #" + i + " with id='" + name
                        + "' must have content");
            }

            if (type != null) {
                type = type.trim();
                LinkExtrator evaltor = null;
                if ("javascript".equalsIgnoreCase(type)) {
                    evaltor = new LinkExtratorJavascript(name, content);
                }
                else if ("xpath1.0".equalsIgnoreCase(type)) {
                    evaltor = new LinkExtratorXpath(name, content);
                }
                if (evaltor == null) {
                    throw new RuntimeException("Could not instantiate a "
                            + extractorTag + " of type " + type);
                }
                scriptsMap.put(name, evaltor);
            }
        }
    }


    private void addToCopyContentsQueue(String href)
    {
        try {
            URI uri = new URI(href);
            downloader.getState().addToDownloadQueue(uri);
        }
        catch (URISyntaxException e) {
            throw new RuntimeException("Could not convert to uri: " + href);
        }
    }

    private void addToExtractLinksQueue(String href, String linkExtractorId)
    {
        try {
            URI uri = new URI(href);
            downloader.getState().addToCrawlQueue(uri);
            if (linkExtractorId == null || linkExtractorId.isEmpty()) {
                return;
            }
            LinkExtrator evaltor = scriptsMap.get(linkExtractorId);
            downloader.getConfig().getUriCrawlScripts().put(uri, evaltor);
        }
        catch (URISyntaxException e) {
            throw new RuntimeException("Could not convert to uri: " + href);
        }
    }


    // getters and setters
    public File getConfFile()
    {
        return confFile;
    }

    public void setConfFile(File confFile)
    {
        this.confFile = confFile;
    }

    public File getBaseDir()
    {
        return baseDir;
    }

    public void setBaseDir(File baseDir)
    {
        this.baseDir = baseDir;
    }
}
