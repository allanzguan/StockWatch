package com.allanguan.stockwatch;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class StockDownloader implements Runnable {

    private static final String TAG = "StockDownloader";

    private static final String TOKEN = "pk_dafcbf7e0e8d4a32b8b0ba0a3331a7b2";
    private static final String STOCK_URL = "https://cloud.iexapis.com/stable/stock/";
    private static final String TOKEN_URL = "/quote?token=" + TOKEN;

    private MainActivity mainActivity;
    private String searchTarget;

    public StockDownloader(MainActivity mainActivity, String searchTarget){
        this.mainActivity = mainActivity;
        this.searchTarget = searchTarget;
    }


    @Override
    public void run() {
        Uri.Builder uriBuilder = Uri.parse(STOCK_URL + searchTarget + TOKEN_URL).buildUpon();
//        uriBuilder.appendQueryParameter("fullText", "true");
        String urlToUse = uriBuilder.toString();
        Log.d(TAG, "urlToUse:   " + urlToUse);

        StringBuilder sb = new StringBuilder();
        try{
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "run: HTTP ResponseCode NOT OK: " + conn.getResponseCode());
            }

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            Log.d(TAG, "run: " + sb.toString());

        } catch (Exception e){
            Log.d(TAG, "run: ", e);
            return;
        }

        process(sb.toString());
    }

    private void process(String s){
        try{
//            JSONArray jArray = new JSONArray(s);
//            JSONObject jStock = (JSONObject) jArray.get(0);

            JSONObject jStock = new JSONObject(s);

            String symbol = jStock.getString("symbol");
            String companyName = jStock.getString("companyName");
            String latestPrice = jStock.getString("latestPrice");
            if ( latestPrice == null ){
                latestPrice = "0.0";
            }

            String change = jStock.getString("change");
            if ( change == null){
                change = "0.0";
            }

            String changePercent = jStock.getString("changePercent");
            if ( changePercent == null){
                changePercent = "0.0";
            }

            final Stock stock = new Stock(symbol,
                    companyName,
                    Math.round(Double.parseDouble(latestPrice) * 100.0) / 100.0,
                    Math.round(Double.parseDouble(change) * 100.0) / 100.0,
                    Math.round(Double.parseDouble(changePercent) * 100.0 * 100.0)/100.0);

            Log.d(TAG, "calling thread");
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.addStockToList(stock);
                }
            });
            Log.d(TAG, "calling thread done");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
