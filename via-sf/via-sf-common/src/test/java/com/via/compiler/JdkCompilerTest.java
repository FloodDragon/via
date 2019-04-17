package com.via.compiler;

import com.via.common.compiler.support.JdkCompiler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

public class JdkCompilerTest extends JavaCodeTest {

    @Test
    public void test_compileJavaClass() throws Exception {
        JdkCompiler compiler = new JdkCompiler();
        Class<?> clazz = compiler.compile(getSimpleCode(), JdkCompiler.class.getClassLoader());
        Object instance = clazz.newInstance();
        Method sayHello = instance.getClass().getMethod("sayHello");
        Assertions.assertEquals("Hello world!", sayHello.invoke(instance));
    }

    @Test
    public void test_compileJavaClass0() throws Exception {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            JdkCompiler compiler = new JdkCompiler();
            Class<?> clazz = compiler.compile(getSimpleCodeWithoutPackage(), JdkCompiler.class.getClassLoader());
            Object instance = clazz.newInstance();
            Method sayHello = instance.getClass().getMethod("sayHello");
            Assertions.assertEquals("Hello world!", sayHello.invoke(instance));
        });
    }

    @Test
    public void test_compileJavaClass1() throws Exception {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            JdkCompiler compiler = new JdkCompiler();
            Class<?> clazz = compiler.compile(getSimpleCodeWithSyntax(), JdkCompiler.class.getClassLoader());
            Object instance = clazz.newInstance();
            Method sayHello = instance.getClass().getMethod("sayHello");
            Assertions.assertEquals("Hello world!", sayHello.invoke(instance));
        });
    }
}
