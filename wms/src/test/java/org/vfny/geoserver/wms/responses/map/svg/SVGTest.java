package org.vfny.geoserver.wms.responses.map.svg;

import java.io.InputStream;
import java.net.URL;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.Test;

import org.geoserver.data.test.MockData;
import org.geoserver.wms.WMSTestSupport;
import org.vfny.geoserver.config.WMSConfig;
import org.w3c.dom.Document;

public class SVGTest extends WMSTestSupport {
    
    /**
     * This is a READ ONLY TEST so we can use one time setup
     */
    public static Test suite() {
        return new OneTimeTestSetup(new SVGTest());
    }
    
    @Override
    protected void populateDataDirectory(MockData dataDirectory) throws Exception {
        super.populateDataDirectory(dataDirectory);
        dataDirectory.addStyle("multifts", getClass().getResource("./polyMultiFts.sld"));
    }
    
    public void testBasicSvgGenerator() throws Exception {
        getWMS().setSvgRenderer(WMSConfig.SVG_SIMPLE);
            Document doc = getAsDOM(
                "wms?request=getmap&service=wms&version=1.1.1" + 
                "&format=" + SvgMapProducerProxy.MIME_TYPE + 
                "&layers=" + MockData.BASIC_POLYGONS.getPrefix() + ":" + MockData.BASIC_POLYGONS.getLocalPart() + 
                "&styles=" + MockData.BASIC_POLYGONS.getLocalPart() + 
                "&height=1024&width=1024&bbox=-180,-90,180,90&srs=EPSG:4326" +  
                "&featureid=BasicPolygons.1107531493643"
            );
            
            assertEquals( 1, doc.getElementsByTagName("svg").getLength());
            assertEquals( 1, doc.getElementsByTagName("g").getLength());
    }
    
    public void testBasicSvgGeneratorMultipleFts() throws Exception {
        getWMS().setSvgRenderer(WMSConfig.SVG_SIMPLE);
            Document doc = getAsDOM(
                "wms?request=getmap&service=wms&version=1.1.1" + 
                "&format=" + SvgMapProducerProxy.MIME_TYPE + 
                "&layers=" + getLayerId(MockData.BASIC_POLYGONS) + 
                "&styles=multifts" + 
                "&height=1024&width=1024&bbox=-180,-90,180,90&srs=EPSG:4326" +  
                "&featureid=BasicPolygons.1107531493643"
            );
            
            assertEquals( 1, doc.getElementsByTagName("svg").getLength());
            assertEquals( 1, doc.getElementsByTagName("g").getLength());
    }
    
    public void testBatikSvgGenerator() throws Exception {
        //batik includes DTD reference which forces us to be online, skip test
        // in offline case
        try {
            new URL( "http://www.w3.org").openConnection().connect();
        } catch (Exception e) {
            return;
        }
        
        getWMS().setSvgRenderer(WMSConfig.SVG_BATIK);
        Document doc = getAsDOM(
            "wms?request=getmap&service=wms&version=1.1.1" + 
            "&format=" + SvgMapProducerProxy.MIME_TYPE + 
            "&layers=" + getLayerId(MockData.BASIC_POLYGONS) + 
            "&styles=" + MockData.BASIC_POLYGONS.getLocalPart() + 
            "&height=1024&width=1024&bbox=-180,-90,180,90&srs=EPSG:4326" +  
            "&featureid=BasicPolygons.1107531493643"
        );
        
        assertEquals( 1, doc.getElementsByTagName("svg").getLength());
        assertTrue(doc.getElementsByTagName("g").getLength() > 1);
    }
    
    public void testBatikMultipleFts() throws Exception {
        //batik includes DTD reference which forces us to be online, skip test
        // in offline case
        try {
            new URL( "http://www.w3.org").openConnection().connect();
        } catch (Exception e) {
            return;
        }
        
        getWMS().setSvgRenderer(WMSConfig.SVG_BATIK);
        Document doc = getAsDOM(
            "wms?request=getmap&service=wms&version=1.1.1" + 
            "&format=" + SvgMapProducerProxy.MIME_TYPE + 
            "&layers=" + getLayerId(MockData.BASIC_POLYGONS) + 
            "&styles=multifts" + 
            "&height=1024&width=1024&bbox=-180,-90,180,90&srs=EPSG:4326" +  
            "&featureid=BasicPolygons.1107531493643"
        );
        
        assertEquals( 1, doc.getElementsByTagName("svg").getLength());
        assertTrue(doc.getElementsByTagName("g").getLength() > 1);
    }
    
}
