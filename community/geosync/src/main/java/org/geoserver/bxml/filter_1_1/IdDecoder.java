package org.geoserver.bxml.filter_1_1;

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
import org.opengis.filter.identity.Identifier;

public class IdDecoder implements Decoder<Filter> {

    private static final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
            .getDefaultHints());

    private SequenceDecoder<Identifier> sequence;

    public IdDecoder() {
        this.sequence = new SequenceDecoder<Identifier>(1, Integer.MAX_VALUE);
    }

    @Override
    public Id decode(BxmlStreamReader r) throws Exception {
        Set<Identifier> identifiers = new HashSet<Identifier>();

        Iterator<Identifier> ids = sequence.decode(r);
        while (ids.hasNext()) {
            Identifier identifier = ids.next();
            identifiers.add(identifier);
        }
        Id id = ff.id(identifiers);
        return id;
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
