package si.bismuth.mixins;

import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.material.Material;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FenceGateBlock.class)
public class FenceGateBlockMixin {
	@Redirect(method = "canSurvive", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/material/Material;isSolid()Z"))
	private boolean onPlaceFenceGate(Material material) {
		return true;
	}
}
