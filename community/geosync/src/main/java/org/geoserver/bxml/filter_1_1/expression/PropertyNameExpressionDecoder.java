package org.geoserver.bxml.filter_1_1.expression;

import static org.geotools.filter.v1_1.OGC.PropertyName;

import org.opengis.filter.expression.Expression;

public class PropertyNameExpressionDecoder extends ExpressionLinkDecoder {

    private String property;

    public PropertyNameExpressionDecoder() {
        super(PropertyName, new AddExpressionDecoder());
    }

    @Override
    protected void setStringValue(String value) throws Exception {
        property = value;
    }

    @Override
    protected Expression buildResult() {
        return ff.property(property);
    }
}
