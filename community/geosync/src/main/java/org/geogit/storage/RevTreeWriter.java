package org.geogit.storage;

import static org.geogit.storage.BLOBS.BUCKET;
import static org.geogit.storage.BLOBS.ENTRY;
import static org.geogit.storage.BLOBS.STRING;
import static org.geogit.storage.BLOBS.TREE;

import java.io.IOException;
import java.io.OutputStream;

import org.geogit.api.ObjectId;
import org.geogit.api.Ref;
import org.geogit.api.RevTree;
import org.geogit.api.TreeVisitor;
import org.gvsig.bxml.stream.BxmlOutputFactory;
import org.gvsig.bxml.stream.BxmlStreamWriter;

public class RevTreeWriter implements ObjectWriter<RevTree> {

    private final RevSHA1Tree tree;

    public RevTreeWriter(RevTree tree) {
        this.tree = (RevSHA1Tree) tree;
    }

    /**
     * @see org.geogit.storage.ObjectWriter#write(java.io.OutputStream)
     */
    public void write(final OutputStream out) throws IOException {
        tree.normalize();

        final BxmlOutputFactory outputFactory = BLOBS.cachedOutputFactory;
        final BxmlStreamWriter writer = outputFactory.createSerializer(out);

        try {
            writer.writeStartDocument();
            writer.writeStartElement(TREE);
            {
                TreeVisitor visitor = new WriteTreeVisitor(writer);
                tree.accept(visitor);
            }
            writer.writeEndElement();
            writer.writeEndDocument();
        } finally {
            writer.flush();
        }

    }

    private static final class WriteTreeVisitor implements TreeVisitor {

        private final BxmlStreamWriter writer;

        public WriteTreeVisitor(final BxmlStreamWriter writer) {
            this.writer = writer;
        }

        /**
         * @see org.geogit.api.TreeVisitor#visitEntry(org.geogit.api.Ref)
         */
        public boolean visitEntry(final Ref ref) {
            try {
                writer.writeStartElement(ENTRY);
                writer.writeStartAttribute("", "type");
                writer.writeValue(ref.getType().value());
                writer.writeEndAttributes();
                {
                    writer.writeStartElement(STRING);
                    writer.writeValue(ref.getName());
                    writer.writeEndElement();

                    BLOBS.writeObjectId(writer, ref.getObjectId());
                }
                writer.writeEndElement();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        /**
         * @see org.geogit.api.TreeVisitor#visitSubTree(int, org.geogit.api.ObjectId)
         */
        public boolean visitSubTree(final int bucket, final ObjectId treeId) {
            try {
                writer.writeStartElement(TREE);
                {
                    writer.writeStartElement(BUCKET);
                    writer.writeValue(bucket);
                    writer.writeEndElement();

                    BLOBS.writeObjectId(writer, treeId);
                }
                writer.writeEndElement();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

    }

}