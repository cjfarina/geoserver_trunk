/* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geogit.api;

import org.geogit.repository.Repository;

public class CheckoutOp extends AbstractGeoGitOp<Ref> {

    private String refName;

    public CheckoutOp(Repository repository) {
        super(repository);
    }

    public CheckoutOp setName(final String refName) {
        this.refName = refName;
        return this;
    }

    public Ref call() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
