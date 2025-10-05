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
package io.jenkins.plugins.bitbucket.webhook.moveworkforward.v2;

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
import io.jenkins.plugins.bitbucket.webhook.moveworkforward.v2.PostWebhook2Payload.Destination;
import io.jenkins.plugins.bitbucket.webhook.moveworkforward.v2.trait.PostWebhooks2ConfigurationTrait;
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
import org.apache.commons.lang3.StringUtils;

// See https://help.moveworkforward.com/BPW/how-to-update-settings-using-rest-apis#HowtoupdatesettingsusingRESTAPIs-RESTAPI
// See https://help.moveworkforward.com/BPW/how-to-manage-configurations-using-post-webhooks-f#HowtomanageconfigurationsusingPostWebhooksforBitbucketAPIs?-Version2
@Extension
public class PostWebhooks2Manager implements BitbucketWebhookManager {
    private static final String WEBHOOK_API = "/rest/webhook/2.0/configurations{/id}{?projectKey,repositorySlug}";
    private static final Logger logger = Logger.getLogger(PostWebhooks2Manager.class.getName());

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

    private PostWebhooks2Configuration configuration;
    private String callbackURL;
    private String[] ignoredUsers;
    private String[] ignoredGroups;
    private boolean ignoreCerts;
    private boolean ignoreURLValidation;
    private boolean skipCI;

    @Override
    public Collection<Class<? extends SCMSourceTrait>> supportedTraits() {
        return List.of(PostWebhooks2ConfigurationTrait.class);
    }

    @Override
    public void apply(SCMSourceTrait trait) {
        if (trait instanceof PostWebhooks2ConfigurationTrait cfgTrait) {
            ignoredUsers = StringUtils.split(Util.fixEmptyAndTrim(cfgTrait.getIgnoredUsers()), ',');
            ignoredGroups = StringUtils.split(Util.fixEmptyAndTrim(cfgTrait.getIgnoredGroups()), ',');
            ignoreCerts = cfgTrait.isIgnoreCerts();
            ignoreURLValidation = cfgTrait.isIgnoreURLValidation();
            skipCI = cfgTrait.isSkipCI();
        }
    }

    @Override
    public void apply(BitbucketWebhookConfiguration configuration) {
        this.configuration = (PostWebhooks2Configuration) configuration;
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
                .set("projectKey", client.getRepositoryOwner())
                .set("repositorySlug", client.getRepositoryName())
                .expand();

        PostWebhook2Payload[] hooks = JsonParser.toJava(client.get(url), PostWebhook2Payload[].class);
        return Stream.of(hooks)
                .map(BitbucketWebHook.class::cast)
                .filter(hook -> hook.getUrl().startsWith(endpointJenkinsRootURL))
                .toList();
    }

    @NonNull
    private PostWebhook2Payload buildPayload() {
        PostWebhook2Payload hook = new PostWebhook2Payload();
        hook.setActive(true);
        hook.setDescription("Jenkins hook");
        hook.setDestinations(new Destination[] { new Destination(callbackURL) });
        hook.setEvents(PLUGIN_SERVER_EVENTS);
        hook.setIgnoredUsers(ignoredUsers);
        hook.setIgnoredGroups(ignoredGroups);
        hook.setIgnoreCerts(ignoreCerts);
        hook.setIgnoreURLValidation(ignoreURLValidation);
        hook.setSkipCI(skipCI);
        return hook;
    }

    private void register(@NonNull PostWebhook2Payload payload, @NonNull BitbucketAuthenticatedClient client) throws IOException {
        String url = UriTemplate.fromTemplate(WEBHOOK_API)
                .expand();
        client.post(url, payload);
    }

    private boolean shouldUpdate(@NonNull PostWebhook2Payload current, @NonNull PostWebhook2Payload expected) {
        boolean update = false;
        if (!Arrays.deepEquals(current.getIgnoredUsers(), expected.getIgnoredUsers())) {
            current.setIgnoredUsers(expected.getIgnoredUsers());
            logger.info(() -> "Update ignoredUsers");
            update = true;
        }
        if (!Arrays.deepEquals(current.getIgnoredGroups(), expected.getIgnoredGroups())) {
            current.setIgnoredGroups(expected.getIgnoredGroups());
            logger.info(() -> "Update ignoredGroups");
            update = true;
        }
        if (current.isIgnoreCerts() != expected.isIgnoreCerts()) {
            current.setIgnoreCerts(expected.isIgnoreCerts());
            logger.info(() -> "Update ignoreCerts to " + expected.isIgnoreCerts());
            update = true;
        }
        if (current.isIgnoreURLValidation() != expected.isIgnoreURLValidation()) {
            current.setIgnoreURLValidation(expected.isIgnoreURLValidation());
            logger.info(() -> "Update ignoreURLValidation to " + expected.isIgnoreURLValidation());
            update = true;
        }
        if (current.isSkipCI() != expected.isSkipCI()) {
            current.setSkipCI(expected.isSkipCI());
            logger.info(() -> "Update skipCI to " + expected.isSkipCI());
            update = true;
        }

        if (!current.isActive()) {
            current.setActive(true);
            logger.info(() -> "Re-activate webhook " + current.getUuid());
            update = true;
        }

        if (!Objects.equal(current.getUrl(), expected.getUrl())) {
            logger.info(() -> "Update webhook " + current.getUuid() + " callback URL");
            current.setDestinations(new Destination[] { new Destination(expected.getUrl()) });
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

    private void update(@NonNull PostWebhook2Payload payload, @NonNull BitbucketAuthenticatedClient client) throws IOException {
        String url = UriTemplate
                .fromTemplate(WEBHOOK_API)
                .set("id", payload.getUuid())
                .expand();
        client.put(url, payload);
    }

    @Override
    public void remove(@NonNull String webhookId, @NonNull BitbucketAuthenticatedClient client) throws IOException {
        String url = UriTemplate.fromTemplate(WEBHOOK_API)
                .set("id", webhookId)
                .expand();
        client.delete(url);
    }

    @Override
    public void register(@NonNull BitbucketAuthenticatedClient client) throws IOException {
        PostWebhook2Payload existingHook = (PostWebhook2Payload) read(client)
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
