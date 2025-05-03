# Android FileTree by Xedox

RecyclerView-based file tree component for Android.

## Usage

1. Add to layout:
```xml
<org.xedox.filetree.widget.FileTree
    android:id="@+id/filetree"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>
```

2. Load directory example:
```java
FileTree fileTree = findViewById(R.id.filetree);
fileTree.loadPath(Environment.getExternalStorageDirectory());
```

## Basic Customization

### Attributes
> While file tree no supported attributes...

### Icons
You can add custom icons to files, and set default icons with drawable.

#### Example
```java
fileTree.adapter.setDefaultFileIcon(R.drawable.ic_file);
fileTree.adapter.setDefaultFolderIcon(R.drawable.ic_folder);
fileTree.adapter.setDefaultFolderOpenIcon(R.drawable.ic_folder_open);

fileTree.adapter.addIcon(".txt", R.drawable.ic_text_file);
fileTree.adapter.addIcon(".zip", R.drawable.ic_zip);
fileTree.adapter.addIcon(".apk", R.drawable.ic_android);

fileTree.adapter.addIcon(".gitignore", R.drawable.ic_git);
fileTree.adapter.addIcon("build.gradle", R.drawable.ic_gradle);
```

### Click listeners:
```java
fileTree.adapter.setOnFileClickListener((node, file, view) -> {...});
fileTree.adapter.setOnFileLongClickListener((node, file, view) -> {...});
```

## Requirements
- Android minSDK - 26, target/compile SDK - 35
- AndroidX - 1.7.0
- Material - 1.13.0-alpha13