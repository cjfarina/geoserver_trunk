package org.geoserver.bxml.filter_1_1;

import static org.geotools.filter.v1_1.OGC.FeatureId;
import static org.geotools.filter.v1_1.OGC.Filter;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geoserver.bxml.AbstractDecoder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.filter.MalformedFilterException;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;

public class FilterChainDecoder extends AbstractDecoder<Filter> {

    private AndFilterDecoder filterLink;

    private Filter filter;

    private Set<FeatureId> identifiers = new HashSet<FeatureId>();

    private static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
            .getDefaultHints());

    public FilterChainDecoder(QName name) {
        super(Filter);
        filterLink = new AndFilterDecoder();
    }

    public Filter decodeFilter(final BxmlStreamReader r) throws Exception {
        if (filterLink != null) {
            return filterLink.decode(r);
        } else {
            return null;
        }
    }

    protected void decodeElement(final BxmlStreamReader r) throws Exception {
        QName name = r.getElementName();

        if (FeatureId.equals(name)) {
            if (filter != null) {
                throw new MalformedFilterException(
                        "The <_Id> element can't be in the same Filter than a <logicalOps>, <comparisonOps> and <spatialOps>");
            }
            FeatureIdDecoder featureIdDecoder = new FeatureIdDecoder();
            identifiers.add(featureIdDecoder.decode(r));
        } else {
            if (identifiers.size() > 0) {
                throw new MalformedFilterException(
                        "The <_Id> element can't be in the same Filter than a <logicalOps>, <comparisonOps> and <spatialOps>");
            }
            filter = decodeFilter(r);
        }
    }

    @Override
    protected Filter buildResult() {
        if (identifiers.size() > 0) {
            return ff.id(identifiers);
        } else if (filter != null) {
            return filter;
        } else {
            return null;
        }
    }
}
