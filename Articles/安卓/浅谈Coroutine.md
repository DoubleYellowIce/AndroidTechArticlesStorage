### 一.Coroutine是什么？

Coroutine是什么？这个问题很重要，重要到你即使只理解了这一小节内容就关闭了页面，顺便还骂骂咧咧点了个踩，这篇文章的目的仍达到了，一开始学习Coroutine，我总是急于去学习Coroutine的用法，扣细节，却没有先去理解Coroutine的概念，结果就是始终摸不到Coroutine的门道，这就是典型的见树木而不见森林，如今回过头来理解Coroutine的基础概念后，之前许多无头绪的问题，一下子就豁然开朗了，虽万万不敢说精通Coroutine，但多少也算了解了点Coroutine的皮毛。

Coroutine可以拆分为Co+routine，Co有Cooperation，即协作的意思，routine有计算机例行程序(来自有道词典)的意思，我们可以将其理解为函数，那么把两个单词简单合起来稍加理解就是不同函数进行协作的意思，这听起来会多少有点云里雾里，不知所言了，但这至少给我们一个模糊的概念了。

举个参考资料1中的例子，假设如今有2个函数，functionA()和functionB()，其源码如下所示。

```kotlin
fun functionA(case: Int) {
    when (case) {
        1 -> {
            println("Execute functionA mission 1")
            functionB(1)
        }
        2 -> {
            println("Execute functionA mission 2")
            functionB(2)
        }
        3 -> {
            println("Execute functionA mission 3")
            functionB(3)
        }
        4 -> {
            println("Execute functionA mission 4")
            functionB(4)
        }
    }
}
fun functionB(case: Int) {
    when (case) {
        1 -> {
            println("Execute functionB mission 1")
            functionA(2)
        }
        2 -> {
            println("Execute functionB mission 2")
            functionA(3)
        }
        3 -> {
            println("Execute functionB mission 3")
            functionA(4)
        }
        4 -> {
            println("Execute functionB mission 3")
        }
    }
}
```

在main()方法调用functionA()，并传入参数1。

```kotlin
fun main(){
    functionA(1)
}
```

运行一下，下图所示即为运行结果。

<p><img src="https://raw.githubusercontent.com/DoubleYellowIce/AndroidTechArticlesStorage/master/imgs/image-20220819143315812.png"></img></p>

可以看见线程在functionA()和functionB()之间反复来回横跳。

<p><img src="https://raw.githubusercontent.com/DoubleYellowIce/AndroidTechArticlesStorage/master/imgs/image-20220819143653402.png"></img></p>



上述代码不难理解，而这正是Coroutine做的事情，尽管其原理要复杂得多。

- Coroutine存在的意义是什么？

  - 提高线程的利用率。比如说，线程在等待IO事件响应的时候，与其在这期间空等，还不如让线程去执行其它函数。

  - 避免开发中常见的回调地狱问题。

- 线程和协程的区别是什么？

  实际上，Coroutine并不是Kotlin的独创，不少语言里都有Coroutine，目前有两种实现方案。

  - Stackless。
  - Stackfull。

  而Kotlin采用的是Stackless方案，Stackless很好理解，无栈，也就是说，Coroutine没有独立的虚拟机栈，而在Java虚拟机里面，每个线程都会有独立的虚拟机栈，这说明了Kotlin的Coroutine原理实现是在Java语言层面上的，这是协程与线程在实现上的最大区别。

  要理解线程与协程的关系，不妨回想一下线程于进程的优点是什么？

  - 提高并行性。
  - 提高资源利用率。

  而协程之于线程，就相当于线程之于进程，协程不是来替代线程的，相反，协程是一套线程管理框架，让线程更好用的。

### 二.Coroutine基础知识

#### CoroutineScope

#### Dispatchers

Dispatchers可以指定协程执行任务所在的线程，Dispatchers有三种类型。

- Main。如名所示，应用该Dispatchers的协程会在主线程执行任务，一般用于UI相关操作，因为安卓系统是不允许在非主线程中进行UI相关操作的。

  ```kotlin
  GlobalScope.launch(Dispatchers.Main) {
         updateUI()
     }
  ```

- Default。应用该Dispatchers的协程适合执行CPU密集型的任务。

  

- IO。

前面提到过Coroutine是一个线程管理框架，

Dispatchers

#### launch VS async

#### 异常处理方式

#### 生命周期管理



### 参考资料

1. [Mastering Kotlin Coroutines In Android - Step By Step Guide](https://blog.mindorks.com/mastering-kotlin-coroutines-in-android-step-by-step-guide)