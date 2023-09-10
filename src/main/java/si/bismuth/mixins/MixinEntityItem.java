package si.bismuth.mixins;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import si.bismuth.utils.IRecipeBookItemDuper;

@Mixin(ItemEntity.class)
public abstract class MixinEntityItem implements IRecipeBookItemDuper {
	@Shadow
	public abstract ItemStack getItemStack();

	@Inject(method = "onPlayerCollision", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/item/EntityItem;pickupDelay:I", opcode = Opcodes.GETFIELD))
	private void startDupe(PlayerEntity entityIn, CallbackInfo ci) {
		((IRecipeBookItemDuper) entityIn).dupeItemScan(true);
	}

	@Inject(method = "onPlayerCollision", at = @At("RETURN"))
	private void endDupe(PlayerEntity entityIn, CallbackInfo ci) {
		((IRecipeBookItemDuper) entityIn).dupeItemScan(false);
	}

	@Inject(method = "tryMerge", at = @At("HEAD"), cancellable = true)
	private void preSearchForOtherItemsNearby(CallbackInfo ci) {
		final ItemStack stack = this.getItemStack();
		if (stack.getSize() >= stack.getMaxSize()) {
			ci.cancel();
		}
	}

	@Inject(method = "canStack", at = @At("HEAD"), cancellable = true)
	private void combineItems(ItemEntity other, CallbackInfoReturnable<Boolean> cir) {
		final ItemStack stack = this.getItemStack();
		if (stack.getSize() >= stack.getMaxSize()) {
			cir.setReturnValue(false);
		}
	}
}
