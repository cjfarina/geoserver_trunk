/* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.gss.service;

import org.geoserver.gss.internal.atom.FeedImpl;

public class GetEntriesResponse {

    private final FeedImpl response;

    private final String baseUrl;

    public GetEntriesResponse(String baseUrl, FeedImpl response) {
        this.baseUrl = baseUrl;
        this.response = response;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public FeedImpl getResult() {
        return response;
    }
}
