package org.geoserver.bxml.wfs_1_1;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geoserver.bxml.Decoder;
import org.geoserver.bxml.base.GenericValueDecoder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

/**
 * The Class SimpleFeatureAttributeDecoder.
 * 
 * @author cfarina
 */
public class SimpleFeatureAttributeDecoder implements Decoder<Object> {

    /** The namespace. */
    private final String namespace;

    /**
     * Instantiates a new simple feature attribute decoder.
     * 
     * @param namespace the namespace
     */
    public SimpleFeatureAttributeDecoder(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Decode.
     * 
     * @param r the r
     * @return the object
     * @throws Exception the exception
     */
    @Override
    public Object decode(BxmlStreamReader r) throws Exception {
        r.require(EventType.START_ELEMENT, null, null);
        Object value = new GenericValueDecoder().decode(r);
        r.require(EventType.END_ELEMENT, null, null);
        return value;
    }

    /**
     * Can handle.
     * 
     * @param name the name
     * @return true, if successful
     */
    @Override
    public boolean canHandle(QName name) {
        return namespace.equals(name.getNamespaceURI());
    }

    /**
     * Gets the targets.
     * 
     * @return the targets
     */
    @Override
    public Set<QName> getTargets() {
        return new HashSet<QName>();
    }

}
