package org.xedox.utils;

import androidx.fragment.app.Fragment;

public abstract class BaseFragment extends Fragment {

    protected OnTitleChangedListener onTitleChanged;

    public String getTitle() {
        return "FragmentTitle";
    }
    
    public void changeTitle(String title) {
        if (onTitleChanged != null) {
            onTitleChanged.onTitleChanged(title);
        }
    }
    
    public void changeTitle() {
        if (onTitleChanged != null) {
            onTitleChanged.onTitleChanged(getTitle());
        }
    }

    public interface OnTitleChangedListener {
        void onTitleChanged(String newTitle);
    }

    public OnTitleChangedListener getOnTitleChanged() {
        return this.onTitleChanged;
    }

    public void setOnTitleChanged(OnTitleChangedListener onTitleChanged) {
        this.onTitleChanged = onTitleChanged;
        changeTitle(getTitle());
    }
}