package si.bismuth.mixins;

import net.minecraft.command.CommandGameMode;
import net.minecraft.command.ICommandSender;
import net.minecraft.world.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import si.bismuth.MCServer;

@Mixin(CommandGameMode.class)
public abstract class MixinCommandGameMode {
	@Inject(method = "getGameModeFromCommand", at = @At("RETURN"), cancellable = true)
	private void onGetGameModeFromCommand(ICommandSender sender, String mode, CallbackInfoReturnable<GameType> cir) {
		if (!MCServer.server.isServerInOnlineMode()) {
			return;
		}

		if (cir.getReturnValue() == GameType.CREATIVE) {
			cir.setReturnValue(GameType.SPECTATOR);
		} else if (cir.getReturnValue() == GameType.ADVENTURE) {
			cir.setReturnValue(GameType.SURVIVAL);
		}
	}
}
