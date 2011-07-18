package org.geoserver.bxml.filter_1_1;

import static org.geotools.filter.v1_1.OGC.Filter;
import static org.geotools.filter.v1_1.OGC.FeatureId;

import java.util.Set;

import javax.xml.namespace.QName;

import org.geoserver.bxml.AbstractDecoder;
import org.geoserver.bxml.Decoder;
import org.geoserver.bxml.filter_1_1.spatial.BBOXFilterDecoder;
import org.geoserver.bxml.filter_1_1.spatial.BinarySpatialOperationDecoder;
import org.geoserver.bxml.filter_1_1.spatial.DistanceBufferFilterDecoder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.filter.MalformedFilterException;
import org.gvsig.bxml.stream.BxmlStreamReader;
import java.util.HashSet;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;

public class FilterDecoder extends AbstractDecoder<Filter> {

    private Decoder<Filter> chain;

    private Filter filter;

    private Set<FeatureId> identifiers = new HashSet<FeatureId>();

    protected static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
            .getDefaultHints());

    public FilterDecoder() {
        setup();
    }

    @SuppressWarnings("unchecked")
    private void setup() {
        this.chain = new ChoiceDecoder<Filter>(
                new BinaryComparisonOperatorDecoder(), //
                new PropertyIsBetweenFilterDecoder(), new PropertyIsLikeFilterDecoder(),
                new PropertyIsNullFilterDecoder(), new LogicOperatorDecoder(),
                new DistanceBufferFilterDecoder(), new BinarySpatialOperationDecoder(),
                new BBOXFilterDecoder());
    }

    @Override
    public Filter decode(BxmlStreamReader r) throws Exception {
        if (Filter.equals(r.getElementName())) {
            return super.decode(r);
        } else {
            return chain.decode(r);
        }
    }

    @Override
    public void decodeElement(final BxmlStreamReader r) throws Exception {
        QName name = r.getElementName();
        setup();
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
            filter = chain.decode(r);
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

    @Override
    public Boolean canHandle(QName name) {
        return chain.canHandle(name);
    }
}
