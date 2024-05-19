package com.github.igormich.autumn;

import org.reflections.Reflections;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.function.Predicate;

import static org.reflections.scanners.Scanners.SubTypes;

public class Autumn {

    //Actually must be user project root
    static Reflections reflections = new Reflections("com.github.igormich.autumn");

    private static class ProxyHandler implements InvocationHandler {

        private final Object realObject;

        public ProxyHandler(Object realObject) {
            this.realObject = realObject;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            var realMethod = realObject.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
            if (realMethod.isAnnotationPresent(PrintToConsole.class)) {
                System.out.println("method " + method.getName() + " has been called with " + Arrays.toString(args));
                var result = method.invoke(realObject, args);
                System.out.println("result is " + result);
                return result;
            } else {
                return method.invoke(realObject, args);
            }
        }
    }



    @SuppressWarnings("unchecked")
    private static <T> T makeProxy(Object fieldValue, Class<T> type) {
        return (T) Proxy.newProxyInstance(
                Autumn.class.getClassLoader(),
                new Class[]{type},
                new ProxyHandler(fieldValue));
    }

    @SuppressWarnings("unchecked")
    private static <I, C extends I> Class<C> findClazz(Class<I> type) {
        if (type.isInterface() && Modifier.isAbstract(type.getModifiers())) {
            var implementations = reflections.get(SubTypes.of(type).asClass())
                    .stream().filter(Predicate.not(t -> t.isInterface() && Modifier.isAbstract(t.getModifiers())))
                    .toList();
            if (implementations.size() == 1) {
                return (Class<C>) implementations.getFirst();
            } else {
                throw new IllegalStateException();//TODO: add message
            }
        }
        return (Class<C>) type;
    }

    private static Object prepare(Class<?> clazz) throws Exception {
        var constructor = clazz.getDeclaredConstructor();
        var obj = constructor.newInstance();
        var fields = clazz.getDeclaredFields();
        for (var field : fields) {
            if (field.isAnnotationPresent(AutoWired.class)) {
                field.setAccessible(true);
                var fieldClazz = findClazz(field.getType());
                var fieldValue = prepare(fieldClazz);
                var fieldProxy = makeProxy(fieldValue, field.getType());
                field.set(obj, fieldProxy);
            }
        }
        return obj;
    }


    public static void start(Class<? extends Application> clazz) throws Exception {
        var obj = (Application) prepare(clazz);
        obj.run();
    }
}
