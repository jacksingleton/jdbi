package org.skife.jdbi.v2.sqlobject;

import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.MethodInvocation;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionCallback;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.TransactionStatus;

class TransactionAdvice implements MethodAdvice
{
    private final TransactionIsolationLevel isolation;

    public TransactionAdvice(Transaction tx)
    {

        this.isolation = tx.value();
    }

    @Override
    public void advise(final MethodInvocation invocation)
    {
        HandleDing ding = invocation.getInstanceContext().get(HandleDing.class);

        Object retval;
        ding.retain("pass-through-transaction");
        try {
            Handle h = ding.getHandle();
            if (isolation == TransactionIsolationLevel.INVALID_LEVEL) {
                retval = h.inTransaction(new TransactionCallback<Object>()
                {
                    @Override
                    public Object inTransaction(Handle conn, TransactionStatus status) throws Exception
                    {
                        invocation.proceed();
                        if (invocation.didThrowCheckedException()) {
                            status.setRollbackOnly();
                        }
                        return null;
                    }
                });
            }
            else {
                retval = h.inTransaction(isolation, new TransactionCallback<Object>()
                {
                    @Override
                    public Object inTransaction(Handle conn, TransactionStatus status) throws Exception
                    {
                        invocation.proceed();
                        if (invocation.didThrowCheckedException()) {
                            status.setRollbackOnly();
                        }
                        return null;
                    }
                });

            }
        }
        finally {
            ding.release("pass-through-transaction");
        }
        if (!invocation.getMethod().getReturnType().equals(void.class)) {
            invocation.setReturnValue(retval);
        }

    }
}
