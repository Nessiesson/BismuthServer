package si.bismuth.mixins;

import net.minecraft.server.network.handler.ServerLoginNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ServerLoginNetworkHandler.class)
public class ServerLoginNetworkHandlerMixin {
	@ModifyConstant(method = "tick", constant = @Constant(intValue = 600))
	private int disableLoginTimeout(int value) {
		return -1;
	}
}
