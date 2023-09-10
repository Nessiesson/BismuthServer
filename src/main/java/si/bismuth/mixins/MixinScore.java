package si.bismuth.mixins;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardScore;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScoreboardScore.class)
public class MixinScore {
    @Shadow @Final private Scoreboard scoreboard;
    @Shadow @Final private ScoreboardObjective objective;
    @Shadow @Final private String owner;

    @Inject(method="increase", at = @At("RETURN"))
    public void increaseTotal(int amount, CallbackInfo ci){
        if (!"Total".equals(owner)){
            final ScoreboardScore totalScore = scoreboard.getScore("Total", objective);
            if (totalScore.get() > -1){
                totalScore.increase(amount);
            }
        }
    }
}
