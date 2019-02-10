package com.waslak.waslak.adapters;

/**
 * Created by Abdelrahman Hesham on 3/8/2018.
 */

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.waslak.waslak.ActiveOrdersFragment;
import com.waslak.waslak.NotificationsFragment;
import com.waslak.waslak.R;
import com.waslak.waslak.RealStoresFragment;
import com.waslak.waslak.ShopInfoFragment;
import com.waslak.waslak.StorePendingOrdersFragment;

/**
 * Created by Abdelrahman Hesham on 4/22/2017.
 */

public class StoresFragmentAdapter extends FragmentStatePagerAdapter {
    private Context mContext;

    public StoresFragmentAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new ShopInfoFragment();
            case 1:
                return new StorePendingOrdersFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.shop_info);
            case 1:
                return mContext.getString(R.string.pending_orders);
            default:
                return null;
        }
    }



}
