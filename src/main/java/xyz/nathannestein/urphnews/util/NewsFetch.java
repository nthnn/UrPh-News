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

package xyz.nathannestein.urphnews.util;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import xyz.nathannestein.urphnews.NewsActivity;
import xyz.nathannestein.urphnews.core.SessionCache;
import xyz.nathannestein.urphnews.core.News;

public class NewsFetch extends AsyncTask<Activity, Void, Void> {
    private Activity act;
    private boolean isSearch;
    private String query;
    private Runnable postExec;

    public NewsFetch(boolean isSearch, String query, Runnable postExec) {
        this.isSearch = isSearch;
        this.query = query;
        this.postExec = postExec;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        if(!this.isSearch) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    NewsFetch.this.act.startActivity(new Intent(NewsFetch.this.act, NewsActivity.class));
                    NewsFetch.this.act.finish();

                    timer.cancel();
                }
            }, 2000);

        }
        else this.postExec.run();
    }

    @Override
    protected Void doInBackground(Activity... vd) {
        this.act = vd[0];

        if(!this.isSearch) SessionCache.headlines = SessionCache.news = (ArrayList<News>) this.getHeadlines();
        else SessionCache.news = (ArrayList<News>) this.search(this.query);

        return null;
    }

    private String readUrl(String href) {
        try {
            StringBuilder sb = new StringBuilder();
            URL url = new URL(href);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("User-Agent", "Android");
            connection.setRequestMethod("GET");
            connection.connect();

            InputStream is = null;
            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                is = connection.getErrorStream();
            else is = connection.getInputStream();

            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            String line;

            while((line = in.readLine()) != null)
                sb.append(line);
            in.close();

            return sb.toString();
        }
        catch(Exception ex) {
            Toast.makeText(this.act, "Please check your internet connection.", Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    private static List<News> parseJSON(String JSONString) {
        List<News> news = new ArrayList<>();

        try {
            JSONObject objs = new JSONObject(JSONString);
            JSONArray articles = objs.getJSONArray("articles");

            for(int i = 0; i < 20; i++) {
                JSONObject article = articles.getJSONObject(i);

                if(article.get("content") != null)
                    news.add(new News(article.get("author").toString(), article.get("content").toString(), article.get("description").toString(), article.get("publishedAt").toString(), article.getJSONObject("source").get("name").toString(), article.get("title").toString(), article.get("url").toString(), article.get("urlToImage").toString()));
            }
        }
        catch(Exception Ex) {
            return news;
        }

        Collections.shuffle(news);
        return news;
    }

    private List<News> getHeadlines() {
        return NewsFetch.parseJSON(this.readUrl("https://newsapi.org/v2/top-headlines?country=ph&apiKey=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"));
    }

    private List<News> search(String query) {
        try {
            return NewsFetch.parseJSON(this.readUrl("https://newsapi.org/v2/everything?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8.toString()) + "%20philippines&apiKey=c498423287fd4643af56c3347429e343"));
        }
        catch(Exception ex) { }

        return null;
    }
}