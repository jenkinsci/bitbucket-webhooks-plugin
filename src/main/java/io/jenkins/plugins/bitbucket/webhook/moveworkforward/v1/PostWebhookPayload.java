/*
 * The MIT License
 *
 * Copyright (c) 2025, Falco Nikolas
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.jenkins.plugins.bitbucket.webhook.moveworkforward.v1;

import com.cloudbees.jenkins.plugins.bitbucket.api.BitbucketWebHook;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.plugins.bitbucket.webhook.moveworkforward.processor.PostWebhooksEventType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// See https://help.moveworkforward.com/BPW/how-to-manage-configurations-using-post-webhooks-f#HowtomanageconfigurationsusingPostWebhooksforBitbucketAPIs?-Version1
public class PostWebhookPayload implements BitbucketWebHook {
    private Integer id;
    @JsonProperty("title")
    private String description;
    @JsonProperty("url")
    private String url;
    @JsonProperty("enabled")
    private boolean active;
    private String committersToIgnore;
    private String branchesToIgnore;
    private boolean skipCI;
    private boolean tagCreated;
    private boolean branchDeleted;
    private boolean branchCreated;
    private boolean repoPush;
    private boolean prDeclined;
    private boolean prRescoped;
    private boolean prMerged;
    private boolean prReopened;
    private boolean prUpdated;
    private boolean prCreated;
    private boolean prCommented;
    private boolean prDeleted;
    private boolean repoMirrorSynced;
    private boolean buildStatus;

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCommittersToIgnore() {
        return committersToIgnore;
    }

    public void setCommittersToIgnore(String committersToIgnore) {
        this.committersToIgnore = committersToIgnore;
    }

    public String getBranchesToIgnore() {
        return branchesToIgnore;
    }

    public void setBranchesToIgnore(String branchesToIgnore) {
        this.branchesToIgnore = branchesToIgnore;
    }

    public boolean isSkipCI() {
        return skipCI;
    }

    public void setSkipCI(boolean skipCI) {
        this.skipCI = skipCI;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @JsonIgnore
    @Override
    public List<String> getEvents() {
        return getEventTypes().stream().map(PostWebhooksEventType::name).toList();
    }

    @JsonIgnore
    public List<PostWebhooksEventType> getEventTypes() {
        Set<PostWebhooksEventType> events = new HashSet<>();
        if (prCommented) {
            events.add(PostWebhooksEventType.PULL_REQUEST_COMMENT);
        }
        if (prCreated) {
            events.add(PostWebhooksEventType.PULL_REQUEST_OPENED);
        }
        if (prDeclined) {
            events.add(PostWebhooksEventType.PULL_REQUEST_DECLINED);
        }
        if (prDeleted) {
            events.add(PostWebhooksEventType.PULL_REQUEST_DELETED);
        }
        if (prMerged) {
            events.add(PostWebhooksEventType.PULL_REQUEST_MERGED);
        }
        if (prReopened) {
            events.add(PostWebhooksEventType.PULL_REQUEST_REOPENED);
        }
        if (prRescoped) {
            events.add(PostWebhooksEventType.PULL_REQUEST_RESCOPED);
        }
        if (prUpdated) {
            events.add(PostWebhooksEventType.PULL_REQUEST_UPDATED);
        }
        if (repoMirrorSynced) {
            events.add(PostWebhooksEventType.REPOSITORY_MIRROR_SYNCHRONIZED);
        }
        if (repoPush) {
            events.add(PostWebhooksEventType.ABSTRACT_REPOSITORY_REFS_CHANGED);
        }
        if (tagCreated) {
            events.add(PostWebhooksEventType.TAG_CREATED);
        }
        if (branchCreated) {
            events.add(PostWebhooksEventType.BRANCH_CREATED);
        }
        if (branchDeleted) {
            events.add(PostWebhooksEventType.BRANCH_DELETED);
        }
        return new ArrayList<>(events);
    }

    public void setEventTypes(List<PostWebhooksEventType> events) {
        prCommented = events.contains(PostWebhooksEventType.PULL_REQUEST_COMMENT);
        prCreated = events.contains(PostWebhooksEventType.PULL_REQUEST_OPENED);
        prDeclined = events.contains(PostWebhooksEventType.PULL_REQUEST_DECLINED);
        prDeleted = events.contains(PostWebhooksEventType.PULL_REQUEST_DELETED);
        prMerged = events.contains(PostWebhooksEventType.PULL_REQUEST_MERGED);
        prReopened = events.contains(PostWebhooksEventType.PULL_REQUEST_REOPENED);
        prRescoped = events.contains(PostWebhooksEventType.PULL_REQUEST_RESCOPED);
        prUpdated = events.contains(PostWebhooksEventType.PULL_REQUEST_UPDATED);
        repoMirrorSynced = events.contains(PostWebhooksEventType.REPOSITORY_MIRROR_SYNCHRONIZED);
        repoPush = events.contains(PostWebhooksEventType.ABSTRACT_REPOSITORY_REFS_CHANGED);
        tagCreated = events.contains(PostWebhooksEventType.TAG_CREATED);
        branchCreated = events.contains(PostWebhooksEventType.BRANCH_CREATED);
        branchDeleted = events.contains(PostWebhooksEventType.BRANCH_DELETED);
    }

    @Override
    @JsonIgnore
    public String getUuid() {
        if (id != null) {
            return String.valueOf(id);
        }
        return null;
    }

    @Override
    public String getSecret() {
        return null;
    }

    public boolean isTagCreated() {
        return tagCreated;
    }

    public void setTagCreated(boolean tagCreated) {
        this.tagCreated = tagCreated;
    }

    public boolean isBranchDeleted() {
        return branchDeleted;
    }

    public void setBranchDeleted(boolean branchDeleted) {
        this.branchDeleted = branchDeleted;
    }

    public boolean isBranchCreated() {
        return branchCreated;
    }

    public void setBranchCreated(boolean branchCreated) {
        this.branchCreated = branchCreated;
    }

    public boolean isRepoPush() {
        return repoPush;
    }

    public void setRepoPush(boolean repoPush) {
        this.repoPush = repoPush;
    }

    public boolean isPrDeclined() {
        return prDeclined;
    }

    public void setPrDeclined(boolean prDeclined) {
        this.prDeclined = prDeclined;
    }

    public boolean isPrRescoped() {
        return prRescoped;
    }

    public void setPrRescoped(boolean prRescoped) {
        this.prRescoped = prRescoped;
    }

    public boolean isPrMerged() {
        return prMerged;
    }

    public void setPrMerged(boolean prMerged) {
        this.prMerged = prMerged;
    }

    public boolean isPrReopened() {
        return prReopened;
    }

    public void setPrReopened(boolean prReopened) {
        this.prReopened = prReopened;
    }

    public boolean isPrUpdated() {
        return prUpdated;
    }

    public void setPrUpdated(boolean prUpdated) {
        this.prUpdated = prUpdated;
    }

    public boolean isPrCreated() {
        return prCreated;
    }

    public void setPrCreated(boolean prCreated) {
        this.prCreated = prCreated;
    }

    public boolean isPrCommented() {
        return prCommented;
    }

    public void setPrCommented(boolean prCommented) {
        this.prCommented = prCommented;
    }

    public boolean isPrDeleted() {
        return prDeleted;
    }

    public void setPrDeleted(boolean prDeleted) {
        this.prDeleted = prDeleted;
    }

    public boolean isRepoMirrorSynced() {
        return repoMirrorSynced;
    }

    public void setRepoMirrorSynced(boolean repoMirrorSynced) {
        this.repoMirrorSynced = repoMirrorSynced;
    }

    public boolean isBuildStatus() {
        return buildStatus;
    }

    public void setBuildStatus(boolean buildStatus) {
        this.buildStatus = buildStatus;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}
