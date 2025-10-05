/*
 * The MIT License
 *
 * Copyright (c) 2016, CloudBees, Inc.
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
package io.jenkins.plugins.bitbucket.webhook.moveworkforward.processor;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSource;
import com.cloudbees.jenkins.plugins.bitbucket.api.BitbucketPushEvent;
import com.cloudbees.jenkins.plugins.bitbucket.api.endpoint.BitbucketEndpoint;
import com.cloudbees.jenkins.plugins.bitbucket.api.webhook.BitbucketWebhookProcessor;
import com.cloudbees.jenkins.plugins.bitbucket.api.webhook.BitbucketWebhookProcessorException;
import com.cloudbees.jenkins.plugins.bitbucket.hooks.HookEventType;
import com.cloudbees.jenkins.plugins.bitbucket.server.client.BitbucketServerWebhookPayload;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.scm.api.SCMEvent;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.lang3.StringUtils;

@Extension
public class PostWebhooksProcessor implements BitbucketWebhookProcessor {

    private static final Logger logger = Logger.getLogger(PostWebhooksProcessor.class.getName());
    private static final List<String> supportedEvents = List.of(
            HookEventType.PUSH.getKey());

    protected static final String EVENT_TYPE_HEADER = "X-Event-Key";
    protected static final String SERVER_URL_PARAMETER = "server_url";

    @NonNull
    @Override
    public String getServerURL(@NonNull Map<String, String> headers, @NonNull MultiValuedMap<String, String> parameters) {
        String serverURL = parameters.get(SERVER_URL_PARAMETER).stream()
                .findFirst()
                .orElse(null);
        if (StringUtils.isBlank(serverURL)) {
            throw new BitbucketWebhookProcessorException(HttpServletResponse.SC_BAD_REQUEST, SERVER_URL_PARAMETER + " query parameter not found or empty. Refer to the user documentation on how configure the webHook in Bitbucket at https://github.com/jenkinsci/bitbucket-branch-source-plugin/blob/master/docs/USER_GUIDE.adoc#webhooks-registering");
        }
        return serverURL;
    }

    @NonNull
    @Override
    public String getEventType(@NonNull Map<String, String> headers, @NonNull MultiValuedMap<String, String> parameters) {
        String eventType = headers.get(EVENT_TYPE_HEADER);
        if (StringUtils.isEmpty(eventType)) {
            throw new IllegalStateException(EVENT_TYPE_HEADER + " is missing or empty, this processor should not proceed after canHandle method. Please fill an issue at https://issues.jenkins.io reporting this stacktrace.");
        }
        return eventType;
    }

    @Override
    public boolean canHandle(Map<String, String> headers, MultiValuedMap<String, String> parameters) {
        return headers.containsKey(EVENT_TYPE_HEADER)
                && headers.containsKey("X-Bitbucket-Type")
                && supportedEvents.contains(headers.get(EVENT_TYPE_HEADER))
                && parameters.containsKey(SERVER_URL_PARAMETER);
    }

    @Override
    public void process(@NonNull String eventType, @NonNull String payload, @NonNull Map<String, Object> context, @NonNull BitbucketEndpoint endpoint) {
        BitbucketPushEvent push = BitbucketServerWebhookPayload.pushEventFromPayload(payload);
        if (push != null) {
            if (push.getChanges().isEmpty()) {
                final String owner = push.getRepository().getOwnerName();
                final String repository = push.getRepository().getRepositoryName();
                logger.log(Level.INFO, "Received push hook with empty changes from Bitbucket for {0}/{1}. Skipping.", new Object[]{owner, repository});
            } else {
                SCMEvent.Type type = null;
                for (BitbucketPushEvent.Change change : push.getChanges()) {
                    if ((type == null || type == SCMEvent.Type.CREATED) && change.isCreated()) {
                        type = SCMEvent.Type.CREATED;
                    } else if ((type == null || type == SCMEvent.Type.REMOVED) && change.isClosed()) {
                        type = SCMEvent.Type.REMOVED;
                    } else {
                        type = SCMEvent.Type.UPDATED;
                    }
                }
                notifyEvent(new PostWebhooksPushEvent(type, push, getOrigin(context)), BitbucketSCMSource.getEventDelaySeconds());
            }
        }
    }

    @Override
    public void verifyPayload(Map<String, String> headers, String body, BitbucketEndpoint endpoint) {
        // not supported
    }

    private String getOrigin(Map<String, Object> context) {
        return StringUtils.firstNonBlank((String) context.get("origin"), "unknow");
    }
}
