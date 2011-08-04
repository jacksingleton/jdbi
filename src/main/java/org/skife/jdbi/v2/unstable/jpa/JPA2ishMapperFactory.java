package org.skife.jdbi.v2.unstable.jpa;

import org.skife.jdbi.v2.ResultSetMapperFactory;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import javax.persistence.Entity;

public class JPA2ishMapperFactory implements ResultSetMapperFactory
{
    public boolean accepts(Class type, StatementContext ctx)
    {
        return type.isAnnotationPresent(Entity.class);
    }

    public ResultSetMapper mapperFor(Class type, StatementContext ctx)
    {



        return null;
    }


}
