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
import android.os.Bundle;
import android.widget.Toast;

import xyz.nathannestein.urphnews.util.InternetUtil;
import xyz.nathannestein.urphnews.util.NewsFetch;

public class MainActivity extends AppCompatActivity {
    private long pressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        if(InternetUtil.isOnline(this))
            new NewsFetch(false, null, null).execute(this);
        else {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3000);
                        MainActivity.this.finish();
                    }
                    catch(Exception ex) {}
                }
            });

            Toast.makeText(this, "Please check your internet connection.", Toast.LENGTH_LONG).show();
            try {
                thread.join();
                thread.start();
            }
            catch(Exception ex) { }
        }
    }

    @Override
    public void onBackPressed() {
        if(pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            this.finish();
        }
        else Toast.makeText(this, "Press back again to exit.", Toast.LENGTH_SHORT).show();

        pressedTime = System.currentTimeMillis();
    }
}