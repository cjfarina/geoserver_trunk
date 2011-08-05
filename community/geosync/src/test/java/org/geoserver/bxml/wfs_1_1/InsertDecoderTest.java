package org.geoserver.bxml.wfs_1_1;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import net.opengis.wfs.InsertElementType;

import org.geoserver.bxml.BxmlTestSupport;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.config.GeoServer;
import org.geotools.feature.NameImpl;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;

public class InsertDecoderTest extends BxmlTestSupport {

    public void testFeedDecodeDelete1() throws Exception {

        Catalog mockCatalog = mock(Catalog.class);
        FeatureTypeInfo mockFeatureTypeInfo = mock(FeatureTypeInfo.class);
        
        SimpleFeatureType mockFeatureType = mock(SimpleFeatureType.class);
        when(mockFeatureType.getAttributeCount()).thenReturn(6);
        when(mockFeatureTypeInfo.getFeatureType()).thenReturn(mockFeatureType);
        Name typeName = new NameImpl("http://opengeo.org/osm", "planet_osm_point");
        when(
                mockCatalog.getFeatureTypeByName(eq(typeName.getNamespaceURI()),
                        eq(typeName.getLocalPart()))).thenReturn(mockFeatureTypeInfo);

        BxmlStreamReader reader = super.getReader("insert_point");
        reader.nextTag();
        InsertElementTypeDecoder decoder = new InsertElementTypeDecoder(mockCatalog);
        InsertElementType insertElement = (InsertElementType) decoder.decode(reader);

        /*
         * QName deleteTypeName = insertElement.getTypeName();
         * assertEquals("http://opengeo.org/osm", deleteTypeName.getNamespaceURI());
         * assertEquals("planet_osm_point", deleteTypeName.getLocalPart()); Filter filter =
         * deleteElement.getFilter(); assertNotNull(deleteElement.getFilter());
         * assertNotNull(filter);
         */

        reader.close();
    }

}
