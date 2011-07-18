package org.geoserver.bxml.filter_1_1.expression;

import static org.geotools.filter.v1_1.OGC.Add;
import static org.geotools.filter.v1_1.OGC.Div;
import static org.geotools.filter.v1_1.OGC.Mul;
import static org.geotools.filter.v1_1.OGC.Sub;

import java.util.ArrayList;
import java.util.List;

import org.geoserver.bxml.filter_1_1.ListDecoder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;

public class ArithmeticOperatorDecoder extends ListDecoder<Expression> {

    protected final List<Expression> expresions = new ArrayList<Expression>();

    protected static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
            .getDefaultHints());

    public ArithmeticOperatorDecoder() {
        add(Add);
        add(Sub);
        add(Mul);
        add(Div);
    }

    @Override
    protected void decodeElement(final BxmlStreamReader r) throws Exception {
        expresions.add(new ExpressionDecoder().decode(r));
    }

    @Override
    protected Expression buildResult() {
        if (Add.equals(name)) {
            return ff.add(expresions.get(0), expresions.get(1));
        }

        if (Sub.equals(name)) {
            return ff.subtract(expresions.get(0), expresions.get(1));
        }

        if (Mul.equals(name)) {
            return ff.multiply(expresions.get(0), expresions.get(1));
        }

        if (Div.equals(name)) {
            return ff.divide(expresions.get(0), expresions.get(1));
        }

        throw new IllegalArgumentException(this.getClass().getName() + " can not decode " + name);
    }
}
