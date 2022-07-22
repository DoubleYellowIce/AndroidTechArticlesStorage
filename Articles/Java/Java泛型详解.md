# 一.引言
Java泛型出现的主要原因之一就是为了更好地实现集合类，下面我们实现一个简单的字符串容器StringHolder。
```java
public class StringHolder {

    private final String value;

    public StringHolder(String value) {
       this.value=value;
    }

    public String get(){
        return value;
    }

}
```
StringHolder的代码非常简单，只实现了一个存取字符串的功能，但有一个很明显的缺陷，就是该容器只能存取字符串，代码复用性太差了，为了让其支持更多的元素，我们可以将value的类型改为Object，同时类名改为ObjectHolder。
```java
public class ObjectHolder {

    private final Object value;

    public ObjectHolder(Object value) {
       this.value=value;
    }

    public Object get(){
        return value;
    }

    public static void main(String[] args) {
        ObjectHolder stringHolder=new ObjectHolder("value");
        String value=(String) stringHolder.get();
    }

}
```
从ObjectHolder取出元素时，必须经过向下转型这一步骤，这存在两个问题，一是向下转型是不安全的，二是若每次取元素都需要向下转型极为不方便，而泛型很好地解决了这种问题。
```java
public class ObjectHolder<T> {

    private final T t;

    public ObjectHolder(T t) {
       this.t=t;
    }

    public T get(){
        return t;
    }

    public static void main(String[] args) {
        //Java7前的做法
        ObjectHolder<String> stringHolder=new ObjectHolder<String>("value");
        //Java7后的做法
        ObjectHolder<String> stringHolder=new ObjectHolder<>("value");
        
        String value=stringHolder.get();
    }

}
```
可以看到，使用泛型后从stringHolder取出元素时再也不需要向下转型这一步了。

上述代码中的T是一个类型参数，需要用尖括号<>括住，其作用是充当一个占位符，等到使用时再用具体类型去替代这个类型参数。

在Java5，也就是泛型刚出现时，实例化时右边尖括号内的类型参数是不允许省略的，而到了Java7，实例化时右边尖括号内的类型参数是允许省略的，该新增的语法特性被称为钻石语法。

# 二.类型参数
上文提到过T是类型参数，那么什么是类型参数呢，类型参数就是把类型当作一个参数来使用，举一个例子。
```java
ObjectHolder<String> stringHolder=new ObjectHolder<String>("value");
```
上述代码中就是把String类当作一个参数传入ObjectHolder，从而来达到创建一个专门存储String类的容器的目的。

类型参数又分为类型形参和类型实参。

## 2.1类型形参
T是类型形参，是英文单词Type的缩写，Java对类型形参并没有严格限制，只要求类型形参是字母，对大小写和个数并没有限制，不过为了代码的可阅读性，类型形参一般都是单个大写字母，且该字母一般为有意义单词的缩写，比如说常见的类型形参中，V为Value的缩写，N为Node的缩写。

个人观点认为，Java没有对类型形参的命名作过多的限制是有原因的，前面提到过，类型参数主要的作用是充当一个占位符，对其的命名作过多的限制反而会让其失去了意义。

这里做一个可能不那么恰当的比喻来陈述我的观点，高中时期在饭堂吃饭，吃饭前好不容易找到了个位置，这时候同学们一般会把随身携带的物品，或书，或雨伞，放置在桌面或椅子上来占位，这时如果对用来占位的物品作过多限制，比如说不能是书，水杯或饭盒，在这种情况下同学往往只能是本人坐在位置上来占位了，换句话说就是，只有同学才能起到了占位的作用，而这就失去占位的意义了，占位本身就是为了在同学去打饭的过程仍能占有位置，如今却只能人坐在那才能占位，Java若对占位符的命名作太多的限制也是一样的道理。

## 2.2类型实参
类型实参比较好理解了，任意有明确定义的类都是类型实参，比如说String类就是类型实参，这里就不再赘述。
## 2.3通配符
介绍完类型形参和类型实参，我们趁热打铁再来介绍一下通配符，先来看看通配符的简单使用。
```java
class Demo{
    public void demo1(ObjectHolder<?> objectHolder){
        System.out.println("demo1() is executed,and objectHolder's T is "+objectHolder.get());
    }

}
```
上述代码中，我新建了一个Demo类，并实现了demo1()方法，该方法接受一个ObjectHolder<?>参数。尖括号<>括住的是就是通配符，它的符号是?，该参数的意思是只要是ObjectHolder类就行，不管ObjectHolder存储的是什么元素。

值得一提的是通配符?是类型实参(这也是我把通配符放在这里介绍的原因),换句话说就是，通配符和String，Object等有明确定义的类为同一个类型。

```java
public static void main(String[] args) {
    ObjectHolder<String> stringHolder=new ObjectHolder<>("String");
    ObjectHolder<Integer> integerHolder=new ObjectHolder<>(0);
    ObjectHolder<Boolean> booleanHolder=new ObjectHolder<>(true);
    Demo demo=new Demo();
    demo.demo1(stringHolder);
    demo.demo1(integerHolder);
    demo.demo1(booleanHolder);
}
```
运行上述代码，结果截图如下所示。

![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/554aa720fff042c9a5add8efa2351fb1~tplv-k3u1fbpfcp-watermark.image?)

可以看到，代码并没有进行报错，demo1()也成功获取到每个Object存储的值了。

## 2.4 上下边界
既然介绍完通配符了，就顺便把上下边界也一并介绍了吧，毕竟上下边界里通配符是必不可少的元素。

先来两个小demo演示如何对ObjectHolder里的元素进行进一步限制。

```java
class A{
    private String name;

    public A(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
class B extends A{


    public B(String name) {
        super(name);
    }
   
}
class C extends B{

    public C(String name) {
        super(name);
    }
}
class Demo{

...

public void demo2(ObjectHolder<? extends B> objectHolder){
    System.out.println("demo2() is executed,and objectHolder's T is "+objectHolder.get().getName());
}

public void demo3(ObjectHolder<? super B> objectHolder){
    System.out.println("demo3() is executed,and objectHolder's T is "+((A)objectHolder.get()).getName());
}

}
```
上述代码中，我新建了三个类A，B，C，A是B的直接父类，B是C的直接父类，由于只是为了作为对继承关系进一步限制的demo，三个类并没有进行复杂的实现。

此外，我在Demo类新建了两个方法，接收ObjectHolder<? extends B>为参数的demo2()方法，接收ObjectHolder<? super B>参数的demo()3方法。

ObjectHolder<? extends B>参数的意思是只要ObjectHolder存储的元素是B类或是B类的子类即可。

ObjectHolder<? super B>参数的是只要ObjectHolder存储的元素是B类或是B类的父类即可。

修改ObjectHolder中的main方法来进行验证。

```java
public class ObjectHolder<T> {

    ...

    public static void main(String[] args) {
          ObjectHolder<A> aHolder=new ObjectHolder<A>(new A("A"));
    			ObjectHolder<B> bHolder=new ObjectHolder<B>(new B("B"));
    			ObjectHolder<C> cHolder=new ObjectHolder<C>(new C("C"));
    			Demo demo=new Demo();
    			//接收ObjectHolder<? extends B>为参数的demo2()方法
    			//demo.demo2(aHolder);该代码会报错，原因是A类不是B类的子类
    			demo.demo2(bHolder);
    			demo.demo2(cHolder);
    			//接收ObjectHolder<? super B> 参数的demo()3方法
   				demo.demo3(aHolder);
   				demo.demo3(bHolder);
    				//demo.demo3(cHolder);该代码会报错，原因是C类不是B类的父类
}
}
```
代码中需要注意的地方已进行注释，这里就不再啰嗦，直接运行一下。

![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/1cefdd7e0e394cb994f788d79a7cd4f6~tplv-k3u1fbpfcp-watermark.image?)
## 2.5上下边界的限制
细心的读者会发现在demo3()方法里是先对objectHolder.get()获取到的类型强转成A类才调用了getName()方法，这是因为在Java语言里，Object类是所有类的父类，换句话说就是，demo3()方法也接收ObjectHolder<Object>参数，所以这里进行强转是存在一定的风险的，因为当ObjectHolder里存储的是Object，转型就变成了由Object到A的向下转型，程序会抛异常。

这是下边界的限制，即从含有下边界的ObjectHolder类获取到的元素只能是Object类。

这里再对上边界的限制简单介绍。

```java
public static void main(String[] args) {
    ObjectHolder<? extends A> objectHolder=new ObjectHolder<>(new C("C"));
    //C c=objectHolder.get();
    A c=objectHolder.get();
}
```
代码中被注释掉的一行会报错，这是因为objectHolder只知道存储的是A类或者其子类，不知道存储的是C类。

# 三.泛型类
上述代码中ObjectHolder类就是将泛型应用于类的常见做法，声明位置紧跟类名后面。
![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/22858fb9eae94961929dee2b90333bc1~tplv-k3u1fbpfcp-watermark.image?)
上图中红色方框圈起的是泛型的声明位置，而其它出现T的地方只是使用位置。
# 四.泛型接口
泛型接口的定义和泛型类的定义差不多，声明位置也是紧跟接口名字后面常见用于各种生产器中，比较简单，这里不再赘述。
# 五.泛型方法。
## 5.1泛型实例方法
泛型方法中的泛型声明位置在返回值前面，需要注意的是，下图中get()方法可不是泛型方法，它只是返回了在类中已经申明的泛型而已。
![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/60fcdca2d605454587c5b6a8c0993c67~tplv-k3u1fbpfcp-watermark.image?)
下面的getT()方法才是真正的泛型方法。

```java
public class ObjectHolder<T> {
...
public <T> T getT(T t){
    return t;
}
...
}
```

需要注意的是getT()申明的泛型T可以与类中声明的T相同，也可以不同，下面的代码会进行演示。

当然了，如果方法要申明的泛型与类中声明的泛型相同，直接使用类声明的泛型即可，又何必再多此一举呢？

```java
public class ObjectHolder<T> {

...


public static void main(String[] args) {
    ObjectHolder<? extends A> objectHolder=new ObjectHolder<>(new A("A"));
    String t=objectHolder.getT("String");
    System.out.println("objectHolder实例指定泛型的类型为"+objectHolder.get().getClass());
    System.out.println("getT()中指定泛型的类型为"+t.getClass());
}

}
```
上述代码的运行结果如下图所示。


![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/3cff481cebf9463cb195773315a76c9b~tplv-k3u1fbpfcp-watermark.image?)

## 5.2泛型静态方法
在静态方法使用泛型需要注意的是一点就是，静态方法无法使用类声明的泛型，必须重新声明，这是因为类方法是与实例相关的,为上面代码中getT()方法添加静态修饰符，程序会报错。

![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/61a3ee83a7bc4b38ad7ae2feffb4b137~tplv-k3u1fbpfcp-watermark.image?)

IDE会报错说静态方法无法访问ObjectHolder.this成员，而我们知道ObjectHolder.this成员是与实例相关的，换句话说就是，静态方法无法访问非静态方法和非静态成员。

# 六.泛型可变参数
由于泛型在申明过后，可直接当有具体定义的类型使用，因此与普通可变参数没什么区别,下面是一个使用例子。
```java
public  class ObjectHolder<T> {

    private final T t;

    public void demo4(T... t){
    ...
    }
}
```
# 七.泛型数组
根据Sun官方文档定义，Java不能定义明确类型的泛型数组，这句话听起来有点拗口，以上面的ObjectHolder类为例子，Java不允许构造只存储特定元素的ObjectHolder类数组，但可以创建泛型ObjectHolder类数组。
```java
public  class ObjectHolder<T> {
...
    public static void main(String[] args) {

        //ObjectHolder<A>[] objectHolders=new ObjectHolder<A>[3];
        //上述代码是不被允许的
        ObjectHolder<A>[] objectHolders=new ObjectHolder[3];
        ObjectHolder<?>[] objectHolders1=new ObjectHolder<?>[3];
    }

}
```
个人观点认为，Java进行这种限制是有道理的，数组只要求是元素是同一个类型即可，以上述代码为例子，ObjectHolder[]数组，而ObjectHolder< A>却进一步要求必须是存储A类的，我想，这是与数组的意图相违背的。

在这里，我再做一个可能不那么恰当的比喻，这就好比客厅的桌子，只要有位置且主人愿意，就可以在上面放盘子，不管盘子里装的是什么，我想不会有哪个主人愿意作茧自缚，要求只可以放装苹果的盘子。
# 八.泛型擦除
稍微对泛型有所了解的读者都知道，泛型只在编译阶段有效，下面我将通过两种方式进行验证。
## 方式一
```java
public  class ObjectHolder<T> {
...
    public static void main(String[] args) {

        //ObjectHolder<A>[] objectHolders=new ObjectHolder<A>[3];
        ObjectHolder<A> objectHolderA=new ObjectHolder<>(new A("A"));
        ObjectHolder<B> objectHolderB=new ObjectHolder<>(new B("B"));
        Class clazzA=objectHolderA.getClass();
        Class clazzB=objectHolderB.getClass();
        System.out.println("objectHolderA's class is "+clazzA );
        System.out.println("objectHolderB's class is "+clazzB);
    }
}
```
运行结果截图如下所示。
![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/7090306f445d4894a1f1d702e0b7f42c~tplv-k3u1fbpfcp-watermark.image?)
可以看到，存储A类的objectHolderA和存储B类的objectHolderB是同一个类。
## 方式二
既然泛型只存在于编译阶段，那么在程序运行期间就可以随意往存储指定元素的容器内添加任意元素了，那么如何在程序运行期间往容器里添加元素呢？答案还是反射。
```java
public  class ObjectHolder<T> {
...
    public static void main(String[] args) {

        LinkedList<String> strings=new LinkedList<>();
        Class clazz=strings.getClass();
        try {
            Method method=clazz.getDeclaredMethod("add",Object.class);
            for (int i=1;i<=10;i++){
                method.invoke(strings,i);
            }
            for (Object obj:strings){
                System.out.println(obj);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
```
运行截图如下所示，可以看到我们成功地往只存储String类的LinkedList容器添加Integer元素，并且程序并没有进行报错。
![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/bbd7005aef024cb08539cb97ca8e76bf~tplv-k3u1fbpfcp-watermark.image?)

# 参考资料
1.《On Java 8》[美] Bruce Eckel
2.[泛型中 extends 和 super 的区别？](https://itimetraveler.github.io/2016/12/27/%E3%80%90Java%E3%80%91%E6%B3%9B%E5%9E%8B%E4%B8%AD%20extends%20%E5%92%8C%20super%20%E7%9A%84%E5%8C%BA%E5%88%AB%EF%BC%9F/)
3.[java 泛型详解-绝对是对泛型方法讲解最详细的，没有之一](https://blog.csdn.net/s10461/article/details/53941091)
4.[Java基础篇：反射机制详解](https://blog.csdn.net/a745233700/article/details/82893076)