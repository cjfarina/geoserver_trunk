package org.geoserver.bxml.gml_3_1;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.geoserver.bxml.AbstractDecoder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import static org.geotools.gml3.GML.pos;

import com.vividsolutions.jts.geom.Coordinate;

public class CoordinatePostListParser extends AbstractDecoder<List<Coordinate>> {

    private List<Coordinate> coordinates = new ArrayList<Coordinate>();
    
    private int dimension;
    
    public CoordinatePostListParser(final QName name, final int dimension) {
        super(name);
        this.dimension = dimension;
    }
    
    @Override
    protected void decodeElement(BxmlStreamReader r) throws Exception {
        
        QName name = r.getElementName();
        
        if(pos.equals(name)){
            coordinates.addAll(toCoordList(readStringValue(r, name)));
        }
    }
    
    protected void setStringValue(String value) throws Exception {
        coordinates.addAll(toCoordList(value));
    }
    
    private List<Coordinate> toCoordList(String value) {
        value = value.trim();
        value = value.replaceAll("\n", " ");
        value = value.replaceAll("\r", " ");
        String[] split = value.trim().split(" +");
        final int ordinatesLength = split.length;
        if (ordinatesLength % dimension != 0) {
            throw new IllegalArgumentException("Number of ordinates (" + ordinatesLength
                    + ") does not match crs dimension: " + dimension);
        }
        final int nCoords = ordinatesLength / dimension;
        List<Coordinate> coordinates = new ArrayList<Coordinate>(nCoords);
        Coordinate coord;
        double x, y, z;
        for (int i = 0; i < ordinatesLength; i += dimension) {
            x = Double.valueOf(split[i]);
            y = Double.valueOf(split[i + 1]);
            if (dimension > 2) {
                z = Double.valueOf(split[i + 2]);
                coord = new Coordinate(x, y, z);
            } else {
                coord = new Coordinate(x, y);
            }
            coordinates.add(coord);
        }
        return coordinates;
    }

    @Override
    protected List<Coordinate> buildResult() {
        return coordinates;
    }
}
