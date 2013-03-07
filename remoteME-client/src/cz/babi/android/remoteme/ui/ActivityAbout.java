/**
 * Copyright 2013 Martin Misiarz (dev.misiarz@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cz.babi.android.remoteme.ui;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;
import cz.babi.android.remoteme.R;
import cz.babi.android.remoteme.common.Common;

/**
 * Activity About.
 * 
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class ActivityAbout extends Activity {
	
	private static final String TAG_CLASS_NAME = ActivityAbout.class.getSimpleName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(Common.DEBUG)
			Log.d(TAG_CLASS_NAME, "[onCreate]");
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_about);
		
		TextView title = (TextView)findViewById(R.id.about_title);
		title.setText(getString(R.string.text_app_name) + " v"
				+ getString(R.string.text_app_version));
		
		TextView homepageLink = (TextView)findViewById(R.id.about_homepage_link);
		homepageLink.setText(Html.fromHtml(getString(R.string.about_information_text) + " <a href=\""
				+ getString(R.string.about_homepage_html) + "\">"
				+ getString(R.string.about_homepage_text) + "</a>."));
		homepageLink.setMovementMethod(LinkMovementMethod.getInstance());
		
		TextView supportLink = (TextView)findViewById(R.id.about_support_link);
		supportLink.setText(Html.fromHtml(getString(R.string.about_support_text) + " <a href=\""
				+ Common.SUPPORT_LINK + "\">"
				+ getString(R.string.about_support_beer_text) + "</a>. " +
				getString(R.string.about_support_thanks_text) + "."));
		supportLink.setMovementMethod(LinkMovementMethod.getInstance());
		
		TextView copyrightLink = (TextView)findViewById(R.id.about_copyright_link);
		copyrightLink.setText(Html.fromHtml(getString(R.string.about_license_copyright_text) + " <a href=\"mailto:"
				+ getString(R.string.about_license_copyright_email) + "\">"
				+ getString(R.string.about_license_copyright_email) + "</a>."));
		copyrightLink.setMovementMethod(LinkMovementMethod.getInstance());
		
		TextView licenseLink = (TextView)findViewById(R.id.about_license_link);
		licenseLink.setText(Html.fromHtml(getString(R.string.about_license_text) + " <a href=\""
				+ getString(R.string.about_license_apache_html) + "\">"
				+ getString(R.string.about_license_apache_text) + "</a>."));
		licenseLink.setMovementMethod(LinkMovementMethod.getInstance());
		
		TextView newquickaction3d = (TextView)findViewById(R.id.about_content_newquickactiond3d);
		newquickaction3d.setText(Html.fromHtml("<a href=\""
				+ getString(R.string.about_content_newquickaction3d_html) + "\">"
				+ getString(R.string.about_content_newquickaction3d_text) + "</a>"));
		newquickaction3d.setMovementMethod(LinkMovementMethod.getInstance());
		
		TextView multiselectlistpreference = (TextView)findViewById(R.id.about_content_multiselectlistpreference);
		multiselectlistpreference.setText(Html.fromHtml("<a href=\""
				+ getString(R.string.about_content_multiselectlistpreference_html) + "\">"
				+ getString(R.string.about_content_multiselectlistpreference_text) + "</a>"));
		multiselectlistpreference.setMovementMethod(LinkMovementMethod.getInstance());
		
		TextView background = (TextView)findViewById(R.id.about_content_background);
		background.setText(Html.fromHtml("<a href=\""
				+ getString(R.string.about_content_background_html) + "\">"
				+ getString(R.string.about_content_background_text) + "</a>"));
		background.setMovementMethod(LinkMovementMethod.getInstance());
		
		TextView web02ama = (TextView)findViewById(R.id.about_icon_web02ama);
		web02ama.setText(Html.fromHtml("<a href=\""
				+ getString(R.string.about_icon_web02ama_html) + "\">"
				+ getString(R.string.about_icon_web02ama_text) + "</a>"));
		web02ama.setMovementMethod(LinkMovementMethod.getInstance());
		
		TextView meliae = (TextView)findViewById(R.id.about_icon_meliae);
		meliae.setText(Html.fromHtml("<a href=\""
				+ getString(R.string.about_icon_meliae_html) + "\">"
				+ getString(R.string.about_icon_meliae_text) + "</a>"));
		meliae.setMovementMethod(LinkMovementMethod.getInstance());
		
		TextView aimp = (TextView)findViewById(R.id.about_icon_aimp);
		aimp.setText(Html.fromHtml("<a href=\""
				+ getString(R.string.about_icon_aimp_html) + "\">"
				+ getString(R.string.about_icon_aimp_text) + "</a>"));
		aimp.setMovementMethod(LinkMovementMethod.getInstance());
		
		TextView banshee = (TextView)findViewById(R.id.about_icon_banshee);
		banshee.setText(Html.fromHtml("<a href=\""
				+ getString(R.string.about_icon_banshee_html) + "\">"
				+ getString(R.string.about_icon_banshee_text) + "</a>"));
		banshee.setMovementMethod(LinkMovementMethod.getInstance());
		
		TextView bsplayer = (TextView)findViewById(R.id.about_icon_bsplayer);
		bsplayer.setText(Html.fromHtml("<a href=\""
				+ getString(R.string.about_icon_bsplayer_html) + "\">"
				+ getString(R.string.about_icon_bsplayer_text) + "</a>"));
		bsplayer.setMovementMethod(LinkMovementMethod.getInstance());
		
		TextView gommediaplayer = (TextView)findViewById(R.id.about_icon_gommediaplayer);
		gommediaplayer.setText(Html.fromHtml("<a href=\""
				+ getString(R.string.about_icon_gommediaplayer_html) + "\">"
				+ getString(R.string.about_icon_gommediaplayer_text) + "</a>"));
		gommediaplayer.setMovementMethod(LinkMovementMethod.getInstance());
		
		TextView kmplayer = (TextView)findViewById(R.id.about_icon_kmplayer);
		kmplayer.setText(Html.fromHtml("<a href=\""
				+ getString(R.string.about_icon_kmplayer_html) + "\">"
				+ getString(R.string.about_icon_kmplayer_text) + "</a>"));
		kmplayer.setMovementMethod(LinkMovementMethod.getInstance());
		
		TextView mediamonkey = (TextView)findViewById(R.id.about_icon_mediamonkey);
		mediamonkey.setText(Html.fromHtml("<a href=\""
				+ getString(R.string.about_icon_mediamonkey_html) + "\">"
				+ getString(R.string.about_icon_mediamonkey_text) + "</a>"));
		mediamonkey.setMovementMethod(LinkMovementMethod.getInstance());
		
		TextView mediaplayerclassic = (TextView)findViewById(R.id.about_icon_mediaplayerclassic);
		mediaplayerclassic.setText(Html.fromHtml("<a href=\""
				+ getString(R.string.about_icon_mediaplayerclassic_html) + "\">"
				+ getString(R.string.about_icon_mediaplayerclassic_text) + "</a>"));
		mediaplayerclassic.setMovementMethod(LinkMovementMethod.getInstance());
		
		TextView rhythmbox = (TextView)findViewById(R.id.about_icon_rhythmbox);
		rhythmbox.setText(Html.fromHtml("<a href=\""
				+ getString(R.string.about_icon_rhythmbox_html) + "\">"
				+ getString(R.string.about_icon_rhythmbox_text) + "</a>"));
		rhythmbox.setMovementMethod(LinkMovementMethod.getInstance());
		
		TextView vlc = (TextView)findViewById(R.id.about_icon_vlc);
		vlc.setText(Html.fromHtml("<a href=\""
				+ getString(R.string.about_icon_vlc_html) + "\">"
				+ getString(R.string.about_icon_vlc_text) + "</a>"));
		vlc.setMovementMethod(LinkMovementMethod.getInstance());
		
		TextView winamp = (TextView)findViewById(R.id.about_icon_winamp);
		winamp.setText(Html.fromHtml("<a href=\""
				+ getString(R.string.about_icon_winamp_html) + "\">"
				+ getString(R.string.about_icon_winamp_text) + "</a>"));
		winamp.setMovementMethod(LinkMovementMethod.getInstance());
		
		TextView wmp = (TextView)findViewById(R.id.about_icon_wmp);
		wmp.setText(Html.fromHtml("<a href=\""
				+ getString(R.string.about_icon_wmp_html) + "\">"
				+ getString(R.string.about_icon_wmp_text) + "</a>"));
		wmp.setMovementMethod(LinkMovementMethod.getInstance());
		
		setOrientation();
	}
	
	/**
	 * Set orientation.
	 */
	private void setOrientation() {
		String currentOrientationLock = PreferenceManager
				.getDefaultSharedPreferences(this).getString(
						getString(R.string.pref_name_orientation_lock),
						getString(R.string.pref_value_default));
		
		if(currentOrientationLock
				.equals(getString(R.string.pref_value_portait))) {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else if(currentOrientationLock
				.equals(getString(R.string.pref_value_landscape))) {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
	}
}
