package org.geoserver.gss;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.geoserver.config.GeoServer;
import org.geoserver.gss.config.GSSInfo;
import org.geoserver.gss.config.GSSXStreamLoader;
import org.geoserver.gss.xml.GSSConfiguration;
import org.geoserver.test.GeoServerTestSupport;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.util.logging.Logging;
import org.geotools.xml.Parser;
import org.opengis.filter.FilterFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.mockrunner.mock.web.MockHttpServletResponse;

/**
 * Base class for gss functional testing, sets up a proper testing enviroment for gss test
 */
public abstract class GSSTestSupport extends GeoServerTestSupport {

    protected static XpathEngine xpath;

    protected FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

    // @Override
    // public TestData buildTestData() throws Exception {
    // File base = new File("./src/test/resources/");
    // LiveDbmsData data = new LiveDbmsData(new File(base, "data_dir"), "unit", new File(base,
    // "unit.sql"));
    // List<String> filteredPaths = data.getFilteredPaths();
    // filteredPaths.clear();
    // filteredPaths.add("workspaces/topp/synch/datastore.xml");
    // return data;
    // }

    @Override
    protected void setUpInternal() throws Exception {
    }

    @Override
    protected void oneTimeSetUp() throws Exception {
        super.oneTimeSetUp();
        Logging.ALL.forceMonolineConsoleOutput();

        // configure the GSS service
        GeoServer gs = getGeoServer();
        GSSXStreamLoader loader = (GSSXStreamLoader) applicationContext.getBean("gssLoader");
        GSSInfo gssInfo = loader.load(gs);
        assertNotNull(gssInfo);
        loader.save(gssInfo, gs);
        gs.add(gssInfo);

        // init xmlunit
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("gss", "http://www.opengis.net/gss/1.0");
        namespaces.put("wfs", "http://www.opengis.net/wfs");
        namespaces.put("ows", "http://www.opengis.net/ows/1.1");
        namespaces.put("fes", "http://www.opengis.net/ogc");
        namespaces.put("gml", "http://www.opengis.net/gml");
        namespaces.put("sf", "http://www.openplans.org/spearfish");
        namespaces.put("xs", "http://www.w3.org/2001/XMLSchema");
        namespaces.put("app", "http://www.w3.org/2007/app");
        namespaces.put("atom", "http://www.w3.org/2005/Atom");
        namespaces.put("georss", "http://www.georss.org/georss");
        namespaces.put("os", "http://a9.com/-/spec/opensearch/1.1/");

        namespaces.put("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");

        XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(namespaces));

        xpath = XMLUnit.newXpathEngine();
    }

    /**
     * Validates
     * 
     * @param document
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
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
            // print(dom(response));
            fail("Document is not valid, see standard output for a document dump and validation exceptions");
        }
    }

    /**
     * Parses the mock response into a DOM tree
     */
    protected Document dom(MockHttpServletResponse response) throws IOException, SAXException,
            ParserConfigurationException {
        return dom(new ByteArrayInputStream(response.getOutputStreamContent().getBytes()));
    }

    /**
     * Loads a text file in the classpath into a String
     * 
     * @param path
     *            Path relative to the calling class
     * @return
     * @throws Exception
     */
    protected String loadTextResource(String path) throws Exception {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass()
                .getResourceAsStream(path)));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        br.close();
        return sb.toString();
    }
}
