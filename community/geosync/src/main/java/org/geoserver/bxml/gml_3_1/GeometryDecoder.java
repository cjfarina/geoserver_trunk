package org.geoserver.bxml.gml_3_1;

import java.util.Set;

import javax.xml.namespace.QName;

import org.geoserver.bxml.ChoiceDecoder;
import org.geoserver.bxml.Decoder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Geometry;

public class GeometryDecoder implements Decoder<Geometry> {

    private ChoiceDecoder<Geometry> choice;

    public GeometryDecoder() {
        choice = new ChoiceDecoder<Geometry>();
        choice.addOption(new PointDecoder());
        //choice.addOption(new MultiPointDecoder());
        //choice.addOption(new LineStringDecoder());
        //choice.addOption(new MultiLineStringDecoder());
        choice.addOption(new PolygonDecoder());
        //choice.addOption(new MultiPolygonDecoder());
    }

    @Override
    public Geometry decode(BxmlStreamReader r) throws Exception {
        r.require(EventType.START_ELEMENT, null, null);

        Geometry geometry = choice.decode(r);

        r.require(EventType.END_ELEMENT, null, null);
        return geometry;
    }

    @Override
    public boolean canHandle(final QName name) {
        return choice.canHandle(name);
    }

    @Override
    public Set<QName> getTargets() {
        return choice.getTargets();
    }

}
