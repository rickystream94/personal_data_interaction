package com.bobbytables.phrasebook;


import android.os.Bundle;
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
        View rootView = inflater.inflate(R.layout.fragment_cards, container, false);
        //Handling cards recycler view
        cardsRecyclerView = (RecyclerView) rootView.findViewById(R.id.cardsRecyclerView);
        cardsRecyclerView.setHasFixedSize(true);
        recyclerViewLayoutManager = new LinearLayoutManager(getActivity());
        cardsRecyclerView.setLayoutManager(recyclerViewLayoutManager);
        cardsAdapter = new ChallengeCardsAdapter(getContext());
        cardsRecyclerView.setAdapter(cardsAdapter);
        return rootView;
    }
}