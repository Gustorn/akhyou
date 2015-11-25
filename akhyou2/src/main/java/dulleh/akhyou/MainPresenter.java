package dulleh.akhyou;

import android.content.Context;
import android.os.Bundle;

import java.util.List;

import de.greenrobot.event.EventBus;
import dulleh.akhyou.event.FavoriteEvent;
import dulleh.akhyou.event.OpenAnimeEvent;
import dulleh.akhyou.event.SearchEvent;
import dulleh.akhyou.event.SearchSubmittedEvent;
import dulleh.akhyou.event.SnackbarEvent;
import dulleh.akhyou.anime.Anime;
import dulleh.akhyou.util.GeneralUtils;
import nucleus.presenter.RxPresenter;

public class MainPresenter extends RxPresenter<MainActivity> {
    private MainModel mainModel;

    public MainPresenter(Context context) {
        mainModel = new MainModel(context);
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainModel = null;
    }

    public Anime getLastAnime() {
        return mainModel.getLastAnime();
    }

    public MainModel getModel () {
        return mainModel;
    }

    public List<Anime> getFavourites () {
        return mainModel.getFavorites();
    }

    public void onFreshStart(MainActivity mainActivity) {
        mainActivity.requestFragment(MainActivity.SEARCH_FRAGMENT);
    }

    public void onEvent (FavoriteEvent event) {
        // colors are inconsistent for whatever reason, causing duplicate favourites,
        // so Set is pretty useless ;-;
        try {
            if (event.action == FavoriteEvent.Action.ADD) {
                mainModel.addFavorite(event.anime);
            } else {
                mainModel.removeFavorite(event.anime);
            }
            if (getView() != null) {
                getView().favoritesChanged();
            }
        } catch (Exception e) {
            postError(e);
        }
    }

    public void onEvent (SearchSubmittedEvent event) {
        if (getView() != null) {
            if (getView().getSupportFragmentManager().findFragmentByTag(MainActivity.ANIME_FRAGMENT) != null) {
                getView().getSupportFragmentManager().popBackStack();
            }
            if (getView().getSupportFragmentManager().findFragmentByTag(MainActivity.SEARCH_FRAGMENT) == null) {
                getView().requestFragment(MainActivity.SEARCH_FRAGMENT);
            }
        }
        EventBus.getDefault().postSticky(new SearchEvent(event.searchTerm));
    }

    public void onEvent(OpenAnimeEvent event) {
        mainModel.lastAnime = event.anime;
    }

    public void onEvent (SnackbarEvent event) {
        getView().showSnackBar(event);
    }

    public void postError (Throwable e) {
        e.printStackTrace();
        EventBus.getDefault().post(new SnackbarEvent(GeneralUtils.formatError(e)));
    }
}
