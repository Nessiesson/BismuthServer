package si.bismuth.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.inventory.menu.TraderMenu;
import net.minecraft.world.village.trade.TradeOffer;
import net.minecraft.world.village.trade.TradeOffers;
import net.minecraft.world.village.trade.Trader;

@Mixin(TraderMenu.class)
public class TraderMenuMixin {
	private final Random rand = new Random();

	@Shadow
	@Final
	private Trader trader;

	@Inject(method = "close", at = @At("HEAD"))
	private void unlockRecipes(PlayerEntity player, CallbackInfo ci) {
		final TradeOffers recipes = this.trader.getOffers(player);
		if (recipes == null) {
			return;
		}

		boolean shouldUnlock = true;
		for (TradeOffer recipe : recipes) {
			if (!recipe.isDisabled()) {
				shouldUnlock = false;
			}
		}

		if (shouldUnlock) {
			for (TradeOffer recipe : recipes) {
				recipe.increaseMaxUses(this.rand.nextInt(6) + this.rand.nextInt(6) + 2);
			}
		}
	}
}
