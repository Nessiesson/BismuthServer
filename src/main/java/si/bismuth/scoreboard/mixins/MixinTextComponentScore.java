package si.bismuth.scoreboard.mixins;

import net.minecraft.command.ICommandSender;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.TextComponentScore;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import si.bismuth.scoreboard.IScoreboard;
import si.bismuth.scoreboard.LongScore;

@Mixin(TextComponentScore.class)
public abstract class MixinTextComponentScore {
    @Shadow @Final private String name;
    @Shadow @Final private String objective;
    @Shadow private String value;

    @Shadow
    public abstract void setValue(String p_setValue_1_);

    @Inject(method = "resolve", at = @At(value = "HEAD"), cancellable = true)
    void resolve(ICommandSender p_resolve_1_, CallbackInfo ci) {
        MinecraftServer lvt_2_1_ = p_resolve_1_.getServer();
        if (lvt_2_1_ != null && lvt_2_1_.isAnvilFileSet() && StringUtils.isNullOrEmpty(this.value)) {
            Scoreboard lvt_3_1_ = lvt_2_1_.getWorld(0).getScoreboard();
            ScoreObjective lvt_4_1_ = lvt_3_1_.getObjective(this.objective);
            if (lvt_3_1_.entityHasObjective(this.name, lvt_4_1_)) {
                LongScore lvt_5_1_ = ((IScoreboard)lvt_3_1_).getOrCreateScore(this.name, lvt_4_1_);
                this.setValue(String.format("%d", lvt_5_1_.getScorePoints()));
            } else {
                this.value = "";
            }
        }
        ci.cancel();
    }
}
