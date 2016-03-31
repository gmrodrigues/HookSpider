package com.gmrodrigues.hookspider.scrapper;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

public class ScrapperFilter
{
    private final Set<String> matchedUrlFilters = new LinkedHashSet<String>();
    private final Set<String> refererUrlFilters = new LinkedHashSet<String>();
    private URI referer;
    private String foundWithExtractor;

    public URI getReferer()
    {
        return referer;
    }

    public void setReferer(URI referer)
    {
        this.referer = referer;
    }

    public String getFoundWithExtractor()
    {
        return foundWithExtractor;
    }

    public void setFoundWithExtractor(String foundWithExtractor)
    {
        this.foundWithExtractor = foundWithExtractor;
    }

    public Set<String> getMatchedUrlFilters()
    {
        return matchedUrlFilters;
    }

    public Set<String> getRefererUrlFilters()
    {
        return refererUrlFilters;
    }
}