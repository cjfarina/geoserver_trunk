/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.wps;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;
import java.io.Writer;

import org.geoserver.wps.ppio.CDataPPIO;
import org.geotools.xml.EncoderDelegate;
import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;

/**
 * Encodes objects as text sections within CDATA markers
 * 
 * @author Andrea Aime - OpenGeo
 */
public class CDataEncoderDelegate implements EncoderDelegate {

	CDataPPIO ppio;
	Object object;

	public CDataEncoderDelegate(CDataPPIO ppio, Object object) {
		this.ppio = ppio;
		this.object = object;
	}

	public void encode(ContentHandler output) throws Exception {
		((LexicalHandler) output).startCDATA();
		PipedInputStream pins = new PipedInputStream();
        Writer w = new OutputStreamWriter(new PipedOutputStream(pins));
		ppio.encode(object, w);
		Reader r = new InputStreamReader(pins);
		char[] buffer = new char[1024];
		int read;
		while((read = r.read(buffer)) > 0) {
		    output.characters(buffer, 0, read);
		}
		w.close();
		r.close();
		((LexicalHandler) output).endCDATA();
	}

	public void encode(Writer w) throws Exception {
	    ppio.encode(object, w);
	}

}
