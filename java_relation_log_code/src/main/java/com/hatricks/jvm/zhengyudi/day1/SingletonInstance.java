package com.hatricks.jvm.zhengyudi.day1;

/**
 * @program: jvmlog
 * @description: 任何时候把单例单独拿出来说, 就像设计模式, 任何时候将设计模式单独拿出来说都是没有任何意思
 * 应该和前台调用,或者说应该跟业务综合起来,来进行讲解
 * @author: hatrick
 * @create: 2019-02-01 10:44
 */
public class SingletonInstance {
    public static SingletonInstance singletonInstance;

    public static SingletonInstance getInstance() {
        return LazySingletonInstance.SINGLETON_INSTANCE;
    }

    private static class LazySingletonInstance {
        static final SingletonInstance SINGLETON_INSTANCE = new SingletonInstance();
    }
}
