/*
 * Copyright (C) 2014 Lucas Rocha
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.owen.tvrecyclerview.example;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.owen.tvrecyclerview.example.focus.FocusBorder;
import com.owen.tvrecyclerview.example.fragment.BaseFragment;
import com.owen.tvrecyclerview.example.fragment.GridFragment;
import com.owen.tvrecyclerview.example.fragment.ListFragment;
import com.owen.tvrecyclerview.example.fragment.MetroFragment;
import com.owen.tvrecyclerview.example.fragment.SpannableFragment;
import com.owen.tvrecyclerview.example.fragment.StaggeredFragment;
import com.owen.tvrecyclerview.example.fragment.UpdateDataFragment;
import com.owen.tvrecyclerview.example.fragment.V7GridFragment;
import com.owen.tvrecyclerview.example.tablayout.TabLayout;
import com.owen.tvrecyclerview.example.tablayout.TvTabLayout;
import com.owen.tvrecyclerview.utils.Loger;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends FragmentActivity implements BaseFragment.FocusBorderHelper{
    private final String LOGTAG = MainActivity.class.getSimpleName();

    @BindView(R.id.tab_layout) TvTabLayout mTabLayout;
    
    private FocusBorder mFocusBorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        
        Loger.isDebug = true; //是否打开TvRecyclerView的log打印
        
        // 移动框
        if(null == mFocusBorder) {
            mFocusBorder = new FocusBorder.Builder().asDrawable().borderResId(R.drawable.focus).build(this);
        }
        
        mTabLayout.setScaleValue(1.1f);
        mTabLayout.addOnTabSelectedListener(new TabSelectedListener());

        mTabLayout.addTab(
                mTabLayout.newTab()
                        .setText("Metro")
//                        .setIcon(R.drawable.ic_staggered)
                , true);
        mTabLayout.addTab(
                mTabLayout.newTab()
                        .setText("List")
//                        .setIcon(R.drawable.ic_list)
                );
        mTabLayout.addTab(
                mTabLayout.newTab()
                        .setText("Grid")
//                        .setIcon(R.drawable.ic_grid)
        );
        mTabLayout.addTab(
                mTabLayout.newTab()
                        .setText("V7Grid")
//                        .setIcon(R.drawable.ic_grid)
        );
        mTabLayout.addTab(
                mTabLayout.newTab()
                        .setText("Staggered")
//                        .setIcon(R.drawable.ic_staggered)
        );
        mTabLayout.addTab(
                mTabLayout.newTab()
                        .setText("Spannable")
//                        .setIcon(R.drawable.selector_ic_spannable)
        );
        mTabLayout.addTab(
                mTabLayout.newTab()
                        .setText("UpdateData")
//                        .setIcon(R.drawable.ic_launcher)
        );

    }

    @Override
    public FocusBorder getFocusBorder() {
        return mFocusBorder;
    }

    public class TabSelectedListener implements TabLayout.OnTabSelectedListener {
        private Fragment mFragment;
        private int[] layoutIds = {
                R.layout.layout_metro_grid,
                R.layout.layout_list,
                R.layout.layout_grid,
                R.layout.layout_grid2,
                R.layout.layout_staggered_grid,
                R.layout.layout_spannable_grid,
                R.layout.layout_update_data_changed
        };

        public TabSelectedListener() {
        }

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            final int position = tab.getPosition();
            Log.i(LOGTAG, "onTabSelected...position="+position);
            mFragment = (Fragment) getSupportFragmentManager().findFragmentByTag(position + "");
            FragmentTransaction mFt = getSupportFragmentManager().beginTransaction();
            if (mFragment == null) {
                switch (layoutIds[position]) {
                    case R.layout.layout_metro_grid:
                        mFragment = MetroFragment.newInstance();
                        break;
                    case R.layout.layout_list:
                        mFragment = ListFragment.newInstance();
                        break;
                    case R.layout.layout_grid:
                        mFragment = GridFragment.newInstance();
                        break;
                    case R.layout.layout_grid2:
                        mFragment = V7GridFragment.newInstance();
                        break;
                    case R.layout.layout_staggered_grid:
                        mFragment = StaggeredFragment.newInstance();
                        break;
                    case R.layout.layout_spannable_grid:
                        mFragment = SpannableFragment.newInstance();
                        break;
                    case R.layout.layout_update_data_changed:
                        mFragment = UpdateDataFragment.newInstance();
                        break;
                }
                mFt.add(R.id.content, mFragment, String.valueOf(position));
            } else {
                mFt.attach(mFragment);
            }
            mFt.commit();
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            if (mFragment != null) {
                FragmentTransaction mFt = getSupportFragmentManager().beginTransaction();
                mFt.detach(mFragment);
                mFt.commit();
            }
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    }
}
