package org.xedox.filetree.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.xedox.filetree.R;
import org.xedox.filetree.utils.Node;
import java.util.List;

public class FileTreeView extends RecyclerView {

    public FileTreeAdapter adapter;
    private int fileItemLayoutHeight;
    private Paint linePaint;
    private int lineColor = 0xFF888888;
    public int lineWidth = 2;
    public boolean turnOnLines = false;
    public boolean childrenLines = false;

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
        setWillNotDraw(false);

        adapter = new FileTreeAdapter(context);
        setLayoutManager(new LinearLayoutManager(context));

        setAdapter(adapter);
        setHasFixedSize(false);

        addOnScrollListener(
                new OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        invalidate();
                    }
                });

        View itemView = LayoutInflater.from(context).inflate(R.layout.file_item, this, false);
        itemView.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        fileItemLayoutHeight = itemView.getMeasuredHeight();

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(lineColor);
        linePaint.setStrokeWidth(lineWidth);
        linePaint.setStyle(Paint.Style.STROKE);
        initAttributes(attrs);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (fileItemLayoutHeight == 0) {
            View itemView =
                    LayoutInflater.from(getContext()).inflate(R.layout.file_item, this, false);
            itemView.measure(widthMeasureSpec, heightMeasureSpec);
            fileItemLayoutHeight = itemView.getMeasuredHeight();
        }
    }

    public void loadPath(String path) {
        Node root = new Node(path);
        adapter.setRoot(root);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawConnectingLines(canvas);
    }

    private void drawConnectingLines(Canvas canvas) {
        List<Node> nodes = adapter.getNodes();
        if (nodes == null || nodes.isEmpty() || fileItemLayoutHeight == 0 || !turnOnLines) return;
        int scrollY = computeVerticalScrollOffset();
        int scrollX = computeHorizontalScrollOffset();

        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if (node.isFile || !node.isOpen || node.children.isEmpty()) continue;

            int indent = adapter.getIndent();
            float startX = node.level * indent + indent / 2f - scrollX;
            float startY = i * fileItemLayoutHeight + fileItemLayoutHeight / 2 - scrollY;
            float endY = 0;
            if (node.children().get(node.children.size()-1).isFile) {
                endY = startY + getChildrenCount(node) * fileItemLayoutHeight;
            } else {
                endY = startY + 1 * fileItemLayoutHeight;
            }
            canvas.drawLine(startX, startY, startX, endY, linePaint);

            float childLevel = node.level + 1;
            for (int j = i + 1; j < nodes.size(); j++) {
                Node child = nodes.get(j);
                if (child.level <= node.level) break;
                if (child.level == childLevel) {
                    float childStartX = child.level * indent - scrollX;
                    float childY = j * fileItemLayoutHeight + fileItemLayoutHeight / 2f - scrollY;
                    canvas.drawLine(startX, childY, childStartX, childY, linePaint);
                }
            }
        }
    }

    private int getChildrenCount(Node node) {
        if (!node.isOpen) return 0;
        int count = node.children.size();
        for (Node n : node.children) {
            if (n.isOpen) {
                count += getChildrenCount(n);
            }
        }
        return count;
    }

    public void setLineColor(int color) {
        this.lineColor = color;
        linePaint.setColor(color);
        invalidate();
    }

    public void setLineWidth(int width) {
        this.lineWidth = width;
        linePaint.setStrokeWidth(width);
        invalidate();
    }

    public void shutdown() {
        adapter.shutdown();
    }

    private void initAttributes(AttributeSet attrs) {
        if (attrs == null) return;
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FileTreeView);
        try {
            int indentA =
                    (int) a.getDimension(R.styleable.FileTreeView_indent, adapter.getIndent());
            int lineColorA = a.getColor(R.styleable.FileTreeView_lineColor, this.lineColor);
            int lineWidthA =
                    (int) a.getDimension(R.styleable.FileTreeView_lineWidth, this.lineWidth);
            String path = a.getString(R.styleable.FileTreeView_path);
            adapter.setIndent(indentA);
            lineColor = lineColorA;
            lineWidth = lineWidthA;
            if (path != null && !path.isBlank()) loadPath(path);
        } catch (Exception err) {
            err.printStackTrace();
        } finally {
            a.recycle();
        }
    }
}
