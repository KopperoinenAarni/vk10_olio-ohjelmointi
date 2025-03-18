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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

public class SearchActivity extends AppCompatActivity {

    private TextView statusText;
    private EditText cityEdit;
    private EditText yearEdit;

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
        String city = cityEdit.getText().toString().trim();
        String yearText = yearEdit.getText().toString().trim();

        if (city.isEmpty() || yearText.isEmpty()) {
            statusText.setText("Haku epäonnistui.");
            return;
        }

        int year;
        try {
            year = Integer.parseInt(yearText);
        } catch (NumberFormatException e) {
            statusText.setText("Haku epäonnistui.");
            return;
        }

        statusText.setText("Haetaan aluetta...");

        new Thread(() -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode areasRoot = mapper.readTree(new URL("https://pxdata.stat.fi:443/PxWeb/api/v1/fi/StatFin/mkan/statfin_mkan_pxt_11ic.px"));

                HashMap<String, String> areaCodesMap = new HashMap<>();
                JsonNode areaNames = areasRoot.get("variables").get(0).get("valueTexts");
                JsonNode areaCodes = areasRoot.get("variables").get(0).get("values");

                for (int i = 0; i < areaNames.size(); i++) {
                    areaCodesMap.put(areaNames.get(i).asText(), areaCodes.get(i).asText());
                }

                if (!areaCodesMap.containsKey(city)) {
                    runOnUiThread(() -> statusText.setText("Haku epäonnistui."));
                    return;
                }

                runOnUiThread(() -> statusText.setText("Haku epäonnistui."));

                getData(SearchActivity.this, city, year);

            } catch (Exception e) {
                runOnUiThread(() -> statusText.setText("Haku epäonnistui.: " + e.getMessage()));
            }
        }).start();
    }



    public void getData(Context context, String city, int year) {
        CarDataStorage.getInstance().clearData();

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            URL url = new URL("https://pxdata.stat.fi:443/PxWeb/api/v1/fi/StatFin/mkan/statfin_mkan_pxt_11ic.px");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            ObjectNode root = objectMapper.createObjectNode();
            ArrayNode queryArray = objectMapper.createArrayNode();

            ObjectNode ajoneuvoQuery = objectMapper.createObjectNode();
            ajoneuvoQuery.put("code", "Ajoneuvoluokka");
            ObjectNode ajoneuvoSelection = objectMapper.createObjectNode();
            ajoneuvoSelection.put("filter", "item");
            ArrayNode ajoneuvoValues = objectMapper.createArrayNode();
            ajoneuvoValues.add("01");
            ajoneuvoValues.add("02");
            ajoneuvoValues.add("03");
            ajoneuvoValues.add("04");
            ajoneuvoValues.add("05");
            ajoneuvoSelection.set("values", ajoneuvoValues);
            ajoneuvoQuery.set("selection", ajoneuvoSelection);
            queryArray.add(ajoneuvoQuery);

            ObjectNode liikenneQuery = objectMapper.createObjectNode();
            liikenneQuery.put("code", "Liikennekäyttö");
            ObjectNode liikenneSelection = objectMapper.createObjectNode();
            liikenneSelection.put("filter", "item");
            ArrayNode liikenneValues = objectMapper.createArrayNode();
            liikenneValues.add("0");
            liikenneSelection.set("values", liikenneValues);
            liikenneQuery.set("selection", liikenneSelection);
            queryArray.add(liikenneQuery);

            ObjectNode vuosiQuery = objectMapper.createObjectNode();
            vuosiQuery.put("code", "Vuosi");
            ObjectNode vuosiSelection = objectMapper.createObjectNode();
            vuosiSelection.put("filter", "item");
            ArrayNode vuosiValues = objectMapper.createArrayNode();
            vuosiValues.add(String.valueOf(year));
            vuosiSelection.set("values", vuosiValues);
            vuosiQuery.set("selection", vuosiSelection);
            queryArray.add(vuosiQuery);

            root.set("query", queryArray);
            ObjectNode responseNode = objectMapper.createObjectNode();
            responseNode.put("format", "json-stat2");
            root.set("response", responseNode);

            root.put("tableIdForQuery", "statfin_mkan_pxt_11ic.px");


            byte[] input = objectMapper.writeValueAsBytes(root);
            OutputStream os = con.getOutputStream();
            os.write(input, 0, input.length);
            os.flush();
            os.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                responseBuilder.append(line.trim());
            }
            br.close();
            con.disconnect();

            JsonNode responseJson = objectMapper.readTree(responseBuilder.toString());
            ArrayNode values = (ArrayNode) responseJson.get("value");
            JsonNode dimension = responseJson.get("dimension");
            JsonNode ajoneuvoDim = dimension.get("Ajoneuvoluokka");
            JsonNode category = ajoneuvoDim.get("category");

            JsonNode indexNode = category.get("index");
            JsonNode labelNode = category.get("label");

            Iterator<String> fieldNames = indexNode.fieldNames();
            while (fieldNames.hasNext()) {
                String code = fieldNames.next();
                int pos = indexNode.get(code).asInt();
                String carType = labelNode.get(code).asText();
                int amount = values.get(pos).asInt();
                CarDataStorage.getInstance().addCarData(new CarData(carType, amount));
            }

            CarDataStorage.getInstance().setCity(city);
            CarDataStorage.getInstance().setYear(year);

            SearchActivity.this.runOnUiThread(() -> statusText.setText("Haku onnistui"));


        } catch (Exception e) {
            e.printStackTrace();
            statusText.post(() -> statusText.setText("Haku epäonnistui: " + e.getMessage()));
        }
    }
}