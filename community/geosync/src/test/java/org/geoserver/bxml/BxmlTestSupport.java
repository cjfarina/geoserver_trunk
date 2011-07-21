package org.geoserver.bxml;

import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;

import junit.framework.TestCase;

import org.gvsig.bxml.adapt.stax.XmlStreamReaderAdapter;
import org.gvsig.bxml.stream.BxmlStreamReader;

public abstract class BxmlTestSupport extends TestCase {

    protected BxmlStreamReader getXmlReader(final String resource) throws Exception {
        final InputStream input = getClass().getResourceAsStream(resource);
        assertNotNull(resource + " not found by " + getClass().getName(), input);
        return getXmlReader(input);
    }

    protected BxmlStreamReader getXmlReader(final InputStream input) throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
        BxmlStreamReader reader = new XmlStreamReaderAdapter(factory, input);
        return reader;
    }

}