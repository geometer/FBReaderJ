package com.yotadevices.yotaphone2.fbreader;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.text.TextUtils;

import com.abbyy.mobile.lingvo.api.TranslationContract;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class AbbyyTranslator extends AsyncTask<String, Integer, Boolean> {

    public static class Translate {
        public String Language;
        public String Translation;
        public String Heading;
        public String ArticleURI;
        public String DictionaryArticleURI;
        public String SoundURI;
    }

    public interface TranslateCompletitionResult {
        public enum Error {
            NOTHING_TO_TRANSLATE,
            TRANSLATION_NOT_FOUND,
            UNKNOWN_ERROR
        }
        public void onTranslationComplete(List<Translate> results);
        public void onTranslationError(Error error);
    }

    private final TranslateCompletitionResult mListener;
    private final ContentResolver mResolver;
    private TranslateCompletitionResult.Error mError;
    private ArrayList<Translate> mTranslationResult;

    public AbbyyTranslator(ContentResolver resolver, TranslateCompletitionResult listener) {
        mListener = listener;
        mResolver = resolver;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        if (mResolver == null || params.length == 0 || TextUtils.isEmpty(params[0])) {
            mError = TranslateCompletitionResult.Error.NOTHING_TO_TRANSLATE;
            return Boolean.FALSE;
        }
        final String[] projection = {
                TranslationContract.Translations._ID,
                TranslationContract.Translations.HEADING,
                TranslationContract.Translations.TRANSLATION,
                TranslationContract.Translations.LANGUAGE_FROM,
                TranslationContract.Translations.LANGUAGE_TO,
                TranslationContract.Translations.ARTICLE_URI,
                TranslationContract.Translations.DICTIONARY_ARTICLE_URI,
                TranslationContract.Translations.SOUND_URI
        };

        final Uri toTranslate = createTranslationUri(params[0]);
        String[] args = {""};
        Cursor data = mResolver.query(toTranslate, projection, null, null, null);
        if (data == null) {
            mError = TranslateCompletitionResult.Error.UNKNOWN_ERROR;
            return Boolean.FALSE;
        }
        final TreeMap<String, Integer> columns = new TreeMap<String, Integer>();
        for (String column : projection) {
            columns.put(column, data.getColumnIndex(column));
        }
        mTranslationResult = new ArrayList<Translate>();

        while (data.moveToNext()) {
            Translate translate = new Translate();
            translate.Language = data.getString(columns.get(TranslationContract.Translations.LANGUAGE_TO));
            translate.Translation = data.getString(columns.get(TranslationContract.Translations.TRANSLATION));
            translate.Heading = data.getString(columns.get(TranslationContract.Translations.HEADING));
            translate.ArticleURI = data.getString(columns.get(TranslationContract.Translations.ARTICLE_URI));
            translate.DictionaryArticleURI = data.getString(columns.get(TranslationContract.Translations.DICTIONARY_ARTICLE_URI));
            translate.SoundURI = data.getString(columns.get(TranslationContract.Translations.SOUND_URI));
            mTranslationResult.add(translate);
        }
        if (mTranslationResult.size() > 0) {
            return Boolean.TRUE;
        }
        mError = TranslateCompletitionResult.Error.TRANSLATION_NOT_FOUND;
        return Boolean.FALSE;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if (aBoolean) {
            mListener.onTranslationComplete(mTranslationResult);
        }
        else {
            mListener.onTranslationError(mError);
        }
    }

    private Uri createTranslationUri(final String text) {
        return new TranslationContract.Translations.UriBuilder( text )
                .setSourceLanguage("")
                .setTargetLanguage("")
                .setForceLemmatization(true)
                .setEnablePrefixVariants(false)
                .setEnableSuggestions(false)
                .setEnableInverseLookup(false)
                .setTranslateSuggestions(false)
                .setTranslateVariants(false)
                .build();
    }
}
