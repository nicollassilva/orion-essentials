package dev.thewarrior.Components.Data;

import dev.thewarrior.Utils.NamedLocation;

import java.util.Arrays;

public class PlayerHomes {
    private NamedLocation[] homes = new NamedLocation[0];

    public NamedLocation[] getHomes() {
        return Arrays.stream(homes).filter(location -> location != null && location.getName() != null && !location.getName().isEmpty()).toArray(NamedLocation[]::new);
    }

    public void setHomes(NamedLocation[] homes) {
        this.homes = homes;
    }

    public void addHome(NamedLocation home) {
        NamedLocation[] newHomes = new NamedLocation[homes.length + 1];

        System.arraycopy(homes, 0, newHomes, 0, homes.length);
        newHomes[homes.length] = home;

        this.homes = newHomes;
    }

    public boolean hasHome(String name) {
        for (NamedLocation home : homes) {
            if (home.getName() != null && home.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }

    public void removeHome(String name) {
        NamedLocation[] newHomes = new NamedLocation[homes.length - 1];
        int index = 0;

        for (NamedLocation home : homes) {
            if (home.getName() != null && !home.getName().equalsIgnoreCase(name)) {
                newHomes[index++] = home;
            }
        }

        this.homes = Arrays.stream(newHomes).filter(location -> location != null && location.getName() != null && !location.getName().isEmpty()).toArray(NamedLocation[]::new);
    }

    public NamedLocation getHome(String name) {
        for (NamedLocation home : homes) {
            if (home.getName() != null && home.getName().equalsIgnoreCase(name)) {
                return home;
            }
        }

        return null;
    }
}
