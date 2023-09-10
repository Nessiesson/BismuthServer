package si.bismuth.mixins;

import com.google.common.collect.Maps;
import net.minecraft.scoreboard.SavedScoreboardData;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardScore;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(SavedScoreboardData.class)
public class MixinScoreboardSaveData {
    @Shadow @Final Scoreboard scoreboard;

    @Inject(method="setScoreboard", at=@At(value="TAIL", target="Lnet/minecraft/scoreboard/SavedScoreboardData;readNbt(Lnet/minecraft/nbt/NbtCompound;)V"))
    private void updateTotals(CallbackInfo ci){
        final Map<ScoreboardObjective, Long> totalsMap = Maps.<ScoreboardObjective, Long>newHashMap();

        for (ScoreboardScore score : scoreboard.getScores()){
            if (!"Total".equals(score.getOwner())){
                totalsMap.put(score.getObjective(), totalsMap.getOrDefault(score.getObjective(), (long) 0) + score.get());
            }
        }

        for (ScoreboardObjective objective : totalsMap.keySet()){
            long total = totalsMap.get(objective);

            if (total > Integer.MAX_VALUE){
                total = -1;
            }

            scoreboard.getScore("Total", objective).set((int) total);
        }
    }
}
