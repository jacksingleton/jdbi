package org.skife.jdbi.v2.sqlobject;

import com.fasterxml.classmate.members.ResolvedMethod;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.MethodInvocation;
import org.skife.jdbi.v2.ConcreteStatementContext;
import org.skife.jdbi.v2.GeneratedKeys;
import org.skife.jdbi.v2.Update;
import org.skife.jdbi.v2.exceptions.UnableToCreateStatementException;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

class UpdateAdvice extends CustomizingStatementAdvice implements MethodAdvice
{
    private final String sql;
    private final Returner returner;

    public UpdateAdvice(Class<?> sqlObjectType, ResolvedMethod method) {
        super(sqlObjectType, method);
        this.sql = SqlObject.getSql(method.getRawMember().getAnnotation(SqlUpdate.class), method.getRawMember());
        if (method.getRawMember().isAnnotationPresent(GetGeneratedKeys.class)) {

            final ResultReturnThing magic = ResultReturnThing.forType(method);
            final GetGeneratedKeys ggk = method.getRawMember().getAnnotation(GetGeneratedKeys.class);
            final ResultSetMapper mapper;
            try {
                mapper = ggk.value().newInstance();
            }
            catch (Exception e) {
                throw new UnableToCreateStatementException("Unable to instantiate result set mapper for statement", e);
            }
            this.returner = new Returner()
            {
                public Object value(Update update, HandleDing baton)
                {
                    GeneratedKeys o = update.executeAndReturnGeneratedKeys(mapper);
                    return magic.result(o, baton);
                }
            };
        }
        else {
            this.returner = new Returner()
            {
                public Object value(Update update, HandleDing baton)
                {
                    return update.execute();
                }
            };
        }
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

        Update q = h.getHandle().createStatement(sql);
        populateSqlObjectData((ConcreteStatementContext)q.getContext());
        applyCustomizers(q, args);
        applyBinders(q, args);
        Object retval = this.returner.value(q, h);
        if (!invocation.getMethod().getReturnType().equals(void.class)) {
            invocation.setReturnValue(retval);
        }

    }

    private interface Returner
    {
        Object value(Update update, HandleDing baton);
    }

}
