package org.geoserver.bxml.filter_1_1;

import static org.geotools.filter.v1_1.OGC.FeatureId;

import java.util.Map;

import javax.xml.namespace.QName;

import org.geoserver.bxml.AbstractDecoder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;

public class FeatureIdDecoder extends AbstractDecoder<FeatureId> {

    protected static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
            .getDefaultHints());
    
    private String entryId;
    
    public static final QName fid = new QName("http://www.opengis.net/ogc", "fid");
    
    public FeatureIdDecoder() {
        super(FeatureId);
    }
    
    @Override
    protected void decodeAttributtes(final BxmlStreamReader r, Map<QName, String> attributes)
            throws Exception {
        entryId = attributes.get(fid);
    }
    
    @Override
    protected FeatureId buildResult() {
        return ff.featureId(entryId);
    }
}
