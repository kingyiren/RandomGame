package me.veryyoung.wechat.randomgame;


public class VersionParam {

    public static String randomGameClass;
    public static String gameType;

    public static void init(String version) {
        switch (version) {
            case "6.3.23":
            case "6.3.25":
                randomGameClass = "com.tencent.mm.sdk.platformtools.be";
                gameType = "rT";
                break;
            default:
                randomGameClass = "com.tencent.mm.sdk.platformtools.be";
                gameType = "rT";
        }
    }
}
