package dulleh.akhyou.search;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;


public class SearchHolderAdapter extends FragmentStatePagerAdapter{
    public SearchHolderAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return null;// new SearchFragment();
    }

    @Override
    public int getCount() {
        return 1;
    }
}
