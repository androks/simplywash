package androks.simplywash.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androks.simplywash.Models.Service;
import androks.simplywash.R;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by androks on 3/16/2017.
 */

public class CheckedPriceListRecyclerAdapter extends
        RecyclerView.Adapter<CheckedPriceListRecyclerAdapter.ViewHolder> {

    public interface ServiceSelect{
        void onServiceSelected(Service service);
    }

    private ServiceSelect mListener;
    private List<Service> mValues;

    public CheckedPriceListRecyclerAdapter(List<Service> currencies, ServiceSelect listener) {
        mValues = currencies;
        mListener = listener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CheckedPriceListRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                         int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_price, parent, false);
        return new CheckedPriceListRecyclerAdapter.ViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    @Override
    public void onBindViewHolder(final CheckedPriceListRecyclerAdapter.ViewHolder holder,
                                 int position) {
        final Service service = mValues.get(position);
        setItemSelected(holder, false);
        holder.name.setText(service.getName());
        holder.price.setText(String.valueOf(service.getPrice() + " UAH"));
        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                service.toggle();
                setItemSelected(holder, service.isSelected());
                mListener.onServiceSelected(service);
            }
        });
    }

    private void setItemSelected(CheckedPriceListRecyclerAdapter.ViewHolder holder, boolean value) {
        holder.name.setTextColor(value ? holder.colorAccent : holder.black);
        holder.price.setTextColor(value ? holder.colorAccent : holder.black);
        holder.item.setBackgroundColor(value ? holder.gray : holder.white);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.price)
        TextView price;
        @BindView(R.id.item)
        View item;

        @BindColor(R.color.colorAccent)
        int colorAccent;
        @BindColor(android.R.color.black)
        int black;
        @BindColor(android.R.color.white)
        int white;
        @BindColor(R.color.black_trans80)
        int gray;
    }
}
