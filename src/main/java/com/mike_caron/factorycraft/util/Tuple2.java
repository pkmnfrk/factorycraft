package com.mike_caron.factorycraft.util;

import com.google.common.collect.ImmutableMap;

import java.util.Objects;

public class Tuple2<A,B>
    implements ImmutableMap.Entry<A,B>
{
    public final A first;
    public final B second;

    public Tuple2(A first, B second)
    {
        this.first = first;
        this.second = second;
    }

    public A getFirst() { return first; }
    public A getLeft() { return first; }
    public A getKey() { return first; }

    public B getSecond() { return second;}
    public B getRight() { return second; }
    public B getValue() { return second; }

    @Override
    public B setValue(B value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof Tuple2))
            return false;
        Tuple2<?, ?> tuple2 = (Tuple2<?, ?>) o;
        return Objects.equals(first, tuple2.first) &&
            Objects.equals(second, tuple2.second);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(first, second);
    }

}
