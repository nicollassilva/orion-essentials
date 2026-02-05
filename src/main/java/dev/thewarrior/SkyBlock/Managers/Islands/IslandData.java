package dev.thewarrior.SkyBlock.Managers.Islands;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.thewarrior.SkyBlock.Managers.Islands.Data.IslandMemberData;
import dev.thewarrior.SkyBlock.Managers.Islands.Data.IslandLastVisitData;
import dev.thewarrior.SkyBlock.Managers.Islands.Data.IslandSettings;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings(value = { "FieldMayBeFinal", "FieldCanBeLocal" })
public class IslandData {
    private UUID id;
    private String name;
    private UUID ownerId;

    private Vector3d spawnLocation;
    private Vector3d spawnRotation;
    private String worldName;

    private String enterTitle;

    private int level = 1;
    private double experience = 0d;

    private IslandSettings settings;
    private long createdAt;

    private ObjectArrayList<IslandLastVisitData> lastVisitData;
    private ObjectArrayList<IslandMemberData> members;

    private transient AtomicBoolean needsUpdate;

    public IslandData(UUID ownerId, String worldName, Vector3d spawnLocation, Vector3d spawnRotation) {
        this.id = UUID.randomUUID();
        this.name = "Default";
        this.ownerId = ownerId;
        this.worldName = worldName;
        this.spawnLocation = spawnLocation.clone();
        this.spawnRotation = spawnRotation.clone();
        this.settings = new IslandSettings();
        this.createdAt = System.currentTimeMillis();

        if(!worldName.endsWith("1")) {
            this.name += worldName.substring(worldName.length() - 1);
        }

        this.lastVisitData = new ObjectArrayList<>();
        this.members = new ObjectArrayList<>();
        this.needsUpdate = new AtomicBoolean(false);

        this.needsUpdate.set(true);
    }

    public UUID getId() {
        return this.id;
    }

    public Vector3d getSpawnLocation() {
        return this.spawnLocation;
    }

    public void setSpawnLocation(Vector3d spawnLocation) {
        this.spawnLocation = spawnLocation;
        this.setNeedsUpdate(true);
    }

    public Vector3d getSpawnRotation() {
        return this.spawnRotation;
    }

    public void setSpawnRotation(Vector3d spawnRotation) {
        this.spawnRotation = spawnRotation;
        this.setNeedsUpdate(true);
    }

    public String getWorldName() {
        return this.worldName;
    }

    public String getName() {
        return this.name;
    }

    public String getEnterTitle() {
        return this.enterTitle;
    }

    public void setEnterTitle(String enterTitle) {
        this.enterTitle = enterTitle;
        this.setNeedsUpdate(true);
    }

    public void setName(String name) {
        this.name = name;

        this.setNeedsUpdate(true);
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;

        this.setNeedsUpdate(true);
    }

    public double getExperience() {
        return this.experience;
    }

    public void setExperience(double experience) {
        this.experience = experience;
        this.setNeedsUpdate(true);
    }

    public IslandLastVisitData getLastVisitData() {
        if(this.lastVisitData == null || this.lastVisitData.isEmpty()) return null;

        return this.lastVisitData.getLast();
    }

    public void addLastVisitData(IslandLastVisitData lastVisitData) {
        final IslandLastVisitData lastVisit = this.getLastVisitData();

        if(lastVisit != null
                && lastVisit.getUsername().equals(lastVisitData.getUsername())
                && (System.currentTimeMillis() - lastVisit.getTime()) < 300000
        ) return;

        this.addLastVisitDataInternal(lastVisit);
    }

    private void addLastVisitDataInternal(IslandLastVisitData lastVisitData) {
        if(this.lastVisitData == null) this.lastVisitData = new ObjectArrayList<>();

        if(this.lastVisitData.size() >= 20) {
            this.lastVisitData.removeFirst();
        }

        this.lastVisitData.add(lastVisitData);
        this.setNeedsUpdate(true);
    }

    public ObjectArrayList<IslandMemberData> getMembers() {
        return this.members;
    }

    public boolean isMember(UUID friendUuid) {
        if(this.members == null || this.members.isEmpty()) return false;

        for (IslandMemberData friendData : this.members) {
            if(friendData.getUuid().equals(friendUuid)) {
                return true;
            }
        }

        return false;
    }

    public boolean addMember(PlayerRef playerRef) {
        if(this.members == null) {
            this.members = new ObjectArrayList<>();
        }

        if(this.isMember(playerRef.getUuid())) return false;
        if(this.members.size() >= 5) return false;

        this.members.add(new IslandMemberData(playerRef));

        this.setNeedsUpdate(true);

        return true;
    }

    public void removeMember(UUID friendUuid) {
        if(this.members == null) return;

        this.members.removeIf(friendData -> friendData.getUuid().equals(friendUuid));

        this.setNeedsUpdate(true);
    }

    /**
     * Getters are necessary because AtomicBoolean is transient, so after deserialization it can be null.
     */
    public boolean needsUpdate() {
        if(this.needsUpdate == null) {
            this.needsUpdate = new AtomicBoolean(false);
        }

        return this.needsUpdate.getAndSet(false);
    }

    public void setNeedsUpdate(boolean needsUpdate) {
        if(this.needsUpdate == null) {
            this.needsUpdate = new AtomicBoolean(needsUpdate);
        } else {
            this.needsUpdate.set(needsUpdate);
        }
    }
    /*********/

    public long getCreatedAt() {
        return this.createdAt;
    }

    public UUID getOwnerId() {
        return this.ownerId;
    }

    public IslandSettings getSettings() {
        return this.settings;
    }
}
