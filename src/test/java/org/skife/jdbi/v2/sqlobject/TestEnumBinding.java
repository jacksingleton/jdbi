/*
 * Copyright 2004 - 2012 Brian McCallister
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.skife.jdbi.v2.sqlobject;

import com.sun.xml.internal.bind.v2.runtime.output.SAXOutput;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestEnumBinding
{
    private DBI dbi;
    private Handle handle;

    @Before
    public void setUp() throws Exception
    {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test");
        dbi = new DBI(ds);
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
    public void testFoo() throws Exception
    {
        Dao d = handle.attach(Dao.class);
        d.insert(1, Name.Brian);
        d.insert(2, Name.David);

        int id = d.findByName(Name.David);
        assertThat(id, equalTo(2));
    }

    public static interface Dao
    {
        @SqlUpdate("insert into something (id, name) values (:id, :name)")
        public void insert(@Bind("id") int id, @Bind("name") Name name);

        @SqlQuery("select id from something where name = :name")
        public int findByName(@Bind("name") Name name);
    }

    public static enum Name
    {
        Brian, David
    }
}
