package dulleh.akhyou.search.provider;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

import dulleh.akhyou.anime.Anime;
import dulleh.akhyou.util.GeneralUtils;
import rx.exceptions.OnErrorThrowable;

public class HummingbirdSearchProvider implements SearchProvider {
    private static final String BASE_URL = "http://hummingbird.me/api/v1/search/anime?query=";

    @Override
    public List<Anime> searchFor(String searchTerm) throws OnErrorThrowable {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw OnErrorThrowable.from(new Throwable("Please enter a search term."));
        }

        String body = GeneralUtils.getWebPage(BASE_URL + GeneralUtils.encodeForUtf8(searchTerm));
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readValue(body, JsonNode.class);
            return Stream.of(root).map(Anime::new).collect(Collectors.toList());
        } catch (IOException e) {
            throw OnErrorThrowable.from(new Throwable("No results found."));
        }
    }
}
