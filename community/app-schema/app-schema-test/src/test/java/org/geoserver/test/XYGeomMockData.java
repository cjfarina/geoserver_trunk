/* 
 * Copyright (c) 2001 - 2009 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.test;

import org.geoserver.data.test.MockData;
import org.geotools.data.complex.AppSchemaDataAccess;

/**
 * Mock data for testing use of geometryless data sources {@link AppSchemaDataAccess} with
 * GeoServer.
 * 
 * Inspired by {@link MockData}.
 * 
 * @author Rob Atkinson
 * @author Ben Caradoc-Davies, CSIRO Exploration and Mining
 * 
 */
public class XYGeomMockData extends AbstractAppSchemaMockData {

    public static final String TEST_PREFIX = "test";

    public static final String TEST_URI = "http://test";

    public void addContent() {
        setNamespace(TEST_PREFIX, TEST_URI);
        addFeatureType(TEST_PREFIX, "PointFeature", "PointFeature.xml",
                "PointFeatureGeomPropertyfile.properties");
    }

}
