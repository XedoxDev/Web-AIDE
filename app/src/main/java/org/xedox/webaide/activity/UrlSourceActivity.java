package org.xedox.webaide.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.AsyncTask;
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

public class UrlSourceActivity extends AppCompatActivity {

    EditText inputUrl;
    TextView outputSource;
    Button buttonParse;
    Button buttonCopy;

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
                    new FetchSourceTask().execute(url);
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

    private class FetchSourceTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
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
                return "Failed to fetch source: " + e.getMessage();
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            outputSource.setText(result);
        }
    }
}