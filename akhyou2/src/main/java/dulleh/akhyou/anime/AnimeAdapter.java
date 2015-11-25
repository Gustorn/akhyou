package dulleh.akhyou.anime;

import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import dulleh.akhyou.R;
import dulleh.akhyou.util.PaletteTransform;

public class AnimeAdapter { //extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//    private final PaletteTransform paletteTransform;
//    private final AnimeFragment animeFragment;
//    private final AnimePresenter presenter;
//
//    public AnimeAdapter(AnimeFragment animeFragment, AnimePresenter presenter) {
//        this.animeFragment = animeFragment;
//        this.presenter = presenter;
//        paletteTransform = new PaletteTransform();
//    }
//
//    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
//        public ImageView coverImage;
//        public TextView synopsis;
//        public TextView genres;
//        public TextView alternateTitle;
//        public TextView date;
//        public TextView status;
//        public FloatingActionButton favoriteButton;
//
//        public HeaderViewHolder(View v) {
//            super(v);
//            coverImage = (ImageView) v.findViewById(R.id.anime_image_view);
//            synopsis = (TextView) v.findViewById(R.id.anime_desc_view);
//            genres = (TextView) v.findViewById(R.id.anime_genres_view);
//            alternateTitle = (TextView) v.findViewById(R.id.anime_alternate_title_view);
//            date = (TextView) v.findViewById(R.id.anime_date_view);
//            status = (TextView) v.findViewById(R.id.anime_status_view);
//            favoriteButton = (FloatingActionButton) v.findViewById(R.id.favourite_fab);
//        }
//    }
//
//    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        AnimeAdapter.HeaderViewHolder headerViewHolder =
//                new HeaderViewHolder(LayoutInflater.from(parent.getContext())
//                        .inflate(R.layout.anime_header, parent, false));
//
//        headerViewHolder.favoriteButton.setOnClickListener(view -> {
//            isInFavourites = !isInFavourites;
//            animeFragment.getPresenter().onFavouriteCheckedChanged(isInFavourites);
//            headerViewHolder.favoriteButton.setImageDrawable(favoriteIcon());
//        });
//        headerViewHolder.favoriteButton.setOnClickListener(view -> animeFragment.showImageDialog());
//        return headerViewHolder;
//    }
//
//    @Override
//    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
//        Anime anime = animeFragment.getPresenter().lastAnime;
//        HeaderViewHolder headerViewHolder = (HeaderViewHolder) viewHolder;
//
//
//    }
//
//    @Override
//    public int getItemCount() {
//        return 0;
//    }
//
//    private Drawable favoriteIcon() {
//        if (isInFavourites) {
//            return ContextCompat.getDrawable(animeFragment.getContext(), R.drawable.ic_favorite_white_24dp);
//        } else {
//            return ContextCompat.getDrawable(animeFragment.getContext(), R.drawable.ic_favorite_border_white_24dp);
//        }
//    }
//
//    public void matchFavoriteStatus(boolean isInFavourites) {
//        this.isInFavourites = isInFavourites;
//        notifyDataSetChanged();
//    }
//
//    public void setWatched(int position, boolean value) {
//        animeFragment.getPresenter().lastAnime.getEpisode(position).setWatched(value);
//    }
}