package com.gmrodrigues.hookspider.downloader;

public class NamedUriTester extends UriTester
{
    private String name;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return "NamedUriTester [getName()=" + getName()
                + ", getTestStringsList()=" + getTestStringsList() + "]";
    }
}
