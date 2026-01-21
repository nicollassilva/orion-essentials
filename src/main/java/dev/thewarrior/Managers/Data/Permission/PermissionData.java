package dev.thewarrior.Managers.Data.Permission;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class PermissionData {
    private String prefix = "";
    private String suffix = "";
    private int priority = 0;

    private final List<String> permissions;

    private final Set<String> permissionsToDelete;
    private final Set<String> permissionsToAdd;

    private final AtomicBoolean needsDataUpdate = new AtomicBoolean(false);
    private final AtomicBoolean needsPermissionsUpdate = new AtomicBoolean(false);

    private String updatedName = "";

    public PermissionData(List<String> permissions, boolean isCreating) {
        this.permissions = permissions;

        this.permissionsToDelete = new ObjectArraySet<>();
        this.permissionsToAdd = new ObjectArraySet<>();

        if(isCreating) {
            this.permissionsToAdd.addAll(permissions);

            this.needsPermissionsUpdate.set(true);
            this.needsDataUpdate.set(true);
        }
    }

    public PermissionData(List<String> permissions) {
        this(permissions, false);
    }

    public void setUpdatedName(String name) {
        this.updatedName = name;

        this.needsPermissionsUpdate.set(true);
        this.needsDataUpdate.set(true);
    }

    public String getUpdatedName() {
        return this.updatedName;
    }

    public void addPermission(String permission) {
        if(this.permissions.contains(permission)) return;

        this.permissions.addFirst(permission);
        this.permissionsToAdd.add(permission);

        this.needsPermissionsUpdate.set(true);
    }

    public boolean updatePermission(String oldPermission, String newPermission) {
        int index = this.permissions.indexOf(oldPermission);

        if(index == -1) return false;

        this.permissions.set(index, newPermission);

        this.permissionsToAdd.add(newPermission);
        this.permissionsToAdd.remove(oldPermission);

        this.permissionsToDelete.add(oldPermission);
        this.needsPermissionsUpdate.set(true);

        return true;
    }

    public void removePermission(String permission) {
        boolean isDeleted = this.permissions.remove(permission) | this.permissionsToAdd.remove(permission);

        if(!isDeleted) return;

        this.permissionsToDelete.add(permission);

        this.needsPermissionsUpdate.set(true);
    }

    public List<String> getPermissions() {
        return this.permissions;
    }

    public Set<String> getPermissionsToDelete() {
        return this.permissionsToDelete;
    }

    public Set<String> getPermissionsToAdd() {
        return this.permissionsToAdd;
    }

    public void clearPendingPermissionsChanges() {
        this.permissionsToAdd.clear();
        this.permissionsToDelete.clear();
    }

    public boolean needsNameUpdate() {
        return this.updatedName != null && !this.updatedName.isEmpty();
    }

    public boolean needsUpdate() {
        return this.needsDataUpdate.get() || this.needsPermissionsUpdate.get() || this.needsNameUpdate();
    }

    public boolean needsDataUpdate() {
        return this.needsDataUpdate.getAndSet(false) || this.needsNameUpdate();
    }

    public boolean needsPermissionsUpdate() {
        return this.needsPermissionsUpdate.getAndSet(false) || this.needsNameUpdate();
    }

    public String getPrefix() {
        return this.prefix;
    }

    public void setPrefix(String prefix, boolean needsUpdate) {
        this.prefix = prefix;
        this.needsDataUpdate.set(needsUpdate);
    }

    public String getSuffix() {
        return this.suffix;
    }

    public void setSuffix(String suffix, boolean needsUpdate) {
        this.suffix = suffix;
        this.needsDataUpdate.set(needsUpdate);
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority, boolean needsUpdate) {
        this.priority = priority;
        this.needsDataUpdate.set(needsUpdate);
    }

    public JsonObject getUpdatedCustomData() {
        final JsonObject groupData = new JsonObject();

        groupData.addProperty("prefix", this.prefix);
        groupData.addProperty("suffix", this.suffix);
        groupData.addProperty("priority", this.priority);

        return groupData;
    }

    public String toString() {
        return "PermissionData{" +
                "prefix='" + prefix + '\'' +
                ", suffix='" + suffix + '\'' +
                ", priority=" + priority +
                ", permissions=" + permissions +
                ", needsDataUpdate=" + needsDataUpdate +
                ", needsPermissionsUpdate=" + needsPermissionsUpdate +
                '}';
    }
}
