package org.geoserver.bxml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;
import org.springframework.util.Assert;

public class ChoiceDecoder<T> implements Decoder<T> {

    private final List<Decoder<T>> options;

    public ChoiceDecoder() {
        this.options = new ArrayList<Decoder<T>>();
    }

    public ChoiceDecoder(Decoder<T>... options) {
        this.options = new ArrayList<Decoder<T>>();
        if (options != null && options.length > 0) {
            Assert.noNullElements(options);
            this.options.addAll(Arrays.asList(options));
        }
    }

    public void addOption(Decoder<T> option) {
        this.options.add(option);
    }

    @Override
    public T decode(final BxmlStreamReader r) throws Exception {
        r.require(EventType.START_ELEMENT, null, null);
        final QName name = r.getElementName();
        for (Decoder<T> decoder : options) {
            if (decoder.canHandle(name)) {
                return decoder.decode(r);
            }
        }
        throw new IllegalArgumentException("No decoder found for " + name);
    }

    @Override
    public boolean canHandle(QName name) {
        for (Decoder<T> decoder : options) {
            if (decoder.canHandle(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<QName> getTargets() {
        Set<QName> targets = new HashSet<QName>();
        for (Decoder<T> decoder : options) {
            targets.addAll(decoder.getTargets());
        }
        return targets;
    }
}
