package org.geoserver.bxml.atom;

import static org.gvsig.bxml.stream.EventType.END_DOCUMENT;
import static org.gvsig.bxml.stream.EventType.START_ELEMENT;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

import com.google.common.base.Throwables;

public class BxmlElementIterator implements Iterator<BxmlStreamReader> {

    private final BxmlStreamReader reader;

    private final QName elemName;

    public BxmlElementIterator(BxmlStreamReader reader, QName elemName) {
        this.reader = reader;
        this.elemName = elemName;
    }

    /**
     * If not positioned at an start element for the element name this iterator looks for, advances
     * the reader until the next start element for that element name is found, or the end of the
     * stream is reached.
     * 
     * @return {@code true} if there's another element to be read, {@code false} if the end of the
     *         stream was reached.
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
        EventType event = reader.getEventType();
        try {
            while (!END_DOCUMENT.equals(event)
                    && !(START_ELEMENT.equals(event) && elemName.equals(reader.getElementName()))) {
                event = reader.next();
            }
        } catch (IOException e) {
            Throwables.propagate(e);
        }

        return !END_DOCUMENT.equals(event);
    }

    /**
     * Returns the stream reader if {@link #hasNext()}
     * 
     * @see java.util.Iterator#next()
     */
    @Override
    public BxmlStreamReader next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        reader.require(START_ELEMENT, elemName.getNamespaceURI(), elemName.getLocalPart());
        return reader;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
