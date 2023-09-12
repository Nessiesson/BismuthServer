package si.bismuth.commands;

import si.bismuth.logging.LogHandler;
import si.bismuth.logging.Logger;
import si.bismuth.logging.LoggerRegistry;
import si.bismuth.utils.Messenger;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("NoTranslation")
public class LogCommand extends CommandBismuthBase {
	@Override
	public String getName() {
		return "log";
	}

	@Override
	public String getUsage(CommandSource sender) {
		return "/log (interactive menu) OR /log <logName> [?option] [player] [handler ...] OR /log <logName> clear [player]";
	}

	@Override
	public void run(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
		PlayerEntity player = null;
		if (sender instanceof PlayerEntity) {
			player = (PlayerEntity) sender;
		}

		if (args.length == 0) {
			if (player == null) {
				return;
			}
			Map<String, String> subs = LoggerRegistry.getPlayerSubscriptions(player.getName());
			if (subs == null) {
				subs = new HashMap<>();
			}
			List<String> all_logs = new ArrayList<>(LoggerRegistry.getLoggerNames());
			Collections.sort(all_logs);
			Messenger.m(player, "w _____________________");
			Messenger.m(player, "w Available logging options:");
			for (String lname : all_logs) {
				List<Object> comp = new ArrayList<>();
				String color = subs.containsKey(lname) ? "w" : "g";
				comp.add("w  - " + lname + ": ");
				Logger logger = LoggerRegistry.getLogger(lname);
				String[] options = logger.getOptions();
				if (options == null) {
					if (subs.containsKey(lname)) {
						comp.add("l Subscribed ");
					} else {
						comp.add(color + " [Subscribe] ");
						comp.add("^w toggle subscription to " + lname);
						comp.add("!/log " + lname);
					}
				} else {
					for (String option : logger.getOptions()) {
						if (subs.containsKey(lname) && subs.get(lname).equalsIgnoreCase(option)) {
							comp.add("l [" + option + "] ");
						} else {
							comp.add(color + " [" + option + "] ");
							comp.add("^w toggle subscription to " + lname + " " + option);
							comp.add("!/log " + lname + " " + option);
						}

					}
				}
				if (subs.containsKey(lname)) {
					comp.add("nb [X]");
					comp.add("^w Click to toggle subscription");
					comp.add("!/log " + lname);
				}
				Messenger.m(player, comp.toArray(new Object[0]));
			}
			return;
		}
		// toggle to default
		if ("clear".equalsIgnoreCase(args[0])) {
			if (args.length > 1) {
				player = server.getPlayerManager().get(args[1]);
			}
			if (player == null) {
				throw new IncorrectUsageException("No player specified");
			}
			for (String logname : LoggerRegistry.getLoggerNames()) {
				LoggerRegistry.unsubscribePlayer(player.getName(), logname);
			}
			sendSuccess(sender, this, "Unsubscribed from all logs");
			return;
		}
		Logger logger = LoggerRegistry.getLogger(args[0]);
		if (logger != null) {
			String option = null;
			if (args.length >= 2) {
				option = logger.getAcceptedOption(args[1]);
			}
			if (args.length >= 3) {
				player = server.getPlayerManager().get(args[2]);
			}
			if (player == null) {
				throw new IncorrectUsageException("No player specified");
			}
			LogHandler handler = null;
			if (args.length >= 4) {
				handler = LogHandler.createHandler(args[3], ArrayUtils.subarray(args, 4, args.length));
				if (handler == null) {
					throw new CommandException("Invalid handler");
				}
			}
			boolean subscribed = true;
			if (args.length >= 2 && "clear".equalsIgnoreCase(args[1])) {
				LoggerRegistry.unsubscribePlayer(player.getName(), logger.getLogName());
				subscribed = false;
			} else if (option == null) {
				subscribed = LoggerRegistry.togglePlayerSubscription(player.getName(), logger.getLogName(), handler);
			} else {
				LoggerRegistry.subscribePlayer(player.getName(), logger.getLogName(), option, handler);
			}
			if (subscribed) {
				Messenger.m(player, "gi Subscribed to " + logger.getLogName() + ".");
			} else {
				Messenger.m(player, "gi Unsubscribed from " + logger.getLogName() + ".");
			}
		} else {
			throw new IncorrectUsageException("No logger named " + args[0] + ".");
		}
	}

	@Override
	public List<String> getSuggestions(MinecraftServer server, CommandSource sender, String[] args, BlockPos targetPos) {
		if (args.length == 1) {
			Set<String> options = new HashSet<>(LoggerRegistry.getLoggerNames());
			options.add("clear");
			return suggestMatching(args, options);
		} else if (args.length == 2) {
			if ("clear".equalsIgnoreCase(args[0])) {
				List<String> players = Arrays.asList(server.getPlayerNames());
				return suggestMatching(args, players.toArray(new String[0]));
			}
			Logger logger = LoggerRegistry.getLogger(args[0]);
			if (logger != null) {
				String[] opts = logger.getOptions();
				List<String> options = new ArrayList<>();
				options.add("clear");
				if (opts != null)
					options.addAll(Arrays.asList(opts));
				else
					options.add("on");
				return suggestMatching(args, options.toArray(new String[0]));
			}
		} else if (args.length == 3) {
			List<String> players = Arrays.asList(server.getPlayerNames());
			return suggestMatching(args, players.toArray(new String[0]));
		} else if (args.length == 4) {
			return suggestMatching(args, LogHandler.getHandlerNames());
		}

		return Collections.emptyList();
	}
}
