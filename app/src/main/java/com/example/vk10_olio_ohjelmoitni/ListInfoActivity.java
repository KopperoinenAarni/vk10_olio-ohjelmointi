package com.example.vk10_olio_ohjelmoitni;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ListInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView cityText = findViewById(R.id.CityText);
        TextView yearText = findViewById(R.id.YearText);
        TextView carInfoText = findViewById(R.id.CarInfoText);

        CarDataStorage storage = CarDataStorage.getInstance();

        cityText.setText(storage.getCity());
        yearText.setText(String.valueOf(storage.getYear()));

        StringBuilder infoBuilder = new StringBuilder();
        int total = 0;
        for (CarData car : storage.getCarDataList()) {
            infoBuilder.append(car.getType())
                    .append(": ")
                    .append(car.getAmount())
                    .append("\n");
            total += car.getAmount();
        }
        infoBuilder.append("Yhteensä: ").append(total);
        carInfoText.setText(infoBuilder.toString());
    }
}
