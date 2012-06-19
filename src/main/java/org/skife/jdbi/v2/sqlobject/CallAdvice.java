package org.skife.jdbi.v2.sqlobject;

import com.fasterxml.classmate.members.ResolvedMethod;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.MethodInvocation;
import org.skife.jdbi.v2.Call;
import org.skife.jdbi.v2.ConcreteStatementContext;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.OutParameters;

class CallAdvice extends CustomizingStatementAdvice implements MethodAdvice
{
    private final String sql;
    private final boolean returnOutParams;

    CallAdvice(Class<?> sqlObjectType, ResolvedMethod method)
    {
        super(sqlObjectType, method);

        if (null != method.getReturnType() ) {
            if (method.getReturnType().isInstanceOf(OutParameters.class)){
                returnOutParams = true;
            }
            else {
                throw new IllegalArgumentException("@SqlCall methods may only return null or OutParameters at present");
            }
        }
        else {
            returnOutParams = false;
        }

        this.sql = SqlObject.getSql(method.getRawMember().getAnnotation(SqlCall.class), method.getRawMember());
    }

    @Override
    public void advise(MethodInvocation invocation)
    {
        int arg_count = invocation.getMethod().getParameterTypes().length;
        Object[] args = new Object[arg_count];
        for (int i = 0; i < arg_count; i++) {
            args[i] = invocation.getParameter(i);
        }

        HandleDing ding = invocation.getInstanceContext().get(HandleDing.class);

        Handle h = ding.getHandle();
        Call call = h.createCall(sql);
        populateSqlObjectData((ConcreteStatementContext)call.getContext());
        applyCustomizers(call, args);
        applyBinders(call, args);

        OutParameters ou = call.invoke();

        if (returnOutParams) {
            invocation.setReturnValue(ou);
        }
        else {
            invocation.setReturnValue(null);
        }

    }
}
