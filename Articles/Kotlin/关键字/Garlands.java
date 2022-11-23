// 
// Decompiled by Procyon v0.5.36
// 

package coroutinesLearning;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import kotlin.Metadata;

@Metadata(mv = { 1, 6, 0 }, k = 1, xi = 48, d1 = { "\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0001¢\u0006\u0002\u0010\u0003J\b\u0010\u0004\u001a\u00020\u0005H\u0016J\b\u0010\u0006\u001a\u00020\u0005H\u0002J\t\u0010\u0007\u001a\u00020\u0005H\u0096\u0001R\u000e\u0010\u0002\u001a\u00020\u0001X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\b" }, d2 = { "LcoroutinesLearning/Garlands;", "LcoroutinesLearning/ChristmasTree;", "tree", "(LcoroutinesLearning/ChristmasTree;)V", "decorate", "", "decorateWithGarlands", "type", "KotlinStudy" })
public final class Garlands implements ChristmasTree
{
    @NotNull
    private final ChristmasTree tree;
    
    public Garlands(@NotNull final ChristmasTree tree) {
        Intrinsics.checkNotNullParameter((Object)tree, "tree");
        this.tree = tree;
    }
    
    @NotNull
    public String type() {
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
