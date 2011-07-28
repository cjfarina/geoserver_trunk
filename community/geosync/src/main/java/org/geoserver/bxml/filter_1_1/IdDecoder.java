package org.geoserver.bxml.filter_1_1;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geoserver.bxml.Decoder;
import org.geoserver.bxml.SequenceDecoder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.Id;
import org.opengis.filter.identity.FeatureId;

import com.google.common.collect.Iterators;

public class IdDecoder implements Decoder<Filter> {

    private static final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
            .getDefaultHints());

    private SequenceDecoder<FeatureId> sequence;

    public IdDecoder() {
        this.sequence = new SequenceDecoder<FeatureId>(1, Integer.MAX_VALUE);
        sequence.add(new FeatureIdDecoder(), 1, 1);
    }

    @Override
    public Id decode(BxmlStreamReader r) throws Exception {
        final Iterator<FeatureId> iterator = sequence.decode(r);
        final FeatureId[] identifiers = Iterators.toArray(iterator, FeatureId.class);

        return ff.id(new HashSet<FeatureId>(Arrays.asList(identifiers)));
    }

    @Override
    public boolean canHandle(QName name) {
        return sequence.canHandle(name);
    }

    @Override
    public Set<QName> getTargets() {
        return sequence.getTargets();
    }

}
