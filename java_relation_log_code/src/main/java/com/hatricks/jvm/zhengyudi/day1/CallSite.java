package com.hatricks.jvm.zhengyudi.day1;

public class CallSite implements ICallSite {
    @Override
    public void test(String string) {
        System.out.println("12");
    }

    public static void main(String[] args) {
        ICallSite callSite = System.out::println;
        ICallSite callSite1 = System.out::println;
        callSite.test("ss");
        callSite1.test("sss");
    }
}
