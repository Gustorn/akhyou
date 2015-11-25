package dulleh.akhyou.anime;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.greenrobot.event.EventBus;
import dulleh.akhyou.MainActivity;
import dulleh.akhyou.MainApplication;
import dulleh.akhyou.R;
import dulleh.akhyou.episode.Source;
import dulleh.akhyou.episode.Video;
import dulleh.akhyou.event.SearchSubmittedEvent;
import dulleh.akhyou.util.AdapterClickListener;
import nucleus.factory.RequiresPresenter;
import nucleus.view.NucleusSupportFragment;

@RequiresPresenter(AnimePresenter.class)
public class AnimeFragment extends NucleusSupportFragment<AnimePresenter> {
    private AnimeAdapter animeAdapter;
    private RelativeLayout relativeLayout;
    private SwipeRefreshLayout refreshLayout;
    private SearchView searchView;

    private Anime anime;

    public AnimeFragment(Anime anime) {
        this.anime = anime;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.anime_header, container, false);

        RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(R.id.anime_fragment_top_level);

        ImageView coverImage = (ImageView) view.findViewById(R.id.anime_image_view);
        TextView synopsis = (TextView) view.findViewById(R.id.anime_desc_view);
        TextView genres = (TextView) view.findViewById(R.id.anime_genres_view);
        TextView alternateTitle = (TextView) view.findViewById(R.id.anime_alternate_title_view);
        TextView date = (TextView) view.findViewById(R.id.anime_date_view);
        TextView status = (TextView) view.findViewById(R.id.anime_status_view);
        FloatingActionButton favoriteButton = (FloatingActionButton) view.findViewById(R.id.favourite_fab);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext(), LinearLayout.VERTICAL, false));
        recyclerView.setAdapter(animeAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_layout);
        refreshLayout.setColorSchemeResources(R.color.accent);
        refreshLayout.setOnRefreshListener(() -> getPresenter().fetchAnime(false));

        updateRefreshing();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        setToolbarTitle(null);
        if (searchView != null) {
            searchView.setOnQueryTextListener(null);
        }
        MainApplication.getRefWatcher(getActivity()).watch(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (getView() != null) {
            super.onCreateOptionsMenu(menu, inflater);

            MenuItem searchItem = menu.findItem(R.id.search_item);

            if (searchItem == null) {
                inflater.inflate(R.menu.search_menu, menu);
                searchItem = menu.findItem(R.id.search_item);
            }

            searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

            searchView.setQueryHint(getString(R.string.search_item));
            searchView.setIconifiedByDefault(true);
            searchView.setIconified(true);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (!query.isEmpty()) {
                        EventBus.getDefault().post(new SearchSubmittedEvent(query));
                        searchView.clearFocus();
                        refreshLayout.requestFocus();
                    }
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
            searchView.clearFocus();
            refreshLayout.requestFocus();
        }
    }

    public void setAnime(Anime anime) {
        Picasso.with(getActivity()).invalidate(anime.getImageUrl());
        animeAdapter.setAnime(anime, isInFavourites(anime));
        setToolbarTitle(anime.getTitle());
        getPresenter().setNeedToGiveFavourite(false);
        updateRefreshing();
    }

    // returns false if it cannot check.
    public boolean isInFavourites(Anime anime) {
        try {
            return ((MainActivity) getActivity()).getPresenter().getModel().isFavorite(anime);
        } catch (IllegalStateException e) {
            getPresenter().postError(e);
            return false;
        }
    }


    public void notifyAdapter() {
        animeAdapter.notifyDataSetChanged();
    }

    public void updateRefreshing() {
        if (!isRefreshing() && getPresenter().isRefreshing) {
            TypedValue typedValue = new TypedValue();
            getActivity().getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typedValue, true);
            refreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typedValue.resourceId));
            refreshLayout.setRefreshing(true);
        } else if (isRefreshing() && !getPresenter().isRefreshing) {
            refreshLayout.setRefreshing(false);
        }
    }

    public boolean isRefreshing() {
        return refreshLayout.isRefreshing();
    }

    public void setToolbarTitle(String title) {
        ActionBar actionBar = ((MainActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    public void setFavouriteChecked(boolean isInFavourites) {
        animeAdapter.matchFavoriteStatus(isInFavourites);
        getPresenter().setNeedToGiveFavourite(false);
    }

    @Override
    public void onCLick(Anime anime, @Nullable Integer position) {
        getPresenter().fetchSources(null); //episode);
        this.position = position;
    }

    @Override
    public void onLongClick(Anime anime, @Nullable Integer position) {
        if (position != null) {
            getPresenter().flipWatched(position);
        }
    }

    public void showSourcesDialog(List<Source> sources) {
        if (sources.size() >= 1) {
            TypedValue typedValue = new TypedValue();
            getActivity().getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
            int accentColor = typedValue.data;

            new MaterialDialog.Builder(getActivity())
                    .title(getString(R.string.sources))
                    .items(getSourcesAsCharSequenceArray(sources))
                    .itemsCallbackSingleChoice(0, (materialDialog, view, i, charSequence) -> true)
                    .callback(new MaterialDialog.ButtonCallback() {

                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            getPresenter().fetchVideo(sources.get(dialog.getSelectedIndex()), false);
                            if (position != null) {
                                animeAdapter.setWatched(position);
                            }
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            super.onNegative(dialog);
                            getPresenter().fetchVideo(sources.get(dialog.getSelectedIndex()), true);
                            if (position != null) {
                                animeAdapter.setWatched(position);
                            }
                        }

                        @Override
                        public void onNeutral(MaterialDialog dialog) {
                            super.onNeutral(dialog);
                            position = null;
                        }

                    })
                    .widgetColor(accentColor)
                    .positiveText(R.string.stream)
                    .positiveColor(accentColor)
                    .negativeText(R.string.download)
                    .negativeColor(accentColor)
                    .neutralText(R.string.cancel)
                    .neutralColorRes(R.color.grey_darkestXX)
                    .cancelable(true)
                    .show();
        } else {
            getPresenter().postError(new Throwable("Error: No sources found."));
        }
    }

    private void showVideosDialog(List<Video> videos, boolean download) {
        new MaterialDialog.Builder(getActivity())
                .title(getString(R.string.quality))
                .items(getVideosAsCharSequenceArray(videos))
                .itemsCallback((materialDialog, view, i, charSequence)
                    -> getPresenter().downloadOrStream(videos.get(i), download))
                .show();
    }

    public void showImageDialog() {
        getActivity().getLayoutInflater().inflate(R.layout.image_dialog_content, relativeLayout);

        ImageView imageView = (ImageView) getActivity().findViewById(R.id.image_dialog_image_view);
        imageView.setOnClickListener(view -> relativeLayout.removeView(imageView));

        Picasso.with(getActivity())
                .load(getPresenter().lastAnime.getImageUrl())
                .fit()
                .centerInside()
                .into(imageView);
    }

    private CharSequence[] getSourcesAsCharSequenceArray(List<Source> sources) {
        CharSequence[] sourcesAsArray = new CharSequence[sources.size()];
        for (int i = 0; i < sources.size(); i++) {
            sourcesAsArray[i] = sources.get(i).getTitle();
        }
        return sourcesAsArray;
    }

    private CharSequence[] getVideosAsCharSequenceArray(List<Video> videos) {
        CharSequence[] videosAsArray = new CharSequence[videos.size()];
        for (int i = 0; i < videos.size(); i++) {
            videosAsArray[i] = videos.get(i).getTitle();
        }
        return videosAsArray;
    }

    public void shareVideo(Source source, boolean download) {
        if (source.getVideos().size() == 1) {
            getPresenter().downloadOrStream(source.getVideos().get(0), download);
        } else {
            showVideosDialog(source.getVideos(), download);
        }
    }
}
