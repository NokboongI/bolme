package com.foo.bolme;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;

import android.telephony.SmsManager;
import android.telephony.SmsMessage;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foo.bolme.databinding.ActivityMainBinding;
import com.foo.bolme.databinding.ActivitySpamBinding;
import com.foo.bolme.databinding.CategoryItemBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final int READ_CONTACTS_PERMISSION_CODE = 123;
    private static final int READ_SMS_PERMISSION_CODE = 100;
    private static final int SEND_SMS_PERMISSION_CODE = 456;


    private ActivityMainBinding mainBinding;
    private Set<String> buttonNames;
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messagesList;
    private List<Message> messages;
    public HashMap<String, String> contacts = new HashMap<>();;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());
        recyclerView = mainBinding.recyclerView;

        buttonNames = new HashSet<>();

        loadButtons();

        buttonNames = new HashSet<>();

        // 연락처 및 메시지 관련 권한 부여에 관한 조건문
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_PERMISSION_CODE);
        } else {
            // 이전에 생성한 category들 다시 생성
            loadButtonsFromPreferences();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, READ_SMS_PERMISSION_CODE);
        } else {
            getContactData();
            readSMS(); // 권한이 이미 허용된 경우 SMS 읽기를 시작합니다.
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSION_CODE);
        }

        if (messagesList == null) {
            messagesList = new ArrayList<>();
        }

        messageAdapter = new MessageAdapter(messagesList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);
        messages = new ArrayList<>();  // 이 부분에서 초기화
        messageAdapter = new MessageAdapter(messages, this);
        recyclerView.setAdapter(messageAdapter);
        getContactData();
        // SMS 읽어오기
        readSMS();

        mainBinding.addBtn.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                openGetPhoneNumberActivity();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_PERMISSION_CODE);
            }
        });


        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            private float startY = 0;
            private float endY = 0;
            private boolean isScrolling = true;

            private float move = 0;

            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                int action = e.getAction();

                if (action == MotionEvent.ACTION_DOWN) {
                    // 터치 시작 지점 저장
                    startY = e.getY();

                }
                if(action == MotionEvent.ACTION_UP) {
                    endY =e.getY();
                }
                move = endY-startY;
                if(move>30 || move<-30){
                    isScrolling = true;
                }else {
                    isScrolling = false;
                }
                if (isScrolling) {
                    // 스크롤 동작 중이면 아이템 선택을 막음
                    return false;
                }else {

                    View child = rv.findChildViewUnder(e.getX(), e.getY());
                    int position = rv.getChildAdapterPosition(child);

                    if (position != RecyclerView.NO_POSITION) {
                        Message selectedMessage = messages.get(position);

                        // 선택된 메시지 정보를 가져올 수 있음
                        String phoneNumber = selectedMessage.getPhoneNumber();

                        // 해당 번호 또는 연락처 정보를 사용하여 ChatActivity를 시작
                        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                        intent.putExtra("phoneNumber", phoneNumber);
                        intent.putExtra("contacts", contacts);

                        startActivity(intent);

                        return true;
                    }
                }

                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });

        ActivitySpamBinding spamBinding = ActivitySpamBinding.inflate(getLayoutInflater());
        mainBinding.spamUnderbar.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                openGetSpamMessage();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_PERMISSION_CODE);
            }

        });
    }
    private String getContactNameFromPhoneNumber(String phoneNumber) {

        String name = contacts.get(phoneNumber);
        return name; // 예시로 임의의 이름을 리턴
    }

    private void loadButtons() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> buttonNames = prefs.getStringSet("buttonNames", new HashSet<>());
        for (String name : buttonNames) {
            addNewButton(name);
        }
    }

        //연락처에서 전화번호와 해당 번호에 해당하는 이름을 받아와 hashmap 형태로 저장, 추후에 문자 나타낼 이름에 사용됨
    public void getContactData(){
        Set<String> contactIds = new HashSet<>();
        ArrayList<Contact> contactList = new ArrayList<>();
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        if (cursor != null) {
            int phoneNumberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int contactIdIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
            int phoneTypeIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);

            if (phoneNumberIndex == -1 || nameIndex == -1 || contactIdIndex == -1 || phoneTypeIndex == -1) {
                // Error handling if columns are not found
                // Log.e("ColumnError", "One or more columns not found");
                return;
            }

            while (cursor.moveToNext()) {
                String contactId = cursor.getString(contactIdIndex);

                // 이미 추가된 ID인지 확인
                if (!contactIds.contains(contactId)) {
                    int phoneType = cursor.getInt(phoneTypeIndex);

                    if (phoneType == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                        String name = cursor.getString(nameIndex);
                        String phoneNumber = cursor.getString(phoneNumberIndex);
                        contacts.put(phoneNumber,name);
                        Contact contact = new Contact(name, phoneNumber, "");

                        // 중복을 확인하여 추가
                        contactList.add(contact);

                        // 중복을 피하기 위해 ID 추가
                        contactIds.add(contactId);
                    }
                }
            }
            cursor.close();
        }
    }

    // 권한 동의 여부에 따른 응답 메시지 발현
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == READ_CONTACTS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGetPhoneNumberActivity();
            } else {
                Toast.makeText(this, "Permission denied. Cannot access contacts.", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == READ_SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS 읽기 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show();
                readSMS(); // 권한이 부여되면 SMS 읽기를 시작합니다.
            } else {
                // SMS 권한 거부 시 토스트 메시지 표시
                Toast.makeText(this, "SMS 읽기 권한이 거부되었습니다. 일부 기능이 제한될 수 있습니다.", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == SEND_SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS 보내기 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                // SMS 권한 거부 시 토스트 메시지 표시
                Toast.makeText(this, "SMS 보내기 권한이 거부되었습니다. 일부 기능이 제한될 수 있습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //카테고리 추가 버튼을 클릭했을 경우 해당 액티비티 실행을 위한 메소드
    private void openGetPhoneNumberActivity() {
        Intent intent = new Intent(this, get_phonenum.class);
        startActivityForResult(intent, 1);
    }
    private void openGetSpamMessage() {
        Intent intent = new Intent(this, Spam.class);
        startActivityForResult(intent, 1);
    }
    //카테고리 추가 액티비티에서 작성한 폴더 이름을 extra를 통해 받아와 버튼 생성
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String folderName = data.getStringExtra("folderName");
            addNewButton(folderName);
            saveButtonsToPreferences();
        }
    }


    // 추가된 함수: 새로운 카테고리를 버튼 목록에 추가하고 저장
    private void addCategoryToButtons(String name) {
        buttonNames.add(name);
        saveButtonsToPreferences(); // 새로운 카테고리가 추가될 때마다 즉시 저장
    }

    // 새롭게 카테고리를 생성하는 함수
// 새롭게 카테고리를 생성하는 함수
    @SuppressLint("ResourceAsColor")
    private void addNewButton(String name) {
        CategoryItemBinding categoryItemBinding = CategoryItemBinding.inflate(getLayoutInflater());
        categoryItemBinding.categoryName.setText(name);
        categoryItemBinding.categoryView.setTag(name);

        mainBinding.scrollLayout.addView(categoryItemBinding.categoryView);

        // 새로운 카테고리를 버튼 목록에 추가하고 저장
        addCategoryToButtons(name);
    }

    //복구할 때 버튼 생성하는 함수
    private void loadaddNewButton(String name) {
        CategoryItemBinding categoryItemBinding = CategoryItemBinding.inflate(getLayoutInflater());
        categoryItemBinding.categoryName.setText(name);
        categoryItemBinding.categoryView.setTag(name);

        mainBinding.scrollLayout.addView(categoryItemBinding.categoryView);
    }


    // 앱 종류 재 시작시에도 버튼이 계속 유지되도록 버튼에 대한 정보 저장 및 복구
    private void saveButtonsToPreferences() {
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet("ButtonNames", buttonNames);
        editor.apply();
    }

    // 복구하는 함수
    private void loadButtonsFromPreferences() {
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        buttonNames = preferences.getStringSet("ButtonNames", new HashSet<>());
        // 기존 버튼들을 모두 삭제
        mainBinding.scrollLayout.removeAllViews();

        for (String name : buttonNames) {
            loadaddNewButton(name);
        }

        // RecyclerView 및 Adapter 초기화
        if (messages == null) {
            messages = new ArrayList<>();
        } else {
            messages.clear();
        }

        messageAdapter = new MessageAdapter(messages, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);
    }
    private void readSMS() {
        // 이전에 이미 코드가 있는 경우 기존 목록 초기화
        if (messages == null) {
            messages = new ArrayList<>();
        } else {
            messages.clear();
        }

        // 메시지 읽기
        Uri uri = Uri.parse("content://sms");
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int addressIndex = cursor.getColumnIndex("address");
            int bodyIndex = cursor.getColumnIndex("body");

            do {
                if (addressIndex != -1 && bodyIndex != -1) {
                    String phoneNumber = cursor.getString(addressIndex);
                    String message = cursor.getString(bodyIndex);
                    String name = contacts.get(phoneNumber);

                    // 연락처 정보가 없는 경우 번호에 구분자 추가
                    if (name == null) {
                        name = phoneNumberWithSeparator(phoneNumber);
                    }

                    // 중복 체크 및 새 메시지 추가
                    boolean exist = false;
                    for (Message msg : messages) {
                        if (msg.getPhoneNumber() != null && msg.getPhoneNumber().equals(phoneNumber)) {
                            exist = true;
                            break;
                        }
                        if (msg.getContactName() != null && msg.getContactName().equals(name)) {
                            exist = true;
                            break;
                        }
                    }

                    if (!exist) {
                        Message newMessage = new Message(name, phoneNumber, message, Message.TYPE_RIGHT);
                        messages.add(newMessage);
                    }
                }
            } while (cursor.moveToNext());

            cursor.close();
            if (messageAdapter != null) {
                messageAdapter.notifyDataSetChanged();
            } else {
                messageAdapter = new MessageAdapter(messages, this);
                recyclerView.setAdapter(messageAdapter);
            }
        } else {
            Toast.makeText(this, "메시지를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 구분자를 추가하는 함수
    private String phoneNumberWithSeparator(String phoneNumber) {
        // 여기에 구분자 추가 로직을 넣어주세요.
        // 예: "-"를 추가하는 경우
        return phoneNumber.replaceAll("(\\d{3})(\\d{3,4})(\\d{4})", "$1-$2-$3");
    }
}