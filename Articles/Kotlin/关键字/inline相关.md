inline相关的关键字有3个，inline，crossinline，noninline。

### inline

inline关键字主要用途是在使用lambda参数的同时减少内存占用和方法调用。

#### 优点

##### 减少代码调用次数

参考资料1提供了一个小demo。

```kotlin
inline fun execute(action: () -> Unit) {
    action()
}

fun main() {
    execute {
        print("Hello ")
        print("World")
    }
}
```

上述代码就等同于。

```kotlin
fun main() {
    print("Hello ")
    print("World")
}
```

对上述代码编译得到的class文件进行反编译获取相对应的[java文件](InlineDemoKt.java)，下面是部分代码片段。

```java
public final class InlineDemoKt
{
    ..
    public static final void main() {
        final int $i$f$execute = 0;
        final int n = 0;
      	//1
        System.out.print((Object)"Hello ");
        System.out.print((Object)"World");
    }
}

```

可以看到1处代码并没有进行**方法的调用**。

##### 减少内存占用

下面再来验证inline关键字是如何减少内存占用的，把上述代码的inline关键字去掉。

```kotlin
fun execute(action: () -> Unit) {
    action()
}
```

检查编译过生成的文件，会发现多了一个class文件，反编译得到相对应的[java文件](InlineDemoKt$main$1.java)，下面是部分代码片段。

```kotlin
static final class InlineDemoKt$main$1 extends Lambda implements Function0<Unit> {
    public static final InlineDemoKt$main$1 INSTANCE;
    
    public final void invoke() {
        System.out.print((Object)"Hello ");
        System.out.print((Object)"World");
    }
    
    static {
        InlineDemoKt$main$1.INSTANCE = new InlineDemoKt$main$1();
    }
}
```

可以看见lambda参数被包裹进了InlineDemoKt$main$1类的**非静态**invoke()方法里，那么调用lambda参数之前，就必须先实例化InlineDemoKt$main$1类，这一步的操作则会增加内存消耗。

#### 应用场景

inline函数适用于有lambda参数的函数中，避免编译器为每个lambda参数自动创建类并实例化，在普通函数里，并没有明显的优势。

### crossinline

在kotlin中，普通的lambda是不允许直接使用return关键字的。

```kotlin
fun execute(action: () -> Unit) {
    action()
}

fun main() {
    execute {
        print("Hello ")
        print("World")
        return//1
    }
}
```

代码1处这种返回方式叫做non-local *return*s，使用外部（non-local）函数的return关键字使得本地（local）函数返回，终止运行，是无法通过编译的。

而当execute()方法添加inline关键字后，就可以编译通过了。

```kotlin
inline fun execute(action: () -> Unit) {
    action()
}

fun main() {
    execute {
        print("Hello ")
        print("World")
        return//1
    }
}
```

因为上述代码等同于下面代码。

```kotlin
fun main() {
    print("Hello ")
    print("World")
    return
}
```

使用本部（local）函数的return关键字使得本地（local）函数返回，当然是可行的。

再来将上述代码改一改。

```kotlin
inline fun execute(action: () -> Unit) {
    anotherExecute {action()}//1
}

fun anotherExecute(action: () -> Unit) {
    action()
}

fun main() {
    execute {
        print("Hello ")
        print("World")
        return
    }
}
```

上述代码是无法通过编译的，因为其等同于下面代码。

```kotlin
fun anotherExecute(action: () -> Unit) {
    action()
}

fun main() {
    anotherExecute {
        print("Hello ")
        print("World")
        return//1
    }
}
```

这违背了上文提到的non-local return准则。

需要注意的是，即时将代码1处删除掉，也无法通过编译，当确实需要像上述代码的方式一样使用lambda，且确定没有存在违背non-local return准则的代码的时候，就可以使用**crossinline**关键字修饰action参数，这个关键字可以简单理解为告诉编译器lambda没有违背non-local return准则的代码，请放心编译。

```kotlin
inline fun execute(crossinline action: () -> Unit) {
    anotherExecute { action()}
}
```

### noninline

用inline关键字修饰过的函数其所有lambda参数都不会被包裹成类的非静态方法，而noninline关键字就是指定某个lambda参数，让其被包裹成类的非静态方法。

修改上面的代码。

```kotlin
inline fun execute(action: () -> Unit,noinline anotherAction:()->Unit) {
    action()
    anotherAction()
}

fun main() {
    execute({
        print("Hello ")
        print("World")
    },{
        print("Hello ")
        print("AnotherWorld")
    })
}
```

会获取到两个class文件，分别反编译获取其java文件，下面是第一个lambda参数所对应的[java代码](InlineDemoKt 2.java)。

```java
public final class InlineDemoKt
{
    ..
    public static final void main() {
        final Function0 anotherAction$iv = (Function0)InlineDemoKt$main.InlineDemoKt$main$2.INSTANCE;
        final int $i$f$execute = 0;
        final int n = 0;
        System.out.print((Object)"Hello ");
        System.out.print((Object)"World");
        anotherAction$iv.invoke();
    }
}
```

下面是第二个lambda参数所对应的[java代码](InlineDemoKt$main$2.java)。

```kotlin
static final class InlineDemoKt$main$2 extends Lambda implements Function0<Unit> {
    public static final InlineDemoKt$main$2 INSTANCE;
    
    public final void invoke() {
        System.out.print((Object)"Hello ");
        System.out.print((Object)"AnotherWorld");
    }
    
    static {
        InlineDemoKt$main$2.INSTANCE = new InlineDemoKt$main$2();
    }
}
```

可以看到，只有第二个lambda参数被包裹成了类的非静态方法。

### 参考资料

1. [Difference Between crossinline and noinline in Kotlin](https://www.baeldung.com/kotlin/crossinline-vs-noinline)