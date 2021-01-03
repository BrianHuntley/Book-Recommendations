package net.brianhuntley.bookrecommendations;

import android.Manifest;
import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView txtBookInfo;
    private LinearLayout vertLayout;
    private RequestQueue queue;
    private ArrayList<Book> books;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtBookInfo = findViewById(R.id.txtBookInfo);
        vertLayout = findViewById(R.id.vertLayout);
        queue = Volley.newRequestQueue(this);
        books = new ArrayList<>();

        //Get permission to use camera
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PackageManager.PERMISSION_GRANTED);
    }

    //Starts scan search
    public void btnScan(View view) {
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setBeepEnabled(false);
        intentIntegrator.setOrientationLocked(false);
        intentIntegrator.initiateScan();
    }

    //Sends manual search
    public void btnSend(View view) {
        txtBookInfo.setText("Result:");
        EditText edtISBN = findViewById(R.id.edtISBN);
        String isbn = edtISBN.getText().toString();
        Uri uri = Uri.parse("https://www.googleapis.com/books/v1/volumes?q=" + isbn);
        jsonParse(uri.toString());
    }

    //Barcode scanner
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                txtBookInfo.setText("Scan failed, try again or enter manually");
            } else {
                txtBookInfo.setText("Result:");
                String isbn = intentResult.getContents();
                Uri uri = Uri.parse("https://www.googleapis.com/books/v1/volumes?q=" + isbn);
                jsonParse(uri.toString());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //google books api
    private void jsonParse(String url) {
        books.clear();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("items");

                            for (int i = 0; i < 10; ++i) {
                                JSONObject item = jsonArray.getJSONObject(i);
                                JSONObject volumeInfo = item.getJSONObject("volumeInfo");

                                try {
                                    JSONArray industryIdentifiers = volumeInfo.getJSONArray("industryIdentifiers");
                                    JSONObject isbn13 = industryIdentifiers.getJSONObject(0);//TODO sometimes gets isbn10 instead of isbn13?
                                    String isbn = isbn13.getString("identifier");
                                    String title = volumeInfo.getString("title");
                                    JSONArray authors = volumeInfo.getJSONArray("authors");
                                    String author = authors.getString(0);//TODO if there are multiple authors get all of them
                                    JSONObject imageLinks = volumeInfo.getJSONObject("imageLinks");
                                    String img = imageLinks.getString("thumbnail");
                                    books.add(new Book(isbn, title, author, img));

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            displayBook();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        queue.add(request);
    }

    private void displayBook(){
        for(int i = 0; i < books.size(); ++i){
            //make layout
            LinearLayout horzLayout = new LinearLayout(this);
            horzLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            horzLayout.setOrientation(LinearLayout.HORIZONTAL);
            vertLayout.addView(horzLayout);
            //book info
            TextView title = new TextView(this);
            title.setText(books.get(i).toString());
            title.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));
            title.setTextSize(20.0f);
            horzLayout.addView(title);
            //picture
            ImageView picture = new ImageView(this);
            picture.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));
            picture.setPadding(20,10,20, 10);
            horzLayout.addView(picture);
            Picasso.get().load(books.get(i).getImg()).into(picture);
        }

    }
}