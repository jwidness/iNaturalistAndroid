package org.inaturalist.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.flurry.android.FlurryAgent;
import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class NewsArticle extends SherlockFragmentActivity {
    public static final String KEY_ARTICLE = "article";
    public static final String KEY_IS_USER_FEED = "is_user_feed";

    private INaturalistApp mApp;
    private BetterJSONObject mArticle;

    private ActivityHelper mHelper;

    private TextView mArticleTitle;
    private WebView mArticleContentWeb;
    private TextView mArticleContent;
    private TextView mUsername;
    private ImageView mUserPic;
    private boolean mIsUserFeed;

    @Override
	protected void onStart()
	{
		super.onStart();
		FlurryAgent.onStartSession(this, INaturalistApp.getAppContext().getString(R.string.flurry_api_key));
		FlurryAgent.logEvent(this.getClass().getSimpleName());
	}

	@Override
	protected void onStop()
	{
		super.onStop();		
		FlurryAgent.onEndSession(this);
	}	

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        // Respond to the action bar's Up/Home button
        case android.R.id.home:
        	this.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    } 
 
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHelper = new ActivityHelper(this);

        final Intent intent = getIntent();
        setContentView(R.layout.article);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setLogo(R.drawable.ic_arrow_back);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle(R.string.article);

        mArticleTitle = (TextView) findViewById(R.id.article_title);
        mArticleContentWeb = (WebView) findViewById(R.id.article_content_web);
        mArticleContent = (TextView) findViewById(R.id.article_content);
        mUsername = (TextView) findViewById(R.id.username);
        mUserPic = (ImageView) findViewById(R.id.user_pic);

        if (mApp == null) {
            mApp = (INaturalistApp)getApplicationContext();
        }
        
        if (savedInstanceState == null) {
            mArticle = (BetterJSONObject) intent.getSerializableExtra(KEY_ARTICLE);
            mIsUserFeed = intent.getBooleanExtra(KEY_IS_USER_FEED, false);
        } else {
            mArticle = (BetterJSONObject) savedInstanceState.getSerializable(KEY_ARTICLE);
            mIsUserFeed = savedInstanceState.getBoolean(KEY_IS_USER_FEED);
        }

        if (mArticle == null) {
            finish();
            return;
        }

        mArticleTitle.setText(mArticle.getString("title"));

        if (mIsUserFeed) {
            mArticleContent.setVisibility(View.GONE);
            mArticleContentWeb.setVisibility(View.VISIBLE);
            mArticleContentWeb.setBackgroundColor(Color.TRANSPARENT);
            mArticleContentWeb.setVerticalScrollBarEnabled(false);
            String html = "<html><head><style type=\"text/css\">body { line-height: 22pt; margin: 0; padding: 0; font-family: \"HelveticaNeue-UltraLight\", \"Segoe UI\", \"Roboto Light\", sans-serif; font-size: medium; }</style></head><body>";
            mArticleContentWeb.loadDataWithBaseURL("", html + mArticle.getString("body") + "</body></html>", "text/html", "UTF-8", "");
        } else {
            mArticleContentWeb.setVisibility(View.GONE);
            mArticleContent.setVisibility(View.VISIBLE);
            mArticleContent.setText(Html.fromHtml(mArticle.getString("body")));
            Linkify.addLinks(mArticleContent, Linkify.ALL);
        }

        JSONObject user = mArticle.getJSONObject("user");
        mUsername.setText(user.optString("login"));

        if (user.has("user_icon_url") && !user.isNull("user_icon_url")) {
            UrlImageViewHelper.setUrlDrawable(mUserPic, user.optString("user_icon_url"), R.drawable.ic_account_circle_black_24dp, new UrlImageViewCallback() {
                @Override
                public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
                    // Nothing to do here
                }

                @Override
                public Bitmap onPreSetBitmap(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
                    // Return a circular version of the profile picture
                    return ImageUtils.getCircleBitmap(loadedBitmap);
                }
            });
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(KEY_ARTICLE, mArticle);
        outState.putBoolean(KEY_IS_USER_FEED, mIsUserFeed);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mApp == null) {
            mApp = (INaturalistApp) getApplicationContext();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
