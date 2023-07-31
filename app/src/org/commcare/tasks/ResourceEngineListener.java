package org.commcare.tasks;

import org.commcare.engine.resource.AppInstallStatus;
import org.commcare.resources.model.InvalidResourceException;
import org.commcare.resources.model.UnresolvedResourceException;
import org.commcare.views.notifications.NotificationActionButtonInfo;
import org.javarosa.core.reference.InvalidReferenceException;

public interface ResourceEngineListener {
    void reportSuccess(boolean b);

    void failMissingResource(UnresolvedResourceException ure, AppInstallStatus statusmissing);

    void failInvalidResource(InvalidResourceException e, AppInstallStatus statusmissing);

    void failInvalidReference(InvalidReferenceException e, AppInstallStatus status);

    void failBadReqs(String vReq, String vAvail, boolean majorIsProblem);

    void failUnknown(AppInstallStatus statusfailunknown);

    void updateResourceProgress(int done, int pending, int phase);

    void failWithNotification(AppInstallStatus statusfailstate);
    void failWithNotification(AppInstallStatus statusfailstate, NotificationActionButtonInfo.ButtonAction buttonAction);

    void failTargetMismatch();
}
