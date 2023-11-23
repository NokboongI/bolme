package com.foo.bolme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.foo.bolme.ChatActivity;
import com.foo.bolme.Message;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messages;
    private Message message;
    private Context context;

    // 생성자 - 메시지 목록을 받아와서 어댑터에 할당
    public MessageAdapter(List<Message> messages, Context context) {
        this.messages = messages;
        this.context = context;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // viewType에 따라 다른 레이아웃을 사용
        if (context instanceof ChatActivity) {
            if (viewType == Message.TYPE_RIGHT) {
                // 사용자가 보낸 메시지인 경우
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sender_lay, parent, false);
                return new MessageViewHolder(view);
            } else {
                // 상대방이 보낸 메시지인 경우
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reciever_lay, parent, false);
                return new MessageViewHolder(view);
            }
        } else {
            // 메인 액티비티인 경우
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);
            return new MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getMessageType();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView contactNameTextView;
        private TextView messageContentTextView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            if (itemView.getContext() instanceof ChatActivity) {
                // 채팅 액티비티에서는 sender_lay와 reciever_lay에서 사용할 뷰를 찾음
                if (itemView.findViewById(R.id.text_view_left) != null) {
                    // reciever_lay
                    messageContentTextView = itemView.findViewById(R.id.text_view_left);
                    contactNameTextView = null; // 채팅 액티비티에서는 contactNameTextView를 사용하지 않음
                } else {
                    // sender_lay
                    messageContentTextView = itemView.findViewById(R.id.text_view_right);
                    contactNameTextView = null; // 채팅 액티비티에서는 contactNameTextView를 사용하지 않음
                }
            } else {
                // 메인 액티비티에서는 message_item에서 사용할 뷰를 찾음
                contactNameTextView = itemView.findViewById(R.id.contactNameTextView);
                messageContentTextView = itemView.findViewById(R.id.messageContentTextView);
            }
        }

        public void bind(Message message) {
            // ViewHolder에 데이터를 바인딩하여 화면에 보여주기
            if (contactNameTextView != null) {
                // 채팅 액티비티의 경우 contactNameTextView를 사용하지 않으므로 null 체크
                contactNameTextView.setText(message.getContactName());
            }

            messageContentTextView.setText(message.getMessageContent());
        }
    }
}
