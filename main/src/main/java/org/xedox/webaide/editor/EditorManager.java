package org.xedox.webaide.editor;

import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.event.SubscriptionReceipt;
import org.xedox.filetree.utils.Node;
import org.xedox.utils.BaseFragment;
import org.xedox.utils.OverflowMenu;
import org.xedox.utils.dialog.ErrorDialog;
import org.xedox.webaide.EditorActivity;
import org.xedox.webaide.R;

public class EditorManager {

    private EditorActivity context;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private TabLayoutMediator tabLayoutMediator;
    private EditorStateAdapter editorStateAdapter;
    private ImageButton undo, redo;
    private View emptyPager;
    private SubscriptionReceipt contentChangeEvent;
    private Handler handler = new Handler(Looper.getMainLooper());

    public EditorManager(EditorActivity context) {
        this.context = context;
        viewPager = context.getViewPager();
        tabLayout = context.getTabLayout();
        undo = context.findViewById(R.id.undo);
        redo = context.findViewById(R.id.redo);
        emptyPager = context.findViewById(R.id.empty_editor);
        editorStateAdapter = new EditorStateAdapter(context);
        viewPager.setUserInputEnabled(false);
        editorStateAdapter.setOnChangeListener(
                (hasItems) -> {
                    viewPager.setVisibility(hasItems ? View.VISIBLE : View.GONE);
                    tabLayout.setVisibility(hasItems ? View.VISIBLE : View.GONE);
                    undo.setVisibility(hasItems ? View.VISIBLE : View.GONE);
                    redo.setVisibility(hasItems ? View.VISIBLE : View.GONE);
                    emptyPager.setVisibility(hasItems ? View.GONE : View.VISIBLE);
                    context.updateItemVisibility(R.id.save_all, hasItems);
                });
        viewPager.setAdapter(editorStateAdapter);
        tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, this::handleMediator);
        tabLayoutMediator.attach();

        tabLayout.addOnTabSelectedListener(
                new TabLayout.OnTabSelectedListener() {

                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        int pos = viewPager.getCurrentItem();
                        BaseFragment f = editorStateAdapter.get(pos);
                        boolean isFile = f instanceof FileFragment;
                        undo.setVisibility(isFile ? View.VISIBLE : View.GONE);
                        redo.setVisibility(isFile ? View.VISIBLE : View.GONE);
                        context.updateItemEnabled(R.id.save_all, isFile);
                        if (contentChangeEvent != null) contentChangeEvent.unsubscribe();
                        if (isFile) {
                            FileFragment ff = (FileFragment) f;
                            handler.post(
                                    () ->
                                            contentChangeEvent =
                                                    ff.getEditor()
                                                            .subscribeEvent(
                                                                    ContentChangeEvent.class,
                                                                    (s, event) -> {
                                                                        undo.setEnabled(
                                                                                ff.getEditor()
                                                                                        .canUndo());
                                                                        redo.setEnabled(
                                                                                ff.getEditor()
                                                                                        .canRedo());
                                                                    }));
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {}

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {}
                });
        undo.setOnClickListener(
                v -> {
                    BaseFragment f = editorStateAdapter.get(viewPager.getCurrentItem());
                    if (f instanceof FileFragment) {
                        FileFragment ff = (FileFragment) f;
                        ff.getEditor().undo();
                    }
                });
        redo.setOnClickListener(
                v -> {
                    BaseFragment f = editorStateAdapter.get(viewPager.getCurrentItem());
                    if (f instanceof FileFragment) {
                        FileFragment ff = (FileFragment) f;
                        ff.getEditor().redo();
                    }
                });
    }

    private void handleMediator(TabLayout.Tab tab, int position) {
        BaseFragment fragment = editorStateAdapter.get(position);
        if (fragment != null) {
            fragment.setOnTitleChanged(tab::setText);
        }
        tab.setText(fragment.getTitle());
        tab.view.setOnClickListener(
                v -> {
                    if (position != viewPager.getCurrentItem()) return;
                    OverflowMenu.show(
                            v,
                            R.menu.tab,
                            item -> {
                                int id = item.getItemId();
                                if (id == R.id.close_it) {
                                    editorStateAdapter.remove(viewPager.getCurrentItem());
                                } else if (id == R.id.close_other) {
                                    editorStateAdapter.removeOther(viewPager.getCurrentItem());
                                } else if (id == R.id.close_all) {
                                    editorStateAdapter.clear();
                                }
                            });
                });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.save_all) {
            editorStateAdapter.saveAll();
            return true;
        }
        return false;
    }

    public void openFile(Node file) {
        try {
            if (!editorStateAdapter.existsFile(file))
                editorStateAdapter.add(FileFragment.newInstance(file));
        } catch (Exception err) {
            ErrorDialog.show(context, err);
        }
    }

    public void closeFile(Node file) {
        try {
            editorStateAdapter.removeByFile(file);
        } catch (Exception err) {
            ErrorDialog.show(context, err);
        }
    }

    public void onDestroy() {
        tabLayoutMediator.detach();
        editorStateAdapter.setOnChangeListener(null);
        context = null;
    }
}
