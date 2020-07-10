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

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.android.launcher3.R;

import java.util.ArrayList;

public class DashActivity extends AppCompatActivity {
    private DashItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dash);
        DashItems items = new DashItems(getApplicationContext());

        ArrayList<DashModel> itemTitles = items.getItemList();

        // usage sample
        final DashListView circularListView = (DashListView) findViewById(R.id.my_circular_list);
        adapter = new DashItemAdapter(getLayoutInflater(), itemTitles, circularListView.getContext());
        circularListView.setAdapter(adapter);
        circularListView.setRadius(150);
    }
}
