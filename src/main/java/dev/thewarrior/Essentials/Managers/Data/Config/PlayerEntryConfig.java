package dev.thewarrior.Essentials.Managers.Data.Config;

@SuppressWarnings({ "FieldCanBeLocal", "FieldMayBeFinal" })
public class PlayerEntryConfig {
    private boolean firstJoinMessageEnabled = true;
    private String firstJoinMessage = "&6Bem vindo {player} à &fOrion Network&6! Esperamos que você tenha uma &bótima experiência&6 com oque proporcionamos para você!";
    private boolean returnJoinMessageEnabled = true;
    private String returnJoinMessage = "&eBem vindo de volta {player}, estamos felizes em te ver novamente!";
    private boolean everyJoinTitleEnabled = true;
    private String everyJoinTitleTitle = "Seja bem vindo {player}!";
    private String everyJoinTitleSubtitle = "Aproveite seu tempo no servidor";
    private boolean teleportToSpawnOnJoinEnabled = true;
    private String firstJoinSound = "SFX_Discovery_Z3_Medium";

    public boolean isFirstJoinMessageEnabled() {
        return this.firstJoinMessageEnabled;
    }

    public String getFirstJoinMessage() {
        return this.firstJoinMessage;
    }

    public boolean isReturnJoinMessageEnabled() {
        return this.returnJoinMessageEnabled;
    }

    public String getReturnJoinMessage() {
        return this.returnJoinMessage;
    }

    public boolean isEveryJoinTitleEnabled() {
        return this.everyJoinTitleEnabled;
    }

    public String getEveryJoinTitleTitle() {
        return this.everyJoinTitleTitle;
    }

    public String getEveryJoinTitleSubtitle() {
        return this.everyJoinTitleSubtitle;
    }

    public boolean isTeleportToSpawnOnJoinEnabled() {
        return this.teleportToSpawnOnJoinEnabled;
    }

    public String getFirstJoinSound() {
        return this.firstJoinSound;
    }
}
