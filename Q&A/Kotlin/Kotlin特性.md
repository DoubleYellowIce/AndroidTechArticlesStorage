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
