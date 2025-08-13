package org.xedox.webaide.devtools;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.fragment.app.Fragment;
import com.google.android.material.tabs.TabLayout;
import org.xedox.webaide.R;

public class SourceFragment extends Fragment {

    private TabLayout tabLayout;
    private ListView links;
    private View view;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_source, container, false);

            tabLayout = view.findViewById(R.id.tab_layout);
            links = view.findViewById(R.id.links);

            tabLayout.addOnTabSelectedListener(
                    new TabLayout.OnTabSelectedListener() {
                        @Override
                        public void onTabSelected(TabLayout.Tab tab) {
                            updateContent(tab.getPosition());
                        }

                        @Override
                        public void onTabUnselected(TabLayout.Tab tab) {}

                        @Override
                        public void onTabReselected(TabLayout.Tab tab) {}
                    });
        }
        return view;
    }

    private void updateContent(int tabPosition) {

        switch (tabPosition) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
        }
    }

    public static SourceFragment newInstance() {
        return new SourceFragment();
    }
}
