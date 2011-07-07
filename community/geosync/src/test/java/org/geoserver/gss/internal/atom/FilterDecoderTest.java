package org.geoserver.gss.internal.atom;

import java.io.InputStream;
import java.util.List;

import junit.framework.TestCase;
import net.opengis.wfs.DeleteElementType;

import org.geoserver.gss.internal.atom.decoders.FeedDecoder;
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

public class FilterDecoderTest extends TestCase {

    public void testFilterDecoder(){
        try {

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

            DeleteElementType deleteElement = (DeleteElementType)content1.getValue();
            Filter filter = deleteElement.getFilter();
            assertNotNull(deleteElement.getFilter());
            Or orFilter = (Or)filter;
            
            List<Filter> orFilterChildrens = orFilter.getChildren();
            assertEquals(2, orFilterChildrens.size());
            
            PropertyIsEqualTo propertyIsEqualTo1 = (PropertyIsEqualTo)orFilterChildrens.get(0);
            assertEquals("addresses", ((AttributeExpressionImpl)propertyIsEqualTo1.getExpression1()).getPropertyName());
            assertEquals("Adrees1", ((LiteralExpressionImpl)propertyIsEqualTo1.getExpression2()).getValue());
            
            Not notFilter = (Not)orFilterChildrens.get(1);
            And andFilter = (And)notFilter.getFilter();
            
            List<Filter> andFilterChildrens = andFilter.getChildren();
            assertEquals(2, andFilterChildrens.size());
            
            PropertyIsBetween propertyIsBetween1 = (PropertyIsBetween)andFilterChildrens.get(0);
            assertEquals("depth", ((AttributeExpressionImpl)propertyIsBetween1.getExpression()).getPropertyName());
            assertEquals("100", ((LiteralExpressionImpl)propertyIsBetween1.getLowerBoundary()).getValue());
            assertEquals("200", ((LiteralExpressionImpl)propertyIsBetween1.getUpperBoundary()).getValue());
            
            PropertyIsEqualTo propertyIsEqualTo2 = (PropertyIsEqualTo)andFilterChildrens.get(1);
            assertEquals("street", ((AttributeExpressionImpl)propertyIsEqualTo2.getExpression1()).getPropertyName());
            assertEquals("Street1", ((LiteralExpressionImpl)propertyIsEqualTo2.getExpression2()).getValue());
            
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
