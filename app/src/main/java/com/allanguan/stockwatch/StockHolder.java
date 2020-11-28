package com.allanguan.stockwatch;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StockHolder extends RecyclerView.ViewHolder {

    TextView symbol;
    TextView company;
    TextView latest;
    TextView change;
    TextView percent;

    StockHolder(@NonNull View itemView) {
        super(itemView);
        symbol = itemView.findViewById(R.id.symbolView);
        company = itemView.findViewById(R.id.companyView);
        latest = itemView.findViewById(R.id.lastestPriceView);
        change = itemView.findViewById(R.id.changeView);
        percent = itemView.findViewById(R.id.precentView);
    }
}
