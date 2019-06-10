package com.blackviking.campusrush.CampusRant;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.R;
import com.blackviking.campusrush.Settings.Help;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class CampusRant extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private FloatingActionButton addRantTopic;
    private RecyclerView rantTopicRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<RantTopicModel, RantTopicViewHolder> adapter;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference topicRef;
    private EditText searchRantEdt;
    private ImageView searchRantBtn;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*---   FONT MANAGEMENT   ---*/
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Wigrum-Regular.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        setContentView(R.layout.activity_campus_rant);


        /*---   FIREBASE   ---*/
        topicRef = db.getReference("RantTopics");


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        addRantTopic = (FloatingActionButton)findViewById(R.id.addRantTopic);
        rantTopicRecycler = (RecyclerView)findViewById(R.id.rantTopicRecycler);
        searchRantEdt = (EditText)findViewById(R.id.rantSearchEdt);
        searchRantBtn = (ImageView)findViewById(R.id.rantSearchBtn);
        
        
        /*---   EDIT TEXT SEARCH   ---*/
        searchRantEdt.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchRantEdt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    
                    String searchTxt = searchRantEdt.getText().toString().trim();

                    if (!searchTxt.equalsIgnoreCase("")){

                        searchForTopic(searchTxt);
                        return true;

                    } else {

                        Toast.makeText(CampusRant.this, "Cant Process Empty Query", Toast.LENGTH_SHORT).show();
                    }

                }
                return false;
            }
        });
        
        searchRantBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String searchTxt = searchRantEdt.getText().toString().trim();
                
                if (searchTxt.equalsIgnoreCase("")){
                    
                    searchForTopic(searchTxt);
                    
                } else {
                    Toast.makeText(CampusRant.this, "Cant Process Empty Query", Toast.LENGTH_SHORT).show();
                }
            }
        });


        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        activityName.setText("Rant Topics");
        helpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(CampusRant.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });


        addRantTopic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRantTopicCreator();
            }
        });


        loadRantTopics();

    }

    private void searchForTopic(String searchTxt) {

        Query searchQuery = topicRef.orderByChild("name").startAt(searchTxt).endAt(searchTxt + "\uf8ff");

        rantTopicRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        rantTopicRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<RantTopicModel, RantTopicViewHolder>(
                RantTopicModel.class,
                R.layout.rant_topic_item,
                RantTopicViewHolder.class,
                searchQuery
        ) {
            @Override
            protected void populateViewHolder(RantTopicViewHolder viewHolder, final RantTopicModel model, int position) {

                viewHolder.topicName.setText("#"+model.getName());

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent rantRoomIntent = new Intent(CampusRant.this, RantRoom.class);
                        rantRoomIntent.putExtra("RantTopic", model.getName());
                        startActivity(rantRoomIntent);
                        overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                    }
                });

            }
        };
        rantTopicRecycler.setAdapter(adapter);

    }

    private void loadRantTopics() {

        rantTopicRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        rantTopicRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<RantTopicModel, RantTopicViewHolder>(
                RantTopicModel.class,
                R.layout.rant_topic_item,
                RantTopicViewHolder.class,
                topicRef
        ) {
            @Override
            protected void populateViewHolder(RantTopicViewHolder viewHolder, final RantTopicModel model, int position) {

                viewHolder.topicName.setText("#"+model.getName());

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent rantRoomIntent = new Intent(CampusRant.this, RantRoom.class);
                        rantRoomIntent.putExtra("RantTopic", model.getName());
                        startActivity(rantRoomIntent);
                        overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                    }
                });

            }
        };
        rantTopicRecycler.setAdapter(adapter);

    }

    private void openRantTopicCreator() {

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.create_rant_topic_layout,null);

        final MaterialEditText topicName = (MaterialEditText) viewOptions.findViewById(R.id.createTopicEdt);
        final TextView errorMessage = (TextView) viewOptions.findViewById(R.id.errorMessage);
        final Button createTopicButton = (Button) viewOptions.findViewById(R.id.createTopicBtn);
        final ProgressBar createProgress = (ProgressBar) viewOptions.findViewById(R.id.checkingTopicProgress);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        createTopicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String theTopic = topicName.getText().toString().trim();

                if (!TextUtils.isEmpty(theTopic)) {

                    if (Common.isConnectedToInternet(getBaseContext())) {

                        createProgress.setVisibility(View.VISIBLE);
                        createTopicButton.setEnabled(false);
                        topicName.setEnabled(false);

                        topicRef.orderByChild("name").equalTo(theTopic).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {

                                    createProgress.setVisibility(View.GONE);
                                    createTopicButton.setEnabled(true);
                                    topicName.setEnabled(true);

                                    errorMessage.setVisibility(View.VISIBLE);
                                    errorMessage.setText("This Topic Already Exists !");

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            errorMessage.setVisibility(View.GONE);
                                        }
                                    }, 4000);

                                } else {

                                    createProgress.setVisibility(View.GONE);
                                    createTopicButton.setEnabled(false);
                                    topicName.setEnabled(false);

                                    final Map<String, Object> newTopicMap = new HashMap<>();
                                    newTopicMap.put("name", theTopic);

                                    topicRef.push().setValue(newTopicMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            alertDialog.dismiss();
                                        }
                                    });

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    } else {

                        Common.showErrorDialog(CampusRant.this, "No Internet Access !");

                    }

                } else {

                    Common.showErrorDialog(CampusRant.this, "Empty Topic Names Are Not Allowed !");

                }

            }
        });

        alertDialog.show();

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
