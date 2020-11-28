package com.allanguan.stockwatch;

import android.graphics.Color;

import java.io.Serializable;

public class Stock implements Serializable, Comparable<Stock> {

    private String symbol;
    private String companyName;
    private double latestPrice;
    private double change;
    private double changePercent;
    private int color;
    private String arrow;

    Stock(String symbol, String companyName, double latestPrice, double change, double changePercent){
        this.symbol = symbol;
        this.companyName = companyName;
        this.latestPrice = latestPrice;
        this.change = change;
        this.changePercent = changePercent;
        updateArrow();
        updateColor();
    }

    public void updateArrow(){
        if(this.change >= 0){
            this.arrow = "▲";
        }
        else{
            this.arrow = "▼";
        }
    }

    public void updateColor(){
        if(this.change >=0) {
            this.color = Color.rgb(0,255,0);
        }else{
            this.color = Color.rgb(255,0,0);
        }
    }

    public int getColor() {
        return color;
    }

    public String getArrow() {
        return arrow;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public double getLatestPrice() {
        return latestPrice;
    }

    public double getChange() {
        return change;
    }

    public double getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(double changePercent) {
        this.changePercent = changePercent;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public void setLatestPrice(double latestPrice) {
        this.latestPrice = latestPrice;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }


    @Override
    public int compareTo(Stock stock) {
        return symbol.compareTo(stock.getSymbol());
    }

    @Override
    public boolean equals(Object o){
        if(this == o){
            return true;
        }
        if (o ==null || getClass() != o.getClass()){
            return false;
        }
        Stock stock = (Stock) o;
        return symbol.equals(stock.symbol) && companyName.equals(stock.companyName);
    }
}
