// 
// Decompiled by Procyon v0.5.36
// 

package coroutinesLearning;

import kotlin.jvm.internal.Reflection;
import kotlin.jvm.internal.MutablePropertyReference1Impl;
import kotlin.jvm.internal.MutablePropertyReference1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import kotlin.reflect.KProperty;
import kotlin.Metadata;

@Metadata(mv = { 1, 6, 0 }, k = 1, xi = 48, d1 = { "\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\b\u0018\u00002\u00020\u0001B\u0005¢\u0006\u0002\u0010\u0002R+\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0003\u001a\u00020\u00048F@FX\u0086\u008e\u0002¢\u0006\u0012\n\u0004\b\n\u0010\u000b\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\t¨\u0006\f" }, d2 = { "LcoroutinesLearning/Example;", "", "()V", "<set-?>", "", "p", "getP", "()Ljava/lang/String;", "setP", "(Ljava/lang/String;)V", "p$delegate", "LcoroutinesLearning/Delegate;", "KotlinStudy" })
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
    
    static {
        $$delegatedProperties = new KProperty[] { (KProperty)Reflection.mutableProperty1((MutablePropertyReference1)new MutablePropertyReference1Impl((Class)Example.class, "p", "getP()Ljava/lang/String;", 0)) };
    }
}
