package com.foo.bolme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foo.bolme.databinding.ActivityMainBinding;
import com.foo.bolme.databinding.MakeCategoryBinding;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class get_phonenum extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ContactAdapter contactAdapter;
    String foldername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MakeCategoryBinding binding = MakeCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        recyclerView = binding.recyclerViewContacts;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadContacts();

        binding.categoryAddBtn.setOnClickListener(v -> {
            if(binding.folderName.getText().toString().equals("")){
                showToast("폴더 이름을 지정해주세요.");
            }else {
                foldername = binding.folderName.getText().toString();
                saveButtonName(foldername);

                Intent intent = new Intent();
                intent.putExtra("folderName", foldername);
                setResult(RESULT_OK, intent);
                finish();
            }

        });
    }
    private void saveButtonName(String name) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> buttonNames = new HashSet<>(prefs.getStringSet("buttonNames", new HashSet<>()));
        buttonNames.add(name);
        prefs.edit().putStringSet("buttonNames", buttonNames).apply();
        /*ArrayList<Contact> selectedContacts = new ArrayList<>();
        for (Contact contact : contactAdapter.getContacts()) {
            if (contact.isSelected()) {
                selectedContacts.add(contact);
            }
        }*/
    }

    private void loadContacts() {
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

        // RecyclerView 설정
        contactAdapter = new ContactAdapter(contactList);
        recyclerView.setAdapter(contactAdapter);
    }
    private void addNewButton(String name) {
        // 동적으로 버튼을 추가할 레이아웃


        // 새로운 버튼 생성
        Button newButton = new Button(this);
        newButton.setText(name); // 여기에 입력한 텍스트를 넣으실 수 있습니다.
        newButton.setTag(name);
        ActivityMainBinding mainactive = ActivityMainBinding.inflate(getLayoutInflater());
        mainactive.scrollLayout.addView(newButton);
    }

    // 고유한 ID 생성하는 메서드
    public void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


}
