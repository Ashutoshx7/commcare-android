package org.commcare.views.dialogs;

/**
 * @author amstone326
 */
public interface DialogController extends AlertDialogController {
    /**
     * Should call generateProgressDialog to obtain an instance of a dialog
     * for the given taskId, and then call show on that dialog fragment
     */
    void showProgressDialog(int taskId);

    /**
     * Should dismiss the dialog fragment belonging to given taskId
     * @param taskId
     */
    void dismissProgressDialogForTask(int taskId);

    /**
     * Should dismiss the current dialog fragment
     */
    void dismissCurrentProgressDialog();

    /**
     * Return the dialog that is currently showing, or null
     * if none exists
     */
    CustomProgressDialog getCurrentProgressDialog();

    /**
     * Update the current dialog's message to the new text
     */
    void updateProgress(String newMessage, int taskId);

    /**
     * Update the current dialog's title and message to the new text
     */
    void updateProgress(String newMessage, String newTitle, int taskId);

    /**
     * Update the current dialog's progress bar
     */
    void updateProgressBar(int progress, int max, int taskId);

    void updateProgressBarVisibility(boolean visible);

    /**
     * Create an instance of CustomProgressDialog specific to the activity
     * implementing this method -- this method should be implemented lower down
     * in the activity hierarchy, in one of CommCareActivity's subclasses,
     * while the other methods can be handled entirely by CommCareActivity
     */
    CustomProgressDialog generateProgressDialog(int taskId);

    /**
     * @return the alert dialog that is currently on screen (possibly null)
     */
    AlertDialogFragment getCurrentAlertDialog();

    /**
     * If a dialog was showing when a previous instance of this activity was destroyed, this
     * will re-pop up that dialog when the activity is re-launched
     */
    void showPendingAlertDialog();

}
