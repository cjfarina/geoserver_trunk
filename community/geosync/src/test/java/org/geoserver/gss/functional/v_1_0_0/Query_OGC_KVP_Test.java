package org.geoserver.gss.functional.v_1_0_0;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;

import java.util.Arrays;
import java.util.List;

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
     * The ordered list of all values for the {@code /atom:feed/atom:entry/atom:title} XPath in the
     * REPLICATIONFEED as set up at {@link #oneTimeSetUp()}
     */
    private static final List<String> ALL_REPLICATION_TITLES = Arrays.asList( //
            "Insert of Feature Bridges.1107531599613", //
            "Insert of Feature Buildings.1107531701011", //
            "Insert of Feature Buildings.1107531701010", //
            "Update of Feature Bridges.1107531599613",//
            "Update of Feature Buildings.1107531701011",//
            "Delte of Feature Buildings.1107531701010"//
    );

    /**
     * The ordered list of all values for the {@code /atom:feed/atom:entry/atom:summary} XPath in
     * the REPLICATIONFEED as set up at {@link #oneTimeSetUp()}
     */
    final List<String> ALL_REPLICATION_SUMMARIES = Arrays.asList(//
            "Initial import of FeatureType http://www.opengis.net/cite:Bridges",//
            "Initial import of FeatureType http://www.opengis.net/cite:Buildings",//
            "Initial import of FeatureType http://www.opengis.net/cite:Buildings",//
            "Change Cam Bridge",//
            "Moved building",//
            "Deleted building"//
    );

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
        // print(dom);
        Element root = dom.getDocumentElement();
        String nodeName = root.getLocalName();
        assertEquals(Atom.NAMESPACE, root.getNamespaceURI());
        assertEquals("feed", nodeName);
        assertXpathExists("atom:feed/atom:id", dom);
        assertXpathExists("atom:feed/atom:updated", dom);

        final int expectedEntries = 6;
        // as per GSSFunctionalTestSupport's oneTimeSetUp
        final String[] expectedOps = { "Insert", "Insert", "Insert", "Update", "Update", "Delete" };

        assertXpathEvaluatesTo(String.valueOf(expectedEntries), "count(atom:feed/atom:entry)", dom);

        for (int entryIndex = 1; entryIndex <= expectedEntries; entryIndex++) {
            String entryPath = "atom:feed/atom:entry[" + entryIndex + "]";

            assertXpathExists(entryPath + "/atom:title", dom);
            assertXpathExists(entryPath + "/atom:summary", dom);
            assertXpathExists(entryPath + "/atom:updated", dom);
            assertXpathExists(entryPath + "/atom:author/atom:name", dom);
            assertXpathExists(entryPath + "/atom:contributor/atom:name", dom);
            assertXpathExists(entryPath + "/atom:content", dom);

            assertXpathExists(entryPath + "/atom:content/wfs:" + expectedOps[entryIndex - 1], dom);

            assertXpathExists(entryPath + "/georss:where", dom);
        }
    }

    public void testStartPositionFiltering() throws Exception {

        String request;
        Document dom;
        List<String> result;

        request = REPLICATION_FEED_BASE;
        dom = super.getAsDOM(request);
        // print(dom);

        result = evaluateAll("//atom:feed/atom:entry/atom:title", dom);
        assertEquals(ALL_REPLICATION_TITLES.size(), result.size());
        assertEquals(ALL_REPLICATION_TITLES, result);

        request = REPLICATION_FEED_BASE + "&startPosition=2";
        dom = super.getAsDOM(request);
        result = evaluateAll("//atom:feed/atom:entry/atom:title", dom);
        assertEquals(ALL_REPLICATION_TITLES.subList(1, ALL_REPLICATION_TITLES.size()), result);

        request = REPLICATION_FEED_BASE + "&startPosition=6";
        dom = super.getAsDOM(request);
        result = evaluateAll("//atom:feed/atom:entry/atom:title", dom);
        assertEquals(ALL_REPLICATION_TITLES.subList(5, ALL_REPLICATION_TITLES.size()), result);

        request = REPLICATION_FEED_BASE + "&startPosition=10";
        dom = super.getAsDOM(request);
        result = evaluateAll("//atom:feed/atom:entry/atom:title", dom);
        assertTrue(result.isEmpty());
    }

    public void testSearchTermsFiltering() throws Exception {
        String request;
        Document dom;
        List<String> result;

        request = REPLICATION_FEED_BASE;
        dom = super.getAsDOM(request);
        // print(dom);
        result = evaluateAll("//atom:feed/atom:entry/atom:summary", dom);
        assertEquals(ALL_REPLICATION_SUMMARIES.size(), result.size());
        assertEquals(ALL_REPLICATION_SUMMARIES, result);

        request = REPLICATION_FEED_BASE + "&searchTerms=Insert,Update";
        dom = super.getAsDOM(request);
        // print(dom);
        assertXpathEvaluatesTo("5", "count(//atom:feed/atom:entry)", dom);

        request = REPLICATION_FEED_BASE + "&searchTerms=Moved,Delte";
        dom = super.getAsDOM(request);
        // print(dom);
        assertXpathEvaluatesTo("2", "count(//atom:feed/atom:entry)", dom);

        request = REPLICATION_FEED_BASE + "&searchTerms=None";
        dom = super.getAsDOM(request);
        // print(dom);
        assertXpathEvaluatesTo("0", "count(//atom:feed/atom:entry)", dom);

        request = REPLICATION_FEED_BASE + "&searchTerms=Buildings.1107531701011";
        dom = super.getAsDOM(request);
        // print(dom);
        assertXpathEvaluatesTo("2", "count(//atom:feed/atom:entry)", dom);

        request = REPLICATION_FEED_BASE + "&searchTerms=Buildings.1107531701011,Moved";
        dom = super.getAsDOM(request);
        // print(dom);
        // expect 2, an entry matches the seatchTerms if it containst ANY of the terms
        assertXpathEvaluatesTo("2", "count(//atom:feed/atom:entry)", dom);
    }

    public void testEntryIdFilter() throws Exception {
        final String insertId;
        final String updateId;
        final String deleteId;
        {
            final String request = REPLICATION_FEED_BASE;
            Document dom = super.getAsDOM(request);
            // print(dom);
            insertId = xpath.evaluate("atom:feed/atom:entry[1]/atom:id", dom);
            updateId = xpath.evaluate("atom:feed/atom:entry[4]/atom:id", dom);
            deleteId = xpath.evaluate("atom:feed/atom:entry[6]/atom:id", dom);
            assertTrue(insertId != null && updateId != null && updateId != null
                    && !insertId.equals(updateId) && !updateId.equals(deleteId));
        }
        {
            final String queryById = REPLICATION_FEED_BASE + "&ENTRYID=" + insertId;
            final Document dom = super.getAsDOM(queryById);
            // print(dom);
            assertXpathEvaluatesTo("1", "count(atom:feed/atom:entry)", dom);
            assertXpathExists("atom:feed/atom:entry[1]/atom:content/wfs:Insert", dom);
        }
        {
            final String queryById = REPLICATION_FEED_BASE + "&ENTRYID=" + updateId;
            final Document dom = super.getAsDOM(queryById);
            // print(dom);
            assertXpathEvaluatesTo("1", "count(atom:feed/atom:entry)", dom);
            assertXpathExists("atom:feed/atom:entry[1]/atom:content/wfs:Update", dom);
        }
        {
            final String queryById = REPLICATION_FEED_BASE + "&ENTRYID=" + deleteId;
            final Document dom = super.getAsDOM(queryById);
            // print(dom);
            // REVISIT: this assertion fails becuase we don't map geogit ids to gss ids yet, hence
            // the same geogit id for the feature insert and then the delete is being used twice
            assertXpathEvaluatesTo("1", "count(atom:feed/atom:entry)", dom);
            assertXpathExists("atom:feed/atom:entry[1]/atom:content/wfs:Delete", dom);
        }
    }
}
