package com.gmrodrigues.hookspider.model;

import java.io.File;
import java.net.URI;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class DownloadedUriModel
{
    private final URI uri;
    private final File toFile;

    private URI refererUri;
    private String foundWithExtractor;

    private Calendar startedTime;
    private Calendar finishedTime;
    private long bytesCopied;
    private Set<String> passedUriTestNames = new HashSet<String>();

    private UriScannedForLinksModel uriScannedForLinksModel;

    public DownloadedUriModel(URI uri, File toFile)
    {
        this.uri = uri;
        this.toFile = toFile;
    }

    public URI getUri()
    {
        return uri;
    }

    public File getToFile()
    {
        return toFile;
    }

    public Calendar getStartedTime()
    {
        return startedTime;
    }

    public void setStartedTime(Calendar startedTime)
    {
        this.startedTime = startedTime;
    }

    public Calendar getFinishedTime()
    {
        return finishedTime;
    }

    public void setFinishedTime(Calendar finishedTime)
    {
        this.finishedTime = finishedTime;
    }

    public long getBytesCopied()
    {
        return bytesCopied;
    }

    public void setBytesCopied(long bytesDownloaded)
    {
        this.bytesCopied = bytesDownloaded;
    }

    public double getDeltaTimeInSeconds()
    {
        long startTime = startedTime.getTimeInMillis();
        long finishTime = finishedTime.getTimeInMillis();
        double durationInSeconds = (finishTime - startTime) / 1000.0;
        return durationInSeconds;
    }

    public double getCopiedBytesPerSecond()
    {
        double copiedBytesPerSecond = bytesCopied / getDeltaTimeInSeconds();
        return copiedBytesPerSecond;
    }

    public boolean wasScannedForLinks()
    {
        return uriScannedForLinksModel != null;
    }

    public Set<String> getPassedUriTestNames()
    {
        return passedUriTestNames;
    }

    public void setPassedUriTestNames(Set<String> passedUriTestNames)
    {
        this.passedUriTestNames = passedUriTestNames;
    }

    public URI getRefererUri()
    {
        return refererUri;
    }

    public void setRefererUri(URI refererUri)
    {
        this.refererUri = refererUri;
    }

    public String getFoundWithExtractor()
    {
        return foundWithExtractor;
    }

    public void setFoundWithExtractor(String foundWithExtractor)
    {
        this.foundWithExtractor = foundWithExtractor;
    }

    public UriScannedForLinksModel getUriScannedForLinks()
    {
        return uriScannedForLinksModel;
    }

    public void setUriScannedForLinks(UriScannedForLinksModel uriScannedForLinks)
    {
        if (uriScannedForLinks == null) {
            this.uriScannedForLinksModel = null;
            return;
        }
        uriScannedForLinks.setUri(uri);
        this.uriScannedForLinksModel = uriScannedForLinks;
        return;
    }
}
