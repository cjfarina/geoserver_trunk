/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wps.gs;

import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.operator.ConstantDescriptor;

import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.types.Environment.Variable;
import org.geoserver.wps.WPSException;
import org.geoserver.wps.jts.DescribeParameter;
import org.geoserver.wps.jts.DescribeProcess;
import org.geoserver.wps.jts.DescribeResult;
import org.geoserver.wps.jts.DescribeResults;
import org.geoserver.wps.resource.WPSFileResource;
import org.geoserver.wps.resource.WPSResourceManager;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.data.Query;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.image.ImageWorker;
import org.geotools.process.ProcessException;
import org.geotools.referencing.CRS;
import org.geotools.util.Utilities;
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Georectifies a GridCoverage based on GCPs using gdal_warp under covers
 * 
 * @author Daniele Romagnoli, GeoSolutions SAS
 * @author Andrea Aime, GeoSolutions SAS
 */
@DescribeProcess(title = "GeorectifyCoverage", description = "Process which allows to georectify a coverage through GCPs using gdal_warp")
public class GeorectifyCoverage implements GeoServerProcess {

    private final static Pattern GCP_PATTERN = Pattern
            .compile("\\[((\\+|-)?[0-9]+(.[0-9]+)?), ((\\+|-)?[0-9]+(.[0-9]+)?)(, ((\\+|-)?[0-9]+(.[0-9]+)?))?\\]");

    GeorectifyConfiguration config;

    WPSResourceManager resourceManager;

    public GeorectifyConfiguration getConfig() {
        return config;
    }

    public void setConfig(GeorectifyConfiguration config) {
        this.config = config;
    }

    public GeorectifyCoverage(GeorectifyConfiguration config) {
        this.config = config;
    }

    public GeorectifyCoverage() {

    }

    @DescribeResults({
        @DescribeResult(name = "result", description = "The GridCoverage2D coming from the georectification process", type=GridCoverage2D.class),
        @DescribeResult(name = "path", description = "The server side path to the generated coverage", type=String.class)
    })
    public Map<String, Object> execute(
            @DescribeParameter(name = "data", description = "The input raster to transform") GridCoverage2D coverage,
            @DescribeParameter(name = "gcp", description = "The Ground Control Points list in the form ") String gcps,
            @DescribeParameter(name = "bbox", description = "The destination bounding box", min = 0) Envelope bbox,
            @DescribeParameter(name = "targetCRS", description = "The destination coordinate refence system") CoordinateReferenceSystem crs,
            @DescribeParameter(name = "width", description = "The final image width", min = 0) Integer width,
            @DescribeParameter(name = "height", description = "The final image height", min = 0) Integer height,
            @DescribeParameter(name = "warpOrder", min = 0, description = "The order of the warping polynomial (optional)") Integer warpOrder,
            @DescribeParameter(name = "transparent", min = 0, description = "Force the output image to have transparent background") Boolean transparent,
            @DescribeParameter(name = "store", min = 0, description = "Don't remove the output file once done") Boolean store,
            @DescribeParameter(name = "outputPath", min = 0, description = "Full path where the output file has to be stored") String outputPath)
            throws IOException {

        GeoTiffReader reader = null;
        List<File> removeFiles = new ArrayList<File>();
        String location = null;
        try {
            File tempFolder = config.getTempFolder();
            File loggingFolder = config.getLoggingFolder();

            // do we have to add the alpha channel?
            boolean forceTransparent = false;
            if (transparent == null) {
                transparent = true;
            }
            ColorModel cm = coverage.getRenderedImage().getColorModel();
            if (cm.getTransparency() == Transparency.OPAQUE && transparent) {
                forceTransparent = true;
            }

            // //
            //
            // STEP 1: Getting the dataset to be georectified
            //
            // //
            final Object fileSource = coverage.getProperty(AbstractGridCoverage2DReader.FILE_SOURCE_PROPERTY);
            if (fileSource != null && fileSource instanceof String) {
                location = (String) fileSource;
            }
            if (location == null) {
                RenderedImage image = coverage.getRenderedImage();
                if (forceTransparent) {
                    ImageWorker iw = new ImageWorker(image);
                    iw.forceComponentColorModel();
                    final ImageLayout tempLayout = new ImageLayout(image);
                    tempLayout.unsetValid(ImageLayout.COLOR_MODEL_MASK).unsetValid(
                            ImageLayout.SAMPLE_MODEL_MASK);
                    RenderedImage alpha = ConstantDescriptor.create(
                            Float.valueOf(image.getWidth()), Float.valueOf(image.getHeight()),
                            new Byte[] { Byte.valueOf((byte) 255) }, new RenderingHints(
                                    JAI.KEY_IMAGE_LAYOUT, tempLayout));
                    iw.addBand(alpha, false);
                    image = iw.getRenderedImage();
                    cm = image.getColorModel();
                }
                File storedImageFile = storeImage(image, tempFolder);
                location = storedImageFile.getAbsolutePath();
                removeFiles.add(storedImageFile);
            }

            // //
            //
            // STEP 2: Adding Ground Control Points
            //
            // //
            final int gcpNum[] = new int[1];
            final String gcp = parseGcps(gcps, gcpNum);
            File vrtFile = addGroundControlPoints(location, gcp,
                    config.getGdalTranslateParameters());
            if (vrtFile == null || !vrtFile.exists() || !vrtFile.canRead()) {
                throw new IOException(
                        "Unable to get a valid file with attached Ground Control Points");
            }
            removeFiles.add(vrtFile);

            // //
            //
            // STEP 3: Warping
            //
            // //
            File warpedFile = warpFile(vrtFile, bbox, crs, width, height, warpOrder, tempFolder,
                    loggingFolder, config.getExecutionTimeout(), config.getGdalWarpingParameters());
            if (warpedFile == null || !warpedFile.exists() || !warpedFile.canRead()) {
                throw new IOException("Unable to get a valid georectified file");
            }

            boolean expand = false;
            if (cm instanceof IndexColorModel) {
                expand = true;
            } else if (cm instanceof ComponentColorModel && cm.getNumComponents() == 1
                    && cm.getComponentSize()[0] == 1) {
                expand = true;
            }
            if (expand) {
                removeFiles.add(warpedFile);
                warpedFile = expandRgba(warpedFile.getAbsolutePath());
            }

            // if we have the output path move the final file there
            if(Boolean.TRUE.equals(store) && outputPath != null) {
                File output = new File(outputPath);
                if(output.exists()) {
                    if(!output.delete()) {
                        throw new WPSException("Output file " + outputPath + " exists but cannot be overwritten");
                    }
                } else {
                    File parent = output.getParentFile();
                    if(!parent.exists()) {
                        if(!parent.mkdirs()) {
                            throw new WPSException("Output file parent directory " 
                                    + parent.getAbsolutePath() + " does not exist and cannot be created");
                        }
                    }
                }
                if(!warpedFile.renameTo(output)) {
                    throw new WPSException("Could not move " 
                            + warpedFile.getAbsolutePath() + " to " + outputPath 
                            + ", it's likely a permission issue");
                }
                warpedFile = output;
            }
                
             // mark the output file for deletion at the end of request
                if (resourceManager != null && !Boolean.TRUE.equals(store)) {
                    resourceManager.addResource(new WPSFileResource(warpedFile));
                }

            // //
            //
            // FINAL STEP: Returning the warped gridcoverage
            //
            // //
            reader = new GeoTiffReader(warpedFile);
            GridCoverage2D cov = addLocationProperty(reader.read(null), warpedFile);
            
            
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("result", cov);
            result.put("path", warpedFile.getAbsolutePath());
            return result;
        } finally {
            if (reader != null) {
                try {
                    reader.dispose();
                } catch (Throwable t) {
                    // Does nothing
                }
            }

            for (File file : removeFiles) {
                deleteFile(file);
            }
        }
    }

    GridCoverage2D addLocationProperty(GridCoverage2D coverage, File warpedFile) {
        Map <String, String> properties = new HashMap<String,String>();
        properties.put(AbstractGridCoverage2DReader.FILE_SOURCE_PROPERTY, warpedFile.getAbsolutePath());

        return new GridCoverageFactory().create(coverage.getName(), coverage.getRenderedImage(), 
                coverage.getGridGeometry(), coverage.getSampleDimensions(), null, properties);
    }

    /**
     * Given a target query and a target grid geometry returns the query to be used to read the
     * input data of the process involved in rendering. This method will be called only if the input
     * data is a feature collection.
     * 
     * @param targetQuery
     * @param gridGeometry
     * @return The transformed query, or null if no inversion is possible/meaningful
     */
    public Query invertQuery(Query targetQuery, GridGeometry gridGeometry) {
        return targetQuery;
    }

    /**
     * Given a target query and a target grid geometry returns the grid geometry to be used to read
     * the input data of the process involved in rendering. This method will be called only if the
     * input data is a grid coverage or a grid coverage reader
     * 
     * @param targetQuery
     * @param gridGeometry
     * @return The transformed query, or null if no inversion is possible/meaningful
     */
    public GridGeometry invertGridGeometry(Query targetQuery, GridGeometry targetGridGeometry) {
        // we need the entire image, we don't know how to invert the warping
        return null;
    }

    /**
     * Store a GridCoverage2D and returns the file where the underlying image have been stored.
     * 
     * @param coverage a {@link GridCoverage2D} wrapping the image to be stored.
     * @param tempFolder
     * @return the {@link File} storing the image.
     * @throws IOException
     */
    private File storeImage(final RenderedImage image, final File tempFolder) throws IOException {
        File file = File.createTempFile("readCoverage", ".tif", tempFolder);
        new ImageWorker(image).writeTIFF(file, null, 0, 256, 256);
        return file;
    }

    /**
     * 
     * @param originalFile {@link File} referring the dataset to be warped
     * @param targetEnvelope the target envelope
     * @param width the final image's width
     * @param height the final image's height
     * @param targetCrs the target coordinate reference system
     * @param order
     * @param tempFolder
     * @param loggingFolder
     * @param timeOut
     * @return
     * @throws IOException
     */
    private File warpFile(final File originalFile, final Envelope targetEnvelope, final CoordinateReferenceSystem targetCRS,
            final Integer width, final Integer height, final Integer order, final File tempFolder,
            final File loggingFolder, final Long timeOut, final String warpingParameters)
            throws IOException {
        final File file = File.createTempFile("warped", ".tif", tempFolder);
        final String vrtFilePath = originalFile.getAbsolutePath();
        final String outputFilePath = file.getAbsolutePath();
        final String tEnvelope = parseBBox(targetEnvelope);
        final String tCrs = parseCrs(targetCRS);
        final String argument = buildWarpArgument(tEnvelope, width, height, tCrs, order,
                vrtFilePath, outputFilePath, warpingParameters);
        final String gdalCommand = config.getWarpingCommand();

        executeCommand(gdalCommand, argument, loggingFolder, timeOut, config.getEnvVariables());
        return file;
    }

    /**
     * A simple utility method setting up the command arguments for gdalWarp
     * 
     * @param targetEnvelope the target envelope in the form: xmin ymin xmax ymax
     * @param width the target image width
     * @param height the target image height
     * @param targetCrs the target crs
     * @param order the warping polynomial order
     * @param inputFilePath the path of the file referring to the dataset to be warped
     * @param outputFilePath the path of the file referring to the produced dataset
     * @return
     */
    private final static String buildWarpArgument(final String targetEnvelope, final Integer width,
            final Integer height, final String targetCrs, final Integer order,
            final String inputFilePath, final String outputFilePath, final String warpingParameters) {
        String imageSize = width != null && height != null ? " -ts " + width + " " + height : "";
        String te = targetEnvelope != null && targetEnvelope.length() > 0 ? "-te " + targetEnvelope : ""; 
        return  te + imageSize + " -t_srs " + targetCrs
                + " " + (order != null ? " -order " + order : "") + " " + warpingParameters + " \""
                + inputFilePath + "\" \"" + outputFilePath + "\"";
    }

    private static void checkError(File logFile) {
        InputStream stream = null;
        InputStreamReader streamReader = null;
        BufferedReader reader = null;
        StringBuilder message = new StringBuilder();
        try {
            stream = new FileInputStream(logFile);
            streamReader = new InputStreamReader(stream);
            reader = new BufferedReader(streamReader);
            String strLine;
            while ((strLine = reader.readLine()) != null) {
                message.append(strLine);
            }
            throw new ProcessException(message.toString());
        } catch (Throwable t) {

        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(streamReader);
            IOUtils.closeQuietly(stream);
            // TODO: look for a better delete
            deleteFile(logFile);
        }

    }

    /**
     * Parse the bounding box to be used by gdalwarp command
     * 
     * @param boundingBox
     * @return
     */
    private static String parseBBox(Envelope re) {
        if(re == null) {
            return "";
        } else {
            return re.getMinX() + " " + re.getMinY() + " " + re.getMaxX() + " " + re.getMaxY();
        }
    }

    private static String parseCrs(CoordinateReferenceSystem crs) {
        Utilities.ensureNonNull("coordinateReferenceSystem", crs);
        try {
            return "\"epsg:" + CRS.lookupEpsgCode(crs, true) + "\"";
        } catch (FactoryException e) {
            throw new WPSException("Error occurred looking up target SRS");
        }
    }

    /**
     * First processing step which setup a VRT by adding ground control points to the specified
     * input file.
     * 
     * @param originalFilePath the path of the file referring to the original image.
     * @param gcp the Ground Control Points option to be attached to the translating command.
     * @return a File containing the translated dataset.
     * @throws IOException
     */
    private File addGroundControlPoints(final String originalFilePath, final String gcp,
            final String parameters) throws IOException {
        final File vrtFile = File.createTempFile("vrt_", ".vrt", config.getTempFolder());
        final String argument = "-of VRT " + parameters + " " + gcp + "\"" + originalFilePath
                + "\" \"" + vrtFile.getAbsolutePath() + "\"";
        final String gdalCommand = config.getTranslateCommand();
        executeCommand(gdalCommand, argument, config.getLoggingFolder(),
                config.getExecutionTimeout(), config.getEnvVariables());
        if (vrtFile != null && vrtFile.exists() && vrtFile.canRead()) {
            return vrtFile;
        }
        return vrtFile;
    }

    private File expandRgba(final String originalFilePath) throws IOException {
        final File expandedFile = File.createTempFile("rgba", ".tif", config.getTempFolder());
        final String argument = "-expand RGBA -co TILED=yes -co COMPRESS=LZW " + originalFilePath + " "
                + expandedFile.getAbsolutePath();
        final String gdalCommand = config.getTranslateCommand();
        executeCommand(gdalCommand, argument, config.getLoggingFolder(),
                config.getExecutionTimeout(), config.getEnvVariables());
        return expandedFile;
    }

    /**
     * Execute the following command, given the specified argument and return the File storing
     * logged error messages (if any).
     */
    private static void executeCommand(final String gdalCommand, final String argument,
            final File loggingFolder, final long timeOut, final List<Variable> envVars)
            throws IOException {
        final File logFile = File.createTempFile("LOG", ".log", loggingFolder);

        Project project = new Project();
        project.init();

        ExecTask execTask = new ExecTask();
        execTask.setProject(project);

        // Setting executable
        execTask.setExecutable(gdalCommand);
        if (envVars != null) {
            for (Variable var : envVars) {
                execTask.addEnv(var);
            }
        }

        // Setting command line argument
        execTask.createArg().setLine(argument);
        execTask.setLogError(true);

        execTask.setError(logFile);
        execTask.setOutput(logFile);
        execTask.setFailonerror(true);
        execTask.setTimeout(timeOut);

        System.out.println("Executing " + gdalCommand + " " + argument);

        // Executing
        try {
            execTask.execute();
        } catch (Exception e) {
            if (logFile.exists() && logFile.canRead()) {
                checkError(logFile);
            }
            throw new WPSException("Error launching OS command", e);
        } finally {
            if (logFile != null) {
                logFile.delete();
            }
        }

    }

    /**
     * @param gcps
     * @param gcpNum
     * @return
     */
    private String parseGcps(String gcps, int[] gcpNum) {
        Matcher gcpMatcher = GCP_PATTERN.matcher(gcps);
        // if(!gcpMatcher.matches()) {
        // throw new WPSException("Invalid GCP syntax:" + gcps);
        // }
        StringBuilder gcpCommand = new StringBuilder();
        int gcpPoints = 0;
        // Setting up gcp command arguments
        while (gcpMatcher.find()) {
            String gcp = "-gcp ";
            String pixels = gcpMatcher.group(0);
            gcpMatcher.find();
            String lines = gcpMatcher.group(0);
            gcp += pixels.replace("[", "").replace("]", "").replace(",", "") + " "
                    + lines.replace("[", "").replace("]", "").replace(",", "") + " ";
            gcpCommand.append(gcp);
            gcpPoints++;
        }
        gcpNum[0] = gcpPoints;
        return gcpCommand.toString();
    }

    private static void deleteFile(final File file) {
        if (file != null && file.exists() && file.canRead()) {
            file.delete();
        }
    }

    public WPSResourceManager getResourceManager() {
        return resourceManager;
    }

    public void setResourceManager(WPSResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }
}