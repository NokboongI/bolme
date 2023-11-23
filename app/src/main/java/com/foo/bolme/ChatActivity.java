package com.foo.bolme;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;

import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText editMessage;
    private TextView name;
    private Button buttonSend;
    private List<Message> messages;
    private MessageAdapter messageAdapter;
    private String phoneNumber;
    private HashMap<String, String> contacts;
    private static final int SEND_SMS_PERMISSION_CODE = 456; // 이 줄을 추가해 주세요.



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 인텐트에서 전달된 데이터 받기
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        if (phoneNumber == null) {

            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
            finish(); // 액티비티를 종료하거나 다른 조치를 취할 수 있습니다.
            return;
        }

        contacts = (HashMap<String, String>) getIntent().getSerializableExtra("contacts");

        // RecyclerView 설정
        recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // 이 부분을 추가하여 아이템을 아래에서부터 쌓도록 설정
        recyclerView.setLayoutManager(layoutManager);
        name = findViewById(R.id.contact_name);
        editMessage = findViewById(R.id.edit_text_message);
        buttonSend = findViewById(R.id.button_send);

        // 메시지 목록 초기화
        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(messages, this);
        recyclerView.setAdapter(messageAdapter);
        SmsManager sms = SmsManager.getDefault();


        // 메시지 전송 버튼 클릭 이벤트
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        // 대화 내용을 불러오는 메서드
        loadMessages();
    }



    private void loadMessages() {
        // 이전에 이미 코드가 있는 경우 기존 목록 초기화
        if (messages == null) {
            messages = new ArrayList<>();
        } else {
            messages.clear();
        }

        // 메시지 읽기
        Uri uri = Uri.parse("content://sms");
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        if (cursor != null && cursor.moveToLast()) { // 역순으로 이동
            int addressIndex = cursor.getColumnIndex("address");
            int bodyIndex = cursor.getColumnIndex("body");
            int typeIndex = cursor.getColumnIndex("type");

            do {
                if (addressIndex != -1 && bodyIndex != -1 && typeIndex != -1) {
                    String getPhoneNumber = cursor.getString(addressIndex);
                    String message = cursor.getString(bodyIndex);
                    int messageType = cursor.getInt(typeIndex);
                    if (phoneNumber.equals(getPhoneNumber)) {
                        String contactName = contacts.get(phoneNumber);

                        // SMS 타입에 따라 메시지의 타입을 구분하여 추가
                        if (contacts.containsKey(phoneNumber)) {
                            name.setText(contactName);
                            if (messageType == 1) {
                                // 상대방이 보낸 메시지
                                messages.add(new Message(contactName, null, message, Message.TYPE_LEFT));
                            } else {
                                // 사용자가 보낸 메시지
                                messages.add(new Message(null, "나", message, Message.TYPE_RIGHT));
                            }
                        } else {
                            name.setText(phoneNumber);
                            if (messageType == 1) {
                                // 상대방이 보낸 메시지 (연락처가 없는 경우 전화번호로 표시)
                                messages.add(new Message(getPhoneNumber, null, message, Message.TYPE_LEFT));
                            } else {
                                // 사용자가 보낸 메시지
                                messages.add(new Message(null, "나", message, Message.TYPE_RIGHT));
                            }
                        }
                    }
                }
            } while (cursor.moveToPrevious()); // 역순으로 이동
        }

        // 메시지 어댑터 갱신
        messageAdapter.notifyDataSetChanged();
    }


    private void sendMessage() {
        // EditText에서 메시지 가져오기
        String messageText = editMessage.getText().toString().trim();

        if (!messageText.isEmpty()) {
            // SMS 전송 권한 확인
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용된 경우 메시지 전송 진행
                // 여기에 실제 메시지 전송 코드를 추가해야 합니다.

                // 나머지 코드는 권한 요청과 상관없이 메시지 추가 및 갱신
                Message message = new Message(null, phoneNumber, messageText, Message.TYPE_RIGHT);
                messages.add(message);
                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(phoneNumber, null, messageText, null, null);
                messageAdapter.notifyItemInserted(messages.size() - 1);
                recyclerView.scrollToPosition(messages.size() - 1); // 메시지가 추가된 위치로 스크롤
                editMessage.setText("");
            } else {
                // SMS 전송 권한이 없는 경우 권한 요청
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSION_CODE);
            }
        }
    }




}
