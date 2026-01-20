package dev.thewarrior.Managers.Data.Permission;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PermissionData {
    private String prefix = "";
    private String suffix = "";
    private int priority = 0;
    private final List<String> permissions;

    private final AtomicBoolean needsUpdate = new AtomicBoolean(false);

    public PermissionData(List<String> permissions) {
        this.permissions = permissions;
    }

    public void addPermission(String permission) {
        if(this.permissions.contains(permission)) return;

        this.permissions.add(permission);
        this.needsUpdate.set(true);
    }

    public void removePermission(String permission) {
        this.permissions.remove(permission);
        this.needsUpdate.set(true);
    }

    public void syncPermissions(List<String> permissions) {
        this.permissions.clear();
        this.permissions.addAll(permissions);
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public boolean needsUpdate() {
        return this.needsUpdate.getAndSet(false);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
        this.needsUpdate.set(true);
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
        this.needsUpdate.set(true);
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
        this.needsUpdate.set(true);
    }
}
