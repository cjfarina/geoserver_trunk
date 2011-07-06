package org.geoserver.gss.internal.atom.decoders.filters;

import java.io.IOException;
import java.util.Map;

import javax.xml.namespace.QName;

import org.geoserver.gss.internal.atom.FeedImpl;
import org.geoserver.gss.internal.atom.decoders.AbstractDecoder;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FilterType;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;

import static org.geotools.filter.v1_1.OGC.FeatureId;

public class FeatureIdFilterDecoder extends FilterLinkDecoder {

    private Id filter;

    public FeatureIdFilterDecoder() {
        super(FeatureId, new AndFilterDecoder());
        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
        Id filter = ff.createFidFilter();
    }

    protected void decodeElement(final BxmlStreamReader r) throws IOException {
        QName name = r.getElementName();
        System.out.println(name);
    }

    protected void decodeAttributtes(final BxmlStreamReader r, Map<QName, String> attributes)
            throws IOException {
        
        
    }

    @Override
    protected Filter buildResult() {
        return null;
    }

}
