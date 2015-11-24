package dulleh.akhyou.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

import dulleh.akhyou.MainApplication;
import dulleh.akhyou.Utils.GeneralUtils;
import lombok.Getter;
import lombok.Setter;

public class Anime implements Parcelable{
    @Getter @Setter private int majorColour = MainApplication.RED_ACCENT_RGB;

    @Getter private int hummingbirdId;
    @Getter private int myAnimeListId;

    @Getter private String title;
    private String alternateTitle;
    @Getter private String synopsis;
    @Getter private String status;
    @Getter private String type;

    @Getter private int episodeCount;
    private String startedAiring;
    private String finishedAiring;
    @Getter private String genreString;

    @Getter private String imageUrl;
    @Getter private final List<String> genres;
    @Getter @Setter private List<Episode> episodes;

    public Anime(JsonNode animeJson) {
        episodes = new ArrayList<>();

        hummingbirdId = animeJson.get("id").asInt();
        myAnimeListId = animeJson.get("mal_id").asInt();
        title = animeJson.get("title").asText();
        alternateTitle = animeJson.get("alternate_title").asText();
        synopsis = animeJson.get("synopsis").asText();
        status = animeJson.get("status").asText();
        type = animeJson.get("show_type").asText();

        episodeCount = animeJson.get("episode_count").asInt();
        startedAiring = GeneralUtils.formatHummingbirdDate(animeJson.get("started_airing").asText(""));
        finishedAiring = GeneralUtils.formatHummingbirdDate(animeJson.get("finished_airing").asText(""));

        imageUrl = animeJson.get("cover_image").asText();

        genres = Stream.of(animeJson.get("genres"))
            .map(g -> g.get("name").asText())
            .collect(Collectors.toList());
        genreString = Stream.of(genres).collect(Collectors.joining(", "));
    }

    private Anime(Parcel in) {
        hummingbirdId = in.readInt();
        myAnimeListId = in.readInt();

        title = in.readString();
        alternateTitle = in.readString();
        synopsis = in.readString();
        status = in.readString();
        type = in.readString();

        episodeCount = in.readInt();
        startedAiring = in.readString();
        finishedAiring = in.readString();
        genreString = in.readString();

        imageUrl = in.readString();

        genres = new ArrayList<>();
        in.readList(genres, null);

        episodes = new ArrayList<>();
        in.readList(episodes, null);

        majorColour = in.readInt();
    }

    public String getAlternateTitle() {
        return alternateTitle.isEmpty() ? "-" : alternateTitle;
    }

    public String getAiringString() {
        if (finishedAiring.isEmpty()) {
            return String.format("On %s", startedAiring);
        } else {
            return String.format("From %s To %s", startedAiring, finishedAiring);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(hummingbirdId);
        parcel.writeInt(myAnimeListId);

        parcel.writeString(title);
        parcel.writeString(alternateTitle);
        parcel.writeString(synopsis);
        parcel.writeString(status);
        parcel.writeString(type);

        parcel.writeInt(episodeCount);
        parcel.writeString(startedAiring);
        parcel.writeString(finishedAiring);
        parcel.writeString(genreString);

        parcel.writeString(imageUrl);
        parcel.writeList(genres);
        parcel.writeList(episodes);

        parcel.writeInt(majorColour);
    }

    public static final Creator<Anime> CREATOR = new Creator<Anime>() {
        @Override
        public Anime createFromParcel(Parcel in) {
            return new Anime(in);
        }

        @Override
        public Anime[] newArray(int size) {
            return new Anime[size];
        }
    };

    public void inheritWatchedFrom (List<Episode> oldEpisodes) {
        if (episodes != null) {
            for (Episode episode : episodes) {
                Optional<Episode> matching = Stream.of(oldEpisodes)
                    .filter(e -> e.getTitle().equals(episode.getTitle()))
                    .findFirst();
                matching.ifPresent(e -> episode.setWatched(e.isWatched()));
            }
        }
    }
}
