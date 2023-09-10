package si.bismuth.mixins;

import net.minecraft.block.StainedGlassBlock;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.state.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StainedGlassBlock.class)
public class StainedGlassBlockMixin {
	@Inject(method = "getMaterialColor", at = @At("HEAD"), cancellable = true)
	private void remapStainedGlassMapColor(BlockState state, WorldView world, BlockPos pos, CallbackInfoReturnable<MaterialColor> cir) {
		cir.setReturnValue(MaterialColor.AIR);
	}
}
