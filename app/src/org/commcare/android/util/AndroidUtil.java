/**
 * 
 */
package org.commcare.android.util;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import org.commcare.dalvik.BuildConfig;
import org.javarosa.core.util.DataUtil;
import org.javarosa.core.util.DataUtil.UnionLambda;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author ctsims
 *
 */
public final class AndroidUtil {
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    /**
     * Generate a value suitable for use in {@link #setId(int)}.
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    @SuppressLint("NewApi")
    public static int generateViewId() {
        //raw implementation for < API 17
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            for (;;) {
                final int result = sNextGeneratedId.get();
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        } else {
            //Whatever the current implementation is otherwise
            return View.generateViewId();
        }
    }
    
    /**
     * Initialize platform specific methods for common handlers
     */
    public static void initializeStaticHandlers() {
        DataUtil.setUnionLambda(new AndroidUnionLambda());
    }
    
    public static class AndroidUnionLambda extends UnionLambda {
        public <T> Vector<T> union(final Vector<T> a, final Vector<T> b) {
            //This is kind of (ok, so really) awkward looking, but we can't use sets in 
            //ccj2me (Thanks, Nokia!) also, there's no _collections_ interface in
            //j2me (thanks Sun!) so this is what we get.
            final HashSet<T> joined = new HashSet<T>(a);
            joined.addAll(a);
            
            final HashSet<T> other = new HashSet<T>();
            other.addAll(b);
            
            joined.retainAll(other);
            
            a.clear();
            a.addAll(joined);
            return a;
        }
    }

    public static void setClickListenersForEverything(final Activity activity, final ViewGroup v) {
        if (BuildConfig.DEBUG) {
            final ViewGroup layout = v != null ? v : (ViewGroup) activity.findViewById(android.R.id.content);
            final LinkedList<View> views = new LinkedList<View>();
            views.add(layout);
            for (int i = 0; !views.isEmpty(); i++) {
                final View child = views.getFirst();
                views.removeFirst();
                Log.i("GetID", "Adding onClickListener to view " + child);
                child.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        String vid;
                        try {
                            vid = "View id is: " + v.getResources().getResourceName(v.getId()) + " ( " + v.getId() + " )";
                        } catch (final Resources.NotFoundException excp) {
                            vid = "View id is: " + v.getId();
                        }
                        Log.i("CLK", vid);
                    }
                });
                if(child instanceof ViewGroup) {
                    final ViewGroup vg = (ViewGroup) child;
                    for (int j = 0; j < vg.getChildCount(); j++) {
                        final View gchild = vg.getChildAt(j);
                        if (!views.contains(gchild)) views.add(gchild);
                    }
                }
            }
        }
    }

    public static void setClickListenersForEverything(final Activity act){
        setClickListenersForEverything(act, (ViewGroup) act.findViewById(android.R.id.content));
    }

    /**
     * Returns an int array with the color values for the given attributes (R.attr).
     * @param context
     * @param attrs
     * @return
     */
    public static int[] getThemeColorIDs(final Context context, final int[] attrs){
        final int[] colors = new int[attrs.length];
        final Resources.Theme theme = context.getTheme();
        for (int i = 0; i < attrs.length; i++) {
            final TypedValue typedValue = new TypedValue();
            theme.resolveAttribute(attrs[i], typedValue, true);
            colors[i] = typedValue.data;
        }
        return colors;
    }
}
