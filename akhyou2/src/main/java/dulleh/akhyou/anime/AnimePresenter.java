package dulleh.akhyou.anime;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.graphics.Palette;

import java.util.List;

import de.greenrobot.event.EventBus;
import dulleh.akhyou.episode.provider.AnimeProvider;
import dulleh.akhyou.episode.Source;
import dulleh.akhyou.episode.Video;
import dulleh.akhyou.event.FavoriteEvent;
import dulleh.akhyou.event.LastAnimeEvent;
import dulleh.akhyou.event.OpenAnimeEvent;
import dulleh.akhyou.event.SnackbarEvent;
import dulleh.akhyou.util.GeneralUtils;
import nucleus.presenter.RxPresenter;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AnimePresenter extends RxPresenter<AnimeFragment> {
    private static final String LAST_ANIME_BUNDLE_KEY = "last_anime";

    private Subscription episodeSubscription;
    private Subscription videoSubscription;
    private AnimeProvider animeProvider;

    private Anime anime;
    private boolean isFavorite;

    public boolean isRefreshing;
    private static boolean needToGiveFavouriteState = false;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        if (savedState != null && savedState.containsKey(LAST_ANIME_BUNDLE_KEY)) {
            anime = savedState.getParcelable(LAST_ANIME_BUNDLE_KEY);
        }

    }

    @Override
    protected void onTakeView(AnimeFragment view) {
        super.onTakeView(view);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().registerSticky(this);
        }

        view.updateRefreshing();

        if (anime != null) {
            view.setAnime(anime);
            view.setToolbarTitle(anime.getTitle());

            if (needToGiveFavouriteState) {
                view.setFavouriteChecked(view.isInFavourites(anime));
                needToGiveFavouriteState = false;
            }
        }
    }

    @Override
    protected void onSave(Bundle state) {
        super.onSave(state);
        if (anime != null) {
            state.putParcelable(LAST_ANIME_BUNDLE_KEY, anime);
            EventBus.getDefault().post(new LastAnimeEvent(anime));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        animeProvider = null;
        unsubscribe();
    }

    private void unsubscribe () {
        if (episodeSubscription != null && !episodeSubscription.isUnsubscribed()) {
            episodeSubscription.unsubscribe();
        }
        if (videoSubscription != null && !videoSubscription.isUnsubscribed()) {
            videoSubscription.unsubscribe();
        }
    }

    private Anime setAnimeProvider (Anime anime) {
        // TODO(gustorn): provider dispatch
        return anime;
    }

    public void onEvent (OpenAnimeEvent event) {
        anime = event.anime;
        getView().setAnime(anime);
        if (anime != null) {
            fetchAnime(false);
        } else {
            fetchAnime(false);
        }
    }

    public void fetchAnime(boolean updateCached) {
        isRefreshing = true;
        if (getView() != null) {
            getView().updateRefreshing();
        }
    }

    public void setNeedToGiveFavourite (boolean bool) {
        needToGiveFavouriteState = bool;
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

    public void downloadOrStream (Video video, boolean download) {
        if (download) {
            GeneralUtils.internalDownload((DownloadManager) getView().getActivity().getSystemService(Context.DOWNLOAD_SERVICE), video.getUrl());
        } else {
            postIntent(video.getUrl());
        }
    }

    private void postIntent (String videoUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(videoUrl), "video/*");
        if (intent.resolveActivity(getView().getActivity().getPackageManager()) != null) {
            getView().startActivity(intent);
        }
    }

    public void flipWatched (int position) {
        anime.getEpisode(position).flipWatched();
        getView().notifyAdapter();
    }

    public void fetchSources (Anime anime) {
        if (episodeSubscription != null) {
            if (!episodeSubscription.isUnsubscribed()) {
                episodeSubscription.unsubscribe();
            }
        }

        episodeSubscription = Observable.defer(() -> Observable.just(animeProvider.fetchSources(anime)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.deliver())
                .subscribe(new Subscriber<List<Source>>() {
                    @Override
                    public void onNext(List<Source> sources) {
                        getView().showSourcesDialog(sources);
                        episodeSubscription.unsubscribe();
                    }

                    @Override
                    public void onCompleted() {
                        // should be using Observable.just() as onCompleted is never called
                        // and it only runs once.
                    }

                    @Override
                    public void onError(Throwable e) {
                        postError(e);
                        this.unsubscribe();
                    }

                });
    }

    public void fetchVideo (Source source, boolean download) {
        if (videoSubscription != null) {
            if (!videoSubscription.isUnsubscribed()) {
                videoSubscription.unsubscribe();
            }
        }

        videoSubscription = Observable.defer(() -> Observable.just(animeProvider.fetchVideo(source)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.deliver())
                // this subscriber stays here because it needs the 'lazyDownload'
                .subscribe(new Subscriber<Source>() {
                    @Override
                    public void onNext(Source source) {
                        getView().shareVideo(source, download);
                        this.unsubscribe();
                    }

                    @Override
                    public void onCompleted() {
                        // should be using Observable.just() as onCompleted is never called
                        // and it only runs once.
                    }

                    @Override
                    public void onError(Throwable e) {
                        postError(e);
                        this.unsubscribe();
                    }

                });
    }

    public void postError (Throwable e) {
        e.printStackTrace();
        EventBus.getDefault().post(new SnackbarEvent(GeneralUtils.formatError(e)));
    }

    public void postSuccess (String successMessage) {
        EventBus.getDefault().post(new SnackbarEvent(successMessage));
    }

}