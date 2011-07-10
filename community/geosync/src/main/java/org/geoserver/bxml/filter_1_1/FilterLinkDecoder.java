package org.geoserver.bxml.filter_1_1;

import javax.xml.namespace.QName;

import org.geoserver.bxml.AbstractDecoder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

public abstract class FilterLinkDecoder extends AbstractDecoder<Filter> {

    protected FilterLinkDecoder filterLink;

    protected static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
            .getDefaultHints());

    public FilterLinkDecoder(final QName name) {
        super(name);
    }

    public FilterLinkDecoder(final QName name, final FilterLinkDecoder filterLink) {
        this(name);
        this.filterLink = filterLink;
    }

    @Override
    public Filter decode(final BxmlStreamReader r) throws Exception {
        QName name = r.getElementName();

        if (this.name.equals(name)) {
            return super.decode(r);
        } else if (filterLink != null) {
            return filterLink.decode(r);
        } else {
            return null;
        }
    }
}
