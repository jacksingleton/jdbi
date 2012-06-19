package org.skife.jdbi.v2.sqlobject;

import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.MethodInvocation;

class CreateSqlObjectAdvice implements MethodAdvice
{
    private final Class<?> sqlObjectType;

    public CreateSqlObjectAdvice(Class<?> sqlObjectType)
    {
        this.sqlObjectType = sqlObjectType;
    }

    @Override
    public void advise(MethodInvocation invocation)
    {
        HandleDing ding = invocation.getInstanceContext().get(HandleDing.class);
        invocation.setReturnValue(SqlObject.buildSqlObject(sqlObjectType, ding));
    }
}
