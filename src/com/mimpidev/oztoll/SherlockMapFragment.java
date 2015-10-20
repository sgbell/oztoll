/**
 * 
 * Copyright (C) 2015  Sam Bell
 * @email - sam@mimpidev.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or  any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 * the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sam Bell - initial API and implementation
 * 
 */
package com.mimpidev.oztoll;

import android.app.Activity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.internal.view.menu.MenuItemWrapper;
import com.actionbarsherlock.internal.view.menu.MenuWrapper;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.SupportMapFragment;

import static com.actionbarsherlock.app.SherlockFragmentActivity.OnCreateOptionsMenuListener;
import static com.actionbarsherlock.app.SherlockFragmentActivity.OnOptionsItemSelectedListener;
import static com.actionbarsherlock.app.SherlockFragmentActivity.OnPrepareOptionsMenuListener;

public class SherlockMapFragment extends SupportMapFragment implements OnCreateOptionsMenuListener, OnPrepareOptionsMenuListener, OnOptionsItemSelectedListener {
    private SherlockFragmentActivity mActivity;

    public SherlockFragmentActivity getSherlockActivity() {
        return mActivity;
    }

    @Override
    public void onAttach(Activity activity) {
        if (!(activity instanceof SherlockFragmentActivity)) {
            throw new IllegalStateException(getClass().getSimpleName() + " must be attached to a SherlockFragmentActivity.");
        }
        mActivity = (SherlockFragmentActivity)activity;

        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mActivity = null;
        super.onDetach();
    }

    @Override
    public final void onCreateOptionsMenu(android.view.Menu menu, android.view.MenuInflater inflater) {
        onCreateOptionsMenu(new MenuWrapper(menu), mActivity.getSupportMenuInflater());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //Nothing to see here.
    }

    @Override
    public final void onPrepareOptionsMenu(android.view.Menu menu) {
        onPrepareOptionsMenu(new MenuWrapper(menu));
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        //Nothing to see here.
    }

    @Override
    public final boolean onOptionsItemSelected(android.view.MenuItem item) {
        return onOptionsItemSelected(new MenuItemWrapper(item));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Nothing to see here.
        return false;
    }
}
