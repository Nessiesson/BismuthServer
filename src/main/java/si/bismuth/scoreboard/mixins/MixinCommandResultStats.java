package si.bismuth.scoreboard.mixins;

import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import si.bismuth.scoreboard.IScoreboard;
import si.bismuth.scoreboard.LongScore;

@Mixin(CommandResultStats.class)
public class MixinCommandResultStats {
    @Inject(method = "setCommandStatForSender", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Scoreboard;getOrCreateScore(Ljava/lang/String;Lnet/minecraft/scoreboard/ScoreObjective;)Lnet/minecraft/scoreboard/Score;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    public void setCommandStatForSender(MinecraftServer p_setCommandStatForSender_1_, ICommandSender p_setCommandStatForSender_2_, CommandResultStats.Type p_setCommandStatForSender_3_, int p_setCommandStatForSender_4_, CallbackInfo ci, String lvt_5_1_, ICommandSender lvt_6_1_, String playerName, String lvt_8_2_, Scoreboard instance, ScoreObjective objective) {
        IScoreboard scoreboard = (IScoreboard) instance;
        LongScore longScore = scoreboard.getOrCreateScore(playerName, objective);
        longScore.setScorePoints(p_setCommandStatForSender_4_);
        ci.cancel();
    }
}
