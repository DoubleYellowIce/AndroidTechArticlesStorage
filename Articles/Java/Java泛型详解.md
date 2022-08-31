### 一.引言
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
StringHolder的代码非常简单，只实现了一个存取字符串的功能，但有一个很明显的缺陷，就是该容器只能存取字符串，代码复用性太差了，为了让其支持更多的元素，我们可以将value的类型改为Object类，同时将类名改为ObjectHolder。
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
从ObjectHolder取出元素时，必须经过向下转型这一步骤，这存在两个问题

- 向下转型是不安全的，举一个例子

  ```java
  List arrayList = new ArrayList();
  arrayList.add("aaaa");
  arrayList.add(100);
  
  for(int i = 0; i< arrayList.size();i++){
      String item = (String)arrayList.get(i);
      Log.d("泛型测试","item = " + item);
  }
  ```

  上述代码可以编译运行，但会抛出`class java.lang.Integer cannot be cast to class java.lang.String `异常。

- 每次取元素都需要向下转型极为不方便。

泛型很好地解决了上述两个问题。

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

### 二.类型参数
上文提到过T是类型参数，那么什么是类型参数呢，类型参数就是把类型当作一个参数来使用，举一个例子。
```java
ObjectHolder<String> stringHolder=new ObjectHolder<String>("value");
```
上述代码中就是把String类当作一个参数传入ObjectHolder，从而来达到创建一个专门存储String类的容器的目的。

类型参数又分为类型形参和类型实参。

#### 类型形参
T是类型形参，是英文单词Type的缩写，Java对类型形参并没有严格限制，只要求类型形参是字母，对大小写和个数并没有限制，不过为了代码的可阅读性，类型形参一般都是单个大写字母，且该字母一般为有意义单词的缩写，比如说常见的类型形参中，V为Value的缩写，N为Node的缩写。

#### 类型实参
类型实参比较好理解了，任意有明确定义的类都是类型实参，比如说String类就是类型实参。
#### 通配符
```java
class Printer {
    public static void printHolder(ObjectHolder<?> objectHolder){
        System.out.println("printHolder() is executed,and objectHolder's T is "+objectHolder.get());
    }
}
```
上述代码中，我新建了一个Printer类，并实现了printHolder()方法，该方法接受一个ObjectHolder<?>参数。尖括号<>括住的是就是通配符，它的符号是`?`，该参数的意思是只要是ObjectHolder类就行，不管ObjectHolder存储的是什么元素。

```java
public static void main(String[] args) {
        ObjectHolder<String> stringHolder=new ObjectHolder<>("String");
        ObjectHolder<Integer> integerHolder=new ObjectHolder<>(0);
        ObjectHolder<Boolean> booleanHolder=new ObjectHolder<>(true);
        printHolder(stringHolder);
        printHolder(integerHolder);
        printHolder(booleanHolder);
}
```
运行一下。

<p align="center"><img src="https://raw.githubusercontent.com/DoubleYellowIce/PicBed/main/imgs/image-20220830152701936.png"></img></p>

值得一提的是通配符?是类型实参，换句话说就是，通配符和String，Object等有明确定义的类为相同类型。

#### 上下边界
上下边界用于对元素的继承关系做进一步限制的。

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
```
上述代码中，我新建了三个类A，B，C，A是B的直接父类，B是C的直接父类，由于只是为了作为对继承关系进一步限制的demo，三个类并没有进行复杂的实现。

```java
class Printer {
	public static void printHolderOfObjectExtendsB(ObjectHolder<? extends B> objectHolder){
        System.out.println("printHolderOfObjectExtendsB() is executed,and objectHolder's T is "+objectHolder.get().getName());
    }

    public static void printHolderOfObjectSuperB(ObjectHolder<? super B> objectHolder){
        System.out.println("printHolderOfObjectSuperB() is executed,and objectHolder's T is "+((A)objectHolder.get()).getName());
    }

}
```

此外，我在Printer类添加了两个方法。

- printHolderOfObjectExtendsB()方法。该方法接收ObjectHolder<? extends B>参数，意思是只要ObjectHolder存储的元素是B类或是B类的子类即可。
- printHolderOfObjectSuperB()方法。该方法接收ObjectHolder<? super B>参数，意思是只要ObjectHolder存储的元素是B类或是B类的父类即可。

修改Printer中的main方法来进行验证。

```java
public class ObjectHolder<T> {
    public static void main(String[] args) {
        ObjectHolder<A> holderOfSuperB=new ObjectHolder<>(new A("A"));
        ObjectHolder<B> holderOfB=new ObjectHolder<>(new B("B"));
        ObjectHolder<C> holderOfExtendsB=new ObjectHolder<>(new C("C"));
        //printHolderOfObjectExtendsB(holderOfSuperB);该代码会报错，原因是A类不是B类的子类
        printHolderOfObjectExtendsB(holderOfB);
        printHolderOfObjectExtendsB(holderOfExtendsB);
        //printHolderOfObjectSuperB(holderOfExtendsB);该代码会报错，原因是C类不是B类的父类
        printHolderOfObjectSuperB(holderOfSuperB);
        printHolderOfObjectSuperB(holderOfB);
	}
}
```
运行一下。

<p align="center"><img src="https://raw.githubusercontent.com/DoubleYellowIce/PicBed/main/imgs/image-20220830163009350.png"></img></p>

- 下边界限制

细心的读者会发现在printHolderOfObjectSuperB()方法里需要先对objectHolder.get()获取到的类型强转成A类才能调用了getName()方法，这是因为在Java语言里，Object类是所有类的父类，换句话说就是，demo3()方法也接收ObjectHolder<Object>参数，所以这里进行强转是存在一定的风险的。

这是下边界的限制，即从含有下边界的ObjectHolder类获取到的元素只能是Object类。

- 上边界限制。

```java
public static void main(String[] args) {
    ObjectHolder<? extends A> holderOfExtendsA=new ObjectHolder<>(new C("C"));
    //C c=holderOfExtendsA.get();
    A c=holderOfExtendsA.get();
}
```
代码中被注释掉的一行会报错，这是因为objectHolder只知道存储的是A类或者其子类，不知道存储的是C类。

### 三.泛型应用

#### 泛型类

泛型声明位置紧跟类名后面，同样以ObjectHolder为例。
```java
public class ObjectHolder<T> {

    private final T t;
		..
}
```

#### 泛型接口

泛型接口的定义和泛型类的定义差不多，声明位置也是紧跟接口名字后面，常见用于各种生产器中，比较简单，这里不再赘述。
#### 泛型方法。

##### 实例方法

泛型方法中的泛型声明位置在返回值前面，需要注意的是，下述代码中get()方法可不是泛型方法，它只是返回了在类中已经申明的泛型而已。

```java
public class ObjectHolder<T> {

    private final T t;
		...
    
    public T get(){
        return t;
    }
}
```

下面的getT()方法才是真正的泛型方法。

```java
public class ObjectHolder<T> {

		public <T> T getT(T t){
    	return t;
		}

}
```

需要注意的是getT()申明的泛型T可以与类中声明的T相同，也可以不同，下面例子就演示了不同的情况。

```java
public  class ObjectHolder<T> {

		...
    public static void main(String[] args) {
        ObjectHolder<A> holderOfA=new ObjectHolder<>(new A("A"));
        String t=holderOfA.getT("String");
        System.out.println("holderOfA实例中的泛型的类型为"+holderOfA.get().getClass());
        System.out.println("holderOfA中的getT()方法中的泛型的类型为"+t.getClass());
    }
}
```

运行一下。

<p align="center"><img src="https://raw.githubusercontent.com/DoubleYellowIce/PicBed/main/imgs/image-20220830164226759.png"></img></p>

##### 静态方法

在静态方法使用泛型需要注意的是一点就是，静态方法无法使用类声明的泛型，必须重新声明，这是因为类声明的泛型是与实例相关的,为上述代码中getT()方法添加静态修饰符，程序会报错。

![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/61a3ee83a7bc4b38ad7ae2feffb4b137~tplv-k3u1fbpfcp-watermark.image?)

#### 泛型可变参数

由于泛型在申明过后，可直接当有具体定义的类型使用，因此与普通可变参数没什么区别,下面是一个使用例子。
```java
public  class ObjectHolder<T> {

    private final T t;

    public void variableArgumentsDemo(T... t){
    ...
    }
}
```
### 四.泛型擦除
稍微对泛型有所了解的读者都知道，泛型只在编译阶段有效。

既然泛型只存在于编译阶段，那么反过来说，是可以在程序运行期间通过反射往存储指定元素的容器内添加任意元素了。

```java
public  class ObjectHolder<T> {
    public static void main(String[] args) {
        LinkedList<String> strings=new LinkedList<>();
        Class clazz=strings.getClass();
        try {
            Method method=clazz.getDeclaredMethod("add",Object.class);
            for (int i=1;i<=10;i++){
                method.invoke(strings,i);
            }
            for (Object obj:strings){
                System.out.print(obj+" ");
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
```
运行一下

<p align="center"><img src="https://raw.githubusercontent.com/DoubleYellowIce/PicBed/main/imgs/image-20220831122641476.png"></img></p>

可以看到我们成功地往只存储String类的LinkedList容器添加Integer元素，并且程序并没有报错。

### 五.泛型数组

Java不能定义明确类型的泛型数组，以[Sun官方文档](https://docs.oracle.com/javase/tutorial/extra/generics/fineprint.html)给出的代码为例。

```java
List<String>[] lsa = new List<String>[10]; // 定义明确类型的泛型数组
Object o = lsa;    
Object[] oa = (Object[]) o;    
List<Integer> li = new ArrayList<Integer>();    
li.add(new Integer(3));    
oa[1] = li; //2
String s = lsa[1].get(0); // Run-time error: ClassCastException.
```

假设Java允许定义明确类型的泛型数组，那么上述代码可以编译通过，但会在运行时抛出ClassCastException的异常。

这是因为Java泛型只存在编译阶段，在运行期间，`List<String>`和`List<Integer>`并没有区别，这也是代码2处可以成功运行的原因。

那么怎么构造泛型数组？Sun官方文档也给出了示例。

```java
List<?>[] lsa = new List<?>[10];
Object o = lsa;
Object[] oa = (Object[]) o;
List<Integer> li = new ArrayList<Integer>();
li.add(new Integer(3));
// Correct.
oa[1] = li;
// Run time error, but cast is explicit.
String s = (String) lsa[1].get(0);
```

可以看到，Java允许创建用通配符`?`代替指定类型的泛型数组。

### 参考资料

1. 《On Java 8》[美] Bruce Eckel
2. [泛型中 extends 和 super 的区别？](https://itimetraveler.github.io/2016/12/27/%E3%80%90Java%E3%80%91%E6%B3%9B%E5%9E%8B%E4%B8%AD%20extends%20%E5%92%8C%20super%20%E7%9A%84%E5%8C%BA%E5%88%AB%EF%BC%9F/)
3. [java 泛型详解-绝对是对泛型方法讲解最详细的，没有之一](https://blog.csdn.net/s10461/article/details/53941091)
4. [Java基础篇：反射机制详解](https://blog.csdn.net/a745233700/article/details/82893076)