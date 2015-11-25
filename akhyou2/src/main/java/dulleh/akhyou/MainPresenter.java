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

    public List<Anime> getFavourites() {
        return mainModel.getFavorites();
    }

    public MainModel getModel() {
        return mainModel;
    }

    public void onFreshStart(MainActivity mainActivity) {
        mainActivity.requestFragment(MainActivity.SEARCH_FRAGMENT);
    }

    public void onEvent(FavoriteEvent event) {
        boolean isFavorite = mainModel.isFavorite(event.anime);
        try {
            switch (event.action) {
                case ADD:
                    mainModel.addFavorite(event.anime);
                    break;
                case REMOVE:
                    mainModel.removeFavorite(event.anime);
                    break;
                case TOGGLE:
                    if (isFavorite)
                        mainModel.removeFavorite(event.anime);
                    else
                        mainModel.addFavorite(event.anime);
                    break;

            }
            if (getView() != null) {
                getView().favoritesChanged();
            }
        } catch (Exception e) {
            postError(e);
        }
    }

    public void onEvent(SearchSubmittedEvent event) {
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
        mainModel.updateLastAnime(event.anime);
    }

    public void onEvent(SnackbarEvent event) {
        getView().showSnackBar(event);
    }

    public void postError (Throwable e) {
        e.printStackTrace();
        EventBus.getDefault().post(new SnackbarEvent(GeneralUtils.formatError(e)));
    }
}
