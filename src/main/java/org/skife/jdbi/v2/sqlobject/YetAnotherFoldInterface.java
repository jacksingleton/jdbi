package org.skife.jdbi.v2.sqlobject;

public interface YetAnotherFoldInterface<A, T>
{
    public A fold(A a, T next);
}
