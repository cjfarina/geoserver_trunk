package org.geoserver.gss.functional.v_1_0_0;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import junit.framework.Test;

import org.geoserver.gss.internal.atom.Atom;
import org.geotools.ows.v1_1.OWS;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mockrunner.mock.web.MockHttpServletResponse;

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
public class Query_OGC_KVP_Test extends GSSFunctionalTestSupport {

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
     * 9.3.3.3 Response encoding: The response to a GetEntries operation, using the default output
     * format, shall be an ATOM feed (see IETF RFC 4287) with the mime type of application/atom+xml.
     * 
     * @throws Exception
     */
    public void testResponseCodeAndMimeType() throws Exception {
        final String request = REPLICATION_FEED_BASE;
        MockHttpServletResponse response = super.getAsServletResponse(request);
        assertEquals(200, response.getStatusCode());
        assertEquals("application/atom+xml", response.getContentType());
    }

    /**
     * Only mandatory param besides service/request/version is FEED.
     */
    public void testBaseRequest() throws Exception {
        final String request = REPLICATION_FEED_BASE;
        Document dom = super.getAsDOM(request);
        print(dom);
        Element root = dom.getDocumentElement();
        String nodeName = root.getLocalName();
        assertEquals(Atom.NAMESPACE, root.getNamespaceURI());
        assertEquals("feed", nodeName);
        assertXpathExists("atom:feed/atom:id", dom);
        assertXpathExists("atom:feed/atom:updated", dom);

        assertXpathEvaluatesTo("2", "count(atom:feed/atom:entry)", dom);

        assertXpathExists("atom:feed/atom:entry[1]/atom:title", dom);
        assertXpathExists("atom:feed/atom:entry[1]/atom:summary", dom);
        assertXpathExists("atom:feed/atom:entry[1]/atom:updated", dom);
        assertXpathExists("atom:feed/atom:entry[1]/atom:author/atom:name", dom);
        assertXpathExists("atom:feed/atom:entry[1]/atom:contributor/atom:name", dom);
        assertXpathExists("atom:feed/atom:entry[1]/atom:content", dom);
        assertXpathExists("atom:feed/atom:entry[1]/atom:content/wfs:Insert", dom);

        assertXpathExists("atom:feed/atom:entry[2]/atom:title", dom);
        assertXpathExists("atom:feed/atom:entry[2]/atom:summary", dom);
        assertXpathExists("atom:feed/atom:entry[2]/atom:updated", dom);
        assertXpathExists("atom:feed/atom:entry[2]/atom:author/atom:name", dom);
        assertXpathExists("atom:feed/atom:entry[2]/atom:contributor/atom:name", dom);
        assertXpathExists("atom:feed/atom:entry[2]/atom:content", dom);
        assertXpathExists("atom:feed/atom:entry[2]/atom:content/wfs:Update", dom);
    }

    /**
     * Only mandatory param besides service/request/version is FEED.
     */
    public void testEntryIdFilter() throws Exception {
        final String request = REPLICATION_FEED_BASE;
        Document dom = super.getAsDOM(request);
        // print(dom);
        final String insertId = xpath.evaluate("atom:feed/atom:entry[1]/atom:id", dom);
        final String updateId = xpath.evaluate("atom:feed/atom:entry[2]/atom:id", dom);
        assertTrue(insertId != null && updateId != null && !insertId.equals(updateId));

        final String queryById1 = REPLICATION_FEED_BASE + "&ENTRYID=" + insertId;
        final Document response1 = super.getAsDOM(queryById1);
        print(response1);
        assertXpathEvaluatesTo("1", "count(atom:feed/atom:entry)", response1);
        assertXpathExists("atom:feed/atom:entry[1]/atom:content/wfs:Insert", dom);

        final String queryById2 = REPLICATION_FEED_BASE + "&ENTRYID=" + updateId;
        final Document response2 = super.getAsDOM(queryById2);
        print(response2);
        assertXpathEvaluatesTo("1", "count(atom:feed/atom:entry)", response2);
        assertXpathExists("atom:feed/atom:entry[1]/atom:content/wfs:Update", dom);
    }
}
