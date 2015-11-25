package dulleh.akhyou.anime;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import de.greenrobot.event.EventBus;
import dulleh.akhyou.MainActivity;
import dulleh.akhyou.MainApplication;
import dulleh.akhyou.R;
import dulleh.akhyou.event.FavoriteAction;
import dulleh.akhyou.event.FavoriteEvent;
import dulleh.akhyou.event.SearchSubmittedEvent;
import dulleh.akhyou.util.PaletteTransform;
import nucleus.factory.RequiresPresenter;
import nucleus.view.NucleusSupportFragment;

@RequiresPresenter(AnimePresenter.class)
public class AnimeFragment extends NucleusSupportFragment<AnimePresenter> {
    private ImageView coverImage;
    private TextView synopsis;
    private TextView genres;
    private TextView alternateTitle;
    private TextView date;
    private TextView status;
    private FloatingActionButton favoriteButton;

    private SearchView searchView;
    private CoordinatorLayout animeDetails;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.anime_header, container, false);

        animeDetails = (CoordinatorLayout) view.findViewById(R.id.anime_details);
        coverImage = (ImageView) view.findViewById(R.id.anime_image_view);
        synopsis = (TextView) view.findViewById(R.id.anime_desc_view);
        genres = (TextView) view.findViewById(R.id.anime_genres_view);
        alternateTitle = (TextView) view.findViewById(R.id.anime_alternate_title_view);
        date = (TextView) view.findViewById(R.id.anime_date_view);
        status = (TextView) view.findViewById(R.id.anime_status_view);
        favoriteButton = (FloatingActionButton) view.findViewById(R.id.favourite_fab);

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
                        animeDetails.requestFocus();
                    }
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
            searchView.clearFocus();
            animeDetails.requestFocus();
        }
    }

    public void setAnime(Anime anime) {
        PaletteTransform paletteTransform = new PaletteTransform();
        Picasso.with(getActivity())
            .load(anime.getImageUrl())
            .error(R.drawable.placeholder)
            .fit()
            .centerCrop()
            .transform(paletteTransform)
            .into(coverImage, new Callback.EmptyCallback() {
                @Override
                public void onSuccess() {
                    getPresenter().setMajorColour(paletteTransform.getPallete());
                }
            });

        genres.setText(anime.getGenreString());
        synopsis.setText(anime.getSynopsis());
        alternateTitle.setText(anime.getAlternateTitle());
        date.setText(anime.getAiringString());
        status.setText(anime.getStatus());
        favoriteButton.setImageDrawable(favoriteIcon(anime));
        setToolbarTitle(anime.getTitle());

        favoriteButton.setOnClickListener(e -> {
            toggleFavorite(anime);
            favoriteButton.setImageDrawable(favoriteIcon(anime));
        });
    }

    public void setToolbarTitle(String title) {
        ActionBar actionBar = ((MainActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    private void toggleFavorite(Anime anime) {
        ((MainActivity) getActivity())
            .getPresenter()
            .onEvent(new FavoriteEvent(FavoriteAction.TOGGLE, anime));
    }

    private boolean isFavorite(Anime anime) {
        try {
            return ((MainActivity) getActivity()).getPresenter().getModel().isFavorite(anime);
        } catch (IllegalStateException e) {
            getPresenter().postError(e);
            return false;
        }
    }

    private Drawable favoriteIcon(Anime anime) {
        if (isFavorite(anime)) {
            return ContextCompat.getDrawable(getContext(), R.drawable.ic_favorite_white_24dp);
        } else {
            return ContextCompat.getDrawable(getContext(), R.drawable.ic_favorite_border_white_24dp);
        }
    }
}
