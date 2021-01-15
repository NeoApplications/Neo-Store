/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Saul Henriquez
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.dash;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.android.launcher3.R;
import com.android.launcher3.util.IOUtils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class DashItems {
    ArrayList<DashModel> itemList = new ArrayList<>();
    private Context mContext;

    public DashItems(Context context) {
        mContext = context;
        LoadItems();
    }

    public ArrayList<DashModel> getItemList() {
        return itemList;
    }

    private void LoadItems() {
        JSONArray allItems = openDashFile();
        DashModel item;

        for (int i = 0; i < allItems.length(); i++) {
            try {
                item = new DashModel();
                item.setId(allItems.getJSONObject(i).getInt("item_id"));
                item.setTitle(allItems.getJSONObject(i).getString("item_name"));
                item.setAction(allItems.getJSONObject(i).getString("item_action"));
                item.setIcon(getDrawable(allItems.getJSONObject(i).getString("item_icon")));
                item.setEnabled(allItems.getJSONObject(i).getBoolean("item_enabled"));

                itemList.add(item);
            } catch (JSONException e) {
                Log.d("Dash Items", "Error " + e.getMessage());
            }
        }
    }

    private Drawable getDrawable(String iconName) {
        int icon = mContext.getResources().getIdentifier(iconName, "drawable", mContext.getPackageName());
        return mContext.getResources().getDrawable(icon, null);

    }

    @NotNull
    private JSONArray openDashFile() {
        InputStream file = mContext.getResources().openRawResource(R.raw.dash_items);
        try {
            JSONObject obj = new JSONObject(new String(IOUtils.toByteArray(file)));
            return obj.getJSONArray("dash_items");
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            Log.d("Dash Items", "Error " + e.getMessage());
        }
        return new JSONArray();
    }

}
