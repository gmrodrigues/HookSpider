package com.gmrodrigues.hookspider.downloader;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class NamedUriTestListTester
{
    private List<NamedUriTester> uriTestList = new ArrayList<NamedUriTester>();

    public List<NamedUriTester> getUriTestList()
    {
        return uriTestList;
    }

    public void setUriTestList(List<NamedUriTester> uriTestList)
    {
        this.uriTestList = uriTestList;
    }

    public List<String> getPassedTestsNames(URI uri)
    {
        List<String> passedTestsNames = new ArrayList<String>();
        for (NamedUriTester test : uriTestList) {
            boolean passedTest = test.evalUriTestStrings(uri);
            if (passedTest) {
                passedTestsNames.add(test.getName());
            }
        }
        return passedTestsNames;
    }

    @Override
    public String toString()
    {
        return "NamedUriTestListTester [getUriTestList()=" + getUriTestList()
                + "]";
    }


}
