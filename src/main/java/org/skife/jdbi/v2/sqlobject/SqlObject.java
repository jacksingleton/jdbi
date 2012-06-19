package org.skife.jdbi.v2.sqlobject;

import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.ResolvedMethod;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.tapestry5.internal.plastic.StandardDelegate;
import org.apache.tapestry5.plastic.ClassInstantiator;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticClassTransformer;
import org.apache.tapestry5.plastic.PlasticManager;
import org.apache.tapestry5.plastic.PlasticMethod;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class SqlObject
{
    private static final PlasticManager pm = PlasticManager.withContextClassLoader().create();

    private static final TypeResolver                                  typeResolver  = new TypeResolver();
    private static final Map<Method, Handler>                          mixinHandlers = new HashMap<Method, Handler>();
    private static final ConcurrentMap<Class<?>, Map<Method, Handler>> handlersCache = new ConcurrentHashMap<Class<?>, Map<Method, Handler>>();
    private static final ConcurrentMap<Class<?>, Factory>              factories     = new ConcurrentHashMap<Class<?>, Factory>();

    static {
        mixinHandlers.putAll(TransactionalHelper.handlers());
        mixinHandlers.putAll(GetHandleHelper.handlers());
        mixinHandlers.putAll(TransmogrifierHelper.handlers());
    }

    @SuppressWarnings("unchecked")
    static <T> T buildSqlObject(final Class<T> sqlObjectType, final HandleDing handle)
    {
        final Class parent = sqlObjectType.isInterface() ? Object.class : sqlObjectType;

        ClassInstantiator<Object> pci = pm.createClass(parent, new StandardDelegate(new PlasticClassTransformer()
        {
            @Override
            public void transform(PlasticClass pc)
            {
                if (sqlObjectType.isInterface()) {
                    pc.introduceInterface(sqlObjectType);
                }

                final MemberResolver mr = new MemberResolver(typeResolver);
                final ResolvedType sql_object_type = typeResolver.resolve(sqlObjectType);
                final ResolvedTypeWithMembers d = mr.resolve(sql_object_type, null, null);

                for (ResolvedMethod method : d.getMemberMethods()) {
                    final Method raw_method = method.getRawMember();

                    if (raw_method.isAnnotationPresent(SqlQuery.class)) {
                        pc.introduceMethod(raw_method)
                          .addAdvice(new QueryAdvice(sqlObjectType, method, ResultReturnThing.forType(method)));
                    }
                    else if (raw_method.isAnnotationPresent(SqlUpdate.class)) {
                        pc.introduceMethod(raw_method)
                          .addAdvice(new UpdateAdvice(sqlObjectType, method));
                    }
                    else if (raw_method.isAnnotationPresent(SqlBatch.class)) {
                        pc.introduceMethod(raw_method)
                          .addAdvice(new BatchAdvice(sqlObjectType, method));
                    }
                    else if (raw_method.isAnnotationPresent(SqlCall.class)) {
                        pc.introduceMethod(raw_method)
                          .addAdvice(new CallAdvice(sqlObjectType, method));
                    }
                    else if (raw_method.isAnnotationPresent(CreateSqlObject.class)) {
                        pc.introduceMethod(raw_method)
                          .addAdvice(new CreateSqlObjectAdvice(raw_method.getReturnType()));
                    }
                    else if (method.getName().equals("close") && raw_method.getParameterTypes().length == 0) {
                        pc.introduceMethod(raw_method)
                          .addAdvice(new CloseAdvice());

                    }
                    else if (raw_method.isAnnotationPresent(Transaction.class)) {
                        pc.introduceMethod(raw_method)
                          .addAdvice(new TransactionAdvice(raw_method.getAnnotation(Transaction.class)));
                    }
                    else if (mixinHandlers.containsKey(raw_method)) {
                        //handlers.put(raw_method, mixinHandlers.get(raw_method));
                    }
                    else {
                        //handlers.put(raw_method, new PassThroughHandler(raw_method));
                    }

//                    for (PlasticMethod plasticMethod : pc.getMethodsWithAnnotation(Transaction.class)) {
//                        plasticMethod.addAdvice(new TransactionAdvice(plasticMethod.getAnnotation(Transaction.class)));
//                    }

                }
            }
        }));

        return (T) pci.with(HandleDing.class, handle).newInstance();
    }

    private static Map<Method, Handler> buildHandlersFor(Class<?> sqlObjectType)
    {
        if (handlersCache.containsKey(sqlObjectType)) {
            return handlersCache.get(sqlObjectType);
        }

        final MemberResolver mr = new MemberResolver(typeResolver);
        final ResolvedType sql_object_type = typeResolver.resolve(sqlObjectType);

        final ResolvedTypeWithMembers d = mr.resolve(sql_object_type, null, null);

        final Map<Method, Handler> handlers = new HashMap<Method, Handler>();
        for (final ResolvedMethod method : d.getMemberMethods()) {
            final Method raw_method = method.getRawMember();

            if (raw_method.isAnnotationPresent(SqlQuery.class)) {
                handlers.put(raw_method, new QueryHandler(sqlObjectType, method, ResultReturnThing.forType(method)));
            }
            else if (raw_method.isAnnotationPresent(SqlUpdate.class)) {
                handlers.put(raw_method, new UpdateHandler(sqlObjectType, method));
            }
            else if (raw_method.isAnnotationPresent(SqlBatch.class)) {
                handlers.put(raw_method, new BatchHandler(sqlObjectType, method));
            }
            else if (raw_method.isAnnotationPresent(SqlCall.class)) {
                handlers.put(raw_method, new CallHandler(sqlObjectType, method));
            }
            else if (raw_method.isAnnotationPresent(CreateSqlObject.class)) {
                handlers.put(raw_method, new CreateSqlObjectHandler(raw_method.getReturnType()));
            }
            else if (method.getName().equals("close") && method.getRawMember().getParameterTypes().length == 0) {
                handlers.put(raw_method, new CloseHandler());
            }
            else if (raw_method.isAnnotationPresent(Transaction.class)) {
                handlers.put(raw_method, new PassThroughTransactionHandler(raw_method, raw_method.getAnnotation(Transaction.class)));
            }
            else if (mixinHandlers.containsKey(raw_method)) {
                handlers.put(raw_method, mixinHandlers.get(raw_method));
            }
            else {
                handlers.put(raw_method, new PassThroughHandler(raw_method));
            }
        }

        // this is an implicit mixin, not an explicit one, so we need to *always* add it
        handlers.putAll(CloseInternal.Helper.handlers());

        handlers.putAll(EqualsHandler.handler());
        handlers.putAll(ToStringHandler.handler(sqlObjectType.getName()));
        handlers.putAll(HashCodeHandler.handler());

        return handlers;
    }


    private final Map<Method, Handler> handlers;
    private final HandleDing           ding;

    public SqlObject(Map<Method, Handler> handlers, HandleDing ding)
    {
        this.handlers = handlers;
        this.ding = ding;
    }

    public Object invoke(Object proxy, Method method, Object[] args, MethodProxy mp) throws Throwable
    {
        try {
            ding.retain("top-level");
            return handlers.get(method).invoke(ding, proxy, args, mp);
        }
        finally {
            ding.release("top-level");
        }
    }

    public static void close(Object sqlObject)
    {
        if (!(sqlObject instanceof CloseInternal)) {
            throw new IllegalArgumentException(sqlObject + " is not a sql object");
        }
        CloseInternal closer = (CloseInternal) sqlObject;
        closer.___jdbi_close___();
    }

    static String getSql(SqlCall q, Method m)
    {
        if (SqlQuery.DEFAULT_VALUE.equals(q.value())) {
            return m.getName();
        }
        else {
            return q.value();
        }
    }

    static String getSql(SqlQuery q, Method m)
    {
        if (SqlQuery.DEFAULT_VALUE.equals(q.value())) {
            return m.getName();
        }
        else {
            return q.value();
        }
    }

    static String getSql(SqlUpdate q, Method m)
    {
        if (SqlQuery.DEFAULT_VALUE.equals(q.value())) {
            return m.getName();
        }
        else {
            return q.value();
        }
    }

    static String getSql(SqlBatch q, Method m)
    {
        if (SqlQuery.DEFAULT_VALUE.equals(q.value())) {
            return m.getName();
        }
        else {
            return q.value();
        }
    }
}
