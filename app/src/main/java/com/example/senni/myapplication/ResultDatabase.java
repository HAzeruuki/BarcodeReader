package com.example.senni.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.widget.Toast;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;

public class ResultDatabase extends AsyncTask<Void, Void, Void> {

    private String result = String.valueOf(R.string.result_empty);
    @SuppressLint("StaticFieldLeak")
    private Activity activity;

    private MongoCollection collection;

    private Document test;

    private String newPrice;


    public void giveResult(String text, Context context){
        try {
            result = text;
            activity = (Activity) context;
        }
        catch (Exception e){
            Toast.makeText(activity.getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
    public String getResult(){
        try{
            boolean findResult = test.getString("barcode").contains(result);
            if (findResult) {
                ((MainActivity) activity).nameTextView.setText(test.get("name").toString());
                ((MainActivity) activity).barcodeTextView.setText(test.get("barcode").toString());
                ((MainActivity) activity).priceTextView.setText(test.get("price").toString());
            }
            else {
                ((MainActivity)activity).nameTextView.setText(R.string.null_value);
                ((MainActivity)activity).barcodeTextView.setText(R.string.null_value);
                ((MainActivity)activity).priceTextView.setText(R.string.null_value);
            }

        }
        catch (Exception e){
            Toast.makeText(activity.getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    public void changePrice(String price){
        try{
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            newPrice = price;
            collection.updateOne(eq("barcode", result), new Document("$set", new Document("price", price)));
            test.put("price", newPrice);
            ((MainActivity)activity).priceTextView.setText(test.get("price").toString());

            StrictMode.enableDefaults();
        }
        catch (Exception e){
            Toast.makeText(activity.getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void searchDatabase(String scanResult){
        try{
            result = scanResult;
            getResult();
        }
        catch (Exception e){
            Toast.makeText(activity.getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected Void doInBackground(Void... voids) {
        MongoClient client = new MongoClient("hostip", 00000);
        MongoDatabase db = client.getDatabase("testserver");
        collection = db.getCollection("Product");
        FindIterable<Document> myDatabaseRecords = collection.find();
        MongoCursor<Document> iterator = myDatabaseRecords.iterator();

        while (iterator.hasNext()) {
            test = iterator.next();
        }
        return null;
    }
}
