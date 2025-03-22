package com.example.vk10_olio_ohjelmoitni;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class SearchActivity extends AppCompatActivity {

    private TextView statusText;
    private EditText cityEdit;
    private EditText yearEdit;
    private int total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

        statusText = findViewById(R.id.StatusText);
        cityEdit = findViewById(R.id.CityNameEdit);
        yearEdit = findViewById(R.id.YearEdit);
        total = 0;
    }


    public void switchToList(View view) {
        Intent intent = new Intent(this, ListInfoActivity.class);
        startActivity(intent);
    }

    public void switchToMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void searchButton(View view) {
        String city = cityEdit.getText().toString();
        String yearText = yearEdit.getText().toString();

        int year = Integer.parseInt(yearText);
        getData(SearchActivity.this, city, year);

    }


    public void getData(Context context, String city, int year) {

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode areas = null;
        try {
            areas = objectMapper.readTree(new URL("https://pxdata.stat.fi/PxWeb/api/v1/fi/StatFin/mkan/statfin_mkan_pxt_11ic.px"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Vuosien koodaus
        ArrayList<String> yearKeys = new ArrayList<>();
        ArrayList<String> yearValues = new ArrayList<>();
        for (JsonNode node : areas.get("variables").get(3).get("values")) {
            yearValues.add(node.asText());
        }
        for (JsonNode node : areas.get("variables").get(3).get("valueTexts")) {
            yearKeys.add(node.asText());
        }
        HashMap<String, String> yearCodes = new HashMap<>();
        for(int i = 0; i < yearKeys.size(); i++) {
            yearCodes.put(yearKeys.get(i), yearValues.get(i));
        }
        String yearCode = null;
        yearCode = yearCodes.get(year);


        //Paikkakunta koodaus
        ArrayList<String> municipalityKeys = new ArrayList<>();
        ArrayList<String> municipalityValues = new ArrayList<>();

        for (JsonNode node : areas.get("variables").get(0).get("values")) {
            municipalityValues.add(node.asText());
        }
        for (JsonNode node : areas.get("variables").get(0).get("valueTexts")) {
            municipalityKeys.add(node.asText());
        }

        HashMap<String, String> municipalityCodes = new HashMap<>();

        for(int i = 0; i < municipalityKeys.size(); i++) {
            yearCodes.put(municipalityKeys.get(i), municipalityValues.get(i));
        }
        String municipalityCode = null;
        municipalityCode = yearCodes.get(year);


        try {
            URL url = new URL("https://pxdata.stat.fi:443/PxWeb/api/v1/fi/StatFin/mkan/statfin_mkan_pxt_11ic.px");

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            JsonNode jsonInputString = objectMapper.readTree(context.getResources().openRawResource(R.raw.query));

            ((ObjectNode) jsonInputString.get("query").get(0).get("selection")).putArray("values").add(code);

            byte[] input = objectMapper.writeValueAsBytes(jsonInputString);
            OutputStream os = con.getOutputStream();
            os.write(input, 0, input.length);

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
                response.append(line.trim());
            }


        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}




