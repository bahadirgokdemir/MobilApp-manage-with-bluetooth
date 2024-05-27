package com.example.micromobil;

import java.util.ArrayList;
import java.util.List;

public class ProfileManager {
    private static ProfileManager instance;
    private List<Profile> profiles;

    private ProfileManager() {
        profiles = new ArrayList<>();
    }

    public static ProfileManager getInstance() {
        if (instance == null) {
            instance = new ProfileManager();
        }
        return instance;
    }

    public void addProfile(Profile profile) {
        profiles.add(profile);
    }

    public void removeProfile(String name) {
        Profile profileToRemove = null;
        for (Profile profile : profiles) {
            if (profile.getName().equals(name)) {
                profileToRemove = profile;
                break;
            }
        }
        if (profileToRemove != null) {
            profiles.remove(profileToRemove);
        }
    }

    public void updateProfile(String oldName, String newName, String newType) {
        for (Profile profile : profiles) {
            if (profile.getName().equals(oldName)) {
                profile.setName(newName);
                profile.setType(newType);
                break;
            }
        }
    }

    public List<Profile> getProfiles() {
        return profiles;
    }

    public Profile getProfile(String name) {
        for (Profile profile : profiles) {
            if (profile.getName().equals(name)) {
                return profile;
            }
        }
        return null;
    }

    public Profile getProfileByName(String name) {
        for (Profile profile : profiles) {
            if (profile.getName().equals(name)) {
                return profile;
            }
        }
        return null;
    }
}
