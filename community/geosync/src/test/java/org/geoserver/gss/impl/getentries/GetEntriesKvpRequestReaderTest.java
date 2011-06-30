package org.geoserver.gss.impl.getentries;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import org.geoserver.gss.impl.query.GetEntriesKvpRequestReader;
import org.geoserver.gss.service.FeedType;
import org.geoserver.gss.service.GetEntries;
import org.geoserver.platform.ServiceException;
import org.geoserver.test.ows.KvpRequestReaderTestSupport;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;

/**
 * Test suite for {@link GetEntriesKvpRequestReader}.
 * <p>
 * Asserts the correct parsing of the GetEntries operation as per <i>Table 11 – GetEntries KVP
 * request encoding</i>
 * 
 * @author groldan
 * 
 */
public class GetEntriesKvpRequestReaderTest extends KvpRequestReaderTestSupport {

    private GetEntriesKvpRequestReader reader;

    @SuppressWarnings("rawtypes")
    private Map rawKvp;

    @SuppressWarnings("rawtypes")
    private Map kvp;

    /**
     * This is a READ ONLY TEST so we can use one time setup
     */
    public static Test suite() {
        return new OneTimeTestSetup(new GetEntriesKvpRequestReaderTest());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected void setUpInternal() throws Exception {
        reader = new GetEntriesKvpRequestReader();
        rawKvp = new HashMap();
        rawKvp.put("service", "GSS");
        rawKvp.put("version", "1.0.0");
        rawKvp.put("request", "GetEntries");
        rawKvp.put("feed", "REPLICATIONFEED");

        kvp = parseKvp(rawKvp);
    }

    public void testMinimal() throws Exception {
        GetEntries request = reader.createRequest();
        GetEntries parsed = reader.read(request, kvp, rawKvp);
        assertNotNull(parsed);

        assertEquals("GSS", parsed.getService());
        assertEquals("1.0.0", parsed.getVersion());
        assertEquals("GetEntries", parsed.getRequest());

        assertEquals(FeedType.REPLICATIONFEED, parsed.getFeed());

        assertNull(parsed.getHandle());
        assertNotNull(parsed.getOutputFormat());
        assertTrue(Filter.INCLUDE.equals(parsed.getFilter()));
        assertEquals(Long.valueOf(25), parsed.getMaxEntries());
        assertNull(parsed.getSearchTerms());
        assertNull(parsed.getStartPosition());
    }

    @SuppressWarnings("unchecked")
    public void testFullNoFilter() throws Exception {

        rawKvp.put("handle", "test handle");
        rawKvp.put("OuTputFormaT", "text/xml");
        rawKvp.put("MaxEntries", "10");
        rawKvp.put("SEARCHTERMS", "some,comma,separated,seach,terms");
        rawKvp.put("startPosition", "7");

        kvp = parseKvp(rawKvp);

        GetEntries request = reader.createRequest();
        GetEntries parsed = reader.read(request, kvp, rawKvp);

        assertEquals("test handle", parsed.getHandle());
        assertEquals("text/xml", parsed.getOutputFormat());
        assertEquals(Long.valueOf(10), parsed.getMaxEntries());
        List<String> searchTerms = Arrays.asList("some", "comma", "separated", "seach", "terms");
        assertEquals(searchTerms, parsed.getSearchTerms());
        assertEquals(Long.valueOf(7), parsed.getStartPosition());
    }

    /**
     * ENTRYID: Identifier of an entry to retrieve
     */
    @SuppressWarnings("unchecked")
    public void testIdentityPredicate() throws Exception {
        rawKvp.put("entryId", "fake-entry-id");
        kvp = parseKvp(rawKvp);

        GetEntries request = reader.createRequest();
        GetEntries parsed = reader.read(request, kvp, rawKvp);
        assertTrue(parsed.getFilter() instanceof Id);
    }

    /**
     * This filter has no {@code xmlns:fes="http://www.opengis.net/ogc"} and the parser fails
     * silently, potentially returning all entries instead of filtering them, so lets make sure we
     * catch it up
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public void testInvalidFitler() throws Exception {
        String filter = "<fes:Filter>" + //
                "  <fes:PropertyIsEqualTo>" + //
                "    <fes:PropertyName>foo</fes:PropertyName>" + //
                "    <fes:Literal>1</fes:Literal>" + //
                "  </fes:PropertyIsEqualTo>" + //
                "</fes:Filter>";

        rawKvp.put("filter", filter);
        kvp = parseKvp(rawKvp);

        GetEntries request = reader.createRequest();
        try {
            reader.read(request, kvp, rawKvp);
            fail("Expected ServiceException");
        } catch (ServiceException e) {
            assertEquals("InvalidParameterValue", e.getCode());
            assertEquals("FILTER", e.getLocator());
        }
    }

    @SuppressWarnings("unchecked")
    public void testFitler() throws Exception {
        String filter = "<fes:Filter xmlns:fes=\"http://www.opengis.net/ogc\">" + //
                "  <fes:PropertyIsEqualTo>" + //
                "    <fes:PropertyName>foo</fes:PropertyName>" + //
                "    <fes:Literal>1</fes:Literal>" + //
                "  </fes:PropertyIsEqualTo>" + //
                "</fes:Filter>";

        rawKvp.put("filter", filter);
        kvp = parseKvp(rawKvp);

        GetEntries request = reader.createRequest();
        GetEntries parsed = reader.read(request, kvp, rawKvp);
        assertNotNull(parsed.getFilter());
    }

    /**
     * Test case for the BBOX spatial parameter.
     * <p>
     * Spatial parameters could be: BBOX, GEOM, SPATIALOP, CRS
     * </p>
     */
    public void testSpatialParameterBBOX_NoCRS() throws Exception {
        fail("not implemented");
    }

    public void testSpatialParameterBBOX_CRS() throws Exception {
        fail("not implemented");
    }

    /**
     * SpatialOP: one of Equals, Disjoint, Touches, Within, Overlaps, Crosses, Intersects , Contains
     * 
     * @throws Exception
     */
    public void testSpatialParameterBBOX_SPATIALOP() throws Exception {
        fail("not implemented");
    }

    /**
     * Test case for the GEOM spatial parameter.
     * <p>
     * Spatial parameters could be: BBOX, GEOM, SPATIALOP, CRS
     * </p>
     */
    public void testSpatialParameterGEOM_DefaultCRS() throws Exception {
        fail("not implemented");
    }

    public void testSpatialParameterGEOM_CRS() throws Exception {
        fail("not implemented");
    }

    public void testSpatialParameterGEOM_SPATIALOP() throws Exception {
        fail("not implemented");
    }

    public void testSpatialParameterAndFilterAreMutuallyExclusive() throws Exception {
        fail("not implemented");
    }

    /**
     * <ul>
     * <li>STARTTIME: An RFC 3339 string encoding a start time.
     * <li>ENDTIME: An RFC 3339 string encoding an end time
     * <li>TEMPORALOP: After , Before, Begins, BegunBy, TContains, During, EndedBy, Ends, TEquals,
     * Meets, MetBy, TOverlaps, OverlappedBy
     * </ul>
     */
    public void testTemporalParameters() throws Exception {
        fail("not implemented");
    }

}
