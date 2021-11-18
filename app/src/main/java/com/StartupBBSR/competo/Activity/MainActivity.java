package com.StartupBBSR.competo.Activity;


import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.IntentSender;
import android.os.Bundle;
import android.os.Message;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.StartupBBSR.competo.Fragments.EventFragment;
import com.StartupBBSR.competo.Fragments.EventPalFragment;
import com.StartupBBSR.competo.Fragments.FeedFragment;
import com.StartupBBSR.competo.Fragments.InboxNewFragment;
import com.StartupBBSR.competo.Fragments.ProfileFragment;
import com.StartupBBSR.competo.Models.UserModel;
import com.StartupBBSR.competo.R;
import com.StartupBBSR.competo.Utils.Constant;
import com.StartupBBSR.competo.databinding.ActivityMainBinding;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int MY_REQUEST_CODE = 132;
    private ActivityMainBinding activityMainBinding;

    Menu menu;

    private boolean isConnected;

    private DrawerLayout drawerLayout;

    private NavigationView navigationView;
    private View header;
    private NavHostFragment navHostFragment;
    private BottomNavigationView bottomNavigationView;

    private DocumentReference documentReference;
    private FirebaseFirestore firestoreDB;
    private FirebaseAuth firebaseAuth;
    private String userid;
    private DocumentSnapshot documentSnapshot;

    private Constant constant;
    private UserModel userModel;

    private AppUpdateManager appUpdateManager;

    private static final String TAG = "test";
    private static final String testTAG = "empty";

    private Fragment fragment;
    private ProfileFragment profileFragment;

    private FeedFragment feedFragment;
    private EventFragment eventFragment;
    private InboxNewFragment inboxNewFragment;
    private EventPalFragment eventPalFragment;

    private AlarmManager alarmManager;

    private PendingIntent pendingIntent;

    private AlertDialog.Builder builder1;
    private AlertDialog.Builder builder2;


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

//        Disable night mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());

        builder1 = new AlertDialog.Builder(MainActivity.this);
        builder2 = new AlertDialog.Builder(MainActivity.this);

        FirebaseMessaging.getInstance().subscribeToTopic("Event")
                .addOnCompleteListener(task -> {
                    String msg = "Success";
                    Log.d("subscribe success", "token");
                    if (!task.isSuccessful()) {
                        msg = "Failed";
                        Log.d("subscribe failed", "token");
                    }
                });

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("token failed", "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        else
                        {
                            // Get new FCM registration token
                            String token = task.getResult();

                            // Log and toast
                            Log.d("token success", token);
                            //sendfcm(token);

                            Map<String, Object> fcmtoken = new HashMap<>();
                            fcmtoken.put("token", token);

                            firestoreDB.collection("token").document(firebaseAuth.getUid())
                                    .set(fcmtoken)
                                    .addOnSuccessListener((OnSuccessListener<Void>) aVoid -> Log.d("token uploading", "DocumentSnapshot successfully written!"))
                                    .addOnFailureListener((OnFailureListener) e -> Log.w("token uploading", "Error writing document", e));
                        }
                    }
                });


        drawerLayout = activityMainBinding.drawer;
        navigationView = activityMainBinding.navView;
        navigationView.setNavigationItemSelectedListener(this);
        header = navigationView.getHeaderView(0);


        float radius = getResources().getDimension(R.dimen.radius_10);
        MaterialShapeDrawable materialShapeDrawable = (MaterialShapeDrawable) navigationView.getBackground();
        materialShapeDrawable.setShapeAppearanceModel(materialShapeDrawable.getShapeAppearanceModel()
        .toBuilder().setTopRightCorner(CornerFamily.ROUNDED, radius)
        .setBottomRightCorner(CornerFamily.ROUNDED, radius)
        .build());

//        Bottom Navigation bar
        bottomNavigationView = activityMainBinding.bottomNavBar;

        ConnectivityManager cm =
                (ConnectivityManager)MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();

        if (!isConnected) {
            Snackbar.make(activityMainBinding.getRoot(), "No Internet", Snackbar.LENGTH_INDEFINITE)
                    .setAction("CLOSE", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    })
                    .setActionTextColor(getResources().getColor(android.R.color.holo_red_light ))
                    .show();
        }


        firestoreDB = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        userid = firebaseAuth.getUid();
        constant = new Constant();
        userModel = new UserModel();

        documentReference = firestoreDB.collection(constant.getUsers()).document(userid);
        status("Online");

        profileFragment = new ProfileFragment();
        eventPalFragment = new EventPalFragment();

        feedFragment = new FeedFragment();
        eventFragment = new EventFragment();
        inboxNewFragment = new InboxNewFragment();

        navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);


        activityMainBinding.btnTeamFinder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomNavigationView.getMenu().setGroupCheckable(0, false, true);
                bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
                loadFragment(eventPalFragment);
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            bottomNavigationView.getMenu().setGroupCheckable(0, true, true);
            bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);

            switch (item.getItemId()) {

                case R.id.feedFragment:
                    fragment = feedFragment;
                    loadFragment(fragment);
                    break;

                case R.id.eventFragment:
                    fragment = eventFragment;
                    loadFragment(fragment);
                    break;

                case R.id.inboxNewFragment:
                    fragment = inboxNewFragment;
                    loadFragment(fragment);
                    break;

                case R.id.profileFragment:
                    fragment = profileFragment;
                    loadFragment(fragment);
                    break;
            }
            return true;
        });

        activityMainBinding.drawerToggleIcon.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View view) {
                if (!drawerLayout.isDrawerOpen(Gravity.START))
                    drawerLayout.openDrawer(Gravity.START);
            }
        });

        /*Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if(extras.getString("notification").equals("chat"))
            {
                Log.d("fragment test","passed_chat");
                bottomNavigationView.setSelectedItemId(R.id.inboxNewFragment);
                loadFragment(inboxNewFragment);
            }
            else if(extras.getString("notification").equals("event"))
            {
                Log.d("fragment test","passed_event");
                onViewAllEventsClick();
            }
        }*/// TODO: 28-10-2021 abhi karna he 

    }

    private void popupSnackbarForCompleteUpdate() {
        Snackbar snackbar =
                Snackbar.make(
                        activityMainBinding.getRoot(),
                        "An update has just been downloaded.",
                        Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("RESTART", view -> appUpdateManager.completeUpdate());
        snackbar.setActionTextColor(
                getResources().getColor(R.color.ui_blue));
        snackbar.show();
    }

    public void sendfcm(String token)
    {
        Runnable runnable = () -> {
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON,"{\n" +
                    "    \"notification\":{\n" +
                    "      \"title\":\"Portugal vs. Denmark\",\n" +
                    "      \"body\":\"great match!\"\n" +
                    "    },\n" +
                    "    \"data\" : {\n" +
                    "      \"category\" : \"chat\",\n" +
                    "    },\n" +
                    "    \"to\":\"/topics/test\"\n" +
                    "}");
            Request request = new Request.Builder()
                    .url("https://fcm.googleapis.com/fcm/send")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "key=AAAABmOW__8:APA91bFEiWxr4rRQa3M_5n-w-5XDjLnQ9nf2IgAs1r0ppfwgTLZoGgOJmRAF1pt59hHqdMZ74AmAx1lkk0HaCuLwUCsHi_M_BWEZAGwkXyp-57YJk_pGmGWwJKNEU_bnJLl7bv7VDPzy")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                Log.d("response",response.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        Get Data when this activity starts
        getUserData();
        Log.d(testTAG, "onStart: ");
    }


    private void getUserData() {

//      get realtime data and store it in a class
        Log.d(testTAG, "getUserData: ");
        documentReference.addSnapshotListener(MainActivity.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                if (error != null) {
                    Log.d(TAG, "onEvent: " + error.toString());
                    return;
                }

                if (value != null && value.exists()) {
                    documentSnapshot = value;
                    saveDataToClass();
                    Log.d(TAG, "onEvent: " + value.getData());
                }
            }
        });

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NotNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot alertSnapshot = task.getResult();
                    int temp = 0;
                    if (alertSnapshot.getString(constant.getUserPhotoField()) == null || alertSnapshot.getString(constant.getUserBioField()) == null) {
                        Log.d(testTAG, "onCompleteAlert: Photo or bio null: " + alertSnapshot.getString(constant.getUserPhotoField()) + ", " + alertSnapshot.getString(constant.getUserBioField()));

                        builder1.setTitle("Tell us a bit about yourself");
                        builder1.setMessage("Let others know a bit about you\nAdd a photo and a bio to continue.\nA profile picture is necessary to make your profile visible to others.");
                        builder1.setIcon(R.drawable.ic_baseline_settings_24);
                        builder1.setCancelable(false);
                        builder1.setPositiveButton("Go to my profile", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                Intent intent = new Intent(MainActivity.this, EditProfileActivity.class).putExtra(constant.getUserModelObject(), userModel);
                                startActivity(intent);
                            }
                        }).show();

                        temp = 1;
                    }

                    if (temp != 1 && (List<String>) alertSnapshot.get(constant.getUserInterestedChipsField()) == null) {
                        Log.d(testTAG, "onCompleteAlert: Chips null: " + (List<String>) alertSnapshot.get(constant.getUserInterestedChipsField()));
                        builder2.setTitle("Add your skills");
                        builder2.setMessage("Add the skills most relevant to you\nTap on the 'Add skills' button in the profile page");
                        builder2.setIcon(R.drawable.ic_baseline_settings_24);
                        builder2.setCancelable(false);
                        builder2.setPositiveButton("Add skills", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                loadFragment(profileFragment);
                                dialogInterface.dismiss();
                            }
                        }).show();
                    }
                }
            }
        });
    }

    private void saveDataToClass() {
        getIntent().putExtra(constant.getUserModelObject(), userModel);

        userModel.setUserName(documentSnapshot.getString(constant.getUserNameField()));
        userModel.setUserEmail(documentSnapshot.getString(constant.getUserEmailField()));
        userModel.setUserPhoto(documentSnapshot.getString(constant.getUserPhotoField()));
        userModel.setUserBio(documentSnapshot.getString(constant.getUserBioField()));
        userModel.setUserLinkedin(documentSnapshot.getString(constant.getUserLinkedinField()));
        userModel.setUserPhone(documentSnapshot.getString(constant.getUserPhoneField()));
        userModel.setUserRole(documentSnapshot.getString(constant.getUserisUserField()));
        userModel.setOrganizerRole(documentSnapshot.getString(constant.getUserisOrganizerField()));
        userModel.setUserChips((List<String>) documentSnapshot.get(constant.getUserInterestedChipsField()));
        userModel.setUserID(documentSnapshot.getString(constant.getUserIdField()));

        Log.d(TAG, "saveDataToClass: " + userModel.getUserChips());

        checkRole();
        updateHeader();
    }


    private void loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, fragment)
                    .commit();
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_logOut) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("weather");
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Log Out");
            builder.setMessage("Are you sure you want to log out?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    logout();
                }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).show();
        }

        else if (id == R.id.menu_addEvent)
            startActivity(new Intent(MainActivity.this, ManageEventActivity.class));

        else if (id == R.id.menu_faq)
            startActivity(new Intent(MainActivity.this, FAQActivity.class));

        else if (id == R.id.menu_about_us)
            startActivity(new Intent(MainActivity.this, AboutUsActivity.class));

        return true;
    }

    private void logout() {
        onPause();
        FirebaseMessaging.getInstance().unsubscribeFromTopic("Event");
        FirebaseMessaging.getInstance().deleteToken();
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }

    private void checkRole() {
        menu = navigationView.getMenu();
        if (userModel.getOrganizerRole().equals("1"))
            menu.findItem(R.id.menu_addEvent).setVisible(true);
        else if (userModel.getUserRole().equals("1"))
            menu.findItem(R.id.menu_addEvent).setVisible(false);
    }

    private void updateHeader() {
        TextView tvname = header.findViewById(R.id.tvHeaderName);
        TextView tvRole = header.findViewById(R.id.tvHeaderRole);
        ImageView ivprofile = header.findViewById(R.id.header_image);
        ImageView ivprofilebackground = header.findViewById(R.id.headerBackgroundImage);

        tvname.setText(userModel.getUserName());

        if (userModel.getUserRole().equals("1"))
            tvRole.setText("User");
        else
            tvRole.setText("Organizer");

        String imguri = userModel.getUserPhoto();
        if (imguri != null) {
//            Clear image
            loadUsingGlide(imguri, ivprofile, 1, 1);
//            Blurred Background
            loadUsingGlide(imguri, ivprofilebackground, 25, 5);
        }
    }

    public void onProfileImageClick() {
        bottomNavigationView.setSelectedItemId(R.id.profileFragment);
        loadFragment(profileFragment);
    }

    public void onViewAllEventsClick() {
        bottomNavigationView.setSelectedItemId(R.id.eventFragment);
        loadFragment(eventFragment);
    }

    public void onExploreClick() {
        bottomNavigationView.getMenu().setGroupCheckable(0, false, true);
        bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        loadFragment(eventPalFragment);
    }

    public void onGoHomeOnBackPressed() {
        bottomNavigationView.setSelectedItemId(R.id.feedFragment);
        loadFragment(feedFragment);
    }


    private void loadUsingGlide(String imgurl, ImageView imageView, int radius, int sampling) {
        Glide.with(this).
                load(imgurl).
                apply(RequestOptions.bitmapTransform(new BlurTransformation(radius, sampling)))
                .into(imageView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("Offline");
        Log.d("status", "onPause: Offline");
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("Online");
        Log.d("status", "onResume: Online");

    }

    private void status(String status) {
        documentReference.update("status", status);
    }

    }
