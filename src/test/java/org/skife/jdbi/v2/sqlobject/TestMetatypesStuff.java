package org.skife.jdbi.v2.sqlobject;

import com.google.common.collect.Sets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Something;
import org.skife.jdbi.v2.sqlobject.customizers.Meta;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.ExternalizedSqlViaStringTemplate3;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestMetatypesStuff
{

    private DBI    dbi;
    private Handle h;

    @Before
    public void setUp() throws Exception
    {
        this.dbi = new DBI("jdbc:h2:mem:" + UUID.randomUUID().toString());
        this.h = dbi.open();
        h.execute("create table something (id int primary key, name varchar)");
    }

    @After
    public void tearDown() throws Exception
    {
        h.close();
    }

    @Test
    public void testAnnotationFound() throws Exception
    {
        MetaAnnotatedClass mac = new MetaAnnotatedClass(Waffle.class);
        assertThat(mac.isAnnotationPresent(RegisterMapper.class), equalTo(true));
    }

    @Test
    public void testStuffWorks() throws Exception
    {
        Waffle waffle = dbi.onDemand(Waffle.class);

        waffle.save(new Something(1, "Brian"), new Something(2, "Henning"));

        assertThat(waffle.findAll(), equalTo((Set<Something>) Sets.newHashSet(new Something(1, "Brian"),
                                                                                   new Something(2, "Henning"))));
    }

    @Dao
    public static interface Waffle
    {
        @SqlBatch
        void save(@BindBean Something... things);

        @SqlQuery
        Set<Something> findAll();
    }

    @Meta
    @RegisterMapper(SomethingMapper.class)
    @ExternalizedSqlViaStringTemplate3
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Dao {
 }

}
