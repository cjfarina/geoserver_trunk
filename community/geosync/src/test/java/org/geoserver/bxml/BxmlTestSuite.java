package org.geoserver.bxml;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.geoserver.bxml.atom.EntryDecoderTest;
import org.geoserver.bxml.atom.FeedDecoderTest;
import org.geoserver.bxml.filter_1_1.FilterDecoderTest;
import org.geoserver.bxml.gml_3_1.GeometryDecoderTest;
import org.geoserver.bxml.wfs_1_1.DeleteDecoderTest;
import org.geoserver.bxml.wfs_1_1.InsertDecoderTest;
import org.geoserver.bxml.wfs_1_1.UpdateDecoderTest;

public class BxmlTestSuite {

    public static Test suite() {
        TestSuite suite = new TestSuite(BxmlTestSuite.class.getName());

        suite.addTestSuite(EntryDecoderTest.class);
        suite.addTestSuite(FeedDecoderTest.class);
        suite.addTestSuite(FilterDecoderTest.class);
        suite.addTestSuite(GeometryDecoderTest.class);
        suite.addTestSuite(DeleteDecoderTest.class);
        suite.addTestSuite(UpdateDecoderTest.class);
        suite.addTestSuite(InsertDecoderTest.class);
        suite.addTestSuite(PrimitiveValuesDecoderTest.class);

        return suite;
    }

}