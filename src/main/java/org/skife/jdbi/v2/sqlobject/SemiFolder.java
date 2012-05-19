package org.skife.jdbi.v2.sqlobject;

public interface SemiFolder<A, T>
{
    public A fold(A a, T next);
}
