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
package io.jenkins.plugins.bitbucket.webhook.moveworkforward;

import com.cloudbees.jenkins.plugins.bitbucket.api.BitbucketAuthenticatedClient;
import com.cloudbees.jenkins.plugins.bitbucket.api.BitbucketWebHook;
import com.cloudbees.jenkins.plugins.bitbucket.api.endpoint.BitbucketEndpoint;
import com.cloudbees.jenkins.plugins.bitbucket.api.webhook.BitbucketWebhookConfiguration;
import com.cloudbees.jenkins.plugins.bitbucket.api.webhook.BitbucketWebhookManager;
import com.damnhandy.uri.template.UriTemplate;
import com.google.common.base.Objects;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Util;
import io.jenkins.plugins.bitbucket.webhook.JsonParser;
import io.jenkins.plugins.bitbucket.webhook.moveworkforward.trait.PostWebhooksConfigurationTrait;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import jenkins.scm.api.trait.SCMSourceTrait;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;

import static hudson.Util.fixEmptyAndTrim;

@Extension
public class PostWebhooksManager implements BitbucketWebhookManager {
    private static final String WEBHOOK_API = "/rest/webhook/1.0/projects/{owner}/repos/{repo}/configurations{/id}";
    private static final Logger logger = Logger.getLogger(PostWebhooksManager.class.getName());

    // See https://help.moveworkforward.com/BPW/how-to-manage-configurations-using-post-webhooks-f#HowtomanageconfigurationsusingPostWebhooksforBitbucketAPIs?-Possibleeventtypes
    private static final List<String> PLUGIN_SERVER_EVENTS = Collections.unmodifiableList(Arrays.asList(
            "ABSTRACT_REPOSITORY_REFS_CHANGED", // push event
            "BRANCH_CREATED",
            "BRANCH_DELETED",
            "PULL_REQUEST_DECLINED",
            "PULL_REQUEST_DELETED",
            "PULL_REQUEST_MERGED",
            "PULL_REQUEST_OPENED",
            "PULL_REQUEST_REOPENED",
            "PULL_REQUEST_UPDATED",
            "REPOSITORY_MIRROR_SYNCHRONIZED", // not supported by the hookprocessor
            "TAG_CREATED"));

    private PostWebhooksConfiguration configuration;
    private String callbackURL;
    private String committersToIgnore;
    private String branchesToIgnore;
    private boolean skipCI;

    @Override
    public Collection<Class<? extends SCMSourceTrait>> supportedTraits() {
        return List.of(PostWebhooksConfigurationTrait.class);
    }

    @Override
    public void apply(SCMSourceTrait trait) {
        if (trait instanceof PostWebhooksConfigurationTrait cfgTrait) {
            committersToIgnore = Util.fixEmptyAndTrim(cfgTrait.getCommittersToIgnore());
            branchesToIgnore = Util.fixEmptyAndTrim(cfgTrait.getBranchesToIgnore());
            skipCI = cfgTrait.isSkipCI();
        }
    }

    @Override
    public void apply(BitbucketWebhookConfiguration configuration) {
        this.configuration = (PostWebhooksConfiguration) configuration;
    }

    @Override
    public void setCallbackURL(@NonNull String callbackURL, @NonNull BitbucketEndpoint endpoint) {
        this.callbackURL = UriTemplate.buildFromTemplate(callbackURL)
                .query("server_url")
                .build()
                .set("server_url", endpoint.getServerURL())
                .expand();;
    }

    @Override
    @NonNull
    public Collection<BitbucketWebHook> read(@NonNull BitbucketAuthenticatedClient client) throws IOException {
        String endpointJenkinsRootURL = ObjectUtils.firstNonNull(configuration.getEndpointJenkinsRootURL(), BitbucketWebhookConfiguration.getDefaultJenkinsRootURL());

        String url = UriTemplate.fromTemplate(WEBHOOK_API)
                .set("owner", client.getRepositoryOwner())
                .set("repo", client.getRepositoryName())
                .expand();

        BitbucketPostWebhook[] hooks = JsonParser.toJava(client.get(url), BitbucketPostWebhook[].class);
        return Stream.of(hooks)
                .map(BitbucketWebHook.class::cast)
                .filter(hook -> hook.getUrl().startsWith(endpointJenkinsRootURL))
                .toList();
    }

    @NonNull
    private BitbucketPostWebhook buildPayload() {
        BitbucketPostWebhook hook = new BitbucketPostWebhook();
        hook.setActive(true);
        hook.setDescription("Jenkins hook");
        hook.setUrl(callbackURL);
        hook.setEvents(PLUGIN_SERVER_EVENTS);
        hook.setCommittersToIgnore(committersToIgnore);
        hook.setBranchesToIgnore(branchesToIgnore);
        hook.setSkipCI(skipCI);
        return hook;
    }

    private void register(@NonNull BitbucketPostWebhook payload, @NonNull BitbucketAuthenticatedClient client) throws IOException {
        String url = UriTemplate.fromTemplate(WEBHOOK_API)
                .set("owner", client.getRepositoryOwner())
                .set("repo", client.getRepositoryName())
                .expand();
        client.post(url, payload);
    }

    private boolean shouldUpdate(@NonNull BitbucketPostWebhook current, @NonNull BitbucketPostWebhook expected) {
        boolean update = false;
        if (!Objects.equal(fixEmptyAndTrim(current.getCommittersToIgnore()), expected.getCommittersToIgnore())) {
            logger.info(() -> "Update branchesToIgnore to " + expected.getBranchesToIgnore());
            current.setCommittersToIgnore(expected.getCommittersToIgnore());
            update = true;
        }
        if (!Objects.equal(fixEmptyAndTrim(current.getBranchesToIgnore()), expected.getBranchesToIgnore())) {
            logger.info(() -> "Update branchesToIgnore to " + expected.getBranchesToIgnore());
            current.setBranchesToIgnore(expected.getBranchesToIgnore());
            update = true;
        }
        if (current.isSkipCI() != expected.isSkipCI()) {
            logger.info(() -> "Update skipCI to " + expected.isSkipCI());
            current.setSkipCI(expected.isSkipCI());
            update = true;
        }

        if (!current.isActive()) {
            current.setActive(true);
            logger.info(() -> "Re-activate webhook " + current.getUuid());
            update = true;
        }

        if (!Objects.equal(current.getUrl(), expected.getUrl())) {
            current.setUrl(expected.getUrl());
            logger.info(() -> "Update webhook " + current.getUuid() + " callback URL");
            update = true;
        }

        List<String> events = current.getEvents();
        List<String> expectedEvents = expected.getEvents();
        if (!events.containsAll(expectedEvents)) {
            Set<String> newEvents = new TreeSet<>(events);
            newEvents.addAll(expectedEvents);
            current.setEvents(new ArrayList<>(newEvents));
            logger.info(() -> "Update webhook " + current.getUuid() + " events because was missing: " + CollectionUtils.subtract(expectedEvents, events));
            update = true;
        }
        return update;
    }

    private void update(@NonNull BitbucketPostWebhook payload, @NonNull BitbucketAuthenticatedClient client) throws IOException {
        String url = UriTemplate
                .fromTemplate(WEBHOOK_API)
                .set("owner", client.getRepositoryOwner())
                .set("repo", client.getRepositoryName())
                .set("id", payload.getUuid())
                .expand();
        client.put(url, payload);
    }

    @Override
    public void remove(@NonNull String webhookId, @NonNull BitbucketAuthenticatedClient client) throws IOException {
        String url = UriTemplate.fromTemplate(WEBHOOK_API)
                .set("owner", client.getRepositoryOwner())
                .set("repo", client.getRepositoryName())
                .set("id", webhookId)
                .expand();
        client.delete(url);
    }

    @Override
    public void register(@NonNull BitbucketAuthenticatedClient client) throws IOException {
        BitbucketPostWebhook existingHook = (BitbucketPostWebhook) read(client)
                .stream()
                .findFirst()
                .orElse(null);

        if (existingHook == null) {
            logger.log(Level.INFO, "Registering cloud hook for {0}/{1}", new Object[] { client.getRepositoryOwner(), client.getRepositoryName() });
            register(buildPayload(), client);
        } else if (shouldUpdate(existingHook, buildPayload())) {
            logger.log(Level.INFO, "Updating cloud hook for {0}/{1}", new Object[] { client.getRepositoryOwner(), client.getRepositoryName() });
            update(existingHook, client);
        }
    }

}
