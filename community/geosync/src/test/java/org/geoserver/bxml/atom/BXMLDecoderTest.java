package org.geoserver.bxml.atom;

import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.geoserver.bxml.BxmlTestSupport;
import org.geotools.filter.AttributeExpressionImpl;
import org.geotools.filter.LiteralExpressionImpl;
import org.opengis.filter.spatial.BinarySpatialOperator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

public abstract class BXMLDecoderTest extends BxmlTestSupport {

    protected void testSpatialBinaryOperation(BinarySpatialOperator comparisonOperator,
            String property, double[][] fs) {
        assertEquals(property,
                ((AttributeExpressionImpl) comparisonOperator.getExpression1()).getPropertyName());
        Polygon p = (Polygon) ((LiteralExpressionImpl) comparisonOperator.getExpression2())
                .getValue();
        LineString exteriorRing = p.getExteriorRing();
        testLineRing(exteriorRing, fs);
    }

    protected int getTimeZone() {
        return TimeZone.getDefault().getOffset(new Date().getTime()) / 3600000;
    }

}
