package si.bismuth.commands;

import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class CommandDisplayItem extends CommandBismuthBase {
	@Override
	public String getName() {
		return "displayitem";
	}

	@Override
	public String getUsage(CommandSource sender) {
		return this.getName();
	}

	@Override
	public void run(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
		if (!(sender instanceof ServerPlayerEntity)) {
			throw new CommandException("Unknown " + sender.getName() + " tried to run " + this.getName() + "!");
		}

		final ItemStack stack = ((ServerPlayerEntity) sender).getMainHandStack();
		if (!stack.isEmpty()) {
			final Text component = new TranslatableText("chat.type.text", sender.getDisplayName(), stack.getDisplayName());
			server.getPlayerManager().sendMessage(component, false);
		}
	}
}
