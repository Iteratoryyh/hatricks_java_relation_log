JVM-学习郑宇迪教程-有感  
---
2019年2月1日22:06:15

### invokedynamic(methodhandler)

**方法句柄:**
方法句柄是由所指向方法的参数类型以及返回值类型组成的,是用来确定当前方法句柄是否适配的唯一关键
,方法句柄相对于反射来说,省去每次做参数校验的麻烦,但是由于是间接调用还是会产生无法进行方法内联的这种情况
其实invokedynamic能够使java这种静态类型的语言支持动态的特性.
- 先获得callsite对象,使用当前的方法签名(对于jvm来说,返回值+参数列表+方法名称确定唯一的方法)
- 调用LambdaMetafactory.metafactory方法使用asm字节码技术生成字节码,然后动态链接到callsite上
- 完成动态调用
```
 当虚拟机遇到invokedynamic指令-->bootstrapmethod-->callsite.makesite-->在连接过程中
 lambdametafactory.metafactory方法生成内部类,通过unsafe的方式加载到方法区里面-->然后通过
 classsite持有的methodhandler链接到生成内部类的获取方法.以后在调用的时候就不需要在进行加载了
```  

**运行期:**
也就是我们需要动态确定,运行时需要调用的方法,也就是虚方法表,其实我们在重写了父类的方法之后,使用父类的引用在调用子类对象的时候,
在具体引用的时候,如果当前是(invokestatic,invokespecial)的时候,当前的引用代表就是需要标示方法的具体内存地址,在当前是invokevirtual
和invokeinterface的时候,虚拟机需要在执行的过程中,根据调用者的动态类型,来确定具体的目标方法;

**其实invokevirtual的时候,是根据实际类型是一个方法表的索引,然后根据实际类型对象头中的kclass找到实际类型的地址:classloader+包名+类名的内存地址,拿到实际调用的方法,入栈**
**然后invokeinterface的时候,是拿到当前实际类型,然后到方法表中去搜索当前方法名称以及参数名称还有返回值匹配的方法,进行调用入栈**

在进行JIT即时编译的时候,会有方法内联和内联缓存两种内部调优方式;JavaJIT会采用单态内联缓存来进行优化,就是说第一次访问会将实际类型缓存起来,
然后再下一次访问的时候,动态类型和缓存类型不匹配的情况下,就会回退到查找方法表(即使是这样子,其实影响也并不是很大,只是多了2次内存地址访问而已),
跟使用设计模式良好的扩展性比起来,其实无伤大雅.

## JVM的异常处理机制:

- 抛异常的时候,我们要从当前栈帧中,拿到方法的执行信息,以及方法报错的字节码行号,还有源代码的行号,以及文件名称,所以在构建异常栈帧的时候,
是一个比较费时的操作.

- 在catch操作中相当于使用了责任链模式,每一次自己处理不了的时候,都会传递给下一个,finally中的代码相当于在所有的正常出口以及异常出口都
进行了类似于代码复制的操作,来保证finally中的代码能够进行正常的执行


- JVM的tools里面自带的工具 java -p -v 会输出当前的成员信息,如果直接使用javap -verbose 是不会输出成员变量的相关信息的

## JVM的反射机制

- 其实调用反射机制的开销是比较高的,主要是因为反射机制的方法内联,以及逃逸分析,导致的栈帧优化策略不同,如果是可逃逸的对象,那么就只能在栈上进行分配,
对于不可逃逸的对象的,jvm在具体实现上面,会考虑进行栈上分配内存,我个人认为也就是在cpu的寄存器上面进行分配,如果不妥之处,望看到的大神,能够指点指点..

- 这里先引用郑大的一段代码:在具体反射的时候,我们会通过MethodAccessor对象进行委派实现,因为如果我们的反射只调用一次的话,用本地方法实现其实是最快的,
但是当我们如果需要进行多次调用,那么选择动态实现其实是更快一点的,不然就会浪费性能了

* 方法内联:是指编译器在编译一个方法的时候,将某个方法调用的目标方法也纳入编译的范围内了,并且用它的返回值带起原方法调用的这么个过程
(这里由于水平有限,就不贴源代码了..因为自己也看不太懂,大致意思就是15次之后会基于你调用的类,生成新的字节码文件后面在进行调用的时候就会快不少,但是由于是invokevirtual调用,其实也不会快多少)
---
```
import java.lang.reflect.Method;
 
public class Test {
  public static void target(int i) {
    // 空方法
  }
 
  public static void main(String[] args) throws Exception {
    Class<?> klass = Class.forName("Test");
    Method method = klass.getMethod("target", int.class);
    method.setAccessible(true);  // 关闭权限检查
    polluteProfile();
 
    long current = System.currentTimeMillis();
    for (int i = 1; i <= 2_000_000_000; i++) {
      if (i % 100_000_000 == 0) {
        long temp = System.currentTimeMillis();
        System.out.println(temp - current);
        current = temp;
      }
 
      method.invoke(null, 128);
    }
  }
 
  public static void polluteProfile() throws Exception {
    Method method1 = Test.class.getMethod("target", int.class);
    Method method2 = Test.class.getMethod("target", int.class);
    for (int i = 0; i < 2000; i++) {
      method1.invoke(null, 0);
      method2.invoke(null, 0);
    }
  }
  public static void target1(int i) { }
  public static void target2(int i) { }
}
```  
