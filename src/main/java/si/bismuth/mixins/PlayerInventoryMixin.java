package si.bismuth.mixins;

import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import si.bismuth.utils.IRecipeBookItemDuper;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {
	@Shadow
	public PlayerEntity player;

	@Inject(method = "putStackInInventory", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/player/PlayerInventory;getEmptySlot()I"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void putStackInInventory(ItemStack stack, CallbackInfoReturnable<Integer> cir, int i) {
		((IRecipeBookItemDuper) this.player).dupeItem(i);
	}

	@Inject(method = "insertStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;getEmptySlot()I", shift = At.Shift.AFTER))
	private void megaDupe(int i, ItemStack item, CallbackInfoReturnable<Boolean> cir) {
		((IRecipeBookItemDuper) this.player).dupeItem(i);
	}
}
