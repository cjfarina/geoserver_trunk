package org.geoserver.gss.functional.v_1_0_0;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import junit.framework.Test;

import org.geoserver.gss.GSSTestSupport;
import org.geoserver.gss.internal.atom.Atom;
import org.geoserver.gss.internal.atom.FeedImpl;
import org.geotools.ows.v1_1.OWS;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Functional test suite for the {@code GetEntries} GSS operation using KVP request encoding.
 * <p>
 * 
 * <pre>Excerpt OCG Document 10-069r2_OWS_7_Engineering_Report, Section 9.3.3.1:
 * 
 * <pre>
 * 
 * <i> This clause defines a generalized query operation, called GetEntries, that allows feeds to be
 * queried for entries that satisfy some set of spatial and non-spatial predicates where the spatial
 * predicates can include geometric and temporal constraints. The response to this query operation
 * shall itself be encoded as an ATOM feed but only containing the entries that satisfy the
 * predicates. The operation's parameters are intentionally designed to map the standard OpenSearch
 * parameters as well as the parameters found in the geo and time extensions (see OGC 10-032). </i>
 * 
 * </p>
 * 
 * @author groldan
 * 
 */
public class Query_OGC_KVP_Test extends GSSTestSupport {

    private static final String BASE_REQUEST_PATH = "/ows?service=GSS&version=1.0.0&request=GetEntries";

    private static final String REPLICATION_FEED_BASE = BASE_REQUEST_PATH + "&FEED=REPLICATIONFEED";

    /**
     * This is a READ ONLY TEST so we can use one time setup
     */
    public static Test suite() {
        return new OneTimeTestSetup(new Query_OGC_KVP_Test());
    }

    public void testExceptionIsOWS11ExceptionReport() throws Exception {
        final String request = BASE_REQUEST_PATH + "&FEED=non-existent-feed-id";
        Document dom = super.getAsDOM(request);
        // print(dom);
        Element root = dom.getDocumentElement();

        assertEquals(OWS.NAMESPACE, root.getNamespaceURI());
        assertEquals(OWS.ExceptionReport.getLocalPart(), root.getLocalName());
    }

    /**
     * Only mandatory param besides service/request/version is FEED.
     */
    public void testEmptyFeed() throws Exception {
        final String request = REPLICATION_FEED_BASE;
        Document dom = super.getAsDOM(request);
        print(dom);
        Element root = dom.getDocumentElement();
        String nodeName = root.getLocalName();
        assertEquals(Atom.NAMESPACE, root.getNamespaceURI());
        assertEquals("feed", nodeName);
        assertXpathEvaluatesTo(FeedImpl.NULL_ID, "atom:feed/atom:id", dom);
        assertXpathExists("atom:feed/atom:updated", dom);
    }
}
