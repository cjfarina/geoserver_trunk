package org.geoserver.bxml.gml_3_1;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.geoserver.bxml.SequenceDecoder;
import org.geotools.gml2.GML;
import org.gvsig.bxml.stream.BxmlStreamReader;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

public class MultiLineStringDecoder extends AbstractGeometryDecoder<Geometry> {

    public MultiLineStringDecoder() {
        super(GML.MultiLineString);
    }

    @Override
    protected MultiLineString decodeInternal(BxmlStreamReader r, QName name) throws Exception {
        SequenceDecoder<Geometry> seq = new SequenceDecoder<Geometry>(1, Integer.MAX_VALUE);
        seq.add(new GeometryMemberDecoder(GML.lineStringMember, new LineStringDecoder()), 1, 1);

        r.nextTag();
        Iterator<Geometry> iterator = seq.decode(r);
        List<LineString> lines = new ArrayList<LineString>();
        while (iterator.hasNext()) {
            lines.add((LineString) iterator.next());

        }
        MultiLineString multiLineString = new GeometryFactory().createMultiLineString(lines
                .toArray(new LineString[lines.size()]));
        multiLineString.setUserData(getCrs());
        return multiLineString;
    }

}
