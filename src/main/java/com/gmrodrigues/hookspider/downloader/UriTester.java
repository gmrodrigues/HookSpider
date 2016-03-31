package com.gmrodrigues.hookspider.downloader;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class UriTester
{

    private List<String> testStrings = new ArrayList<String>();
    private Map<String, Pattern> patterns = new HashMap<String, Pattern>();

    public List<String> getTestStringsList()
    {
        return testStrings;
    }

    public void setTestStrings(List<String> testStrings)
    {
        this.testStrings = testStrings;
    }

    private Map<String, String> getUriQueryMap(URI uri)
    {
        Map<String, String> map = new HashMap<String, String>();
        String query = uri.getQuery();
        if (query == null || query.isEmpty()) {
            return map;
        }
        String[] params = query.split("&");
        for (String param : params) {
            String[] nameValue = param.split("=");
            if (nameValue.length > 1) {
                map.put(nameValue[0].trim(), nameValue[1].trim());
            }
            else {
                map.put(nameValue[0].trim(), "");
            }
        }
        return map;
    }

    public boolean evalUriTestStrings(URI uri)
    {
        boolean result = true;
        for (String testString : testStrings) {
            result &= evalTestString(uri, testString);
            if (!result) {
                return false;
            }
        }
        return true;
    }

    public boolean evalTestString(URI uri, String testString)
    {
        if (testString.startsWith("!")) {
            return !evalTestString(uri, testString.substring(1));
        }
        if (testString.startsWith("@")) {
            return evalHostTestString(uri, testString);
        }
        if (testString.startsWith("/")) {
            return evalPathTestString(uri, testString);
        }
        if (testString.startsWith(".")) {
            return evalFileExtensionTestString(uri, testString);
        }
        if (testString.startsWith("?")) {
            return evalQuerystringTestString(uri, testString);
        }
        return false;
    }

    private boolean evalFileExtensionTestString(URI uri, String testString)
    {
        return false;
    }

    private boolean evalPathTestString(URI uri, String testString)
    {
        String arg = testString.substring("/".length()).trim();
        String path = uri.getPath();
        return match(arg, path);
    }

    private boolean match(String pattern, String value)
    {
        if (pattern.startsWith("m{") && pattern.endsWith("}")) {
            pattern = pattern.substring("m{".length(), pattern.length() - "}".length());
            Pattern pat = getPattern(pattern);
            boolean matched = pat.matcher(value).find();
            return matched;
        }
        return pattern.equals(value);
    }

    private Pattern getPattern(String arg)
    {
        if (patterns.containsKey(arg)) {
            return patterns.get(arg);
        }
        Pattern pat = Pattern.compile(arg);
        patterns.put(arg, pat);
        return pat;
    }

    private boolean evalQuerystringTestString(URI uri, String testString)
    {
        String arg = testString.substring("?".length()).trim();
        int equalsSignIndex = arg.indexOf("=");
        int argLength = arg.length();
        String paramName = null;
        String paramValue = null;
        if (equalsSignIndex < 0) {
            paramName = arg;
        }
        else {
            paramName = arg.substring(0, equalsSignIndex);
            if (equalsSignIndex < argLength) {
                paramValue = arg.substring(equalsSignIndex + 1);
            }
            else {
                paramValue = "";
            }
        }
        Map<String, String> paramMap = getUriQueryMap(uri);
        if (paramValue == null) {
            return paramMap.containsKey(paramName);
        }
        String uriParamValue = paramMap.get(paramName);
        boolean equals = paramValue.equals(uriParamValue);
        return equals;
    }

    private boolean evalHostTestString(URI uri, String testString)
    {
        String arg = testString.substring("@".length()).trim();
        return arg.equals(uri.getHost());
    }
}
