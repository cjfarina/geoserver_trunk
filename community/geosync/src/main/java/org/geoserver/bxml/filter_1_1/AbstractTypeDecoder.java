package org.geoserver.bxml.filter_1_1;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.geoserver.bxml.Decoder;
import org.geoserver.bxml.atom.AbstractAtomEncoder;
import org.geotools.util.logging.Logging;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;
import org.springframework.util.Assert;

public abstract class AbstractTypeDecoder<T> implements Decoder<T> {

    private final Set<QName> canHandle;

    protected static final Logger LOGGER = Logging.getLogger(AbstractAtomEncoder.class);

    public AbstractTypeDecoder(final QName... names) {
        Set<QName> nameSet = new HashSet<QName>(Arrays.asList(names));
        canHandle = Collections.unmodifiableSet(nameSet);
    }

    @Override
    public boolean canHandle(QName name) {
        return canHandle.contains(name);
    }

    @Override
    public Set<QName> getTargets() {
        return canHandle;
    }

    @Override
    public T decode(final BxmlStreamReader r) throws Exception {
        r.require(EventType.START_ELEMENT, null, null);
        final QName name = r.getElementName();
        Assert.isTrue(canHandle(name));

        T result = decodeInternal(r, name);

        r.require(EventType.END_ELEMENT, name.getNamespaceURI(), name.getLocalPart());
        return result;
    }

    protected abstract T decodeInternal(BxmlStreamReader r, QName name) throws Exception;

}
