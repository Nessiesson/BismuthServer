package si.bismuth.ebictranslononation.mixins;

import net.minecraft.block.entity.MovingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MovingBlockEntity.class)
public abstract class MixinTileEntityPiston {
	@Shadow
	private boolean extending;

	@ModifyVariable(method = "moveCollidedEntities", ordinal = 0, index = 3, name = "d0", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList()Ljava/util/ArrayList;", remap = false))
	private double adjustRetractionAmount(double value) {
		if (!this.extending) {
			value *= 3.3D;
		}

		return value;
	}

	@Inject(method = "addCollissionAABBs", at = @At("HEAD"), cancellable = true)
	private void removeMethod(CallbackInfo ci) {
		ci.cancel();
	}
}
