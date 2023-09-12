package si.bismuth.utils;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Stolen from/based on code from https://github.com/kyrptonaught/Inventory-Sorter
public class SortCases {
	public static String getStringForSort(ItemStack stack) {
		return specialCases(stack);
	}

	private static String specialCases(ItemStack stack) {
		if (stack.getSize() != stack.getMaxSize()) {
			return stackSize(stack);
		}

		if (stack.getItem() instanceof EnchantedBookItem) {
			return enchantedBookNameCase(stack);
		}

		if (stack.getItem() instanceof ToolItem) {
			return toolDuribilityCase(stack);
		}

		return stack.getItem().toString();
	}

	private static String stackSize(ItemStack stack) {
		return stack.getItem().toString() + stack.getSize();
	}

	private static String enchantedBookNameCase(ItemStack stack) {
		final NbtList enchants = EnchantedBookItem.getStoredEnchantments(stack);
		final List<String> names = new ArrayList<>();
		for (int i = 0; i < enchants.size(); i++) {
			final NbtCompound enchantTag = enchants.getCompound(i);
			final Enchantment enchant = Enchantment.byKey(enchantTag.getString("id"));
			if (enchant != null) {
				names.add(enchant.getName(enchants.getCompound(i).getInt("lvl")));
			}
		}

		Collections.sort(names);
		final String enchantNames = StringUtils.join(names, " ");
		return stack.getItem().toString() + " " + enchants.size() + " " + enchantNames;
	}

	private static String toolDuribilityCase(ItemStack stack) {
		return stack.getItem().toString() + stack.getDamage();
	}
}
