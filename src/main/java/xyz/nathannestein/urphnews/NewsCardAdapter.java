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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import xyz.nathannestein.urphnews.core.News;

public class NewsCardAdapter extends ArrayAdapter<News> {
    public List<News> news;
    private Context context;
    private ListView listView;

    public NewsCardAdapter(@NonNull Context context, ArrayList<News> news) {
        super(context, 0, news);
        this.context = context;
        this.news = news;
        this.listView = listView;
    }

    @NonNull @Override
    public View getView(int position, @Nullable View contentView, @NonNull ViewGroup parent) {
        if(contentView == null)
            contentView = LayoutInflater.from(this.context).inflate(R.layout.card_news, parent, false);

        News currentNewsItem = this.news.get(position);
        ImageView imv = (ImageView) contentView.findViewById(R.id.card_image_header);

        ((TextView) contentView.findViewById(R.id.card_title)).setText(currentNewsItem.title);

        if(currentNewsItem.description.equals("null"))
            ((TextView) contentView.findViewById(R.id.card_description)).setText((!currentNewsItem.author.equals("null") ? ("By:" + currentNewsItem.author) : ("Via: " + currentNewsItem.sourceName)) + " (" + currentNewsItem.publishedAt + ")");
        else {
            if(!currentNewsItem.publishedAt.equals("null")) {
                TextView publishedAt = (TextView) contentView.findViewById(R.id.card_published_at);
                publishedAt.setVisibility(View.VISIBLE);
                publishedAt.setText(currentNewsItem.publishedAt);
            }

            ((TextView) contentView.findViewById(R.id.card_description)).setText(currentNewsItem.description);
        }

        if(currentNewsItem.urlToImage.equals("null"))
            ((ImageView) contentView.findViewById(R.id.card_image_header)).setVisibility(View.GONE);
        else Glide.with(contentView).load(currentNewsItem.urlToImage).into(imv);

        final View cv = contentView;
        final ImageButton popupButton = (ImageButton) contentView.findViewById(R.id.card_popup_menu);
        popupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch(item.getItemId()) {
                            case R.id.card_menu_copy_link:
                                ((ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Link", currentNewsItem.url));
                                Toast.makeText(NewsCardAdapter.this.context, "Link copied!", Toast.LENGTH_SHORT).show();
                                return true;

                            case R.id.card_menu_share_link:
                                {
                                    Intent sendIntent = new Intent();
                                    sendIntent.setAction(Intent.ACTION_SEND);
                                    sendIntent.putExtra(Intent.EXTRA_TITLE, currentNewsItem.title);
                                    sendIntent.putExtra(Intent.EXTRA_TEXT, currentNewsItem.url);
                                    sendIntent.setType("text/plain");

                                    v.getContext().startActivity(Intent.createChooser(sendIntent, "Share news"));
                                }
                                return true;

                            case R.id.card_menu_share_as_image:
                                {
                                    cv.setDrawingCacheEnabled(true);
                                    popupButton.setVisibility(View.GONE);

                                    Bitmap bit = Bitmap.createBitmap(cv.getDrawingCache());

                                    cv.setDrawingCacheEnabled(false);
                                    popupButton.setVisibility(View.VISIBLE);

                                    File imageFile = new File(context.getApplicationContext().getFilesDir(), "screenshot.png");
                                    OutputStream os;

                                    try {
                                        os = new FileOutputStream(imageFile);
                                        bit.compress(Bitmap.CompressFormat.PNG, 100, os);

                                        os.flush();
                                        os.close();
                                    } catch (Exception e) { }

                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_SEND);
                                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                    Uri imageUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, imageFile);
                                    intent.putExtra(Intent.EXTRA_STREAM, imageUri);
                                    intent.setType("image/png");

                                    context.startActivity(intent);
                                }
                                return true;
                        }

                        return false;
                    }
                });
                popup.getMenuInflater().inflate(R.menu.card_menu, popup.getMenu());
                popup.show();
            }
        });

        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewsCardAdapter.this.context, NewsWebViewActivity.class);
                intent.putExtra("newsUrl", currentNewsItem.url);
                intent.putExtra("newsTitle", currentNewsItem.title);

                NewsCardAdapter.this.context.startActivity(intent);
            }
        });
        return contentView;
    }
}
