package org.skife.jdbi.v2.sqlobject;

import org.h2.jdbcx.JdbcDataSource;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.sql.Connection;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class TestNPEOnLongResult
{
    private Handle handle;

    @Before
    public void setUp() throws Exception
    {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test");
        DBI dbi = new DBI(ds);
        handle = dbi.open();

        handle.execute("create table something (id int primary key, name varchar(100))");
    }

    @After
    public void tearDown() throws Exception
    {
        handle.execute("drop table something");
        handle.close();
    }

    @Test
    public void testAgainstH2() throws Exception
    {
        Dao dao = handle.attach(Dao.class);
        assertThat(dao.findByIdName("Brian"), nullValue());
    }

    @Test
    public void testAgainstPostgres() throws Exception
    {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setServerName("localhost");
        ds.setDatabaseName("jdbi-testing");

        DBI dbi = new DBI(ds);
        Handle h = dbi.open();
        h.execute("create table something (id int primary key, name varchar(100))");

        Dao dao = h.attach(Dao.class);
        assertThat(dao.findByIdName("Brian"), nullValue());

        h.execute("drop table something");
    }


    public static interface Dao
    {
        @SqlQuery("select id from something where name = :name")
        public Long findByIdName(@Bind("name") String name);
    }

}
