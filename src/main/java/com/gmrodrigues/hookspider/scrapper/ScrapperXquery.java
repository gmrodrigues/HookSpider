package com.gmrodrigues.hookspider.scrapper;

import com.gmrodrigues.hookspider.model.DownloadedUriModel;
import com.gmrodrigues.hookspider.scrapper.html.HtmlNormalizer;
import com.gmrodrigues.hookspider.scrapper.xquery.XqueryEvaluator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.xquery.XQException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ScrapperXquery
{
    private final String name;
    private String xquery;
    private String rootTag;
    private OutputStream outStream;
    private boolean useHtmlCleaner;

    private final ScrapperFilter filter = new ScrapperFilter();

    private Map<URI, DownloadedUriModel> uris = new LinkedHashMap<URI, DownloadedUriModel>();


    public ScrapperXquery(String name)
    {
        this.name = name;
    }

    public void scrap() throws SAXException, IOException
    {
        XqueryEvaluator xqe;
        try {
            xqe = new XqueryEvaluator();
        }
        catch (XQException e) {
            throw new RuntimeException("Error creating XqueryEvaluator: "
                    + e.getMessage());
        }

        OutputStream out = outStream;
        if (out == null) {
            out = System.out;
        }

        writeRootOpenTag(out);

        for (URI uri : uris.keySet()) {
            DownloadedUriModel du = uris.get(uri);
            boolean passedFilter = true;
            if (!filter.getMatchedUrlFilters().isEmpty()) {
                passedFilter = false;
                testFilterLoop:
                for (String uriFilterId : filter.getMatchedUrlFilters()) {
                    for (String passed : du.getPassedUriTestNames()) {
                        if (uriFilterId.equalsIgnoreCase(passed)) {
                            passedFilter = true;
                            break testFilterLoop;
                        }
                    }
                }
                if (!passedFilter) {
                    continue;
                }
            }
            Map<String, Object> varMap = new HashMap<String, Object>();
            File docFile = du.getToFile();
            Document doc = HtmlNormalizer.getCleanW3cDomDocumentFromFile(docFile);
            insertContextNode(du, doc);
            varMap.put(".", doc);
            try {
                xqe.eval(xquery, varMap, out);
            }
            catch (StackOverflowError e) {
                e.printStackTrace();
                System.exit(0);
            }
            catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }

        writeRootCloseTag(out);
    }

    private void writeRootOpenTag(OutputStream out) throws IOException
    {
        out.write("<?xml version=\"1.0\"?>\n".getBytes());
        out.write("<".getBytes());
        out.write(rootTag.getBytes());
        out.write(">\n".getBytes());
    }

    private void writeRootCloseTag(OutputStream out) throws IOException
    {
        out.write("</".getBytes());
        out.write(rootTag.getBytes());
        out.write(">\n".getBytes());
    }

    private void insertContextNode(DownloadedUriModel du, Document doc)
    {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DecimalFormat nf = new DecimalFormat("0.00");
        DecimalFormatSymbols separator = new DecimalFormatSymbols();
        separator.setDecimalSeparator('.');
        nf.setDecimalFormatSymbols(separator);

        Element contextNode = doc.createElement("context");
        Element urlNode = doc.createElement("url");
        contextNode.appendChild(urlNode);
        doc.getDocumentElement().appendChild(contextNode);
        urlNode.setAttribute("href", du.getUri().toString());
        urlNode.setAttribute("path", du.getToFile().toString());
        urlNode.setAttribute("started", df.format(du.getStartedTime().getTime()));
        urlNode.setAttribute("finished", df.format(du.getFinishedTime().getTime()));
        urlNode.setAttribute("delta_time_in_seconds", nf.format(du.getDeltaTimeInSeconds()));
        urlNode.setAttribute("bytes_copied",
                new Long(du.getBytesCopied()).toString());
        urlNode.setAttribute("elapsed_seconds", nf.format(du.getDeltaTimeInSeconds()));
        urlNode.setAttribute("kbytes_per_second",
                nf.format(du.getDeltaTimeInSeconds()));
        urlNode.setAttribute("crawled", new Boolean(du.wasScannedForLinks()).toString());
        if (du.getRefererUri() != null) {
            urlNode.setAttribute("referer", du.getRefererUri().toString());
        }
        if (du.getFoundWithExtractor() != null) {
            urlNode.setAttribute("found_with_extractor",
                    du.getFoundWithExtractor());
        }
        for (String urlFilterId : du.getPassedUriTestNames()) {
            Element matched = doc.createElement("matched");
            matched.setAttribute("url_filter", urlFilterId);
            urlNode.appendChild(matched);
        }

        return;
    }

    public ScrapperFilter getFilter()
    {
        return filter;
    }

    public String getName()
    {
        return name;
    }

    public String getXquery()
    {
        return xquery;
    }

    public void setXquery(String xquery)
    {
        this.xquery = xquery;
    }

    public String getRootTag()
    {
        return rootTag;
    }

    public void setRootTag(String rootTag)
    {
        this.rootTag = rootTag;
    }

    public OutputStream getOutStream()
    {
        return outStream;
    }

    public void setOutStream(OutputStream outStream)
    {
        this.outStream = outStream;
    }

    public boolean isUseHtmlCleaner()
    {
        return useHtmlCleaner;
    }

    public void setUseHtmlCleaner(boolean useHtmlCleaner)
    {
        this.useHtmlCleaner = useHtmlCleaner;
    }

    public Map<URI, DownloadedUriModel> getUris()
    {
        return uris;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof ScrapperXquery)) {
            return false;
        }
        ScrapperXquery sxObj = (ScrapperXquery) obj;
        return getName().equals(sxObj.getName());
    }
}
