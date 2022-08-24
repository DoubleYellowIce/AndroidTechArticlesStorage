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

  可以看到Companion是CompanyObjectDemo的内部静态类，而Java的内部静态类是可以访问外部类的私有成员的。

- Companion Object的主要应用场景是什么。

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

### 匿名类

- Kotlin的匿名类和Java的匿名类有什么不同吗。

  Kotlin的匿名类可以修改声明所在的方法里的变量，而Java的匿名类只能读取声明所在的方法里的变量。

- 为什么Kotlin的匿名类可以修改声明所在的方法里的变量？

  因为Kotlin将声明所在的方法里的变量包装成持有了该变量的类。

  ```kotlin
  interface CallBack{
      fun callBack()
  }
  class CallBackDemo constructor(val callBack:CallBack){
      fun invoke(){
          callBack.callBack()
      }
  }
  fun main() {
      var i=1
      val callBackDemo=CallBackDemo(object :CallBack{
          override fun callBack() {
             i++
          }
      })
      callBackDemo.invoke()
      println("The value of i is $i")
  }
  ```

  上述代码中声明了CallBack接口，并在该接口的匿名实现类中对i变量进行修改。

  运行结果如下所示。

  <p align="center"><img src="https://raw.githubusercontent.com/DoubleYellowIce/AndroidTechArticlesStorage/master/imgs/image-20220824152228769.png"></img> </p>

  可以看见，修改i变量成功了。

  对生成的class文件在[Decompilers online](http://www.javadecompilers.com/)进行反编译获取Java文件，其中main方法的代码如下所示。

  ```kotlin
  public final class KotlinStudyKt
  {
      public static final void main() {
          final Ref$IntRef i = new Ref$IntRef();
          i.element = 1;
          final CallBackDemo callBackDemo = new CallBackDemo((CallBack)new KotlinStudyKt$main$callBackDemo.KotlinStudyKt$main$callBackDemo$1(i));
          callBackDemo.invoke();
          System.out.println((Object)("The value of i is " + i.element));
      }
  }
  ```

  可以看到i的类型不是int，而是Ref$IntRef类，此外，Ref$IntRef还有一个成员element的值为1。
