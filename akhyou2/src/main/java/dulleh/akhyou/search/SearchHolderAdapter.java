package dulleh.akhyou.search;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import dulleh.akhyou.search.item.SearchFragment;


public class SearchHolderAdapter extends FragmentPagerAdapter {
    public SearchHolderAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return  new SearchFragment();
    }

    @Override
    public int getCount() {
        return 1;
    }
}
