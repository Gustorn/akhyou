package dulleh.akhyou.anime;

import android.support.v7.graphics.Palette;

import de.greenrobot.event.EventBus;
import dulleh.akhyou.event.FavoriteEvent;
import dulleh.akhyou.event.OpenAnimeEvent;
import dulleh.akhyou.event.SnackbarEvent;
import dulleh.akhyou.util.GeneralUtils;
import nucleus.presenter.RxPresenter;

public class AnimePresenter extends RxPresenter<AnimeFragment> {
    private Anime anime;

    @Override
    protected void onTakeView(AnimeFragment view) {
        super.onTakeView(view);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().registerSticky(this);
        }

        if (anime != null) {
            view.setToolbarTitle(anime.getTitle());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void setMajorColour (Palette palette) {
        if (palette != null) {
            if (palette.getVibrantSwatch() != null) {
                anime.setMajorColour(palette.getVibrantSwatch().getRgb());
            } else if (palette.getLightVibrantSwatch() != null) {
                anime.setMajorColour(palette.getLightVibrantSwatch().getRgb());
            } else if (palette.getDarkMutedSwatch() != null) {
                anime.setMajorColour(palette.getDarkMutedSwatch().getRgb());
            }
        }
    }

    public void onFavouriteCheckedChanged(boolean b) {
        FavoriteEvent.Action action = b ? FavoriteEvent.Action.ADD : FavoriteEvent.Action.REMOVE;
        EventBus.getDefault().post(new FavoriteEvent(action, anime));
    }

    public void onEvent(OpenAnimeEvent event) {
        anime = event.anime;
        if (getView() != null) {
            getView().setAnime(anime);
        }
    }

    public void postError (Throwable e) {
        e.printStackTrace();
        EventBus.getDefault().post(new SnackbarEvent(GeneralUtils.formatError(e)));
    }
}