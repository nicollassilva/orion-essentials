package dev.thewarrior.SkyBlock.Managers.Islands;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.thewarrior.SkyBlock.Managers.Islands.Data.IslandFriendData;
import dev.thewarrior.SkyBlock.Managers.Islands.Data.IslandLastVisitData;
import dev.thewarrior.SkyBlock.Managers.Islands.Data.IslandSettings;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private IslandLastVisitData lastVisitData;

    private transient AtomicBoolean needsUpdate = new AtomicBoolean(false);

    private IslandSettings settings;

    private ObjectArrayList<IslandFriendData> friends = new ObjectArrayList<>();

    public IslandData(UUID ownerId, String worldName, Vector3d spawnLocation, Vector3d spawnRotation) {
        this.id = UUID.randomUUID();
        this.name = "Default";
        this.ownerId = ownerId;
        this.worldName = worldName;
        this.spawnLocation = spawnLocation.clone();
        this.spawnRotation = spawnRotation.clone();
        this.settings = new IslandSettings();

        if(!worldName.endsWith("1")) {
            this.name += worldName.substring(worldName.length() - 1);
        }

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
        return this.lastVisitData;
    }

    public void setLastVisitData(IslandLastVisitData lastVisitData) {
        this.lastVisitData = lastVisitData;
        this.setNeedsUpdate(true);
    }

    public ObjectArrayList<IslandFriendData> getFriends() {
        return this.friends;
    }

    public boolean isFriend(UUID friendUuid) {
        if(this.friends == null) return false;

        for (IslandFriendData friendData : this.friends) {
            if(friendData.getUuid().equals(friendUuid)) {
                return true;
            }
        }

        return false;
    }

    public void addFriend(PlayerRef playerRef, String nickname) {
        if(this.isFriend(playerRef.getUuid())) return;

        this.friends.add(new IslandFriendData(playerRef.getUuid(), nickname));

        this.setNeedsUpdate(true);
    }

    public void removeFriend(UUID friendUuid) {
        if(this.friends == null) return;

        this.friends.removeIf(friendData -> friendData.getUuid().equals(friendUuid));

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

    public UUID getOwnerId() {
        return this.ownerId;
    }

    public IslandSettings getSettings() {
        return this.settings;
    }
}
