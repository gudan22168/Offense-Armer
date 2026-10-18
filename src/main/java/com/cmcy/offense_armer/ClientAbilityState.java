package com.cmcy.offense_armer;

/**
 * 客户端缓存的氮气能力状态，由同步包更新，供 HUD 渲染使用。
 */
public class ClientAbilityState {
    public static int level = 0;
    public static float nitrogen = 0f;
    public static float maxNitrogen = 100f;
    public static boolean active = false;

    /** HUD 平滑过渡用的显示值。 */
    public static float displayNitrogen = 0f;

    private ClientAbilityState() {
    }
}
