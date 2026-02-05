package dev.thewarrior.SkyBlock.Managers.Islands.Data;

public class IslandSettings {
    private boolean allowAnonymousToVisit = true;
    private boolean allowAnonymousToChat = true;

    private boolean allowVisitorsToBuild = false;

    private boolean allowMembersToVisit = true;
    private boolean allowMembersToBuild = true;
    private boolean allowMembersToDestroy = false;

    private boolean pvpEnabled = false;

    public boolean isAllowAnonymousToVisit() {
        return allowAnonymousToVisit;
    }

    public boolean isAllowAnonymousToChat() {
        return allowAnonymousToChat;
    }

    public boolean isAllowVisitorsToBuild() {
        return allowVisitorsToBuild;
    }

    public boolean isAllowMembersToVisit() {
        return allowMembersToVisit;
    }

    public boolean isAllowMembersToBuild() {
        return allowMembersToBuild;
    }

    public boolean isAllowMembersToDestroy() {
        return allowMembersToDestroy;
    }

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public void setAllowAnonymousToVisit(boolean allowAnonymousToVisit) {
        this.allowAnonymousToVisit = allowAnonymousToVisit;
    }

    public void setAllowAnonymousToChat(boolean allowAnonymousToChat) {
        this.allowAnonymousToChat = allowAnonymousToChat;
    }

    public void setAllowVisitorsToBuild(boolean allowVisitorsToBuild) {
        this.allowVisitorsToBuild = allowVisitorsToBuild;
    }

    public void setAllowMembersToVisit(boolean allowMembersToVisit) {
        this.allowMembersToVisit = allowMembersToVisit;
    }

    public void setAllowMembersToBuild(boolean allowMembersToBuild) {
        this.allowMembersToBuild = allowMembersToBuild;
    }

    public void setAllowMembersToDestroy(boolean allowMembersToDestroy) {
        this.allowMembersToDestroy = allowMembersToDestroy;
    }

    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }

    public IslandSettings copy() {
        IslandSettings copy = new IslandSettings();

        copy.setAllowAnonymousToVisit(this.allowAnonymousToVisit);
        copy.setAllowAnonymousToChat(this.allowAnonymousToChat);
        copy.setAllowVisitorsToBuild(this.allowVisitorsToBuild);
        copy.setAllowMembersToVisit(this.allowMembersToVisit);
        copy.setAllowMembersToBuild(this.allowMembersToBuild);
        copy.setAllowMembersToDestroy(this.allowMembersToDestroy);
        copy.setPvpEnabled(this.pvpEnabled);

        return copy;
    }
}
