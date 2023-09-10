package si.bismuth.mixins;

import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShulkerBoxBlock.class)
public interface IShulkerBoxBlock {
	@Accessor
	DyeColor getColor();
}
