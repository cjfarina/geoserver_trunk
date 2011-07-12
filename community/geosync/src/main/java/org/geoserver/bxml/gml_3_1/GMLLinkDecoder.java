package org.geoserver.bxml.gml_3_1;

import static org.geotools.gml3.GML.srsName;

import java.util.Map;

import javax.xml.namespace.QName;

import org.geoserver.bxml.AbstractDecoder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.GeometryFactory;

import com.vividsolutions.jts.geom.Geometry;

public abstract class GMLLinkDecoder extends AbstractDecoder<Geometry> {

    public static final QName srsDimension = new QName("http://www.opengis.net/gml", "srsDimension");
    
    protected GMLLinkDecoder link;
    
    protected CoordinateReferenceSystem crs;
    
    protected int crsDimension;

    protected static final GeometryFactory gf = new GeometryFactory();

    public GMLLinkDecoder(final QName name) {
        super(name);
    }

    public GMLLinkDecoder(final QName name, final GMLLinkDecoder link) {
        this(name);
        this.link = link;
    }

    @Override
    public Geometry decode(final BxmlStreamReader r) throws Exception {
        QName name = r.getElementName();

        if (this.name.equals(name)) {
            return super.decode(r);
        } else if (link != null) {
            return link.decode(r);
        } else {
            return null;
        }
    }

    protected void decodeAttributtes(final BxmlStreamReader r, Map<QName, String> attributes)
            throws Exception {
        crs = parseCrs(attributes.get(srsName));
        crsDimension = parseCrsDimension(attributes.get(srsDimension));
    }

    protected CoordinateReferenceSystem parseCrs(String srsName)
            throws NoSuchAuthorityCodeException, FactoryException {
        if (srsName == null) {
            return DefaultGeographicCRS.WGS84;
        }
        CoordinateReferenceSystem crs = CRS.decode(srsName);
        return crs;
    }

    protected int parseCrsDimension(String srsDimension) {
        if (srsDimension == null) {
            return 2;
        }
        int dimension = Integer.valueOf(srsDimension);
        return dimension;
    }
}
