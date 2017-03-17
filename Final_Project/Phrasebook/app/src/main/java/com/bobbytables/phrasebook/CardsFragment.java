package com.bobbytables.phrasebook;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CardsFragment extends Fragment {

    private RecyclerView cardsRecyclerView;
    private RecyclerView.Adapter cardsAdapter;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(R.layout.page_fragment, container, false);
        //Handling cards recycler view
        cardsRecyclerView = (RecyclerView) rootView.findViewById(R.id.cardsRecyclerView);
        cardsRecyclerView.setHasFixedSize(true);
        recyclerViewLayoutManager = new LinearLayoutManager(getActivity());
        cardsRecyclerView.setLayoutManager(recyclerViewLayoutManager);
        cardsAdapter = new ChallengeCardsAdapter();
        cardsRecyclerView.setAdapter(cardsAdapter);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
}