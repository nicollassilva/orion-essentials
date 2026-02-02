package dev.thewarrior.SkyBlock.Managers.Islands.Data;

public class IslandSettings {
    private boolean allowVisitors = true;
    private boolean allowVisitorsChat = true;

    private boolean allowVisitorsToBuild = false;

    private boolean allowFriendsToVisit = true;
    private boolean allowFriendsToBuild = true;
    private boolean allowFriendsToDestroy = false;

    private boolean pvpEnabled = false;

    public boolean isAllowVisitors() {
        return allowVisitors;
    }

    public boolean isAllowVisitorsChat() {
        return allowVisitorsChat;
    }

    public boolean isAllowVisitorsToBuild() {
        return allowVisitorsToBuild;
    }

    public boolean isAllowFriendsToVisit() {
        return allowFriendsToVisit;
    }

    public boolean isAllowFriendsToBuild() {
        return allowFriendsToBuild;
    }

    public boolean isAllowFriendsToDestroy() {
        return allowFriendsToDestroy;
    }

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public void setAllowVisitors(boolean allowVisitors) {
        this.allowVisitors = allowVisitors;
    }

    public void setAllowVisitorsChat(boolean allowVisitorsChat) {
        this.allowVisitorsChat = allowVisitorsChat;
    }

    public void setAllowVisitorsToBuild(boolean allowVisitorsToBuild) {
        this.allowVisitorsToBuild = allowVisitorsToBuild;
    }

    public void setAllowFriendsToVisit(boolean allowFriendsToVisit) {
        this.allowFriendsToVisit = allowFriendsToVisit;
    }

    public void setAllowFriendsToBuild(boolean allowFriendsToBuild) {
        this.allowFriendsToBuild = allowFriendsToBuild;
    }

    public void setAllowFriendsToDestroy(boolean allowFriendsToDestroy) {
        this.allowFriendsToDestroy = allowFriendsToDestroy;
    }

    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }
}
