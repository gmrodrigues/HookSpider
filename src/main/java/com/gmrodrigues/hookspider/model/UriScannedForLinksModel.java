package com.gmrodrigues.hookspider.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class UriScannedForLinksModel
{
    private Calendar startedTime;
    private Calendar finishedTime;

    private URI uri;
    private String nameOfLinkExtractorUsed;
    private final List<URI> urisExtracted = new ArrayList<URI>();

    public double getDeltaTimeInSeconds()
    {
        if (startedTime == null || finishedTime == null) {
            throw new RuntimeException("You must provide both startedTime and finishedTime to getDeltaTimeInSeconds()");
        }
        long startTime = startedTime.getTimeInMillis();
        long finishTime = finishedTime.getTimeInMillis();
        double durationInSeconds = (finishTime - startTime) / 1000.0;
        return durationInSeconds;
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

    public URI getUri()
    {
        return uri;
    }

    public void setUri(URI uri)
    {
        this.uri = uri;
    }

    public String getNameOfLinkExtractorUsed()
    {
        return nameOfLinkExtractorUsed;
    }

    public void setNameOfLinkExtractorUsed(String nameOfLinkExtractorUsed)
    {
        this.nameOfLinkExtractorUsed = nameOfLinkExtractorUsed;
    }

    public List<URI> getUrisExtracted()
    {
        return urisExtracted;
    }
}
