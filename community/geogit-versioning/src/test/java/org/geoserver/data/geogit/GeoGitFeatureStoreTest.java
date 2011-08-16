package org.geoserver.data.geogit;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.geogit.api.GeoGIT;
import org.geogit.test.RepositoryTestCase;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.Id;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.identity.ResourceId;

public class GeoGitFeatureStoreTest extends RepositoryTestCase {

    private static final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);

    private GeoGitDataStore dataStore;

    private GeoGitFeatureStore points;

    private GeoGitFeatureStore lines;

    @Override
    protected void setUpInternal() throws Exception {
        dataStore = new GeoGitDataStore(repo);
        dataStore.createSchema(super.pointsType);
        dataStore.createSchema(super.linesType);

        points = (GeoGitFeatureStore) dataStore.getFeatureSource(pointsTypeName);
        lines = (GeoGitFeatureStore) dataStore.getFeatureSource(linesTypeName);
    }

    public void testAddFeatures() throws Exception {

        FeatureCollection<SimpleFeatureType, SimpleFeature> collection;
        collection = DataUtilities.collection(Arrays.asList((SimpleFeature) points1,
                (SimpleFeature) points2, (SimpleFeature) points3));

        try {
            points.addFeatures(collection);
            fail("Expected UnsupportedOperationException on AUTO_COMMIT");
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("AUTO_COMMIT"));
        }

        Transaction tx = new DefaultTransaction();
        points.setTransaction(tx);
        assertSame(tx, points.getTransaction());
        try {
            List<FeatureId> addedFeatures = points.addFeatures(collection);
            assertNotNull(addedFeatures);
            assertEquals(3, addedFeatures.size());

            assertEquals(idP1, addedFeatures.get(0).getID());
            assertEquals(idP2, addedFeatures.get(1).getID());
            assertEquals(idP3, addedFeatures.get(2).getID());
            for (FeatureId id : addedFeatures) {
                assertTrue(id instanceof ResourceId);
                assertNotNull(((ResourceId) id).getFeatureVersion());
            }

            // assert transaction isolation

            assertEquals(3, points.getFeatures().size());
            assertEquals(0, dataStore.getFeatureSource(pointsTypeName).getFeatures().size());

            tx.commit();

            assertEquals(3, dataStore.getFeatureSource(pointsTypeName).getFeatures().size());
        } catch (Exception e) {
            tx.rollback();
            throw e;
        } finally {
            tx.close();
        }
    }

    @SuppressWarnings("deprecation")
    public void testModifyFeatures() throws Exception {
        // add features circunventing FeatureStore.addFeatures to keep the test independent of the
        // addFeatures functionality
        insertAndAdd(lines1, lines2, lines3, points1, points2, points3);
        new GeoGIT(repo).commit().call();

        Id filter = ff.id(Collections.singleton(ff.featureId(idP1)));
        Transaction tx = new DefaultTransaction();
        points.setTransaction(tx);
        try {
            // initial value
            assertEquals("StringProp1_1", points.getFeatures(filter).iterator().next()
                    .getAttribute("sp"));
            // modify
            points.modifyFeatures("sp", "modified", filter);
            // modified value before commit
            assertEquals("modified", points.getFeatures(filter).iterator().next()
                    .getAttribute("sp"));
            // unmodified value before commit on another store instance (tx isolation)
            assertEquals("StringProp1_1",
                    dataStore.getFeatureSource(pointsTypeName).getFeatures(filter).iterator()
                            .next().getAttribute("sp"));

            tx.commit();

            // modified value after commit on another store instance
            assertEquals("modified", dataStore.getFeatureSource(pointsTypeName).getFeatures(filter)
                    .iterator().next().getAttribute("sp"));
        } catch (Exception e) {
            tx.rollback();
            throw e;
        } finally {
            tx.close();
        }
        SimpleFeature modified = points.getFeatures(filter).iterator().next();
        assertEquals("modified", modified.getAttribute("sp"));
    }

    public void testRemoveFeatures() throws Exception {
        // add features circunventing FeatureStore.addFeatures to keep the test independent of the
        // addFeatures functionality
        insertAndAdd(lines1, lines2, lines3, points1, points2, points3);
        new GeoGIT(repo).commit().call();

        Id filter = ff.id(Collections.singleton(ff.featureId(idP1)));
        Transaction tx = new DefaultTransaction();
        points.setTransaction(tx);
        try {
            // initial # of features
            assertEquals(3, points.getFeatures().size());
            // remove feature
            points.removeFeatures(filter);

            // #of features before commit on the same store
            assertEquals(2, points.getFeatures().size());

            // #of features before commit on a different store instance
            assertEquals(3, dataStore.getFeatureSource(pointsTypeName).getFeatures().size());

            tx.commit();

            // #of features after commit on a different store instance
            assertEquals(2, dataStore.getFeatureSource(pointsTypeName).getFeatures().size());
        } catch (Exception e) {
            tx.rollback();
            throw e;
        } finally {
            tx.close();
        }

        assertEquals(2, points.getFeatures().size());
        assertEquals(0, points.getFeatures(filter).size());
    }

}
