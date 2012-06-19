package org.skife.jdbi.v2.sqlobject;

import com.fasterxml.classmate.members.ResolvedMethod;
import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.MethodInvocation;
import org.skife.jdbi.v2.ConcreteStatementContext;
import org.skife.jdbi.v2.Query;

import java.lang.reflect.Method;

public class QueryAdvice extends CustomizingStatementAdvice implements MethodAdvice
{
    private final ResultReturnThing magic;
    private final String sql;
    private final ResolvedMethod resolvedMethod;

    public QueryAdvice(Class<?> sqlObjectType, ResolvedMethod method, ResultReturnThing magic)
    {
        super(sqlObjectType, method);
        this.magic = magic;
        this.resolvedMethod = method;
        this.sql = SqlObject.getSql(method.getRawMember().getAnnotation(SqlQuery.class), method.getRawMember());
    }

    @Override
    public void advise(MethodInvocation invocation)
    {
        int arg_count = invocation.getMethod().getParameterTypes().length;
        Object[] args = new Object[arg_count];
        for (int i = 0; i < arg_count; i++) {
            args[i] = invocation.getParameter(i);
        }

        HandleDing h = invocation.getInstanceContext().get(HandleDing.class);
        Query q = h.getHandle().createQuery(sql);
        populateSqlObjectData((ConcreteStatementContext) q.getContext());
        applyCustomizers(q, args);
        applyBinders(q, args);

        Object result = magic.map(resolvedMethod, q, h);
        invocation.setReturnValue(result);

    }
}
