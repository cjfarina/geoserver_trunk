package org.geoserver.gss.internal.storage;

import org.geogit.api.GeoGIT;

import com.sleepycat.je.Environment;

public class GeoSyncDatabase {

    private final GeoGIT ggit;

    private final Environment env;

    public GeoSyncDatabase(GeoGIT ggit, Environment env) {
        this.ggit = ggit;
        this.env = env;
    }

    public void create() {
        // TODO Auto-generated method stub

    }

    public void close() {
        env.close();
    }

}
