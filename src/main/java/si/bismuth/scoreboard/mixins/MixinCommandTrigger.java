package si.bismuth.scoreboard.mixins;

import net.minecraft.command.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import si.bismuth.scoreboard.IScoreboard;
import si.bismuth.scoreboard.LongScore;

@Mixin(CommandTrigger.class)
public abstract class MixinCommandTrigger extends CommandBase {
    @Shadow
    public abstract String getName();

    @Shadow
    public abstract String getUsage(ICommandSender iCommandSender);

    @Shadow
    public abstract void execute(MinecraftServer minecraftServer, ICommandSender iCommandSender, String[] strings) throws CommandException;

    @Inject(method = "execute", at = @At(value = "HEAD"), cancellable = true)
    void execute(MinecraftServer p_execute_1_, ICommandSender p_execute_2_, String[] p_execute_3_, CallbackInfo ci) throws CommandException {
        if (p_execute_3_.length < 3) {
            throw new WrongUsageException("commands.trigger.usage", new Object[0]);
        } else {
            EntityPlayerMP lvt_4_1_;
            if (p_execute_2_ instanceof EntityPlayerMP) {
                lvt_4_1_ = (EntityPlayerMP)p_execute_2_;
            } else {
                Entity lvt_5_1_ = p_execute_2_.getCommandSenderEntity();
                if (!(lvt_5_1_ instanceof EntityPlayerMP)) {
                    throw new CommandException("commands.trigger.invalidPlayer", new Object[0]);
                }

                lvt_4_1_ = (EntityPlayerMP)lvt_5_1_;
            }

            Scoreboard lvt_5_2_ = p_execute_1_.getWorld(0).getScoreboard();
            ScoreObjective lvt_6_1_ = lvt_5_2_.getObjective(p_execute_3_[0]);
            if (lvt_6_1_ != null && lvt_6_1_.getCriteria() == IScoreCriteria.TRIGGER) {
                long lvt_7_1_ = parseLong(p_execute_3_[2]);
                if (!lvt_5_2_.entityHasObjective(lvt_4_1_.getName(), lvt_6_1_)) {
                    throw new CommandException("commands.trigger.invalidObjective", new Object[]{p_execute_3_[0]});
                } else {
                    LongScore lvt_8_1_ = ((IScoreboard)lvt_5_2_).getOrCreateScore(lvt_4_1_.getName(), lvt_6_1_);
                    if (lvt_8_1_.isLocked()) {
                        throw new CommandException("commands.trigger.disabled", new Object[]{p_execute_3_[0]});
                    } else {
                        if ("set".equals(p_execute_3_[1])) {
                            lvt_8_1_.setScorePoints(lvt_7_1_);
                        } else {
                            if (!"add".equals(p_execute_3_[1])) {
                                throw new CommandException("commands.trigger.invalidMode", new Object[]{p_execute_3_[1]});
                            }

                            lvt_8_1_.increaseScore(lvt_7_1_);
                        }

                        lvt_8_1_.setLocked(true);
                        if (lvt_4_1_.interactionManager.isCreative()) {
                            notifyCommandListener(
                                    p_execute_2_, this, "commands.trigger.success", new Object[]{p_execute_3_[0], p_execute_3_[1], p_execute_3_[2]}
                            );
                        }

                    }
                }
            } else {
                throw new CommandException("commands.trigger.invalidObjective", new Object[]{p_execute_3_[0]});
            }
        }
        ci.cancel();
    }
}
