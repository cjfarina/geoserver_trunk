package org.geoserver.bxml;

import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;

import junit.framework.TestCase;

import org.geotools.filter.AttributeExpressionImpl;
import org.geotools.filter.LiteralExpressionImpl;
import org.gvsig.bxml.adapt.stax.XmlStreamReaderAdapter;
import org.gvsig.bxml.stream.BxmlFactoryFinder;
import org.gvsig.bxml.stream.BxmlInputFactory;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.spatial.BinarySpatialOperator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

public abstract class BxmlTestSupport extends TestCase {

    Boolean isBinary = false;
    protected BxmlStreamReader getReader(final String resource) throws Exception {
        String isBinaryString = System.getProperty("isBinaryXML");
        if(isBinaryString != null){
            isBinary = Boolean.parseBoolean(isBinaryString);
            
        }
        String resourceName = isBinary? resource + ".bxml": resource + ".xml";
        final InputStream input = getClass().getResourceAsStream(resourceName);
        assertNotNull(resourceName + " not found by " + getClass().getName(), input);
        if(isBinary){
            return getBmlReader(input);
        } else {
            return getXmlReader(input);
        }
    }

    protected BxmlStreamReader getBmlReader(final InputStream input) throws Exception {
        BxmlInputFactory factory = BxmlFactoryFinder.newInputFactory();

        factory.setNamespaceAware(true);
        BxmlStreamReader reader = factory.createScanner(input);
        return reader;
    }

    protected BxmlStreamReader getXmlReader(final InputStream input) throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();

        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
        BxmlStreamReader reader = new XmlStreamReaderAdapter(factory, input);
        return reader;
    }

    protected void testLineRing(LineString exteriorRing, double[][] fs) {
        for (int i = 0; i < fs.length; i++) {
            Coordinate coordinate = exteriorRing.getCoordinateN(i);
            assertEquals(fs[i][0], coordinate.x, 0.01);
            assertEquals(fs[i][1], coordinate.y, 0.01);
        }
    }

    protected void testSpatialBinaryOperation(BinarySpatialOperator comparisonOperator,
            String property, double[][] fs) {
        assertEquals(property,
                ((AttributeExpressionImpl) comparisonOperator.getExpression1()).getPropertyName());
        Polygon p = (Polygon) ((LiteralExpressionImpl) comparisonOperator.getExpression2())
                .getValue();
        LineString exteriorRing = p.getExteriorRing();
        testLineRing(exteriorRing, fs);
    }

}