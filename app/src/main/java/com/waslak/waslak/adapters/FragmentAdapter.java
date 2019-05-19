package com.waslak.waslak.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import com.waslak.waslak.R;
import com.waslak.waslak.RealStoresFragment;

/**
 * Created by Abdelrahman Hesham on 4/22/2017.
 */

public class FragmentAdapter extends FragmentStatePagerAdapter {
    private Context mContext;

    public FragmentAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            RealStoresFragment realStoresFragment = new RealStoresFragment();
            Bundle bundle = new Bundle();
            bundle.putString("Type", "Nearby");
            realStoresFragment.setArguments(bundle);
            return realStoresFragment;
//            case 1:
//                RealStoresFragment realStoresFragment2 = new RealStoresFragment();
//                Bundle bundle2 = new Bundle();
//                bundle2.putString("Type","All");
//                realStoresFragment2.setArguments(bundle2);
//                return realStoresFragment2;
//            case 2:
//                RealStoresFragment realStoresFragment3 = new RealStoresFragment();
//                Bundle bundle3 = new Bundle();
//                bundle3.putString("Type","Active");
//                realStoresFragment3.setArguments(bundle3);
//                return realStoresFragment3;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return mContext.getString(R.string.nearby);
//            case 1:
//                return mContext.getString(R.string.all_stores);
//            case 2:
//                return mContext.getString(R.string.active);
        }
        return null;
    }





}
