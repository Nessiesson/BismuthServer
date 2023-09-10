package si.bismuth.mixins;

import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.inventory.menu.ActionType;
import net.minecraft.inventory.menu.InventoryMenu;
import net.minecraft.inventory.slot.InventorySlot;
import net.minecraft.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(InventoryMenu.class)
public class InventoryMenuMixin {
	@Shadow
	public List<InventorySlot> slots;

	@Redirect(method = "onClickSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/slot/InventorySlot;hasStack()Z", ordinal = 0), slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/inventory/menu/ActionType;THROW:Lnet/minecraft/inventory/menu/ActionType;", opcode = Opcodes.GETSTATIC)))
	private boolean onClickSlot(InventorySlot slot, int id, int dragType, ActionType clickType, PlayerEntity player) {
		if (slot != null && slot.hasStack() && slot.canPickUp(player)) {
			final ItemStack stack = slot.removeStack(slot.getStack().getSize());
			slot.onStackRemovedByPlayer(player, stack);
			player.dropItem(stack, true);
		}

		return false;
	}

	@Inject(method = "onClickSlot", at = @At("HEAD"), cancellable = true)
	private void craftingHax(int id, int dragType, ActionType clickType, PlayerEntity player, CallbackInfoReturnable<ItemStack> cir) {
		if (clickType == ActionType.THROW && player.inventory.getCursorStack().isEmpty() && id >= 0) {
			ItemStack stack = ItemStack.EMPTY;
			final InventorySlot slot = this.slots.get(id);
			if (slot != null && slot.canPickUp(player)) {
				if (id == 0 && dragType == 1) {
					ItemStack dropAll = this.dropAllCrafting(player, id, this.slots);
					while (!dropAll.isEmpty() && ItemStack.matchesItem(slot.getStack(), dropAll)) {
						stack = dropAll.copy();
						dropAll = this.dropAllCrafting(player, id, this.slots);
					}

					cir.setReturnValue(stack);
					cir.cancel();
				}
			}
		}
	}

	private ItemStack dropAllCrafting(PlayerEntity player, int index, List<InventorySlot> inventorySlotsParam) {
		ItemStack itemstack = ItemStack.EMPTY;
		final InventorySlot slot = inventorySlotsParam.get(index);
		if (slot != null && slot.hasStack()) {
			final ItemStack slotStack = slot.getStack();
			itemstack = slotStack.copy();
			if (index == 0) {
				player.dropItem(itemstack, true);
				slotStack.setSize(0);
				slot.onQuickMoved(slotStack, itemstack);
			}

			if (slotStack.getSize() == itemstack.getSize()) {
				return ItemStack.EMPTY;
			}

			final ItemStack stackOnTake = slot.onStackRemovedByPlayer(player, slotStack);
			if (index == 0) {
				player.dropItem(stackOnTake, false);
			}
		}

		return itemstack;
	}
}
