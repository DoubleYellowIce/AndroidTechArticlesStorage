// 
// Decompiled by Procyon v0.5.36
// 

package coroutinesLearning.Inline;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.Metadata;

@Metadata(mv = { 1, 6, 0 }, k = 2, xi = 48, d1 = { "\u0000\u0010\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u001a*\u0010\u0000\u001a\u00020\u00012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\u000e\b\b\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003H\u0086\b\u00f8\u0001\u0000\u001a\u0006\u0010\u0005\u001a\u00020\u0001\u0082\u0002\u0007\n\u0005\b\u009920\u0001¨\u0006\u0006" }, d2 = { "execute", "", "action", "Lkotlin/Function0;", "anotherAction", "main", "KotlinStudy" })
public final class InlineDemoKt
{
    public static final void execute(@NotNull final Function0<Unit> action, @NotNull final Function0<Unit> anotherAction) {
        Intrinsics.checkNotNullParameter((Object)action, "action");
        Intrinsics.checkNotNullParameter((Object)anotherAction, "anotherAction");
        final int $i$f$execute = 0;
        action.invoke();
        anotherAction.invoke();
    }
    
    public static final void main() {
        final Function0 anotherAction$iv = (Function0)InlineDemoKt$main.InlineDemoKt$main$2.INSTANCE;
        final int $i$f$execute = 0;
        final int n = 0;
        System.out.print((Object)"Hello ");
        System.out.print((Object)"World");
        anotherAction$iv.invoke();
    }
}
