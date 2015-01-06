/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.utils.history;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * A history store of any object extending {@link Parcelable}.<br>
 * <b>Note:</b> This class does not support storing its {@link HistoryListener}
 * 's into {@link Parcelable}. You must re-build all listeners after getting
 * your {@link HistoryStore} from a {@link Bundle} for example.
 * 
 * @author Hai Bison
 * @since v2.0 alpha
 */
public class HistoryStore<A extends Parcelable> implements History<A> {

    private final ArrayList<A> mHistoryList = new ArrayList<A>();
    private final int mMaxSize;
    private final List<HistoryListener<A>> mListeners = new ArrayList<HistoryListener<A>>();

    /**
     * Creates new {@link HistoryStore}
     * 
     * @param maxSize
     *            the maximum size that allowed, if it is &lt;= {@code 0},
     *            {@code 100} will be used
     */
    public HistoryStore(int maxSize) {
        this.mMaxSize = maxSize > 0 ? maxSize : 100;
    }

    @Override
    public void push(A newItem) {
        if (newItem == null)
            return;

        if (!mHistoryList.isEmpty() && mHistoryList.indexOf(newItem) == mHistoryList.size() - 1)
            return;

        mHistoryList.add(newItem);
        notifyHistoryChanged();
    }// push()

    @Override
    public void truncateAfter(A item) {
        if (item == null)
            return;

        int idx = mHistoryList.indexOf(item);
        if (idx >= 0 && idx < mHistoryList.size() - 1) {
            mHistoryList.subList(idx + 1, mHistoryList.size()).clear();
            notifyHistoryChanged();
        }
    }// truncateAfter()

    @Override
    public void remove(A item) {
        if (mHistoryList.remove(item))
            notifyHistoryChanged();
    }

    @Override
    public void removeAll(HistoryFilter<A> filter) {
        boolean changed = false;
        for (int i = mHistoryList.size() - 1; i >= 0; i--) {
            if (filter.accept(mHistoryList.get(i))) {
                mHistoryList.remove(i);
                if (!changed)
                    changed = true;
            }
        }// for

        if (changed)
            notifyHistoryChanged();
    }// removeAll()

    @Override
    public void notifyHistoryChanged() {
        for (HistoryListener<A> listener : mListeners)
            listener.onChanged(this);
    }

    @Override
    public int size() {
        return mHistoryList.size();
    }

    @Override
    public int indexOf(A a) {
        return mHistoryList.indexOf(a);
    }

    @Override
    public A prevOf(A a) {
        int idx = mHistoryList.indexOf(a);
        if (idx > 0)
            return mHistoryList.get(idx - 1);
        return null;
    }

    @Override
    public A nextOf(A a) {
        int idx = mHistoryList.indexOf(a);
        if (idx >= 0 && idx < mHistoryList.size() - 1)
            return mHistoryList.get(idx + 1);
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ArrayList<A> items() {
        return (ArrayList<A>) mHistoryList.clone();
    }// items()

    @Override
    public boolean isEmpty() {
        return mHistoryList.isEmpty();
    }

    @Override
    public void clear() {
        mHistoryList.clear();
        notifyHistoryChanged();
    }

    @Override
    public void addListener(HistoryListener<A> listener) {
        mListeners.add(listener);
    }

    @Override
    public void removeListener(HistoryListener<A> listener) {
        mListeners.remove(listener);
    }

    /*-----------------------------------------------------
     * Parcelable
     */

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mMaxSize);

        dest.writeInt(size());
        for (int i = 0; i < size(); i++)
            dest.writeParcelable(mHistoryList.get(i), flags);
    }

    @SuppressWarnings("rawtypes")
    public static final Parcelable.Creator<HistoryStore> CREATOR = new Parcelable.Creator<HistoryStore>() {

        public HistoryStore createFromParcel(Parcel in) {
            return new HistoryStore(in);
        }

        public HistoryStore[] newArray(int size) {
            return new HistoryStore[size];
        }
    };

    @SuppressWarnings("unchecked")
    private HistoryStore(Parcel in) {
        mMaxSize = in.readInt();

        int count = in.readInt();
        for (int i = 0; i < count; i++)
            mHistoryList.add((A) in.readParcelable(null));
    }
}
