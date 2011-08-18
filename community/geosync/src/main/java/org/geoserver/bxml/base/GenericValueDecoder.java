package org.geoserver.bxml.base;

import org.geoserver.bxml.ChoiceDecoder;
import org.geoserver.bxml.ValueDecoder;
import org.geoserver.bxml.gml_3_1.EnvelopeDecoder;
import org.geoserver.bxml.gml_3_1.GeometryDecoder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

/**
 * Decodes the value of an element. This value can be a GML or a primitive value.
 * 
 * @author cfarina
 */
public class GenericValueDecoder implements ValueDecoder<Object> {

    /**
     * Decode.
     * 
     * @param r the r
     * @return the object
     * @throws Exception the exception
     */
    @Override
    public Object decode(BxmlStreamReader r) throws Exception {
        EventType event = r.next();
        Object value = null;
        if (r.getEventType().isValue()) {
            value = new PrimitivesValueDecoder().decode(r);
            event = r.getEventType();
        }

        if (EventType.START_ELEMENT == event) {
            @SuppressWarnings("rawtypes")
            ChoiceDecoder choice = new ChoiceDecoder<Object>();
            choice.addOption(new GeometryDecoder());
            choice.addOption(new EnvelopeDecoder());
            value = choice.decode(r);
            r.nextTag();
        }
        return value;
    }

    /**
     * Can handle.
     * 
     * @param type the type
     * @return true, if successful
     */
    @Override
    public boolean canHandle(EventType type) {
        return true;
    }

}
