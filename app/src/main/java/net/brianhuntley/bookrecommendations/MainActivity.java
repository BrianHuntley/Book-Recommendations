package net.brianhuntley.bookrecommendations;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


//TODO organize this whole thing
public class MainActivity extends AppCompatActivity {

    private TextView txtISBN;
    private RequestQueue queue;
    private ArrayList<Book> books;

    private void jsonParse(String url){
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("items");

                            for(int i = 0; i < 1; ++i){//TODO don't recommend the book they search, recommend books after this. i < 1 for test purposes
                                JSONObject item = jsonArray.getJSONObject(i);
                                JSONObject volumeInfo = item.getJSONObject("volumeInfo");

                                try {
                                    JSONArray industryIdentifiers = volumeInfo.getJSONArray("industryIdentifiers");
                                    JSONObject isbn13 = industryIdentifiers.getJSONObject(0);
                                    String isbn = isbn13.getString("identifier");
                                    String title = volumeInfo.getString("title");
                                    JSONArray authors = volumeInfo.getJSONArray("authors");
                                    String author = authors.getString(0);//TODO if there are multiple authors get all of them
                                    JSONObject imageLinks = volumeInfo.getJSONObject("imageLinks");
                                    String img = imageLinks.getString("thumbnail");
                                    txtISBN.setText(title);
                                    //TODO this is broken
                                    //books.add(new Book(isbn, title, author, img));

                                }catch (JSONException e){
                                    e.printStackTrace();
                                }
                            }
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtISBN = findViewById(R.id.txtISBN);
        queue = Volley.newRequestQueue(this);

        //Get permission to use camera
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA}, PackageManager.PERMISSION_GRANTED);
    }

    public void btnScan(View view){
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setBeepEnabled(false);
        intentIntegrator.setOrientationLocked(false);
        intentIntegrator.initiateScan();
    }

    public void btnSend(View view){
        EditText edtISBN = findViewById(R.id.edtISBN);
        String isbn = edtISBN.getText().toString();
        Uri uri = Uri.parse("https://www.googleapis.com/books/v1/volumes?q=" + isbn);
        jsonParse(uri.toString());
        //System.out.println(books.get(0).toString());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if(intentResult != null){
            if(intentResult.getContents() == null){
                txtISBN.setText("Scan failed");
            }else{
                String isbn = intentResult.getContents();
                Uri uri = Uri.parse("https://www.googleapis.com/books/v1/volumes?q=" + isbn);
                jsonParse(uri.toString());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}