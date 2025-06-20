package org.xedox.webaide.activity;

import android.os.Bundle;
import android.text.SpannableString;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import org.xedox.webaide.R;
import org.xedox.webaide.util.MarkdownManager;

public class MarkdownPreviewActivity extends BaseActivity {

    private FrameLayout content;
    private String markdownText;
    private MarkdownManager markdownManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_markdown_preview);
        
        loadToolbar();
        markdownText = getIntent().getStringExtra("text");
        content = findViewById(R.id.content);
        markdownManager = new MarkdownManager(this);
        displayMarkdownContent();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void displayMarkdownContent() {
        TextView textView = new TextView(this);
        textView.setTextSize(16);
        textView.setPadding(32, 32, 32, 32);
        
        SpannableString formattedText = markdownText != null 
            ? markdownManager.toSpannable(markdownText)
            : new SpannableString("No markdown conteng");
        
        textView.setText(formattedText);
        content.removeAllViews();
        content.addView(textView);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}