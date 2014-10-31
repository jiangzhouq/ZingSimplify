/*
 * Copyright 2011 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android.result.supplement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.google.zxing.client.android.HttpHelper;
import com.google.zxing.client.android.LocaleManager;
import com.google.zxing.client.android.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * @author Kamil Kaczmarczyk
 * @author Sean Owen
 */
final class BookResultInfoRetriever extends SupplementalInfoRetriever {

	private final String isbn;
	private final String source;
	private final Context context;

	BookResultInfoRetriever(TextView textView, String isbn, Context context) {
		super(textView);
		this.isbn = isbn;
		this.source = context.getString(R.string.msg_google_books);
		this.context = context;
	}

	@Override
	void retrieveSupplementalInfo() throws IOException {

		CharSequence contents = HttpHelper.downloadViaHttp(
				"https://api.douban.com/v2/book/isbn/:" + isbn,
				HttpHelper.ContentType.JSON);

		if (contents.length() == 0) {
			return;
		}

		String average;
		String imageUrl;
		Collection<String> authors = null;

		try {

			JSONObject topLevel = (JSONObject) new JSONTokener(
					contents.toString()).nextValue();
			// title
			Log.d("qiqi", "title:" + topLevel.getString("title"));
			// author
			JSONArray authorsArray = topLevel.optJSONArray("author");
			if (authorsArray != null && !authorsArray.isNull(0)) {
				authors = new ArrayList<>(authorsArray.length());
				for (int i = 0; i < authorsArray.length(); i++) {
					authors.add(authorsArray.getString(i));
				}
			}
			Log.d("qiqi", "authors:" + authors);
			//rating
			JSONObject ratingInfo = ((JSONObject) topLevel.getJSONObject("rating"));
			average = ratingInfo.getString("average");
			Log.d("qiqi", "average:" + average);
			//publisher
			Log.d("qiqi", "publisher:" + topLevel.getString("publisher"));
			//image
			JSONObject iamgesInfo = ((JSONObject) topLevel.getJSONObject("images"));
			imageUrl = iamgesInfo.getString("small");
			Log.d("qiqi", "imageUrl:" + imageUrl);
		} catch (JSONException e) {
			throw new IOException(e);
		}

		Collection<String> newTexts = new ArrayList<>();
		maybeAddText(average, newTexts);
		maybeAddTextSeries(authors, newTexts);

		String baseBookUri = "http://www.google."
				+ LocaleManager.getBookSearchCountryTLD(context)
				+ "/search?tbm=bks&source=zxing&q=";

		append(isbn, source, newTexts.toArray(new String[newTexts.size()]),
				baseBookUri + isbn);
	}

}
