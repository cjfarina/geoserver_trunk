package org.geoserver.data.geogit;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.geogit.api.GeoGIT;
import org.geogit.test.RepositoryTestCase;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.text.ecql.ECQL;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.sort.SortBy;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Polygon;

public class GeoGitFeatureSourceTest extends RepositoryTestCase {

    private static final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);

    private GeoGitDataStore dataStore;

    private GeoGitFeatureSource pointsSource;

    private GeoGitFeatureSource linesSource;

    @Override
    protected void setUpInternal() throws Exception {
        dataStore = new GeoGitDataStore(repo);
        dataStore.createSchema(super.pointsType);
        dataStore.createSchema(super.linesType);
        insertAndAdd(points1, points2, points3, lines1, lines2, lines3);
        new GeoGIT(repo).commit().setAuthor("yo").setCommitter("me").setMessage("initial import")
                .call();

        pointsSource = dataStore.getFeatureSource(pointsTypeName);
        linesSource = dataStore.getFeatureSource(linesTypeName);
    }

    public void testGetName() {
        assertEquals(pointsTypeName, pointsSource.getName());
        assertEquals(linesTypeName, linesSource.getName());
    }

    public void testGetInfo() {
        assertNotNull(pointsSource.getInfo());
        assertNotNull(pointsSource.getInfo().getBounds());
        assertNotNull(pointsSource.getInfo().getCRS());
        assertEquals(pointsName, pointsSource.getInfo().getName());

        assertNotNull(linesSource.getInfo());
        assertNotNull(linesSource.getInfo().getBounds());
        assertNotNull(linesSource.getInfo().getCRS());
        assertEquals(linesName, linesSource.getInfo().getName());
    }

    public void testGetDataStore() {
        assertSame(dataStore, pointsSource.getDataStore());
        assertSame(dataStore, linesSource.getDataStore());
    }

    public void testGetQueryCapabilities() {
        assertNotNull(pointsSource.getQueryCapabilities());
        assertFalse(pointsSource.getQueryCapabilities().isJoiningSupported());
        assertFalse(pointsSource.getQueryCapabilities().isOffsetSupported());
        assertTrue(pointsSource.getQueryCapabilities().isReliableFIDSupported());
        assertTrue(pointsSource.getQueryCapabilities().isUseProvidedFIDSupported());

        SortBy[] sortAttributes = { SortBy.NATURAL_ORDER };
        assertFalse(pointsSource.getQueryCapabilities().supportsSorting(sortAttributes));
    }

    public void testGetSchema() {
        assertEquals(pointsType, pointsSource.getSchema());
        assertEquals(linesType, linesSource.getSchema());
    }

    public void testGetBounds() throws IOException {
        ReferencedEnvelope expected;
        ReferencedEnvelope bounds;

        bounds = pointsSource.getBounds();
        assertNotNull(bounds);
        expected = boundsOf(points1, points2, points3);
        assertEquals(expected, bounds);

        bounds = linesSource.getBounds();
        assertNotNull(bounds);
        expected = boundsOf(lines1, lines2, lines3);
        assertEquals(expected, bounds);
    }

    public void testGetBoundsQuery() throws Exception {

        ReferencedEnvelope bounds;
        Filter filter;

        filter = ff.id(Collections.singleton(ff.featureId(RepositoryTestCase.idP2)));
        bounds = pointsSource.getBounds(new Query(pointsName, filter));
        assertEquals(boundsOf(points2), bounds);

        ReferencedEnvelope queryBounds = boundsOf(points1, points2);

        Polygon geometry = JTS.toGeometry(queryBounds);
        filter = ff.intersects(ff.property(pointsType.getGeometryDescriptor().getLocalName()),
                ff.literal(geometry));

        bounds = pointsSource.getBounds(new Query(pointsName, filter));
        assertEquals(boundsOf(points1, points2), bounds);

        ReferencedEnvelope transformedQueryBounds;
        CoordinateReferenceSystem queryCrs = CRS.decode("EPSG:900913");
        transformedQueryBounds = queryBounds.transform(queryCrs, true);

        geometry = JTS.toGeometry(transformedQueryBounds);
        geometry.setUserData(queryCrs);

        filter = ff.intersects(ff.property(pointsType.getGeometryDescriptor().getLocalName()),
                ff.literal(geometry));

        bounds = pointsSource.getBounds(new Query(pointsName, filter));
        assertEquals(boundsOf(points1, points2), bounds);

        filter = ECQL.toFilter("sp = 'StringProp2_3' OR ip = 2000");
        bounds = linesSource.getBounds(new Query(linesName, filter));
        assertEquals(boundsOf(lines3, lines2), bounds);
    }

    public void testGetCount() throws Exception {
        assertEquals(3, pointsSource.getCount(Query.ALL));
        assertEquals(3, linesSource.getCount(Query.ALL));

        ReferencedEnvelope bounds;
        Filter filter;

        filter = ff.id(Collections.singleton(ff.featureId(RepositoryTestCase.idP2)));
        assertEquals(1, pointsSource.getCount(new Query(pointsName, filter)));

        ReferencedEnvelope queryBounds = boundsOf(points1, points2);

        Polygon geometry = JTS.toGeometry(queryBounds);
        filter = ff.intersects(ff.property(pointsType.getGeometryDescriptor().getLocalName()),
                ff.literal(geometry));

        assertEquals(2, pointsSource.getCount(new Query(pointsName, filter)));

        ReferencedEnvelope transformedQueryBounds;
        CoordinateReferenceSystem queryCrs = CRS.decode("EPSG:900913");
        transformedQueryBounds = queryBounds.transform(queryCrs, true);

        geometry = JTS.toGeometry(transformedQueryBounds);
        geometry.setUserData(queryCrs);

        filter = ff.intersects(ff.property(pointsType.getGeometryDescriptor().getLocalName()),
                ff.literal(geometry));

        assertEquals(2, pointsSource.getCount(new Query(pointsName, filter)));

        filter = ECQL.toFilter("sp = 'StringProp2_3' OR ip = 2000");
        bounds = linesSource.getBounds(new Query(linesName, filter));
        assertEquals(2, linesSource.getCount(new Query(linesName, filter)));
    }

    @SuppressWarnings("deprecation")
    public void testGetFeatures() throws Exception {
        SimpleFeatureCollection collection;
        Set<SimpleFeature> features;
        Set<Feature> expected;

        collection = pointsSource.getFeatures();
        assertEquals(pointsType, collection.getSchema());
        assertEquals(boundsOf(points1, points2, points3), collection.getBounds());

        features = new HashSet<SimpleFeature>(toList(collection.iterator()));
        expected = new HashSet<Feature>(Arrays.asList(points1, points2, points3));

        assertEquals(expected, features);

        collection = linesSource.getFeatures();
        assertEquals(linesType, collection.getSchema());
        assertEquals(boundsOf(lines1, lines2, lines3), collection.getBounds());

        features = new HashSet<SimpleFeature>(toList(collection.iterator()));
        expected = new HashSet<Feature>(Arrays.asList(lines1, lines2, lines3));

        assertEquals(expected, features);
    }

    public void testGetFeaturesFilter() throws Exception {
        SimpleFeatureCollection collection;
        Set<SimpleFeature> features;
        Set<Feature> expected;

        Filter filter;

        filter = ff.id(Collections.singleton(ff.featureId(RepositoryTestCase.idP2)));
        collection = pointsSource.getFeatures(new Query(pointsName, filter));
        features = new HashSet<SimpleFeature>(toList(collection.iterator()));
        expected = new HashSet<Feature>(Arrays.asList(points2));

        assertEquals(expected, features);

        ReferencedEnvelope queryBounds = boundsOf(points1, points2);
        Polygon geometry = JTS.toGeometry(queryBounds);
        filter = ff.intersects(ff.property(pointsType.getGeometryDescriptor().getLocalName()),
                ff.literal(geometry));

        collection = pointsSource.getFeatures(new Query(pointsName, filter));
        features = new HashSet<SimpleFeature>(toList(collection.iterator()));
        expected = new HashSet<Feature>(Arrays.asList(points1, points2));

        assertEquals(expected, features);

        ReferencedEnvelope transformedQueryBounds;
        CoordinateReferenceSystem queryCrs = CRS.decode("EPSG:900913");
        transformedQueryBounds = queryBounds.transform(queryCrs, true);

        geometry = JTS.toGeometry(transformedQueryBounds);
        geometry.setUserData(queryCrs);

        filter = ff.intersects(ff.property(pointsType.getGeometryDescriptor().getLocalName()),
                ff.literal(geometry));

        collection = pointsSource.getFeatures(new Query(pointsName, filter));
        features = new HashSet<SimpleFeature>(toList(collection.iterator()));
        expected = new HashSet<Feature>(Arrays.asList(points1, points2));

        assertEquals(expected, features);

        filter = ECQL.toFilter("sp = 'StringProp2_3' OR ip = 2000");
        collection = linesSource.getFeatures(new Query(linesName, filter));
        features = new HashSet<SimpleFeature>(toList(collection.iterator()));
        expected = new HashSet<Feature>(Arrays.asList(lines2, lines3));

        assertEquals(expected, features);

    }

}
