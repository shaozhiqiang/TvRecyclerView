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

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.util.TypedValue;

import com.owen.focus.FocusBorder;
import com.owen.tab.TvTabLayout;
import com.owen.tvrecyclerview.example.fragment.BaseFragment;
import com.owen.tvrecyclerview.example.fragment.GridFragment;
import com.owen.tvrecyclerview.example.fragment.ListFragment;
import com.owen.tvrecyclerview.example.fragment.MetroFragment;
import com.owen.tvrecyclerview.example.fragment.NestedFragment;
import com.owen.tvrecyclerview.example.fragment.SpannableFragment;
import com.owen.tvrecyclerview.example.fragment.StaggeredFragment;
import com.owen.tvrecyclerview.example.fragment.UpdateDataFragment;
import com.owen.tvrecyclerview.example.fragment.V7GridFragment;
import com.owen.tvrecyclerview.example.fragment.VLayoutFragment;
import com.owen.tvrecyclerview.utils.Loger;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author ZhouSuQiang
 */
public class MainActivity extends AppCompatActivity implements BaseFragment.FocusBorderHelper{
    private final String LOGTAG = MainActivity.class.getSimpleName();
    private final String[] tabNames = {
            "VLayout", "Metro", "Spannable",
            "Nested", "Menu&List", "Grid",
            "V7Grid", "Staggered", "UpdateData"
    };
    private final String[] fragments = {
            VLayoutFragment.class.getName(), MetroFragment.class.getName(), SpannableFragment.class.getName(),
            NestedFragment.class.getName(), ListFragment.class.getName(), GridFragment.class.getName(),
            V7GridFragment.class.getName(), StaggeredFragment.class.getName(), UpdateDataFragment.class.getName()
    };

    @BindView(R.id.tab_layout)
    TvTabLayout mTabLayout;
    
    private FocusBorder mFocusBorder;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    
        //是否打开TvRecyclerView的log打印
        Loger.isDebug = true;
        
        // 移动框
        if(null == mFocusBorder) {
            mFocusBorder = new FocusBorder.Builder()
                    .asColor()
                    .borderColorRes(R.color.actionbar_color)
                    .borderWidth(TypedValue.COMPLEX_UNIT_DIP, 3.2f)
                    .shadowColorRes(R.color.green_bright)
                    .shadowWidth(TypedValue.COMPLEX_UNIT_DIP, 22f)
                    .build(this);
        }
        
        mTabLayout.addOnTabSelectedListener(new TabSelectedListener());
        for(String tabName : tabNames) {
            mTabLayout.addTab(mTabLayout.newTab().setText(tabName));
        }
        mTabLayout.selectTab(0);
    }

    @Override
    public FocusBorder getFocusBorder() {
        return mFocusBorder;
    }

    public class TabSelectedListener implements TvTabLayout.OnTabSelectedListener {
        private Fragment mFragment;

        @Override
        public void onTabSelected(TvTabLayout.Tab tab) {
            final int position = tab.getPosition();
            mFragment = getSupportFragmentManager().findFragmentByTag(position + "");
            FragmentTransaction mFt = getSupportFragmentManager().beginTransaction();
            if (mFragment == null) {
                mFragment = Fragment.instantiate(MainActivity.this, fragments[position]);
                mFt.add(R.id.content, mFragment, String.valueOf(position));
            } else {
                mFt.attach(mFragment);
            }
            mFt.commit();
        }

        @Override
        public void onTabUnselected(TvTabLayout.Tab tab) {
            if (mFragment != null) {
                FragmentTransaction mFt = getSupportFragmentManager().beginTransaction();
                mFt.detach(mFragment);
                mFt.commit();
            }
        }

        @Override
        public void onTabReselected(TvTabLayout.Tab tab) {

        }
    }
}
