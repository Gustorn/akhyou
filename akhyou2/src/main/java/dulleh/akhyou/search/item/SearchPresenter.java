package dulleh.akhyou.search.item;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import dulleh.akhyou.event.SearchEvent;
import dulleh.akhyou.event.SnackbarEvent;
import dulleh.akhyou.anime.Anime;
import dulleh.akhyou.search.SearchHolderFragment;
import dulleh.akhyou.search.provider.HummingbirdSearchProvider;
import dulleh.akhyou.search.provider.SearchProvider;
import dulleh.akhyou.util.GeneralUtils;
import nucleus.presenter.RxPresenter;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SearchPresenter extends RxPresenter<SearchFragment> {
    private Subscription subscription;
    private SearchProvider searchProvider;

    private String searchTerm;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        searchProvider = new HummingbirdSearchProvider();
    }

    @Override
    protected void onTakeView(SearchFragment view) {
        super.onTakeView(view);
        subscribe();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        searchProvider = null;
        unsubscribe();
    }

    private void subscribe () {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().registerSticky(this);
        }
    }

    private void unsubscribe () {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        EventBus.getDefault().unregister(this);
    }

    public void onEvent (SearchEvent event) {
        this.searchTerm = event.searchTerm;
        getView().startLoading();
        search();
    }

    public void search () {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }

        subscription = Observable
            .defer(() -> Observable.just(searchProvider.searchFor(searchTerm)))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .compose(this.deliver())
            .subscribe(new Subscriber<List<Anime>>() {
                @Override
                public void onNext(List<Anime> results) {
                    SearchHolderFragment.setSearchResults(results);
                    getView().finishLoading();
                    this.unsubscribe();
                }

                @Override
                public void onCompleted() {
                }

                @Override
                public void onError(Throwable e) {
                    getView().finishLoading();
                    SearchHolderFragment.setSearchResults(new ArrayList<>(0));
                    getView().updateSearchResults();
                    postError(e);
                    this.unsubscribe();
                }
            });
    }

    public void postError (Throwable e) {
        e.printStackTrace();
        EventBus.getDefault().post(new SnackbarEvent(GeneralUtils.formatError(e)));
    }

    public void postSuccess () {
        EventBus.getDefault().post(new SnackbarEvent("SUCCESS"));
    }

}
