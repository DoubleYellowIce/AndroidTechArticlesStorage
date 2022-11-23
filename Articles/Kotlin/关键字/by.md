by可以简单理解为provided by，该字段/类由什么提供，有两个方面的应用，一个是字段，一个是类。

### 字段

by关键字应用于字段的时候只有两个核心方法getValue()和setValue()。

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

#### 原理

原理也很简单，很容易想到的一个实现思路是在访问字段的getter()和setter()时调用Delegate实例的方法。

将Example.class文件反编译获得[Example.java](./Example.java)，下面是代码的部分片段。

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

#### 应用场景

by关键字的应用场景是当很多个字段都有逻辑相同的getter()和setter()方法时，就不再需要为每个字段写同样的getter()和setter()方法，只需要写一个Delegate类，然后用关键字by，来减少重复代码。

### 类

by关键字用于类时，是为了在实现装饰者模式的时候能减少繁琐代码。

装饰者模式的提出主要是为了实现子类在父类的基础上添加功能，而在正常情况下，子类只需要复写父类部分方法，剩余方法只是简单调用了父类的方法。

参考资料3提供了个小demo。

```kotlin
interface ChristmasTree {
    fun decorate(): String

    fun type():String
}

class PineChristmasTree : ChristmasTree {

    override fun type()="PineChristmasTree"

    override fun decorate() = "Christmas tree"
}

class Garlands(private val tree: ChristmasTree) : ChristmasTree {

    override fun decorate(): String {//1
        return tree.decorate() + decorateWithGarlands()
    }

    override fun type(): String {//2
        return tree.type()
    }
    
    private fun decorateWithGarlands(): String {
        return " with Garlands"
    }
}
```

代码1处就是子类需要复写父类的方法，而2处只是简单调用了父类的方法，在较复杂的类中，像2处的代码只多不少，这会造成较多的繁琐代码，下面再来看看用关键字by怎么实现同样的效果。

```kotlin
class Garlands(private val tree: ChristmasTree) : ChristmasTree by tree {

    override fun decorate(): String {
        return tree.decorate() + decorateWithGarlands()
    }

    private fun decorateWithGarlands(): String {
        return " with Garlands"
    }
}

fun main() {
    val christmasTree = Garlands(PineChristmasTree())
    val decoratedChristmasTree = christmasTree.decorate()
    println(decoratedChristmasTree)
    println(christmasTree.type())
}
```

运行一下。

![image-20221123113334237](https://private-pirture-storage.oss-cn-hangzhou.aliyuncs.com/img/image-20221123113334237.png)

反编译Garlands.class文件获取[Garlands.java](./Garlands.java)文件。

```kotlin
public final class Garlands implements ChristmasTree
{
    @NotNull
    private final ChristmasTree tree;
    
    public Garlands(@NotNull final ChristmasTree tree) {
        Intrinsics.checkNotNullParameter((Object)tree, "tree");
        this.tree = tree;
    }
    
    @NotNull
    public String type() {//1
        return this.tree.type();
    }
    
    @NotNull
    public String decorate() {
        return this.tree.decorate() + this.decorateWithGarlands();
    }
    
    private final String decorateWithGarlands() {
        return " with Garlands";
    }
}
```

可以看到编译器自动生成了1处的方法。

需要注意的是，编译器只会自动生成接口里的方法，在这里是ChristmasTree接口的方法，而不是生成父类的所有方法。

### 参考资料

1.[What does 'by' keyword do in Kotlin?](https://stackoverflow.com/questions/38250022/what-does-by-keyword-do-in-kotlin)

2.[Delegated properties](https://kotlinlang.org/docs/delegated-properties.html)

3.[The Decorator Pattern in Kotlin](https://www.baeldung.com/kotlin/decorator-pattern#:~:text=The%20Decorator%20Pattern%20is%20a,of%20this%20pattern%20in%20Kotlin.)