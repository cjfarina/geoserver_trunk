package org.geogit.api;

import java.math.BigInteger;
import java.util.Iterator;

import com.google.common.base.Predicate;
import com.sun.istack.internal.NotNull;

public abstract class RevTree extends RevObject {

    public RevTree(ObjectId id) {
        super(id);
    }

    @Override
    public final TYPE getType() {
        return TYPE.TREE;
    }

    public abstract void put(@NotNull final Ref ref);

    public abstract Ref get(@NotNull final String key);

    public abstract void remove(@NotNull final String key);

    public abstract void accept(@NotNull TreeVisitor visitor);

    public abstract BigInteger size();

    public abstract Iterator<Ref> iterator(Predicate<Ref> filter);

    public abstract void normalize();

    public abstract boolean isNormalized();
}