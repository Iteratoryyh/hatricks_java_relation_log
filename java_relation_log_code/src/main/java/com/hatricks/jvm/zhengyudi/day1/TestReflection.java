package com.hatricks.jvm.zhengyudi.day1;

import java.lang.reflect.Method;

public class TestReflection {
    public static void target(int i) {
        new Exception("#" + i).printStackTrace();
    }

    public static void main(String[] args) throws Exception {
        Class<?> testReflection = Class.forName("com.hatricks.jvm.zhengyudi.day1.TestReflection");
        Method target = testReflection.getMethod("target", int.class);
        for (int i = 0; i < 20; i++) {
            target.invoke(null, i);
        }
    }
}
