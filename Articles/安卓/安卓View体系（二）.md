### 一.View的工作流程
View绘制分为三个步骤measure()，layout()，draw()，分别对应着测量，布局，绘制，下面一一讲解。
### 二.measure阶段
#### View工作流程入口performTraversals()
View工作流程入口在ViewRootImpl中的performTrasversal()方法中，traversal有遍历的意思，而这正是performTrasversal()方法做的事情，依次执行onMeasure()，onLayout()，onDraw()方法，分别在这三个方法里遍历子View，如下为performTrasversal()部分源码。

```java
private void performTraversals() {
    performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
    ...
    performLayout(lp, mWidth, mHeight);
    ...
    performDraw();

}
```
#### MeasureSpec
MeasureSpec源码如下所示。

```java
 public static class MeasureSpec {
        private static final int MODE_SHIFT = 30;
        
        private static final int MODE_MASK  = 0x3 << MODE_SHIFT;
 
        public static final int UNSPECIFIED = 0 << MODE_SHIFT;
    
        public static final int EXACTLY     = 1 << MODE_SHIFT;

        public static final int AT_MOST     = 2 << MODE_SHIFT;
}
```
尽管MeasureSpec是一个类，但其本质是一个32位int。

- 最高2位存储SpecMode。

  SpecMode有三个值UNSPECIFIED，AT_MOST，EXACTLY。

  - UNSPECIFIED是不确定的意思，即父View对子View的大小没有限制，子View想多大就可以多大，不超过屏幕大小即可。

  - AT_MOST是最多的意思，即父View允许子View的大小最大可以是SpecSize的值。

  - EXACTLY是确定的意思，即父View指定子View的大小只能是SpecSize的值。

- 剩下的30位存储SpecSize。

MeasureSpec是父View对子View的布局要求，在这里做一个可能不那么恰当的比喻，MeasureSpec好比是孩子找父亲要钱。

<div align="center"><img src="https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/3f2f51cc942d4d91a5f61c82ab353912~tplv-k3u1fbpfcp-watermark.image?" width="30%" height="30%"></img></div>

- UNSPECIFIED对应的情况是父亲甩给孩子一张银行储蓄卡，“卡号是你生日”，父亲的意思很明确，就是孩子想花多少钱都可以，只要银行储蓄卡里还有钱，这样的父亲请给我来一打谢谢。

  在这里银行储蓄卡对应着着屏幕的大小，换句话说，`只要银行卡里有钱`等同于`只要屏幕还有位置`

<div align="center"><img src="https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/24231e752c6f496fb7cd73869e7c3fc7~tplv-k3u1fbpfcp-watermark.image?" width="30%"></img></div>

- AT_MOST对应的情况是父亲在计算完家里可支配的钱完后，给孩子钱后说，“你最多只能花这么多。”。

- EXACTLY的情况就好比父亲是个霸道总裁，计算完家里可支配的钱完后之余还要求孩子必须刚刚好把这份钱花完，一分不能多花，一分也不能少花，“花不完的就丢了吧”。

  在上面两个比喻中，`家里可支配的钱`是指家里存款减去一切开销如房贷，车贷后所剩余的钱，对应着父View的大小减去上下左右margin和padding等位置开销后仍有剩余的屏幕空间。

#### 获取DecorView的measureSpec
解释完MeasureSpec，再回过头来看看performTraversals()中的代码。
```java
private void performTraversals() {
    performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
    ...
    performLayout(lp, mWidth, mHeight);
    ...
    performDraw();

}
```
传入的performMeasure()方法里的childWidthMeasureSpec和childHeightMeasureSpec是什么，又是怎么计算出来的,答案还是要回到源码中查找。

```java
private void performTraversals() {
    WindowManager.LayoutParams lp = mWindowAttributes;
    ...
    int childWidthMeasureSpec = getRootMeasureSpec(mWidth, lp.width);
    int childHeightMeasureSpec = getRootMeasureSpec(mHeight, lp.height);
}
```
我们在xml文件中的设置的android:layout_width，android：layout_height属性的值最终就会变成LayoutParams。

上述代码中，lp.width和lp.height默认是match_parent，mWidth和mHeight默认是屏幕的宽度和高度。

再来看看getRootMeasureSpec的源代码。

```java
private static int getRootMeasureSpec(int windowSize, int rootDimension) {
    int measureSpec;
    switch (rootDimension) {

    case ViewGroup.LayoutParams.MATCH_PARENT:
        // Window can't resize. Force root view to be windowSize.
        measureSpec = MeasureSpec.makeMeasureSpec(windowSize, MeasureSpec.EXACTLY);
        break;
    case ViewGroup.LayoutParams.WRAP_CONTENT:
        // Window can resize. Set max size for root view.
        measureSpec = MeasureSpec.makeMeasureSpec(windowSize, MeasureSpec.AT_MOST);
        break;
    default:
        // Window wants to be an exact size. Force root view to be that size.
        measureSpec = MeasureSpec.makeMeasureSpec(rootDimension, MeasureSpec.EXACTLY);
        break;
    }
    return measureSpec;
}
```
可以看到getRootMeasureSpec()根据大小是否可以变化来返回相应的measureSpec。

#### performMeasure()
再回到performMeasure()方法里。
```java
private void performMeasure(int childWidthMeasureSpec, int childHeightMeasureSpec) {
    if (mView == null) {
        return;
    }
    Trace.traceBegin(Trace.TRACE_TAG_VIEW, "measure");
    try {
      	//1
        mView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    } finally {
        Trace.traceEnd(Trace.TRACE_TAG_VIEW);
    }
}
```
代码1处的measure()方法一般情况下又会调用onMeasure()方法，measure()源码太长就不贴出来了，感兴趣的小伙伴可以自行查看。

#### FrameLayout::onMeasure()
onMeasure()在ViewGroup中没有实现，只有在特定的布局里有实现。


mView是指DecorView，DecorView是一个FrameLayout，所以我们再去看看FrameLayout的onMeasure()方法。

喜大普奔，FrameLayout的onMeasure()源码非常简单明了。
```java
@Override
protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    //获取子View数量
    int count = getChildCount();
    //看长和宽的SpecMode是否是MeasureSpec.EXACTLY
    final boolean measureMatchParentChildren =
            MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY ||
            MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;
    mMatchParentChildren.clear();

    int maxHeight = 0;
    int maxWidth = 0;
    int childState = 0;

    for (int i = 0; i < count; i++) {
        //遍历子View
        final View child = getChildAt(i);
        if (mMeasureAllChildren || child.getVisibility() != GONE) {
        //如果要求测量所有子View或者子View为可见的状态
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            //lp为xml文件的match_Parent,wrap_Content,margin之类的属性
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            //记录最大宽度
            maxWidth = Math.max(maxWidth,
                    child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
            //记录最大高度
            maxHeight = Math.max(maxHeight,
                    child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
            childState = combineMeasuredStates(childState, child.getMeasuredState());
            if (measureMatchParentChildren) {
            //如果子View有属性要求match_parent
                if (lp.width == LayoutParams.MATCH_PARENT ||
                        lp.height == LayoutParams.MATCH_PARENT) {
                    mMatchParentChildren.add(child);
                }
            }
        }
    }

    // Account for padding too
    maxWidth += getPaddingLeftWithForeground() + getPaddingRightWithForeground();
    maxHeight += getPaddingTopWithForeground() + getPaddingBottomWithForeground();

    // Check against our minimum height and width
    maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
    maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

    // Check against our foreground's minimum height and width
    final Drawable drawable = getForeground();
    if (drawable != null) {
        maxHeight = Math.max(maxHeight, drawable.getMinimumHeight());
        maxWidth = Math.max(maxWidth, drawable.getMinimumWidth());
    }

    setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
            resolveSizeAndState(maxHeight, heightMeasureSpec,
                    childState << MEASURED_HEIGHT_STATE_SHIFT));
    
    count = mMatchParentChildren.size();
    if (count > 1) {
    //对子View为match_parent进行重新测量，原因详见下文
        for (int i = 0; i < count; i++) {
            final View child = mMatchParentChildren.get(i);
            final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

            final int childWidthMeasureSpec;
            if (lp.width == LayoutParams.MATCH_PARENT) {
                final int width = Math.max(0, getMeasuredWidth()
                        - getPaddingLeftWithForeground() - getPaddingRightWithForeground()
                        - lp.leftMargin - lp.rightMargin);
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                        width, MeasureSpec.EXACTLY);
            } else {
                childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                        getPaddingLeftWithForeground() + getPaddingRightWithForeground() +
                        lp.leftMargin + lp.rightMargin,
                        lp.width);
            }

            final int childHeightMeasureSpec;
            if (lp.height == LayoutParams.MATCH_PARENT) {
                final int height = Math.max(0, getMeasuredHeight()
                        - getPaddingTopWithForeground() - getPaddingBottomWithForeground()
                        - lp.topMargin - lp.bottomMargin);
                childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                        height, MeasureSpec.EXACTLY);
            } else {
                childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                        getPaddingTopWithForeground() + getPaddingBottomWithForeground() +
                        lp.topMargin + lp.bottomMargin,
                        lp.height);
            }

            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
    }
}
```
该方法的作用就是遍历子View并调用其measureChildWithMargins()，代码里注释较详细，这里就不再赘述。

值得一提的是，上述代码中会在父View(也就是方法的执行者)的Spec_Mode为EXACTLY的时候，对属性为match_parent的子View进行特殊照顾，重新测量，该点不是本文的终点，所以由于篇幅问题，我将原因放在了文章末尾，详情可看附录ForeGroundPadding解释。

#### measureChildWithMargins()
在上述代码中measureChildWithMargins()是核心方法。

```java
protected void measureChildWithMargins(View child,
        int parentWidthMeasureSpec, int widthUsed,
        int parentHeightMeasureSpec, int heightUsed) {
    final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
    //mPaddingLeft + mPaddingRight是父View的内边距
    //lp.leftMargin + lp.rightMargin是子View的外边距
    //widthUsed是已经使用了的宽度
    final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
            mPaddingLeft + mPaddingRight + lp.leftMargin + lp.rightMargin
                    + widthUsed, lp.width);
    final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
            mPaddingTop + mPaddingBottom + lp.topMargin + lp.bottomMargin
                    + heightUsed, lp.height);

    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
}
```
该方法又调用了getChildMeasureSpec()方法，如名字所示，该方法就是为了测量子View的MeasureSpec。
```java
public static int getChildMeasureSpec(int spec, int padding, int childDimension) {
    int specMode = MeasureSpec.getMode(spec);
    int specSize = MeasureSpec.getSize(spec);

    int size = Math.max(0, specSize - padding);

    int resultSize = 0;
    int resultMode = 0;

    switch (specMode) {
    // Parent has imposed an exact size on us
    case MeasureSpec.EXACTLY:
        if (childDimension >= 0) {
            //1
            resultSize = childDimension;
            resultMode = MeasureSpec.EXACTLY;
        } else if (childDimension == LayoutParams.MATCH_PARENT) {
            // Child wants to be our size. So be it.
            resultSize = size;
            resultMode = MeasureSpec.EXACTLY;
        } else if (childDimension == LayoutParams.WRAP_CONTENT) {
            // Child wants to determine its own size. It can't be
            // bigger than us.
            resultSize = size;
            resultMode = MeasureSpec.AT_MOST;
        }
        break;

    // Parent has imposed a maximum size on us
    case MeasureSpec.AT_MOST:
        if (childDimension >= 0) {
            //2
            // Child wants a specific size... so be it
            resultSize = childDimension;
            resultMode = MeasureSpec.EXACTLY;
        } else if (childDimension == LayoutParams.MATCH_PARENT) {
            //4
            // Child wants to be our size, but our size is not fixed.
            // Constrain child to not be bigger than us.
            resultSize = size;
            resultMode = MeasureSpec.AT_MOST;
        } else if (childDimension == LayoutParams.WRAP_CONTENT) {
            //5
            // Child wants to determine its own size. It can't be
            // bigger than us.
            resultSize = size;
            resultMode = MeasureSpec.AT_MOST;
        }
        break;

    // Parent asked to see how big we want to be
    case MeasureSpec.UNSPECIFIED:
        if (childDimension >= 0) {
            //3
            // Child wants a specific size... let him have it
            resultSize = childDimension;
            resultMode = MeasureSpec.EXACTLY;
        } else if (childDimension == LayoutParams.MATCH_PARENT) {
            // Child wants to be our size... find out how big it should
            // be
            resultSize = View.sUseZeroUnspecifiedMeasureSpec ? 0 : size;
            resultMode = MeasureSpec.UNSPECIFIED;
        } else if (childDimension == LayoutParams.WRAP_CONTENT) {
            // Child wants to determine its own size.... find out how
            // big it should be
            resultSize = View.sUseZeroUnspecifiedMeasureSpec ? 0 : size;
            resultMode = MeasureSpec.UNSPECIFIED;
        }
        break;
    }
    //noinspection ResourceType
    return MeasureSpec.makeMeasureSpec(resultSize, resultMode);
}
```
可以看到测量的核心是**结合父view的measureSpec和子view的LayoutParameter计算子View的measureSpec**。

上面代码还是比较简单好理解的，这里挑几个点讲讲。

- 当childDimension >= 0（可见代码1，2，3处），该代码意思就是xml文件中的layout_width或layout_height指定了具体值，既然都指定了具体值了，那就没必要测量了，Spec_Size就是指定的值，Spec_Mode就是EXACTLY。

- 敲黑板，下面是重点来了。

  当父View的Spec_Mode为AT_MOST时，不管子View是Match_Parent还是Wrap_Content，最终获取到Measure_Spec都是一样的。

  原因也很简单，假设父View最大只能为X。

  - 当子View是Match_Parent时，父View的大小都不确定，那么子View的Spec_Size最大只能是X，Spec_Mode只能是AT_MOST了。
  - 当子View是Wrap_Content，虽说是Wrap_Content，但指不定子View有多大呢，所以Spec_Size最大也只能是X，Spec_Mode也只能是AT_MOST了。

  这里再以孩子找父亲要钱为例，父亲跟孩子说“我现在没钱，等我发工资先吧，但我保底工资加上奖金最多也就3200”

  - Match_Parent对应的情况是父亲工资发多少，孩子要多少，这种情况下孩子最多能要到3200。
  - Wrap_Content对应的情况是，孩子想买一台手机，尽管指不定孩子想买的手机有多贵，手机最贵也只能3200块。

不同的ViewGroup有自己onMeasure()实现方法，这里就不再一一分析。

#### View::onMeasure()
下面再来看看View的onMeasure()方法。
```java
protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
            getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
}
```
前面getChildMeasureSpec()方法中如果此刻遍历的child是view，那么就会在最后一行就会调用该view的measure()方法，然后measure()方法再调用onMeasure()方法。
```java
protected int getSuggestedMinimumWidth() {
    return (mBackground == null) ? mMinWidth : max(mMinWidth, mBackground.getMinimumWidth());
}
```
getSuggestedMinimumWidth()比较简单。

- 当有背景时，就取背景的最小宽度和mMinWidth中的最大那个。
- 当没有背景时，就取mMinWidth。

```java
public static int getDefaultSize(int size, int measureSpec) {
    int result = size;
    int specMode = MeasureSpec.getMode(measureSpec);
    int specSize = MeasureSpec.getSize(measureSpec);

    switch (specMode) {
    case MeasureSpec.UNSPECIFIED:
        result = size;
        break;
    case MeasureSpec.AT_MOST:
    case MeasureSpec.EXACTLY:
        result = specSize;
        break;
    }
    return result;
}
```
getDefaultSize()也比较简单，只有Spec_Mode为UNSPECIFIED，size才会取getSuggestedMinimumWidth()中获取到的值，否则size取Spec_Size中的值。

原因也可以大胆猜测一下，当Spec_Mode为UNSPECIFIED时，Spec_Size并没有什么意义了，此刻大小还不如采用建议的值。

总结一下，measure阶段的调用序列如下图所示。


![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/a369aa6c75f7439da37ba3df1a92701e~tplv-k3u1fbpfcp-watermark.image?)

### 三.Layout()
#### View::layout()

```java
@SuppressWarnings({"unchecked"})
public void layout(int l, int t, int r, int b) {
    if ((mPrivateFlags3 & PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT) != 0) {
        onMeasure(mOldWidthMeasureSpec, mOldHeightMeasureSpec);
        mPrivateFlags3 &= ~PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT;
    }


    int oldL = mLeft;
    int oldT = mTop;
    int oldB = mBottom;
    int oldR = mRight;
    
    boolean changed = isLayoutModeOptical(mParent) ?
            setOpticalFrame(l, t, r, b) : setFrame(l, t, r, b);
    //位置是否发生改变或者强制重新绘制
    if (changed || (mPrivateFlags & PFLAG_LAYOUT_REQUIRED) == PFLAG_LAYOUT_REQUIRED) {
        onLayout(changed, l, t, r, b);

        if (shouldDrawRoundScrollbar()) {
            if(mRoundScrollbarRenderer == null) {
                mRoundScrollbarRenderer = new RoundScrollbarRenderer(this);
            }
        } else {
            mRoundScrollbarRenderer = null;
        }

        mPrivateFlags &= ~PFLAG_LAYOUT_REQUIRED;

        ListenerInfo li = mListenerInfo;
        if (li != null && li.mOnLayoutChangeListeners != null) {
            ArrayList<OnLayoutChangeListener> listenersCopy =
                    (ArrayList<OnLayoutChangeListener>)li.mOnLayoutChangeListeners.clone();
            int numListeners = listenersCopy.size();
            for (int i = 0; i < numListeners; ++i) {
                listenersCopy.get(i).onLayoutChange(this, l, t, r, b, oldL, oldT, oldR, oldB);
            }
        }
    }
}    
```
由于view的layout()方法源码较多，我就只贴了核心部分。

代码逻辑是首先调用setFrame()或者setLayoutFrame()来判断View的位置是否发生改变，如果发生改变或者标志位为强制重新绘制，就调用onLayout()方法， view的onLayout()方法如下所示，是预留方法，由子类复写。
```java
protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
}
```
#### ViewGroup::layout()方法。

```java
@Override
public final void layout(int l, int t, int r, int b) {
    if (!mSuppressLayout && (mTransition == null || !mTransition.isChangingLayout())) {
        if (mTransition != null) {
            mTransition.layoutChange(this);
        }
        super.layout(l, t, r, b);
    } else {
        // record the fact that we noop'd it; request layout when transition finishes
        mLayoutCalledWhileSuppressed = true;
    }
}
```
如上图所示，ViewGroup的layout()又调用了View的layout()，而View的layout()又会调用ViewGroup的onLayout()，该方法代码如下所示，是个抽象方法，由不同的ViewGroup实现。
```java
@Override
protected abstract void onLayout(boolean changed,int l, int t, int r, int b);
```
由于DecorView是FrameLayout，那就查找下FrameLayout的代码。
```java
@Override
protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    layoutChildren(left, top, right, bottom, false /* no force left gravity */);
}


```
FrameLayout的onLayout()方法调用了layoutChildren()方法。

```java
void layoutChildren(int left, int top, int right, int bottom, boolean forceLeftGravity) {
    final int count = getChildCount();

    final int parentLeft = getPaddingLeftWithForeground();
    final int parentRight = right - left - getPaddingRightWithForeground();

    final int parentTop = getPaddingTopWithForeground();
    final int parentBottom = bottom - top - getPaddingBottomWithForeground();

    for (int i = 0; i < count; i++) {
        final View child = getChildAt(i);
        if (child.getVisibility() != GONE) {
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            final int width = child.getMeasuredWidth();
            final int height = child.getMeasuredHeight();

            int childLeft;
            int childTop;

            int gravity = lp.gravity;
            if (gravity == -1) {
                gravity = DEFAULT_CHILD_GRAVITY;
            }

            final int layoutDirection = getLayoutDirection();
            final int absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
            final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

            switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                case Gravity.CENTER_HORIZONTAL:
                    childLeft = parentLeft + (parentRight - parentLeft - width) / 2 +
                    lp.leftMargin - lp.rightMargin;
                    break;
                case Gravity.RIGHT:
                    if (!forceLeftGravity) {
                        childLeft = parentRight - width - lp.rightMargin;
                        break;
                    }
                case Gravity.LEFT:
                default:
                    childLeft = parentLeft + lp.leftMargin;
            }

            switch (verticalGravity) {
                case Gravity.TOP:
                    childTop = parentTop + lp.topMargin;
                    break;
                case Gravity.CENTER_VERTICAL:
                    childTop = parentTop + (parentBottom - parentTop - height) / 2 +
                    lp.topMargin - lp.bottomMargin;
                    break;
                case Gravity.BOTTOM:
                    childTop = parentBottom - height - lp.bottomMargin;
                    break;
                default:
                    childTop = parentTop + lp.topMargin;
            }

            child.layout(childLeft, childTop, childLeft + width, childTop + height);
        }
    }
}
```

可以看到该方法与onMeasure()方法类似，都是遍历子View，调用子View的方法，只不过是onMeasure()调用的是子View的measure()，layoutChildren()调用的是子View的layout()。

总结一下，layout阶段的方法调用顺序如下所示。

![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/bb4bd19ca9cd49e6b6a485b2ec451198~tplv-k3u1fbpfcp-watermark.image?)
### 四.draw()
ViewRootImpl的performDraw()方法最终会调用到DecorView的draw()方法，DecorView()没有复写父类View的draw()方法，所以调用实际上是View的draw()。

View的draw()方法有以下几个步骤，但源码比较长，这里不再贴出来了。

1. 绘制背景。
2. 保存canvas。

3. 调用onDraw()方法。

4. 绘制子View。该功能主要由dispatchDraw()方法完成，该方法会遍历子View并调用其onDraw()，

5. 绘制ScrollBar
6. 绘制褪色效果。

需要注意的是，onDraw()方法和dispatchDraw()方法在View里都是个空实现，且FrameLayout()对这两个方法都没有进行复写，而FrameLayout()的直接父类ViewGroup只复写了dispatchDraw()，所以调用的实际上是ViewGroup的dispatchDraw()和View的onDraw()。

draw()阶段的绘制流程如下所示。

![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/f95bbf2f5de041dabbc38fddf752d34e~tplv-k3u1fbpfcp-watermark.image?)

### 附录
#### ForeGroundPadding解释
源码中也出现getPaddingLeftWithForeground()，getPaddingTopWithForeground()，getPaddingRightWithForeground()，getPaddingBottomWithForeground()等方法。

不是，getPadding()我知道，获取内边距嘛，foreground是什么东西，getPaddingWithForeground()又是个什么东西?

<div align="center"><img src="https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/16c0ec5e289a417c88c273c6c4e55082~tplv-k3u1fbpfcp-watermark.image?" width="30%" height="50%"></img></div>

随便打开个getPaddingLeftWithForeground()方法一看。
```java
int getPaddingLeftWithForeground() {
    return isForegroundInsidePadding() ? Math.max(mPaddingLeft, mForegroundPaddingLeft) :
        mPaddingLeft + mForegroundPaddingLeft;
}
```
还好，源码还是比较好理解的，稍微改改应该就可以放上高中英语试卷了，有小伙伴可能会好奇，为什么不可以放上大学英语试卷，也不为什么，就因为我还本科在读，等我毕业了，爱咋咋地，放在考研卷也不关我事。

<div align="center"><img src="https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/bc192b115d4d4ab1a01c796141f710ac~tplv-k3u1fbpfcp-watermark.image?" width="30%" height="50%"></img></div>

扯远了，回到正题上，从源码可以看到mForegroundPaddingLeft说白了也是个padding，getPaddingLeftWithForeground()方法的意思是，如果mForegroundPaddingLeft在padding内，就取两者的最大值，如果不在，就取两者的和。

再回想一下什么时候需要对属性为match_parent的子View进行特殊照顾，是在父View的Spec_Mode为EXACTLY的时候，哎，破案了。

子View想充满父View，那么子View的大小就等于父View大小减去父View的Padding值，但是父View的大小是确切的，且有两个Padding，所以必须要好好计算一下两个Padding最终占有的长度大小才能计算子View的长度。

同样以孩子找父亲要钱为比喻，孩子想要父亲所有钱，但目前父亲自己也有两笔需要开销的钱，不能全给孩子，一笔是娱乐的钱，一笔是烟酒的钱，父亲要先计算娱乐的钱的包不包括烟酒的钱，如果包括，那就自己所需要的钱取两者最大那笔就好，如果不包括，那就是二者之和，最终孩子获得的钱就是父亲所拥有的钱减去父亲需要开销的钱。

### 参考资料
1. 《Android进阶之光》第一版，刘望舒