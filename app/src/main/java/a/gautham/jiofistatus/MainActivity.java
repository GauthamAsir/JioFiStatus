package a.gautham.jiofistatus;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import a.gautham.jiofistatus.service.BackgroundService;
import a.gautham.library.AppUpdater;
import a.gautham.library.helper.Display;

public class MainActivity extends AppCompatActivity {

    private TextView textView, signal_strength_value, error_tv, upload_speed, download_speed;
    private Timer timer = new Timer();
    private ProgressBar progressBar;
    private ImageView signal_strength;

    private ProgressBar progress_circular;
    private MaterialCardView tools_layout;

    private boolean startNotification = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.battery_value);
        progressBar = findViewById(R.id.battery_progressbar);
        signal_strength = findViewById(R.id.signal_strength);
        signal_strength_value = findViewById(R.id.signal_strength_value);
        progress_circular = findViewById(R.id.progress_circular);
        tools_layout = findViewById(R.id.tools_layout);
        error_tv = findViewById(R.id.error_tv);
        upload_speed = findViewById(R.id.upload_speed);
        download_speed = findViewById(R.id.download_speed);

        progress_circular.setVisibility(View.VISIBLE);
        error_tv.setVisibility(View.GONE);
        tools_layout.setVisibility(View.GONE);

        AppUpdater appUpdater = new AppUpdater(this);
        appUpdater.setDisplay(Display.DIALOG);
        appUpdater.setUpGithub("GauthamAsir", "JioFiStatus");
        appUpdater.start();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                new GetData().execute();
            }
        }, 0, 50000);

    }

    @Override
    protected void onResume() {
        if (startNotification) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(getApplicationContext(), BackgroundService.class));
            } else {
                startService(new Intent(getApplicationContext(), BackgroundService.class));
            }
        }
        super.onResume();
    }

    @SuppressLint("StaticFieldLeak")
    class GetData extends AsyncTask<Object, Object, Object> {

        String error = null;
        String batteryLevel, network_signal, upSpeed, dwSpeed;

        @Override
        protected Object doInBackground(Object... objects) {

            try {
                Document document = Jsoup.connect("http://jiofi.local.html/").get();
                System.out.println(document);

                batteryLevel = document.getElementById("batterylevel").val();
                network_signal = document.getElementById("signalstrength").val();
                upSpeed = document.getElementById("ulCurrentDataRate").val();
                dwSpeed = document.getElementById("dlCurrentDataRate").val();

            } catch (Exception e){
                System.out.println("Error: "+e.toString());
                error = e.toString();
                return null;
            }

            return batteryLevel;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            progress_circular.setVisibility(View.GONE);

            if (o == null) {
                error_tv.setText(error);
                error_tv.setVisibility(View.VISIBLE);
                return;
            }

            tools_layout.setVisibility(View.VISIBLE);

            textView.setText(o.toString());

            if (!o.toString().equals("No Battery")) {

                startNotification = true;

                int progress = Integer.parseInt(o.toString().replace("%", ""));
                ObjectAnimator.ofInt(progressBar, "progress", progress)
                        .setDuration(300)
                        .start();

                if (progress <= 20) {
                    progressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
                } else if (progress <= 40) {
                    progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FFA500")));
                } else if (progress <= 60) {
                    progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#CCCC00")));
                } else if (progress <= 80) {
                    progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#198021")));
                } else {
                    progressBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
                }
            }

            signal_strength_value.setText(network_signal);

            switch (network_signal) {
                case "Normal":
                    signal_strength.setImageTintList(ColorStateList.valueOf(Color.BLUE));
                    break;
                case "Weak":
                case "0":
                    signal_strength.setImageTintList(ColorStateList.valueOf(Color.RED));
                    break;
                default:
                    signal_strength.setImageTintList(ColorStateList.valueOf(Color.GREEN));
                    break;
            }

            int up = Integer.parseInt(upSpeed.replace(" bps", "")) / 1000;
            int down = Integer.parseInt(dwSpeed.replace(" bps", "")) / 1000;

            if (up >= 1000) {
                up = up / 1000;
                upload_speed.setText(String.format(Locale.ENGLISH, "%s mbps", up));
            } else {
                upload_speed.setText(String.format(Locale.ENGLISH, "%s kbps", up));
            }

            if (down >= 1000) {
                down = down / 1000;
                download_speed.setText(String.format(Locale.ENGLISH, "%s mbps", down));
            } else {
                download_speed.setText(String.format(Locale.ENGLISH, "%s kbps", down));
            }

        }
    }

    @Override
    protected void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }

}