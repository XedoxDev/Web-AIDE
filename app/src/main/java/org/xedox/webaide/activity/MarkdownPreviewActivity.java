package org.xedox.webaide.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import io.noties.markwon.Markwon;
import org.xedox.webaide.R;

import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;

import java.util.Arrays;

public class MarkdownPreviewActivity extends BaseActivity {
    private FrameLayout content;
    private String markdownText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_markdown_preview);
        markdownText = getIntent().getStringExtra("text");
        content = findViewById(R.id.content);
        loadToolbar();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String displayType = prefs.getString("markdown_display_type", "Markwon");
        switch (displayType) {
            case "Markwon":
                showMarkwonFragment();
                break;
            case "Flexmark":
                showFlexmarkFragment();
                break;
        }
    }

    public void showMarkwonFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, MarkwonFragment.newInstance(markdownText))
                .commit();
    }

    public void showFlexmarkFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, FlexmarkFragment.newInstance(markdownText))
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class MarkwonFragment extends Fragment {
        private Markwon markwon;

        public static MarkwonFragment newInstance(String text) {
            MarkwonFragment fragment = new MarkwonFragment();
            Bundle args = new Bundle();
            args.putString("text", text);
            fragment.setArguments(args);
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
            TextView textView = new TextView(requireActivity());
            markwon = Markwon.create(requireActivity());
            String text = getArguments() != null ? getArguments().getString("text") : "**Failed to load markdown**";
            markwon.setMarkdown(textView, text);
            return textView;
        }
    }

    public static class FlexmarkFragment extends Fragment {
        public static FlexmarkFragment newInstance(String text) {
            FlexmarkFragment fragment = new FlexmarkFragment();
            Bundle args = new Bundle();
            args.putString("text", text);
            fragment.setArguments(args);
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
            TextView textView = new TextView(requireActivity());
            String markdownText = getArguments() != null ? getArguments().getString("text") : "**Failed to load markdown**";
            textView.setText(flexmarkToSpannable(markdownText));
            return textView;
        }

        private Spannable flexmarkToSpannable(String markdown) {
            MutableDataSet options = new MutableDataSet();
            options.set(Parser.EXTENSIONS, Arrays.asList(
                    TablesExtension.create(),
                    StrikethroughExtension.create()
            ));

            Parser parser = Parser.builder(options).build();
            HtmlRenderer renderer = HtmlRenderer.builder(options).build();
            Node document = parser.parse(markdown);
            String html = renderer.render(document);

            return new SpannableString(android.text.Html.fromHtml(html, android.text.Html.FROM_HTML_MODE_LEGACY));
        }
    }
}