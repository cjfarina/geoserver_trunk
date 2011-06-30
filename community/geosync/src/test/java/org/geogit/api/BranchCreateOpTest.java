package org.geogit.api;

import org.geogit.test.RepositoryTestCase;

public class BranchCreateOpTest extends RepositoryTestCase {

    private GeoGIT ggit;

    @Override
    protected void setUpInternal() throws Exception {
        this.ggit = new GeoGIT(getRepository());
    }

    public void testBranchHead() {

    }
}
