/*
 * Copyright (c) 2001 - 2009 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import junit.framework.Test;

import org.geoserver.wfs.WFSInfo;
import org.geoserver.wfs.xml.v1_1_0.WFS;
import org.geotools.data.complex.AppSchemaDataAccess;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * WFS GetFeature to test integration of {@link AppSchemaDataAccess} with GeoServer.
 * 
 * @author Ben Caradoc-Davies, CSIRO Earth Science and Resource Engineering
 * @author Rini Angreani, CSIRO Earth Science and Resource Engineering
 * @author Xiangtan Lin, CSIRO Information Management and Technology
 */
public class FeatureChainingWfsTest extends AbstractAppSchemaWfsTestSupport {
    /**
     * Read-only test so can use one-time setup.
     * 
     * @return
     */
    public static Test suite() {
        return new OneTimeTestSetup(new FeatureChainingWfsTest());
    }

    @Override
    protected NamespaceTestData buildTestData() {
        return new FeatureChainingMockData();
    }

    public static final String GETFEATURE_ATTRIBUTES = "service=\"WFS\" " //
            + "version=\"1.1.0\" " //
            + "xmlns:ogc=\"http://www.opengis.net/ogc\" " //
            + "xmlns:wfs=\"http://www.opengis.net/wfs\" " //
            + "xmlns:gml=\"http://www.opengis.net/gml\" " //
            + "xmlns:gsml=\"" + AbstractAppSchemaMockData.GSML_URI
            + "\" " //
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " //
            + "xsi:schemaLocation=\"" //
            + "http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd " //
            + AbstractAppSchemaMockData.GSML_URI
            + " "
            + AbstractAppSchemaMockData.GSML_SCHEMA_LOCATION_URL //
            + "\""; // end of schemaLocation
    
    /**
     * Test whether GetCapabilities returns wfs:WFS_Capabilities.
     */
    public void testGetCapabilities() {
        Document doc = getAsDOM("wfs?request=GetCapabilities");
        LOGGER.info("WFS GetCapabilities response:\n" + prettyString(doc));
        assertEquals("wfs:WFS_Capabilities", doc.getDocumentElement().getNodeName());
        
        // check wfs schema location is canonical
        String schemaLocation = evaluate("wfs:WFS_Capabilities/@xsi:schemaLocation", doc);                
        String location = "http://www.opengis.net/wfs " + WFS.CANONICAL_SCHEMA_LOCATION;
        assertEquals(location , schemaLocation);

        // make sure non-feature types don't appear in FeatureTypeList
        assertXpathCount(5, "//wfs:FeatureType", doc);
        ArrayList<String> featureTypeNames = new ArrayList<String>(5);
        featureTypeNames.add(evaluate("//wfs:FeatureType[1]/wfs:Name", doc));
        featureTypeNames.add(evaluate("//wfs:FeatureType[2]/wfs:Name", doc));
        featureTypeNames.add(evaluate("//wfs:FeatureType[3]/wfs:Name", doc));
        featureTypeNames.add(evaluate("//wfs:FeatureType[4]/wfs:Name", doc));
        featureTypeNames.add(evaluate("//wfs:FeatureType[5]/wfs:Name", doc));
        // Mapped Feture
        assertTrue(featureTypeNames.contains("gsml:MappedFeature"));
        // Geologic Unit
        assertTrue(featureTypeNames.contains("gsml:GeologicUnit"));
        // FirstParentFeature
        assertTrue(featureTypeNames.contains("ex:FirstParentFeature"));
        // SecondParentFeature
        assertTrue(featureTypeNames.contains("ex:SecondParentFeature"));
        // om:Observation
        assertTrue(featureTypeNames.contains("om:Observation"));
    }

    /**
     * Test whether DescribeFeatureType returns xsd:schema, and if the contents are correct. When no
     * type name specified, it should return imports for all name spaces involved. If type name is
     * specified, it should return imports of GML type and the type's top level schema.
     * 
     * @throws IOException
     */
    public void testDescribeFeatureType() throws IOException {
        File dataDir = this.getTestData().getDataDirectoryRoot();

        /**
         * gsml:MappedFeature
         */
        Document doc = getAsDOM("wfs?request=DescribeFeatureType&typename=gsml:MappedFeature");
        LOGGER.info("WFS DescribeFeatureType, typename=gsml:MappedFeature response:\n"
                + prettyString(doc));
        assertEquals("xsd:schema", doc.getDocumentElement().getNodeName());
        // check target name space is encoded and is correct
        assertXpathEvaluatesTo(AbstractAppSchemaMockData.GSML_URI, "//@targetNamespace", doc);
        // make sure the content is only relevant include
        assertXpathCount(1, "//xsd:include", doc);
        // no import to GML since it's already imported inside the included schema
        // otherwise it's invalid to import twice
        assertXpathCount(0, "//xsd:import", doc);
        // GSML schemaLocation
        assertXpathEvaluatesTo(AbstractAppSchemaMockData.GSML_SCHEMA_LOCATION_URL,
                "//xsd:include/@schemaLocation", doc);
        // nothing else
        assertXpathCount(0, "//xsd:complexType", doc);
        assertXpathCount(0, "//xsd:element", doc);

        /**
         * gsml:GeologicUnit
         */
        doc = getAsDOM("wfs?request=DescribeFeatureType&typename=gsml:GeologicUnit");
        LOGGER.info("WFS DescribeFeatureType, typename=gsml:GeologicUnit response:\n"
                + prettyString(doc));
        assertEquals("xsd:schema", doc.getDocumentElement().getNodeName());
        assertXpathEvaluatesTo(AbstractAppSchemaMockData.GSML_URI, "//@targetNamespace", doc);
        assertXpathCount(1, "//xsd:include", doc);
        assertXpathCount(0, "//xsd:import", doc);
        // GSML schemaLocation
        assertXpathEvaluatesTo(AbstractAppSchemaMockData.GSML_SCHEMA_LOCATION_URL,
                "//xsd:include/@schemaLocation", doc);
        // nothing else
        assertXpathCount(0, "//xsd:complexType", doc);
        assertXpathCount(0, "//xsd:element", doc);

        /**
         * ex:FirstParentFeature and ex:SecondParentFeature
         */
        doc = getAsDOM("wfs?request=DescribeFeatureType&typeName=ex:FirstParentFeature,ex:SecondParentFeature");
        LOGGER.info("WFS DescribeFeatureType, typename=ex:FirstParentFeature,"
                + "ex:SecondParentFeature response:\n" + prettyString(doc));
        assertXpathEvaluatesTo(FeatureChainingMockData.EX_URI, "//@targetNamespace", doc);
        assertXpathCount(1, "//xsd:include", doc);
        assertXpathCount(0, "//xsd:import", doc);
        // EX include
        File exSchemaOne = findFile("featureTypes/ex_FirstParentFeature/simpleContent.xsd", dataDir);
        assertNotNull(exSchemaOne);
        assertTrue(exSchemaOne.exists());
        String exSchemaOneLocation = exSchemaOne.toURI().toString();
        File exSchemaTwo = findFile("featureTypes/ex_SecondParentFeature/simpleContent.xsd", dataDir);
        assertNotNull(exSchemaTwo);
        assertTrue(exSchemaTwo.exists());
        String exSchemaTwoLocation = exSchemaTwo.toURI().toString();
        String schemaLocation = evaluate("//xsd:include/@schemaLocation", doc);
        if (!schemaLocation.equals(exSchemaOneLocation)) {
            assertEquals(exSchemaTwoLocation, schemaLocation);
        }
        // nothing else
        assertXpathCount(0, "//xsd:complexType", doc);
        assertXpathCount(0, "//xsd:element", doc);

        /**
         * om:Observation has 2 schemaURIs specified in the mapping file. Both must appear.
         */
        doc = getAsDOM("wfs?request=DescribeFeatureType&typename=om:Observation");
        LOGGER.info("WFS DescribeFeatureType, typename=om:Observation response:\n"
                + prettyString(doc));
        assertEquals("xsd:schema", doc.getDocumentElement().getNodeName());
        assertXpathEvaluatesTo(FeatureChainingMockData.OM_URI, "//@targetNamespace", doc);
        assertXpathCount(1, "//xsd:include", doc);
        assertXpathCount(1, "//xsd:import", doc);
        // GSML schemaLocation as xsd:import because the namespace is different
        assertXpathEvaluatesTo(AbstractAppSchemaMockData.GSML_URI, "//xsd:import/@namespace", doc);
        assertXpathEvaluatesTo(AbstractAppSchemaMockData.GSML_SCHEMA_LOCATION_URL,
                "//xsd:import/@schemaLocation", doc);
        // OM schemaLocation as xsd:include
        assertXpathEvaluatesTo(FeatureChainingMockData.OM_SCHEMA_LOCATION_URL,
                "//xsd:include/@schemaLocation", doc);
        // nothing else
        assertXpathCount(0, "//xsd:complexType", doc);
        assertXpathCount(0, "//xsd:element", doc);
        
        /**
         * Mixed name spaces
         */
        doc = getAsDOM("wfs?request=DescribeFeatureType&typeName=gsml:MappedFeature,ex:FirstParentFeature");
        LOGGER
                .info("WFS DescribeFeatureType, typename=gsml:MappedFeature,ex:FirstParentFeature response:\n"
                        + prettyString(doc));
        testDescribeFeatureTypeImports(doc, exSchemaOneLocation, null, null);

        /**
         * All type names specified, should result the same as above
         */
        doc = getAsDOM("wfs?request=DescribeFeatureType&typeName=gsml:MappedFeature,gsml:GeologicUnit,ex:FirstParentFeature,ex:SecondParentFeature");
        LOGGER
                .info("WFS DescribeFeatureType, typename=gsml:MappedFeature,gsml:GeologicUnit,ex:FirstParentFeature,ex:SecondParentFeature response:\n"
                        + prettyString(doc));
        testDescribeFeatureTypeImports(doc, exSchemaOneLocation, exSchemaTwoLocation, null);

        /**
         * No type name specified, should result the same as all type names
         */
        doc = getAsDOM("wfs?request=DescribeFeatureType");
        LOGGER.info("WFS DescribeFeatureType response:\n" + prettyString(doc));
        testDescribeFeatureTypeImports(doc, exSchemaOneLocation, exSchemaTwoLocation,
                FeatureChainingMockData.OM_SCHEMA_LOCATION_URL);
    }

    private void testDescribeFeatureTypeImports(Document doc, String exSchemaOneLocation,
            String exSchemaTwoLocation, String omSchemaLocation) {
        assertEquals("xsd:schema", doc.getDocumentElement().getNodeName());
        assertXpathCount(0, "//@targetNamespace", doc);
        int length = omSchemaLocation != null ? 3 : 2;
        assertXpathCount(length, "//xsd:import", doc);
        assertXpathCount(0, "//xsd:include", doc);
        ArrayList<String> namespaces = new ArrayList<String>();
        namespaces.add(AbstractAppSchemaMockData.GSML_URI);
        namespaces.add(FeatureChainingMockData.EX_URI);
        if (omSchemaLocation != null) {
            namespaces.add(FeatureChainingMockData.OM_URI);
        }
        // order is unimportant, and could change, so we don't test the order
        for (int i = 1; i <= length; i++) {
            String namespace = evaluate("//xsd:import[" + i + "]/@namespace", doc);
            String schemaLocation = "//xsd:import[" + i + "]/@schemaLocation";
            if (namespace.equals(AbstractAppSchemaMockData.GSML_URI)) {
                // GSML import
                assertXpathEvaluatesTo(AbstractAppSchemaMockData.GSML_SCHEMA_LOCATION_URL,
                        schemaLocation, doc);
                namespaces.remove(AbstractAppSchemaMockData.GSML_URI);
            } else if (namespace.equals(FeatureChainingMockData.EX_URI)) {
                // EX import
                String loc = evaluate(schemaLocation, doc);
                if (!loc.equals(exSchemaOneLocation)) {
                    // probably the 2nd one, which is the same, but located differently
                    assertNotNull(exSchemaTwoLocation);
                    assertEquals(exSchemaTwoLocation, loc);
                }
                namespaces.remove(FeatureChainingMockData.EX_URI);
            } else {
                // OM import
                assertTrue(omSchemaLocation != null);
                assertEquals(FeatureChainingMockData.OM_URI, namespace);
                assertXpathEvaluatesTo(FeatureChainingMockData.OM_SCHEMA_LOCATION_URL,
                        schemaLocation, doc);
                namespaces.remove(FeatureChainingMockData.OM_URI);
            }
        }
        // ensure there's no repeats in the imports
        assertTrue(namespaces.isEmpty());
        // nothing else
        assertXpathCount(0, "//xsd:complexType", doc);
        assertXpathCount(0, "//xsd:element", doc);
    }

    /**
     * Test whether GetFeature returns wfs:FeatureCollection.
     */
    public void testGetFeature() {
        Document doc = getAsDOM("wfs?request=GetFeature&typename=gsml:MappedFeature");
        LOGGER.info("WFS GetFeature&typename=gsml:MappedFeature response:\n" + prettyString(doc));
        assertEquals("wfs:FeatureCollection", doc.getDocumentElement().getNodeName());
        // non-feature type should return nothing/exception
        doc = getAsDOM("wfs?request=GetFeature&typename=gsml:CompositionPart");
        LOGGER.info("WFS GetFeature&typename=gsml:CompositionPart response, exception expected:\n"
                + prettyString(doc));
        assertEquals("ows:ExceptionReport", doc.getDocumentElement().getNodeName());
    }

    public void testGetFeatureValid() {
        String path = "wfs?request=GetFeature&typename=gsml:MappedFeature";
        String newline = System.getProperty("line.separator");
        Document doc = getAsDOM(path);
        LOGGER.info("Response for " + path + " :" + newline + prettyString(doc));
        validateGet(path);
    }
    
    /**
     * GeologicUnit mapping has mappingName specified, to override targetElementName when feature
     * chained to MappedFeature. This is to test that querying GeologicUnit as top level feature
     * still works, when its real type name is specified in the query.
     */
    public void testGetFeatureWithMappingName() {
        Document doc = getAsDOM("wfs?request=GetFeature&typename=gsml:GeologicUnit");
        LOGGER.info("WFS GetFeature&typename=gsml:GeologicUnit response:\n" + prettyString(doc));
        assertEquals("wfs:FeatureCollection", doc.getDocumentElement().getNodeName());
        assertXpathEvaluatesTo("3", "/wfs:FeatureCollection/@numberOfFeatures", doc);
        assertXpathCount(3, "//gsml:GeologicUnit", doc);
    }

    /**
     * Test nesting features of complex types with simple content. Previously the nested features
     * attributes weren't encoded, so this is to ensure that this works. This also tests that a
     * feature type can have multiple FEATURE_LINK to be referred by different types.
     */
    public void testComplexTypeWithSimpleContent() {
        Document doc = getAsDOM("wfs?request=GetFeature&typename=ex:FirstParentFeature");
        LOGGER
                .info("WFS GetFeature&typename=ex:FirstParentFeature response:\n"
                        + prettyString(doc));
        assertXpathCount(5, "//ex:FirstParentFeature", doc);

        // cc.1
        assertXpathCount(2, "//ex:FirstParentFeature[@gml:id='cc.1']/ex:nestedFeature", doc);
        assertXpathEvaluatesTo(
                "string_one",
                "//ex:FirstParentFeature[@gml:id='cc.1']/ex:nestedFeature[1]/ex:SimpleContent/ex:someAttribute",
                doc);
        assertXpathEvaluatesTo(
                "string_two",
                "//ex:FirstParentFeature[@gml:id='cc.1']/ex:nestedFeature[2]/ex:SimpleContent/ex:someAttribute",
                doc);
        assertXpathCount(
                0,
                "//ex:FirstParentFeature[@gml:id='cc.1']/ex:nestedFeature[2]/ex:SimpleContent/FEATURE_LINK",
                doc);
        // cc.2
        assertXpathCount(0, "//ex:FirstParentFeature[@gml:id='cc.2']/ex:nestedFeature", doc);

        doc = getAsDOM("wfs?request=GetFeature&typename=ex:SecondParentFeature");
        LOGGER.info("WFS GetFeature&typename=ex:SecondParentFeature response:\n"
                + prettyString(doc));
        assertXpathCount(5, "//ex:SecondParentFeature", doc);

        // cc.1
        assertXpathCount(0, "//ex:SecondParentFeature[@gml:id='cc.1']/ex:nestedFeature", doc);
        // cc.2
        assertXpathCount(3, "//ex:SecondParentFeature[@gml:id='cc.2']/ex:nestedFeature", doc);
        assertXpathEvaluatesTo(
                "string_one",
                "//ex:SecondParentFeature[@gml:id='cc.2']/ex:nestedFeature[1]/ex:SimpleContent/ex:someAttribute",
                doc);
        assertXpathEvaluatesTo(
                "string_two",
                "//ex:SecondParentFeature[@gml:id='cc.2']/ex:nestedFeature[2]/ex:SimpleContent/ex:someAttribute",
                doc);
        assertXpathEvaluatesTo(
                "string_three",
                "//ex:SecondParentFeature[@gml:id='cc.2']/ex:nestedFeature[3]/ex:SimpleContent/ex:someAttribute",
                doc);
    }

    /**
     * Test content of GetFeature response.
     */
    public void testGetFeatureContent() throws Exception {
        Document doc = getAsDOM("wfs?request=GetFeature&typename=gsml:MappedFeature");
        LOGGER.info("WFS GetFeature&typename=gsml:MappedFeature response:\n" + prettyString(doc));
        assertXpathEvaluatesTo("4", "/wfs:FeatureCollection/@numberOfFeatures", doc);
        assertXpathCount(4, "//gsml:MappedFeature", doc);

        checkSchemaLocation(doc);

        // mf1
        {
            String id = "mf1";
            assertXpathEvaluatesTo(id, "(//gsml:MappedFeature)[1]/@gml:id", doc);
            checkMf1Content(id, doc);          
        }

        // mf2
        {
            String id = "mf2";
            assertXpathEvaluatesTo(id, "(//gsml:MappedFeature)[2]/@gml:id", doc);
            checkMf2Content(id, doc);          
        }

        // mf3
        {
            String id = "mf3";
            assertXpathEvaluatesTo(id, "(//gsml:MappedFeature)[3]/@gml:id", doc);
            checkMf3Content(id, doc);            
        }

        // mf4
        {
            String id = "mf4";
            assertXpathEvaluatesTo(id, "(//gsml:MappedFeature)[4]/@gml:id", doc);
            checkMf4Content(id, doc);            
        }
        
        // check for duplicate gml:id
        assertXpathCount(1, "//gsml:GeologicUnit[@gml:id='gu.25678']", doc);
    }

    /**
     * Check schema location
     * @param doc
     */
    private void checkSchemaLocation(Document doc) {
        String schemaLocation = evaluate("/wfs:FeatureCollection/@xsi:schemaLocation", doc);
        String gsmlLocation = AbstractAppSchemaMockData.GSML_URI + " "
                + AbstractAppSchemaMockData.GSML_SCHEMA_LOCATION_URL;
        String wfsLocation = org.geoserver.wfs.xml.v1_1_0.WFS.NAMESPACE + " "
                + org.geoserver.wfs.xml.v1_1_0.WFS.CANONICAL_SCHEMA_LOCATION;
        if (schemaLocation.startsWith(AbstractAppSchemaMockData.GSML_URI)) {
            // GSML schema location was encoded first
            assertEquals(gsmlLocation + " " + wfsLocation, schemaLocation);
        } else {
            // WFS schema location was encoded first
            assertEquals(wfsLocation + " " + gsmlLocation, schemaLocation);
        }       
    }

    /**
     * Check mf1 content are encoded correctly
     * @param id
     * @param doc
     */
    private void checkMf1Content(String id, Document doc) {
        assertXpathEvaluatesTo("GUNTHORPE FORMATION", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gml:name", doc);
        // positionalAccuracy
        assertXpathEvaluatesTo("200.0", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:positionalAccuracy/gsml:CGI_NumericValue/gsml:principalValue", doc);
        assertXpathEvaluatesTo("urn:ogc:def:uom:UCUM:m", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:positionalAccuracy/gsml:CGI_NumericValue/gsml:principalValue/@uom",
                doc);
        // shape
        assertXpathEvaluatesTo("-1.2 52.5 -1.2 52.6 -1.1 52.6 -1.1 52.5 -1.2 52.5",
                "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:shape//gml:posList", doc);
        // specification gu.25699
        assertXpathEvaluatesTo("gu.25699", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/@gml:id", doc);
        // description
        assertXpathEvaluatesTo("Olivine basalt, tuff, microgabbro, minor sedimentary rocks",
                "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                        + "/gsml:GeologicUnit/gml:description", doc);
        // name
        assertXpathCount(2, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                + "/gsml:GeologicUnit/gml:name", doc);
        assertXpathEvaluatesTo("Yaugher Volcanic Group", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification"
                + "/gsml:GeologicUnit/gml:name[@codeSpace='urn:ietf:rfc:2141']", doc);
        assertXpathEvaluatesTo("Yaugher Volcanic Group", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gml:name[1]", doc);
        assertXpathEvaluatesTo("urn:ietf:rfc:2141", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gml:name[1]/@codeSpace", doc);
        assertXpathEvaluatesTo("-Py", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gml:name[2]", doc);
        // feature link shouldn't appear as it's not in the schema
        assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/FEATURE_LINK", doc);
        // occurrence
        assertXpathCount(1, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                + "/gsml:GeologicUnit/gsml:occurrence", doc);
        assertXpathEvaluatesTo("", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification" + "/gsml:GeologicUnit/gsml:occurrence[1]", doc);
        assertXpathEvaluatesTo("urn:cgi:feature:MappedFeature:mf1",
                "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                        + "/gsml:GeologicUnit/gsml:occurrence/@xlink:href", doc);
        // exposureColor
        assertXpathCount(1, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                + "/gsml:GeologicUnit/gsml:exposureColor", doc);
        assertXpathEvaluatesTo("Blue", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gsml:exposureColor"
                + "/gsml:CGI_TermValue/gsml:value", doc);
        assertXpathEvaluatesTo(
                "some:uri",
                "//gsml:MappedFeature[@gml:id='"
                        + id
                        + "']/gsml:specification/gsml:GeologicUnit/gsml:exposureColor/gsml:CGI_TermValue/gsml:value/@codeSpace",
                doc);
        // feature link shouldn't appear as it's not in the schema
        assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gsml:exposureColor"
                + "/gsml:CGI_TermValue/FEATURE_LINK", doc);
        // outcropCharacter
        assertXpathCount(1, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                + "/gsml:GeologicUnit/gsml:outcropCharacter", doc);
        assertXpathEvaluatesTo("x", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gsml:outcropCharacter"
                + "/gsml:CGI_TermValue/gsml:value", doc);
        // feature link shouldn't appear as it's not in the schema
        assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gsml:outcropCharacter"
                + "/gsml:CGI_TermValue/FEATURE_LINK", doc);
        // composition
        assertXpathCount(1, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                + "/gsml:GeologicUnit/gsml:composition", doc);
        assertXpathEvaluatesTo("nonexistent", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gsml:composition"
                + "/gsml:CompositionPart/gsml:proportion/gsml:CGI_TermValue/gsml:value", doc);
        assertXpathEvaluatesTo("fictitious component", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gsml:composition"
                + "/gsml:CompositionPart/gsml:role", doc);
        // feature link shouldn't appear as it's not in the schema
        assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gsml:composition"
                + "/gsml:CompositionPart/gsml:role/FEATURE_LINK", doc);
        // lithology
        assertXpathCount(1, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                + "/gsml:GeologicUnit/gsml:composition/gsml:CompositionPart/gsml:lithology",
                doc);
        // feature link shouldn't appear as it's not in the schema
        assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gsml:composition"
                + "/gsml:CompositionPart/gsml:lithology/FEATURE_LINK", doc);
        
    }

    /**
     * Check mf2 content are encoded correctly
     * @param id
     * @param doc
     */
    private void checkMf2Content(String id, Document doc) {
        assertXpathEvaluatesTo("MERCIA MUDSTONE GROUP", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gml:name", doc);
        // positionalAccuracy
        assertXpathEvaluatesTo("100.0", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:positionalAccuracy/gsml:CGI_NumericValue/gsml:principalValue", doc);
        assertXpathEvaluatesTo("urn:ogc:def:uom:UCUM:m", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:positionalAccuracy/gsml:CGI_NumericValue/gsml:principalValue/@uom",
                doc);
        // shape
        assertXpathEvaluatesTo("-1.3 52.5 -1.3 52.6 -1.2 52.6 -1.2 52.5 -1.3 52.5",
                "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:shape//gml:posList", doc);
        // gu.25678
        assertXpathEvaluatesTo("gu.25678", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/@gml:id", doc);
        // name
        assertXpathCount(3, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                + "/gsml:GeologicUnit/gml:name", doc);
        assertXpathEvaluatesTo("Yaugher Volcanic Group 1", "//gsml:MappedFeature[@gml:id='"
                + id + "']/gsml:specification/gsml:GeologicUnit/gml:name[1]", doc);
        assertXpathEvaluatesTo("urn:ietf:rfc:2141", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gml:name[1]/@codeSpace", doc);
        assertXpathEvaluatesTo("Yaugher Volcanic Group 2", "//gsml:MappedFeature[@gml:id='"
                + id + "']/gsml:specification/gsml:GeologicUnit/gml:name[2]", doc);
        assertXpathEvaluatesTo("urn:ietf:rfc:2141", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gml:name[2]/@codeSpace", doc);
        assertXpathEvaluatesTo("-Py", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gml:name[3]", doc);
        assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/FEATURE_LINK", doc);
        // occurrence
        assertXpathCount(2, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                + "/gsml:GeologicUnit/gsml:occurrence", doc);
        assertXpathEvaluatesTo("", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification" + "/gsml:GeologicUnit/gsml:occurrence[1]", doc);
        assertXpathEvaluatesTo("urn:cgi:feature:MappedFeature:mf2",
                "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                        + "/gsml:GeologicUnit/gsml:occurrence[1]/@xlink:href", doc);
        assertXpathEvaluatesTo("", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification" + "/gsml:GeologicUnit/gsml:occurrence[2]", doc);
        assertXpathEvaluatesTo("urn:cgi:feature:MappedFeature:mf3",
                "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                        + "/gsml:GeologicUnit/gsml:occurrence[2]/@xlink:href", doc);
        // description
        assertXpathEvaluatesTo("Olivine basalt, tuff, microgabbro, minor sedimentary rocks",
                "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                        + "/gsml:GeologicUnit/gml:description", doc);
        // exposureColor
        assertXpathCount(2, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                + "/gsml:GeologicUnit/gsml:exposureColor", doc);
        assertXpathEvaluatesTo("Yellow", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gsml:exposureColor[1]"
                + "/gsml:CGI_TermValue/gsml:value", doc);
        assertXpathEvaluatesTo(
                "some:uri",
                "//gsml:MappedFeature[@gml:id='"
                        + id
                        + "']/gsml:specification/gsml:GeologicUnit/gsml:exposureColor[1]/gsml:CGI_TermValue/gsml:value/@codeSpace",
                doc);
        assertXpathEvaluatesTo("Blue", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gsml:exposureColor[2]"
                + "/gsml:CGI_TermValue/gsml:value", doc);
        assertXpathEvaluatesTo(
                "some:uri",
                "//gsml:MappedFeature[@gml:id='"
                        + id
                        + "']/gsml:specification/gsml:GeologicUnit/gsml:exposureColor[2]/gsml:CGI_TermValue/gsml:value/@codeSpace",
                doc);
        assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gsml:exposureColor"
                + "/gsml:CGI_TermValue/FEATURE_LINK", doc);
        // outcropCharacter
        assertXpathCount(2, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                + "/gsml:GeologicUnit/gsml:outcropCharacter", doc);
        assertXpathEvaluatesTo("y", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gsml:outcropCharacter[1]"
                + "/gsml:CGI_TermValue/gsml:value", doc);
        assertXpathEvaluatesTo("x", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gsml:outcropCharacter[2]"
                + "/gsml:CGI_TermValue/gsml:value", doc);
        assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gsml:outcropCharacter"
                + "/gsml:CGI_TermValue/FEATURE_LINK", doc);
        // composition
        assertXpathCount(2, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                + "/gsml:GeologicUnit/gsml:composition", doc);
        assertXpathEvaluatesTo("significant", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gsml:composition[1]"
                + "/gsml:CompositionPart/gsml:proportion/gsml:CGI_TermValue/gsml:value", doc);
        assertXpathEvaluatesTo("interbedded component", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification"
                + "/gsml:GeologicUnit[@gml:id='gu.25678']/gsml:composition[1]"
                + "/gsml:CompositionPart/gsml:role", doc);
        assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gsml:composition[1]"
                + "/gsml:CompositionPart/gsml:role/FEATURE_LINK", doc);
        assertXpathEvaluatesTo("minor", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gsml:composition[2]"
                + "/gsml:CompositionPart/gsml:proportion/gsml:CGI_TermValue/gsml:value", doc);
        assertXpathEvaluatesTo("interbedded component", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gsml:composition[2]"
                + "/gsml:CompositionPart/gsml:role", doc);
        assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gsml:composition[2]"
                + "/gsml:CompositionPart/gsml:role/FEATURE_LINK", doc);
    
        // lithology
        assertXpathCount(2, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                + "/gsml:GeologicUnit/gsml:composition/gsml:CompositionPart/gsml:lithology",
                doc);
        assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gsml:composition"
                + "/gsml:CompositionPart/gsml:lithology/FEATURE_LINK", doc);        
    }

    /**
     * Check mf3 content are encoded correctly
     * @param id
     * @param doc
     */
    private void checkMf3Content(String id, Document doc) {
        assertXpathEvaluatesTo("CLIFTON FORMATION", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gml:name", doc);
        // positionalAccuracy
        assertXpathEvaluatesTo("150.0", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:positionalAccuracy/gsml:CGI_NumericValue/gsml:principalValue", doc);
        assertXpathEvaluatesTo("urn:ogc:def:uom:UCUM:m", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:positionalAccuracy/gsml:CGI_NumericValue/gsml:principalValue/@uom",
                doc);
        // shape
        assertXpathEvaluatesTo("-1.2 52.5 -1.2 52.6 -1.1 52.6 -1.1 52.5 -1.2 52.5",
                "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:shape//gml:posList", doc);
        // gu.25678
        assertXpathEvaluatesTo("#gu.25678", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/@xlink:href", doc);
        // make sure nothing else is encoded
        assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit", doc);        
    }

    /**
     * Check mf4 content are encoded correctly
     * @param id
     * @param doc
     */
    private void checkMf4Content(String id, Document doc) {
        assertXpathEvaluatesTo("MURRADUC BASALT", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gml:name", doc);
        // positionalAccuracy
        assertXpathEvaluatesTo("120.0", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:positionalAccuracy/gsml:CGI_NumericValue/gsml:principalValue", doc);
        assertXpathEvaluatesTo("urn:ogc:def:uom:UCUM:m", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:positionalAccuracy/gsml:CGI_NumericValue/gsml:principalValue/@uom",
                doc);
        // shape
        assertXpathEvaluatesTo("-1.3 52.5 -1.3 52.6 -1.2 52.6 -1.2 52.5 -1.3 52.5",
                "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:shape//gml:posList", doc);
        // gu.25682
        assertXpathEvaluatesTo("gu.25682", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/@gml:id", doc);
        // description
        assertXpathEvaluatesTo("Olivine basalt", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gml:description", doc);
        // name
        assertXpathCount(2, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                + "/gsml:GeologicUnit/gml:name", doc);
        assertXpathEvaluatesTo("New Group", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification"
                + "/gsml:GeologicUnit/gml:name[@codeSpace='urn:ietf:rfc:2141']", doc);
        assertXpathEvaluatesTo("New Group", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gml:name[1]", doc);
        assertXpathEvaluatesTo("urn:ietf:rfc:2141", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gml:name[1]/@codeSpace", doc);
        assertXpathEvaluatesTo("-Xy", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gml:name[2]", doc);
        assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/FEATURE_LINK", doc);
        // occurrence
        assertXpathCount(1, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                + "/gsml:GeologicUnit/gsml:occurrence", doc);
        assertXpathEvaluatesTo("", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification" + "/gsml:GeologicUnit/gsml:occurrence[1]", doc);
        assertXpathEvaluatesTo("urn:cgi:feature:MappedFeature:mf4",
                "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                        + "/gsml:GeologicUnit/gsml:occurrence/@xlink:href", doc);
        // exposureColor
        assertXpathCount(1, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                + "/gsml:GeologicUnit/gsml:exposureColor", doc);
        assertXpathEvaluatesTo(
                "some:uri",
                "//gsml:MappedFeature[@gml:id='"
                        + id
                        + "']/gsml:specification/gsml:GeologicUnit/gsml:exposureColor/gsml:CGI_TermValue/gsml:value/@codeSpace",
                doc);
        assertXpathEvaluatesTo("Red", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gsml:exposureColor"
                + "/gsml:CGI_TermValue/gsml:value", doc);
        assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gsml:exposureColor"
                + "/gsml:CGI_TermValue/FEATURE_LINK", doc);
        // outcropCharacter
        assertXpathCount(1, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                + "/gsml:GeologicUnit/gsml:outcropCharacter", doc);
        assertXpathEvaluatesTo("z", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gsml:outcropCharacter"
                + "/gsml:CGI_TermValue/gsml:value", doc);
        assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gsml:outcropCharacter"
                + "/gsml:CGI_TermValue/FEATURE_LINK", doc);
        // composition
        assertXpathCount(1, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                + "/gsml:GeologicUnit/gsml:composition", doc);
        assertXpathEvaluatesTo("significant", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gsml:composition"
                + "/gsml:CompositionPart/gsml:proportion/gsml:CGI_TermValue/gsml:value", doc);
        assertXpathEvaluatesTo("interbedded component", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gsml:composition"
                + "/gsml:CompositionPart/gsml:role", doc);
        assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gsml:composition"
                + "/gsml:CompositionPart/gsml:role/FEATURE_LINK", doc);
        // lithology
        assertXpathCount(2, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                + "/gsml:GeologicUnit/gsml:composition/gsml:CompositionPart/gsml:lithology",
                doc);
        // lithology:1
        assertXpathEvaluatesTo("cc.1", "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                + "/gsml:GeologicUnit/gsml:composition/gsml:CompositionPart/gsml:lithology[1]"
                + "/gsml:ControlledConcept/@gml:id", doc);            
        assertXpathCount(3, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                + "/gsml:GeologicUnit/gsml:composition/gsml:CompositionPart/gsml:lithology[1]"
                + "/gsml:ControlledConcept/gml:name", doc);
        assertXpathEvaluatesTo("name_a", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit"
                + "/gsml:composition/gsml:CompositionPart/gsml:lithology[1]"
                + "/gsml:ControlledConcept/gml:name[1]", doc);
        assertXpathEvaluatesTo("name_b", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit"
                + "/gsml:composition/gsml:CompositionPart/gsml:lithology[1]"
                + "/gsml:ControlledConcept/gml:name[2]", doc);
        assertXpathEvaluatesTo("name_c", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit"
                + "/gsml:composition/gsml:CompositionPart/gsml:lithology[1]"
                + "/gsml:ControlledConcept/gml:name[3]", doc);
        assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gsml:composition"
                + "/gsml:CompositionPart/gsml:lithology[1]/FEATURE_LINK", doc);
        // lithology:2
        assertXpathEvaluatesTo("cc.2", "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                + "/gsml:GeologicUnit/gsml:composition/gsml:CompositionPart/gsml:lithology[2]/"
                + "/gsml:ControlledConcept/@gml:id", doc);
        assertXpathCount(1, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                + "/gsml:GeologicUnit/gsml:composition/gsml:CompositionPart/gsml:lithology[2]"
                + "/gsml:ControlledConcept/gml:name", doc);
        assertXpathEvaluatesTo("name_2", "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit"
                + "/gsml:composition/gsml:CompositionPart/gsml:lithology[2]"
                + "/gsml:ControlledConcept/gml:name", doc);
        assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                + "']/gsml:specification/gsml:GeologicUnit/gsml:composition"
                + "/gsml:CompositionPart/gsml:lithology[2]/FEATURE_LINK", doc);        
    }

    /**
     * Implementation for tests expected to get mf4 only.
     * 
     * @param xml
     */
    private void checkGetMf4Only(String xml) {
        Document doc = postAsDOM("wfs", xml);
        LOGGER.info("WFS filter GetFeature response:\n" + prettyString(doc));
        assertEquals("wfs:FeatureCollection", doc.getDocumentElement().getNodeName());
        assertXpathEvaluatesTo("1", "/wfs:FeatureCollection/@numberOfFeatures", doc);
        assertXpathCount(1, "//gsml:MappedFeature", doc);
        // mf4
        {
            String id = "mf4";
            assertXpathEvaluatesTo(id, "//gsml:MappedFeature[1]/@gml:id", doc);
            assertXpathEvaluatesTo("MURRADUC BASALT", "//gsml:MappedFeature[@gml:id='" + id
                    + "']/gml:name", doc);
            // gu.25682
            assertXpathEvaluatesTo("gu.25682", "//gsml:MappedFeature[@gml:id='" + id
                    + "']/gsml:specification/gsml:GeologicUnit/@gml:id", doc);
        }
    }

    /**
     * Test if we can get mf4 by its name.
     */
    public void testGetFeaturePropertyFilter() {
        String xml = //
        "<wfs:GetFeature " //
                + GETFEATURE_ATTRIBUTES //
                + ">" //
                + "    <wfs:Query typeName=\"gsml:MappedFeature\">" //
                + "        <ogc:Filter>" //
                + "            <ogc:PropertyIsEqualTo>" //
                + "                <ogc:PropertyName>gml:name</ogc:PropertyName>" //
                + "                <ogc:Literal>MURRADUC BASALT</ogc:Literal>" //
                + "            </ogc:PropertyIsEqualTo>" //
                + "        </ogc:Filter>" //
                + "    </wfs:Query> " //
                + "</wfs:GetFeature>";
        validate(xml);
        checkGetMf4Only(xml);
    }

    /**
     * Test if we can get mf4 with a FeatureId fid filter.
     */
    public void testGetFeatureWithFeatureIdFilter() {
        String xml = //
        "<wfs:GetFeature " //
                + GETFEATURE_ATTRIBUTES //
                + ">" //
                + "    <wfs:Query typeName=\"gsml:MappedFeature\">" //
                + "        <ogc:Filter>" //
                + "            <ogc:FeatureId fid=\"mf4\"/>" //
                + "        </ogc:Filter>" //
                + "    </wfs:Query> " //
                + "</wfs:GetFeature>";
        validate(xml);
        checkGetMf4Only(xml);
    }

    /**
     * Test if we can get mf4 with a GmlObjectId gml:id filter.
     */
    public void testGetFeatureWithGmlObjectIdFilter() {
        String xml = //
        "<wfs:GetFeature " //
                + GETFEATURE_ATTRIBUTES //
                + ">" //
                + "    <wfs:Query typeName=\"gsml:MappedFeature\">" //
                + "        <ogc:Filter>" //
                + "            <ogc:GmlObjectId gml:id=\"mf4\"/>" //
                + "        </ogc:Filter>" //
                + "    </wfs:Query> " //
                + "</wfs:GetFeature>";
        validate(xml);
        checkGetMf4Only(xml);
    }
    
    /**
     * Test anyType as complex attributes, and placeholder type (e.g AnyOrReference) which contains
     * <any/> element.
     */
    public void testAnyTypeAndAnyElement() {
        final String OBSERVATION_ID_PREFIX = "observation:";
        Document doc = getAsDOM("wfs?request=GetFeature&typename=om:Observation");
        LOGGER.info("WFS GetFeature&typename=om:Observation response:\n" + prettyString(doc));

        assertXpathEvaluatesTo("4", "/wfs:FeatureCollection/@numberOfFeatures", doc);
        assertXpathCount(4, "//om:Observation", doc);

        String id = "mf1";
        assertXpathEvaluatesTo(OBSERVATION_ID_PREFIX + id, "(//om:Observation)[1]/@gml:id", doc);
        // om:metadata
        assertXpathEvaluatesTo("651.0",
                "(//om:Observation)[1]/om:metadata/gsml:CGI_NumericValue/gsml:principalValue", doc);
        // om:resultQuality
        Node resultQuality = doc.getElementsByTagName("om:resultQuality").item(0);
        Node geologicUnit = resultQuality.getFirstChild();
        assertEquals("gu.25699", geologicUnit.getAttributes().getNamedItem("gml:id").getNodeValue());
        // om:result
        assertXpathEvaluatesTo(id, "(//om:Observation)[1]/om:result/gsml:MappedFeature/@gml:id",
                doc);

        id = "mf2";
        assertXpathEvaluatesTo(OBSERVATION_ID_PREFIX + id, "(//om:Observation)[2]/@gml:id", doc);
        // om:metadata
        assertXpathEvaluatesTo("269.0",
                "(//om:Observation)[2]/om:metadata/gsml:CGI_NumericValue/gsml:principalValue", doc);
        // om:resultQuality
        resultQuality = doc.getElementsByTagName("om:resultQuality").item(1);
        geologicUnit = resultQuality.getFirstChild();
        assertEquals("gu.25678", geologicUnit.getAttributes().getNamedItem("gml:id").getNodeValue());
        // om:result
        assertXpathEvaluatesTo(id, "(//om:Observation)[2]/om:result/gsml:MappedFeature/@gml:id",
                doc);

        id = "mf3";
        assertXpathEvaluatesTo(OBSERVATION_ID_PREFIX + id, "(//om:Observation)[3]/@gml:id", doc);
        // om:metadata
        assertXpathEvaluatesTo("123.0",
                "(//om:Observation)[3]/om:metadata/gsml:CGI_NumericValue/gsml:principalValue", doc);
        // om:resultQuality
        resultQuality = doc.getElementsByTagName("om:resultQuality").item(2);
        assertEquals("#gu.25678", resultQuality.getAttributes().getNamedItem("xlink:href")
                .getNodeValue());
        // om:result
        assertXpathEvaluatesTo(id, "(//om:Observation)[3]/om:result/gsml:MappedFeature/@gml:id",
                doc);

        id = "mf4";
        assertXpathEvaluatesTo(OBSERVATION_ID_PREFIX + id, "(//om:Observation)[4]/@gml:id", doc);
        // om:metadata
        assertXpathEvaluatesTo("456.0",
                "(//om:Observation)[4]/om:metadata/gsml:CGI_NumericValue/gsml:principalValue", doc);
        // om:resultQuality
        resultQuality = doc.getElementsByTagName("om:resultQuality").item(3);
        geologicUnit = resultQuality.getFirstChild();
        assertEquals("gu.25682", geologicUnit.getAttributes().getNamedItem("gml:id").getNodeValue());
        // om:result
        assertXpathEvaluatesTo(id, "(//om:Observation)[4]/om:result/gsml:MappedFeature/@gml:id",
                doc);
    }

    /**
     * Making sure attributes that are encoded as xlink:href can still be queried in filters.
     */
    public void testFilteringXlinkHref() {
        String xml = //
        "<wfs:GetFeature " //
                + GETFEATURE_ATTRIBUTES //
                + ">" //
                + "    <wfs:Query typeName=\"gsml:MappedFeature\">" //
                + "        <ogc:Filter>" //
                + "            <ogc:PropertyIsEqualTo>" //
                + "                <ogc:PropertyName>gsml:specification/gsml:GeologicUnit/gml:name</ogc:PropertyName>" //
                + "                <ogc:Literal>Yaugher Volcanic Group</ogc:Literal>" //
                + "            </ogc:PropertyIsEqualTo>" //
                + "        </ogc:Filter>" //
                + "    </wfs:Query> " //
                + "</wfs:GetFeature>";
        validate(xml);
        Document doc = postAsDOM("wfs", xml);
        LOGGER.info("WFS filter GetFeature response:\n" + prettyString(doc));
        assertEquals("wfs:FeatureCollection", doc.getDocumentElement().getNodeName());
        // there should be 1:
        // - mf1/gu.25699
        assertXpathEvaluatesTo("1", "/wfs:FeatureCollection/@numberOfFeatures", doc);
        assertXpathCount(1, "//gsml:MappedFeature", doc);
        assertXpathEvaluatesTo("mf1", "//gsml:MappedFeature/@gml:id", doc);
    }
    
    /**
     * Making sure multi-valued attributes in nested features can be queried from the top level. (GEOT-3156) 
     */
    public void testFilteringNestedMultiValuedAttribute() {
        // PropertyIsEqual
        String xml = 
        "<wfs:GetFeature " 
                + GETFEATURE_ATTRIBUTES 
                + ">" 
                + "    <wfs:Query typeName=\"gsml:MappedFeature\">" 
                + "        <ogc:Filter>" 
                + "            <ogc:PropertyIsEqualTo>" 
                + "                <ogc:Literal>Yaugher Volcanic Group 2</ogc:Literal>" 
                + "                <ogc:PropertyName>gsml:specification/gsml:GeologicUnit/gml:name</ogc:PropertyName>" 
                + "            </ogc:PropertyIsEqualTo>" 
                + "        </ogc:Filter>" 
                + "    </wfs:Query> " 
                + "</wfs:GetFeature>";
        validate(xml);
        Document doc = postAsDOM("wfs", xml);
        LOGGER.info("WFS filter GetFeature response:\n" + prettyString(doc));
        assertEquals("wfs:FeatureCollection", doc.getDocumentElement().getNodeName());
        // there should be 2:
        // - mf2/gu.25678
        // - mf3/gu.25678
        assertXpathEvaluatesTo("2", "/wfs:FeatureCollection/@numberOfFeatures", doc);
        assertXpathCount(2, "//gsml:MappedFeature", doc);
        assertXpathEvaluatesTo("mf2", "(//gsml:MappedFeature)[1]/@gml:id", doc);
        assertXpathEvaluatesTo("mf3", "(//gsml:MappedFeature)[2]/@gml:id", doc);

        // PropertyIsLike
        xml = //
        "<wfs:GetFeature " 
                + GETFEATURE_ATTRIBUTES 
                + ">" 
                + "    <wfs:Query typeName=\"gsml:MappedFeature\">" 
                + "        <ogc:Filter>" 
                + "            <ogc:PropertyIsLike wildCard=\"*\" singleChar=\"#\" escapeChar=\"!\">" 
                + "                <ogc:PropertyName>gsml:specification/gsml:GeologicUnit/gml:name</ogc:PropertyName>" 
                + "                <ogc:Literal>Yaugher Volcanic Group*</ogc:Literal>" 
                + "            </ogc:PropertyIsLike>" 
                + "        </ogc:Filter>" 
                + "    </wfs:Query> " 
                + "</wfs:GetFeature>";
        validate(xml);
        doc = postAsDOM("wfs", xml);
        LOGGER.info("WFS filter GetFeature response:\n" + prettyString(doc));
        assertEquals("wfs:FeatureCollection", doc.getDocumentElement().getNodeName());
        // there should be 3:
        // - mf1/gu.25699
        // - mf2/gu.25678
        // - mf3/gu.25678
        assertXpathEvaluatesTo("3", "/wfs:FeatureCollection/@numberOfFeatures", doc);
        assertXpathCount(3, "//gsml:MappedFeature", doc);
        assertXpathEvaluatesTo("mf1", "(//gsml:MappedFeature)[1]/@gml:id", doc);
        assertXpathEvaluatesTo("mf2", "(//gsml:MappedFeature)[2]/@gml:id", doc);
        assertXpathEvaluatesTo("mf3", "(//gsml:MappedFeature)[3]/@gml:id", doc);
    }

    /**
     * Similar to above test case but using AND as a wrapper for 2 filters involving nested
     * attributes.
     */
    public void testFilterAnd() {
        String xml = "<wfs:GetFeature "
                + GETFEATURE_ATTRIBUTES
                + ">"
                + "<wfs:Query typeName=\"gsml:MappedFeature\">"
                + "    <ogc:Filter>"
                + "        <ogc:And>"
                + "            <ogc:PropertyIsEqualTo>"
                + "                <ogc:Literal>significant</ogc:Literal>"
                + "                <ogc:PropertyName>gsml:specification/gsml:GeologicUnit/gsml:composition/gsml:CompositionPart/gsml:proportion/gsml:CGI_TermValue/gsml:value</ogc:PropertyName>"
                + "            </ogc:PropertyIsEqualTo>"
                + "                <ogc:Not>"
                + "                    <ogc:PropertyIsEqualTo>"
                + "                        <ogc:Literal>Yaugher Volcanic Group 1</ogc:Literal>"
                + "                        <ogc:PropertyName>gsml:specification/gsml:GeologicUnit/gml:name</ogc:PropertyName>"
                + "                    </ogc:PropertyIsEqualTo>"
                + "                </ogc:Not>"
                + "            </ogc:And>"
                + "        </ogc:Filter>"
                + "</wfs:Query> "
                + "</wfs:GetFeature>";
        validate(xml);
        Document doc = postAsDOM("wfs", xml);
        LOGGER.info("WFS filter GetFeature response:\n" + prettyString(doc));
        assertEquals("wfs:FeatureCollection", doc.getDocumentElement().getNodeName());
        assertXpathEvaluatesTo("1", "/wfs:FeatureCollection/@numberOfFeatures", doc);
        assertXpathCount(1, "//gsml:MappedFeature", doc);
        assertXpathEvaluatesTo("mf4", "//gsml:MappedFeature/@gml:id", doc);
    }

    /**
     * Test that denormalized data reports the correct number of features
     */
    public void testDenormalisedFeaturesCount() {
        Document doc = getAsDOM("wfs?request=GetFeature&typename=gsml:GeologicUnit&maxFeatures=3");
        LOGGER.info("WFS GetFeature&typename=gsml:GeologicUnit&maxFeatures=3 response:\n"
                + prettyString(doc));
        assertXpathCount(3, "//gsml:GeologicUnit", doc);

        // check that we get features we're expecting
        String id = "gu.25699";
        assertXpathEvaluatesTo(id, "(//gsml:GeologicUnit)[1]/@gml:id", doc);

        id = "gu.25678";
        assertXpathEvaluatesTo(id, "(//gsml:GeologicUnit)[2]/@gml:id", doc);

        id = "gu.25682";
        assertXpathEvaluatesTo(id, "(//gsml:GeologicUnit)[3]/@gml:id", doc);
    }

    /**
     * Test FeatureCollection is encoded with multiple featureMember elements
     * @throws Exception
     */
    public void testEncodeFeatureMember() throws Exception {
        // change fixture settings (must restore this at end)
        WFSInfo wfs = getGeoServer().getService(WFSInfo.class);
        boolean encodeFeatureMember = wfs.isEncodeFeatureMember();
        wfs.setEncodeFeatureMember(true);
        getGeoServer().save(wfs);
        
        Document doc = getAsDOM("wfs?request=GetFeature&typename=gsml:MappedFeature,gsml:GeologicUnit");
        LOGGER.info("WFS GetFeature&typename=gsml:MappedFeature,gsml:GeologicUnit response:\n"
                + prettyString(doc));
        
       checkSchemaLocation(doc);

        assertXpathEvaluatesTo("7", "/wfs:FeatureCollection/@numberOfFeatures", doc);
        assertXpathCount(4, "//gsml:MappedFeature", doc);        

        assertEquals(7, doc.getElementsByTagName("gml:featureMember").getLength());
        assertEquals(0, doc.getElementsByTagName("gml:featureMembers").getLength());

        // mf1
        {
            String id = "mf1";
            checkMf1Content(id, doc);
        }

        // mf2
        {
            String id = "mf2";
            checkMf2Content(id, doc);
        }

        // mf3
        {
            String id = "mf3";
            checkMf3Content(id, doc);
        }

        // mf4
        {
            String id = "mf4";
            checkMf4Content(id, doc);
        }

        // check for duplicate gml:id
        assertXpathCount(1, "//gsml:GeologicUnit[@gml:id='gu.25699']", doc);
        assertXpathCount(1, "//gsml:GeologicUnit[@gml:id='gu.25678']", doc);
        assertXpathCount(1, "//gsml:GeologicUnit[@gml:id='gu.25682']", doc);

        // test for xlink:href is encoded within featureMember
        assertXpathCount(1, "//gml:featureMember[@xlink:href='#gu.25699']", doc);
        assertXpathCount(1, "//gml:featureMember[@xlink:href='#gu.25678']", doc);
        assertXpathCount(1, "//gml:featureMember[@xlink:href='#gu.25682']", doc);

        // restore fixture settings
        wfs = getGeoServer().getService(WFSInfo.class);
        wfs.setEncodeFeatureMember(encodeFeatureMember);
        getGeoServer().save(wfs);
    }

    /**
     * Test FeatureCollection is encoded with one featureMembers element
     * @throws Exception
     */
    public void testEncodeFeatureMembers() throws Exception {
        // change fixture settings (must restore this at end)
        WFSInfo wfs = getGeoServer().getService(WFSInfo.class);
        boolean encodeFeatureMember = wfs.isEncodeFeatureMember();
        wfs.setEncodeFeatureMember(false);
        getGeoServer().save(wfs);
        
        Document doc = getAsDOM("wfs?request=GetFeature&typename=gsml:MappedFeature,gsml:GeologicUnit");
        LOGGER.info("WFS GetFeature&typename=gsml:MappedFeature,gsml:GeologicUnit response:\n"
                + prettyString(doc));
        
        checkSchemaLocation(doc);
        
        assertXpathEvaluatesTo("7", "/wfs:FeatureCollection/@numberOfFeatures", doc);
        assertXpathCount(4, "//gsml:MappedFeature", doc);

        assertEquals(1, doc.getElementsByTagName("gml:featureMembers").getLength());
        assertEquals(0, doc.getElementsByTagName("gml:featureMember").getLength());

        // mf1
        {
            String id = "mf1";
            checkMf1Content(id, doc);
        }

        // mf2
        {
            String id = "mf2";
            checkMf2Content(id, doc);
        }

        // mf3
        {
            String id = "mf3";
            checkMf3Content(id, doc);
        }

        // mf4
        {
            String id = "mf4";
            checkMf4Content(id, doc);
        }

        // check for duplicate gml:id
        assertXpathCount(1, "//gsml:GeologicUnit[@gml:id='gu.25699']", doc);
        assertXpathCount(1, "//gsml:GeologicUnit[@gml:id='gu.25678']", doc);
        assertXpathCount(1, "//gsml:GeologicUnit[@gml:id='gu.25682']", doc);

        // check for xlink:href if encoded within GeologicUnit itself
        // note that this can never be schema-valid, but the best that the
        // encoder can do when configured to use featureMembers.
        assertXpathCount(1, "//gsml:GeologicUnit[@xlink:href='#gu.25699']", doc);
        assertXpathCount(1, "//gsml:GeologicUnit[@xlink:href='#gu.25678']", doc);
        assertXpathCount(1, "//gsml:GeologicUnit[@xlink:href='#gu.25682']", doc);

        // restore fixture settings
        wfs = getGeoServer().getService(WFSInfo.class);
        wfs.setEncodeFeatureMember(encodeFeatureMember);
        getGeoServer().save(wfs);
    }
    
}
