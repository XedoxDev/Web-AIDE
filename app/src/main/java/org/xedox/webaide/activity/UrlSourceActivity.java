package org.xedox.webaide.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.xedox.webaide.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UrlSourceActivity extends AppCompatActivity {

    private EditText inputUrl;
    private TextView outputSource;
    private Button buttonParse;
    private Button buttonCopy;

    // Gunakan ExecutorService
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url_source);

        inputUrl = findViewById(R.id.input_url);
        outputSource = findViewById(R.id.output_source);
        buttonParse = findViewById(R.id.button_parse);
        buttonCopy = findViewById(R.id.button_copy);

        outputSource.setMovementMethod(new ScrollingMovementMethod());

        buttonParse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = inputUrl.getText().toString().trim();
                if (!url.isEmpty()) {
                    fetchSource(url);
                } else {
                    Toast.makeText(UrlSourceActivity.this, "URL cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = outputSource.getText().toString();
                if (!text.isEmpty()) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("source", text);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(UrlSourceActivity.this, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UrlSourceActivity.this, "No text to copy", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchSource(String urlString) {
        executor.execute(() -> {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }
                reader.close();
            } catch (Exception e) {
                result.append("Failed to fetch source: ").append(e.getMessage());
            }

            // Update UI di thread utama
            runOnUiThread(() -> outputSource.setText(result.toString()));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown(); // Matikan executor saat activity dihancurkan
    }
}
