/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.wcs;

import org.geoserver.config.GeoServerInfo;
import org.geoserver.platform.ServiceException;

/**
 * This defines an exception that can be turned into a valid xml service exception that wcs clients
 * will expect. All errors should be wrapped in this before returning to clients.
 * 
 * @author $Author: Alessio Fabiani (alessio.fabiani@gmail.com) $ (last modification)
 * @author Simone Giannecchini, GeoSolutions SAS
 * @version $Id$
 */
public class WcsException extends ServiceException {
    /**
	 * 
	 */
	private static final long serialVersionUID = -6110652531274829497L;

	public enum WcsExceptionCode {
        MissingParameterValue, InvalidParameterValue, NoApplicableCode, UnsupportedCombination, NotEnoughStorage, InvalidUpdateSequence, CurrentUpdateSequence
    }

    /**
     * The fixed MIME type of a WCS exception.
     */
    private static final String SE_XML = "application/vnd.ogc.se_xml";

    /**
     * Message constructor.
     * 
     * @param message
     *            The message for the .
     */
    public WcsException(String message) {
        super(message);
    }

    /**
     * Throwable constructor.
     * 
     * @param e
     *            The message for the .
     */
    public WcsException(Throwable e) {
        super(e);
    }

    /**
     * Message Locator constructor.
     * 
     * @param message
     *            The message for the .
     * @param locator
     *            The java class that caused the problem
     */
    public WcsException(String message, String locator) {
        super(message, locator);
    }

    public WcsException(String message, WcsExceptionCode code, String locator) {
        super(message, code.name(), locator);
    }

    public WcsException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param e
     *            The cause of failure
     * @param preMessage
     *            The message to tack on the front.
     * @param locator
     *            The java class that caused the problem
     */
    public WcsException(Throwable e, String preMessage, String locator) {
        super(e, preMessage, locator);
    }

    /**
     * Returns the mime type that should be exposed to the client when sending the exception
     * message.
     * 
     * <p>
     * Defaults to <code>geoserver.getMimeType()</code>
     * </p>
     * 
     * @return
     */
    public String getMimeType(GeoServerInfo geoserver) {
        return SE_XML + "; charset=" + geoserver.getCharset();
    }
}
