package org.geoserver.bxml.filter_1_1;

import static org.geotools.filter.v1_1.OGC.GmlObjectId;

import java.util.Collections;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geoserver.bxml.Decoder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.gml3.GML;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.GmlObjectId;

public class GmlObjectIdDecoder implements Decoder<GmlObjectId> {

    protected static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
            .getDefaultHints());

    public GmlObjectIdDecoder() {
    }

    @Override
    public GmlObjectId decode(BxmlStreamReader r) throws Exception {
        r.require(EventType.START_ELEMENT, GmlObjectId.getNamespaceURI(),
                GmlObjectId.getLocalPart());

        String id = r.getAttributeValue(null, GML.id.getLocalPart());

        GmlObjectId gmlId = ff.gmlObjectId(id);

        r.nextTag();
        r.require(EventType.END_ELEMENT, GmlObjectId.getNamespaceURI(), GmlObjectId.getLocalPart());
        return gmlId;
    }

    @Override
    public boolean canHandle(QName name) {
        return GmlObjectId.equals(name);
    }

    @Override
    public Set<QName> getTargets() {
        return Collections.singleton(GmlObjectId);
    }
}
