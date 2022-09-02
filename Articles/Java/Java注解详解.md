### 一.引言
文章开始前，我们先来看看如何自定义一个简单的注解AnnotationDemo，以便各位读者对注解有一个初步的理解。  
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AnnotationDemo{
    
}
```
#### @Target
@Target标明该注解可以作用的元素，比如说上述代码里的@Target(ElementType.METHOD)就代表AnnotationDemo这个注释是用来注释方法的。

ElementType是一个枚举类型，下面是其源代码。

```java

public enum ElementType {
    /** Class, interface (including annotation interface), enum, or record
     * declaration */
    TYPE,

    /** Field declaration (includes enum constants) */
    FIELD,

    /** Method declaration */
    METHOD,

    /** Formal parameter declaration */
    PARAMETER,

    /** Constructor declaration */
    CONSTRUCTOR,

    /** Local variable declaration */
    LOCAL_VARIABLE,

    /** Annotation interface declaration (Formerly known as an annotation type.) */
    ANNOTATION_TYPE,

    /** Package declaration */
    PACKAGE,

    /**
     * Type parameter declaration
     * @since 1.8
     */
    TYPE_PARAMETER,

    /**
     * Use of a type
     * @since 1.8
     */
    TYPE_USE,

    /**
     * Module declaration.
     * @since 9
     */
    MODULE,

    /**
     * Record component
     * @since 16
     */
    RECORD_COMPONENT;
}
```
ElementType中的元素即为@Target可以选择的参数。

当没有指明Target中的内容时，则该注解可以作用于任意元素。

当需要让自定义注解可以注解多个内容时，可按下面的形式进行指明。

```java
@Target(value = {ElementType.CONSTRUCTOR,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AnnotationDemo{

}
```
#### @Retention
@Retention的作用是标明注解的声明周期。

@Retention参数的可选择范围是枚举类RetentionPolicy中的元素，我们先来看看该类的源代码。

```java
public enum RetentionPolicy {
    /**
     * Annotations are to be discarded by the compiler.
     */
    SOURCE,

    /**
     * Annotations are to be recorded in the class file by the compiler
     * but need not be retained by the VM at run time.  This is the default
     * behavior.
     */
    CLASS,

    /**
     * Annotations are to be recorded in the class file by the compiler and
     * retained by the VM at run time, so they may be read reflectively.
     *
     * @see java.lang.reflect.AnnotatedElement
     */
    RUNTIME
}
```
- SOURCE。表明该注释只存在于源代码中，会被编译器丢弃，不会存在生成的class文件中。

- CLASS。表明该注释存在于class文件中，但不会被虚拟机保留。

- RUNTIME。表明该注释存在于运行期间。

结合单词的意思还是很好理解的，这里就不再赘述。

### 二.元注解

元注解是作用于注解的注解，上文中@Target和@Retention是Java的元注解，除此之外，Java还有@Inherited，@Documented,@Repeatable这三个元注解。

#### @Inherited
注解本身是不允许被继承的，也就是说不能新建一个类继承于`@Interface`.

@Inherited标明该注释是可以被继承的,这个继承可不是字面意义上的继承，是指子类能继承父类的注解，而不是指注解本身能继承。

我们可以通过getAnnotations()方法进行验证,代码如下所示。

```java

@Target(value = {ElementType.CONSTRUCTOR,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited//新增@Inherited
public @interface AnnotationDemo{

}
...
@AnnotationDemo
class A{

}
class B extends A{
    public static void main(String[] args) {
        B b=new B();
        System.out.println(Arrays.toString(b.getClass().getAnnotations()));
        //如果没有用@Inherited去注解@AnnotationDemo，结果为[]
        //如果用@Inherited去注解@AnnotationDemo，结果为[@AnnotationDemo()]
    }
}
```
#### @Documented
@Documented的作用是标明该注解能否出现在Java文档里。

新建一个类Demo，并用上文所新建的注解@AnnotationDemo对其进行注解，此时的@AnnotationDemo还没有用@Documented进行修饰。

```java
@AnnotationDemo
public class Demo {

}
```
然后利用IDE生成Java文档。

<p align="center"><img src="https://raw.githubusercontent.com/DoubleYellowIce/PicBed/main/imgs/image-20220901160040714.png"></img></p>


修改@AnnotationDemo注解的代码，添加@Documented，并且修改@Target中的内容，添加ElementType.TYPE使其能修饰类，代码如下所示。

```java
@Target(value = {ElementType.CONSTRUCTOR,ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AnnotationDemo{

}
```
同样利用IDE生成Java文档。

<p align="center"><img src="https://raw.githubusercontent.com/DoubleYellowIce/PicBed/main/imgs/image-20220901160357607.png"></img></p>


可以看到@AnnotationDemo注解出现在文档里了。

#### @Repeatable
在讲解@Repeatable之前，我们先来介绍注解中的元素。

注解既可以分为元注解，自定义注解和标准注解，也可以用有无元素的标准将其划分为标记注解和非标记注解。

- 标记注解。指那些不含元素的注解，标记注解不要求我们传入参数，Junit中@Test就是较为常见的标记注解。

- 非标记注解。指那些含元素的注解，如@Target注解，需要传入value值。

我们来定义一个非标记注解，修改@AnnotationDemo，修改后的代码如下所示。

```java
@Target(value = {ElementType.CONSTRUCTOR,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface AnnotationDemo{
    String value() default "";
}
```
在上面的代码中，我们添加了一个类型为String，名字为value的元素，default的意思是value的默认值为空，也就是说用户可以选择使用默认值，或者指定一个值。

Java对注解中的元素要求比较苛刻，元素的类型只能从以下列表中选择。

- 基本数据类型

- String

- Class

- enum

- Annonation。

  从这可知注解是支持嵌套注解的，用法如下所示。

  ```java
  @Target(value = {ElementType.CONSTRUCTOR,ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @Documented
  public @interface AnnotationDemo{
      String value() default "";
  
      AnotherAnnotation anotherAnnotation() default @AnotherAnnotation();
  }
  @interface AnotherAnnotation{
  
  }

- 上述类型的数组。

需要注意的是以下两点。

- 注解的元素必须有明确值。

  Java要求使用注解时必须指定元素的值，除非元素有默认值，此外，不支持将null作为元素的值，为了绕过这个限制，有时会指定一些特殊值，比如说负数来表明该元素的值为null。

  以AnnotationDemo为例，当我们像将value()置空时，可以把`null`字符串作为value()的值。

  ```java
  @AnnotationDemo(value="null")
  public class Demo {
  ...
  }
  ```

- Java支持对特殊元素进行快速赋值，如果注解中元素的名字为value，且是唯一需要进行传值的元素，那么就可以进行不用指定元素名字进行传值。

  如AnnotationDemo，只有一个需要传值的元素，那么上述代码就可以简化为

  ```java
  @AnnotationDemo("null")
  public class Demo {
  ...
  }
  ```

@Repeatable是Java8新增加的元注解，它的作用是表明该注解能重复作用于同一个目标。

在Java8之前，如果一个注解想要重复作用于同一个目标，只能以数组的形式接受元素，我们修改@AnnotationDemo的代码，让其接收String数组,代码如下所示。

```java
@Target(value = {ElementType.CONSTRUCTOR,ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface AnnotationDemo{
    String [] value() default "null";
}
```
同样地，我们让@AnnotationDemo注释Demo类，并传入多个Value值，代码如下所示。

```java
@AnnotationDemo({"value1","value2","value3"})
public class Demo {

}
```
这是Java8之前的做法，在Java8添加了@Repeatable之后，不需要将value元素类型设置为数组，也能接收多个值了。

新建AnnotationDemos注释，并且声明名字为value。

由@AnnotationDemo注释构成的数组元素。

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AnnotationDemos {
    AnnotationDemo[] value();
}
```
修改AnnotationDemo代码，添加@Repeatable注释，并且传入AnnotationDemos.class作为参数。
```java
@Target(value = {ElementType.CONSTRUCTOR,ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(AnnotationDemos.class)
public @interface AnnotationDemo{
    String value() default "null";
}
```
之后我们便可以像下面的代码一样将AnnotationDemo重复作用于同一个目标了。
```java
@AnnotationDemo("value1")
@AnnotationDemo("value2")
@AnnotationDemo("value3")
public class Demo {

}
```
需要注意的是，AnnotationDemos注释的作用时期必须大于等于AnnotationDemo的生命时期，举个例子，如果AnnotationDemo的@Retention的值为**CLASS**，那么AnnotationDemos只能是**CLASS**或者**RUNTIME**。

### 三.标准注解
标准注解总共有5个，前三个都是我们比较常见的注解，@Override，@Deprecated，@SuppressWarnings，Java7新增了@SafeVarargs，Java8新增了@Functionalllinterface。

#### @Override
我们先来看看@Override的源码。
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Override {
}
```
从源码可知@Override只能用于修饰方法，这和我们印象中对@Override的理解是一致的，@Override是来表明子类中的某个方法复写了父类的方法。

#### @Deprecated

@Deprecated也是大家比较常见的注解，该注解用于标明作用目标已经废弃了，不再建议使用。

在A类添加一个用**@Deprecated**修饰的方法。

```java
@AnnotationDemo()
class A{
    @Deprecated
    public void deprecatedMethod(){

    }
}
```

#### @SuppressWarinings
同样地，我们先来查看@SuppressWarinings的源码。
```java
@Target({TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, MODULE})
@Retention(RetentionPolicy.SOURCE)
public @interface SuppressWarnings {
    
    String[] value();
    
}
```
可以看到@SuppressWarnings可以注解的目标不少，既可以注解类，也可以注解方法，参数，构造器等等，需要传入名为value的String[]数组，这里值得一提的是，value没有默认值，这说明在使用该注解时至少需要传入一个字符串。

@SuppressWarnings比较好理解，顾名思义，就是去除警告。

在Demo的main()调用A中的deprecatedMethod()。

<p align="center"><img src="https://raw.githubusercontent.com/DoubleYellowIce/PicBed/main/imgs/image-20220902105406798.png"></img></p>

可以看到deprecatedMethod()被IDE划了一横线来提醒开发者该方法已经废弃。

有些时候如果的确需要用到废弃的方法，但看着那一横线很别扭，就可以用@SuppressWarinings来让IDE去除警告。

<p align="center"><img src="https://raw.githubusercontent.com/DoubleYellowIce/PicBed/main/imgs/image-20220902105808386.png"></img></p>

#### @SafeVarargs
先来看看@SafeVarargs的源码。
```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
public @interface SafeVarargs {}
```
从源码可以看到@SafeVarargs是可以作用于运行期的，其作用目标仅可以为构造器和方法，该注解的具体用法如下所示(来源请见参考资料3)。
> 在声明具有模糊类型（比如：泛型）的可变参数的构造函数或方法时，Java编译器会报unchecked警告。鉴于这些情况，如果程序员断定声明的构造函数和方法的主体不会对其varargs参数执行潜在的不安全的操作，可使用@SafeVarargs进行标记，这样的话，Java编译器就不会报unchecked警告。
在A类中添加如下方法。

```java
class A<T>{
    public static <T> T[] unsafe(T... types){
        return types;
    }
}
```

IDE会报`Possible heap pollution`的警告。

<p align="center"><img src="https://raw.githubusercontent.com/DoubleYellowIce/PicBed/main/imgs/image-20220902144544271.png"></img></p>

`Possible heap pollution`可以直译为有堆污染的可能性，什么是堆污染呢，就是一个类的引用指向另一个类的实例，这种情况主要在泛型中存在，因为泛型只存在编译期，下面简单给出个堆污染的例子。

```java
List<String> listOfString = new ArrayList<>();
listOfString.add("Geeksforgeeks");
  
List<Integer> listOfInteger= (List<Integer>)(Object)listOfString;

Integer firstElement= listOfInteger.get(0);
System.out.println(firstElement);
```

上面代码中`List<Integer>`引用指向了`List<String>`实例。

可以@SafeVarargs让IDE去除警告。

<p align="center"><img src="https://raw.githubusercontent.com/DoubleYellowIce/PicBed/main/imgs/image-20220902145451819.png"></img></p>

@SafeVarargs和@SuppressWarinings都能去除警告，那么二者有什么区别呢？

- 作用对象不同。
  - @SafeVarargs只能作用于final和static方法或构造器，且只能去除`Possible heap pollution`警告。
  - @SuppressWarinings可作用位置不做限制，且能去除任意指定警告。

#### @Functionalllinterface

先来看看@Functionalllinterface的源码。
```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FunctionalInterface {}
```
从源码我们可以看到@Functionalllinterface的唯一作用对象为类，作用时间为运行期。

那么该类有什么作用呢？我们可以看下@Functionalllinterface源码中的注释。

>
> ”An informative annotation type used to indicate that an interface type declaration is intended to be a functional interface as defined by the Java Language Specification. Conceptually, a functional interface has exactly one abstract method. Since default methods have an implementation, they are not abstract. If an interface declares an abstract method overriding one of the public methods of java.lang.Object, that also does not count toward the interface's abstract method count since any implementation of the interface will have an implementation from java.lang.Object or elsewhere.“

简单来说，@Functionalllinterface就是标明某个接口为函数接口，而函数接口是指只含有一个抽象方法的接口，注释里还特别提到，如果接口声明一抽象方法来覆盖父类接口的中的抽象方法，那么这不会占用抽象方法的唯一名额。

### 参考资料
1. 《On Java 8》(又名《Java编程思想 5》)
2. [深入理解Java注解类型(@Annotation)](https://blog.csdn.net/javazejian/article/details/71860633)
3. [@SafeVarargs注解的使用](https://www.cnblogs.com/springmorning/p/10285780.html)
3. [Java @SafeVarargs Annotation](https://www.baeldung.com/java-safevarargs)
3. [What is Heap Pollution in Java and how to resolve it?](https://www.geeksforgeeks.org/what-is-heap-pollution-in-java-and-how-to-resolve-it/)