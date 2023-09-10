package si.bismuth.mixins;

import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DedicatedServer.class)
public class DedicatedServerMixin {
	@Shadow
	private boolean hasGui;

	/**
	 * @author nessie
	 * @reason lazy whatever, screw the server gui
	 */
	@Overwrite
	public void createGui() {
		this.hasGui = false;
	}
}
