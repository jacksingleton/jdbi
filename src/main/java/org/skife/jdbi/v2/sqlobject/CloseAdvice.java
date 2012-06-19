package org.skife.jdbi.v2.sqlobject;

import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.MethodInvocation;

class CloseAdvice implements MethodAdvice
{
    @Override
    public void advise(MethodInvocation invocation)
    {
        invocation.getInstanceContext().get(HandleDing.class).getHandle().close();
    }
}
