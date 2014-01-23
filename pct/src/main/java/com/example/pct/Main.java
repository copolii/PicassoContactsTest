package com.example.pct;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.Locale;

/**
 Created by hookt on 23/01/14.
 */
public class Main
  extends FragmentActivity
  implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOGTAG = "PCT";

    private static final String[] PROJECTION = {
                                                 ContactsContract.Contacts._ID,
                                                 ContactsContract.Contacts.DISPLAY_NAME,
                                                 ContactsContract.Contacts.LOOKUP_KEY
    };

    private static final int IDX_RAW  = 0;
    private static final int IDX_NAME = 1;
    private static final int IDX_LUK  = 2;

    private LayoutInflater inflater;
    private CursorAdapter adapter;

    @Override
    protected void onCreate (final Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        final ListView lv = new ListView (this);
        lv.setId (android.R.id.list);
        setContentView (lv);
        inflater = LayoutInflater.from (this);
        adapter = new ContactsAdapter (null);
        lv.setAdapter (adapter);

        getSupportLoaderManager ().initLoader (0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader (final int i, final Bundle bundle) {
        return new CursorLoader (this,
                                 ContactsContract.Contacts.CONTENT_URI, PROJECTION, null, null,
                                 null);
    }

    @Override
    public void onLoadFinished (final Loader<Cursor> cursorLoader, final Cursor cursor) {
        adapter.swapCursor (cursor);
    }

    @Override
    public void onLoaderReset (final Loader<Cursor> cursorLoader) {
        adapter.swapCursor (null);
    }

    private class ContactsAdapter
      extends CursorAdapter {
        ContactsAdapter (final Cursor cursor) {
            super (Main.this, cursor, false);
        }

        @Override
        public View newView (final Context context, final Cursor cursor, final ViewGroup viewGroup) {
            final View v = inflater.inflate (R.layout.list_item, viewGroup, false);
            v.setTag (new Tag (v));
            return v;
        }

        @Override
        public void bindView (final View view, final Context context, final Cursor cursor) {
            final String lookupKey = cursor.getString (IDX_LUK);
            final long raw = cursor.getLong (IDX_RAW);

            final Tag tag = (Tag) view.getTag ();

            final String name = cursor.getString (IDX_NAME);

            tag.name.setText (name);

            final Uri lookupUri = Uri.withAppendedPath (ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
            final Uri contactUri = ContentUris.withAppendedId (ContactsContract.Contacts.CONTENT_URI, raw);

            Log.d (LOGTAG, String.format (Locale.ENGLISH, "Name: %s Lookup: %s Uri: %s", name, lookupUri, contactUri));

            try {
                final InputStream stream = ContactsContract.Contacts.openContactPhotoInputStream (getContentResolver (),
                                                                                        contactUri);

                Log.d (LOGTAG, String.format (Locale.ENGLISH, "Name: %s Stream: %s", name, stream));
                stream.close ();
            } catch (Exception e) {
                Log.d (LOGTAG, String.format (Locale.ENGLISH, "%s: %s", name, e.getMessage ()));
            } 

            Picasso
              .with (Main.this)
              .load (lookupUri)
              .placeholder (R.drawable.ic_social_person)
              .error (R.drawable.ic_alerts_and_states_warning)
              .into (tag.photo_luk);

            Picasso
              .with (Main.this)
              .load (contactUri)
              .placeholder (R.drawable.ic_social_person)
              .error (R.drawable.ic_alerts_and_states_warning)
              .into (tag.photo_raw);
        }
    }

    private static class Tag {
        final TextView  name;
        final ImageView photo_luk;
        final ImageView photo_raw;

        Tag (final View v) {
            name = (TextView) v.findViewById (android.R.id.title);
            photo_luk = (ImageView) v.findViewById (android.R.id.icon1);
            photo_raw = (ImageView) v.findViewById (android.R.id.icon2);
        }
    }
}
