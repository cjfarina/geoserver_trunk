package org.geoserver.gss.internal.atom;

import java.util.Date;
import java.util.TimeZone;

import org.geotools.filter.AttributeExpressionImpl;
import org.geotools.filter.LiteralExpressionImpl;
import org.opengis.filter.spatial.BinarySpatialOperator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

import junit.framework.TestCase;

public class BXMLDecoderTest extends TestCase {

    protected void testLineRing(LineString exteriorRing, float[][] fs) {
        for (int i = 0; i < fs.length; i++) {
            Coordinate coordinate = exteriorRing.getCoordinateN(i);
            assertEquals(fs[i][0], coordinate.x, 0.01);
            assertEquals(fs[i][1], coordinate.y, 0.01);
        }
    }
    
    protected void testSpatialBinaryOperation(BinarySpatialOperator comparisonOperator,
            String property, float[][] fs) {
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
