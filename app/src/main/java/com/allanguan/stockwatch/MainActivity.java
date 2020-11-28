package com.allanguan.stockwatch;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, Serializable, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private Stock s;
    private StockAdapter sAdapter;
    private final List<Stock> stockList = new ArrayList<>();
    private SwipeRefreshLayout swiper;
    private String choice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler);
        sAdapter = new StockAdapter(stockList, this);
        recyclerView.setAdapter(sAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        swiper = findViewById(R.id.swiper);
        swiper.setOnRefreshListener(this);



        stockList.clear();
        loadFile();

//        stockList.add(new Stock("a", "b", -1.0, -2.0, 3.0));
//        for(int i=0; i<15; i++){
//            stockList.add(new Stock("a " + String.valueOf(i), "b", 1.0, 2.0, 3.0));
//        }

        if (!checkNetworkConnection()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Network Connection");
            builder.setMessage("Content Cannot Be Added Without A Network Connection");
            AlertDialog dialog = builder.create();
            dialog.show();
            if(!stockList.isEmpty()){
                for(Stock s : stockList){
                    s.setChange(0.0);
                    s.setChangePercent(0.0);
                    s.setLatestPrice(0.0);
                    s.updateColor();
                    s.updateArrow();
                }
            }

            sAdapter.notifyDataSetChanged();

            return;
        }

        onRefresh();
//        SymbolNameDownloader rd = new SymbolNameDownloader();
//        new Thread(rd).start();

    }


    @Override
    protected void onPause(){
        saveFile();
        super.onPause();
    }

    @Override
    public void onClick(View view) {
//        Toast.makeText(this, "ONCLICK CLICKED", Toast.LENGTH_SHORT).show();
        final int pos = recyclerView.getChildLayoutPosition(view);
        String url = "https://www.marketwatch.com/investing/stock/"+ stockList.get(pos).getSymbol();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @Override
    public boolean onLongClick(View view) {
        final int pos = recyclerView.getChildLayoutPosition(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.delete);
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                stockList.remove(pos);

                sAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        builder.setMessage("Delete Stock Symbol " + stockList.get(pos).getSymbol() + "?");
        builder.setTitle("Delete Stock");

        AlertDialog dialog = builder.create();
        dialog.show();
        return true;
    }

    @Override
    public void onRefresh() {
        if (!checkNetworkConnection()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Network Connection");
            builder.setMessage("Content Cannot Be Added Without A Network Connection");
            AlertDialog dialog = builder.create();
            dialog.show();
            swiper.setRefreshing(false);
            return;
        }

        List<Stock> tempList = new ArrayList<>(stockList);
        stockList.clear();

        for ( int i = 0; i < tempList.size(); i++ ){
            StockDownloader stockDownloader = new StockDownloader(this, tempList.get(i).getSymbol());
            new Thread(stockDownloader).start();
        }

        sAdapter.notifyDataSetChanged();

//        Toast.makeText(this, "Stocks Refreshed", Toast.LENGTH_SHORT).show();
        swiper.setRefreshing(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_stock, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.addStock){
            makeStockDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void makeStockDialog(){
        if (!checkNetworkConnection()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Network Connection");
            builder.setMessage("Content Cannot Be Added Without A Network Connection");
            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        et.setGravity(Gravity.CENTER_HORIZONTAL);
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

        builder.setView(et);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                choice = et.getText().toString().trim();


                final ArrayList<String> results = SymbolNameDownloader.findMatches(choice);

                Log.d(TAG, "makeStockDialog: results " + results.isEmpty());

                if (results.size() == 0) {
                    doNoAnswer(choice);
                } else if (results.size() == 1) {
                    doSelection(results.get(0));
                } else {
                    String[] array = results.toArray(new String[0]);

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Make a selection");
                    builder.setItems(array, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String symbol = results.get(which);
                            doSelection(symbol);
                        }
                    });
                    builder.setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
                    AlertDialog dialog2 = builder.create();
                    dialog2.show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        builder.setMessage("Please enter a Stock symbol:");
        builder.setTitle("Stock Selection");

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void doNoAnswer(String symbol) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("No data for specified symbol/name");
        builder.setTitle("Symbol Not Found: " + symbol);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void doSelection(String sym) {
        String[] data = sym.split("-");
//        Log.d(TAG, "doSelectiondoSelectiondoSelection: " +sym);
//        Log.d(TAG, "doSelectiondoSelectiondoSelection: " + data[0].trim());
        StockDownloader stockDownloader = new StockDownloader(this, data[0].trim());
        new Thread(stockDownloader).start();
    }


    private boolean checkNetworkConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        Log.d(TAG, "checkNetworkConnection: "+ SymbolNameDownloader.getRan());
        if( SymbolNameDownloader.getRan()){
            SymbolNameDownloader rd = new SymbolNameDownloader();
            new Thread(rd).start();
        }
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    private void badDataAlert(String sym){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("No data for selection");
        builder.setTitle("Symbol Not Found: " + sym);

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public void addStockToList(Stock stock){
//        Log.d(TAG, "addStock: ----------------------------------------");
        if(stock == null){
            badDataAlert(choice);
            return;
        }

        if(stockList.contains(stock)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage("Stock Symbol " + stock.getSymbol() + " is already displayed");
            builder.setTitle("Duplicate Stock");
            builder.setIcon(R.drawable.error);

            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }

        stockList.add(stock);
        Collections.sort(stockList);
        sAdapter.notifyDataSetChanged();
    }


    private void saveFile(){
        try{
            FileOutputStream fos = getApplicationContext().
                    openFileOutput(getString(R.string.file_name), Context.MODE_PRIVATE);

            JsonWriter writer = new JsonWriter(new OutputStreamWriter(fos, getString(R.string.encoding)));
            writer.setIndent("  ");
            writer.beginArray();
            for (Stock i : stockList){
                writer.beginObject();
                writer.name("symbol").value(i.getSymbol());
                writer.name("companyName").value(i.getCompanyName());
                writer.name("latestPrice").value(i.getLatestPrice());
                writer.name("change").value(i.getChange());
                writer.name("changePercent").value(i.getChangePercent());
                writer.endObject();
            }
            writer.endArray();
            writer.close();

        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    private void loadFile(){
        try{
            FileInputStream fis = getApplicationContext().openFileInput("Stocks.json");
            byte[] data = new byte[(int) fis.available()];
            int loaded = fis.read(data);

            fis.close();
            String json = new String(data);

            JSONArray stockArr = new JSONArray(json);
            for (int i = 0; i < stockArr.length(); i++) {
                JSONObject sObj = stockArr.getJSONObject(i);
                Stock s = new Stock(sObj.getString("symbol"), sObj.getString("companyName"),
                        Double.parseDouble(sObj.getString("latestPrice")),
                        Double.parseDouble(sObj.getString("change")),
                        Double.parseDouble(sObj.getString("changePercent")));
                stockList.add(s);
            }
        }  catch (Exception e) {
            e.printStackTrace();
        }

    }



}