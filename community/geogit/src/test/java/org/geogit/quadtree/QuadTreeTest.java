package org.geogit.quadtree;

import java.security.MessageDigest;
import java.util.Arrays;

import org.geogit.api.ObjectId;
import org.geogit.test.RepositoryTestCase;
import org.opengis.feature.Feature;

public class QuadTreeTest extends RepositoryTestCase {

    private QTree tree;

    @Override
    protected void setUpInternal() throws Exception {
        // this.tree = new QTree(environment);
        // tree.create();
    }

    public void test1() throws Exception {

        MessageDigest md = MessageDigest.getInstance("SHA1");

        for (Feature f : Arrays.asList(feature1_1, feature1_2, feature1_3)) {
            String fid = f.getIdentifier().getID();

            System.out.printf("id: %s, sha1: %s, %s\n", fid,
                    ObjectId.toString(md.digest(fid.getBytes("UTF-8"))),
                    new String(fid.getBytes("UTF-8")));

            // tree.insert(f.getBounds().getMinX(), f.getBounds().getMinY(),
            // f.getBounds().getMaxX(),
            // f.getBounds().getMaxY(), f.getIdentifier().getID());
        }
    }

}
