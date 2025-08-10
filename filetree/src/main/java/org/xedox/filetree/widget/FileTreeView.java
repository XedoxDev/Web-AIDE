package org.xedox.filetree.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import org.xedox.filetree.R;
import org.xedox.filetree.utils.Node;
import java.util.List;
import static org.xedox.filetree.adapter.FileTreeAdapter.*;
import org.xedox.filetree.adapter.FileTreeAdapter;

public class FileTreeView extends RecyclerView {

    public FileTreeAdapter adapter;

    public FileTreeView(Context context) {
        super(context);
        initialize(context, null);
    }

    public FileTreeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public FileTreeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context, attrs);
    }

    private void initialize(Context context, AttributeSet attrs) {
        adapter = new FileTreeAdapter(context);
        setLayoutManager(new LinearLayoutManager(context));
        setAdapter(adapter);
    }
}
