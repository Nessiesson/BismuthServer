package si.bismuth.commands;

import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.inventory.slot.InventorySlot;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShulkerBoxItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import si.bismuth.mixins.IShulkerBoxBlock;

import java.util.HashMap;
import java.util.Map;

public class StackBoxesCommand extends CommandBismuthBase {
	@Override
	public String getName() {
		return "stackboxes";
	}

	@Override
	public String getUsage(CommandSource sender) {
		return "stackboxes";
	}

	@Override
	public void run(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
		if (!(sender instanceof ServerPlayerEntity)) {
			throw new CommandException("Unknown " + sender.getName() + " tried to run /stackboxes!");
		}

		Map<DyeColor, Integer> boxesToStack = new HashMap<>();
		final ServerPlayerEntity player = asPlayer(sender);
		for (final InventorySlot slot : player.playerMenu.slots) {
			final Pair<DyeColor, Integer> pair = getShulkerBoxColourAndAmour(slot.getStack());
			if (pair.getRight() > 0) {
				boxesToStack.merge(pair.getLeft(), pair.getRight(), Integer::sum);
				slot.setStack(ItemStack.EMPTY);
			}
		}

		for (Map.Entry<DyeColor, Integer> entry : boxesToStack.entrySet()) {
			if (entry.getValue() > 0) {
				final ItemStack stack = new ItemStack(ShulkerBoxBlock.byColor(entry.getKey()), entry.getValue());
				if (!player.addItem(stack)) {
					player.dropItem(stack, false);
				}
			}
		}

		player.playerMenu.updateListeners();
	}

	private Pair<DyeColor, Integer> getShulkerBoxColourAndAmour(final ItemStack stack) {
		if (stack.getItem() instanceof ShulkerBoxItem) {
			NbtCompound cmp = this.getCompoundOrNull(stack);
			if (cmp == null || cmp.getList("Items", 10).isEmpty()) {
				final DyeColor dye = ((IShulkerBoxBlock) ((ShulkerBoxItem) stack.getItem()).getBlock()).getColor();
				return new ImmutablePair<>(dye, stack.getSize());
			}
		}

		return new ImmutablePair<>(DyeColor.WHITE, 0);
	}

	private NbtCompound getCompoundOrNull(final ItemStack stack) {
		final NbtCompound compound = stack.getNbt();
		if (compound != null && compound.contains("BlockEntityTag")) {
			return compound.getCompound("BlockEntityTag");
		} else {
			return null;
		}
	}
}
