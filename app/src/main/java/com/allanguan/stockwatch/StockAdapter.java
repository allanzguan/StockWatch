package com.allanguan.stockwatch;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;

public class StockAdapter extends RecyclerView.Adapter<StockHolder> {

    private List<Stock> stockList;
    private MainActivity mainAct;
    DecimalFormat df = new DecimalFormat("#0.00");

    StockAdapter(List<Stock> sList, MainActivity ma){
        this.stockList = sList;
        mainAct = ma;
    }


    @NonNull
    @Override
    public StockHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_list_row, parent, false);

        itemView.setOnClickListener(mainAct);
        itemView.setOnLongClickListener(mainAct);
        return new StockHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StockHolder holder, int position) {
        Stock stock = stockList.get(position);

        holder.symbol.setText(stock.getSymbol());
        holder.company.setText(stock.getCompanyName());
        holder.latest.setText(String.valueOf(df.format(stock.getLatestPrice())));
        holder.change.setText(stock.getArrow() + " " + String.valueOf(df.format(stock.getChange())));
        holder.percent.setText(" ("+ String.valueOf(df.format(stock.getChangePercent())) +"%)");

        holder.symbol.setTextColor(stock.getColor());
        holder.company.setTextColor(stock.getColor());
        holder.latest.setTextColor(stock.getColor());
        holder.change.setTextColor(stock.getColor());
        holder.percent.setTextColor(stock.getColor());

    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }
}
