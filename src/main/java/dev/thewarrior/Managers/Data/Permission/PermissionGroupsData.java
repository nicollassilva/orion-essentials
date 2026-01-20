package dev.thewarrior.Managers.Data.Permission;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Map;
import java.util.function.BiConsumer;

public class PermissionGroupsData {
    private Map<String, PermissionData> groups;

    public PermissionData addGroup(String groupName) {
        if(this.groups.containsKey(groupName)) {
            return this.groups.get(groupName);
        }

        return this.groups.put(groupName, new PermissionData(new ObjectArrayList<>()));
    }

    public Map<String, PermissionData> getGroups() {
        return groups;
    }

    public PermissionData getGroupData(final String groupName) {
        return this.groups.getOrDefault(groupName, null);
    }

    public boolean hasGroup(final String groupName) {
        return this.groups.containsKey(groupName);
    }

    public void getFilteredGroups(String search, BiConsumer<String, PermissionData> filter) {
        if(search == null || search.isEmpty()) return;

        for(Map.Entry<String, PermissionData> entry : this.groups.entrySet()) {
            if(entry.getKey().toLowerCase().contains(search.toLowerCase())) {
                groups.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public void syncGroupData(String groupName, JsonObject element) {
        final PermissionData existingData = this.groups.getOrDefault(groupName, null);

        if(existingData == null) return;

        existingData.setPrefix(element.has("prefix") ? element.get("prefix").getAsString() : "");
        existingData.setSuffix(element.has("suffix") ? element.get("suffix").getAsString() : "");
        existingData.setPriority(element.has("priority") ? element.get("priority").getAsInt() : 0);
    }

    public void syncGroupData(String groupName, String prefix, String suffix, Integer priority) {
        if(!this.groups.containsKey(groupName)) return;

        final PermissionData existingData = this.groups.get(groupName);

        if (prefix != null) {
            existingData.setPrefix(prefix);
        }
        if (suffix != null) {
            existingData.setSuffix(suffix);
        }
        if (priority != null) {
            existingData.setPriority(priority);
        }
    }
}
