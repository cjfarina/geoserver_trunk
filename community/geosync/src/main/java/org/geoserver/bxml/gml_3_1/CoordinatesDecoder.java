package org.geoserver.bxml.gml_3_1;

import static org.geotools.gml3.GML.coordinates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geoserver.bxml.Decoder;
import org.geoserver.bxml.base.StringDecoder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequence;

public class CoordinatesDecoder implements Decoder<CoordinateSequence> {

    @Override
    public CoordinateSequence decode(BxmlStreamReader r) throws Exception {
        r.require(EventType.START_ELEMENT, coordinates.getNamespaceURI(),
                coordinates.getLocalPart());

        String value = new StringDecoder(coordinates).decode(r);
        Coordinate[] coordinates = parse(value);
        return new PackedCoordinateSequence.Double(coordinates);
    }

    private Coordinate[] parse(String string) {
        String trim = string.trim();
        final int len = trim.length();

        StringBuilder curr = new StringBuilder();

        List<Coordinate> coordinates = new ArrayList<Coordinate>();
        List<String> strings = new ArrayList<String>();
        Boolean comma = true;
        for (int i = 0; i < len; i++) {
            char c = trim.charAt(i);
            if (!Character.isWhitespace(c) && c != ',') {
                curr.append(c);
            } else if (curr.length() > 0) {
                if (comma) {
                    if (curr.length() > 0) {
                        strings.add(curr.toString());
                    }
                    curr.setLength(0);
                } else {
                    coordinates.add(buildCoordinate(strings));
                    strings = new ArrayList<String>();
                    if (curr.length() > 0) {
                        strings.add(curr.toString());
                    }
                    curr.setLength(0);
                }
                comma = false;
            }
            if (c == ',') {
                comma = true;
            }
            if (i == (len - 1)) {
                if (curr.length() > 0) {
                    strings.add(curr.toString());
                }
                coordinates.add(buildCoordinate(strings));
            }
        }

        return coordinates.toArray(new Coordinate[coordinates.size()]);
    }

    private Coordinate buildCoordinate(List<String> strings) {
        Coordinate coordinate = new Coordinate();
        coordinate.x = Double.parseDouble(strings.get(0));
        coordinate.y = Double.parseDouble(strings.get(1));
        if (strings.size() > 2) {
            coordinate.z = Double.parseDouble(strings.get(2));
        }
        return coordinate;
    }

    @Override
    public boolean canHandle(QName name) {
        return coordinates.equals(name);
    }

    @Override
    public Set<QName> getTargets() {
        return Collections.singleton(coordinates);
    }

}
