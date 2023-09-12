package si.bismuth.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import si.bismuth.utils.BlockRotator;

@Mixin(Entity.class)
public class EntityMixin {
	// @formatter:off
	@Shadow public double x;
	@Shadow public double y;
	@Shadow public double z;
	@Shadow public float yaw;
	@Shadow private Box getShape() { return null; }
	@Shadow private boolean shouldSetPositionOnLoad() { return false; }
	@Shadow private void setShape(Box bb) { }
	@Shadow private void setPosition(double x, double y, double z) { }
	@Shadow private NbtList toNbtList(double... numbers) { return null; }
	// @formatter:on

	@Inject(method = "getDirection", at = @At("HEAD"), cancellable = true)
	private void onGetHorizontalFacing(CallbackInfoReturnable<Direction> cir) {
		if (BlockRotator.flippinEligibility((Entity) (Object) this)) {
			cir.setReturnValue(Direction.byIdHorizontal(MathHelper.floor((double) (this.yaw * 1F / 90F) + 0.5D) & 3).getOpposite());
		}
	}

	@Inject(method = "writeEntityNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;putUuid(Ljava/lang/String;Ljava/util/UUID;)V", shift = At.Shift.AFTER, ordinal = 0))
	private void writeAabNbt(NbtCompound compound, CallbackInfoReturnable<NbtCompound> cir) {
		final Box bb = this.getShape();
		compound.put("AABB", this.toNbtList(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ));
	}

	@Redirect(method = "readEntityNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;shouldSetPositionOnLoad()Z"))
	private boolean readAabNbt(Entity entity, NbtCompound compound) {
		if (this.shouldSetPositionOnLoad()) {
			this.setPosition(this.x, this.y, this.z);
		}

		if (compound.isType("AABB", 9)) {
			final NbtList bb = compound.getList("AABB", 6);
			this.setShape(new Box(bb.getDouble(0), bb.getDouble(1), bb.getDouble(2), bb.getDouble(3), bb.getDouble(4), bb.getDouble(5)));
		}

		return false;
	}
}
