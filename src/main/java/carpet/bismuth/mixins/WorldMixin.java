package carpet.bismuth.mixins;

import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

@Mixin(World.class)
public abstract class WorldMixin {
    @Shadow @Final public Profiler profiler;
    private TileEntity te;

    @Inject(method = "updateEntities", at = @At(value = "INVOKE", target = "Ljava/lang/Class;getSimpleName()Ljava/lang/String;", remap = false), locals = LocalCapture.CAPTURE_FAILHARD)
    private void keepACopy(CallbackInfo ci, Iterator iterator, TileEntity tileentity) {
        this.te = tileentity;
    }

    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Ljava/lang/Class;getSimpleName()Ljava/lang/String;", remap = false))
    private String mc117087(Class clazz) {
        if (this.profiler.profilingEnabled) {
            return te.getClass().getSimpleName();
        }
        return "";
    }
}
