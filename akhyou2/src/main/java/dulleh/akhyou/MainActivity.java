package dulleh.akhyou;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;


import de.greenrobot.event.EventBus;
import dulleh.akhyou.anime.AnimeFragment;
import dulleh.akhyou.event.OpenAnimeEvent;
import dulleh.akhyou.event.SnackbarEvent;
import dulleh.akhyou.search.SearchHolderFragment;
import dulleh.akhyou.anime.Anime;
import dulleh.akhyou.settings.SettingsFragment;
import dulleh.akhyou.util.AdapterClickListener;
import dulleh.akhyou.network.CloudflareHttpClient;
import nucleus.factory.PresenterFactory;
import nucleus.factory.RequiresPresenter;
import nucleus.view.NucleusAppCompatActivity;

@RequiresPresenter(MainPresenter.class)
public class MainActivity extends NucleusAppCompatActivity<MainPresenter> implements AdapterClickListener<Anime>{
    private SharedPreferences sharedPreferences;
    private android.support.v4.app.FragmentManager fragmentManager;
    private FrameLayout parentLayout;
    private DrawerLayout drawerLayout;
    private RecyclerView favouritesList;
    private DrawerAdapter drawerAdapter;

    public static final String SEARCH_FRAGMENT = "SEA";
    public static final String ANIME_FRAGMENT = "ANI";
    public static final String SETTINGS_FRAGMENT = "SET";

    @Override
    public PresenterFactory<MainPresenter> getPresenterFactory() {
        return () -> new MainPresenter(getApplicationContext());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        CloudflareHttpClient.INSTANCE.onCreate(getApplicationContext());

        setContentView(R.layout.activity_main);

        sharedPreferences = getPreferences(MODE_PRIVATE);
        fragmentManager = getSupportFragmentManager();
        parentLayout = (FrameLayout) findViewById(R.id.container);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        RelativeLayout drawerSettingsButton = (RelativeLayout) findViewById(R.id.drawer_settings);
        drawerSettingsButton.setOnClickListener(view -> {}); //getPresenter().onEvent(new SettingsItemSelectedEvent()));
        favouritesList = (RecyclerView) findViewById(R.id.drawer_recycler_view);
        favouritesList.setLayoutManager(new LinearLayoutManager(this));

        setFavoritesAdapter();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // must be after set as actionbar
        toolbar.setNavigationOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));

        if (savedInstanceState == null) {
            getPresenter().onFreshStart(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainApplication.getRefWatcher(this).watch(this);
    }

    public void closeDrawer () {
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void setTheme () {
        sharedPreferences = getPreferences(MODE_PRIVATE);
        int themePref = sharedPreferences.getInt(getApplicationContext().getString(R.string.akhyou_red_theme), 0);
        setTheme(themePref);
    }

    public void requestFragment (String tag) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (tag) {
            case SEARCH_FRAGMENT:
                fragmentTransaction
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, new SearchHolderFragment(), SEARCH_FRAGMENT);
                break;

            case ANIME_FRAGMENT:
                fragmentTransaction.setCustomAnimations(R.anim.enter_down, 0, 0, R.anim.exit_down)
                        .replace(R.id.container, new AnimeFragment(), ANIME_FRAGMENT);

                if (fragmentManager.findFragmentByTag(SEARCH_FRAGMENT) != null) {
                    fragmentTransaction.addToBackStack(SEARCH_FRAGMENT);
                }

                break;

            case SETTINGS_FRAGMENT:
                fragmentTransaction
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, new SettingsFragment(), SETTINGS_FRAGMENT);
                if (fragmentManager.findFragmentByTag(ANIME_FRAGMENT) != null) {
                    fragmentTransaction.addToBackStack(ANIME_FRAGMENT);
                } else {
                    fragmentTransaction.addToBackStack(SEARCH_FRAGMENT);
                }

                break;
        }
        fragmentTransaction.commit();
    }

    public void showSnackBar (SnackbarEvent event) {
        if (event.actionTitle == null) {
            Snackbar.make(parentLayout, event.message, event.duration)
                    .show();
        } else {
            Snackbar.make(parentLayout, event.message, event.duration)
                    .setAction(event.actionTitle, event.onClickListener)
                    .setActionTextColor(event.actionColor)
                    .show();
        }
    }

    @Override
    public void onCLick(Anime item, @Nullable Integer position) {
        if (fragmentManager.findFragmentByTag(ANIME_FRAGMENT) == null) {
            requestFragment(MainActivity.ANIME_FRAGMENT);
            EventBus.getDefault().postSticky(new OpenAnimeEvent(item));
        } else {
            EventBus.getDefault().postSticky(new OpenAnimeEvent(item));
        }

        if (fragmentManager.findFragmentByTag(SETTINGS_FRAGMENT) != null) {
            fragmentManager
                    .beginTransaction()
                    .remove(fragmentManager.findFragmentByTag(SETTINGS_FRAGMENT))
                    .commit();
            fragmentManager.popBackStack();
        }

        closeDrawer();
    }

    @Override
    public void onLongClick(Anime item, @Nullable Integer position) {

    }

    private void setFavoritesAdapter() {
        drawerAdapter = new DrawerAdapter(this, getPresenter().getFavourites());
        favouritesList.setAdapter(drawerAdapter);
    }

    public void favoritesChanged() {
        if (drawerAdapter != null) {
            drawerAdapter.setFavourites(getPresenter().getFavourites());
            drawerAdapter.notifyDataSetChanged();
        } else {
            drawerAdapter = new DrawerAdapter(this, getPresenter().getFavourites());
            favouritesList.setAdapter(drawerAdapter);
            drawerAdapter.notifyDataSetChanged();
        }
    }
}