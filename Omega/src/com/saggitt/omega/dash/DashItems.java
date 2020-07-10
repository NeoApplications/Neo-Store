/*
 *  Copyright (c) 2020 Omega Launcher
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
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
        return mContext.getResources().getDrawable(icon);

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
