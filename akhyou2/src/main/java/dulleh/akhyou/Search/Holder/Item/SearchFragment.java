package dulleh.akhyou.Search.Holder.Item;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
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

import de.greenrobot.event.EventBus;
import dulleh.akhyou.MainActivity;
import dulleh.akhyou.MainApplication;
import dulleh.akhyou.Models.Anime;
import dulleh.akhyou.R;
import dulleh.akhyou.Search.Holder.SearchHolderAdapter;
import dulleh.akhyou.Search.Holder.SearchHolderFragment;
import dulleh.akhyou.Utils.AdapterClickListener;
import dulleh.akhyou.Utils.Events.OpenAnimeEvent;
import dulleh.akhyou.Utils.Events.SearchEvent;
import nucleus.factory.RequiresPresenter;
import nucleus.view.NucleusSupportFragment;

@RequiresPresenter(SearchPresenter.class)
public class SearchFragment extends NucleusSupportFragment<SearchPresenter> implements AdapterClickListener<Anime> {
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView.Adapter searchAdapter;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        // must be after ^ so that getPresenter() doesn't return null
        getPresenter().setProviderType(getArguments().getInt(SearchHolderAdapter.PROVIDER_TYPE_KEY, Anime.ANIME_RUSH));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment, container, false);

        RecyclerView searchResultsView = (RecyclerView) view.findViewById(R.id.recycler_view);
        switch (SearchHolderFragment.SEARCH_GRID_TYPE) {
            case 0:
                searchAdapter = new SearchListAdapter(this);
                searchResultsView.setLayoutManager(new LinearLayoutManager(container.getContext(), LinearLayoutManager.VERTICAL, false));
                break;
            case 1:
                searchAdapter = new SearchGridAdapter(this);
                searchResultsView.setLayoutManager(new GridLayoutManager(container.getContext(), 2, GridLayoutManager.VERTICAL, false));
                break;

        }
        searchResultsView.setAdapter(searchAdapter);
        searchResultsView.setItemAnimator(new DefaultItemAnimator());

        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_layout);
        refreshLayout.setColorSchemeResources(R.color.accent);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getPresenter().search();
            }
        });

        updateRefreshing();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MainApplication.getRefWatcher(getActivity()).watch(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem searchItem = menu.findItem(R.id.search_item);

        if (searchItem == null) {
            inflater.inflate(R.menu.search_menu, menu);
            searchItem = menu.findItem(R.id.search_item);
        }

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setQueryHint(getString(R.string.search_item));
        searchView.setIconifiedByDefault(false);
        searchView.setIconified(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if (!query.isEmpty()) {
                    getPresenter().onEvent(new SearchEvent(query));
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

    public void updateSearchResults () {
        searchAdapter.notifyDataSetChanged();
        updateRefreshing();
    }

    public void updateRefreshing () {
        if (!isRefreshing() && getPresenter().isRefreshing) {
            TypedValue typedValue = new TypedValue();
            getActivity().getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typedValue, true);
            refreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typedValue.resourceId));
            refreshLayout.setRefreshing(true);
        } else if (isRefreshing() && !getPresenter().isRefreshing){
            refreshLayout.setRefreshing(false);
        }
    }

    public boolean isRefreshing () {
        return refreshLayout.isRefreshing();
    }

    @Override
    public void onCLick(Anime anime, @Nullable Integer position) {
        ((MainActivity) getActivity()).requestFragment(MainActivity.ANIME_FRAGMENT);
        EventBus.getDefault().postSticky(new OpenAnimeEvent(anime));
    }

    @Override
    public void onLongClick(Anime item, @Nullable Integer position) {

    }

}
