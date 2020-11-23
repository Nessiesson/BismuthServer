package si.bismuth.mixins;

import com.google.common.collect.Maps;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardSaveData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ScoreboardSaveData.class)
public class MixinScoreboardSaveData {
    @Shadow @Final Scoreboard scoreboard;

    @Inject(method="setScoreboard", at=@At(value="TAIL", target="Lnet/minecraft/scoreboard/ScoreboardSaveData;readFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V"))
    private void updateTotals(CallbackInfo ci){
        final Map<ScoreObjective, Long> totalsMap = Maps.<ScoreObjective, Long>newHashMap();

        for (Score score : scoreboard.getScores()){
            if (!"Total".equals(score.getPlayerName())){
                totalsMap.put(score.getObjective(), totalsMap.getOrDefault(score.getObjective(), (long) 0) + score.getScorePoints());
            }
        }

        for (ScoreObjective objective : totalsMap.keySet()){
            long total = totalsMap.get(objective);

            if (total > Integer.MAX_VALUE){
                total = -1;
            }

            scoreboard.getOrCreateScore("Total", objective).setScorePoints((int) total);
        }
    }
}
