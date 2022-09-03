### 一.View与ViewGroup
View类是安卓UI界面的根基，常见的View有TextView和EditText。

ViewGroup类继承于View类，可以把ViewGroup理解成一个容器，里面存放着ViewGroup和View，常见的ViewGroup有LinearLayout，FrameLayout和RelativeLayout。
### 二.安卓坐标体系
如下图所示，在安卓系统坐标体系中，屏幕左上角为原点，横轴为X轴，竖轴为Y轴。
<div align=center><img  src="https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/f8d91e9e68a04a9cb2a3d518c79629ac~tplv-k3u1fbpfcp-watermark.image?"/></div>

#### View
如下图所示，View类有四个成员mLeft，mRight，mBottom，mTop，这四个成员分别记录着该View相对于父容器的距离。
<div align=center><img  src="https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/6b394db487634d17aeb5126fdb33dd49~tplv-k3u1fbpfcp-watermark.image?" width="50%" height="50%"></div>

那么View的长度不就等于mRight-mLeft，宽度不就等于mBottom-mTop？

查看源码就会发现View的确是通过这种方式来计算的长度和宽度
```java
@UiThread
public class View implements Drawable.Callback, KeyEvent.Callback,
        AccessibilityEventSource{
    protected int mLeft;

    protected int mRight;
    
    protected int mTop;

    protected int mBottom;

    @ViewDebug.ExportedProperty(category = "layout")
    public final int getWidth() {
    return mRight - mLeft;
    }

    @ViewDebug.ExportedProperty(category = "layout")
    public final int getHeight() {
    return mBottom - mTop;
    }

}
```
#### MotionEvent
在安卓系统中，用户点击屏幕的操作叫点击事件，该点击事件的所有信息，比如说位置，会被包装成MotionEvent实例。

可以通过MotionEvent的getX()，getY()获取点击位置到控件的距离，getRawX()和getRawY()获取点击位置到屏幕的距离，下图所示即为用户点击了ImageView控件的某个位置的示意图。
<div align=center><img  src="https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/320e249751d1489d9eee6678370a1873~tplv-k3u1fbpfcp-watermark.image?" width="50%" height="50%"></div>

### 三.View滑动
MotionEvent总共有三个状态。

- up，代表用户的手指离开屏幕。
- down，代表用户的手指点击屏幕。
- move，代表用户的手指在屏幕上滑动。

一个点击事件总是由一个down开始，中间伴随着零个或者多个move，以up结束。

View滑动尽管有多种方法实现，但其核心都是在down时记录坐标，在move时计算偏移量，更改View的坐标，同时记录此刻坐标以便下次move时再次计算偏移量。

#### layout()实现View滑动
先来自定义一个View，新建CustomView继承于View。
```Kotlin
class CustomView:View {

    private var lateX: Float=0f

    private var lateY: Float=0f

    constructor(context: Context):super(context)

    constructor(context: Context,attrs: AttributeSet) : super(context,attrs)

    constructor(context: Context,attrs: AttributeSet,defStyleAttr:Int):super(context,attrs,defStyleAttr)

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val x=event?.x
        val y=event?.y
        when(event?.action){
            MotionEvent.ACTION_DOWN->{
                //记录down时坐标
                lateX=x!!
                lateY=y!!
            }
            MotionEvent.ACTION_MOVE->{
                //计算偏移量
                val offsetX=x!!-lateX
                val offSetY=y!!-lateY
                layout((left+offsetX).toInt(), (top+offSetY).toInt(),(right+offsetX).toInt(),(bottom+offSetY).toInt())
            }
        }
        return true
    }
}
```
修改activity_main.xml文件，把CustomView绘制成80 * 80 的黑色小方块。
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <com.example.demoapplication.CustomView
        android:layout_width="80dp"
        android:layout_height="80dp"
        android:background="@color/black"
        >
    </com.example.demoapplication.CustomView>


</androidx.constraintlayout.widget.ConstraintLayout>
```
启动程序。

<div align=center><img  src="https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/6167275232bf497285801934dfc96afa~tplv-k3u1fbpfcp-watermark.image?" height="20%" width="20%"></div>

可以看见，黑色小方块会随着手指(虚拟机上是鼠标)移动。

需要注意的是，layout方法的参数顺序是根据顺时针方向来确定的，从左至右依次是left，top，right，bottom，而不是先左右后上下的方向，输入参数时不注意顺序，可能会出现各种问题。
#### offsetLeftAndRight()和offsetTopAndBottom()实现View滑动
使用该方法和layout()方法的区别在于onTouchEvent()中。
```kotlin
class CustomView:View {
...
    constructor(context: Context,attrs: AttributeSet,defStyleAttr:Int):super(context,attrs,defStyleAttr)

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val x=event?.x
        val y=event?.y
        when(event?.action){
            MotionEvent.ACTION_DOWN->{
                //记录down时坐标
                lateX=x!!
                lateY=y!!
            }
            MotionEvent.ACTION_MOVE->{
                //计算偏移量
                val offsetX=x!!-lateX
                val offSetY=y!!-lateY
                offsetLeftAndRight(offsetX.toInt())
                offsetTopAndBottom(offSetY.toInt())
            }
        }
        return true
    }
}
```
可以看到，offset()方法仍然需要计算偏移量，与layout()方法相比，其参数比较简单明了，x轴的偏移量就是左右方向上的偏移量，与offsetLeftAndRight()方法的名字匹配，y轴的偏移量就是上下方向的偏移量，与offsetTopAndBottom()方法的名字匹配，不会存在混淆的问题。

#### layoutParams实现View滑动
该实现方案通过修改layoutParams的leftMargin和rightMargin来达到View滑动的目的，和前面两个方法大同小异。

```kotlin
class CustomView:View {

    ...
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val x=event?.x
        val y=event?.y
        when(event?.action){
            MotionEvent.ACTION_DOWN->{
                //记录down时坐标
                lateX=x!!
                lateY=y!!
            }
            MotionEvent.ACTION_MOVE->{
                //计算偏移量
                val offsetX=x!!-lateX
                val offSetY=y!!-lateY
                val p=layoutParams as LinearLayout.LayoutParams
                p.leftMargin= (left+offsetX).toInt()
                p.topMargin=(top+offSetY).toInt()
                layoutParams=p
            }
        }
        return true
    }
}
```
### 四.事件分发
#### 包含关系
在讲View的点击事件传递关系之前，要讲一下，Activity，PhoneWindow，DecorView等参与者之间的关系。

<div align=center><img  src="https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/83149b2baa0d45568046ff59310f059c~tplv-k3u1fbpfcp-watermark.image?" height="50%" width="50%"></div>

<div align="center">图片取自《Android进阶之光》</div>

上图就很好地描述了事件传递流程的参与者之间的关系。

- Activity持有PhoneWindow实例。
- PhoneWindow实例持有DecorView实例。
- DecorView实例有两个子View。
  - TitleView。
  - ContentView。setContentView()大家应该不陌生，该方法通过将传入的ViewGroup设置为DecorView的子View来达到设置Activity的界面UI的目的。

#### 核心方法

- onTouchEvent()。前面实现View滑动的三个实现方案都重写了onTouchEvent()，该方法返回一个boolean值。
  - 当返回true时，该返回值表示该View会消费该点击事件。
  - 当返回false的时候，表示该方法不消费，或无法处理该点击事件。

- dispachTouchEvent()的作用是分发点击事件，与onTouchEvent()类似，当其返回true时则表示其消费了该事件。

- interceptTouchEvent()的作用是判断是否拦截事件，同样的，

  - 当其返回true时则表示该View会拦截该点击事件。
  - 当其返回false时则表示不进行拦截该点击事件。

  需要注意的是，与onTouchEvent()和dispachTouchEvent()不同的是，interceptTouchEvent()只有在ViewGroup中有实现，且该实现默认返回false。

这三个方法是点击事件分发的核心，他们三者的关系可以简单的表现为下述的代码。
```java
public boolean dispatchTouchEvent(TouchEvent te){
    boolean res=false;//res表示是否消费该点击事件
    if(interceptTouchEvent(te)){
        //拦截该事件
        res=onTouchEvent();
    }else{
        //不拦截，交给子View去处理
        res=child.dipatchTouchEvent(te)
    }
    return res;
}
```
需要注意的是，前面提到过，View是没有interceptTouchEvent()方法的，当它的dipatchTouchEvent()被调用时，会默认调用onTouchEvent()方法。
#### 传递方向
当用户点击屏幕时

1. 该点击事件首先会被包装成TouchEvent传递给Activity。
2. Activity再向下传递给PhoneWindow。
3. PhoneWindow再传递给DecorView。
4. DecorView再传递给子ViewGroup。
5. 子ViewGroup再传递给子View。

需要注意的有两点

- 传递点击事件是指调用dispatchTouchEvent()方法。

- 上述传递链不一定会完成，该点击事件一旦被消费就不会继续往下传了。

那么当底层View无法处理该点击事件时，此时点击事件的传递方向就会由向下传递改为向上传递。

1. 该底层View就会让父View来处理该点击事件。
2. 当父View都处理不了该点击事件时，该点击事件就会转交给DecorView处理。
3. DecorView处理不了就转交给PhoneWindow。
4. PhoneWindow处理不了就转交给Activity去处理。

这种先向下后向上的事件传递方向在《Android进阶之光》中有一个很形象的比喻，这里我用自己的话简单复述一遍。

在金庸小说《倚天屠龙记》中，当有外敌来武当派挑衅时，

1. 该消息首先会通知到武当派掌门人张三丰，张三丰什么身份，肯定不会轻易出面，那么张三丰就会让武当七侠中一个去应战。
2. 武当七侠虽不是掌门，但也好歹是江湖名流，也大抵不肯出场，但掌门有令，只好让手下弟子去应战。
3. 手下弟子没有人可以命令，只能硬着头皮自己上。

这就好比点击事件往下传递的方向。

- 当弟子发现敌人比较菜，是自己能应付的，解决完敌人后就会给师父汇报（对应onTouchEvent()返回true），师傅再给张三丰汇报（对应dispatchTouchEvent()方法返回true）。

- 当弟子发现来敌太强，不是自己一个小小的弟子能应付的，就会汇报给师父，这时武当七侠就不得不出面了。

  - 如果武当七侠之一发现自己能应付，就会解决掉来敌后汇报张三丰（对应dispatchTouchEvent()方法返回true）。

  - 同样，如果武当七侠之一也不能解决来敌（对应dispatchTouchEvent()返回false），张三丰就不得不出面了。

    当然了，张三丰是武当派最后的门面，能应付来敌那是最好不过，不能应付怕是武当派从此就要从江湖消失了，这里张三丰对应着安卓系统中的Activity角色，当连Activity也不能处理点击事件的时候，该点击事件就不会被处理。

#### onTouchEvent()细节
需要注意的是，View中有两个boolean属性longClickable和clickable。

- longClickable是指该View长按是否有响应。
- clickable是指该View点击是否有响应。

这两个属性影响着onTouchEvent()的返回值。

- 只有当longClickable和clickable都为false时，onTouchEvent()才会返回false。
- 其余情况均会返回true，那么longClickable和clickable的值受什么因素影响呢？
  - longClickable属性是默认为false的。
  - clickable要分情况，
    - 如果像是Button之类等设计目的就是为了响应点击事件的View，clickable默认为true。
    - 如果像是TextView之类等设计目的更多是为了展示内容的View的clickable默认为false。

此外，当点击事件传递到View时，被调用的顺序是先View的onTouchListener，再到onTouchEvent，最后在onTouchEvent()里才会调用onClickListener，也就是说我们平时设置的onClickListener的优先级其实是最低的。

### 参考资料

1. 《Android进阶之光》第一版，刘望舒
2. 《Android开发艺术探索》，任玉刚