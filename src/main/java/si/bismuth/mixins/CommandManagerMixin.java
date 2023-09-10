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
import si.bismuth.commands.AllowGatewayCommand;
import si.bismuth.commands.DisplayItemCommand;
import si.bismuth.commands.LogCommand;
import si.bismuth.commands.PingCommand;
import si.bismuth.commands.PlayerCommand;
import si.bismuth.commands.SearchForItemCommand;
import si.bismuth.commands.StackBoxesCommand;
import si.bismuth.commands.TickCommand;

@Mixin(CommandManager.class)
public abstract class CommandManagerMixin extends CommandRegistry implements CommandListener {
	@Inject(method = "<init>", at = @At("RETURN"))
	private void onCtor(MinecraftServer server, CallbackInfo ci) {
		this.register(new AllowGatewayCommand());
		this.register(new LogCommand());
		this.register(new DisplayItemCommand());
		this.register(new PingCommand());
		this.register(new PlayerCommand());
		this.register(new SearchForItemCommand());
		this.register(new StackBoxesCommand());
		this.register(new TickCommand());
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
