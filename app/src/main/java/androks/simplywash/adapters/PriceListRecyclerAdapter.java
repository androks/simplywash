package androks.simplywash.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androks.simplywash.models.Service;
import androks.simplywash.R;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by androks on 3/16/2017.
 */

public class PriceListRecyclerAdapter extends RecyclerView.Adapter<PriceListRecyclerAdapter.ViewHolder> {

    private List<Service> mValues;

    public PriceListRecyclerAdapter(List<Service> currencies) {
        mValues = currencies;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PriceListRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                  int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_price, parent, false);
        return new PriceListRecyclerAdapter.ViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    @Override
    public void onBindViewHolder(PriceListRecyclerAdapter.ViewHolder holder, int position) {
        Service price = mValues.get(position);
        holder.name.setText(price.getName());
        holder.price.setText(String.valueOf(price.getPrice()));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_name) TextView name;
        @BindView(R.id.tv_price) TextView price;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
