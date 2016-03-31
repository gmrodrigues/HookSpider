package com.gmrodrigues.hookspider.downloader.registry;

import com.gmrodrigues.dirutils.DirUtils;
import com.gmrodrigues.hookspider.model.DownloadedUriModel;
import com.gmrodrigues.hookspider.model.DownloaderStateModel;
import com.gmrodrigues.hookspider.model.UriScannedForLinksModel;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;

public class DownloaderRegistryTool
{
    private static DateFormat df;
    private static DecimalFormat nf;

    private static final String rootTag = "downloader_state";

    private static final String downloadedTag = "downloaded_urls";

    private static final String downloadedUrlTag = "url";

    private static final String urlFilterTag = "matched";
    private static final String urlFilterAttr = "url_filter";

    private static final String urlHrefAttr = "href";
    private static final String urlPathAttr = "path";
    private static final String urlStartedAttr = "started";
    private static final String urlFinishedAttr = "finished";
    private static final String urlBytesCopiedAttr = "bytes";
    private static final String urlDeltaTimeAttr = "delta_time";
    private static final String urlKbytesPerSeconds = "kbytes_per_second";
    private static final String urlRefererAttr = "referer";
    private static final String urlFoundWithExtractorAttr = "found_with_extractor";

    private static final String scannedTag = "scanned_for_links";
    private static final String scannedLinkExtractorUsedAttr = "link_extrator_used";
    private static final String scannedStartedAttr = "started";
    private static final String scannedFinishedAttr = "finished";
    private static final String scannedDeltaTimeAttr = "delta_time";
    private static final String scannedFoundUrlTag = "found_url";
    private static final String scannedFoundUrlHrefAttr = "href";

    private DownloaderStateModel downloaderState;
    {
        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        nf = new DecimalFormat("0.00");
        DecimalFormatSymbols separator = new DecimalFormatSymbols();
        separator.setDecimalSeparator('.');
        nf.setDecimalFormatSymbols(separator);
    }

    public DownloaderRegistryTool(DownloaderStateModel downloaderState)
    {
        this.downloaderState = downloaderState;
    }

    public DownloaderStateModel getDownloaderState()
    {
        return downloaderState;
    }

    public void setDownloaderState(DownloaderStateModel downloaderState)
    {
        this.downloaderState = downloaderState;
    }

    public void writeModelToXml(PrintStream out) throws IOException
    {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DecimalFormat nf = new DecimalFormat("0.00");
        DecimalFormatSymbols separator = new DecimalFormatSymbols();
        separator.setDecimalSeparator('.');
        nf.setDecimalFormatSymbols(separator);

        Calendar firstDownloadTime = Calendar.getInstance();
        firstDownloadTime.setTimeInMillis(Long.MAX_VALUE);
        Calendar lastDownloadTime = Calendar.getInstance();
        lastDownloadTime.setTimeInMillis(Long.MIN_VALUE);

        long bytesRetrievedCrawl = 0;
        long bytesRetrievedDownload = 0;
        long totalBytesRetrieved = 0;
        long concurrentSeconds = 0;
        long totalCrawled = 0;
        long totalDownloaded = 0;

        for (URI uri : downloaderState.getDownloadedURIs().keySet()) {
            DownloadedUriModel du = downloaderState.getDownloadedURIs()
                    .get(uri);
            if (firstDownloadTime.compareTo(du.getStartedTime()) > 0) {
                firstDownloadTime = du.getStartedTime();
            }
            if (lastDownloadTime.compareTo(du.getFinishedTime()) < 0) {
                lastDownloadTime = du.getFinishedTime();
            }
            bytesRetrievedDownload += du.getBytesCopied();
            concurrentSeconds += du.getDeltaTimeInSeconds();
            totalDownloaded++;
        }
        totalBytesRetrieved = bytesRetrievedCrawl + bytesRetrievedDownload;
        long totalRetrieved = totalCrawled + totalDownloaded;
        long elapsedMillis = lastDownloadTime.getTimeInMillis()
                - firstDownloadTime.getTimeInMillis();

        out.println("<?xml version=\"1.0\"?>");
        out.println("<" + rootTag + ">");
        out.println();
        if (totalRetrieved > 0 && totalBytesRetrieved > 0) {
            out.println(encloseInXmlTag("start_time",
                    df.format(firstDownloadTime.getTime())));
            out.println(encloseInXmlTag("finish_time",
                    df.format(lastDownloadTime.getTime())));
            out.println(encloseInXmlTag("elapsed_seconds",
                    nf.format(elapsedMillis / 1000.0)));
            out.println(encloseInXmlTag("concurrent_elapsed_seconds",
                    concurrentSeconds));
            out.println(encloseInXmlTag("bytes_retrieved",
                    nf.format(totalBytesRetrieved)));
            out.println(encloseInXmlTag("kbytes_per_second",
                    nf.format(totalBytesRetrieved / (elapsedMillis / 1000.0))));
            out.println(encloseInXmlTag("urls_crawled", totalCrawled));
            out.println(encloseInXmlTag("urls_downloaded", totalDownloaded));
            out.println(encloseInXmlTag("total_urls_retrieved", totalCrawled
                    + totalDownloaded));
            out.println(encloseInXmlTag("avg_retrieved_bytes",
                    nf.format(totalBytesRetrieved / totalRetrieved)));
            if (totalCrawled > 0 && bytesRetrievedCrawl > 0) {
                out.println(encloseInXmlTag("avg_crawled_bytes",
                        nf.format(bytesRetrievedCrawl / bytesRetrievedCrawl)));

            }
            if (totalDownloaded > 0 && bytesRetrievedDownload > 0) {
                out.println(encloseInXmlTag(
                        "avg_downloaded_bytes",
                        nf.format(bytesRetrievedDownload
                                / bytesRetrievedDownload)));

            }
        }

        out.println();
        out.println("<" + downloadedTag + ">");

        if (!downloaderState.getDownloadedURIs().isEmpty()) {
            for (URI uri : downloaderState.getDownloadedURIs().keySet()) {
                DownloadedUriModel du = downloaderState.getDownloadedURIs()
                        .get(uri);
                out.print("\n  ");
                out.print(downloadedUriFormatToXml(du,
                        downloaderState.getBaseDir()));
            }

        }

        out.println("</" + downloadedTag + ">");

        if (!downloaderState.getIgnoredURIs().isEmpty()) {
            out.println("<ignored_url>");
            for (URI uri : downloaderState.getIgnoredURIs()) {
                out.print("<url href='");
                out.print(uri.toString());
                out.println("' />");
            }
            out.println("</ignored_url>");
        }
        out.println();
        out.println("</" + rootTag + ">");
        out.flush();
    }

    private static String downloadedUriFormatToXml(DownloadedUriModel du,
                                                   File baseDir)
    {
        String relativePath = DirUtils.getRelativePath(baseDir, du.getToFile());
        Map<String, Object> urlAttrs = new LinkedHashMap<String, Object>();
        urlAttrs.put("\n    " + urlHrefAttr, du.getUri());
        urlAttrs.put("\n    " + urlPathAttr, relativePath);
        urlAttrs.put("\n    " + urlStartedAttr, df.format(du.getStartedTime().getTime()));
        urlAttrs.put("\n    " + urlFinishedAttr,
                df.format(du.getFinishedTime().getTime()));
        urlAttrs.put("\n    " + urlDeltaTimeAttr, du.getDeltaTimeInSeconds());
        urlAttrs.put("\n    " + urlBytesCopiedAttr, du.getBytesCopied());
        urlAttrs.put("\n    " + urlKbytesPerSeconds,
                nf.format(du.getCopiedBytesPerSecond() / 1024));
        if (du.getRefererUri() != null) {
            urlAttrs.put("\n    " + urlRefererAttr, du.getRefererUri());
        }
        if (du.getFoundWithExtractor() != null) {
            urlAttrs.put("\n    " + urlFoundWithExtractorAttr,
                    du.getFoundWithExtractor());
        }

        if ((du.getPassedUriTestNames() == null || du.getPassedUriTestNames()
                .isEmpty()) && !du.wasScannedForLinks()) {
            return encloseInXmlTag(downloadedUrlTag, urlAttrs);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n    ");
        for (String filterName : du.getPassedUriTestNames()) {
            Map<String, Object> urlFilterMap = new LinkedHashMap<String, Object>();
            urlFilterMap.put(urlFilterAttr, filterName);
            sb.append(encloseInXmlTag(urlFilterTag, urlFilterMap));
        }
        if (du.wasScannedForLinks()) {
            sb.append("\n    ");
            Map<String, Object> scannedMap = new LinkedHashMap<String, Object>();
            scannedMap.put("\n      " + scannedLinkExtractorUsedAttr, du.getUriScannedForLinks().getNameOfLinkExtractorUsed());
            scannedMap.put("\n      " + scannedStartedAttr, df.format(du.getUriScannedForLinks().getStartedTime().getTime()));
            scannedMap.put("\n      " + scannedFinishedAttr, df.format(du.getUriScannedForLinks().getFinishedTime().getTime()));
            scannedMap.put("\n      " + scannedDeltaTimeAttr, nf.format(du.getUriScannedForLinks().getDeltaTimeInSeconds()));
            if (du.getUriScannedForLinks().getUrisExtracted().isEmpty()) {

                sb.append(encloseInXmlTag(scannedTag, scannedMap));
            }
            else {
                StringBuilder sbUrisFound = new StringBuilder();
                for (URI foundUri : du.getUriScannedForLinks().getUrisExtracted()) {
                    Map<String, Object> uriFoundAttrs = new LinkedHashMap<String, Object>();
                    uriFoundAttrs.put(scannedFoundUrlHrefAttr, foundUri);
                    sbUrisFound.append("\n      ");
                    sbUrisFound.append(encloseInXmlTag(scannedFoundUrlTag, uriFoundAttrs));
                }
                sb.append(encloseInXmlTag(scannedTag, scannedMap, sbUrisFound));
            }
        }
        return encloseInXmlTag(downloadedUrlTag, urlAttrs, sb);
    }

    private static String encloseInXmlTag(String tagName,
                                          Map<String, Object> attrs, Object value)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(tagName);
        if (attrs != null) {
            for (String attrQName : attrs.keySet()) {
                Object attrValue = attrs.get(attrQName);
                sb.append(" ").append(attrQName).append("='");
                sb.append(attrValue);
                sb.append("'");
            }
        }
        if (value != null) {
            sb.append(">").append(value);
            sb.append("</").append(tagName).append(">");
        }
        else {
            sb.append("/>");
        }

        return sb.toString();
    }

    private static String encloseInXmlTag(String tagName, Object value)
    {
        return encloseInXmlTag(tagName, null, value);
    }

    private static String encloseInXmlTag(String tagName,
                                          Map<String, Object> attrs)
    {
        return encloseInXmlTag(tagName, attrs, null);
    }

    public DownloaderStateModel loadModelFromXml(InputStream xmlInputStream)
            throws ParserConfigurationException, SAXException, IOException
    {

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        DefaultHandler handler = new DefaultHandler()
        {

            List<String> pathElements = new LinkedList<String>();

            private DownloadedUriModel du;
            private File baseFile = downloaderState.getBaseDir();

            private Map<String, URI> uriRefs = new HashMap<String, URI>();

            String pathToDownloadedUrl;
            String pathToUrlFilter;
            String pathToScanned;
            String pathToScannedFoundUrl;

            {
                List<String> path = new ArrayList<String>();
                path.add(rootTag);
                path.add(downloadedTag);

                path.add(downloadedUrlTag);
                pathToDownloadedUrl = path.toString();

                path.add(urlFilterTag);
                pathToUrlFilter = path.toString();
                path.remove(path.size() - 1);

                path.add(scannedTag);
                pathToScanned = path.toString();

                path.add(scannedFoundUrlTag);
                pathToScannedFoundUrl = path.toString();
            }

            private URI getUriRef(String href) throws URISyntaxException
            {
                URI uri = uriRefs.get(href);
                if (uri == null) {
                    uri = new URI(href);
                    uriRefs.put(uri.toString(), uri);
                }
                return uri;
            }

            @Override
            public void startElement(String arg0, String localName,
                                     String qName, Attributes attributes) throws SAXException
            {

                pathElements.add(qName);
                String path = pathElements.toString();

                try {

                    if (pathToUrlFilter.equals(path)) {
                        String urlFilterString = attributes
                                .getValue(urlFilterAttr);
                        du.getPassedUriTestNames().add(urlFilterString);
                        UriScannedForLinksModel us = du.getUriScannedForLinks();

                    }
                    else if (pathToDownloadedUrl.equals(path)) {

                        String startedString = attributes
                                .getValue(urlStartedAttr);
                        String finishedString = attributes
                                .getValue(urlFinishedAttr);
                        String bytesCopiedString = attributes
                                .getValue(urlBytesCopiedAttr);

                        String refererString = attributes
                                .getValue(urlRefererAttr);
                        String foundWithExtractorString = attributes
                                .getValue(urlFoundWithExtractorAttr);

                        String uriString = attributes.getValue(urlHrefAttr);
                        URI uri = getUriRef(uriString);
                        String pathString = attributes.getValue(urlPathAttr);
                        File toFile = new File(baseFile, pathString);
                        URI referer = null;
                        if (refererString != null) {
                            referer = getUriRef(refererString);
                        }

                        Calendar startedTime = Calendar.getInstance();
                        Calendar finishedTime = Calendar.getInstance();

                        startedTime.setTime(df.parse(startedString));
                        finishedTime.setTime(df.parse(finishedString));

                        long bytesCopied = new Long(bytesCopiedString);

                        du = new DownloadedUriModel(uri, toFile);
                        du.setStartedTime(startedTime);
                        du.setFinishedTime(finishedTime);
                        du.setBytesCopied(bytesCopied);
                        du.setRefererUri(referer);
                        du.setFoundWithExtractor(foundWithExtractorString);
                        downloaderState.getDownloadedURIs()
                                .put(du.getUri(), du);
                    }
                    else if (pathToScannedFoundUrl.equals(path)) {
                        UriScannedForLinksModel us = du.getUriScannedForLinks();
                        String foundHref = attributes.getValue(scannedFoundUrlHrefAttr);
                        URI foundUri = getUriRef(foundHref);
                        us.getUrisExtracted().add(foundUri);
                    }
                    else if (pathToScanned.equals(path)) {
                        String linkExtratorUsed = attributes.getValue(scannedLinkExtractorUsedAttr);
                        String startedString = attributes.getValue(scannedStartedAttr);
                        String finishedString = attributes.getValue(scannedStartedAttr);

                        Calendar startedTime = Calendar.getInstance();
                        Calendar finishedTime = Calendar.getInstance();
                        startedTime.setTime(df.parse(startedString));
                        finishedTime.setTime(df.parse(finishedString));

                        UriScannedForLinksModel us = new UriScannedForLinksModel();
                        us.setUri(du.getUri());
                        us.setNameOfLinkExtractorUsed(linkExtratorUsed);
                        us.setStartedTime(startedTime);
                        us.setFinishedTime(finishedTime);
                        du.setUriScannedForLinks(us);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }

            @Override
            public void endElement(String uri, String localName, String qName)
                    throws SAXException
            {
                pathElements.remove(pathElements.size() - 1);
            }
        };
        saxParser.parse(xmlInputStream, handler);
        return null;
    }
}
