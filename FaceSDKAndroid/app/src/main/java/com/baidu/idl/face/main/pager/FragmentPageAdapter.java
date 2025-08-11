package com.baidu.idl.face.main.pager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.baidu.idl.face.main.fragment.BaseFragment;
import com.baidu.idl.face.main.listener.IconPager;

import java.util.ArrayList;
import java.util.List;

public class FragmentPageAdapter extends FragmentStatePagerAdapter implements IconPager {
    private List<BaseFragment> mFragmentList;
    private String[] mTitleArray;
    private int[] mDrawableArray;

    public FragmentPageAdapter(FragmentManager fm, ArrayList<BaseFragment> fragmentList,
                               String[] titleArray, int[] drawableArray) {
        super(fm);
        this.mFragmentList = fragmentList;
        this.mTitleArray = titleArray;
        this.mDrawableArray = drawableArray;
    }

    public Fragment getItem(int index) {
        Fragment fragment = null;
        if (this.mFragmentList != null && this.mFragmentList.size() > 0) {
            fragment = (Fragment) this.mFragmentList.get(index);
        }

        return fragment;
    }

    public int getCount() {
        int count = 0;
        if (this.mFragmentList != null && this.mFragmentList.size() > 0) {
            count = this.mFragmentList.size();
        }

        return count;
    }

    public CharSequence getPageTitle(int position) {
        CharSequence charS = "";
        if (this.mTitleArray != null && this.mTitleArray.length > 0) {
            charS = this.mTitleArray[position];
        }

        return charS;
    }

    public int getIconResId(int index) {
        int resId = 0;
        if (this.mDrawableArray != null && this.mDrawableArray.length > 0) {
            resId = this.mDrawableArray[index];
        }

        return resId;
    }
}
