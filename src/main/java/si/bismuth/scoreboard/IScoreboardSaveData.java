package si.bismuth.scoreboard;

import net.minecraft.nbt.NBTTagList;

public interface IScoreboardSaveData {
    void readScores(NBTTagList p_readScores_1_);

    NBTTagList scoresToNbt();
}
