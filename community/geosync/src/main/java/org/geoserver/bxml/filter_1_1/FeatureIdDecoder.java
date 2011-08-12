package org.geoserver.bxml.filter_1_1;

import java.util.Collections;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geoserver.bxml.Decoder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.filter.v1_1.OGC;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;

public class FeatureIdDecoder implements Decoder<FeatureId> {

    protected static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
            .getDefaultHints());

    private static final QName fid = new QName(OGC.NAMESPACE, "fid");

    public FeatureIdDecoder() {
    }

    @Override
    public FeatureId decode(final BxmlStreamReader r) throws Exception {
        r.require(EventType.START_ELEMENT, OGC.FeatureId.getNamespaceURI(),
                OGC.FeatureId.getLocalPart());
        final String id = r.getAttributeValue(null, fid.getLocalPart());

        r.nextTag();
        r.require(EventType.END_ELEMENT, OGC.FeatureId.getNamespaceURI(),
                OGC.FeatureId.getLocalPart());

        return ff.featureId(id);
    }

    @Override
    public boolean canHandle(final QName name) {
        return OGC.FeatureId.equals(name);
    }

    @Override
    public Set<QName> getTargets() {
        return Collections.singleton(OGC.FeatureId);
    }
}
