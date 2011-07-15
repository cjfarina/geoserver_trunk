package org.geoserver.bxml.atom;

import static org.geoserver.gss.internal.atom.Atom.content;

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

import org.geoserver.bxml.Encoder;
import org.geoserver.bxml.wfs_1_1.DeleteElementTypeEncoder;
import org.geoserver.bxml.wfs_1_1.InsertElementTypeEncoder;
import org.geoserver.bxml.wfs_1_1.UpdateElementTypeEncoder;
import org.gvsig.bxml.stream.BxmlStreamWriter;

public class ContentEncoder<T> extends AbstractAtomEncoder<T> {

    private static final Encoder<Object> NULL_VALUE_ENCODER = new Encoder<Object>() {
        @Override
        public void encode(Object value, BxmlStreamWriter w) {
            // do nothing
        }
    };

    private static Map<Class<?>, Encoder<?>> encoders = new HashMap<Class<?>, Encoder<?>>();
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

    /**
     * @param w
     * @param value
     * @throws XMLStreamException
     * @throws IOException
     */
    @Override
    public void encode(final T obj, final BxmlStreamWriter w) throws IOException {
        @SuppressWarnings("unchecked")
        Encoder<T> contentEncoder = (Encoder<T>) ContentEncoder.findEncoderFor(obj);
        startElement(w, content);
        {
            contentEncoder.encode(obj, w);
        }
        endElement(w);

    }

    static Encoder<? extends Object> findEncoderFor(Object value) throws IllegalArgumentException {
        if (null == value) {
            return NULL_VALUE_ENCODER;
        }
        Encoder<? extends Object> encoder = encoders.get(value.getClass());
        if (encoder == null) {
            throw new IllegalArgumentException("No content encoder found for value object of type "
                    + value.getClass().getName() + ". Known encoders: " + encoders.keySet());
        }
        return encoder;
    }

}
