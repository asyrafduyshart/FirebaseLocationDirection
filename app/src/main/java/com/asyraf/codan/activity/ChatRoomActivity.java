package com.asyraf.codan.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.asyraf.codan.R;
import com.asyraf.codan.common.Constant;
import com.asyraf.codan.object.Messages;
import com.asyraf.codan.object.User;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatRoomActivity extends AppCompatActivity {
    @BindView(R.id.lvMessage)
    ListView lvMessage;
    @BindView(R.id.edtMessage)
    EditText edtMessage;
    @BindView(R.id.btnSendMessage)
    Button btnSendMessage;

    private ArrayList<Messages> arrMessage;
    private ChatAdapter chatAdapter;
    private User receiverUser;
    private User currenUser;
    private DatabaseReference urlChatroom;
    private ChildEventListener childEventListenerMessage;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        ButterKnife.bind(this);
        arrMessage = new ArrayList<>();
        chatAdapter = new ChatAdapter(ChatRoomActivity.this, 0, arrMessage);
        lvMessage.setAdapter(chatAdapter);
        String jsonReceiverUser = getIntent().getStringExtra(Constant.KEY_SEND_USER).split("---")[0];
        String jsonCurrenUser = getIntent().getStringExtra(Constant.KEY_SEND_USER).split("---")[1];
        Gson gson = new Gson();
        receiverUser = gson.fromJson(jsonReceiverUser, User.class);
        currenUser = gson.fromJson(jsonCurrenUser, User.class);
        long createReceiverUser = Long.parseLong(receiverUser.cratedAt);
        long createCurrenUser = Long.parseLong(currenUser.cratedAt);
        String roomName;
        if (createReceiverUser > createCurrenUser) {
            roomName = String.valueOf(createReceiverUser) + String.valueOf(createCurrenUser);
        } else {
            roomName = String.valueOf(createCurrenUser) + String.valueOf(createReceiverUser);
        }

        // Write a message to the database
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("message");


        urlChatroom = database.getReference(Constant.CHILD_CHAT).child(roomName);
        childEventListenerMessage = urlChatroom.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);
                arrMessage.add(messages);
                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            urlChatroom.removeEventListener(childEventListenerMessage);
        } catch (Exception e) {
        }
    }

    @OnClick(R.id.btnSendMessage)
    public void setBtnSendMessage() {
        String ms = edtMessage.getText().toString();
        if (!ms.isEmpty()) {
            Messages messages = new Messages(currenUser.email, ms);
            urlChatroom.push().setValue(messages);
            edtMessage.setText("");
        }
    }

    public class ChatAdapter extends ArrayAdapter<Messages> {
        private Activity activity;
        private ArrayList<Messages> mArrMessage;
        @BindView(R.id.tvRecipientMessage)
        TextView tvRecipientMessage;
        @BindView(R.id.tvSenderMessage)
        TextView tvSenderMessage;

        public ChatAdapter(Activity activity, int resource, ArrayList<Messages> mArrMessage) {
            super(activity, resource, mArrMessage);
            this.activity = activity;
            this.mArrMessage = mArrMessage;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater layoutInflater = activity.getLayoutInflater();
                convertView = layoutInflater.inflate(R.layout.item_list_message, null);
            }
            ButterKnife.bind(this, convertView);
            if (mArrMessage.get(position).emailUser.equals(currenUser.email)) {
                tvSenderMessage.setText(mArrMessage.get(position).message);
                tvSenderMessage.setVisibility(View.VISIBLE);
                tvRecipientMessage.setVisibility(View.GONE);
            } else {
                tvRecipientMessage.setText(mArrMessage.get(position).message);
                tvSenderMessage.setVisibility(View.GONE);
                tvRecipientMessage.setVisibility(View.VISIBLE);
            }
            return convertView;
        }
    }
}
