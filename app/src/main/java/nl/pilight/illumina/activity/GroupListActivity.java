/*
 * illumina, a pilight remote
 *
 * Copyright (c) 2014 Peter Heisig <http://google.com/+PeterHeisig>
 *
 * illumina is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * illumina is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with illumina. If not, see <http://www.gnu.org/licenses/>.
 */

package nl.pilight.illumina.activity;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.ViewGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import nl.pilight.illumina.R;
import nl.pilight.illumina.pilight.Group;
import nl.pilight.illumina.service.PilightService;
import nl.pilight.illumina.widget.LocationPagerAdapter;

public class GroupListActivity extends BaseActivity {

    public static final Logger log = LoggerFactory.getLogger(GroupListActivity.class);

    // ------------------------------------------------------------------------
    //
    //      Service
    //
    // ------------------------------------------------------------------------

    @Override
    public void onPilightError(int cause) {
        super.onPilightError(cause);
        finish();
    }

    @Override
    public void onPilightDisconnected() {
        super.onPilightDisconnected();
        finish();
    }

    @Override
    public void onPilightConnected() {
        super.onPilightConnected();
        requestGroups();
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        dispatch(Message.obtain(null, PilightService.Request.STATE));
    }

    @Override
    public void onGroupListResponse(ArrayList<Group> groups) {
        super.onGroupListResponse(groups);

        final ActionBar actionBar = getSupportActionBar();
        final FragmentPagerAdapter pagerAdapter = new LocationPagerAdapter(
                getSupportFragmentManager(), groups);

        mViewPager.setAdapter(pagerAdapter);

        for (Group group : groups) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(group.getId())
                            .setTabListener(mTabListener));
        }

        if (groups.size() > 1) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }

        if (groups.isEmpty()) {
            mEmptyView.setVisibility(View.VISIBLE);
        }

        if (mSelectedLocationIndex <= actionBar.getTabCount()
                && actionBar.getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD) {
            actionBar.setSelectedNavigationItem(mSelectedLocationIndex);
        }
    }

    private void requestGroups() {
        log.info("requestGroups");
        dispatch(Message.obtain(null, PilightService.Request.GROUP_LIST));
    }

    // ------------------------------------------------------------------------
    //
    //      Lifecycle
    //
    // ------------------------------------------------------------------------

    private ViewPager mViewPager;
    private ViewGroup mEmptyView;
    private int mSelectedLocationIndex;

    private ActionBar.TabListener mTabListener = new ActionBar.TabListener() {

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            mViewPager.setCurrentItem(tab.getPosition());
        }
    };

    private ViewPager.OnPageChangeListener mPageChangeListener
            = new ViewPager.SimpleOnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            if (getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD) {
                getSupportActionBar().setSelectedNavigationItem(position);
                mSelectedLocationIndex = position;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_activity);

        mEmptyView = (ViewGroup) findViewById(android.R.id.empty);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOnPageChangeListener(mPageChangeListener);

        if (savedInstanceState != null) {
            mSelectedLocationIndex = savedInstanceState.getInt("mSelectedLocationIndex", 0);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("mSelectedLocationIndex", mSelectedLocationIndex);
    }

    @Override
    public void onBackPressed() {
        log.info("onBackPressed");
        dispatch(Message.obtain(null, PilightService.Request.PILIGHT_DISCONNECT));
        super.onBackPressed();
    }

    // ------------------------------------------------------------------------
    //
    //      Members
    //
    // ------------------------------------------------------------------------

    @Override
    protected void reset() {
        super.reset();

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.removeAllTabs();
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        }

        if (mViewPager != null) {
            mViewPager.setAdapter(null);
        }

        if (mEmptyView != null) {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

}
