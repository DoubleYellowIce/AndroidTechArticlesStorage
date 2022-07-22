# 一.Activity的生命周期
### 1.1 正常生命周期
<div align=center><img  src="https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/cf59ef495aae48b7b75623f593c691c1~tplv-k3u1fbpfcp-watermark.image?"/></div>

稍微熟悉安卓开发的读者应该对上图再了解不过了，但这里还是允许我啰嗦几句。

当Activity被创建时，onCreate()方法会被执行。

当Activity于用户可见时，onStart()方法会被执行。

当Activity正在与用户进行交互时，onResume()方法会被执行。

当Activity不再与用户进行交互时，onPause()方法会被执行。

当Activity于用户不可见时，onStop()方法会被执行。

当Activity正常结束或因为内存不够被系统杀死时，onDestroy()方法会执行，该方法一般用于资源的回收和内存的释放。

如果将这六个方法进行配对的话，onCreate()是与onDestroy()一对，判定标准是Activity当前是处于创建还是销毁状态。

onStart()与onStop()一对，判定标准是Activity当前于用户是否可见。

onResume()和onPause()一对，判定标准是Activity当前是否正在处于前台，与用户进行交互。

需要注意的是Activity跳转的执行顺序，假设现在有两个Activity，Activity a和Activity b，当a跳转到b时，只有当a的onPause()执行完成后，b的onCreate()才会执行，所以在Activity里的onPause()执行的任务应该是轻量级的，onStop()方法可以执行稍微耗时的操作，但仍不应过于重量级。

### 1.2 异常生命周期
上面所说为Activity正常结束的生命周期，下面再来讨论Activity的异常生命周期，那么什么情况下会Activity的生命周期会出现异常？比较常见的有屏幕旋转和后台Activity因内存不够被系统杀死这两种情况。
#### 1.2.1屏幕旋转
先来讨论第一种情况，当屏幕发生旋转时，默认情况下是系统会销毁旧Activity实例，并重新创建新Activity实例。

当系统销毁旧Activity实例时，会调用onSaveInstanceState()去保存旧Activity的状态，旧Activity就会调用PhoneWindow的onSaveInstanceState()，PhoneWindow又会去调用DecorView的onSaveInstanceState()，DecorView又会去遍历子ViewGroup并调用它们的onSaveInstanceState()让其保存各自的状态，如此递归下去到最底层控件，比如说EditText，就会保存用户已经输入的内容，最终所有的状态都会保存到Bundle实例savedInstanceState中。

<div align=center><img  src="https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/de224c837f134d439f23333403c26a88~tplv-k3u1fbpfcp-watermark.image?"/></div>

可以看到，保存旧Activity实例的状态，其实是一个递归调用onSaveInstanceState()的过程，上层的调用者，比如说Activity，PhoneWindow，它们的状态是所用控件的状态总和。

当系统创建新Activity实例时，会将存储旧Activity状态的savedInstanceState作为参数传入onCreate()方法和onRestoreInstanceState()方法中。

onRestoreInstanceState()方法和onSaveInstanceState()一样，也是一个递归调用的过程，这里就不再赘述。

onRestoreInstanceState()方法的调用时机在onStart()方法之后，那么用onRestoreInstanceState()和onCreate()方法恢复旧Activity实例状态之间的区别是什么呢，最大的区别就是savedInstanceState可不可能为空的问题，onCreate()方法的savedInstanceState参数是可能为空的，而当onRestoreInstanceState()被调用时，savedInstanceState参数一定不为空。

需要注意的是，onSaveInstanceState()只有在Activity的生命周期出现异常且系统判定为该Activity会被重新使用的情况下才会被调用，同样是以屏幕旋转为例，尽管旧Activity实例被销毁了，但是新Activity新实例会被立刻重新创建，那么这时候系统就会判定该Activity会被重新使用，应调用onSaveInstaceState()方法去保存状态。

此外，Activity可以设置在翻转时不重新实例化，只需要在AndroidManifest文件中activity配置添加下述代码即可。
```
android:configChanges="orientation"
```
添加完后activity配置如下所示。


<div align=center><img  src="https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/20f3995ca235419b9b6ab995e4aab993~tplv-k3u1fbpfcp-watermark.image?"/></div>


#### 1.2.2 内存不够导致的异常生命周期
Activity的优先级如下所示，越往上优先级越高。

1.此刻正在与用户交互的Activity。

2.此刻于用户可见的Activity。

3.处于后台于用户不可见的Activity。

当内存不够时，系统会先回收低优先级的Activity，当系统判定Activity会被重新使用时，其方法调用情况与屏幕发生旋转时一致，当系统判定Activity不会被重新使用，则只会简单地执行onDestory()等用于资源回收，内存释放的方法，不会保存Activity的状态，这里就不再过多赘述。
# 二.Activity的启动模式
Activity可以通过两种方式来设置启动模式，一种是静态设置，也就是在AndroidManifest文件进行设置，一种是动态设置，通过代码来进行设置。
### 2.1 栈
在讲解设置方法之前，首先要理解一个概念，就是Activity的任务栈，该栈用来存放Activity实例，默认情况下，一个应用会有两个任务栈，前台任务栈和后台任务栈，后台任务栈用来存放已经暂停的Activity实例，用户可以通过切换键将后台栈中的实例切换到前台中。
### 2.2 静态设置
常见的启动模式有standard，singleTop，singleTask，singleInstance这四种模式,下面一一介绍。
#### 2.2.1 standard
当Activity a启动使用standard模式的Activity b时，不管应用中a所在的栈中是否已经存在b的实例，系统都会创建一个实例b，并将其放在a所在的栈中，如下图所示。

<div align=center><img  src="https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/7e866d0e2a314561986c57b30cac1037~tplv-k3u1fbpfcp-watermark.image?"/></div>

该模式比较简单，但需要注意的是，当从非Activity的Context，比如ApplicationContext，跳转到某个
Activity时，系统会报错,这是因为非Activity的Context没有栈。

#### 2.2.2 singleTop
当Activity a启动使用singleTop模式的Activity b时。

当栈顶的实例不是b时，系统才会新建一个实例b，与singleTop模式类似，b实例也会放入启动它的，也就是a所在的栈中。

<div align=center><img  src="https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/fb1a19ee0eef4940bf699e6c23aa3317~tplv-k3u1fbpfcp-watermark.image?"/></div>


当栈顶的实例是b时，系统不会新建一个实例b，而是调用已经存在的实例b的onNewIntent()方法。

<div align=center><img  src="https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/d4c3511d349349788ea3afcccb8ff560~tplv-k3u1fbpfcp-watermark.image?"/></div>

#### 2.2.3 singleTask
讲解该模式之前，需要介绍属性taskAffinity，如下图所示，与launchMode
一样，taskAffinity也是定义在AndroidManifest文件中的Activity属性，它的作用是指定Activity被创建时想被放入的栈，该栈的名字必须有“com. . ”类似于包名的格式，此外，由于应用的默认栈的名字就是包名，该栈名字要和包名不同，不然意义不大。

<div align=center><img  src="https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/35b32962096f42c6bd36689a81f2c12f~tplv-k3u1fbpfcp-watermark.image?"/></div>

该模式只有与singleTask或者allowTaskReparenting一起搭配才会起作用，这是因为singleTop和standard模式的Activity实例会放在启动它的Activity所在的栈中。

当Activity a启动使用singleTask模式的Activity b时，系统会首先寻找b的目的栈是否存在。

如果不存在，就新建一个栈，并将其命名为taskAffinity中指定的名字，后才创建b的实例将其放入该栈中。

<div align=center><img  src="https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/dbff4f67ad8b449896b4cf864c1dd0b1~tplv-k3u1fbpfcp-watermark.image?"/></div>

如果系统存在b指定的栈时，会再寻找该栈中是否存在b的实例，如果存在，会将b上面的Activity全部出栈，让b处于栈顶位置。

<div align=center><img  src="https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ca022253cef7446a86d64c12772418a9~tplv-k3u1fbpfcp-watermark.image?"/></div>

allowTaskReparenting是一个比较特殊的属性，先用一个例子来讲解，假设如今有一个应用a和应用b，应用a中的 Activity c启动应用b中allowTaskReparenting属性为true的Activity d，那么c会进入应用a中c所在的栈中,如下图所示，需要注意的是，此刻应用b并没有启动。
<div align=center><img  src="https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e8e66e414ee5481f9d49fed1948eb910~tplv-k3u1fbpfcp-watermark.image?"/></div>

当应用b启动时，Activity d就会回到应用b的栈中。

#### 2.2.3 singleInstance
采用singleInstance模式的Activity独占一个栈，可以说是singleTask模式的加强版，下面同样以a启动b为例子进行讲解。
当a启动采用singleInstance模式的b时，如果实例b不存在，系统会单独为该Activity创建一个栈，实例化b并将其放入该栈中，如下图所示。

<div align=center><img  src="https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/402584993cfc4f53b3199f3f98c661cd~tplv-k3u1fbpfcp-watermark.image?"/></div>
如果实例b存在，由于b采用的是singleInstance模式，所以b所在栈中必为上次启动b时新建的栈，且栈中只有一个b实例，系统只需调用b的onNewIntent()即可。

### 2.3动态设置
稍微了解过安卓开发的读者都知道，启动Activity时一般是先实例化一个Intent，然后将该Intent实例传入startActivity()方法中，而动态设置就是在启动之前通过addFlags()方法给Intent实例增加标志。
```Kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val intent=Intent(this,SecondActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }
}
```
下面再来简单介绍常见的启动标志。
#### 2.3.1 FLAG_ACTIVITY_NEW_TASK
该启动标志的效果等同于singleTask启动模式。
#### 2.3.2 FLAG_ACTIVITY_SINGLE_TOP
该启动标志的效果等同于singleTop启动模式。
#### 2.3.3 FLAG_ACTIVITY_CLEAR_TOP
具有该启动标志的Activity会将栈里面位于它上方的Activity实例全部出栈，细心的读者可能会发现那这和singleTask模式作用大差不多，是的，所以该标志一般与FLAG_ACTIVITY_NEW_TASK搭配使用，在这种情况下，如果目标Activity实例存在，会调用其的onNewIntent()。

这里需要注意的是，当目标Activity的启动模式是standard时，系统会将目标Activity实例以及位于它上面的Activity实例全部出栈，然后再新建一个Activity实例放入栈中，如下图所示。

<div align=center><img  src="https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/1b84d09801844656823c22828aef2ee4~tplv-k3u1fbpfcp-watermark.image?"/></div>

#### 2.3.4 FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
设置该标志的Activity不会出现在新启动Activity的列表中，作用是可以让用户不从该列表中回到该Activity中。
# 参考资料
1.《Android开发艺术探索》，任玉刚

