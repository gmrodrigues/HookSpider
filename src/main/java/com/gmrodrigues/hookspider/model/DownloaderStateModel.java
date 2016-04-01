package com.gmrodrigues.hookspider.model;

import com.gmrodrigues.hookspider.downloader.UriFileMapper;
import com.gmrodrigues.hookspider.utils.Uris;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class DownloaderStateModel
{
    private File baseDir;

    private Queue<URI> crawlQueue;
    private Queue<URI> downloadQueue;
    private Map<URI, DownloadedUriModel> downloadedURIs;
    private HashSet<URI> ignoredURIs;
    private AlreadyKnownUris alreadyKnownURIs;
    private Map<URI, URI> referers;
    private Map<URI, String> foundWithExtractor;

    private final Object uriQueueLock = new Object();

    public DownloaderStateModel()
    {
        crawlQueue = new LinkedList<URI>();
        downloadQueue = new LinkedList<URI>();
        ;
        downloadedURIs = new ConcurrentHashMap<URI, DownloadedUriModel>();
        ignoredURIs = new HashSet<URI>();
        alreadyKnownURIs = new AlreadyKnownUris();
        referers = new ConcurrentHashMap<URI, URI>();
        foundWithExtractor = new ConcurrentHashMap<URI, String>();
    }

    public boolean downloadedAll()
    {
        synchronized (uriQueueLock) {
            return (crawlQueue.size() + downloadQueue.size()) < 1;
        }
    }

    public void addUri(URI uri, DownloaderConfigModel config)
    {
        synchronized (uriQueueLock) {
            List<String> passedCrawlTestNames = config.getCrawlTester()
                    .getPassedTestsNames(uri);

            if (!passedCrawlTestNames.isEmpty()
                    && !config.getCrawlTester().getUriTestList().isEmpty()) {
                addToCrawlQueue(uri);
                return;
            }
            else {
                List<String> passedDownloadTestNames = config.getDownloadTester()
                        .getPassedTestsNames(uri);
                if (!passedDownloadTestNames.isEmpty()) {
                    addToDownloadQueue(uri);
                    return;
                }
            }
            alreadyKnownURIs.add(uri);
            ignoredURIs.add(uri);
        }
    }

    public void addToDownloadQueue(URI uri)
    {
        if (alreadyKnownURIs.isKnown(uri)) {
            return;
        }
        alreadyKnownURIs.add(uri);
        downloadQueue.add(uri);
    }

    public void addToCrawlQueue(URI uri)
    {
        if (alreadyKnownURIs.isKnown(uri)) {
            return;
        }
        alreadyKnownURIs.add(uri);
        crawlQueue.add(uri);
    }

    public void markAsDownloaded(DownloadedUriModel downloaded)
    {
        downloadedURIs.put(downloaded.getUri(), downloaded);
    }

    public void markAsScannedForLinks(UriScannedForLinksModel scanned)
    {
        URI scannedURI = scanned.getUri();
        if (!downloadedURIs.containsKey(scannedURI)) {
            throw new RuntimeException("Before calling doneLinkExtraction for " + scannedURI + ", you must first call doneDownload for this URI");
        }
        DownloadedUriModel downloaded = downloadedURIs.get(scannedURI);
        downloaded.setUriScannedForLinks(scanned);
        for (URI foundUri : scanned.getUrisExtracted()) {
            referers.put(foundUri, scannedURI);
            foundWithExtractor.put(foundUri, scanned.getNameOfLinkExtractorUsed());
        }
    }

    public URI getRefererUriFromUri(URI uri)
    {
        return referers.get(uri);
    }

    public String getExtratorUsedToFindUri(URI uri)
    {
        return foundWithExtractor.get(uri);
    }

    public URI pollUriFromDownloadQueue()
    {
        synchronized (uriQueueLock) {
            return downloadQueue.poll();
        }
    }

    public URI pollUriFromCrawlQueue()
    {
        synchronized (uriQueueLock) {
            return crawlQueue.poll();
        }
    }

    public File getFileMappedFromUri(URI uri)
    {
        return UriFileMapper.uriToFile(baseDir, uri);
    }

    // Getters & Setters

    public File getBaseDir()
    {
        return baseDir;
    }

    public void setBaseDir(File baseDir)
    {
        this.baseDir = baseDir;
    }

    public Queue<URI> getCrawlQueue()
    {
        return crawlQueue;
    }

    public void setCrawlQueue(Queue<URI> crawlQueue)
    {
        this.crawlQueue = crawlQueue;
    }

    public Queue<URI> getDownloadQueue()
    {
        return downloadQueue;
    }

    public void setDownloadQueue(Queue<URI> downloadQueue)
    {
        this.downloadQueue = downloadQueue;
    }

    public Map<URI, DownloadedUriModel> getDownloadedURIs()
    {
        return downloadedURIs;
    }

    public void setDownloadedURIs(Map<URI, DownloadedUriModel> downloadedURIs)
    {
        this.downloadedURIs = downloadedURIs;
    }

    public HashSet<URI> getIgnoredURIs()
    {
        return ignoredURIs;
    }

    public void setIgnoredURIs(HashSet<URI> ignoredURIs)
    {
        this.ignoredURIs = ignoredURIs;
    }

    public Map<URI, URI> getReferers()
    {
        return referers;
    }

    public void setReferers(Map<URI, URI> referers)
    {
        this.referers = referers;
    }

    public Map<URI, String> getFoundWithExtractor()
    {
        return foundWithExtractor;
    }

    public void setFoundWithExtractor(Map<URI, String> foundWithExtractor)
    {
        this.foundWithExtractor = foundWithExtractor;
    }

    public Object getUriQueueLock()
    {
        return uriQueueLock;
    }

    private class AlreadyKnownUris {
        private Set<URI> uris = new HashSet<URI>();
        public void add(URI uri){
            uris.add(Uris.removeFragment(uri));
        }
        public boolean isKnown(URI uri){
            return uris.contains(Uris.removeFragment(uri));
        }
    }

    @Override
    public String toString()
    {
        return "DownloaderStateModel [crawlQueue=" + crawlQueue
                + ", downloadQueue=" + downloadQueue + ", downloadedURIs="
                + downloadedURIs + ", ignoredURIs=" + ignoredURIs
                + ", alreadyKnownURIs=" + alreadyKnownURIs + ", referers="
                + referers + ", foundWithExtractor=" + foundWithExtractor
                + ", uriQueueLock=" + uriQueueLock + "]";
    }
}