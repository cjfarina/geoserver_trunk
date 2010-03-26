package org.geoserver.gss;

import static org.custommonkey.xmlunit.XMLAssert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.geoserver.config.GeoServer;
import org.geoserver.data.test.LiveDbmsData;
import org.geoserver.data.test.TestData;
import org.geoserver.gss.GSSInfo;
import org.geoserver.gss.GSSInfo.GSSMode;
import org.geoserver.gss.xml.GSSConfiguration;
import org.geoserver.test.GeoServerAbstractTestSupport;
import org.geotools.xml.Parser;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.mockrunner.mock.web.MockHttpServletResponse;

/**
 * Base class for gss functional testing, sets up a proper testing enviroment for gss test with a
 * real data dir and a connection to a postgis data store
 * 
 * @author Andrea Aime - TOPP
 * 
 */
public abstract class GSSTestSupport extends GeoServerAbstractTestSupport {

    static XpathEngine xpath;

    // protected String getLogConfiguration() {
    // return "/DEFAULT_LOGGING.properties";
    // }

    @Override
    public TestData buildTestData() throws Exception {
        File base = new File("./src/test/resources/");
        LiveDbmsData data = new LiveDbmsData(new File(base, "data_dir"), "unit", new File(base,
                "unit.sql"));
        List<String> filteredPaths = data.getFilteredPaths();
        filteredPaths.clear();
        filteredPaths.add("workspaces/topp/synch/datastore.xml");
        return data;
    }
    
    @Override
    protected void setUpInternal() throws Exception {
        GeoServer gs = getGeoServer();
        GSSInfo gssInfo = gs.getService(GSSInfo.class);
        gssInfo.setMode(GSSMode.Unit);
        gssInfo.setVersioningDataStore(getCatalog().getDataStoreByName("synch"));
        gs.save(gssInfo);
    }

    @Override
    protected void oneTimeSetUp() throws Exception {
        super.oneTimeSetUp();

        // init xmlunit
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("wfs", "http://www.opengis.net/gss");
        namespaces.put("wfs", "http://www.opengis.net/wfs");
        namespaces.put("wfsv", "http://www.opengis.net/wfsv");
        namespaces.put("ows", "http://www.opengis.net/ows");
        namespaces.put("ogc", "http://www.opengis.net/ogc");
        namespaces.put("gml", "http://www.opengis.net/gml");
        namespaces.put("topp", "http://www.openplans.org/topp");
        namespaces.put("gss", "http://geoserver.org/gss");
        namespaces.put("xs", "http://www.w3.org/2001/XMLSchema");
        namespaces.put("", "http://www.opengis.net/ogc");
        XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(namespaces));

        xpath = XMLUnit.newXpathEngine();
    }

    /**
     * Returns the url of the WFSV entry point
     * 
     * @return
     */
    protected String root() {
        return root(false);
    }

    protected String root(boolean validate) {
        return "ows?" + (validate ? "strict=true&" : "");
    }

    /**
     * Validates
     * @param document
     * @throws Exception
     */
    protected void validate(MockHttpServletResponse response) throws Exception {
        GSSConfiguration configuration = (GSSConfiguration) applicationContext
                .getBean("gssXmlConfiguration");
        Parser parser = new Parser(configuration);
        parser.validate(new StringReader(response.getOutputStreamContent()));
        if (parser.getValidationErrors().size() > 0) {
            for (Iterator it = parser.getValidationErrors().iterator(); it.hasNext();) {
                SAXParseException se = (SAXParseException) it.next();
                System.out.println(se);
            }
            print(dom(response));
            fail("Document is not valid, see standard output for a document dump and validation exceptions");
        }
    }

    /**
     * Parses the mock response into a DOM tree
     */
    protected Document dom(MockHttpServletResponse response) throws IOException, SAXException, ParserConfigurationException {
        return dom(new ByteArrayInputStream(response.getOutputStreamContent().getBytes()));
    }
    
    /**
     * Cheks the DOM represents an OWS 1.0 exception
     * @param dom
     * @throws Exception
     */
    protected void checkOwsException(Document dom) throws Exception {
        assertXpathEvaluatesTo("1.0.0", "/ows:ExceptionReport/@version", dom);
        assertXpathEvaluatesTo("1", "count(/ows:ExceptionReport/ows:Exception)", dom);
    }

}
