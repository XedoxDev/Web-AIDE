package org.xedox.webaide.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.xedox.webaide.project.Project;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class GitManager {
    private static final String PREFS_NAME = "GitManagerPrefs";
    private static final String LOGIN_KEY = "git.login";
    private static final String TOKEN_KEY = "git.token";
    private static final Pattern REPO_NAME_PATTERN = Pattern.compile(".*/([^/]+)/?$");

    private Git git;
    private final Project project;
    private final Context context;
    private CredentialsProvider credentialsProvider;

    public GitManager(@NonNull Context context, @NonNull Project project) {
        if (context == null) throw new IllegalArgumentException("Context cannot be null");
        if (project == null) throw new IllegalArgumentException("Project cannot be null");
        if (project.path == null) throw new IllegalStateException("Project path is not set");

        this.context = context.getApplicationContext();
        this.project = project;
        loadCredentials();
    }

    public void init() throws GitAPIException, IOException {
        if (isInitialized()) {
            throw new IllegalStateException("Git repository already initialized");
        }

        File repoDir = project.path.toFile();
        File gitDir = new File(repoDir, ".git");

        if (gitDir.exists()) {
            git = Git.open(repoDir);
            return;
        }

        if (!repoDir.exists() && !repoDir.mkdirs()) {
            throw new IOException("Failed to create repository directory: " + repoDir.getAbsolutePath());
        }

        git = Git.init()
                .setDirectory(repoDir)
                .setBare(false)
                .call();

        if (git.getRepository() == null) {
            throw new IllegalStateException("Repository initialization failed");
        }
    }

    public boolean isInitialized() {
        try {
            return git != null && git.getRepository() != null;
        } catch (Exception e) {
            return false;
        }
    }

    public void add(@NonNull String filePattern) throws GitAPIException, IOException {
        checkInitialized();
        
        Repository repo = git.getRepository();
        File file = new File(repo.getWorkTree(), filePattern);
        
        if (!file.exists()) {
            throw new IOException("File does not exist: " + file.getAbsolutePath());
        }

        git.add()
           .addFilepattern(filePattern)
           .setUpdate(true)
           .call();
    }

    public void commit(@NonNull String message) throws GitAPIException {
        checkInitialized();
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Commit message cannot be empty");
        }
        git.commit().setMessage(message).call();
    }

    public void push() throws GitAPIException {
        checkInitialized();
        checkCredentials();
        git.push().setCredentialsProvider(credentialsProvider).call();
    }

    public void pull() throws GitAPIException {
        checkInitialized();
        checkCredentials();
        git.pull().setCredentialsProvider(credentialsProvider).call();
    }

    public Status getStatus() throws GitAPIException {
        checkInitialized();
        return git.status().call();
    }

    public void addRemote(@NonNull String name, @NonNull String url) throws Exception {
        checkInitialized();
        git.remoteAdd()
           .setName(name)
           .setUri(new URIish(url))
           .call();
    }

    public void setCredentials(@NonNull String username, @NonNull String password) {
        if (username.isEmpty() || password.isEmpty()) {
            throw new IllegalArgumentException("Username and password cannot be empty");
        }
        this.credentialsProvider = new UsernamePasswordCredentialsProvider(username, password);
        saveCredentials(username, password);
    }

    public void close() {
        if (git != null) {
            git.close();
            git = null;
        }
    }

    private void checkInitialized() {
        if (!isInitialized()) {
            throw new IllegalStateException("Git repository not initialized. Call init() first.");
        }
    }

    private void checkCredentials() {
        if (credentialsProvider == null) {
            throw new IllegalStateException("Credentials not set. Call setCredentials() first.");
        }
    }

    private void saveCredentials(String username, String password) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
             .putString(LOGIN_KEY, username)
             .putString(TOKEN_KEY, password)
             .apply();
    }

    private void loadCredentials() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String username = prefs.getString(LOGIN_KEY, null);
        String password = prefs.getString(TOKEN_KEY, null);

        if (username != null && password != null) {
            credentialsProvider = new UsernamePasswordCredentialsProvider(username, password);
        }
    }

    public Set<String> getUntrackedFiles() throws GitAPIException {
        checkInitialized();
        return git.status().call().getUntracked();
    }

    public Set<String> getModifiedFiles() throws GitAPIException {
        checkInitialized();
        return git.status().call().getModified();
    }

    public String getCurrentBranch() throws IOException {
        checkInitialized();
        return git.getRepository().getBranch();
    }

    public static void clone(@NonNull Context context, @NonNull String url, @NonNull String targetDir) throws GitAPIException, IOException {
        if (url.isEmpty()) throw new IllegalArgumentException("Repository URL cannot be empty");
        if (targetDir == null) throw new IllegalArgumentException("Target directory cannot be null");

        String repoName = extractRepoNameFromUrl(url);
        if (repoName == null) throw new IllegalArgumentException("Invalid repository URL format");

        File cloneDir = new File(targetDir, repoName);
        File parentDir = cloneDir.getParentFile();

        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            throw new IOException("Failed to create parent directories: " + parentDir.getAbsolutePath());
        }

        try {
            Git.cloneRepository()
                    .setURI(url)
                    .setDirectory(cloneDir)
                    .call();
        } catch (Exception e) {
            throw new IOException("Failed to clone repository: " + e.getMessage(), e);
        }
    }

    @Nullable
    public static String extractRepoNameFromUrl(@Nullable String url) {
        if (url == null || url.isEmpty()) return null;
        String cleanedUrl = url.replaceAll("\\.git$", "");
        var matcher = REPO_NAME_PATTERN.matcher(cleanedUrl);
        return matcher.find() ? matcher.group(1) : null;
    }

    @Nullable
    public static String getSavedLogin(@NonNull Context context) {
        return getSharedPreferences(context).getString(LOGIN_KEY, null);
    }

    @Nullable
    public static String getSavedToken(@NonNull Context context) {
        return getSharedPreferences(context).getString(TOKEN_KEY, null);
    }

    @NonNull
    private static SharedPreferences getSharedPreferences(@NonNull Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static class RepositoryStatus {
        public final boolean hasUncommittedChanges;
        public final boolean hasUntrackedFiles;
        public final boolean hasUnpushedCommits;
        public final Map<String, Set<String>> fileStatuses;
        public final String currentBranch;
        public final String repositoryState;

        public RepositoryStatus(boolean hasUncommittedChanges, boolean hasUntrackedFiles, boolean hasUnpushedCommits,
                              Map<String, Set<String>> fileStatuses, String currentBranch, String repositoryState) {
            this.hasUncommittedChanges = hasUncommittedChanges;
            this.hasUntrackedFiles = hasUntrackedFiles;
            this.hasUnpushedCommits = hasUnpushedCommits;
            this.fileStatuses = fileStatuses;
            this.currentBranch = currentBranch;
            this.repositoryState = repositoryState;
        }
    }

    @NonNull
    public RepositoryStatus getInfo() throws GitAPIException, IOException {
        checkInitialized();

        Status status = git.status().call();
        Map<String, Set<String>> fileStatuses = new HashMap<>();

        fileStatuses.put("added", status.getAdded());
        fileStatuses.put("changed", status.getChanged());
        fileStatuses.put("modified", status.getModified());
        fileStatuses.put("removed", status.getRemoved());
        fileStatuses.put("missing", status.getMissing());
        fileStatuses.put("conflicting", status.getConflicting());
        fileStatuses.put("untracked", status.getUntracked());

        String branch = git.getRepository().getBranch();
        String repoState = git.getRepository().getRepositoryState().name();

        return new RepositoryStatus(!status.isClean(), !status.getUntracked().isEmpty(),
                !status.getUncommittedChanges().isEmpty(), fileStatuses, branch, repoState);
    }

    @NonNull
    public String getFullStatus() throws GitAPIException, IOException {
        checkInitialized();

        Status status = git.status().call();
        StringBuilder sb = new StringBuilder();

        sb.append("=== Repository Status ===\n");
        sb.append("Branch: ").append(git.getRepository().getBranch()).append("\n");
        sb.append("Has changes: ").append(!status.isClean()).append("\n\n");

        appendStatusSection(sb, "Modified files:", status.getModified());
        appendStatusSection(sb, "Added files:", status.getAdded());
        appendStatusSection(sb, "Changed files:", status.getChanged());
        appendStatusSection(sb, "Removed files:", status.getRemoved());
        appendStatusSection(sb, "Missing files:", status.getMissing());
        appendStatusSection(sb, "Conflicting files:", status.getConflicting());
        appendStatusSection(sb, "Untracked files:", status.getUntracked());
        sb.append("\n=== Commits Status ===\n");
        sb.append("Uncommitted changes: ").append(!status.getUncommittedChanges().isEmpty()).append("\n");

        return sb.toString();
    }

    private void appendStatusSection(StringBuilder sb, String title, Set<String> files) {
        if (!files.isEmpty()) {
            sb.append(title).append("\n");
            for (String file : files) {
                sb.append(" - ").append(file).append("\n");
            }
            sb.append("\n");
        }
    }
}