package org.geoserver.bxml;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.geoserver.bxml.atom.EntryDecoderTest;
import org.geoserver.bxml.atom.FeedDecoderTest;
import org.geoserver.bxml.filter_1_1.FilterDecoderTest;
import org.geoserver.bxml.gml_3_1.LineStringDecoderTest;
import org.geoserver.bxml.gml_3_1.LinearRingDecoderTest;
import org.geoserver.bxml.gml_3_1.MultiLineStringDecoderTest;
import org.geoserver.bxml.gml_3_1.MultiPointDecoderTest;
import org.geoserver.bxml.gml_3_1.MultiPolygonDecoderTest;
import org.geoserver.bxml.gml_3_1.PointDecoderTest;
import org.geoserver.bxml.gml_3_1.PolygonDecoderTest;
import org.geoserver.bxml.wfs_1_1.DeleteDecoderTest;
import org.geoserver.bxml.wfs_1_1.UpdateDecoderTest;

public class BxmlTestSuite {

    public static Test suite() {
        TestSuite suite = new TestSuite(BxmlTestSuite.class.getName());

        System.setProperty("isBinaryXML", "false");

        suite.addTestSuite(EntryDecoderTest.class);
        suite.addTestSuite(FeedDecoderTest.class);
        suite.addTestSuite(FilterDecoderTest.class);
        suite.addTestSuite(LinearRingDecoderTest.class);
        suite.addTestSuite(LineStringDecoderTest.class);
        suite.addTestSuite(MultiLineStringDecoderTest.class);
        suite.addTestSuite(MultiPointDecoderTest.class);
        suite.addTestSuite(MultiPolygonDecoderTest.class);
        suite.addTestSuite(PointDecoderTest.class);
        suite.addTestSuite(PolygonDecoderTest.class);
        suite.addTestSuite(DeleteDecoderTest.class);
        suite.addTestSuite(UpdateDecoderTest.class);
        suite.addTestSuite(PrimitiveValuesDecoderTest.class);

        return suite;
    }

}
