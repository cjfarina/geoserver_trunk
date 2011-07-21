package org.geoserver.bxml.filter_1_1.expression;

import static org.geotools.filter.v1_1.OGC.PropertyName;

import java.util.Collections;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geoserver.bxml.Decoder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;

public class PropertyNameExpressionDecoder implements Decoder<Expression> {

    protected static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
            .getDefaultHints());

    @Override
    public Expression decode(final BxmlStreamReader r) throws Exception {
        r.require(EventType.START_ELEMENT, PropertyName.getNamespaceURI(),
                PropertyName.getLocalPart());

        StringBuilder sb = new StringBuilder();
        while (r.next().isValue()) {
            String chunk = r.getStringValue();
            sb.append(chunk);
        }

        r.require(EventType.END_ELEMENT, PropertyName.getNamespaceURI(),
                PropertyName.getLocalPart());

        return ff.property(sb.toString());
    }

    @Override
    public boolean canHandle(QName name) {
        return PropertyName.equals(name);
    }

    @Override
    public Set<QName> getTargets() {
        return Collections.singleton(PropertyName);
    }
}
