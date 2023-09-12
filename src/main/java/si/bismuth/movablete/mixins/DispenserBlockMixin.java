package si.bismuth.movablete.mixins;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.state.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DispenserBlock.class)
public class DispenserBlockMixin {
	@Inject(method = "onAdded", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/DispenserBlock;updateFacing(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/BlockState;)V"), cancellable = true)
	private void fixDispenserDropperRotation(World world, BlockPos pos, BlockState state, CallbackInfo ci) {
		ci.cancel();
	}
}
