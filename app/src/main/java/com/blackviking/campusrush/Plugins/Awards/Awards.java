package com.blackviking.campusrush.Plugins.Awards;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.R;
import com.blackviking.campusrush.Settings.Help;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Awards extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private RelativeLayout emptyData;
    private RecyclerView awardsRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<AwardListModel, AwardListViewHolder> adapter;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference awardRef;

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

        setContentView(R.layout.activity_awards);


        /*---   FIREBASE   ---*/
        awardRef = db.getReference("Awards");


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        awardsRecycler = (RecyclerView)findViewById(R.id.awardsRecycler);
        emptyData = (RelativeLayout)findViewById(R.id.emptyMaterialLayout);


        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        activityName.setText("Awards");
        helpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(Awards.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });


        /*---   EMPTY CHECK   ---*/
        awardRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    emptyData.setVisibility(View.GONE);

                } else {

                    emptyData.setVisibility(View.VISIBLE);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        loadAwards();
    }

    private void loadAwards() {

        awardsRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(Awards.this);
        awardsRecycler.setLayoutManager(layoutManager);


        adapter = new FirebaseRecyclerAdapter<AwardListModel, AwardListViewHolder>(
                AwardListModel.class,
                R.layout.award_list_item,
                AwardListViewHolder.class,
                awardRef
        ) {
            @Override
            protected void populateViewHolder(AwardListViewHolder viewHolder, AwardListModel model, int position) {

                viewHolder.awardName.setText(model.getAwardName());
                viewHolder.awardFaculty.setText(model.getAwardFaculty());
                viewHolder.awardDepartment.setText(model.getAwardDepartment());

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent awardsIntent = new Intent(Awards.this, AwardPolls.class);
                        awardsIntent.putExtra("AwardId", adapter.getRef(position).getKey());
                        startActivity(awardsIntent);
                        overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                    }
                });

            }
        };
        awardsRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }
}
