package org.xedox.webaide.devtools;

import androidx.fragment.app.Fragment;
import org.xedox.utils.view.WebViewX;
import android.widget.ProgressBar;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.os.Bundle;
import android.view.View;

public class PreviewFragment extends Fragment {
    public WebViewX webView;
    public ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle extraArgs) {
        if (webView == null) {
            webView = new WebViewX(getActivity());
            webView.setProgressBar(progressBar);
            progressBar.setVisibility(View.GONE);
        }
        return webView;
    }

    public static PreviewFragment newInstance(ProgressBar progressBar) {
        PreviewFragment f = new PreviewFragment();
        f.progressBar = progressBar;
        return f;
    }
}
