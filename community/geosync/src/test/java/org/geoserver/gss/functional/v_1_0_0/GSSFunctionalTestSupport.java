package org.geoserver.gss.functional.v_1_0_0;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.data.test.MockData;
import org.geoserver.gss.GSSTestSupport;
import org.geoserver.gss.impl.AuthenticationResolver;
import org.geoserver.gss.impl.GSS;
import org.geoserver.platform.GeoServerExtensions;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.NameImpl;
import org.geotools.util.logging.Logging;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.PropertyName;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

public abstract class GSSFunctionalTestSupport extends GSSTestSupport {

    protected static final Logger LOGGER = Logging.getLogger("org.geoserver.gss.functional");

    protected static final QName REPLICATED_TYPE_1 = MockData.BRIDGES;

    @Override
    public void oneTimeSetUp() throws Exception {
        super.oneTimeSetUp();

        GSS gss = GeoServerExtensions.bean(GSS.class, applicationContext);
        gss.setAuthenticationResolver(new AuthenticationResolver() {
            @Override
            public String getCurrentUserName() {
                return "admin";
            }
        });

        LOGGER.info("Importing FeatureType as versioned: " + REPLICATED_TYPE_1);
        NameImpl featureTypeName = new NameImpl(REPLICATED_TYPE_1);
        Future<Void> future = gss.initialize(featureTypeName);
        future.get();// lock until imported
        assertTrue(gss.isReplicated(featureTypeName));

        FeatureTypeInfo typeInfo = getCatalog().getFeatureTypeByName(featureTypeName);
        SimpleFeatureStore store = (SimpleFeatureStore) typeInfo.getFeatureSource(null, null);

        GeometryFactory gf = new GeometryFactory();

        Filter filter = Filter.INCLUDE;
        List<PropertyName> updatedProperties = Arrays.asList(ff.property("NAME"),
                ff.property("the_geom"));
        List<Object> newValues = Arrays.asList("Cam Bridge2",
                (Object) gf.createPoint(new Coordinate(3, 4)));

        store.modifyFeatures(new String[] { "NAME", "the_geom" }, newValues.toArray(), filter);
        
        FeatureCollection affectedFeatures = store.getFeatures();

        LOGGER.info("Creating commit with one update op in feature type: " + REPLICATED_TYPE_1);
        gss.stageUpdate("t1", featureTypeName, filter, updatedProperties, newValues,
                affectedFeatures);

        gss.commitChangeSet("t1", "Change Cam Bridge");
        LOGGER.info("Update committed");

        
    }
}
