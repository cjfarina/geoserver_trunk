package org.geoserver.bxml;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;
import org.springframework.util.Assert;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterators;

public class SequenceDecoder<T> implements Decoder<Iterator<T>> {

    private List<Particle<T>> sequence;

    private final int minOccurs;

    private final int maxOccurs;

    public SequenceDecoder() {
        this(1, 1);
    }

    public SequenceDecoder(final int minOccurs, final int maxOccurs) {
        this.minOccurs = minOccurs;
        this.maxOccurs = maxOccurs;
        Assert.isTrue(minOccurs >= 0);
        Assert.isTrue(maxOccurs >= 0);
        Assert.isTrue(minOccurs <= maxOccurs);
        this.sequence = new LinkedList<Particle<T>>();
    }

    public void add(final Decoder<T> particleDecoder, final int minOccurs, final int maxOccurs) {
        Assert.notNull(particleDecoder);
        Assert.isTrue(minOccurs >= 0);
        Assert.isTrue(maxOccurs >= 0);
        Assert.isTrue(minOccurs <= maxOccurs);
        sequence.add(new Particle<T>(particleDecoder, minOccurs, maxOccurs));
    }

    @Override
    public Iterator<T> decode(final BxmlStreamReader r) throws Exception {
        r.require(EventType.START_ELEMENT, null, null);

        Set<QName> sequenceNames = getTargets();
        BxmlElementIterator xmlIterator = buildIterator(r, sequenceNames);

        Chain<T> chain = new Chain<T>(sequence, minOccurs, maxOccurs);
        Iterator<T> result = Iterators.transform(xmlIterator, chain);
        return result;
    }

    protected BxmlElementIterator buildIterator(final BxmlStreamReader r, Set<QName> sequenceNames) {
        return new BxmlElementIterator(r, new HashSet<QName>(sequenceNames));
    }

    /**
     * @see org.geoserver.bxml.Decoder#getTargets()
     */
    public Set<QName> getTargets() {
        Set<QName> names = new HashSet<QName>();
        for (Particle<T> p : sequence) {
            names.addAll(p.particleDecoder.getTargets());
        }
        return names;
    }

    @Override
    public boolean canHandle(final QName name) {
        for (Particle<T> p : sequence) {
            if (p.particleDecoder.canHandle(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @author groldan
     * 
     * @param <E>
     */
    private static class Particle<E> {

        private final Decoder<E> particleDecoder;

        private final int minOccurs;

        private final int maxOccurs;

        public Particle(Decoder<E> particleDecoder, int minOccurs, int maxOccurs) {
            this.particleDecoder = particleDecoder;
            this.minOccurs = minOccurs;
            this.maxOccurs = maxOccurs;
        }

    }

    private static class Chain<E> implements Function<BxmlStreamReader, E> {

        private final int sequenceMinOccurs;

        private final int sequenceMaxOccurs;

        private int sequenceOccurrencies;

        private final List<Particle<E>> sequence;

        private int currentParticle;

        private int currentParticleOccurrencies;

        public Chain(List<Particle<E>> sequence, int minOccurs, int maxOccurs) {
            this.sequenceMinOccurs = minOccurs;
            this.sequenceMaxOccurs = maxOccurs;
            this.sequenceOccurrencies = 0;
            this.sequence = sequence;
            this.currentParticle = -1;
        }

        @Override
        public E apply(final BxmlStreamReader positionedReader) {
            final QName elementName = positionedReader.getElementName();
            EventType eventType = positionedReader.getEventType();
            final Decoder<E> particleDecoder = findParticleDecoder(elementName);
            E decoded = null;
            try {
                decoded = particleDecoder.decode(positionedReader);
            } catch (Exception e) {
                Throwables.propagate(e);
            }
            return decoded;
        }

        private Decoder<E> findParticleDecoder(final QName elementName) {
            while (true) {
                if (currentParticle == -1 || currentParticle == sequence.size()) {
                    currentParticle = 0;
                    currentParticleOccurrencies = 0;
                    sequenceOccurrencies++;
                }
                Particle<E> p = sequence.get(currentParticle);
                if (currentParticleOccurrencies == p.maxOccurs) {
                    currentParticle++;
                    currentParticleOccurrencies = 0;
                    continue;
                }

                if (p.particleDecoder.canHandle(elementName)) {
                    currentParticleOccurrencies++;
                    if (currentParticleOccurrencies > p.maxOccurs) {
                        throw new IllegalStateException();
                    }
                    return p.particleDecoder;
                }
                if (currentParticleOccurrencies < p.minOccurs) {
                    throw new IllegalStateException();
                }
            }
        }
    }
}
