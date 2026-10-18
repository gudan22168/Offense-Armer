package com.cmcy.offense_armer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 玩家身上挂载的「氮气装甲」能力数据。
 * 由 DataAttachment 持久化，随玩家存档保存（copyOnDeath）。
 */
public class NitrogenAbility {
    /** 能力最高等级（原作 Level 4 为巅峰，Lv5 为超越原作的觉醒）。 */
    public static final int MAX_LEVEL = 5;

    private int level;
    private float nitrogen;
    private boolean active;
    /** 爆冲状态（客户端按键同步，不持久化）。 */
    private boolean bursting;

    public NitrogenAbility() {
        this.level = 0;
        this.nitrogen = 0f;
        this.active = false;
    }

    public NitrogenAbility(int level, float nitrogen, boolean active) {
        this.level = level;
        this.nitrogen = nitrogen;
        this.active = active;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(0, Math.min(MAX_LEVEL, level));
    }

    public float getNitrogen() {
        return nitrogen;
    }

    public void setNitrogen(float nitrogen) {
        this.nitrogen = Math.max(0f, Math.min(getMaxNitrogen(), nitrogen));
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isBursting() {
        return bursting;
    }

    public void setBursting(boolean bursting) {
        this.bursting = bursting;
    }

    public float getMaxNitrogen() {
        return (float) Config.maxNitrogen;
    }

    public void addNitrogen(float amount) {
        setNitrogen(this.nitrogen + amount);
    }

    public void consumeNitrogen(float amount) {
        setNitrogen(this.nitrogen - amount);
    }

    public static final Codec<NitrogenAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("level").forGetter(NitrogenAbility::getLevel),
            Codec.FLOAT.fieldOf("nitrogen").forGetter(NitrogenAbility::getNitrogen),
            Codec.BOOL.fieldOf("active").forGetter(NitrogenAbility::isActive)
    ).apply(instance, NitrogenAbility::new));
}
