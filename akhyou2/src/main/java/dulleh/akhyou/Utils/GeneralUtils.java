package dulleh.akhyou.Utils;

import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import de.greenrobot.event.EventBus;
import dulleh.akhyou.MainApplication;
import dulleh.akhyou.Models.Anime;
import dulleh.akhyou.Models.Source;
import dulleh.akhyou.Models.SourceProviders.SourceProvider;
import dulleh.akhyou.Utils.Events.SnackbarEvent;
import rx.exceptions.OnErrorThrowable;

public class GeneralUtils {
    private static final DateFormat FROM_DATE = new SimpleDateFormat("yyyy-mm-dd", Locale.getDefault());
    private static final DateFormat TO_DATE = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());

    public static String getWebPage (final String url) {
        return GeneralUtils.getWebPage(new Request.Builder().url(url).build());
    }

    public static String getWebPage (final Request request) {
        OkHttpClient client = new OkHttpClient();
        client.setCookieHandler(CloudflareHttpClient.INSTANCE.getCookieManager());
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            throw OnErrorThrowable.from(new Throwable("Failed to connect.", e));
        }
    }

    public static String encodeForUtf8 (String s) {
        try {
            return URLEncoder.encode(s, "utf-8");
        } catch (UnsupportedEncodingException u) {
            u.printStackTrace();
            return s.replace(":", "%3A")
                    .replace("/", "%2F")
                    .replace("#", "%23")
                    .replace("?", "%3F")
                    .replace("&", "%24")
                    .replace("@", "%40")
                    .replace("%", "%25")
                    .replace("+", "%2B")
                    .replace(" ", "+")
                    .replace(";","%3B")
                    .replace("=", "%3D")
                    .replace("$", "%26")
                    .replace(",", "%2C")
                    .replace("<", "%3C")
                    .replace(">", "%3E")
                    .replace("~", "%25")
                    .replace("^", "%5E")
                    .replace("`", "%60")
                    .replace("\\", "%5C")
                    .replace("[", "%5B")
                    .replace("]", "%5D")
                    .replace("{", "%7B")
                    .replace("|", "%7C")
                    .replace("\"", "%22");
        }
    }

    private static String getFileNameFromUrl (String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public static void internalDownload (DownloadManager downloadManager, String url) {
        String title = getFileNameFromUrl(url);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(title);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title);
        request.setMimeType("video/*");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        //request.allowScanningByMediaScanner();
        // ^ opens a dialog to open your launcher for some reason.
        downloadManager.enqueue(request);
    }

    public static void lazyDownload(AppCompatActivity activity,  String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        } else {
            EventBus.getDefault().post(new SnackbarEvent("No app to open this link."));
        }
    }

    public static String formatError (Throwable e) {
        if (e.getMessage() != null) {
            return "Error: " + e.getMessage().replace("java.lang.Throwable:", "").trim();
        }
        return "An error occurred.";
    }

    public static String jwPlayerIsolate (String body) {
        return Jsoup.parse(body).select("div#player").first().nextElementSibling().html();
    }

    // Need to handle null by yourself
    @Nullable
    public static String serializeAnime(Anime anime) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(anime);
        }catch (IOException io) {
            io.printStackTrace();
            return null;
        }
    }

    // Need to handle null by yourself
    @Nullable
    public static Anime deserializeAnime(String serializedFavourite) {
        try {
            return new ObjectMapper().readValue(serializedFavourite, Anime.class);
        }catch (IOException io) {
            io.printStackTrace();
            return null;
        }
    }

    public static boolean isAccentColour (int colorInt) {
        return colorInt == MainApplication.RED_ACCENT_RGB;
    }


    public static String formatHummingbirdDate(String date) {
        try {
            return TO_DATE.format(FROM_DATE.parse(date));
        } catch (ParseException e) {
            return date;
        }
    }

    public static SourceProvider determineSourceProvider (String lowerCaseTitle) {
        for (String sourceName : Source.sourceMap.keySet()) {
            if (lowerCaseTitle.contains(sourceName)) {
                return Source.sourceMap.get(sourceName);
            }
        }
        return null;
    }

}
