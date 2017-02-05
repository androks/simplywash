package androks.simplywash.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androks.simplywash.Models.Price;
import androks.simplywash.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PriceFragment extends Fragment {

    private Price mPrices;

    public PriceFragment() {
        // Required empty public constructor
    }

    public void setPrices(Price mPrices) {
        this.mPrices = mPrices;
    }

    public static PriceFragment newInstance(Price prices) {
        PriceFragment fragment = new PriceFragment();
        fragment.setPrices(prices);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_prices, container, false);
        view.findViewById(R.id.loading_prices).setVisibility(View.GONE);
        view.findViewById(R.id.prices).setVisibility(View.VISIBLE);
        ((TextView) view.findViewById(R.id.contact)).setText(String.valueOf(mPrices.getContact()));
        ((TextView) view.findViewById(R.id.none_contact)).setText(String.valueOf(mPrices.getNoneContact()));
        ((TextView) view.findViewById(R.id.wax)).setText(String.valueOf(mPrices.getWax()));
        return view;
    }
}
