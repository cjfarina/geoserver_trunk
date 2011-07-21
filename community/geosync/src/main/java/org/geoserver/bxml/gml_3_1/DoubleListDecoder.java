package org.geoserver.bxml.gml_3_1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geoserver.bxml.Decoder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

public class DoubleListDecoder implements Decoder<double[]> {

    private final QName name;

    public DoubleListDecoder(final QName name) {
        this.name = name;
    }

    @Override
    public double[] decode(BxmlStreamReader r) throws Exception {
        r.require(EventType.START_ELEMENT, name.getNamespaceURI(), name.getLocalPart());
        EventType type;

        StringBuilder sb = new StringBuilder();

        double[] values = {};
        int pos = 0;
        while ((type = r.next()).isValue()) {

            if (EventType.VALUE_DOUBLE.equals(type)) {
                if (sb.length() > 0) {
                    values = append(values, sb);
                    sb.setLength(0);
                }
                final int valueCount = r.getValueCount();
                values = expand(values, valueCount);
                for (int i = 0; i < valueCount; i++, pos++) {
                    values[pos] = r.getDoubleValue();
                }
            } else if (EventType.VALUE_FLOAT.equals(type)) {
                if (sb.length() > 0) {
                    values = append(values, sb);
                    sb.setLength(0);
                }
                final int valueCount = r.getValueCount();
                values = expand(values, valueCount);
                for (int i = 0; i < valueCount; i++, pos++) {
                    values[pos] = r.getFloatValue();
                }
            } else {
                sb.append(r.getStringValue());
            }
        }
        if (sb.length() > 0) {
            values = append(values, sb);
            sb.setLength(0);
        }

        r.require(EventType.END_ELEMENT, name.getNamespaceURI(), name.getLocalPart());
        return values;
    }

    private double[] append(double[] values, final StringBuilder sb) {
        final int len = sb.length();

        StringBuilder curr = new StringBuilder();

        List<String> strings = new ArrayList<String>();
        for (int i = 0; i < len; i++) {
            char c = sb.charAt(i);
            if (!Character.isWhitespace(c)) {
                curr.append(c);
            } else if (curr.length() > 0) {
                strings.add(curr.toString());
                curr.setLength(0);
            }
        }
        if (curr.length() > 0) {
            strings.add(curr.toString());
            curr.setLength(0);
        }
        final int count = strings.size();
        final int startIndex = values.length;
        values = expand(values, count);
        for (int i = 0; i < count; i++) {
            values[startIndex + i] = Double.parseDouble(strings.get(i));
        }
        return values;
    }

    private double[] expand(double[] values, int valueCount) {
        double[] expanded = new double[values.length + valueCount];
        System.arraycopy(values, 0, expanded, 0, values.length);
        return expanded;
    }

    @Override
    public boolean canHandle(QName name) {
        return this.name.equals(name);
    }

    @Override
    public Set<QName> getTargets() {
        return Collections.singleton(name);
    }

}
