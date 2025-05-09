package org.commcare.activities;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.commcare.CommCareApplication;
import org.commcare.dalvik.R;
import org.commcare.views.notifications.NotificationActionButtonInfo;
import org.commcare.views.notifications.NotificationMessage;

import java.text.DateFormat;
import java.util.ArrayList;

/**
 * An activity to display messages for the user about something that
 * happened which might not be easy to explain.
 *
 * @author ctsims
 */
public class MessageActivity extends CommcareListActivity {
    private ArrayList<NotificationMessage> messages;

    private static final String KEY_MESSAGES = "ma_key_messages";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_MESSAGES)) {
            messages = savedInstanceState.getParcelableArrayList(KEY_MESSAGES);
        } else {
            messages = CommCareApplication.notificationManager().purgeNotifications();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(KEY_MESSAGES, messages);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.setListAdapter(new ArrayAdapter<NotificationMessage>(this, R.layout.layout_note_msg, messages) {
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View messageView = convertView;
                if (convertView == null) {
                    messageView = LayoutInflater.from(MessageActivity.this).inflate(R.layout.layout_note_msg, parent, false);
                }
                NotificationMessage msg = this.getItem(position);
                TextView title = messageView.findViewById(R.id.layout_note_msg_title);
                TextView body = messageView.findViewById(R.id.layout_note_msg_body);
                TextView date = messageView.findViewById(R.id.layout_note_msg_date);
                TextView action = messageView.findViewById(R.id.layout_note_msg_action);
                Button actionButton = messageView.findViewById(R.id.layout_note_msg_action_button);

                title.setText(msg.getTitle());
                body.setText(msg.getDetails());
                date.setText(DateUtils.formatSameDayTime(msg.getDate().getTime(), System.currentTimeMillis(), DateFormat.DEFAULT, DateFormat.DEFAULT));

                String actionText = msg.getAction();
                if (actionText == null) {
                    action.setVisibility(View.GONE);
                } else {
                    action.setText(actionText);
                }

                if(msg.getButtonInfo() != null) {
                    actionButton.setVisibility(View.VISIBLE);
                    actionButton.setText(msg.getButtonInfo().getButtonText());

                    actionButton.setOnClickListener(v -> {
                        switch(msg.getButtonInfo().getButtonAction()) {
                            case LAUNCH_DATE_SETTINGS -> SettingsHelper.launchDateSettings(getContext());
                            //Future actions will be added here once implemented
                            default -> throw new RuntimeException("Unhandled button action: " + msg.getButtonInfo().getButtonAction());
                        }
                    });
                }

                return messageView;
            }

            @Override
            public boolean isEnabled(int position) {
                return false;
            }

        });
    }
}
