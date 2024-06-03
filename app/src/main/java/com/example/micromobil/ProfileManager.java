package com.example.micromobil;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProfileManager {
    private static ProfileManager instance;
    private List<Profile> profiles;
    private Context context;

    private ProfileManager(Context context) {
        this.context = context.getApplicationContext();
        profiles = new ArrayList<>();
        loadProfilesFromFile();
    }

    public static ProfileManager getInstance(Context context) {
        if (instance == null) {
            instance = new ProfileManager(context);
        }
        return instance;
    }

    public void addProfile(Profile profile) {
        profiles.add(profile);
        saveProfilesToFile();
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
            saveProfilesToFile();
        }
    }

    public void updateProfile(String oldName, String newName, Map<String, List<Integer>> drinkTemperatures) {
        for (Profile profile : profiles) {
            if (profile.getName().equals(oldName)) {
                profile.setName(newName);
                profile.setDrinkTemperatures(drinkTemperatures);
                saveProfilesToFile();
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

    private void saveProfilesToFile() {
        try {
            File file = new File(context.getFilesDir(), "profiles.dat");
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(profiles);
            oos.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProfilesFromFile() {
        try {
            File file = new File(context.getFilesDir(), "profiles.dat");
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis);
                profiles = (List<Profile>) ois.readObject();
                ois.close();
                fis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
