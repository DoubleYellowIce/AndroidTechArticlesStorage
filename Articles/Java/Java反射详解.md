# 一.引言
反射是在程序运行过程中动态加载类的机制。

简单来说，正常类加载机制如下图所示[1]。

![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/fb17a66b7cc74bb49c3a650af224f94d~tplv-k3u1fbpfcp-watermark.image?)

由于类加载机制不是本文的重点，上述内容只是简单描述了正常类加载机制的三个步骤，还有较多细节没有提及，感兴趣的读者可自行翻阅周志明作者的《深入理解Java虚拟机》。

而反射是反其道而行，首先获取代表某一个类的java.lang.Class对象(后文会简称为.Class对象)，然后进行反编译获取该类的信息。

反射的优点是使写出的代码更加灵活，缺点是破坏了类的封装性和会消耗一定的系统资源。

# 二.获取Class对象的方式
获取Class对象的方式有四种，下面简单介绍下。
```java
public class ReflectionDemo {
    
    public static void main(String[] args) throws ClassNotFoundException {
        // 方式一
        String demo=new String("Demo");
        Class class1=demo.getClass();
        //方式二
        Class class2=String.class;
        //方式三
        Class class3=Class.forName("java.lang.String");
        //方式四
        Class class4=demo.getClass().getClassLoader().loadClass("java.lang.String");
    }
}
```
这里值得一提的是，方式四在非静态方法中可以将demo换成this。
```java
class4=this.getClass().getClassLoader().loadClass("java.lang.String");
```
但不管怎么样，方式四也不是最推荐的获取class对象的方式，原因也很简单，步骤太繁琐了，这点从其代码比其它方式的长度长不少就可以知道。

此外，方式一也是不推荐的，反射的最终目的是获取类的信息，既然已经实例化了，又何必再获取.Class对象。

那么方式二和方式三的区别又在哪里呢？

新建一个ReflectDemo类。
```java
class ReflectDemo{
    static {
        System.out.println("ReflectDemo static");
    }

    {
        System.out.println("ReflectDemo nonstatic");
    }

    public ReflectDemo(){
        System.out.println("ReflectDemo constructor");
    }
}
```
执行下述代码。
```java
public class ReflectionDemo {

    public static void main(String[] args) throws ClassNotFoundException {
        Class clazz1=Class.forName("ReflectDemo");
    }

}
```
运行结果如下所示。
![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/d2321bf4b636491ba3fe448efbdb012c~tplv-k3u1fbpfcp-watermark.image?)
执行下述代码。
```java
public class ReflectionDemo {

    public static void main(String[] args) throws ClassNotFoundException {

            Class clazz2=ReflectDemo.class;
            
    }
}
```
![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/5eeb8c502dc642538dd556c165ee725a~tplv-k3u1fbpfcp-watermark.image?)
可以看到方式二和方式三都不会调用类的构造器方法，也就是说，不会进行类的初始化，同时，它们也不会调用普通代码块。

不同的是方式二会调用静态代码块，而方式三不会。
# 三.获取构造器
修改ReflectDemo类，新增一个接受String参数的私有构造方法。
```java
class ReflectDemo{
    ..
    private ReflectDemo(String name){

    }

    public ReflectDemo(){
        System.out.println("ReflectDemo constructor");
    }
    ..
}
```
获取java构造器方法有以下四种。
```java
public class ReflectionDemo {

    public static void main(String[] args) throws  NoSuchMethodException {
        Class clazz=ReflectDemo.class;
        //方式一，批量获取公有构造器方法
        Constructor[] constructors =clazz.getConstructors();
        System.out.println("clazz.getConstructors()'s results are");
        for (Constructor constructor:constructors){
            System.out.println(constructor);
        }
        //方式二，批量获取构造器方法，包括公有，私有构造器
        constructors=clazz.getDeclaredConstructors();
        System.out.println("clazz.getDeclaredConstructors()'s results are");
        for (Constructor constructor:constructors){
            System.out.println(constructor);
        }
        //方式三，获取指定构造器方法，但不包括私有构造器
        Constructor constructor;
        try {
            constructor=clazz.getConstructor(String.class);
            System.out.println("clazz.getConstructor(String.class)'s result is "+constructor);
        }catch (NoSuchMethodException exception){
            System.out.println("clazz.getConstructor(String.class)'s result is null");
        }
        //方式四，获取指定构造器方法，包括私有构造器
        constructor=clazz.getDeclaredConstructor(String.class);
        if (constructor==null){
            System.out.println("clazz.getDeclaredConstructor(String.class)'s result is null");
        }else {
            System.out.println("clazz.getDeclaredConstructor(String.class)'s result is "+constructor);
        }
    }
}
```
运行截图如下所示。

![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/c0cbfef203f84bf285598e11fc8246f4~tplv-k3u1fbpfcp-watermark.image?)

需要注意的是，Java中不管类成员也好，构造器也罢，只要没有显式使用权限修进行饰符，那么就是默认包级可见，无法被getConstructors()所获取。

以方式四获取到的私有构造器方法为例，进行实例化。

```java
public class ReflectionDemo {

    public static void main(String[] args) throws  NoSuchMethodException {
        ...
        //方式四，获取指定构造器方法，包括私有构造器
        constructor=clazz.getDeclaredConstructor(String.class);
        if (constructor==null){
            System.out.println("clazz.getDeclaredConstructor(String.class)'s result is null");
        }else {
            System.out.println("clazz.getDeclaredConstructor(String.class)'s result is "+constructor);
        }
        try {
            //由于该构造器为私有，需要先将其设置为accessible，才能进行实例化
            constructor.setAccessible(true);
            constructor.newInstance("ReflectionDemo");
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
```
运行结果如下所示。

![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/16ec1340eccf4c6fbad8af7f1a8e90d4~tplv-k3u1fbpfcp-watermark.image?)

除了用构造器方法实例化之外，还可以用class对象进行实例化，但由于该方法已经在Java9就被@Deprecated了，就不再介绍了。

# 四.获取方法
修改ReflectDemo类，新增三个空方法。
```java
public class ReflectionDemo {

    public static void main(String[] args) throws  NoSuchMethodException {
        Class clazz=ReflectDemo.class;
        //方式一，批量获取所有公有方法，包括父类的
        Method[] methods =clazz.getMethods();
        System.out.println("clazz.getMethods()'s results are ");
        int num=0;
        for (Method m:methods){
            System.out.println(m);
            if (++num==5){
            //由于隐式Object父类的方法过多，这里只打印5个方法
                break;
            }
        }
        //获取所有方法，包括私有方法，但不包括父类的方法
        methods=clazz.getDeclaredMethods();
        System.out.println();
        System.out.println();
        System.out.println("clazz.getDeclaredMethods()'s results are ");
        for (Method m:methods){
            System.out.println(m);
        }
    }
    
}
```
运行结果如下所示。

![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/968854a4b0914f8a9bd098cc331e8a36~tplv-k3u1fbpfcp-watermark.image?)
获取单个指定方法。
```java
public class ReflectionDemo {

    public static void main(String[] args) {
        Class clazz=ReflectDemo.class;
        try {
            //方式三，获取指定方法，但不包括私有方法和父类中的方法
            Method method=clazz.getMethod("method1");
            System.out.println("method1() is "+method);
            //方式四，获取指定方法，包括私有方法但不包括父类中的方法
            method=clazz.getDeclaredMethod("method3");
            method.setAccessible(true);
            method.invoke(clazz.getConstructor().newInstance());
            System.out.println("method3() is "+method);
        }catch (NoSuchMethodException noSuchMethodException){
            noSuchMethodException.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

    }
}
```
需要注意的是，在调用method3()之前由于该方法为私有方法，需要将其设置为accessible，才能进行调用，invoke()方法传入的是要调用的实例和所需参数，所需参数为可变参数，由于method3()为无参方法，只需要传入调用实例即可。
# 五.获取字段
修改ReflectDemo类，新增两个变量，一个公有和一个私有，并增加私有变量的getter()方法。
批量获取字段的方式。
```java 
public class ReflectionDemo {

    public static void main(String[] args) {
            Class clazz=ReflectDemo.class;
            //方式一，获取所有公有字段
            Field[] fields=clazz.getFields();
            System.out.println("clazz.getFields()'s results are");
            if (fields.length==0){
                System.out.println("fields's length is 0");
            }else {
                for (Field field:fields){
                    System.out.println(field);
                }
            }
        //方式二，获取所有字段，包括私有字段
        fields=clazz.getDeclaredFields();
            System.out.println("clazz.getDeclaredFields()'s results are");
            for (Field field:fields){
                System.out.println(field);
            }
    }
}
```
运行结果截图所示。

![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/776a0c909ef34ab8aec1d86a25f8d774~tplv-k3u1fbpfcp-watermark.image?)
获取单个指定字段的方式。
```java
public class ReflectionDemo {

    public static void main(String[] args) {
            Class clazz=ReflectDemo.class;
            try {
                //方式三，获取指定的公有字段
                //Field field1=clazz.getField("field1");
                //由于ReflectDemo类没有公有字段，所以上述代码会抛异常
                //方式四，获取指定的字段，包括私有字段
                Field field2=clazz.getDeclaredField("field2");
                System.out.println("filed1 is"+field2);
                field2.setAccessible(true);
                field2.set(clazz.getDeclaredConstructor().newInstance(),3);
            } catch (NoSuchFieldException | NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
    }
}
```
与获取方法的方式类似，这里不再啰嗦。
# 五.总结
不难发现，通过反射获取构造器，方法，字段的函数中，名字若无Declared的只能获取公有元素，有Declared的则可以获取全部元素，需要注意的是，获取方法的函数getMethods()会获取父类的公有方法。

此外，通过反射调用方法，或者设置字段，如果目标元素为私有，必须得先通过setAccessible()将其设置为可访问的，才能做进一步的操作。

 # 参考资料
 1.《深入理解Java虚拟机》，周志明
 2.[Java基础篇：反射机制详解](https://blog.csdn.net/a745233700/article/details/82893076)
 3.[面试:说说Java反射中获取Class对象三种方式的区别？](https://cloud.tencent.com/developer/article/1606540)