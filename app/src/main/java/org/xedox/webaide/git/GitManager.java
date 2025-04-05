package org.xedox.webaide.git;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.xedox.webaide.Project;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class GitManager {
    private static final String PREFS_NAME = "GitManagerPrefs";
    private static final String LOGIN_KEY = "git.login";
    private static final String TOKEN_KEY = "git.token";

    private static final Pattern REPO_NAME_PATTERN = Pattern.compile(".*[:/]([^/]+?)/?$");

    private Git git;
    private final Project project;
    private final Context context;
    private static CredentialsProvider auth;

    public GitManager(@NonNull Context context, @NonNull Project project) {
        if (context == null) throw new IllegalArgumentException("Context cannot be null");
        if (project == null) throw new IllegalArgumentException("Project cannot be null");

        this.project = project;
        this.context = context.getApplicationContext();
        loadCredentials();
    }

    public void init() throws GitAPIException, IOException {
        if (git != null) {
            throw new IllegalStateException("Git repository already initialized");
        }

        File repoDir = project.path.toFile();
        if (!repoDir.exists() && !repoDir.mkdirs()) {
            throw new IOException(
                    "Failed to create repository directory: " + repoDir.getAbsolutePath());
        }

        git = Git.init().setDirectory(repoDir).call();
    }

    public static void clone(@NonNull String url, @NonNull String targetDir)
            throws GitAPIException, IOException {
        if (url.isEmpty()) {
            throw new IllegalArgumentException("Repository URL cannot be empty");
        }
        if (targetDir == null) {
            throw new IllegalArgumentException("Target directory cannot be null");
        }

        String repoName = extractRepoNameFromUrl(url);
        if (repoName == null) {
            throw new IllegalArgumentException("Invalid repository URL format");
        }

        File cloneDir = new File(targetDir, repoName);
        File parentDir = cloneDir.getParentFile();

        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            throw new IOException(
                    "Failed to create parent directories: " + parentDir.getAbsolutePath());
        }

        try {
            Git.cloneRepository()
                    .setURI(url)
                    .setDirectory(cloneDir)
                    .setCredentialsProvider(auth)
                    .call();
        } catch (GitAPIException e) {
            throw new RuntimeException("Failed to clone repository: " + e.getMessage(), e);
        }
    }

    @Nullable
    public static String extractRepoNameFromUrl(@Nullable String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        String cleanedUrl = url.replaceAll("\\.git$", "");
        var matcher = REPO_NAME_PATTERN.matcher(cleanedUrl);

        return matcher.find() ? matcher.group(1) : null;
    }

    public void add(@NonNull String filePattern) throws GitAPIException {
        git.add().addFilepattern(filePattern).call();
    }

    public void commit(@NonNull String message) throws GitAPIException {
        if (message.isEmpty()) {
            throw new IllegalArgumentException("Commit message cannot be empty");
        }
        git.commit().setMessage(message).call();
    }

    public void push() throws GitAPIException {
        checkAuthentication();
        git.push().setCredentialsProvider(auth).call();
    }

    public void pull() throws GitAPIException {
        checkAuthentication();
        git.pull().setCredentialsProvider(auth).call();
    }

    public void addRemote(@NonNull String url, @NonNull String remoteName) throws GitAPIException {
        if (remoteName.isEmpty()) {
            throw new IllegalArgumentException("Remote name cannot be empty");
        }
        try {

            git.remoteAdd().setName(remoteName).setUri(new URIish(url)).call();
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public void setCredentials(@NonNull String login, @NonNull String token) {
        if (login.isEmpty() || token.isEmpty()) {
            throw new IllegalArgumentException("Login and token cannot be empty");
        }

        auth = new UsernamePasswordCredentialsProvider(login, token);
        saveCredentials(login, token);
    }

    public void close() {
        if (git != null) {
            git.close();
            git = null;
        }
    }

    @Nullable
    public static String getSavedLogin(@NonNull Context context) {
        return getSharedPreferences(context).getString(LOGIN_KEY, null);
    }

    @Nullable
    public static String getSavedToken(@NonNull Context context) {
        return getSharedPreferences(context).getString(TOKEN_KEY, null);
    }

    @Nullable
    public static CredentialsProvider getCurrentAuth() {
        return auth;
    }

    private void saveCredentials(@NonNull String login, @NonNull String token) {
        getSharedPreferences(context)
                .edit()
                .putString(LOGIN_KEY, login)
                .putString(TOKEN_KEY, token)
                .apply();
    }

    private void loadCredentials() {
        SharedPreferences prefs = getSharedPreferences(context);
        String login = prefs.getString(LOGIN_KEY, null);
        String token = prefs.getString(TOKEN_KEY, null);

        if (login != null && token != null) {
            auth = new UsernamePasswordCredentialsProvider(login, token);
        }
    }

    @NonNull
    private static SharedPreferences getSharedPreferences(@NonNull Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private void checkAuthentication() {
        if (auth == null) {
            throw new IllegalStateException(
                    "Authentication credentials not set. Call setCredentials() first.");
        }
    }

    public Status getStatus() throws GitAPIException {
        return git.status().call();
    }

    public static class RepositoryStatus {
        public final boolean hasUncommittedChanges;
        public final boolean hasUntrackedFiles;
        public final boolean hasUnpushedCommits;
        public final Map<String, Set<String>> fileStatuses;
        public final String currentBranch;
        public final String repositoryState;

        public RepositoryStatus(
                boolean hasUncommittedChanges,
                boolean hasUntrackedFiles,
                boolean hasUnpushedCommits,
                Map<String, Set<String>> fileStatuses,
                String currentBranch,
                String repositoryState) {
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
        
        Status status = git.status().call();
        Map<String, Set<String>> fileStatuses = new HashMap<>();

        fileStatuses.put("added", status.getAdded());
        fileStatuses.put("changed", status.getChanged());
        fileStatuses.put("modified", status.getModified());
        fileStatuses.put("removed", status.getRemoved());
        fileStatuses.put("missing", status.getMissing());
        fileStatuses.put("conflicting", status.getConflicting());
        fileStatuses.put("untracked", status.getUntracked());
        fileStatuses.put("uncommittedChanges", status.getUncommittedChanges());

        String branch = git.getRepository().getBranch();

        String repoState = git.getRepository().getRepositoryState().name();

        return new RepositoryStatus(
                !status.isClean(),
                !status.getUntracked().isEmpty(),
                !status.getUncommittedChanges().isEmpty(),
                fileStatuses,
                branch,
                repoState);
    }

    @NonNull
    public String getFullStatus() throws GitAPIException, IOException {
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
        sb.append("Uncommitted changes: ")
                .append(!status.getUncommittedChanges().isEmpty())
                .append("\n");

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
