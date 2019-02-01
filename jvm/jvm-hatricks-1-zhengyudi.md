**JVM-学习郑宇迪教程-有感**  
---
2019年2月1日10:42:39

**1**
JIT编译器和AOT编译器(JVM中的)

JIT编译器,可以在解释器执行的过程中,使用C1和C2解释器先解释执行,使用方法的回边统计计数法,统计当前方法的执行次数,第一次出发编译
执行的次数是500次,使用C2执行的次数是15000次;
主要是JIT有一些正在运行时的一些运行信息,可以将代码优化的更符合当前服务器CPU的执行理念的一种代码形式

其实Java字节码还是设计的是一种平台无关性的理念,我不管你是linux还是windows,只要安装jre我就可以对于当前代码进行解释执行,或者中
前置编译也行,这样的话其实启动速度比较慢,或者说无法让获得代码的一些运行时信息,JIT话,会得到当前代码的一些运行时信息,优化成当前
最符合当前服务器的代码

**2**
Java中的一些基础类型的概念

boolean类型会被映射成0和1  if(true)这种代码,使用ifeq字节码指令来判断当前入参!=0,如果不等于0,那么就会执行,还有另外一种,
if(x == true)
这种情况下,就不会在执行

如果手动修改字节码,将入参改为2或者3,jvm就会进行掩码,然后2就会入参为0,3就是1,取低位
```
class JVM{
    //下面这两种其实是生成不同的字节码比较
    if(x == true)
    if(true) 
}
```  
在实际方法执行的过程中,使用基本数据类型比对象更快一点,占用内存更少,虚拟机对于32位和64位虚拟机的表示方法来看,是实际是2倍的字节数
来标示的,为了性能,忽略了空间.

栈:在方法执行的过程中,每个方法都是一个栈帧,对应着java方法栈的压栈操作,每个线程都自己的方法栈,程序计数器标示当前栈帧的活动行号,
   还有本地方法栈
    
堆:存储对象的地方

方法区:在jdk8之后是元数据区,其实这里面存储着类加载器+类名的类或者接口的静态数据结构,在实际new一个对象的时候,只会查到当前类的字节码,
拿到非final,static修饰的实例数据,在加载的过程中,除了基本数据类型和字符串会生成类似于constantvalue的值,其他的只是在方法区的静态
数据结构中的其实相当于只是一个引用来引用堆内的对象,而且只有constantvalue会在类链接的准备阶段,进行值得初始化,其他的都是在准备阶段,
赋予当前数据类型的默认值,会在最后类进行初始化的时候进行client方法类的赋值,在类还没有被初始化的时候这个时候类是不能够被使用的
(最后附加:当前就算是给对象static.final修饰的对象,在jvm进行执行的时候也还是会放到client方法中进行执行)

```
public class SingletonInstance {
    public static SingletonInstance singletonInstance;

    public static SingletonInstance getInstance() {
        return LazySingletonInstance.SINGLETON_INSTANCE;
    }

    private static class LazySingletonInstance {
        static final SingletonInstance SINGLETON_INSTANCE = new SingletonInstance();
    }
}
```  

类加载过程:
java会首先将bootstrap类加载器,也就是根类(c写的类记载器)加载到方法区(lib/rt.jar),然后是扩展类加载器extensionclassloader(lib/ext.jar)
目录下的文件,然后我自己平时写一些类其实都是使用appclassloader进行加载的,也就是我们应用类加载器,当前了我们也可以自定义类加载器,来使得
同一个class文件加载出不同的效果

加载-链接(验证-准备-解析)-初始化-使用-卸载

在加载阶段其实已经将字节码加载到方法去了,只不过当前可能会被标记为不可用

在准备阶段:为当前的constantvalue赋值,并且准备当前类的虚方发表(在方法名和入参一样的方法,生成的方法签名其实是一样的)在查找具体使用方法的
时候,会根据当前对象的对象头中kclass引用和当前的类加载去方法区找到当前类的静态数据结构,找到那个方法的内存地址值,然后进行执行

在初始化阶段:会为其他的对象类型的static或者staticfinal类型变量进行赋值

解析:会将当前类引用的,其实说白了也就是初始化阶段需要使用的一些其他类的引用,也进行那一套加载进来,也就是把一些filed_ref和method_ref
的内存地址拿到需要调用的地方

类的初始化和类的实例化:

初始化会触发jvm的加载过程,而实例化其实就只是jvm为对象设置对象头以及当前对象的实例数据的过程,这个过程是非常快的