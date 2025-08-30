package org.xedox.webaide.sora.snippet;

import io.github.rosemoe.sora.lang.completion.snippet.CodeSnippet;
import com.google.gson.annotations.SerializedName;
import io.github.rosemoe.sora.lang.completion.SnippetDescription;

public class Snippet {
    @SerializedName("prefix")
    private String prefix;

    @SerializedName("description")
    private String description;

    @SerializedName("body")
    private String[] body;

    private CodeSnippet buildedBody;

    public String getPrefix() {
        return prefix;
    }

    public String getDescription() {
        return description;
    }

    public String[] getBody() {
        return body;
    }

    public CodeSnippet getBuildedBody() {
        return buildedBody;
    }

    public void setBuildedBody(CodeSnippet body) {
        this.buildedBody = body;
    }

    public SnippetCompletionItem getComplectionItem() {
        return new SnippetCompletionItem(
                prefix,
                description,
                new SnippetDescription(prefix.length(), getBuildedBody(), true));
    }
}
