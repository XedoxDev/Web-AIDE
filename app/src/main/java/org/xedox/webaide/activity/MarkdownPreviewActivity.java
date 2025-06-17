package org.xedox.webaide.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import io.noties.markwon.Markwon;
import org.xedox.webaide.R;

public class MarkdownPreviewActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_markdown_preview);
        loadToolbar();
        TextView tw = findViewById(R.id.markdown);
        Markwon markwon = Markwon.create(this);
        markwon.setMarkdown(tw, getIntent().getStringExtra("text"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem arg0) {
        if (arg0.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(arg0);
    }
}
