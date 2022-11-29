### Companion Object

- Companion Object是什么？

  Companion Object是Kotlin相较于Java的新特性，可以把它简单理解为Java的静态方法，这样就不需要实例化就可以调用类的方法或访问类的属性

- 顶层函数也能实现不需要实例化就可以直接调用，为什么Kotlin在有顶层函数的情况下，仍要引入Companion Object？

  因为顶层函数不可以访问类的私有成员，而Companion Object可以访问。

- 为什么Companion Object可以访问类的私有成员？

  新建一个什么都不做的CompanyObjectDemo类。

  ```kotlin
  class CompanyObjectDemo private constructor(){
  
      companion object{
      }
  
  }
  ```

  生成了CompanyObjectDemo$Companion.class文件，从这命名我们可以知道Companion(也就是companion object，下同)是CompanyObjectDemo的内部类，在[Decompilers online](http://www.javadecompilers.com/)进行反编译获得[CompanyObjectDemo$Companion.java](../choreFiles/CompanyObjectDemo$Companion.java)

  ```java
  public static final class Companion
  {
      private Companion() {
      }
  }
  ```

  可以看到Companion是CompanyObjectDemo的内部静态类，而Java的内部静态类是除了不可以访问外部类的非静态成员之外，其余成员都是可以访问的，即使是该成员是私有的。

  ```java
  public class StaticClassDemo {
  
      private static final String OUTER_CLASS_FLAG ="This is a outer class";
  
      static class StaticInnerClass{
  
          public String getOuterClassFlag(){
              return OUTER_CLASS_FLAG;
          }
      }
  
      public static void main(String[] args) {
          StaticInnerClass staticInnerClass=new StaticInnerClass();
          System.out.println(staticInnerClass.getOuterClassFlag());
      }
  }
  ```

  运行一下。

  <p align="center"><img src="https://private-pirture-storage.oss-cn-hangzhou.aliyuncs.com/img/image-20221128154827124.png"></img> </p>

- Companion Object的主要应用场景是什么?

  Companion Object可以用来控制类的实例化，进一步延伸的应用可以用来实现类的工厂方法设计模式。

  ```kotlin
  class CompanyObjectDemo private constructor(){
  
      companion object{
          fun getInstance():CompanyObjectDemo{
              return CompanyObjectDemo()
          }
      }
  
  }
  ```

  上述代码中，类的构造器设置为私有，这样就可以通过companion object::getInstance()控制类的实例化。