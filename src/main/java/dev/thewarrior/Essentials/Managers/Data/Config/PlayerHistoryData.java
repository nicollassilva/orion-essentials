package dev.thewarrior.Essentials.Managers.Data.Config;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.Set;
import java.util.UUID;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public class PlayerHistoryData {
    private Set<UUID> history = new ObjectOpenHashSet<>();

    public Set<UUID> getHistory() {
        return this.history;
    }

    public boolean containsHistory(UUID history) {
        return this.history.contains(history);
    }

    public void addHistory(UUID history) {
        this.history.add(history);
    }
}
