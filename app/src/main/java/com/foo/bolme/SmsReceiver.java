package com.foo.bolme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // SMS 수신 시 호출되는 메서드

        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            // PDU(Protocol Data Unit) 객체 배열 가져오기
            Object[] pdus = (Object[]) bundle.get("pdus");

            if (pdus != null) {
                for (Object pdu : pdus) {
                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);

                    // SMS 발신자의 전화번호와 내용 가져오기
                    String sender = smsMessage.getDisplayOriginatingAddress();
                    String messageBody = smsMessage.getMessageBody();

                    // TODO: 가져온 정보를 기반으로 UI 업데이트 또는 처리 작업 수행

                }
            }
        }
    }


}
