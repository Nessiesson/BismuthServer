package si.bismuth.mixins;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.Command;
import net.minecraft.server.command.handler.CommandListener;
import net.minecraft.server.command.handler.CommandManager;
import net.minecraft.server.command.handler.CommandRegistry;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import si.bismuth.MCServer;
import si.bismuth.commands.CommandAllowGateway;
import si.bismuth.commands.CommandDisplayItem;
import si.bismuth.commands.CommandLog;
import si.bismuth.commands.CommandPing;
import si.bismuth.commands.CommandPlayer;
import si.bismuth.commands.CommandSearchForItem;
import si.bismuth.commands.CommandStackBoxes;
import si.bismuth.commands.CommandTick;

@Mixin(CommandManager.class)
public abstract class MixinServerCommandManager extends CommandRegistry implements CommandListener {
	@Inject(method = "<init>", at = @At("RETURN"))
	private void onCtor(MinecraftServer server, CallbackInfo ci) {
		this.register(new CommandAllowGateway());
		this.register(new CommandLog());
		this.register(new CommandDisplayItem());
		this.register(new CommandPing());
		this.register(new CommandPlayer());
		this.register(new CommandSearchForItem());
		this.register(new CommandStackBoxes());
		this.register(new CommandTick());
	}

	@Inject(method = "sendSuccess", at = @At("HEAD"), cancellable = true)
	private void silenceRcon(CommandSource sender, Command command, int flags, String translationKey, Object[] translationArgs, CallbackInfo ci) {
		if (sender.getName().equals("Rcon")) {
			ci.cancel();
		}
	}

	@Inject(method = "sendSuccess", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Style;setItalic(Ljava/lang/Boolean;)Lnet/minecraft/text/Style;", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
	private void logAdminCommands(CommandSource sender, Command command, int flags, String translationKey, Object[] translationArgs, CallbackInfo ci, boolean flag, MinecraftServer server, Text component) {
		if (server.isOnlineMode()) {
			MCServer.bot.sendToDiscord(component.buildString());
		}
	}
}
