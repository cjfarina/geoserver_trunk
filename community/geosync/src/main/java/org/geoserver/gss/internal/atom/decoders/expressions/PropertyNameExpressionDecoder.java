package org.geoserver.gss.internal.atom.decoders.expressions;

import static org.geotools.filter.v1_1.OGC.PropertyName;

import java.io.IOException;

import org.opengis.filter.expression.Expression;

public class PropertyNameExpressionDecoder extends ExpressionLinkDecoder {

    private String property;
    
    public PropertyNameExpressionDecoder() {
        super(PropertyName);
    }
    
    @Override
    protected void setStringValue(String value) throws IOException {
        property = value;
    }

    @Override
    protected Expression buildResult() {
        return ff.property(property);
    }
}
