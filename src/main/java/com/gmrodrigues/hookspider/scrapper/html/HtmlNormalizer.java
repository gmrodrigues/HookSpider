package com.gmrodrigues.hookspider.scrapper.html;

import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class HtmlNormalizer
{

    public static Document getCleanW3cDomDocumentFromInputSource(InputSource inputSource) throws SAXException, IOException
    {
        DOMParser parser = new DOMParser();
        parser.parse(inputSource);
        Document doc = parser.getDocument();
        return doc;
    }

    public static Document getCleanW3cDomDocumentFromInputStream(InputStream inputStream) throws SAXException, IOException
    {
        InputSource is = new InputSource(inputStream);
        return getCleanW3cDomDocumentFromInputSource(is);
    }

    public static Document getCleanW3cDomDocumentFromFile(File file) throws SAXException, IOException
    {
        InputStream in = new FileInputStream(file);
        return getCleanW3cDomDocumentFromInputStream(in);
    }
}
