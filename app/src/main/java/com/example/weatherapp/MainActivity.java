package com.example.weatherapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.weatherapp.WeatherUpdate.ApiClient;
import com.example.weatherapp.WeatherUpdate.ApiService;
import com.example.weatherapp.WeatherUpdate.CurrentWeatherResponse;
import com.example.weatherapp.WeatherUpdate.DailyForecastResponse;
import com.example.weatherapp.WeatherUpdate.ForecastAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "e6703e9a69e64c118c7165416240106";
    private TextView cityInfo, currentWeatherDescription, currentTemperature, currentFahren, currentTime, bacoor, search,city, enter_city;
    private ImageView currentIcon;
    private RecyclerView dailyForecastRecyclerView;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bacoor = findViewById(R.id.bacoor);
        currentTime = findViewById(R.id.currentTime);
        city = findViewById(R.id.city);
        enter_city = findViewById(R.id.enter_city);
        search = findViewById(R.id.search);
        cityInfo = findViewById(R.id.cityInfo);
        currentWeatherDescription = findViewById(R.id.currentWeatherDescription);
        currentTemperature = findViewById(R.id.currentTemperature);
        currentFahren = findViewById(R.id.currentFahren);
        currentIcon = findViewById(R.id.currentIcon);
        dailyForecastRecyclerView = findViewById(R.id.dailyForecastRecyclerView);
        dailyForecastRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        apiService = ApiClient.getClient().create(ApiService.class);
        fetchWeatherData(bacoor.getText().toString().trim());

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchWeatherData(enter_city.getText().toString().trim());
                city.setText(enter_city.getText().toString().trim());
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void fetchWeatherData(String city) {
        if (city.isEmpty()) {
            showToast("Please enter a city.");
            return;
        }

        apiService.getCurrentWeather(API_KEY, city).enqueue(new Callback<CurrentWeatherResponse>() {
            @Override
            public void onResponse(Call<CurrentWeatherResponse> call, Response<CurrentWeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CurrentWeatherResponse currentWeather = response.body();
                    fetchDailyForecast(currentWeather.location.lat, currentWeather.location.lon, currentWeather);
                } else {
                    showToast("Failed to fetch weather data.");
                }
            }

            @Override
            public void onFailure(Call<CurrentWeatherResponse> call, Throwable t) {
                handleNetworkFailure(t);
            }
        });
    }

    private void fetchDailyForecast(double lat, double lon, CurrentWeatherResponse currentWeather) {
        String location = String.format(Locale.getDefault(), "%.2f,%.2f", lat, lon);
        apiService.getDailyForecast(API_KEY, location, 7).enqueue(new Callback<DailyForecastResponse>() {
            @Override
            public void onResponse(Call<DailyForecastResponse> call, Response<DailyForecastResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayWeather(currentWeather, response.body().forecast.forecastday);
                } else {
                    showToast("Failed to fetch daily forecast data.");
                }
            }

            @Override
            public void onFailure(Call<DailyForecastResponse> call, Throwable t) {
                handleNetworkFailure(t);
            }
        });
    }

    private void displayWeather(CurrentWeatherResponse currentWeather, List<DailyForecastResponse.ForecastDay> dailyForecast) {
        if (this == null) {
            return;
        }

        cityInfo.setText(String.format(Locale.getDefault(), "%s, %s", currentWeather.location.name, currentWeather.location.country));
        currentWeatherDescription.setText(currentWeather.current.condition.text);
        currentTemperature.setText(String.format(Locale.getDefault(), " %.2f°C", currentWeather.current.tempC));
        currentFahren.setText(String.format(Locale.getDefault(), " %.2f°F", currentWeather.current.tempF));

        // Use currentTime field directly
        displayFormattedDateTime(currentWeather.current.last_updated);

        Glide.with(this).load("https:" + currentWeather.current.condition.icon).into(currentIcon);

        ForecastAdapter adapter = new ForecastAdapter(dailyForecast, this);
        dailyForecastRecyclerView.setAdapter(adapter);
    }

    private void displayFormattedDateTime(String dateString) {
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date date = originalFormat.parse(dateString);

            SimpleDateFormat desiredFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a", Locale.getDefault());
            String formattedDate = desiredFormat.format(date);

            // Use currentTime field directly from MainActivity
            currentTime.setText(formattedDate); // Example: "June 7, 2002 10:30 AM"
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleNetworkFailure(Throwable t) {
        showToast("Network failure: " + t.getMessage());
        t.printStackTrace();
    }

    private void showToast(String message) {
        if (this != null) {
            this.runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
        }
    }
}