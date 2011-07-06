package org.geoserver.gss.internal.atom.decoders.expressions;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.geoserver.gss.internal.atom.decoders.AbstractDecoder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;

public abstract class ExpressionLinkDecoder extends AbstractDecoder<Expression> {

    protected ExpressionLinkDecoder expressionLink;

    protected static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
            .getDefaultHints());

    public ExpressionLinkDecoder(final QName name) {
        super(name);
    }

    public ExpressionLinkDecoder(final QName name, final ExpressionLinkDecoder filterLink) {
        this(name);
        this.expressionLink = filterLink;
    }

    public Expression decode(final BxmlStreamReader r) throws IOException {
        QName name = r.getElementName();

        if (this.name.equals(name)) {
            return super.decode(r);
        } else if (expressionLink != null) {
            return expressionLink.decode(r);
        } else {
            return null;
        }
    }
}
