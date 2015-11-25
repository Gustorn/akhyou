package dulleh.akhyou.anime;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import dulleh.akhyou.MainActivity;
import dulleh.akhyou.MainApplication;
import dulleh.akhyou.R;
import dulleh.akhyou.util.PaletteTransform;
import nucleus.factory.RequiresPresenter;
import nucleus.view.NucleusSupportFragment;

@RequiresPresenter(AnimePresenter.class)
public class AnimeFragment extends NucleusSupportFragment<AnimePresenter> {
    ImageView coverImage;
    TextView synopsis;
    TextView genres;
    TextView alternateTitle;
    TextView date;
    TextView status;
    FloatingActionButton favoriteButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.anime_header, container, false);

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
        MainApplication.getRefWatcher(getActivity()).watch(this);
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
        favoriteButton.setImageDrawable(favoriteIcon());
        setToolbarTitle(anime.getTitle());
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

    public void setToolbarTitle(String title) {
        ActionBar actionBar = ((MainActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }


    private Drawable favoriteIcon() {
        if (true) {
            return ContextCompat.getDrawable(getContext(), R.drawable.ic_favorite_white_24dp);
        } else {
            return ContextCompat.getDrawable(getContext(), R.drawable.ic_favorite_border_white_24dp);
        }
    }
}
