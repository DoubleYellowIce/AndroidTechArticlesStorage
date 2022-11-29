// 
// Decompiled by Procyon v0.5.36
// 

package coroutinesLearning.Inline;

import kotlin.Metadata;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;

@Metadata(mv = { 1, 6, 0 }, k = 3, xi = 48, d1 = { "\u0000\b\n\u0000\n\u0002\u0010\u0002\n\u0000\u0010\u0000\u001a\u00020\u0001H\n¢\u0006\u0002\b\u0002" }, d2 = { "<anonymous>", "", "invoke" })
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