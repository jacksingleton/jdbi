package org.skife.jdbi.v2.unstable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

public class TestJPA2ishMapperFactory
{

    @Entity
    @Table(name = "something")
    public static class Thing
    {
        private int id;
        private String name;

        @Id
        public int getId() {
            return this.id;
        }

        public void setId(int id)
        {
            this.id = id;
        }


        public String getName()
        {
            return this.name;
        }

        public void setName(String name)
        {
            this.name = name;
        }
    }
}
