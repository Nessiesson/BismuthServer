package si.bismuth.scoreboard.mixins;

import net.minecraft.command.*;
import net.minecraft.command.server.CommandScoreboard;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import si.bismuth.scoreboard.IScoreboard;
import si.bismuth.scoreboard.LongScore;

import java.util.Collection;
import java.util.Map;

@Mixin(CommandScoreboard.class)
public abstract class MixinCommandScoreboard extends CommandBase {
    @Shadow
    protected abstract Scoreboard getScoreboard(MinecraftServer p_listPlayers_4_);

    @Shadow
    protected abstract ScoreObjective convertToObjective(String s, boolean b, MinecraftServer p_addPlayerScore_4_);

    @Shadow
    public abstract String getName();

    @Shadow
    public abstract String getUsage(ICommandSender iCommandSender);

    @Shadow
    public abstract void execute(MinecraftServer minecraftServer, ICommandSender iCommandSender, String[] strings) throws CommandException;

    @Inject(method = "listPlayers", at = @At(value = "HEAD"), cancellable = true)
    void listPlayers(ICommandSender p_listPlayers_1_, String[] p_listPlayers_2_, int p_listPlayers_3_, MinecraftServer p_listPlayers_4_, CallbackInfo ci) throws CommandException {
        Scoreboard lvt_5_1_ = this.getScoreboard(p_listPlayers_4_);
        if (p_listPlayers_2_.length > p_listPlayers_3_) {
            String lvt_6_1_ = getEntityName(p_listPlayers_4_, p_listPlayers_1_, p_listPlayers_2_[p_listPlayers_3_]);
            Map<ScoreObjective, LongScore> lvt_7_1_ = ((IScoreboard)lvt_5_1_).getObjectivesForEntity(lvt_6_1_);
            p_listPlayers_1_.setCommandStat(CommandResultStats.Type.QUERY_RESULT, lvt_7_1_.size());
            if (lvt_7_1_.isEmpty()) {
                throw new CommandException("commands.scoreboard.players.list.player.empty", new Object[]{lvt_6_1_});
            }

            TextComponentTranslation lvt_8_1_ = new TextComponentTranslation(
                    "commands.scoreboard.players.list.player.count", new Object[]{lvt_7_1_.size(), lvt_6_1_}
            );
            lvt_8_1_.getStyle().setColor(TextFormatting.DARK_GREEN);
            p_listPlayers_1_.sendMessage(lvt_8_1_);

            for(LongScore lvt_10_1_ : lvt_7_1_.values()) {
                p_listPlayers_1_.sendMessage(
                        new TextComponentTranslation(
                                "commands.scoreboard.players.list.player.entry",
                                new Object[]{lvt_10_1_.getScorePoints(), lvt_10_1_.getObjective().getDisplayName(), lvt_10_1_.getObjective().getName()}
                        )
                );
            }
        } else {
            Collection<String> lvt_6_2_ = lvt_5_1_.getObjectiveNames();
            p_listPlayers_1_.setCommandStat(CommandResultStats.Type.QUERY_RESULT, lvt_6_2_.size());
            if (lvt_6_2_.isEmpty()) {
                throw new CommandException("commands.scoreboard.players.list.empty", new Object[0]);
            }

            TextComponentTranslation lvt_7_2_ = new TextComponentTranslation("commands.scoreboard.players.list.count", new Object[]{lvt_6_2_.size()});
            lvt_7_2_.getStyle().setColor(TextFormatting.DARK_GREEN);
            p_listPlayers_1_.sendMessage(lvt_7_2_);
            p_listPlayers_1_.sendMessage(new TextComponentString(joinNiceString(lvt_6_2_.toArray())));
        }
        ci.cancel();
    }

    @Inject(method = "addPlayerScore", at = @At(value = "HEAD"), cancellable = true)
    void addPlayerScore(ICommandSender p_addPlayerScore_1_, String[] p_addPlayerScore_2_, int p_addPlayerScore_3_, MinecraftServer p_addPlayerScore_4_, CallbackInfo ci) throws CommandException {
        String lvt_5_1_ = p_addPlayerScore_2_[p_addPlayerScore_3_ - 1];
        int lvt_6_1_ = p_addPlayerScore_3_;
        String lvt_7_1_ = getEntityName(p_addPlayerScore_4_, p_addPlayerScore_1_, p_addPlayerScore_2_[p_addPlayerScore_3_++]);
        if (lvt_7_1_.length() > 40) {
            throw new SyntaxErrorException("commands.scoreboard.players.name.tooLong", new Object[]{lvt_7_1_, 40});
        } else {
            ScoreObjective lvt_8_1_ = this.convertToObjective(p_addPlayerScore_2_[p_addPlayerScore_3_++], true, p_addPlayerScore_4_);
            long lvt_9_1_ = "set".equalsIgnoreCase(lvt_5_1_)
                    ? parseLong(p_addPlayerScore_2_[p_addPlayerScore_3_++])
                    : parseLong(p_addPlayerScore_2_[p_addPlayerScore_3_++], 0L, Long.MAX_VALUE);
            if (p_addPlayerScore_2_.length > p_addPlayerScore_3_) {
                Entity lvt_10_1_ = getEntity(p_addPlayerScore_4_, p_addPlayerScore_1_, p_addPlayerScore_2_[lvt_6_1_]);

                try {
                    NBTTagCompound lvt_11_1_ = JsonToNBT.getTagFromJson(buildString(p_addPlayerScore_2_, p_addPlayerScore_3_));
                    NBTTagCompound lvt_12_1_ = entityToNBT(lvt_10_1_);
                    if (!NBTUtil.areNBTEquals(lvt_11_1_, lvt_12_1_, true)) {
                        throw new CommandException("commands.scoreboard.players.set.tagMismatch", new Object[]{lvt_7_1_});
                    }
                } catch (NBTException var13) {
                    throw new CommandException("commands.scoreboard.players.set.tagError", new Object[]{var13.getMessage()});
                }
            }

            Scoreboard lvt_10_2_ = this.getScoreboard(p_addPlayerScore_4_);
            LongScore lvt_11_3_ = ((IScoreboard)lvt_10_2_).getOrCreateScore(lvt_7_1_, lvt_8_1_);
            if ("set".equalsIgnoreCase(lvt_5_1_)) {
                lvt_11_3_.setScorePoints(lvt_9_1_);
            } else if ("add".equalsIgnoreCase(lvt_5_1_)) {
                lvt_11_3_.increaseScore(lvt_9_1_);
            } else {
                lvt_11_3_.decreaseScore(lvt_9_1_);
            }

            notifyCommandListener(
                    p_addPlayerScore_1_, this, "commands.scoreboard.players.set.success", new Object[]{lvt_8_1_.getName(), lvt_7_1_, lvt_11_3_.getScorePoints()}
            );
        }
        ci.cancel();
    }

    @Inject(method = "testPlayerScore", at = @At(value = "HEAD"), cancellable = true)
    void testPlayerScore(ICommandSender p_testPlayerScore_1_, String[] p_testPlayerScore_2_, int p_testPlayerScore_3_, MinecraftServer p_testPlayerScore_4_, CallbackInfo ci) throws CommandException {
        Scoreboard lvt_5_1_ = this.getScoreboard(p_testPlayerScore_4_);
        String lvt_6_1_ = getEntityName(p_testPlayerScore_4_, p_testPlayerScore_1_, p_testPlayerScore_2_[p_testPlayerScore_3_++]);
        if (lvt_6_1_.length() > 40) {
            throw new SyntaxErrorException("commands.scoreboard.players.name.tooLong", new Object[]{lvt_6_1_, 40});
        } else {
            ScoreObjective lvt_7_1_ = this.convertToObjective(p_testPlayerScore_2_[p_testPlayerScore_3_++], false, p_testPlayerScore_4_);
            if (!lvt_5_1_.entityHasObjective(lvt_6_1_, lvt_7_1_)) {
                throw new CommandException("commands.scoreboard.players.test.notFound", new Object[]{lvt_7_1_.getName(), lvt_6_1_});
            } else {
                long lvt_8_1_ = p_testPlayerScore_2_[p_testPlayerScore_3_].equals("*")
                        ? Long.MIN_VALUE
                        : parseLong(p_testPlayerScore_2_[p_testPlayerScore_3_]);
                ++p_testPlayerScore_3_;
                long lvt_9_1_ = p_testPlayerScore_3_ < p_testPlayerScore_2_.length && !p_testPlayerScore_2_[p_testPlayerScore_3_].equals("*")
                        ? parseLong(p_testPlayerScore_2_[p_testPlayerScore_3_], lvt_8_1_, Long.MAX_VALUE)
                        : Long.MAX_VALUE;
                LongScore lvt_10_1_ = ((IScoreboard)lvt_5_1_).getOrCreateScore(lvt_6_1_, lvt_7_1_);
                if (lvt_10_1_.getScorePoints() >= lvt_8_1_ && lvt_10_1_.getScorePoints() <= lvt_9_1_) {
                    notifyCommandListener(
                            p_testPlayerScore_1_, this, "commands.scoreboard.players.test.success", new Object[]{lvt_10_1_.getScorePoints(), lvt_8_1_, lvt_9_1_}
                    );
                } else {
                    throw new CommandException("commands.scoreboard.players.test.failed", new Object[]{lvt_10_1_.getScorePoints(), lvt_8_1_, lvt_9_1_});
                }
            }
        }
        ci.cancel();
    }

    @Inject(method = "applyPlayerOperation", at = @At(value = "HEAD"), cancellable = true)
    void applyPlayerOperation(ICommandSender p_applyPlayerOperation_1_, String[] p_applyPlayerOperation_2_, int p_applyPlayerOperation_3_, MinecraftServer p_applyPlayerOperation_4_, CallbackInfo ci) throws CommandException {
        Scoreboard lvt_5_1_ = this.getScoreboard(p_applyPlayerOperation_4_);
        String lvt_6_1_ = getEntityName(p_applyPlayerOperation_4_, p_applyPlayerOperation_1_, p_applyPlayerOperation_2_[p_applyPlayerOperation_3_++]);
        ScoreObjective lvt_7_1_ = this.convertToObjective(p_applyPlayerOperation_2_[p_applyPlayerOperation_3_++], true, p_applyPlayerOperation_4_);
        String lvt_8_1_ = p_applyPlayerOperation_2_[p_applyPlayerOperation_3_++];
        String lvt_9_1_ = getEntityName(p_applyPlayerOperation_4_, p_applyPlayerOperation_1_, p_applyPlayerOperation_2_[p_applyPlayerOperation_3_++]);
        ScoreObjective lvt_10_1_ = this.convertToObjective(p_applyPlayerOperation_2_[p_applyPlayerOperation_3_], false, p_applyPlayerOperation_4_);
        if (lvt_6_1_.length() > 40) {
            throw new SyntaxErrorException("commands.scoreboard.players.name.tooLong", new Object[]{lvt_6_1_, 40});
        } else if (lvt_9_1_.length() > 40) {
            throw new SyntaxErrorException("commands.scoreboard.players.name.tooLong", new Object[]{lvt_9_1_, 40});
        } else {
            LongScore lvt_11_1_ = ((IScoreboard)lvt_5_1_).getOrCreateScore(lvt_6_1_, lvt_7_1_);
            if (!lvt_5_1_.entityHasObjective(lvt_9_1_, lvt_10_1_)) {
                throw new CommandException("commands.scoreboard.players.operation.notFound", new Object[]{lvt_10_1_.getName(), lvt_9_1_});
            } else {
                LongScore lvt_12_1_ = ((IScoreboard)lvt_5_1_).getOrCreateScore(lvt_9_1_, lvt_10_1_);
                if ("+=".equals(lvt_8_1_)) {
                    lvt_11_1_.setScorePoints(lvt_11_1_.getScorePoints() + lvt_12_1_.getScorePoints());
                } else if ("-=".equals(lvt_8_1_)) {
                    lvt_11_1_.setScorePoints(lvt_11_1_.getScorePoints() - lvt_12_1_.getScorePoints());
                } else if ("*=".equals(lvt_8_1_)) {
                    lvt_11_1_.setScorePoints(lvt_11_1_.getScorePoints() * lvt_12_1_.getScorePoints());
                } else if ("/=".equals(lvt_8_1_)) {
                    if (lvt_12_1_.getScorePoints() != 0) {
                        lvt_11_1_.setScorePoints(lvt_11_1_.getScorePoints() / lvt_12_1_.getScorePoints());
                    }
                } else if ("%=".equals(lvt_8_1_)) {
                    if (lvt_12_1_.getScorePoints() != 0) {
                        lvt_11_1_.setScorePoints(lvt_11_1_.getScorePoints() % lvt_12_1_.getScorePoints());
                    }
                } else if ("=".equals(lvt_8_1_)) {
                    lvt_11_1_.setScorePoints(lvt_12_1_.getScorePoints());
                } else if ("<".equals(lvt_8_1_)) {
                    lvt_11_1_.setScorePoints(Math.min(lvt_11_1_.getScorePoints(), lvt_12_1_.getScorePoints()));
                } else if (">".equals(lvt_8_1_)) {
                    lvt_11_1_.setScorePoints(Math.max(lvt_11_1_.getScorePoints(), lvt_12_1_.getScorePoints()));
                } else {
                    if (!"><".equals(lvt_8_1_)) {
                        throw new CommandException("commands.scoreboard.players.operation.invalidOperation", new Object[]{lvt_8_1_});
                    }

                    long lvt_13_1_ = lvt_11_1_.getScorePoints();
                    lvt_11_1_.setScorePoints(lvt_12_1_.getScorePoints());
                    lvt_12_1_.setScorePoints(lvt_13_1_);
                }

                notifyCommandListener(p_applyPlayerOperation_1_, this, "commands.scoreboard.players.operation.success", new Object[0]);
            }
        }
        ci.cancel();
    }
}
