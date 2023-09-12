package si.bismuth.mixins;

import net.minecraft.server.entity.EntityTrackerEntry;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {
	@Redirect(method = "createAddEntityPacket", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;)V", remap = false))
	private void silenceFetchingForRemovedEntity(Logger logger, String message) {
		// noop
	}
}
