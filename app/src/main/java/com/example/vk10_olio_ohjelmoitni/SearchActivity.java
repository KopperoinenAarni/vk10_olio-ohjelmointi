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
        CarDataStorage storage = CarDataStorage.getInstance();
        storage.clearData();

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            URL url = new URL("https://pxdata.stat.fi:443/PxWeb/api/v1/fi/StatFin/mkan/statfin_mkan_pxt_11ic.px");
            JsonNode areasRoot = objectMapper.readTree(url);

            HashMap<String, String> areaCodesMap = new HashMap<>();
            JsonNode areaNames = areasRoot.get("variables").get(0).get("valueTexts");
            JsonNode areaCodesNode = areasRoot.get("variables").get(0).get("values");

            for (int i = 0; i < areaNames.size(); i++) {
                areaCodesMap.put(areaNames.get(i).asText(), areaCodesNode.get(i).asText());
            }

            if (!areaCodesMap.containsKey(city)) {
                runOnUiThread(() -> statusText.setText("Kaupunkia ei löytynyt!"));
                return;
            }

            String areaCode = areaCodesMap.get(city);


            URL postUrl = new URL("https://pxdata.stat.fi:443/PxWeb/api/v1/fi/StatFin/mkan/statfin_mkan_pxt_11ic.px");
            HttpURLConnection con = (HttpURLConnection) postUrl.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            ObjectNode root = objectMapper.createObjectNode();
            ArrayNode queryArray = objectMapper.createArrayNode();

            ObjectNode alueQuery = objectMapper.createObjectNode();
            alueQuery.put("code", "Alue");
            ObjectNode selectionAlue = objectMapper.createObjectNode();
            selectionAlue.put("filter", "item");
            ArrayNode alueValues = objectMapper.createArrayNode();
            alueValues.add(areaCodesMap.get(city));
            selectionAlue.set("values", alueValues);
            alueQuery.set("selection", selectionAlue);

            // Ajoneuvoluokka
            ObjectNode ajoneuvoQuery = objectMapper.createObjectNode();
            ajoneuvoQuery.put("code", "Ajoneuvoluokka");
            ObjectNode selectionAjoneuvo = objectMapper.createObjectNode();
            selectionAjoneuvo.put("filter", "item");
            ArrayNode ajoneuvoValues = objectMapper.createArrayNode();
            ajoneuvoValues.add("01").add("02").add("03").add("04").add("05");
            selectionAjoneuvo.set("values", ajoneuvoValues);
            ajoneuvoQuery.set("selection", selectionAjoneuvo);

            // Liikennekäyttö
            ObjectNode liikennekayttoQuery = objectMapper.createObjectNode();
            liikennekayttoQuery.put("code", "Liikennekäyttö");
            ObjectNode selectionLiikenne = objectMapper.createObjectNode();
            selectionLiikenne.put("filter", "item");
            ArrayNode liikenneValues = objectMapper.createArrayNode();
            liikenneValues.add("0");
            selectionLiikenne.set("values", liikenneValues);
            liikennekayttoQuery.set("selection", selectionLiikenne);

            ObjectNode vuosiQuery = objectMapper.createObjectNode();
            vuosiQuery.put("code", "Vuosi");
            ObjectNode selectionVuosi = objectMapper.createObjectNode();
            selectionVuosi.put("filter", "item");
            ArrayNode vuosiValues = objectMapper.createArrayNode();
            vuosiValues.add(String.valueOf(year));
            selectionVuosi.set("values", vuosiValues);
            vuosiQuery.set("selection", selectionVuosi);

            queryArray.add(alueQuery);
            queryArray.add(ajoneuvoQuery);
            queryArray.add(liikennekayttoQuery);
            queryArray.add(vuosiQuery);

            root = objectMapper.createObjectNode();
            root.set("query", queryArray);
            root.putObject("response").put("format", "json-stat2");

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

            JsonNode jsonResponse = objectMapper.readTree(responseBuilder.toString());
            ArrayNode values = (ArrayNode) jsonResponse.get("value");
            JsonNode labels = jsonResponse.get("dimension").get("Ajoneuvoluokka").get("category").get("label");
            JsonNode indices = jsonResponse.get("dimension").get("Ajoneuvoluokka").get("category").get("index");

            Iterator<String> fieldNames = indices.fieldNames();
            while (fieldNames.hasNext()) {
                String key = fieldNames.next();
                int index = indices.get(key).asInt();
                String carType = labels.get(key).asText();
                int amount = values.get(index).asInt();
                storage.addCarData(new CarData(carType, amount));
            }

            storage.setCity(city);
            storage.setYear(year);

            runOnUiThread(() -> statusText.setText("Haku onnistui!"));

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> statusText.setText("Haku epäonnistui: " + e.getMessage()));
        }
    }


}