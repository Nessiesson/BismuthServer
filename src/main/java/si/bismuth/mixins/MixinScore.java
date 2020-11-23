package si.bismuth.mixins;

import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Score.class)
public class MixinScore {
    @Shadow @Final private Scoreboard scoreboard;
    @Shadow @Final private ScoreObjective objective;
    @Shadow @Final private String scorePlayerName;

    @Inject(method="increaseScore", at = @At("RETURN"))
    public void increaseTotal(int amount, CallbackInfo ci){
        if (!"Total".equals(scorePlayerName)){
            final Score totalScore = scoreboard.getOrCreateScore("Total", objective);
            if (totalScore.getScorePoints() > -1){
                totalScore.increaseScore(amount);
            }
        }
    }
}
