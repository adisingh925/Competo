package com.StartupBBSR.competo.Fragments;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.StartupBBSR.competo.Activity.MainActivity;
import com.StartupBBSR.competo.Activity.ManageEventActivity;
import com.StartupBBSR.competo.Adapters.EventFragmentAdapter;
import com.StartupBBSR.competo.Models.EventModel;
import com.StartupBBSR.competo.R;
import com.StartupBBSR.competo.Utils.Constant;
import com.StartupBBSR.competo.databinding.FragmentEventMainBinding;
import com.airbnb.lottie.model.content.ShapeStroke;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;
import java.util.List;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static androidx.core.content.ContextCompat.getSystemService;

public class EventMainFragment extends Fragment implements EventFilterBottomSheetDialog.BottomSheetListener {

    private FragmentEventMainBinding binding;
    private EventFragmentAdapter adapter;
    private FirebaseFirestore firestoreDB;

    private NavController navController;

    private Constant constant;
    private CollectionReference collectionReference;
    private FirestoreRecyclerOptions<EventModel> options;

    private EventFilterBottomSheetDialog bottomSheetDialog;
    public static final String TAG = "filter";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
//                ((MainActivity) getActivity()).onGoHomeOnBackPressed();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventMainBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        firestoreDB = FirebaseFirestore.getInstance();

        constant = new Constant();
        collectionReference = firestoreDB.collection(constant.getEvents());

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                return false;
            }
        });

        binding.AddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (getContext(), ManageEventActivity.class);
                startActivity(intent);
            }
        });



        bottomSheetDialog = new EventFilterBottomSheetDialog(getContext());
        bottomSheetDialog.setTargetFragment(this, 0);


        binding.btnEventFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.show(getParentFragmentManager().beginTransaction(), "eventFilterSheet");
            }
        });

        return view;
    }

    private void search(String newText) {
        Query eventSearchQuery = collectionReference
                .orderBy("eventTitle")
                .whereGreaterThanOrEqualTo("eventTitle", newText);

        options = new FirestoreRecyclerOptions.Builder<EventModel>()
                .setQuery(eventSearchQuery, EventModel.class)
                .build();

        initRecycler();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initData();
        initRecycler();

        navController = Navigation.findNavController(view);

        binding.eventRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && binding.AddEvent.getVisibility() == View.VISIBLE) {
                    binding.AddEvent.hide();
                } else if (dy < 0 && binding.AddEvent.getVisibility() != View.VISIBLE) {
                    binding.AddEvent.show();
                }
            }
        });
    }

    private void initRecycler() {
        binding.eventRecyclerView.setHasFixedSize(true);
        adapter = new EventFragmentAdapter(getContext(), options);

        adapter.setOnItemClickListener(new EventFragmentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot snapshot) {
                EventModel model = snapshot.toObject(EventModel.class);

                Bundle bundle = new Bundle();
                bundle.putSerializable("eventDetails", model);
                bundle.putString("from", "event");
                navController.navigate(R.id.action_eventMainFragment_to_eventDetailsFragment, bundle);
            }
        });

        binding.eventRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }


    private void initData() {
        Query query = collectionReference.orderBy("eventDateStamp")
                .whereGreaterThanOrEqualTo("eventDateStamp", new Date().getTime());

        options = new FirestoreRecyclerOptions.Builder<EventModel>()
                .setQuery(query, EventModel.class)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    @Override
    public void onApplyButtonClicked(List<String> selectedFilters) {
        Log.d(TAG, "onApplyButtonClicked: " + selectedFilters);

        if (selectedFilters.size() != 0) {
            Query eventFilterQuery = collectionReference.whereArrayContainsAny("eventTags", selectedFilters);
            options = new FirestoreRecyclerOptions.Builder<EventModel>()
                    .setQuery(eventFilterQuery, EventModel.class)
                    .build();

            initRecycler();
        } else {
            initData();
            initRecycler();
        }

    }
}
