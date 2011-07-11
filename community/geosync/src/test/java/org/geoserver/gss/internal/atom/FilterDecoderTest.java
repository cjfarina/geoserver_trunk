package org.geoserver.gss.internal.atom;

import java.io.InputStream;
import java.util.List;

import junit.framework.TestCase;
import net.opengis.wfs.DeleteElementType;

import org.geoserver.bxml.atom.FeedDecoder;
import org.geotools.filter.AttributeExpressionImpl;
import org.geotools.filter.LiteralExpressionImpl;
import org.gvsig.bxml.stream.BxmlFactoryFinder;
import org.gvsig.bxml.stream.BxmlInputFactory;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNotEqualTo;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Add;
import org.opengis.filter.expression.Divide;
import org.opengis.filter.expression.Multiply;
import org.opengis.filter.expression.Subtract;

public class FilterDecoderTest extends TestCase {

    public void testFilterDecoder() throws Exception {

        final InputStream input = getClass().getResourceAsStream(
                "/test-data/gss/1.0.0/examples/transactions/filter.bxml");
        BxmlStreamReader reader;
        BxmlInputFactory inputFactory = BxmlFactoryFinder.newInputFactory();
        inputFactory.setNamespaceAware(true);
        reader = inputFactory.createScanner(input);
        FeedDecoder feedDecoder = new FeedDecoder();
        FeedImpl feed = feedDecoder.decode(reader);

        assertEquals(new Long(50), feed.getMaxEntries());
        assertEquals(new Long(1), feed.getStartPosition());

        assertEquals("01cbd610bbf9e37714980377ffc6600dc3fef24e", feed.getId());
        EntryImpl entry = feed.getEntry().next();

        assertEquals("453e5a7ba8917ed3550e088d69ff1ac0aa6d1a4d", entry.getId());

        assertNotNull(entry.getContent());
        ContentImpl content1 = entry.getContent();
        assertEquals("type1", content1.getType());
        assertEquals("source1", content1.getSrc());

        DeleteElementType deleteElement = (DeleteElementType) content1.getValue();
        Filter filter = deleteElement.getFilter();
        assertNotNull(deleteElement.getFilter());
        Or orFilter = (Or) filter;

        List<Filter> orFilterChildrens = orFilter.getChildren();
        assertEquals(2, orFilterChildrens.size());

        PropertyIsEqualTo propertyIsEqualTo1 = (PropertyIsEqualTo) orFilterChildrens.get(0);
        assertEquals("addresses",
                ((AttributeExpressionImpl) propertyIsEqualTo1.getExpression1()).getPropertyName());
        assertEquals("Adrees1",
                ((LiteralExpressionImpl) propertyIsEqualTo1.getExpression2()).getValue());

        Not notFilter = (Not) orFilterChildrens.get(1);
        And andFilter = (And) notFilter.getFilter();

        List<Filter> andFilterChildrens = andFilter.getChildren();
        assertEquals(13, andFilterChildrens.size());

        PropertyIsEqualTo propertyIsEqualTo2 = (PropertyIsEqualTo) andFilterChildrens.get(0);
        assertEquals("street",
                ((AttributeExpressionImpl) propertyIsEqualTo2.getExpression1()).getPropertyName());
        assertEquals("Street1",
                ((LiteralExpressionImpl) propertyIsEqualTo2.getExpression2()).getValue());

        PropertyIsNotEqualTo propertyIsNotEqualTo = (PropertyIsNotEqualTo) andFilterChildrens
                .get(1);
        assertEquals("name",
                ((AttributeExpressionImpl) propertyIsNotEqualTo.getExpression1()).getPropertyName());
        assertEquals("Name1",
                ((LiteralExpressionImpl) propertyIsNotEqualTo.getExpression2()).getValue());

        PropertyIsLessThan propertyIsLessThan = (PropertyIsLessThan) andFilterChildrens.get(2);
        assertEquals("age",
                ((AttributeExpressionImpl) propertyIsLessThan.getExpression1()).getPropertyName());
        assertEquals("26", ((LiteralExpressionImpl) propertyIsLessThan.getExpression2()).getValue());

        PropertyIsGreaterThan propertyIsGreaterThan = (PropertyIsGreaterThan) andFilterChildrens
                .get(3);
        assertEquals("age2",
                ((AttributeExpressionImpl) propertyIsGreaterThan.getExpression1())
                        .getPropertyName());
        assertEquals("30",
                ((LiteralExpressionImpl) propertyIsGreaterThan.getExpression2()).getValue());

        PropertyIsLessThanOrEqualTo propertyIsLessThanOrEqualTo = (PropertyIsLessThanOrEqualTo) andFilterChildrens
        .get(4);
        assertEquals("age4",
                ((AttributeExpressionImpl) propertyIsLessThanOrEqualTo.getExpression1())
                .getPropertyName());
        assertEquals("37",
                ((LiteralExpressionImpl) propertyIsLessThanOrEqualTo.getExpression2()).getValue());

        PropertyIsGreaterThanOrEqualTo propertyIsGreaterThanOrEqualTo = (PropertyIsGreaterThanOrEqualTo) andFilterChildrens
                .get(5);
        assertEquals("age5",
                ((AttributeExpressionImpl) propertyIsGreaterThanOrEqualTo.getExpression1())
                        .getPropertyName());
        assertEquals("45",
                ((LiteralExpressionImpl) propertyIsGreaterThanOrEqualTo.getExpression2()).getValue());
        
        PropertyIsLike propertyIsLike = (PropertyIsLike) andFilterChildrens
        .get(6);
        assertEquals("name7",
                ((AttributeExpressionImpl) propertyIsLike.getExpression())
                .getPropertyName());
        assertEquals("albert", propertyIsLike.getLiteral());
        
        PropertyIsNull propertyIsNull = (PropertyIsNull) andFilterChildrens
        .get(7);
        assertEquals("propertyNull",
                ((AttributeExpressionImpl) propertyIsNull.getExpression())
                .getPropertyName());
        
        PropertyIsBetween propertyIsBetween1 = (PropertyIsBetween) andFilterChildrens.get(8);
        assertEquals("depth",
                ((AttributeExpressionImpl) propertyIsBetween1.getExpression()).getPropertyName());
        assertEquals("100",
                ((LiteralExpressionImpl) propertyIsBetween1.getLowerBoundary()).getValue());
        assertEquals("200",
                ((LiteralExpressionImpl) propertyIsBetween1.getUpperBoundary()).getValue());

        PropertyIsEqualTo propertyIsEqualTo3 = (PropertyIsEqualTo) andFilterChildrens.get(9);
        assertEquals("addFilter",
                ((AttributeExpressionImpl) propertyIsEqualTo3.getExpression1()).getPropertyName());
        Add add = (Add)propertyIsEqualTo3.getExpression2();
        assertEquals("property1",
                ((AttributeExpressionImpl) add.getExpression1()).getPropertyName());
        assertEquals("100",
                ((LiteralExpressionImpl) add.getExpression2()).getValue());
        
        PropertyIsEqualTo propertyIsEqualTo4 = (PropertyIsEqualTo) andFilterChildrens.get(10);
        assertEquals("subFilter",
                ((AttributeExpressionImpl) propertyIsEqualTo4.getExpression1()).getPropertyName());
        Subtract sub = (Subtract)propertyIsEqualTo4.getExpression2();
        assertEquals("property2",
                ((AttributeExpressionImpl) sub.getExpression1()).getPropertyName());
        assertEquals("159",
                ((LiteralExpressionImpl) sub.getExpression2()).getValue());
        
        PropertyIsEqualTo propertyIsEqualTo5 = (PropertyIsEqualTo) andFilterChildrens.get(11);
        assertEquals("mulFilter",
                ((AttributeExpressionImpl) propertyIsEqualTo5.getExpression1()).getPropertyName());
        Multiply mul = (Multiply)propertyIsEqualTo5.getExpression2();
        assertEquals("property3",
                ((AttributeExpressionImpl) mul.getExpression1()).getPropertyName());
        assertEquals("543",
                ((LiteralExpressionImpl) mul.getExpression2()).getValue());
        
        PropertyIsEqualTo propertyIsEqualTo6 = (PropertyIsEqualTo) andFilterChildrens.get(12);
        assertEquals("divFilter",
                ((AttributeExpressionImpl) propertyIsEqualTo6.getExpression1()).getPropertyName());
        Divide div = (Divide)propertyIsEqualTo6.getExpression2();
        assertEquals("property4",
                ((AttributeExpressionImpl) div.getExpression1()).getPropertyName());
        assertEquals("45",
                ((LiteralExpressionImpl) div.getExpression2()).getValue());
        
        reader.close();

    }
}
