package si.bismuth.mixins;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandListener;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
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

@Mixin(ServerCommandManager.class)
public abstract class MixinServerCommandManager extends CommandHandler implements ICommandListener {
	@Inject(method = "<init>", at = @At("RETURN"))
	private void onCtor(MinecraftServer server, CallbackInfo ci) {
		this.registerCommand(new CommandAllowGateway());
		this.registerCommand(new CommandLog());
		this.registerCommand(new CommandDisplayItem());
		this.registerCommand(new CommandPing());
		this.registerCommand(new CommandPlayer());
		this.registerCommand(new CommandSearchForItem());
		this.registerCommand(new CommandStackBoxes());
		this.registerCommand(new CommandTick());
	}

	@Inject(method = "notifyListener", at = @At("HEAD"), cancellable = true)
	private void silenceRcon(ICommandSender sender, ICommand command, int flags, String translationKey, Object[] translationArgs, CallbackInfo ci) {
		if (sender.getName().equals("Rcon")) {
			ci.cancel();
		}
	}

	@Inject(method = "notifyListener", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/text/Style;setItalic(Ljava/lang/Boolean;)Lnet/minecraft/util/text/Style;", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
	private void logAdminCommands(ICommandSender sender, ICommand command, int flags, String translationKey, Object[] translationArgs, CallbackInfo ci, boolean flag, MinecraftServer server, ITextComponent component) {
		if (server.isServerInOnlineMode()) {
			MCServer.bot.sendToDiscord(component.getUnformattedText());
		}
	}
}
