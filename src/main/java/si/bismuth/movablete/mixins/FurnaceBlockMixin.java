package si.bismuth.movablete.mixins;

import net.minecraft.block.FurnaceBlock;
import net.minecraft.block.state.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FurnaceBlock.class)
public class FurnaceBlockMixin {
	@Inject(method = "onAdded", at = @At("HEAD"), cancellable = true)
	private void fixFurnaceRotation(World world, BlockPos pos, BlockState state, CallbackInfo ci) {
		ci.cancel();
	}
}
