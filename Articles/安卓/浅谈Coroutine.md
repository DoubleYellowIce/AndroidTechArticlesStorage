### 一.Coroutine是什么？

Coroutine是什么？这个问题很重要，重要到你即使只理解了这一小节内容就关闭了页面，顺便还骂骂咧咧点了个踩，这篇文章的目的仍达到了，一开始学习Coroutine，我总是急于去学习Coroutine的用法，扣细节，却没有先去理解Coroutine的概念，结果就是始终摸不到Coroutine的门道，这就是典型的见树木而不见森林，如今回过头来理解Coroutine的基础概念后，之前许多无头绪的问题，一下子就豁然开朗了，虽万万不敢说精通Coroutine，但多少也算了解了点Coroutine的皮毛。

Coroutine可以拆分为Co+routine，Co有Cooperation，即协作的意思，routine有计算机例行程序（词意来自有道词典）的意思，我们可以将其理解为函数，那么把两个单词简单合起来稍加理解就是不同函数进行协作的意思。

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

<p align="center"><img src="https://raw.githubusercontent.com/DoubleYellowIce/AndroidTechArticlesStorage/master/imgs/image-20220819143315812.png"></img></p>

可以看见线程在functionA()和functionB()之间反复横跳。

<p align="center"><img src="https://raw.githubusercontent.com/DoubleYellowIce/AndroidTechArticlesStorage/master/imgs/image-20220819143653402.png"></img></p>

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

### 二.CoroutineScope

CoroutineScope是协程域，所有协程都必须在协程域里运行。

CoroutineScope只有一个构造器参数，CoroutineContext，而CoroutineContext又由Job，Dispatchers，CoroutineName，ExceptionHandler组成，需要注意的是，CoroutineName，ExceptionHandler是可选参数。

#### Job

Job用于管理协程的生命周期，每次启动协程都会返回一个Job。

Job总共有六个状态，分别是**New**， **Active**，**Completing**，**Completed**，**Cancelling** 和 **Cancelled**，尽管不能直接获取这六个状态，可以通过访问Job的isActive，isCancelled和 isCompleted字段来判断协程的状态。

```kotlin
fun main() {
    val coroutineContext=Dispatchers.Default+CoroutineName("JobDemo")
    val job=CoroutineScope(coroutineContext).launch {
        //延迟0.1秒
      	delay(100)
    }
    while(job.isActive){
        println("The coroutine is still active ")
    }
    if (job.isCompleted){
        println("The coroutine is completed")
    }
}
```

运行一下。

<p align="center"><img src="https://private-pirture-storage.oss-cn-hangzhou.aliyuncs.com/img/image-20220910144844779.png"></img></p>

Job里有一个**cancel()**方法，该方法比较重要，字如其名，其作用是取消掉协程。

一个很常见的开发场景，假设用户点开了一个页面，页面对应的Activity通过协程发起网络请求，这时用户秒退页面，但网络请求还没执行完成，那么就可以在onDestroy()调用Job的cancel()方法来取消掉协程。

```kotlin
class MainActivity : AppCompatActivity(), CoroutineScope {
		//代码来自资料1
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var job: Job

  	override fun onCreate(savedInstanceState: Bundle?) {
    	super.onCreate(savedInstanceState)
    	job = Job() // create the Job
		}

		override fun onDestroy() {
    	job.cancel() // cancel the Job
    	super.onDestroy()
		}
}
```

需要注意的是，MainActivity继承了CoroutineScope。

#### Dispatchers

Dispatchers可以指定协程执行任务所在的线程，Dispatchers有三种类型。

- Main。如名所示，应用该Dispatchers的协程会在主线程执行任务，一般用于UI相关操作，因为安卓系统是不允许在非主线程中进行UI相关操作的。

  ```kotlin
  GlobalScope.launch(Dispatchers.Main) {
         updateUI()
     }
  ```

- Default。应用该Dispatchers的协程适合执行CPU密集型的任务。

  ```kotlin
  GlobalScope.launch(Dispatchers.Default) {
  			someCpuIntensiveWork()
  	 }
  ```

- IO。应用该Dispatchers的协程适合执行IO任务。

  ```kotlin
  GlobalScope.launch(Dispatchers.IO) {
  			fetchDataFromServer()
  	 }
  ```

#### CoroutineName

CoroutineName是协程名字。

#### CoroutineExceptionHandler

CoroutineExceptionHandler是协程异常处理器，当协程抛出异常，就会调用CoroutineExceptionHandler的handleException()方法。

```kotlin
public interface CoroutineExceptionHandler : CoroutineContext.Element {
    ..
    public fun handleException(context: CoroutineContext, exception: Throwable)
}
```

来个小demo。

```kotlin
fun main() {
    val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        println("CoroutineExceptionHandler got $exception")
    }
    val coroutineContext = Dispatchers.Default+CoroutineName("ExceptionHandlerDemo")+exceptionHandler
    CoroutineScope(coroutineContext).launch (){
        throw Exception()
    }
  	//CoroutineScope(context)为顶级协程，线程休眠0.1秒等待协程执行完成。
    Thread.sleep(100)
}
```

运行一下。

<p align="center"><img src="https://private-pirture-storage.oss-cn-hangzhou.aliyuncs.com/img/image-20220912100634767.png"></img></p>



### 三.异常处理

#### try-catch块

除了CoroutineExceptionHandler之外，异常还可以用常规的try-catch块处理。

```kotlin
fun main() {
    val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        println("CoroutineExceptionHandler got $exception")
    }
    val coroutineContext = Dispatchers.Default+CoroutineName("ExceptionHandlerDemo")+exceptionHandler
    CoroutineScope(coroutineContext).launch (){
				try {
            throw Exception()
        }catch (exception:Exception){
            println("Caught exception in try-catch block")
        }    
    }
  	//CoroutineScope(context)为顶级协程，线程休眠0.1秒等待协程执行完成。
    Thread.sleep(100)
}
```

运行一下。

<p align="center"><img src="https://private-pirture-storage.oss-cn-hangzhou.aliyuncs.com/img/image-20220912104238266.png"></img></p>

需要注意的是，上面的代码既有CoroutineExceptionHandler又有try-catch块，而抛出的异常被try-catch块给处理了，换句话说，CoroutineExceptionHandler比try-catch块的优先级要低，CoroutineExceptionHandler更多是作为一个兜底措施，只有在协程内没有进行异常处理，才会调用CoroutineExceptionHandler的handleException()方法。

#### 子协程的异常处理

##### 顶层协程的CoroutineExceptionHandler

如果是子协程的异常，且子协程没有进行任何异常处理，会出现什么情况？

```kotlin
fun main() {
    val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        println("CoroutineExceptionHandler got $exception")
    }
    val coroutineContext = Dispatchers.Default+CoroutineName("ExceptionHandlerDemo")+exceptionHandler
    CoroutineScope(coroutineContext).launch (){//父协程
				try {
            launch {//发起子协程
                throw Exception()
            }
        }catch (exception:Exception){
            println("Caught exception in try-catch block")
        }
    }
  	//CoroutineScope(context)为顶级协程，线程休眠0.1秒等待协程执行完成。
    Thread.sleep(100)
}
```

运行一下。

<p align="center"><img src="https://private-pirture-storage.oss-cn-hangzhou.aliyuncs.com/img/image-20220912105738427.png"></img></p>

可以看到，CoroutineExceptionHandler的handleException()方法被调用了，这是因为当子协程没有进行任何异常处理的时候，会将异常向上传递给顶层协程（没有父协程的协程）的CoroutineExceptionHandler，如下图所示。

<p align="center"><img src="https://private-pirture-storage.oss-cn-hangzhou.aliyuncs.com/img/Screenshot-2020-08-25-at-10.51.22.png"></img></p>

<p align="center">图片来源见参考资料4</p>

上图可以通过一个简单的例子来验证一下。

```kotlin
fun main() {
    val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        println("Top-Level CoroutineExceptionHandler got $exception")
    }
    val subExceptionHandler= CoroutineExceptionHandler{_,exception->
        println("SubCoroutineExceptionHandler got $exception")
    }
    val coroutineContext = Dispatchers.Default + CoroutineName("ExceptionHandler")
    CoroutineScope(coroutineContext+exceptionHandler).launch() {//顶层协程
        try {
            launch (subExceptionHandler){//第二层协程/子协程
                launch(subExceptionHandler){//第三层协程/子子协程
                    launch (subExceptionHandler){//第四层协程/子子子协程
                        throw Exception()
                    }
                }
            }

        } catch (exception: Exception) {
            println("Caught $exception in try-catch block")
        }
    }
    Thread.sleep(100)
}
```

上面代码中每一个子协程都有一个subExceptionHandler，只有顶层协程是exceptionHandler，异常会在向上传递过程中被subExceptionHandler处理吗？运行一下。

<p align="center"><img src="https://private-pirture-storage.oss-cn-hangzhou.aliyuncs.com/img/image-20220912160342501.png"></img></p>

可以看到，异常只会被传递给顶层协程的CoroutineExceptionHandler。

那么如果顶层协程没有CoroutineExceptionHandler，异常会被try-catch块处理了呢？

```kotlin
fun main() {CoroutineExceptionHandler
    val coroutineContext = Dispatchers.Default+CoroutineName("ExceptionHandler")//没有CoroutineExceptionHandler
    CoroutineScope(coroutineContext).launch (){//父协程
        try {
            launch {//发起子协程
                throw Exception()
            }
        }catch (exception:Exception){
            println("Caught exception in try-catch block")
        }
    }
    Thread.sleep(100)
}
```

答案是不会。

![image-20220912110625089](https://private-pirture-storage.oss-cn-hangzhou.aliyuncs.com/img/image-20220912110625089.png)

还是那句话，子协程只会把异常传递给顶层协程的CoroutineExceptionHandler，当没有CoroutineExceptionHandler时，顶层协程就会直接把异常抛出去在所在的线程。

那么用try-catch块把顶层协程给包起来能处理异常吗。

```kotlin
fun main() {
    val coroutineContext = Dispatchers.Default + CoroutineName("ExceptionHandler")//没有CoroutineExceptionHandler
    try {//用try-catch块把顶层协程给包起来
        CoroutineScope(coroutineContext).launch() {
            try {
                launch {
                    throw Exception()
                }
            } catch (exception: Exception) {
                println("Caught $exception in try-catch block")
            }
        }
    }catch (exception: Exception){
        println("Caught $exception in try-catch block")
    }
}
```

答案是不能。

![image-20220912163138651](https://private-pirture-storage.oss-cn-hangzhou.aliyuncs.com/img/image-20220912163138651.png)

原因是协程运行在的线程是在`DefaultDispatcher-worker-3`线程，main线程的try-catch块无法处理`DefaultDispatcher-worker-3`线程的异常，对于这点有疑问的小伙伴可以自行去查阅一下子线程的异常处理方式，这里就不再做演示了。

因此，为每一个顶层协程都设置一个CoroutineExceptionHandler来作为兜底措施是一个良好的编程规范。

##### coroutineScope和supervisorScope

那有没有一种可以用try-catch块的方式来处理子协程的异常，有的，用coroutineScope和supervisorScope将子协程包起来就可以了。

```kotlin
fun main() {

    val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        println("Top-Level CoroutineExceptionHandler got $exception")
    }
    val coroutineContext = Dispatchers.Default + CoroutineName("ExceptionHandler")+exceptionHandler
    CoroutineScope(coroutineContext).launch() {
        try {
            coroutineScope {//这里改成supervisorScope也是可以的
                launch {
                    throw Exception()
                }
            }
        } catch (exception: Exception) {
            println("Caught $exception in try-catch block")
        }
    }
    Thread.sleep(100)
}
```

运行一下。

<p align="center"><img src="https://private-pirture-storage.oss-cn-hangzhou.aliyuncs.com/img/image-20220913104900577.png"></img></p>

coroutineScope和supervisorScope都可以实现上述的功能，但二者区别是

- coroutineScope内只要有一个协程抛出了异常，就不再运行后续代码。

  ```kotlin
  fun main() {
      val exceptionHandler = CoroutineExceptionHandler { _, exception ->
          println("Top-Level CoroutineExceptionHandler got $exception")
      }
      val coroutineContext = Dispatchers.Default + CoroutineName("ExceptionHandler")+exceptionHandler
      CoroutineScope(coroutineContext).launch() {
          try {
              coroutineScope{
                  launch {
                      println("This is sub coroutine 1")
                      throw Exception()
                  }
                  launch {
                      println("This is sub coroutine 2")
                  }
                  launch {
                      println("This is sub coroutine 3")
                  }
              }
          } catch (exception: Exception) {
              println("Caught $exception in try-catch block")
          }
      }
      Thread.sleep(100)
  }
  ```

  运行一下。

  <p align="center"><img src="https://private-pirture-storage.oss-cn-hangzhou.aliyuncs.com/img/image-20220913111126936.png"></img></p>

  可以看见，子协程1抛出异常后，子协程2和子协程3就没有再运行了。

- supervisorScope内即使有协程抛出了异常，也会运行后续代码。

  将上述代码中的**coroutineScope**改成**supervisorScope**后运行一下。

  <p align="center"><img src="https://private-pirture-storage.oss-cn-hangzhou.aliyuncs.com/img/image-20220913111418387.png"></img></p>

  可以看见，子协程1抛出异常后，子协程2和子协程3也是照常运行了。

##### 总结

有两种方式处理子协程抛出异常的情况。

- 设置顶层协程的CoroutineExceptionHandler。
- 用coroutineScope或supervisorScope将子协程包起来

更详细的协程异常处理的讲解可以查阅参考资料4。

### 四.协程使用

协程主要有两种发起方式，launch和async。

#### launch

launch适用于`fire and forget`的情况，说人话就是，这种协程发起方式适合执行不需要回调的任务，前面的例子都是用launch，再加上launch比较简单，这里就一笔带过了。

#### async

async适合的场景刚好与launch相反，适合执行需要回调的任务，上文提到过协程一大亮点就是可以解决地狱回调问题，而这正是通过async方法来实现的。

```kotlin
fun main() {
    val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        println("Top-Level CoroutineExceptionHandler got $exception")
    }
    val coroutineContext = Dispatchers.Default + CoroutineName("ExceptionHandler")+exceptionHandler
    CoroutineScope(coroutineContext).launch() {
        try {
            val res=async { 
                getDataFromNetWork()//模拟获取网络数据
            }
            parseData(res.await())  //模拟parse获取到的网络数据
        } catch (exception: Exception) {
            println("Caught $exception in try-catch block")
        }
    }
    Thread.sleep(100)
}
```

`async`方法会返回一个Deferred<T>，Deferred类有一个await()方法，该方法可以等待结果的返回，从而避免回调地狱。

```kotlin
public interface Deferred<out T> : Job {
		public suspend fun await(): T
}
```

async用法还是比较简单的，难点在于async的异常处理。

```kotlin
fun main() {
    val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        println("Top-Level CoroutineExceptionHandler got $exception")
    }
    val coroutineContext = Dispatchers.Default + CoroutineName("ExceptionHandler")+exceptionHandler
    CoroutineScope(coroutineContext).launch{
        try {
            val res=async {
                println("Exception is about to throw")
                throw Exception()
            }
            println("Before exception is thrown")
            res.await()
        } catch (exception: Exception) {
            println("Caught $exception in try-catch block")
        }
    }
    Thread.sleep(100)
}
```

不妨猜猜是CoroutineExceptionHandler的handleException()方法会被调用，还是异常会进到catch块。

<p align="center"><img src="https://private-pirture-storage.oss-cn-hangzhou.aliyuncs.com/img/image-20220913162648987.png"></img></p>

答案是两个都会。

上述代码中有两处会抛异常。

- `res.await()`抛一次，这个异常会被try-catch块处理，只要async{}块里出现了异常，该情况必现。

- async{}块中的`throw Exception()`抛一次，这个异常会被CoroutineExceptionHandler处理，但不一定每次async{}块出现异常，异常都会被CoroutineExceptionHandler处理，下列两种特殊情况异常不会由CoroutineExceptionHandler处理。

  - 协程是顶层协程

    ```kotlin
    fun main() {
        val exceptionHandler = CoroutineExceptionHandler { _, exception ->
            println("Top-Level CoroutineExceptionHandler got $exception")
        }
        val coroutineContext = Dispatchers.Default + CoroutineName("ExceptionHandler")+exceptionHandler
        CoroutineScope(coroutineContext).launch{
            try {
                val res=GlobalScope.async {//发起顶层协程
                    println("Exception is about to throw")
                    throw Exception()
                }
                println("Before exception is thrown")
                res.await()
            } catch (exception: Exception) {
                println("Caught $exception in try-catch block")
            }
        }
        Thread.sleep(100)
    }
    ```

    运行一下。

    <p align="center"><img src="https://private-pirture-storage.oss-cn-hangzhou.aliyuncs.com/img/image-20220913162755543.png"></img></p>

  - 协程在coroutineScope或supervisorScope内发起的。

    这点可查看前面第三部分关于coroutineScope和supervisorScope的介绍。

总结一下，async{}块中的异常一般是在await()方法调用的时候抛出来，但当协程为非顶层协程，或者没有用coroutineScope或supervisorScope将协程包起来的情况，抛出的异常也就会被CoroutineExceptionHandler处理。

### 五.总结

学习协程这么久，个人感觉协程主要的难点在于

- 如何理解协程这一概念。
- 如何进行异常处理。

相比于上面这两点，协程的其他知识点要其实要容易理解得多，本文并没有面面俱到，很多协程的知识点并没有讲到，如`suspend函数`，`runBlocking`，`GlobalScope`，`withContext`，但相信各位读者一定能通过其它各种途径学习到。

希望能对你有帮助，有错欢迎留言。

peace。

### 参考资料

1. [Mastering Kotlin Coroutines In Android - Step By Step Guide](https://blog.mindorks.com/mastering-kotlin-coroutines-in-android-step-by-step-guide)
1. [Kotlin Coroutines in Android Summary](https://proandroiddev.com/kotlin-coroutines-in-android-summary-1ed3048f11c3)
1. [CoroutineExceptionHandler not executed when provided as launch context](https://stackoverflow.com/questions/53576189/coroutineexceptionhandler-not-executed-when-provided-as-launch-context)
1. [Why exception handling with Kotlin Coroutines is so hard and how to successfully master it!](https://www.lukaslechner.com/why-exception-handling-with-kotlin-coroutines-is-so-hard-and-how-to-successfully-master-it/)
1. [kotlin 协程的异常处理](https://zhuanlan.zhihu.com/p/424591901)
1. [kotlin协程async await的异常踩坑以及异常处理的正确姿势](https://blog.csdn.net/yuzhiqiang_1993/article/details/121049744)