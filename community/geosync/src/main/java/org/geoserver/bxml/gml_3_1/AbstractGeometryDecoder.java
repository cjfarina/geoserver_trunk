package org.geoserver.bxml.gml_3_1;

import static org.geotools.gml3.GML.srsName;

import javax.xml.namespace.QName;

import org.geoserver.bxml.BXMLDecoderUtil;
import org.geoserver.bxml.filter_1_1.AbstractTypeDecoder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.util.Assert;

public abstract class AbstractGeometryDecoder<T> extends AbstractTypeDecoder<T> {

    public static final QName srsDimension = new QName("http://www.opengis.net/gml", "srsDimension");

    private CoordinateReferenceSystem crs;

    private int dimension = -1;
    
    public AbstractGeometryDecoder(final QName... names) {
        super(names);
    }
    
    public AbstractGeometryDecoder(final CoordinateReferenceSystem crs, final int dimension, final QName... names) {
        this(names);
        this.crs = crs;
        this.dimension = dimension;
    }
    
    @Override
    public final T decode(final BxmlStreamReader r) throws Exception {
        r.require(EventType.START_ELEMENT, null, null);
        final QName name = r.getElementName();
        Assert.isTrue(canHandle(name));
        
        if (crs == null) {
            crs = parseCrs(r.getAttributeValue(null, srsName.getLocalPart()));
        }
        if (dimension == -1) {
            dimension = parseCrsDimension(r.getAttributeValue(null, srsDimension.getLocalPart()));
        }
        
        T result = decodeInternal(r, name);

        BXMLDecoderUtil.goToEnd(r, name);
        
        r.require(EventType.END_ELEMENT, name.getNamespaceURI(), name.getLocalPart());
        return result;
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

    public CoordinateReferenceSystem getCrs() {
        return crs;
    }

    public void setCrs(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }
}
