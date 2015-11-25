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

    public static final String LATEST_VERSION_LINK = "https://api.github.com/gists/d67e3b97a672e8c3f544";
    public static final String LATEST_RELEASE_LINK = "https://github.com/dulleh/akhyou/blob/master/akhyou-latest.apk?raw=true";

    private SharedPreferences sharedPreferences;
    private Map<Integer, Anime> favorites;
    private Anime lastAnime;

    public MainModel(Context context) {
        this.sharedPreferences = context.getSharedPreferences(MAIN_STORAGE, Context.MODE_PRIVATE);
        initializeFromPersistent();
    }

    public MainModel(Context context, Anime lastAnime) {
        this(context);
        this.lastAnime = lastAnime;
    }

    public Anime getLastAnime() {
        return lastAnime;
    }

    public List<Anime> getFavorites() {
        return new ArrayList<>(favorites.values());
    }

    public boolean isFavorite(Anime anime) {
        return favorites.containsKey(anime.getHummingbirdId());
    }

    public void addFavorite(Anime favorite) {
        favorites.put(favorite.getHummingbirdId(), favorite);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Integer.toString(favorite.getHummingbirdId()),
                         GeneralUtils.encode(favorite));
        editor.apply();
    }

    public void removeFavorite(Anime anime) {
        favorites.remove(anime.getHummingbirdId());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(Integer.toString(anime.getHummingbirdId()));
        editor.apply();
    }

    public void updateFavorite(Anime anime) {
        removeFavorite(anime);
        addFavorite(anime);
    }

    private void initializeFromPersistent() {
        favorites = new HashMap<>();

        Map<String, ?> serialized = sharedPreferences.getAll();
        for (Map.Entry<String, ?> favorite : serialized.entrySet()) {
            Integer id = Integer.parseInt(favorite.getKey());
            String encodedAnime = (String)favorite.getValue();
            Anime anime = GeneralUtils.decode(encodedAnime, Anime.class);
            favorites.put(id, anime);
        }
    }
}
