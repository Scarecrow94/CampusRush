package com.blackviking.campusrush.Profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.FeedDetails;
import com.blackviking.campusrush.ImageController.BlurImage;
import com.blackviking.campusrush.Model.FeedModel;
import com.blackviking.campusrush.R;
import com.blackviking.campusrush.ViewHolder.FeedViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class OtherUserProfile extends AppCompatActivity {

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private String userId, currentUid;
    private ImageView userProfileImage, coverPhoto;
    private TextView username, fullName, status, department, gender, bio;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, timelineRef, likeRef, commentRef;
    private CoordinatorLayout rootLayout;
    private int BLUR_PRECENTAGE = 50;
    private RecyclerView timelineRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<FeedModel, FeedViewHolder> adapter;
    private Target target;
    private String offenceString = "";
    private String serverUsername, serverFullName, serverGender, serverStatus, serverDepartment, serverBio, serverProfilePictureThumb;

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

        setContentView(R.layout.activity_other_user_profile);


        /*---   TOOLBAR   ---*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        /*---   LOCAL   ---*/
        Paper.init(this);


        /*---   INTENT DATA   ---*/
        userId = getIntent().getStringExtra("UserId");


        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();
        userRef = db.getReference("Users");
        timelineRef = db.getReference("Feed");
        likeRef = db.getReference("Likes");
        commentRef = db.getReference("FeedComments");


        /*---   WIDGETS   ---*/
        collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing);
        coverPhoto = (ImageView)findViewById(R.id.userProfilePictureBlur);
        userProfileImage = (ImageView)findViewById(R.id.userProfilePicture);
        username = (TextView)findViewById(R.id.userUsername);
        fullName = (TextView)findViewById(R.id.userFullName);
        status = (TextView)findViewById(R.id.userStatus);
        gender = (TextView)findViewById(R.id.userGender);
        department = (TextView)findViewById(R.id.userDepartment);
        bio = (TextView)findViewById(R.id.userBio);
        timelineRecycler = (RecyclerView)findViewById(R.id.otherUserTimelineRecycler);


        /*---   TOOLBAR   ---*/
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);


        /*---   LOAD PROFILE   ---*/
        if (Common.isConnectedToInternet(getBaseContext())){

            loadUserProfile(userId);

        } else {
            Common.showErrorDialog(OtherUserProfile.this, "Could Not Load User Profile Because There Is No Internet Access !");
        }
    }

    private void loadUserTimeline(String userId) {

        /*---   TIMELINE RECYCLER   ---*/
        timelineRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        timelineRecycler.setLayoutManager(layoutManager);

        Query myTimeline = timelineRef.orderByChild("sender").equalTo(userId);

        adapter = new FirebaseRecyclerAdapter<FeedModel, FeedViewHolder>(
                FeedModel.class,
                R.layout.feed_item,
                FeedViewHolder.class,
                myTimeline
        ) {
            @Override
            protected void populateViewHolder(final FeedViewHolder viewHolder, final FeedModel model, final int position) {

                /*---   OPTIONS   ---*/
                viewHolder.options.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        /*---   POPUP MENU FOR UPDATE   ---*/
                        PopupMenu popup = new PopupMenu(OtherUserProfile.this, viewHolder.options);
                        popup.inflate(R.menu.feed_item_menu_other);
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.action_feed_other_report:

                                        openReportDialog(model.getSender());

                                        return true;
                                    case R.id.action_feed_other_share:

                                        Intent i = new Intent(android.content.Intent.ACTION_SEND);
                                        i.setType("text/plain");
                                        i.putExtra(android.content.Intent.EXTRA_SUBJECT,"Campus Rush Share");
                                        i.putExtra(android.content.Intent.EXTRA_TEXT, "Hey There, \n \nCheck Out My Latest Post On The CAMPUS RUSH App.");
                                        i.putExtra("FeedId", adapter.getRef(position).getKey());
                                        startActivity(Intent.createChooser(i,"Share via"));

                                        return true;
                                    default:
                                        return false;
                                }
                            }
                        });

                        popup.show();
                    }
                });


                /*---   POSTER DETAILS   ---*/
                if (!serverProfilePictureThumb.equals("")){

                    Picasso.with(getBaseContext())
                            .load(serverProfilePictureThumb)
                            .placeholder(R.drawable.ic_loading_animation)
                            .into(viewHolder.posterImage);

                } else {

                    viewHolder.posterImage.setImageResource(R.drawable.profile);

                }


                /*---   POST IMAGE   ---*/
                if (!model.getImageThumbUrl().equals("")){

                    viewHolder.postImage.setVisibility(View.VISIBLE);

                    Picasso.with(getBaseContext())
                            .load(model.getImageThumbUrl())
                            .placeholder(R.drawable.ic_loading_animation)
                            .into(viewHolder.postImage);

                } else {

                    viewHolder.postImage.setVisibility(View.GONE);

                }


                /*---   UPDATE   ---*/
                if (!model.getUpdate().equals("")){

                    viewHolder.postText.setVisibility(View.VISIBLE);
                    viewHolder.postText.setText(model.getUpdate());

                } else {

                    viewHolder.postText.setVisibility(View.GONE);

                }


                /*---  TIME   ---*/
                viewHolder.postTime.setText(model.getTimestamp());


                /*---   LIKES   ---*/
                final String feedId = adapter.getRef(position).getKey();
                likeRef.child(feedId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        /*---   LIKES   ---*/
                        int countLike = (int) dataSnapshot.getChildrenCount();

                        viewHolder.likeCount.setText(String.valueOf(countLike));

                        if (dataSnapshot.child(currentUid).exists()){

                            viewHolder.likeBtn.setImageResource(R.drawable.liked_icon);

                            viewHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    likeRef.child(feedId).child(currentUid).removeValue();
                                }
                            });

                        } else {

                            viewHolder.likeBtn.setImageResource(R.drawable.unliked_icon);

                            viewHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    likeRef.child(feedId).child(currentUid).setValue("liked");
                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                /*---   COMMENTS   ---*/
                commentRef.child(feedId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        int countComment = (int) dataSnapshot.getChildrenCount();

                        viewHolder.commentCount.setText(String.valueOf(countComment));

                        viewHolder.commentBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent feedDetail = new Intent(OtherUserProfile.this, FeedDetails.class);
                                feedDetail.putExtra("CurrentFeedId", adapter.getRef(position).getKey());
                                startActivity(feedDetail);
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                /*---   FEED IMAGE CLICK   ---*/
                viewHolder.postImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent feedDetail = new Intent(OtherUserProfile.this, FeedDetails.class);
                        feedDetail.putExtra("CurrentFeedId", adapter.getRef(position).getKey());
                        startActivity(feedDetail);

                    }
                });


                /*---   FEED TEXT CLICK   ---*/
                viewHolder.postText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent feedDetail = new Intent(OtherUserProfile.this, FeedDetails.class);
                        feedDetail.putExtra("CurrentFeedId", adapter.getRef(position).getKey());
                        startActivity(feedDetail);

                    }
                });


            }
        };
        timelineRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    private void openReportDialog(final String sender) {

        final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(OtherUserProfile.this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.report_form,null);

        final Spinner offenceClass = (Spinner) viewOptions.findViewById(R.id.reportTypeSpinner);
        final EditText offenceDetails = (EditText) viewOptions.findViewById(R.id.reportDetails);
        final Button submitReport = (Button) viewOptions.findViewById(R.id.submitReportBtn);
        final DatabaseReference reportRef = db.getReference("Reports");


        /*---   SETUP SPINNER   ---*/
        /*---   FILL GENDER SPINNER   ---*/
        List<String> offenceList = new ArrayList<>();
        offenceList.add(0, "Report Type");
        offenceList.add("Inappropriate Content");
        offenceList.add("Offensive Acts");
        offenceList.add("Bullying");

        ArrayAdapter<String> dataAdapterOffence;
        dataAdapterOffence = new ArrayAdapter(this, android.R.layout.simple_spinner_item, offenceList);

        dataAdapterOffence.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        offenceClass.setAdapter(dataAdapterOffence);


        /*---    GENDER SPINNER   ---*/
        offenceClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!parent.getItemAtPosition(position).equals("Report Type")){

                    offenceString = parent.getItemAtPosition(position).toString();

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        //layoutParams.x = 100; // left margin
        layoutParams.y = 200; // bottom margin
        alertDialog.getWindow().setAttributes(layoutParams);

        submitReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectedToInternet(getBaseContext())){

                    if (offenceString.equals("") || TextUtils.isEmpty(offenceDetails.getText().toString())){

                        Snackbar.make(rootLayout, "Invalid Report", Snackbar.LENGTH_SHORT).show();

                    } else {

                        final Map<String, Object> reportUserMap = new HashMap<>();
                        reportUserMap.put("reporter", currentUid);
                        reportUserMap.put("reported", sender);
                        reportUserMap.put("reportClass", offenceString);
                        reportUserMap.put("reportDetails", offenceDetails.getText().toString());

                        reportRef.push().setValue(reportUserMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Snackbar.make(rootLayout, "Snitch", Snackbar.LENGTH_SHORT).show();
                            }
                        });

                    }

                }else {

                    Snackbar.make(rootLayout, "No Internet Access !", Snackbar.LENGTH_LONG).show();
                }
                alertDialog.dismiss();

            }
        });

        alertDialog.show();

    }

    private void loadUserProfile(final String userId) {

        /*---   BLUR COVER   ---*/
        target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                coverPhoto.setImageBitmap(BlurImage.fastblur(bitmap, 1f,
                        BLUR_PRECENTAGE));
            }
            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                coverPhoto.setImageResource(R.drawable.profile);
            }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };

        userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                serverUsername = dataSnapshot.child("username").getValue().toString();
                serverFullName = dataSnapshot.child("lastName").getValue().toString() + " " + dataSnapshot.child("firstName").getValue().toString();
                serverStatus = dataSnapshot.child("status").getValue().toString();
                serverGender = dataSnapshot.child("gender").getValue().toString();
                serverDepartment = dataSnapshot.child("department").getValue().toString();
                serverBio = dataSnapshot.child("bio").getValue().toString();
                serverProfilePictureThumb = dataSnapshot.child("profilePictureThumb").getValue().toString();

                /*---   DETAILS   ---*/
                collapsingToolbarLayout.setTitle("@"+serverUsername);
                username.setText("@"+serverUsername);
                fullName.setText(serverFullName);
                status.setText(serverStatus);
                department.setText(serverDepartment);
                gender.setText(serverGender);
                bio.setText(serverBio);


                /*---   IMAGE   ---*/
                if (!serverProfilePictureThumb.equals("")){

                    /*---   PROFILE IMAGE   ---*/
                    Picasso.with(getBaseContext())
                            .load(serverProfilePictureThumb)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.ic_loading_animation)
                            .into(userProfileImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(getBaseContext())
                                            .load(serverProfilePictureThumb)
                                            .placeholder(R.drawable.ic_loading_animation)
                                            .into(userProfileImage);
                                }
                            });


                    /*---   COVER PHOTO   ---*/
                    Picasso.with(getBaseContext())
                            .load(serverProfilePictureThumb)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.ic_loading_animation)
                            .into(target);


                    userProfileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            /*Intent profileImgIntent = new Intent(MyProfile.this, ProfileImageView.class);
                            startActivity(profileImgIntent);*/
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}