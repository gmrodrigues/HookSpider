package com.gmrodrigues.hookspider.scrapper.xquery;

import net.sf.saxon.xqj.SaxonXQDataSource;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.xquery.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class XqueryEvaluator
{

    private XQDataSource ds;
    private XQConnection xqc;

    public XqueryEvaluator() throws XQException
    {
        ds = new SaxonXQDataSource();
        xqc = ds.getConnection();
    }

    public void eval(String xquery, Map<String, Object> extVars,
                     OutputStream out) throws XQException, IOException, SAXException
    {

        XQExpression xqe = xqc.createExpression();

        if (extVars != null) {
            bindExtVarsMap(extVars, xqe);
        }

        XQSequence xqs = xqe.executeQuery(xquery);
        if (out != null) {
            xqs.writeSequence(out, null);
            System.out.print(".");
        }
        else {
            xqs.writeSequence(System.out, null);
        }
        out.write('\n');
        xqs.close();
    }

    private void bindExtVarsMap(Map<String, Object> extVars, XQExpression xqe)
            throws XQException, FileNotFoundException
    {
        for (String extVar : extVars.keySet()) {
            Object o = extVars.get(extVar);
            QName qname;
            if (extVar.equals(".")) {
                qname = XQConstants.CONTEXT_ITEM;
            }
            else {
                qname = new QName(extVar);
            }
            if (o instanceof Node) {
                xqe.bindNode(qname, (Node) o, null);
            }
            else if (o instanceof XMLStreamReader) {
                xqe.bindDocument(qname, (XMLStreamReader) o, null);
            }
            else if (o instanceof String) {
                xqe.bindString(qname, o.toString(), null);
            }
            else if (o instanceof Long || o instanceof Integer) {
                xqe.bindLong(qname, (Long) o, null);
            }
            else if (o instanceof Double || o instanceof Float) {
                xqe.bindDouble(qname, (Double) o, null);
            }
            else if (o instanceof Boolean) {
                xqe.bindBoolean(qname, (Boolean) o, null);
            }
            else {
                xqe.bindObject(qname, o, null);
            }
        }
    }
}
