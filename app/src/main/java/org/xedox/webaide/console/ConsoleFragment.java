package org.xedox.webaide.console;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import androidx.fragment.app.Fragment;

public class ConsoleFragment extends Fragment {

    public ConsoleView console;
    private String name;

    public ConsoleFragment(String name) {
        this.name = name;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle extraArgs) {
        console = new ConsoleView(getActivity());
        return console;
    }

    public static ConsoleFragment newInstance(String name) {
        ConsoleFragment f = new ConsoleFragment(name);
        return f;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
