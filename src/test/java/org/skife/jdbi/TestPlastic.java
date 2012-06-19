package org.skife.jdbi;

import org.apache.tapestry5.internal.plastic.StandardDelegate;
import org.apache.tapestry5.plastic.ClassInstantiator;
import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.MethodInvocation;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticClassTransformer;
import org.apache.tapestry5.plastic.PlasticManager;
import org.junit.Test;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public class TestPlastic
{
    private static PlasticManager pm = PlasticManager.withContextClassLoader().create();

    @Test
    public void testFoo() throws Exception
    {
        ClassInstantiator<Foo> fci = pm.createProxy(Foo.class, new StandardDelegate(new PlasticClassTransformer()
        {
            @Override
            public void transform(PlasticClass pc)
            {
                pc.introduceInterface(InternalInterface.class);

                System.out.println("--");
                for (Class<?> ifa : pc.getClass().getInterfaces()) {
                    System.out.println(ifa.getName());
                }
                System.out.println("--");
            }
        }));


        Foo foo = fci.newInstance();
        int huh = foo.secret();
        System.out.println(huh);
    }

    @Test
    public void testBar() throws Exception
    {
        ClassInstantiator<Object> ci = pm.createClass(Object.class, new StandardDelegate(new PlasticClassTransformer() {
            @Override
            public void transform(PlasticClass pc)
            {
                pc.introduceInterface(Foo.class);
                for (Method fm : Foo.class.getMethods()) {
                    pc.introduceMethod(fm).addAdvice(new MethodAdvice()
                    {
                        @Override
                        public void advise(MethodInvocation mi)
                        {
                            mi.setReturnValue(42);
                        }
                    });
                }
            }
        }));
        Object foo = ci.newInstance();
        assertThat(foo, instanceOf(Foo.class));
        Foo f = (Foo) foo;
        assertThat(f.secret(), equalTo(42));
    }

    @Test
    public void testClass() throws Exception
    {
        ClassInstantiator<Hamburger> ci = pm.createClass(Hamburger.class, new StandardDelegate(new PlasticClassTransformer() {
            @Override
            public void transform(PlasticClass pc)
            {
                for (Method method : Hamburger.class.getDeclaredMethods()) {
                    if (method.getName().equals("toppings")) {
                        System.out.println("!!!!");
                        pc.introduceMethod(method).addAdvice(new MethodAdvice()
                        {
                            @Override
                            public void advise(MethodInvocation invocation)
                            {
                                String topping = invocation.getInstanceContext().get(String.class);
                                invocation.setReturnValue(topping);
                            }
                        });
                    }
                }
            }
        }));

        Hamburger foo = ci.with(String.class, "cheese").newInstance();
        assertThat(foo.toppings(), equalTo("cheese"));
    }

    @Test
    public void testSql() throws Exception
    {
        ClassInstantiator<Object> ci = pm.createClass(Object.class, new StandardDelegate(new PlasticClassTransformer()
        {
            @Override
            public void transform(PlasticClass pc)
            {
                pc.introduceInterface(SqlThing.class);

            }
        }));
        ci.newInstance();
    }

    public static class Hamburger
    {
         public String toppings() {
            return "bacon!";
        }
    }

    public static interface SqlThing
    {
        @SqlQuery("select name from something where id = :id")
        public String findNameFor(@Bind("id") int id);
    }

    public static interface Foo
    {
        public int secret();
    }

    public static interface InternalInterface
    {
        public int waffle();
    }
}
