package org.skife.jdbi.v2;

import org.skife.jdbi.derby.Tools;

import java.util.List;

/**
 * 
 */
public class TestNamedParams extends DBITestCase
{
    public void testInsert() throws Exception
    {
        Handle h = openHandle();
        SQLStatement insert = h.createStatement("insert into something (id, name) values (:id, :name)");
        insert.setInteger("id", 1);
        insert.setString("name", "Brian");
        int count = insert.execute();
        assertEquals(1, count);
    }

    public void testDemo() throws Exception
    {
        Handle h = DBI.open(Tools.getDataSource());
        h.createStatement("insert into something (id, name) values (:id, :name)")
                .setInteger("id", 1)
                .setString("name", "Brian")
                .execute();
        h.insert("insert into something (id, name) values (?, ?)", 2, "Eric");
        h.insert("insert into something (id, name) values (?, ?)", 3, "Erin");

        List<Something> r = h.createQuery("select id, name from something " +
                                          "where name like :name " +
                                          "order by id")
                .setString("name", "Eri%")
                .map(Something.class)
                .list();

        assertEquals(2, r.size());
        assertEquals(2, r.get(0).getId());
        assertEquals(3, r.get(1).getId());

        h.close();
    }
}
