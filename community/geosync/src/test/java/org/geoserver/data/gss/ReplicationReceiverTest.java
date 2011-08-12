package org.geoserver.data.gss;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.geoserver.bxml.BxmlTestSupport;
import org.geoserver.bxml.atom.FeedDecoder;
import org.geoserver.gss.impl.GSS;
import org.geoserver.gss.internal.atom.FeedImpl;
import org.geoserver.platform.GeoServerExtensions;
import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.NameImpl;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

public class ReplicationReceiverTest extends BxmlTestSupport {

    private GeometryFactory gf = new GeometryFactory();

    private FilterFactory2 ff;

    private GSS gss;

    @Override
    public void oneTimeSetUp() throws Exception {
        super.oneTimeSetUp();
        gf = new GeometryFactory();
        ff = CommonFactoryFinder.getFilterFactory2(null);

        gss = GeoServerExtensions.bean(GSS.class, applicationContext);

        // insert the single bridge in cite:Bridges
        // assertTrue(makeVersioned(gss, CITE_BRIDGES) instanceof RevCommit);

        List<String> properties = Arrays.asList("NAME", "the_geom");
        List<Object> newValues = Arrays.asList("Cam Bridge",
                (Object) gf.createPoint(new Coordinate(4, 5)));
        String commitMessage = "Change Cam Bridge";
        Filter filter = Filter.INCLUDE;
        recordUpdateCommit(gss, CITE_BRIDGES, filter, properties, newValues, commitMessage);

    }

    public void testUpdate() throws Exception {
        BxmlStreamReader reader = super.getReader("replicationReceiverUpdate");

        FeedDecoder decoder = new FeedDecoder();

        reader.nextTag();
        FeedImpl feed = decoder.decode(reader);

        List<String> properties = Arrays.asList("NAME", "FID", "the_geom");
        List<Object> values = Arrays.asList("Cam Bridge", "110",
                (Object) gf.createPoint(new Coordinate(4, 5)));

        assertFeature(CITE_BRIDGES, "Bridges.1107531599613", properties, values);

        ReplicationReceiver replicationReceiver = new ReplicationReceiver();
        replicationReceiver.receive(feed, getCatalog());

        List<Object> newValues = Arrays.asList("Cam Bridge 1", "150",
                (Object) gf.createPoint(new Coordinate(5, 6)));
        assertFeature(CITE_BRIDGES, "Bridges.1107531599613", properties, newValues);
    }

    public void testDelete() throws Exception {
        BxmlStreamReader reader = super.getReader("replicationReceiverDelete");

        FeedDecoder decoder = new FeedDecoder();

        reader.nextTag();
        FeedImpl feed = decoder.decode(reader);

        Feature feature = getFeature(CITE_BRIDGES, "Bridges.1107531599613");
        assertNotNull(feature);

        ReplicationReceiver replicationReceiver = new ReplicationReceiver();
        replicationReceiver.receive(feed, getCatalog());

        feature = getFeature(CITE_BRIDGES, "Bridges.1107531599613");
        assertNull(feature);
    }

    public void testInsert() throws Exception {
        BxmlStreamReader reader = super.getReader("replicationReceiverInsert");

        FeedDecoder decoder = new FeedDecoder();

        reader.nextTag();
        FeedImpl feed = decoder.decode(reader);

        Feature feature = getFeature(CITE_BRIDGES, "Bridges.1234568745412");
        if(feature != null){
            Filter filter = ff.id(Collections.singleton(ff.featureId("Bridges.1234568745412")));
            recordDeleteCommit(gss, CITE_BRIDGES, filter, "Deleted bridge");
            
        }

        ReplicationReceiver replicationReceiver = new ReplicationReceiver();
        replicationReceiver.receive(feed, getCatalog());

        feature = getFeature(CITE_BRIDGES, "Bridges.1234568745412");
        assertNotNull(feature);

    }

    private void assertFeature(Name featureName, String featureId, List<String> properties,
            List<Object> values) throws IOException {
        Feature feature = getFeature(featureName, featureId);

        for (int i = 0; i < properties.size(); i++) {
            Property propertyName = feature.getProperty(new NameImpl(properties.get(i)));
            assertEquals(values.get(i), propertyName.getValue());

        }

    }

    protected Feature getFeature(Name featureName, String featureId) throws IOException {
        
        FeatureSource<? extends FeatureType, ? extends Feature> featureSource = getCatalog()
                .getFeatureTypeByName(featureName).getFeatureSource(null, null);

        Filter filter = ff.id(Collections.singleton(ff.featureId(featureId)));

        FeatureCollection<? extends FeatureType, ? extends Feature> features = featureSource
                .getFeatures(filter);
        FeatureIterator<? extends Feature> iterator = features.features();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

}
