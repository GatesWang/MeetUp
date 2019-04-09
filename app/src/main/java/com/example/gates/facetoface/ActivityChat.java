package com.example.gates.facetoface;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;

public class ActivityChat extends AppCompatActivity {

    private User person;
    private String chatName;
    private Uri uri;
    private AdapterMessage messagesAdapter;
    private ArrayList<ChatMessage> messagesArrayList;
    private ListView messagesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messagesArrayList = new ArrayList<ChatMessage>();
        messagesListView = (ListView) findViewById(R.id.list_of_messages);
        messagesListView.setAdapter(messagesAdapter);

        getUserInfo();
        getSupportActionBar().setTitle(chatName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        displayChatMessages();

        FloatingActionButton fab =
                (FloatingActionButton)findViewById(R.id.new_message);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText)findViewById(R.id.message_input);
                String inputText = input.getText().toString();

                if(inputText.trim().equals("")){
                    Toast.makeText(ActivityChat.this, "Input must contain text", Toast.LENGTH_SHORT).show();
                }
                else{
                    //now each chat message has a picture
                    ChatMessage chatMessage = new ChatMessage(inputText, person.getName(), person.getId());
                    FirebaseDatabase.getInstance()
                            .getReference()
                            .child("messages")
                            .child(chatName)
                            .push()
                            .setValue(chatMessage);

                    // Clear the input
                    input.setText("");
                    displayChatMessages();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    private void signOut() {
        AuthUI.getInstance().signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(ActivityChat.this,
                                "You have been signed out.",
                                Toast.LENGTH_LONG)
                                .show();

                        // Close activity
                        Intent login = new Intent(ActivityChat.this, ActivitySignIn.class);
                        login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(login);
                        finish();
                    }
                });

    }

    private void displayChatMessages() {
        //only display last 20 messages
        messagesArrayList.clear();
        //get messages of this chat
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("messages").child(chatName);
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    ArrayList<String> temp = new ArrayList<>();
                    for(DataSnapshot d : childSnap.getChildren()){
                        temp.add(d.getValue().toString());
                    }
                    String text = temp.get(0);
                    long time = Long.parseLong(temp.get(1));
                    String user = temp.get(2);
                    String id = temp.get(3);

                    ChatMessage chatMessage = new ChatMessage(text, user, id, time);
                    messagesArrayList.add(chatMessage);
                }
                messagesAdapter = new AdapterMessage(messagesArrayList, ActivityChat.this);
                messagesListView.setAdapter(messagesAdapter);
            }
            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_sign_out:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void goToEventCalendar(){

    }

    private void getUserInfo(){
        Intent intent = getIntent();
        person = (User) intent.getSerializableExtra("person");
        chatName = (String) intent.getStringExtra("chat");
    }

}
