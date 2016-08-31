package me.veryyoung.wechat.randomgame;


import de.robv.android.xposed.XSharedPreferences;

public class PreferencesUtils {

    private static XSharedPreferences instance = null;

    private static XSharedPreferences getInstance() {
        if (instance == null) {
            instance = new XSharedPreferences(PreferencesUtils.class.getPackage().getName());
            instance.makeWorldReadable();
        } else {
            instance.reload();
        }
        return instance;
    }

    public static boolean diceCheat() {
        return getInstance().getBoolean("dice_cheat", false);
    }

    public static int diceNum() {
        return Integer.parseInt(getInstance().getString("dice_num", "0"));
    }

    public static boolean moraCheat() {
        return getInstance().getBoolean("mora_cheat", false);
    }

    public static int moraNum() {
        return Integer.parseInt(getInstance().getString("mora_num", "0"));
    }


}


