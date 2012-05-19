package org.skife.jdbi.v2.sqlobject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Fold
{

    Class<? extends FoldSpec> value();

    public static interface FoldSpec<A, T>
    {
        public A initialValue();

        public YetAnotherFoldInterface<A, T> folder();
    }
}
