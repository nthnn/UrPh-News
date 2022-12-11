/*
	Copyright © 2022 Nathanne Isip

	Permission is hereby granted, free of charge,
	to any person obtaining a copy of this software
	and associated documentation files (the “Software”),
	to deal in the Software without restriction,
	including without limitation the rights to use, copy,
	modify, merge, publish, distribute, sublicense,
	and/or sell copies of the Software, and to permit
	persons to whom the Software is furnished to do so,
	subject to the following conditions:

	The above copyright notice and this permission notice
	shall be included in all copies or substantial portions
	of the Software.

	THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF
	ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
	TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
	PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
	THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
	DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
	CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
	CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
	IN THE SOFTWARE.
*/

package xyz.nathannestein.urphnews;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import xyz.nathannestein.urphnews.core.SessionCache;
import xyz.nathannestein.urphnews.util.InternetUtil;
import xyz.nathannestein.urphnews.util.NewsFetch;

public class NewsActivity extends AppCompatActivity {
    private ListView listView;
    private long pressedTime;
    private NewsCardAdapter adapter;

    private void refreshListView() {
        this.adapter = new NewsCardAdapter(NewsActivity.this, SessionCache.news);
        this.listView.setAdapter(this.adapter);

        this.adapter.notifyDataSetChanged();
        this.adapter.setNotifyOnChange(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_news);

        this.adapter = new NewsCardAdapter(NewsActivity.this, SessionCache.news);
        this.listView = (ListView) this.findViewById(R.id.news_list);
        this.listView.setAdapter(this.adapter);

        EditText searchBox = (EditText) this.findViewById(R.id.search_box);
        searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    if(InternetUtil.isOnline(NewsActivity.this))
                        new NewsFetch(true, searchBox.getText().toString(), new Runnable() {
                            @Override
                            public void run() {
                                NewsActivity.this.refreshListView();

                                searchBox.clearFocus();
                                ((InputMethodManager) NewsActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
                            }
                        }).execute(NewsActivity.this);
                    else {
                        Toast.makeText(NewsActivity.this, "Please check your internet connection.", Toast.LENGTH_LONG).show();

                        searchBox.clearFocus();
                        ((InputMethodManager) NewsActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
                    }

                    return true;
                }
                return false;
            }
        });
        ((ImageButton) this.findViewById(R.id.clear_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBox.setText("");

                SessionCache.news = SessionCache.headlines;
                NewsActivity.this.refreshListView();
            }
        });
    }

    @Override
    public void onBackPressed() {
        EditText searchBox = (EditText) this.findViewById(R.id.search_box);

        if(pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            this.finish();
        }
        else Toast.makeText(this, "Press back again to exit.", Toast.LENGTH_SHORT).show();

        pressedTime = System.currentTimeMillis();
    }
}