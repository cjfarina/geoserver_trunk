package org.geogit.quadtree;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.ClassCatalog;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.Transaction;
import com.vividsolutions.jts.geom.Envelope;

/**
 * @TODO: extract interface
 */
public class QTree {

    private final Environment env;

    private Database treeDb;

    private ClassCatalog classCatalog;

    private final EntryBinding<INode> NODE_BINDING = new SerialBinding<INode>(classCatalog,
            INode.class);

    public QTree(final Environment env) {
        this.env = env;
    }

    public void create() {
        Transaction txn = null;
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        treeDb = env.openDatabase(txn, "TreeStore", dbConfig);
        classCatalog = new StoredClassCatalog(treeDb);
    }

    public void close() {
        // env is not closed as it's passed as argument and hence not owned by this class
        classCatalog.close();
        treeDb.close();
    }

    public void insert(double minX, double minY, double maxX, double maxY, String id) {
        INode node = new INode(new Envelope(minX, maxX, minY, maxY), id);

        DatabaseEntry value = new DatabaseEntry();
        NODE_BINDING.objectToEntry(node, value);
    }

}
