package org.xedox.webaide.devtools;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.tabs.TabLayout;
import org.xedox.utils.ListFragment;
import org.xedox.utils.TextFragment;
import org.xedox.webaide.R;

public class SourceFragment extends Fragment implements TabLayout.OnTabSelectedListener {

    private static final String KEY_HTML = "html_content";
    private static final String TAB_HTML = "HTML";
    private static final String TAB_CSS = "CSS";
    private static final String TAB_JS = "JavaScript";

    private TabLayout tabLayout;
    private String html;
    private WebManager webManager = new WebManager();

    private ListFragment<String> scriptsFragment;
    private ListFragment<String> stylesFragment;
    private TextFragment htmlFragment;

    private boolean initedFragments = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            html = savedInstanceState.getString(KEY_HTML);
        }
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_source, container, false);
        tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.addOnTabSelectedListener(this);

        FragmentManager fm = getChildFragmentManager();
        if (!initedFragments) {
            initFragments(fm);
            initedFragments = true;
        }

        return view;
    }

    private void initFragments(FragmentManager fm) {
        scriptsFragment = ListFragment.newInstance();
        stylesFragment = ListFragment.newInstance();
        htmlFragment = TextFragment.newInstance("source.html");

        fm.beginTransaction()
                .add(R.id.source_content, htmlFragment)
                .add(R.id.source_content, stylesFragment)
                .add(R.id.source_content, scriptsFragment)
                .commit();

        fm.beginTransaction().hide(stylesFragment).hide(scriptsFragment).commit();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_HTML, html);
    }

    public void updateHtml(String newHtml) {
        this.html = newHtml;
        if (htmlFragment != null && htmlFragment.getEditor() != null) {
            htmlFragment.getEditor().setText(html);
        }
    }

    public static SourceFragment newInstance() {
        return new SourceFragment();
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        String tabText = tab.getText().toString();
        FragmentManager fm = getChildFragmentManager();

        switch (tabText) {
            case TAB_HTML:
                showFragment(fm, htmlFragment);
                break;
            case TAB_CSS:
                showFragment(fm, stylesFragment);
                break;
            case TAB_JS:
                showFragment(fm, scriptsFragment);
                break;
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {}

    @Override
    public void onTabReselected(TabLayout.Tab tab) {}

    private void showFragment(FragmentManager fm, Fragment fragment) {
        if (isAdded() && getActivity() != null) {
            fm.beginTransaction()
                    .hide(fragment == htmlFragment ? stylesFragment : htmlFragment)
                    .hide(
                            fragment == htmlFragment
                                    ? scriptsFragment
                                    : (fragment == stylesFragment
                                            ? scriptsFragment
                                            : stylesFragment))
                    .show(fragment)
                    .commit();
        }
    }
}
