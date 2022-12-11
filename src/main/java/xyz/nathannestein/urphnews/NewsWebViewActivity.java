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

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.adblockplus.libadblockplus.android.settings.AdblockHelper;
import org.adblockplus.libadblockplus.android.settings.AdblockSettings;
import org.adblockplus.libadblockplus.android.settings.AdblockSettingsStorage;
import org.adblockplus.libadblockplus.android.webview.AdblockWebView;

public class NewsWebViewActivity extends AppCompatActivity {
    private AdblockWebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_news_web_view);

        String url = null, title = null;
        if(savedInstanceState != null) {
            url = (String) savedInstanceState.getSerializable("newsUrl");
            title = (String) savedInstanceState.getSerializable("newsTitle");
        }
        else {
            url = this.getIntent().getExtras().getString("newsUrl");
            title = this.getIntent().getExtras().getString("newsTitle");
        }

        ((ImageButton) this.findViewById(R.id.close_webview_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewsWebViewActivity.this.finish();
                NewsWebViewActivity.super.onBackPressed();
            }
        });

        if(!AdblockHelper.get().isInit()) {
            AdblockHelper.get().init(this, this.getFilesDir().getAbsolutePath(), true, AdblockHelper.PREFERENCE_NAME);
            AdblockHelper.get().getProvider().retain(true);
        }

        AdblockSettingsStorage storage = AdblockHelper.get().getStorage();
        AdblockSettings settings = storage.load();

        if(settings == null)
            settings = AdblockSettingsStorage.getDefaultSettings(this);
        settings.setAcceptableAdsEnabled(false);
        settings.setAdblockEnabled(true);
        storage.save(settings);

        final TextView titleView = (TextView) this.findViewById(R.id.news_webview_title), urlView = (TextView) this.findViewById(R.id.news_webview_url);
        final ProgressBar progressView = (ProgressBar) this.findViewById(R.id.webview_load_progress);

        titleView.setText(title);
        urlView.setText(url);

        this.webView = (AdblockWebView) this.findViewById(R.id.adbp_webview);
        this.webView.setProvider(AdblockHelper.get().getProvider());
        this.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                progressView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                urlView.setText(url);
                titleView.setText(view.getTitle());
                progressView.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                progressView.setVisibility(View.GONE);
            }
        });
        this.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);

                progressView.setProgress(newProgress);
            }
        });
        this.webView.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        if(this.webView.canGoBack())
            this.webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AdblockHelper.get().getProvider().release();
    }
}