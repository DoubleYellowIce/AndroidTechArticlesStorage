by可以简单理解为provided by，该字段由什么提供。

只有两个核心方法getValue()和setValue()。

- 当访问有by关键字修饰的字段的时候，就去调用getValue()方法。
- 当修改有by关键字修饰的字段，就去调用setValue()方法。

参考资料2提供了一个小demo。

```kotlin
class Delegate {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return "$thisRef, thank you for delegating '${property.name}' to me!"
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        println("$value has been assigned to '${property.name}' in $thisRef.")
    }
}

class Example {
    var p: String by Delegate()
}
```

新建一个Example实例，并访问p。

```kotlin
fun main() {
    val example=Example()
    println(example.p)
}
```

运行一下。

![image-20221121111002329](https://private-pirture-storage.oss-cn-hangzhou.aliyuncs.com/img/image-20221121111002329.png)

可以看到getValue()方法被访问了。

修改p。

```kotlin
fun main() {
    val example=Example()
    example.p="1"
}
```

![image-20221121111053826](https://private-pirture-storage.oss-cn-hangzhou.aliyuncs.com/img/image-20221121111053826.png)

可以看到setValue()方法被访问了。

### 原理

原理也很简单，很容易想到的一个实现思路是在访问字段的getter()和setter()时调用Delegate实例的方法。

将Example.class文件反编译获得[Example.java](../Example.java)，下面是代码的部分片段。

```java
public final class Example
{
    static final /* synthetic */ KProperty<Object>[] $$delegatedProperties;
    @NotNull
    private final Delegate p$delegate;
    
    public Example() {
        this.p$delegate = new Delegate();
    }
    
    @NotNull
    public final String getP() {
        return this.p$delegate.getValue(this, Example.$$delegatedProperties[0]);
    }
    
    public final void setP(@NotNull final String <set-?>) {
        Intrinsics.checkNotNullParameter((Object)<set-?>, "<set-?>");
        this.p$delegate.setValue(this, Example.$$delegatedProperties[0], <set-?>);
    }
    ...
}
```

bingo，getP()和setP()果然访问了Delegate实例的方法。

### 应用场景

by关键字的应用场景是当很多个字段都有逻辑相同的getter()和setter()方法时，就不再需要为每个字段写同样的getter()和setter()方法，只需要写一个Delegate类，然后用关键字by，来减少重复代码。

### 参考资料

1.[What does 'by' keyword do in Kotlin?](https://stackoverflow.com/questions/38250022/what-does-by-keyword-do-in-kotlin)

2.[Delegated properties](https://kotlinlang.org/docs/delegated-properties.html)