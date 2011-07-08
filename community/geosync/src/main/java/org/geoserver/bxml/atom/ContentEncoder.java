package org.geoserver.bxml.atom;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import net.opengis.wfs.DeleteElementType;
import net.opengis.wfs.InsertElementType;
import net.opengis.wfs.UpdateElementType;
import net.opengis.wfs.impl.DeleteElementTypeImpl;
import net.opengis.wfs.impl.InsertElementTypeImpl;
import net.opengis.wfs.impl.UpdateElementTypeImpl;

import org.geoserver.bxml.AbstractEncoder;
import org.geoserver.bxml.feature.DeleteElementTypeEncoder;
import org.geoserver.bxml.feature.InsertElementTypeEncoder;
import org.geoserver.bxml.feature.UpdateElementTypeEncoder;
import org.gvsig.bxml.stream.BxmlStreamWriter;

public abstract class ContentEncoder extends AbstractEncoder {

    private static final ContentEncoder NULL_VALUE_ENCODER = new ContentEncoder() {
        @Override
        public void encode(BxmlStreamWriter w, Object value) {
            // do nothing
        }
    };

    private static Map<Class<?>, ContentEncoder> encoders = new HashMap<Class<?>, ContentEncoder>();
    static {
        InsertElementTypeEncoder insertEncoder = new InsertElementTypeEncoder();
        UpdateElementTypeEncoder updateEncoder = new UpdateElementTypeEncoder();
        DeleteElementTypeEncoder deleteEncoder = new DeleteElementTypeEncoder();
        encoders.put(InsertElementType.class, insertEncoder);
        encoders.put(InsertElementTypeImpl.class, insertEncoder);
        encoders.put(UpdateElementType.class, updateEncoder);
        encoders.put(UpdateElementTypeImpl.class, updateEncoder);
        encoders.put(DeleteElementType.class, deleteEncoder);
        encoders.put(DeleteElementTypeImpl.class, deleteEncoder);
    }

    public static ContentEncoder findEncoderFor(Object value) throws IllegalArgumentException {
        if (null == value) {
            return NULL_VALUE_ENCODER;
        }
        ContentEncoder encoder = encoders.get(value.getClass());
        if (encoder == null) {
            throw new IllegalArgumentException("No content encoder found for value object of type "
                    + value.getClass().getName() + ". Known encoders: " + encoders.keySet());
        }
        return encoder;
    }

    /**
     * @param w
     * @param value
     * @throws XMLStreamException
     * @throws IOException
     */
    public abstract void encode(BxmlStreamWriter w, Object value) throws IOException;

}
