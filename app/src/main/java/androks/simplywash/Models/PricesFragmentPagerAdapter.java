package androks.simplywash.Models;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.HashMap;

import androks.simplywash.Enums.CarType;
import androks.simplywash.Fragments.PriceFragment;

/**
 * Created by androks on 2/5/2017.
 */

public class PricesFragmentPagerAdapter extends FragmentPagerAdapter {

    private final int PAGE_COUNT = 3;

    private String tabTitles[] = new String[] { "Car", "SUV", "Minivan" };

    private HashMap<String, Price> mPrices;

    public PricesFragmentPagerAdapter(FragmentManager fm, HashMap<String, Price> prices) {
        super(fm);
        mPrices = prices;
    }

    @Override
    public Fragment getItem(int position) {
        if(position == CarType.MINIVAN.ordinal())
            return PriceFragment.newInstance(mPrices.get(CarType.MINIVAN.toString()));
        else if(position == CarType.SUV.ordinal())
            return PriceFragment.newInstance(mPrices.get(CarType.SUV.toString()));
        else
            return PriceFragment.newInstance(mPrices.get(CarType.CAR.toString()));
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }


}
