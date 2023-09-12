package si.bismuth.mixins;

import net.minecraft.entity.living.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import si.bismuth.utils.BlockRotator;

@Mixin(Direction.class)
public class DirectionMixin {
	@Inject(method = "nearest", at = @At(value = "FIELD", target = "Lnet/minecraft/util/math/Direction;UP:Lnet/minecraft/util/math/Direction;", shift = At.Shift.BEFORE), cancellable = true)
	private static void onNearest1(BlockPos pos, LivingEntity placer, CallbackInfoReturnable<Direction> cir) {
		if (BlockRotator.flippinEligibility(placer)) {
			cir.setReturnValue(Direction.DOWN);
		}
	}

	@Inject(method = "nearest", at = @At(value = "FIELD", target = "Lnet/minecraft/util/math/Direction;DOWN:Lnet/minecraft/util/math/Direction;", shift = At.Shift.BEFORE), cancellable = true)
	private static void onNearest2(BlockPos pos, LivingEntity placer, CallbackInfoReturnable<Direction> cir) {
		if (BlockRotator.flippinEligibility(placer)) {
			cir.setReturnValue(Direction.DOWN);
		}
	}
}
