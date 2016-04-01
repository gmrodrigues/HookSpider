package com.gmrodrigues.hookspider.scrapper;

import com.gmrodrigues.hookspider.model.DownloadedUriModel;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class ScrapperFactory
{
    private static final String scrapperTag = "scrapper_foreach";
    Map<String, ScrapperXquery> scrapperXquery = new HashMap<String, ScrapperXquery>();

    public void loadFromConfigInputStreamAndDownloadedUrisMap(File baseDir, InputStream configInputStream, Map<URI, DownloadedUriModel> downloadedUrisMap) throws FileNotFoundException
    {

        XMLConfiguration xconfig = new XMLConfiguration();
        xconfig.setDelimiterParsingDisabled(true);
        try {
            xconfig.load(configInputStream);
        }
        catch (ConfigurationException e) {
            throw new RuntimeException("Failed loading config: " + e.getMessage());
        }

        int scrapperConfs = xconfig.getMaxIndex(scrapperTag) + 1;
        for (int i = 0; i < scrapperConfs; i++) {
            String index = scrapperTag + "(" + i + ")";

            String name = xconfig.getString(index + "[@id]");
            if (name == null || name.isEmpty()) {
                throw new RuntimeException("Config file refers scrapper without id");
            }

            String scrapperBody = xconfig.getString(index);
            if (scrapperBody == null || scrapperBody.isEmpty()) {
                throw new RuntimeException("Config file refers scrapper without a body: " + name);
            }

            String outFileName = xconfig.getString(index + "[@out_file]");
            boolean htmlCleaning = xconfig.getBoolean(index + "[@html_cleaning]", true);
            String matched = xconfig.getString(index + "[@matched]");
            String foundWithExtractor = xconfig.getString(index + "[@found_with_extractor]");
            String referer = xconfig.getString(index + "[@referer]");
            String refererMatched = xconfig.getString(index + "[@referer_matched]");

            String type = xconfig.getString(index + "[@type]");
            if ("xquery1.0".equalsIgnoreCase(type)) {
                String rootTag = xconfig.getString(index + "[@root_tag]", name.replaceAll("[^\\w]", "_"));
                ScrapperXquery sx = new ScrapperXquery(name);
                sx.setXquery(scrapperBody);
                sx.setUseHtmlCleaner(htmlCleaning);
                sx.getFilter().setFoundWithExtractor(foundWithExtractor);
                if (referer != null) {
                    try {
                        sx.getFilter().setReferer(new URI(referer));
                    }
                    catch (URISyntaxException e) {
                        throw new RuntimeException("Bad formed url: " + referer
                                + " " + e.getMessage());
                    }
                }
                sx.setRootTag(rootTag);
                if (matched != null) {
                    String[] urlFiltersIds = matched.split("\\s*[,;]\\s*");
                    for (String urlFilterId : urlFiltersIds) {
                        sx.getFilter().getMatchedUrlFilters().add(urlFilterId);
                    }
                }
                if (refererMatched != null) {
                    String[] urlFiltersIds = refererMatched.split("\\s*[,;]\\s*");
                    for (String urlFilterId : urlFiltersIds) {
                        sx.getFilter().getRefererUrlFilters().add(urlFilterId);
                    }
                }
                if (outFileName != null && !outFileName.isEmpty()) {
                    File outFile = new File(baseDir, outFileName);
                    OutputStream out = new FileOutputStream(outFile);
                    sx.setOutStream(out);
                }
                sx.getUris().putAll(downloadedUrisMap);
                scrapperXquery.put(sx.getName(), sx);
            }
            else {
                throw new RuntimeException("Config file refers unknown scrapper type: " + type);
            }

        }
    }

    public void loadFromConfigFileAndDownloadedUrisMap(File baseDir, File configFile, Map<URI, DownloadedUriModel> downloadedUrisMap) throws FileNotFoundException
    {
        InputStream configInputStream = new FileInputStream(configFile);
        loadFromConfigInputStreamAndDownloadedUrisMap(baseDir, configInputStream, downloadedUrisMap);
    }

    public void loadFromConfigInputStreamAndDownloadedUrisList(File baseDir, InputStream configInputStream, Iterable<DownloadedUriModel> downloadedUrisList) throws FileNotFoundException
    {
        Map<URI, DownloadedUriModel> downloadedUrisMap = new HashMap<URI, DownloadedUriModel>();
        for (DownloadedUriModel du : downloadedUrisList) {
            downloadedUrisMap.put(du.getUri(), du);
        }
        loadFromConfigInputStreamAndDownloadedUrisMap(baseDir, configInputStream, downloadedUrisMap);
    }

    public void loadFromConfigFileAndDownloadedUrisList(File baseDir, File configFile, Iterable<DownloadedUriModel> downloadedUrisList) throws FileNotFoundException
    {
        InputStream configInputStream = new FileInputStream(configFile);
        loadFromConfigInputStreamAndDownloadedUrisList(baseDir, configInputStream, downloadedUrisList);
    }

    public Map<String, ScrapperXquery> getInstancesMap()
    {
        return scrapperXquery;
    }

    public ScrapperXquery getInstanceByName(String name)
    {
        return scrapperXquery.get(name);
    }
}
