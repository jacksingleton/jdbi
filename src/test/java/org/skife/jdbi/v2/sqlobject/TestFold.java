package org.skife.jdbi.v2.sqlobject;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Something;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestFold
{
    private DBI    dbi;
    private Handle handle;

    @Before
    public void setUp() throws Exception
    {
        dbi = new DBI("jdbc:h2:mem:");
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
    public void testFoldToMap() throws Exception
    {
        Dao dao = handle.attach(Dao.class);
        dao.insert(1, "Ian");
        dao.insert(2, "Abby");

        Map<String, Something> expected = ImmutableMap.of("Ian", new Something(1, "Ian"),
                                                          "Abby", new Something(2, "Abby"));

        assertThat(dao.findAllByName(), equalTo(expected));
    }

    @RegisterMapper(SomethingMapper.class)
    public static interface Dao
    {
        @SqlUpdate("insert into something (id, name) values (:id, :name)")
        public void insert(@Bind("id") int id, @Bind("name") String name);


        @SqlQuery("select id, name from something")
        @Fold(NameToSomethingSpec.class)
        public Map<String, Something> findAllByName();
    }

    public static class NameToSomethingSpec implements Fold.FoldSpec<Map<String, Something>, Something>
    {
        @Override
        public Map<String, Something> initialValue()
        {
            return Maps.newHashMap();
        }

        @Override
        public SemiFolder<Map<String, Something>, Something> folder()
        {
            return new SemiFolder<Map<String, Something>, Something>()
            {
                @Override
                public Map<String, Something> fold(Map<String, Something> a, Something rs)
                {
                    a.put(rs.getName(), rs);
                    return a;
                }

            };
        }
    }


}
