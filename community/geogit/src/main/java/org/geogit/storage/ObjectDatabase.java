package org.geogit.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.map.LRUMap;
import org.geogit.api.ObjectId;
import org.geotools.util.logging.Logging;
import org.springframework.util.Assert;

import com.ning.compress.lzf.LZFInputStream;
import com.ning.compress.lzf.LZFOutputStream;
import com.sleepycat.collections.CurrentTransaction;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

/**
 * @TODO: extract interface
 */
public class ObjectDatabase {

    private static final Logger LOGGER = Logging.getLogger(ObjectDatabase.class);

    private final Environment env;

    private Database objectDb;

    private CurrentTransaction txn;

    private Map<ObjectId, Object> cache;

    @SuppressWarnings("unchecked")
    public ObjectDatabase(final Environment env) {
        this.env = env;
        // TODO: use an external cache
        final long maxMemMegs = Runtime.getRuntime().maxMemory() / 1024 / 1024;
        final int maxSize;
        if (maxMemMegs <= 64) {
            maxSize = 16;
        } else if (maxMemMegs <= 100) {
            maxSize = 64;
        } else if (maxMemMegs <= 200) {
            maxSize = 128;
        } else {
            maxSize = 256;
        }
        cache = new LRUMap(maxSize);
    }

    public void close() {
        objectDb.close();
    }

    public void create() {
        txn = CurrentTransaction.getInstance(env);
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        dbConfig.setTransactional(env.getConfig().getTransactional());
        this.objectDb = env.openDatabase(null, "BlobStore", dbConfig);
    }

    public boolean exists(final ObjectId id) {
        Assert.notNull(id, "id");

        DatabaseEntry key = new DatabaseEntry(id.getRawValue());
        DatabaseEntry data = new DatabaseEntry();
        // tell db not to retrieve data
        data.setPartial(true);

        final LockMode lockMode = LockMode.DEFAULT;
        CurrentTransaction.getInstance(env);
        OperationStatus status = objectDb.get(txn.getTransaction(), key, data, lockMode);
        return OperationStatus.SUCCESS == status;
    }

    /**
     * @param id
     * @return
     * @throws IOException
     * @throws IllegalArgumentException
     *             if an object with such id does not exist
     */
    public InputStream getRaw(final ObjectId id) throws IOException {
        Assert.notNull(id, "id");
        DatabaseEntry key = new DatabaseEntry(id.getRawValue());
        DatabaseEntry data = new DatabaseEntry();

        final LockMode lockMode = LockMode.READ_COMMITTED;
        Transaction transaction = txn.getTransaction();
        OperationStatus operationStatus = objectDb.get(transaction, key, data, lockMode);
        if (OperationStatus.NOTFOUND.equals(operationStatus)) {
            throw new IllegalArgumentException("Object does not exist: " + id.toString());
        }
        final byte[] cData = data.getData();

        // return new GZIPInputStream(new ByteArrayInputStream(cData));
        // return new ByteArrayInputStream(cData);
        return new LZFInputStream(new ByteArrayInputStream(cData));
    }

    /**
     * @param <T>
     * @param id
     * @param reader
     * @return
     * @throws IOException
     * @throws IllegalArgumentException
     *             if an object with such id does not exist
     */
    public <T> T get(final ObjectId id, final ObjectReader<T> reader) throws IOException {
        Assert.notNull(id, "id");
        Assert.notNull(reader, "reader");

        T object;
        InputStream raw = getRaw(id);
        try {
            object = reader.read(id, raw);
        } finally {
            raw.close();
        }
        return object;
    }

    /**
     * Returns a possibly cached version of the object identified by the given {@code id}.
     * <p>
     * The returned object is meant to be immutable. If any modification is to be made the calling
     * code is in charge of cloning the returned object so that it doesn't affect the cached
     * version.
     * </p>
     * 
     * @param <T>
     *            the type of object returned
     * @param id
     *            the id of the object to return from the cache, or to look up in the database and
     *            cache afterwards.
     * @param reader
     *            the reader to use in the case of a cache miss.
     * @return the cached version of the required object.
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public <T> T getCached(final ObjectId id, final ObjectReader<T> reader) throws IOException {
        Assert.notNull(id, "id");
        Assert.notNull(reader, "reader");

        T object = (T) cache.get(id);
        if (object == null) {
            object = get(id, reader);
            if (object != null) {
                cache.put(id, object);
            }
        }
        return object;
    }

    /**
     * 
     */
    public <T> ObjectId put(final ObjectWriter<T> writer) throws Exception {
        MessageDigest sha1;
        sha1 = MessageDigest.getInstance("SHA1");

        ByteArrayOutputStream rawOut = new ByteArrayOutputStream();

        DigestOutputStream keyGenOut = new DigestOutputStream(rawOut, sha1);
        // GZIPOutputStream cOut = new GZIPOutputStream(keyGenOut);
        LZFOutputStream cOut = new LZFOutputStream(keyGenOut);

        try {
            writer.write(cOut);
        } finally {
            // cOut.finish();
            cOut.flush();
            cOut.close();
            keyGenOut.flush();
            keyGenOut.close();
            rawOut.flush();
            rawOut.close();
        }

        final byte[] rawKey = keyGenOut.getMessageDigest().digest();
        final byte[] rawData = rawOut.toByteArray();

        final ObjectId id = new ObjectId(rawKey);
        DatabaseEntry key = new DatabaseEntry(rawKey);
        DatabaseEntry data = new DatabaseEntry(rawData);

        OperationStatus status = objectDb.putNoOverwrite(txn.getTransaction(), key, data);
        if (LOGGER.isLoggable(Level.FINER)) {
            if (OperationStatus.SUCCESS == status) {
                LOGGER.finer("Successfully inserted object with writer " + writer
                        + ". Resulting id: " + id);
            } else {
                LOGGER.finer("Key already exists in blob store, blob reused for id: " + id);
            }
        }
        return id;
    }

    /**
     * @param id
     * @param writer
     * @return {@code true} if the object was inserted and it didn't exist previously, {@code false}
     *         if the object was inserted and it replaced an already existing object with the same
     *         key.
     * @throws Exception
     */
    public boolean put(final ObjectId id, final ObjectWriter<?> writer) throws Exception {
        ByteArrayOutputStream rawOut = new ByteArrayOutputStream();
        // GZIPOutputStream cOut = new GZIPOutputStream(rawOut);
        LZFOutputStream cOut = new LZFOutputStream(rawOut);
        try {
            // writer.write(cOut);
            writer.write(cOut);
        } finally {
            // cOut.finish();
            cOut.flush();
            cOut.close();
            rawOut.flush();
            rawOut.close();
        }

        final byte[] rawKey = id.getRawValue();
        final byte[] rawData = rawOut.toByteArray();

        DatabaseEntry key = new DatabaseEntry(rawKey);
        DatabaseEntry data = new DatabaseEntry(rawData);

        OperationStatus status = objectDb.put(txn.getTransaction(), key, data);
        return OperationStatus.SUCCESS.equals(status);
    }

    public ObjectInserter newObjectInserter() {
        return new ObjectInserter(this);
    }

}
