package si.bismuth.mixins;

import net.minecraft.block.PistonHeadBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MovingBlockEntity;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(MovingBlockEntity.class)
public class MovingBlockEntityMixin extends BlockEntity {
	@Shadow
	private float progress;
	@Shadow
	private float lastProgress;

	@Inject(method = "readNbt", at = @At(value = "FIELD", target = "Lnet/minecraft/block/entity/MovingBlockEntity;lastProgress:F", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
	private void readNbt(NbtCompound compound, CallbackInfo ci) {
		if (compound.isType("lastProgress", 5)) {
			this.lastProgress = this.progress;
		}
	}

	@Redirect(method = "writeNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;putFloat(Ljava/lang/String;F)V"))
	private void writeNbt(NbtCompound compound, String key, float value) {
		compound.putFloat("progress", this.progress);
		compound.putFloat("lastProgress", this.lastProgress);
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/BlockState;I)Z"))
	private void tick(CallbackInfo ci) {
		final BlockState state = this.world.getBlockState(this.pos);
		this.world.onBlockChanged(pos.offset(state.get(PistonHeadBlock.FACING).getOpposite()), state, state, 0);
	}

	@Inject(method = "moveEntities", at = @At(value = "INVOKE", target = "Ljava/lang/ThreadLocal;set(Ljava/lang/Object;)V", ordinal = 1, shift = At.Shift.AFTER, remap = false), locals = LocalCapture.CAPTURE_FAILHARD)
	public void tickEntity(float f, CallbackInfo ci, Direction face, double d0, List<Box> list, Box bb, List<Entity> list1, boolean isSlime, int i, Entity entity) {
		this.world.tickEntity(entity, false);
	}
}
