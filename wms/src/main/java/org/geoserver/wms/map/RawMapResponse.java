/* Copyright (c) 2010 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wms.map;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

import org.geoserver.ows.Response;
import org.geoserver.platform.Operation;
import org.geoserver.platform.ServiceException;
import org.springframework.util.Assert;

/**
 * A {@link Response} to handle a {@link RawMap}
 * 
 * @author Gabriel Roldan
 * @see RawMap
 */
public class RawMapResponse extends AbstractMapResponse {

    public RawMapResponse(final Set<String> outputFormats) {
        super(RawMap.class, outputFormats);
    }

    @Override
    public void write(Object value, OutputStream output, Operation operation) throws IOException,
            ServiceException {
        Assert.isInstanceOf(RawMap.class, value);
        RawMap map = (RawMap) value;
        try {
            map.writeTo(output);
            output.flush();
        } finally {
            map.dispose();
        }
    }
}
