package org.geogit.api;

public interface TreeVisitor {

    boolean visitEntry(Ref ref);

    boolean visitSubTree(int bucket, ObjectId treeId);

}
