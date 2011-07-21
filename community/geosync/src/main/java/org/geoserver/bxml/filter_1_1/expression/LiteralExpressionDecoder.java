package org.geoserver.bxml.filter_1_1.expression;

import static org.geotools.filter.v1_1.OGC.Literal;

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

public class LiteralExpressionDecoder implements Decoder<Expression> {

    protected static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
            .getDefaultHints());

    @Override
    public Expression decode(final BxmlStreamReader r) throws Exception {
        r.require(EventType.START_ELEMENT, Literal.getNamespaceURI(), Literal.getLocalPart());

        StringBuilder sb = new StringBuilder();
        Object value = null;
        EventType event;
        while ((event = r.next()).isValue()) {
            String chunk = r.getStringValue();
            sb.append(chunk);
        }
        r.require(EventType.END_ELEMENT, Literal.getNamespaceURI(), Literal.getLocalPart());

        value = sb.length() == 0 ? null : sb.toString();

        return ff.literal(value);
    }

    @Override
    public boolean canHandle(QName name) {
        return Literal.equals(name);
    }

    @Override
    public Set<QName> getTargets() {
        return Collections.singleton(Literal);
    }

}
