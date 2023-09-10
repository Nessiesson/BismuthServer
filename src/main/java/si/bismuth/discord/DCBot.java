package si.bismuth.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Formatting;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import si.bismuth.MCServer;

import javax.security.auth.login.LoginException;
import java.util.Arrays;
import java.util.List;

public class DCBot extends ListenerAdapter {
	private final static Logger LOGGER = LogManager.getLogger();
	private final static long BismuthID = 635252849571266580L;
	private final static long ChannelID = 635254222916419590L;
	private final static String channelURL = String.format("https://discordapp.com/channels/%d/%d/", BismuthID, ChannelID);
	private final static String PREFIX = ";";
	private final boolean isTestServer;
	private final JDA jda;

	public DCBot(String token, boolean isOnlineMode) throws LoginException, InterruptedException {
		isTestServer = !isOnlineMode;
		this.jda = JDABuilder
				.createDefault(token)
				.addEventListeners(this)
				.build()
				.awaitReady();
		this.sendToDiscord("Server started!");
	}

	public void sendToDiscord(String message) {
		final TextChannel channel = this.jda.getTextChannelById(DCBot.ChannelID);
		if (channel != null) {
			if (isTestServer) {
				message = "\uD83E\uDDEA" + message;
			}

			channel.sendMessage(message).queue();
		}
	}

	public void sendAdvancementMessage(Text component) {
		this.sendToDiscord("**\uD83C\uDF8A " + component.buildString() + "**");
	}

	public void sendDeathMessage(Text component) {
		this.sendToDiscord("**\uD83D\uDD71 " + component.buildString() + "**");
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getMember() == null || event.getAuthor().isBot() || event.getGuild().getIdLong() != BismuthID) {
			return;
		}

		// TODO: handle DC2MC images
		final MessageChannel channel = event.getChannel();
		final Member member = event.getMember();
		final Message message = event.getMessage();
		final String content = message.getContentDisplay();
		final List<Message.Attachment> attachments = message.getAttachments();
		if (channel.getIdLong() == ChannelID && !content.startsWith(PREFIX)) {
			final String name = event.getMember().getEffectiveName();
			final Text symbol = new LiteralText("\u24B9");
			final HoverEvent hoverText = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(member.getUser().getAsTag()));
			final ClickEvent clickText = new ClickEvent(ClickEvent.Action.OPEN_URL, channelURL);
			symbol.getStyle().setColor(Formatting.BLUE).setHoverEvent(hoverText).setClickEvent(clickText);
			final Text text = new TranslatableText("chat.type.text", name, content);
			for (Message.Attachment file : attachments) {
				final Text fileName = new LiteralText(" " + file.getFileName());
				fileName.getStyle()
						.setColor(Formatting.BLUE)
						.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Open attachment")))
						.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, file.getUrl()));
				text.append(fileName);
			}

			MCServer.server.submit(() -> MCServer.server.getPlayerManager().sendSystemMessage(new LiteralText("").append(symbol).append(text)));
			return;
		}

		final String[] args = content.split(" ");
		if (args.length == 0) {
			return;
		}

		final String command = args[0];

		if (this.isCommand(command, new String[]{"lazy"})) {
			MCServer.server.submit(() -> channel.sendMessage("no u").queue());
		}

		if (this.isCommand(command, new String[]{"tps"})) {
			MCServer.server.submit(() -> {
				final double MSPT = MathHelper.average(MCServer.server.averageTickTimes) * 1E-6D;
				final double TPS = 1000D / Math.max(50, MSPT);
				channel.sendMessage(String.format("**TPS: %.2f MSPT: %.2f**", TPS, MSPT)).queue();
			});
		}

		if (this.isCommand(command, new String[]{"list", "players", "online"})) {
			MCServer.server.submit(() -> {
				final String[] players = MCServer.server.getPlayerNames();
				final String title = String.format("%d player%s online:", players.length, players.length != 1 ? "s" : "");
				final MessageEmbed.AuthorInfo author = new MessageEmbed.AuthorInfo("BismuthBot", null, "https://i.imgur.com/a2w3DjI.png", null);
				final MessageEmbed embed = new MessageEmbed(null, title, StringUtils.join(players, "\n").replaceAll("_", "\\\\_"), EmbedType.RICH, null, 0x8665BD, null, null, author, null, null, null, null);
				channel.sendMessage(embed).queue();
			});
		}
	}

	private boolean isCommand(String text, String[] command) {
		return Arrays.stream(command).anyMatch(s -> text.startsWith(PREFIX + s));
	}
}
