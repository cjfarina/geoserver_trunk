package org.geogit.repository;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import org.geogit.api.ObjectId;
import org.geogit.api.ShowOp;
import org.geogit.storage.FeatureWriter;
import org.geogit.test.RepositoryTestCase;
import org.geotools.data.DataUtilities;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.WKTReader2;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;

public class IndexTest extends RepositoryTestCase {

    private Index index;

    @Override
    protected void setUpInternal() throws Exception {
        index = repo.getIndex();
    }

    // two features with the same content and different fid should point to the same object
    public void testInsertIdenticalObjects() throws Exception {
        ObjectId oId1 = index.inserted(new FeatureWriter(feature1_1), namespace1, typeName1,
                feature1_1.getIdentifier().getID());
        Feature equalContentFeature = feature(featureType1, "DifferentId",
                ((SimpleFeature) feature1_1).getAttributes().toArray());

        ObjectId oId2 = index.inserted(new FeatureWriter(equalContentFeature), namespace1,
                typeName1, equalContentFeature.getIdentifier().getID());

        // BLOBS.print(repo.getRawObject(insertedId1), System.err);
        // BLOBS.print(repo.getRawObject(insertedId2), System.err);
        assertNotNull(oId1);
        assertNotNull(oId2);
        assertEquals(oId1, oId2);
    }

    // two features with different content should point to different objects
    public void testInsertNonEqualObjects() throws Exception {
        ObjectId oId1 = index.inserted(new FeatureWriter(feature1_1), namespace1, typeName1,
                feature1_1.getIdentifier().getID());

        ObjectId oId2 = index.inserted(new FeatureWriter(feature1_2), namespace1, typeName1,
                feature1_2.getIdentifier().getID());
        assertNotNull(oId1);
        assertNotNull(oId2);
        assertFalse(oId1.equals(oId2));
    }

    public void testWriteTree() throws Exception {
        String namespace = "http://geoserver.org/test";
        String typeName = "TestType";
        String typeSpec = "sp:String,ip:Integer,pp:LineString:srid=4326";
        SimpleFeatureType featureType = DataUtilities.createType(namespace, typeName, typeSpec);

        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
        builder.set("sp", "String Property");
        builder.set("ip", Integer.valueOf(1000));
        builder.set("pp", new WKTReader2()
                .read("LINESTRING(1 1, 2 2, 3 3, 4 4, 5 5, 6 6, 7 7, 8 8, 9 9 , 10 10)"));

        Feature feature1 = builder.buildFeature("TestType.feature.1");
        Feature feature2 = builder.buildFeature("TestType.feature.2");

        Name featureTypeName = featureType.getName();
        String namespaceURI = featureTypeName.getNamespaceURI();
        String localPart = featureTypeName.getLocalPart();

        final ObjectId insertedId1 = index.inserted(new FeatureWriter(feature1), namespaceURI,
                localPart, feature1.getIdentifier().getID());
        final ObjectId insertedId2 = index.inserted(new FeatureWriter(feature1), namespaceURI,
                localPart, feature2.getIdentifier().getID());
        assertEquals(insertedId1, insertedId2);

        Repository mockRepo = mock(Repository.class);
        InputStream value = repo.getRawObject(insertedId1);

        when(mockRepo.getRawObject(eq(insertedId1))).thenReturn(value);
        ShowOp showOp = new ShowOp(mockRepo);
        showOp.setObjectId(insertedId1).call();
    }
}
