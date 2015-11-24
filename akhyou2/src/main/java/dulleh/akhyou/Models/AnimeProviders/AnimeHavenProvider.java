package dulleh.akhyou.Models.AnimeProviders;

import android.os.AsyncTask;
import android.util.Log;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dulleh.akhyou.Models.Anime;
import dulleh.akhyou.Models.Episode;
import dulleh.akhyou.Models.Source;
import dulleh.akhyou.Utils.GeneralUtils;
import rx.exceptions.OnErrorThrowable;

public class AnimeHavenProvider implements AnimeProvider {
    private static final Pattern LARGE_IMAGE = Pattern.compile("(.*)-\\d\\d\\dx\\d\\d\\d(.*)");
    private static final Pattern EXTRACT_INFO = Pattern.compile("");

    @Override
    public Anime fetchAnime(String url) throws OnErrorThrowable {
        Document main = Jsoup.parse(GeneralUtils.getWebPage(url));
        Element header = main.select(".series_page").first();
        if (header != null) {
            return parseDetailedPage(main, header);
        } else {
            return parseDetailedPage(main, main.select(".entry-content").first());
        }
    }

    @Override
    public Anime updateCachedAnime(Anime cachedAnime) throws OnErrorThrowable {
        Anime updatedAnime = null; //fetchAnime(cachedAnime.getUrl());
        updatedAnime.inheritWatchedFrom(cachedAnime.getEpisodes());
        updatedAnime.setMajorColour(cachedAnime.getMajorColour());
        return updatedAnime;
    }

    @Override
    public List<Source> fetchSources(String url) throws OnErrorThrowable {
        return null;
    }

    @Override
    public Source fetchVideo(Source source) throws OnErrorThrowable {
        return null;
    }

    private Anime parseDetailedPage(Document main, Element header) {
        String title = header.select(".entry-title span").text();
        String genreString = Stream.of(header.select(".text-left > p:nth-of-type(2) > a"))
                .map(Element::text).collect(Collectors.joining(", "));
        String desc = header.select(".category-archive-meta").text();
        String image = header.select(".series_main_image img").attr("src");

        Matcher largerImage = LARGE_IMAGE.matcher(image);
        if (largerImage.find()) {
            image = largerImage.group(1) + largerImage.group(2);
        }

        return null;
//        return new Anime()
//                .setTitle(title)
//                .setGenresString(genreString)
//                .setImageUrl(image)
//                .setDesc(desc)
//                .setEpisodes(getEpisodes(main));
    }

    private Anime parseSinglePage(Document main, Element header) {
        Elements entries = header.select("> strong");
        //String genreString = Stream.of(entries.first()).collect(Collectors.joining(", "));

        return null;
//        return new Anime()
//            .setTitle(main.select("[data-mark=for_single]").text());
    }

    private List<Elements> getEpisodeElements(Document firstPage) {
        Elements pages = firstPage.select(".pagination > a");
        List<Elements> episodes = new ArrayList<>(pages.size());
        List<QueryHavenAsync> requests = new ArrayList<>(pages.size());

        episodes.add(firstPage.select("#content article[id]"));
        for (Element elem : pages) {
            QueryHavenAsync request = new QueryHavenAsync();
            requests.add(request);
            request.execute(elem.attr("href"));
        }

        for (QueryHavenAsync request : requests) {
            try {
                episodes.add(request.get());
            } catch (InterruptedException | ExecutionException e) {
                Log.w("Haven", e);
            }
        }
        return episodes;
    }

    private List<Episode> getEpisodes(Document firstPage) {
        List<Elements> episodes = getEpisodeElements(firstPage);

        return Stream.of(episodes)
            .flatMap(Stream::of)
            .map(e -> {
                Element link = e.select(".entry-header > .entry-title > a[href]").first();
                return new Episode().setTitle(link.text()).setUrl(link.attr("href"));
            }).collect(Collectors.toList());
    }

    private class QueryHavenAsync extends AsyncTask<String, Void, Elements> {
        @Override
        protected Elements doInBackground(String... params) {
            return Jsoup.parse(GeneralUtils.getWebPage(params[0])).select("#content article[id]");
        }
    }
}
