package org.odk.collect.android.widgets;

import org.commcare.dalvik.R;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.chrono.EthiopicChronology;
import org.odk.collect.android.utilities.UniversalDate;

import android.content.Context;
import android.content.res.Resources;

/**
 * Ethiopian Date Widget.
 * 
 * @author Alex Little (alex@alexlittle.net), Richard Lu
 */
public class EthiopianDateWidget extends AbstractUniversalDateWidget {
    
    private static final Chronology CHRON_ETH = EthiopicChronology.getInstance();

    public EthiopianDateWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
    }
    
    private UniversalDate constructUniversalDate(DateTime dt) {
        return new UniversalDate(
                dt.getYear(),
                dt.getMonthOfYear(),
                dt.getDayOfMonth(),
                dt.getMillis()
        );
    }
    
    @Override
    protected UniversalDate decrementMonth(long millisFromJavaEpoch) {
        DateTime dt = new DateTime(millisFromJavaEpoch)
            .withChronology(CHRON_ETH)
            .minusMonths(1);
        return constructUniversalDate(dt);
    }

    @Override
    protected UniversalDate decrementYear(long millisFromJavaEpoch) {
        DateTime dt = new DateTime(millisFromJavaEpoch)
            .withChronology(CHRON_ETH)
            .minusYears(1);
        return constructUniversalDate(dt);
    }

    @Override
    protected UniversalDate fromMillis(long millisFromJavaEpoch) {
        DateTime dt = new DateTime(millisFromJavaEpoch)
            .withChronology(CHRON_ETH);
        return constructUniversalDate(dt);
    }

    @Override
    protected String[] getMonthsArray() {
        Resources res = getResources();
        // load the months - will automatically get correct strings for current phone locale
        return res.getStringArray(R.array.ethiopian_months);
    }

    @Override
    protected UniversalDate incrementMonth(long millisFromJavaEpoch) {
        DateTime dt = new DateTime(millisFromJavaEpoch)
            .withChronology(CHRON_ETH)
            .plusMonths(1);
        return constructUniversalDate(dt);
    }

    @Override
    protected UniversalDate incrementYear(long millisFromJavaEpoch) {
        DateTime dt = new DateTime(millisFromJavaEpoch)
            .withChronology(CHRON_ETH)
            .plusYears(1);
        return constructUniversalDate(dt);
    }

    @Override
    protected long toMillisFromJavaEpoch(int year, int month, int day, long millisOffset) {
        DateTime dt = new DateTime(CHRON_ETH)
            .withYear(year)
            .withMonthOfYear(month)
            .withDayOfMonth(day)
            .withMillisOfDay((int) millisOffset);
        return dt.getMillis();
    }
}
