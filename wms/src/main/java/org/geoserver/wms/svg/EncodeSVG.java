/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wms.svg;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import org.geoserver.wms.Map;
import org.geoserver.wms.WMSMapContext;
import org.geotools.data.DataUtilities;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FilterType;
import org.geotools.filter.GeometryFilter;
import org.geotools.map.MapLayer;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

/**
 * Streaming SVG encoder (does not support styling)
 * 
 * @author Gabriel Roldan
 * @version $Id$
 */
public class EncodeSVG extends Map {

    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.vfny.geoserver.responses.wms.map");

    private static final String DOCTYPE = "<!DOCTYPE svg \n\tPUBLIC \"-//W3C//DTD SVG 20001102//EN\" \n\t\"http://www.w3.org/TR/2000/CR-SVG-20001102/DTD/svg-20001102.dtd\">\n";

    /** the XML and SVG header */
    private static final String SVG_HEADER = "<?xml version=\"1.0\" standalone=\"no\"?>\n\t"
            + "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" \n\tstroke=\"green\" \n\tfill=\"none\" \n\tstroke-width=\"0.1%\"\n\tstroke-linecap=\"round\"\n\tstroke-linejoin=\"round\"\n\twidth=\"_width_\" \n\theight=\"_height_\" \n\tviewBox=\"_viewBox_\" \n\tpreserveAspectRatio=\"xMidYMid meet\">\n";

    /** the SVG closing element */
    private static final String SVG_FOOTER = "</svg>\n";

    private WMSMapContext mapContext;

    private SVGWriter writer;

    /**
     * Creates a new EncodeSVG object.
     * 
     * @param mapContext
     * 
     */
    public EncodeSVG(WMSMapContext mapContext) {
        this.mapContext = mapContext;
    }

    public void encode(final OutputStream out) throws IOException {
        Envelope env = this.mapContext.getAreaOfInterest();
        this.writer = new SVGWriter(out, mapContext.getAreaOfInterest());
        writer.setMinCoordDistance(env.getWidth() / 1000);

        long t = System.currentTimeMillis();

        writeHeader();

        writeLayers();

        writer.write(SVG_FOOTER);

        this.writer.flush();
        t = System.currentTimeMillis() - t;
        LOGGER.info("SVG generated in " + t + " ms");

    }

    public String createViewBox() {
        Envelope referenceSpace = mapContext.getAreaOfInterest();
        String viewBox = writer.getX(referenceSpace.getMinX()) + " "
                + (writer.getY(referenceSpace.getMinY()) - referenceSpace.getHeight()) + " "
                + referenceSpace.getWidth() + " " + referenceSpace.getHeight();

        return viewBox;
    }

    private void writeHeader() throws IOException {
        // TODO: this does not write out the doctype definition, there should be
        // a configuration option wether to include it or not.
        String viewBox = createViewBox();
        String header = SVG_HEADER.replaceAll("_viewBox_", viewBox);
        header = header.replaceAll("_width_", String.valueOf(mapContext.getMapWidth()));
        header = header.replaceAll("_height_", String.valueOf(mapContext.getMapHeight()));
        writer.write(header);
    }

    private void writeDefs(SimpleFeatureType layer) throws IOException {
        GeometryDescriptor gtype = layer.getGeometryDescriptor();
        Class geometryClass = gtype.getType().getBinding();

        if ((geometryClass == MultiPoint.class) || (geometryClass == Point.class)) {
            writePointDefs();
        }
    }

    private void writePointDefs() throws IOException {
        writer.write("<defs>\n\t<circle id='point' cx='0' cy='0' r='0.25%' fill='blue'/>\n</defs>\n");
    }

    /**
     * 
     * 
     * @task TODO: respect layer filtering given by their Styles
     */
    private void writeLayers() throws IOException {
        MapLayer[] layers = mapContext.getLayers();
        int nLayers = layers.length;

        // FeatureTypeInfo layerInfo = null;
        int defMaxDecimals = writer.getMaximunFractionDigits();

        FilterFactory fFac = FilterFactoryFinder.createFilterFactory();

        for (int i = 0; i < nLayers; i++) {
            MapLayer layer = layers[i];
            SimpleFeatureIterator featureReader = null;
            SimpleFeatureSource fSource;
            fSource = (SimpleFeatureSource) layer.getFeatureSource();
            SimpleFeatureType schema = fSource.getSchema();

            try {
                Expression bboxExpression = fFac.createBBoxExpression(mapContext
                        .getAreaOfInterest());
                GeometryFilter bboxFilter = fFac
                        .createGeometryFilter(FilterType.GEOMETRY_INTERSECTS);
                bboxFilter.addLeftGeometry(fFac.createAttributeExpression(schema, schema
                        .getGeometryDescriptor().getName().getLocalPart()));
                bboxFilter.addRightGeometry(bboxExpression);

                Query bboxQuery = new Query(schema.getTypeName(), bboxFilter);
                Query definitionQuery = layer.getQuery();
                Query finalQuery = new Query(DataUtilities.mixQueries(definitionQuery, bboxQuery,
                        "svgEncoder"));
                finalQuery.setHints(definitionQuery.getHints());
                finalQuery.setSortBy(definitionQuery.getSortBy());
                finalQuery.setStartIndex(definitionQuery.getStartIndex());

                LOGGER.fine("obtaining FeatureReader for " + schema.getTypeName());
                featureReader = fSource.getFeatures(finalQuery).features();
                LOGGER.fine("got FeatureReader, now writing");

                String groupId = null;
                String styleName = null;

                groupId = schema.getTypeName();

                styleName = layer.getStyle().getName();

                writer.write("<g id=\"" + groupId + "\"");

                if (!styleName.startsWith("#")) {
                    writer.write(" class=\"" + styleName + "\"");
                }

                writer.write(">\n");

                writeDefs(schema);

                writer.writeFeatures(fSource.getSchema(), featureReader, styleName);
                writer.write("</g>\n");
            } catch (IOException ex) {
                throw ex;
            } catch (Throwable t) {
                LOGGER.warning("UNCAUGHT exception: " + t.getMessage());

                IOException ioe = new IOException("UNCAUGHT exception: " + t.getMessage());
                ioe.setStackTrace(t.getStackTrace());
                throw ioe;
            } finally {
                if (featureReader != null) {
                    featureReader.close();
                }
            }
        }
    }
}
