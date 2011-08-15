package org.geoserver.bxml;

import static org.gvsig.bxml.stream.EventType.END_ELEMENT;
import static org.gvsig.bxml.stream.EventType.START_ELEMENT;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

import com.google.common.collect.AbstractIterator;

public class BxmlElementIterator extends AbstractIterator<BxmlStreamReader> {

    private final BxmlStreamReader reader;

    private final Set<QName> siblingNames;

    private final int depth;

    public BxmlElementIterator(final BxmlStreamReader reader, final QName elemName) {
        this.reader = reader;
        this.siblingNames = Collections.singleton(elemName);
        this.depth = reader.getTagDepth();
    }

    public BxmlElementIterator(final BxmlStreamReader reader, final Set<QName> siblingNames) {
        this.reader = reader;
        this.siblingNames = new HashSet<QName>(siblingNames);
        this.depth = reader.getTagDepth();
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
    protected BxmlStreamReader computeNext() {
        try {
            EventType event;
            int tagDepth;
            event = reader.getEventType();
            if (!event.isTag()) {
                event = reader.nextTag();
            }
            tagDepth = reader.getTagDepth();

            while (true) {
                QName elementName = reader.getElementName();
                boolean isExpectedElement = isExpectedElement(elementName);

                if (tagDepth == this.depth && START_ELEMENT.equals(event) && isExpectedElement) {
                    return reader;
                }
                event = reader.nextTag();
                tagDepth = reader.getTagDepth();

                if ((END_ELEMENT.equals(event) && tagDepth == this.depth - 2) || finish(reader)) {
                    return endOfData();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected boolean isExpectedElement(QName elementName) {
        return siblingNames.contains(elementName);
    }

    protected boolean finish(BxmlStreamReader reader) {
        return false;
    }

}
