package dulleh.akhyou;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dulleh.akhyou.anime.Anime;
import dulleh.akhyou.util.GeneralUtils;

public class MainModel {
    private static final String MAIN_STORAGE = "main_model";
    private static final String FINAL_ANIME = "final_anime";

    public static final String LATEST_VERSION_LINK = "https://api.github.com/gists/d67e3b97a672e8c3f544";
    public static final String LATEST_RELEASE_LINK = "https://github.com/dulleh/akhyou/blob/master/akhyou-latest.apk?raw=true";

    private SharedPreferences favoriteStorage;
    private SharedPreferences keyValueStorage;
    private Map<Integer, Anime> favorites;
    private Anime lastAnime;

    public MainModel(Context context) {
        this.favoriteStorage = context.getSharedPreferences(MAIN_STORAGE, Context.MODE_PRIVATE);
        this.keyValueStorage = context.getSharedPreferences(FINAL_ANIME, Context.MODE_PRIVATE);
        initializeFromPersistent();
    }

    public List<Anime> getFavorites() {
        return new ArrayList<>(favorites.values());
    }

    public boolean isFavorite(Anime anime) {
        return favorites.containsKey(anime.getHummingbirdId());
    }

    public void addFavorite(Anime favorite) {
        favorites.put(favorite.getHummingbirdId(), favorite);
        SharedPreferences.Editor editor = favoriteStorage.edit();
        editor.putString(Integer.toString(favorite.getHummingbirdId()),
                         GeneralUtils.encode(favorite));
        editor.apply();
    }

    public void removeFavorite(Anime anime) {
        favorites.remove(anime.getHummingbirdId());
        SharedPreferences.Editor editor = favoriteStorage.edit();
        editor.remove(Integer.toString(anime.getHummingbirdId()));
        editor.apply();
    }

    public Anime getLastAnime() {
        return lastAnime;
    }

    public void updateLastAnime(Anime anime) {
        lastAnime = anime;
        SharedPreferences.Editor editor = keyValueStorage.edit();
        editor.putString(FINAL_ANIME, GeneralUtils.encode(lastAnime));
        editor.apply();
    }


    private void initializeFromPersistent() {
        favorites = new HashMap<>();

        Map<String, ?> serialized = favoriteStorage.getAll();
        for (Map.Entry<String, ?> favorite : serialized.entrySet()) {
            Integer id = Integer.parseInt(favorite.getKey());
            String encodedAnime = (String)favorite.getValue();
            Anime anime = GeneralUtils.decode(encodedAnime, Anime.class);
            favorites.put(id, anime);
        }

        if (keyValueStorage.contains(FINAL_ANIME)) {
            lastAnime = GeneralUtils.decode(keyValueStorage.getString(FINAL_ANIME, ""), Anime.class);
        }
    }
}
