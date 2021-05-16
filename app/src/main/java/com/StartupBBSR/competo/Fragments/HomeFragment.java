package com.StartupBBSR.competo.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.StartupBBSR.competo.databinding.FragmentHomeBinding;
import com.google.android.material.tabs.TabLayoutMediator;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    // tab titles
    private String[] homeTabTitles = new String[]{"Feed", "Events", "Event Pal", "Inbox"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        init();
        return view;
    }

    private void init() {
        binding.homeViewpager.setAdapter(new HomeViewPagerFragmentAdapter(this));

//        Attaching tab mediator
        new TabLayoutMediator(binding.tabLayout2,
                binding.homeViewpager, ((tab, position) ->
                tab.setText(homeTabTitles[position])
        )).attach();
    }


    private class HomeViewPagerFragmentAdapter extends FragmentStateAdapter {

        public HomeViewPagerFragmentAdapter(@NonNull HomeFragment fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new FeedFragment();
                case 1:
                    return new EventFragment();
                case 2:
                    return new EventPalFragment();
                case 3:
                    return new InboxFragment();
            }
            return new FeedFragment();
        }

        @Override
        public int getItemCount() {
//            tab size
            return homeTabTitles.length;
        }
    }
}