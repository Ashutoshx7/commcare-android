package org.odk.collect.android.jr.extensions;

import org.javarosa.core.model.FormDef;
import org.javarosa.xform.parse.ImageRestrictionExtensionParser;
import org.javarosa.xform.parse.QuestionExtensionParser;
import org.javarosa.xform.util.XFormUtils;

import java.io.InputStream;
import java.util.Vector;

/**
 * Created by amstone326 on 8/31/15.
 */
public class XFormExtensionUtils {

    public static Vector<QuestionExtensionParser> getAllAndroidExtensionParsers() {
        Vector<QuestionExtensionParser> extensionParsers = new Vector<>();
        extensionParsers.add(new ImageRestrictionExtensionParser());
        // Add any future extension parsers for CommCare Android here
        return extensionParsers;
    }

    // Return the form def that results from parsing the given input stream, with all Android
    // extension parsers
    public static FormDef getFormFromInputStream(InputStream is) {
        return XFormUtils.getFormFromInputStream(is, getAllAndroidExtensionParsers());
    }

}
