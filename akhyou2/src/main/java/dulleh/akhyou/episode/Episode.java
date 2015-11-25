package dulleh.akhyou.episode;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.experimental.NonFinal;

@Data
public class Episode implements Serializable {
    private final String title;
    private final String url;

    @NonFinal private List<Source> sources;
    @NonFinal private boolean watched = false;

    public void flipWatched () {
        watched = !watched;
    }
}
