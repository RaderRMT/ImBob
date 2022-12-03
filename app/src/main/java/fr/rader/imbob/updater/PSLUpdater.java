package fr.rader.imbob.updater;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import fr.rader.imbob.utils.OS;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

public class PSLUpdater {

    private static final String GITHUB_VERSION_PATH = "https://raw.githubusercontent.com/RaderRMT/BobProtocols/master/version";
    private static final String GITHUB_REPO = "https://github.com/RaderRMT/BobProtocols";

    private static final String ASSETS_PATH = OS.getImBobFolder() + "assets/";
    private static final String LOCAL_VERSION_PATH = ASSETS_PATH + "version";

    public static final int INVALID_VERSION = -1;

    private int githubVersion;
    private String githubUpdateMessage;

    private int localVersion;
    private String localUpdateMessage;

    private boolean hasBeenUpdated = false;

    public PSLUpdater() {
        this.githubVersion = INVALID_VERSION;
        this.localVersion = INVALID_VERSION;
    }

    public void update() {
        if (!isUpdateAvailable()) {
            // if we're here and the local version or the github version is -1, this means we don't have internet
            if (this.localVersion == INVALID_VERSION && this.githubVersion == INVALID_VERSION) {
                System.out.println("Cannot download files!");
                System.exit(0);
            }

            return;
        }

        removeOldFiles();
        downloadUpdate();

        this.hasBeenUpdated = true;
    }

    public void removeOldFiles() {
        File assetsFolder = new File(ASSETS_PATH);
        if (!assetsFolder.exists()) {
            assetsFolder.mkdirs();
        }

        FileUtils.deleteQuietly(assetsFolder);
    }

    public void downloadUpdate() {
        try {
            Git.cloneRepository().setURI(GITHUB_REPO).setDirectory(new File(ASSETS_PATH)).call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }

    public void updateLatestVersionNumber() {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(GITHUB_VERSION_PATH).openConnection();
        } catch (IOException e) {
            e.printStackTrace();

            return;
        }

        JsonObject response = null;
        try (
                InputStream connectionStream = connection.getInputStream();
                InputStreamReader streamReader = new InputStreamReader(connectionStream)
        ) {
            response = new Gson().fromJson(streamReader, JsonObject.class);
        } catch (IOException e) {
            e.printStackTrace();

            connection.disconnect();
            return;
        }

        this.githubVersion = response.get("version").getAsInt();
        this.githubUpdateMessage = response.get("update_message").getAsString();
    }

    private void updateLocalVersion() {
        File file = new File(LOCAL_VERSION_PATH);
        if (!file.exists()) {
            return;
        }

        JsonObject response = null;

        try (
                FileReader streamReader = new FileReader(LOCAL_VERSION_PATH)
        ) {
            response = new Gson().fromJson(streamReader, JsonObject.class);
        } catch (IOException e) {
            e.printStackTrace();

            return;
        }

        this.localVersion = response.get("version").getAsInt();
        this.localUpdateMessage = response.get("update_message").getAsString();
    }

    public boolean isUpdateAvailable() {
        updateLatestVersionNumber();

        if (this.githubVersion == INVALID_VERSION) {
            return false;
        }

        if (this.localVersion == INVALID_VERSION) {
            updateLocalVersion();
        }

        return this.githubVersion > this.localVersion;
    }

    public String getLocalUpdateMessage() {
        return this.localUpdateMessage;
    }

    public String getGithubUpdateMessage() {
        return this.githubUpdateMessage;
    }

    public int getGithubVersion() {
        return this.githubVersion;
    }

    public int getLocalVersion() {
        return this.localVersion;
    }

    public boolean hasBeenUpdated() {
        return this.hasBeenUpdated;
    }
}
