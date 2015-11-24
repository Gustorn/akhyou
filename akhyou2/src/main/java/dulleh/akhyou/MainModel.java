package dulleh.akhyou;

import android.content.SharedPreferences;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import dulleh.akhyou.Models.Anime;
import dulleh.akhyou.Models.HummingbirdApi;
import dulleh.akhyou.Utils.GeneralUtils;
import rx.exceptions.OnErrorThrowable;

public class MainModel {
    private static final String FAVOURITES_PREF = "favourites_preference";
    private static final String LAST_ANIME_PREF = "last_anime_preference";
    public static final String AUTO_UPDATE_PREF = "should_auto_update_preference";
    public static final String OPEN_TO_LAST_ANIME_PREF ="open_to_last_anime_preference";

    private static final String LATEST_VERSION_LINK = "https://api.github.com/gists/d67e3b97a672e8c3f544";
    public static final String LATEST_RELEASE_LINK = "https://github.com/dulleh/akhyou/blob/master/akhyou-latest.apk?raw=true";

    private SharedPreferences sharedPreferences;
    private HummingbirdApi hummingbirdApi;
    // The key is the anime url.
    private HashMap<Integer, Anime> favouritesMap;
    private Anime lastAnime;
    public static boolean openToLastAnime = true;

    public void setSharedPreferences(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public MainModel (SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
        hummingbirdApi = new HummingbirdApi();
        refreshFavourites();
        refreshLastAnime();
        refreshOpenToLastAnime();
    }

    public void refreshFavourites () {
        Set<String> favourites = new HashSet<>(sharedPreferences.getStringSet(FAVOURITES_PREF, new HashSet<>()));

        favouritesMap = new HashMap<>(favourites.size());
        for (String favourite : favourites) {
            Anime anime = deserializeAnime(favourite);
            if (anime != null) {
                favouritesMap.put(anime.getHummingbirdId(), anime);
            }
        }
    }

    public void refreshLastAnime () {
        // need to check for null or else deserialize will throw null pointer exception
        String serializedAnime = sharedPreferences.getString(LAST_ANIME_PREF, null);
        if (serializedAnime != null) {
            lastAnime = deserializeAnime(serializedAnime);
        }
    }

    public void refreshOpenToLastAnime () {
        openToLastAnime = sharedPreferences.getBoolean(OPEN_TO_LAST_ANIME_PREF, true);
    }

    public boolean updateLastAnimeAndFavourite (Anime anime) {
        saveNewLastAnime(anime);
           // THIS METHOD IS BEING EXECUTED
        return updateFavourite(anime);
    }


    /*
    *
    *               FAVOURITES
    *
    */


    public void setFavourites (ArrayList<Anime> favourites) {
        for (Anime favourite : favourites) {
            favouritesMap.put(favourite.getHummingbirdId(), favourite);
        }
    }

    public ArrayList<Anime> getFavourites () {
        if (favouritesMap != null) {
            ArrayList<Anime> favourites = new ArrayList<>();
            favourites.addAll(favouritesMap.values());
            return favourites;
        } else if (sharedPreferences != null) {
            refreshFavourites();
            ArrayList<Anime> favourites = new ArrayList<>();
            favourites.addAll(favouritesMap.values());
            return favourites;
        }
        return null;
    }

    public void saveFavourites () {
        Set<String> favourites = new HashSet<>(favouritesMap.size());
        for (Anime anime : favouritesMap.values()) {
            favourites.add(serializeAnime(anime));
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(FAVOURITES_PREF, favourites);
        editor.apply();
    }

    public boolean isInFavourites (int id)  throws IllegalStateException {
        if (favouritesMap == null && sharedPreferences != null) {
            refreshFavourites();
        }
        if (favouritesMap != null) {
            return (favouritesMap.containsKey(id));
        }
        throw new IllegalStateException("Can't find favourites.");
    }

    public void addToFavourites (Anime anime) {
        if (favouritesMap == null) {
            refreshFavourites();
        }
        favouritesMap.put(anime.getHummingbirdId(), anime);
    }

    public void removeFromFavourites (Anime anime) {
        if (favouritesMap == null) {
            refreshFavourites();
        }
        favouritesMap.remove(anime.getHummingbirdId());
    }

    // Returns true if a favourite was updated. False if not.
    public boolean updateFavourite (Anime favourite) {
        if (favouritesMap.containsKey(favourite.getHummingbirdId())) {
            favouritesMap.put(favourite.getHummingbirdId(), favourite);
            return true;
        }
        return false;
    }


    /*
    *
    *               LAST ANIME
    *
    */


    public Anime getLastAnime () {
        return lastAnime;
    }

    public void saveNewLastAnime (Anime anime) {
        if (sharedPreferences != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(LAST_ANIME_PREF, serializeAnime(anime));
            editor.apply();
        }
    }


    /*
    *
    *               UTILS
    *
    */


    private String serializeAnime (Anime anime) {
        return GeneralUtils.serializeAnime(anime);
    }

    private Anime deserializeAnime (String serializedAnime) {
        return GeneralUtils.deserializeAnime(serializedAnime);
    }

    public boolean shouldAutoUpdate() {
        return sharedPreferences.getBoolean(AUTO_UPDATE_PREF, true);
    }

    // returns update's version
    public String isUpdateAvailable() {
        String versionName = BuildConfig.VERSION_NAME;

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readValue(GeneralUtils.getWebPage(LATEST_VERSION_LINK), JsonNode.class);
            String newUpdateVersion = rootNode.get("description").textValue();
            if (!newUpdateVersion.equals(versionName)) {
                return newUpdateVersion + rootNode.get("files").get("latestRelease").get("content").textValue();
            } else {
                return null;
            }
        } catch (IOException io) {
            throw OnErrorThrowable.from(new Throwable("Checking update failed."));
        }
    }


    /*
    *
    *               HUMMINGBIRD
    *
    */


    public String loginHummingbird () {
        return hummingbirdApi.getAuthToken("username", "password");
    }

}
