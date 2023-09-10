package si.bismuth.utils;

import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.math.BlockPos;
import si.bismuth.MCServer;
import si.bismuth.network.SearchForItemPacket;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Stolen from/based on code from https://github.com/kyrptonaught/Inventory-Sorter
public class InventoryHelper {
	public static void sortInv(Inventory inv, int startSlot, int invSize) {
		final List<ItemStack> stacks = new ArrayList<>();
		for (int i = 0; i < invSize; i++) {
			addStackWithMerge(stacks, inv.getStack(startSlot + i));
		}

		if (stacks.isEmpty()) {
			return;
		}

		stacks.sort(Comparator.comparing(SortCases::getStringForSort));
		for (int i = 0; i < invSize; i++) {
			inv.setStack(startSlot + i, i < stacks.size() ? stacks.get(i) : ItemStack.EMPTY);
		}

		inv.markDirty();
	}

	private static void addStackWithMerge(List<ItemStack> stacks, ItemStack newStack) {
		if (newStack.isEmpty()) {
			return;
		}

		if (newStack.isStackable() && newStack.getSize() != newStack.getMaxSize()) {
			for (int j = stacks.size() - 1; j >= 0; j--) {
				final ItemStack oldStack = stacks.get(j);
				if (canMergeItems(newStack, oldStack)) {
					combineStacks(newStack, oldStack);
					if (oldStack.isEmpty()) {
						stacks.remove(j);
					}
				}
			}
		}

		stacks.add(newStack);
	}

	private static void combineStacks(ItemStack first, ItemStack second) {
		if (first.getMaxSize() >= first.getSize() + second.getSize()) {
			first.increase(second.getSize());
			second.setSize(0);
		}

		final int maxInsertAmount = Math.min(first.getMaxSize() - first.getSize(), second.getSize());
		first.increase(maxInsertAmount);
		second.decrease(maxInsertAmount);
	}

	private static boolean canMergeItems(ItemStack first, ItemStack second) {
		if (!first.isStackable() || !second.isStackable()) {
			return false;
		}

		if (first.getSize() >= first.getMaxSize() || second.getSize() >= second.getMaxSize()) {
			return false;
		}

		return ItemStack.matchesItem(first, second);
	}

	public static boolean areItemStacksEqualIgnoringCount(ItemStack first, ItemStack second) {
		if (first.isEmpty() && second.isEmpty()) {
			return true;
		} else if (first.getItem() != second.getItem()) {
			return false;
		} else if (first.getDamage() != second.getDamage()) {
			return false;
		} else {
			return first.getNbt() == null || first.getNbt().equals(second.getNbt());
		}
	}

	public static void processFindItem(ServerPlayerEntity player, ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}

		final int range = 16;
		final DefaultedList<BlockPos> positions = DefaultedList.ofNull();
		for (int i = -range; i <= range; i++) {
			for (int j = -range; j <= range; j++) {
				for (int k = -range; k <= range; k++) {
					final int x = i + player.getSourceBlockPos().getX();
					final int y = j + player.getSourceBlockPos().getY();
					final int z = k + player.getSourceBlockPos().getZ();
					final BlockPos pos = new BlockPos(x, y, z);
					Inventory container = HopperBlockEntity.getInventoryAt(player.world, x, y, z);
					if (player.world.getBlockEntity(pos) instanceof EnderChestBlockEntity) {
						container = player.getEnderChestInventory();
					}

					//noinspection ConstantConditions
					if (container == null) {
						continue;
					}

					for (int s = 0; s < container.getSize(); s++) {
						final ItemStack stackInSlot = container.getStack(s);
						if (InventoryHelper.areItemStacksEqualIgnoringCount(stack, stackInSlot)) {
							positions.add(pos);
							break;
						} else if (Block.byItem(stackInSlot.getItem()) instanceof ShulkerBoxBlock) {
							final NbtCompound tag = stackInSlot.getNbt();
							if (tag != null && tag.isType("BlockEntityTag", 10)) {
								final NbtList list = tag.getCompound("BlockEntityTag").getList("Items", 10);
								for (int b = 0; b < list.size(); b++) {
									final NbtCompound compound = list.getCompound(b);
									if (InventoryHelper.areItemStacksEqualIgnoringCount(stack, new ItemStack(compound))) {
										positions.add(pos);
										break;
									}
								}
							}
						}
					}
				}
			}
		}

		MCServer.pcm.sendPacketToPlayer(player, new SearchForItemPacket(positions));
	}
}
