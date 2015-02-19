/**
 * 
 */
package org.commcare.android.util;

import java.util.HashSet;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import org.javarosa.core.util.DataUtil;
import org.javarosa.core.util.DataUtil.UnionLambda;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.View;

/**
 * @author ctsims
 *
 */
public class AndroidUtil {
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
        public <T> Vector<T> union(Vector<T> a, Vector<T> b) {
            //This is kind of (ok, so really) awkward looking, but we can't use sets in 
            //ccj2me (Thanks, Nokia!) also, there's no _collections_ interface in
            //j2me (thanks Sun!) so this is what we get.
            HashSet<T> joined = new HashSet<T>(a);
            joined.addAll(a);
            
            HashSet<T> other = new HashSet<T>();
            other.addAll(b);
            
            joined.retainAll(other);
            
            a.clear();
            a.addAll(joined);
            return a;
        }
    }
}
