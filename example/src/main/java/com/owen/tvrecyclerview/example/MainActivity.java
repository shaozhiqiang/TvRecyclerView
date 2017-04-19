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
import android.view.KeyEvent;

import com.owen.tvrecyclerview.example.tablayout.TabLayout;
import com.owen.tvrecyclerview.example.tablayout.TvTabLayout;

public class MainActivity extends FragmentActivity {
    private final String LOGTAG = MainActivity.class.getSimpleName();
    private final String ARG_SELECTED_LAYOUT_ID = "selectedLayoutId";

    private final int DEFAULT_LAYOUT = R.layout.layout_list;

    private int mSelectedLayoutId;

    private TvTabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTabLayout = (TvTabLayout) findViewById(R.id.tab_layout);
        mTabLayout.setScaleValue(1.1f);
        mTabLayout.addOnTabSelectedListener(new TabSelectedListener());

        mTabLayout.addTab(
                mTabLayout.newTab()
                        .setText("list")
                        .setIcon(R.drawable.ic_list)
                , true);
        mTabLayout.addTab(
                mTabLayout.newTab()
                        .setText("grid")
                        .setIcon(R.drawable.ic_grid)
        );
        mTabLayout.addTab(
                mTabLayout.newTab()
                        .setText("grid2")
                        .setIcon(R.drawable.ic_grid)
        );
        mTabLayout.addTab(
                mTabLayout.newTab()
                        .setText("staggered")
                        .setIcon(R.drawable.ic_staggered)
        );
        mTabLayout.addTab(
                mTabLayout.newTab()
                        .setText("spannable")
                        .setIcon(R.drawable.selector_ic_spannable)
        );

        mSelectedLayoutId = DEFAULT_LAYOUT;
        if (savedInstanceState != null) {
            mSelectedLayoutId = savedInstanceState.getInt(ARG_SELECTED_LAYOUT_ID);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_SELECTED_LAYOUT_ID, mSelectedLayoutId);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(LOGTAG, "keyCode=" + keyCode);
        return super.onKeyDown(keyCode, event);
    }

    public class TabSelectedListener implements TabLayout.OnTabSelectedListener {
        private Fragment mFragment;
        private int[] layoutIds = {
                R.layout.layout_list,
                R.layout.layout_grid,
                R.layout.layout_grid2,
                R.layout.layout_staggered_grid,
                R.layout.layout_spannable_grid,
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
                mFragment = LayoutFragment.newInstance(layoutIds[position]);
                mFt.add(R.id.content, mFragment, String.valueOf(position));
            } else {
                mFt.attach(mFragment);
            }
            mFt.commit();
            mSelectedLayoutId = layoutIds[position];
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
