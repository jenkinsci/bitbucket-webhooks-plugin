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
import com.cloudbees.jenkins.plugins.bitbucket.api.BitbucketPullRequestEvent;
import com.cloudbees.jenkins.plugins.bitbucket.api.endpoint.BitbucketEndpoint;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import jenkins.scm.api.SCMEvent;

@Extension
public class PostWebhooksPullRequestProcessor extends AbstractPostWebhookProcessor {
    private static final Logger logger = Logger.getLogger(PostWebhooksPullRequestProcessor.class.getName());

    @Override
    protected List<PostWebhooksEventType> getSupportedEvents() {
        return List.of(PostWebhooksEventType.PULL_REQUEST_OPENED,
                PostWebhooksEventType.PULL_REQUEST_REOPENED,
                PostWebhooksEventType.PULL_REQUEST_RESCOPED,
                PostWebhooksEventType.PULL_REQUEST_MERGED,
                PostWebhooksEventType.PULL_REQUEST_DECLINED,
                PostWebhooksEventType.PULL_REQUEST_UPDATED);
    }

    @Override
    public void process(@NonNull String hookEventType, @NonNull String payload, @NonNull Map<String, Object> context, @NonNull BitbucketEndpoint endpoint) {
        logger.finer(() -> "Incoming webhook payload: " + payload);

        PostWebhooksEventType hookEvent = PostWebhooksEventType.fromHeader(hookEventType);
        BitbucketPullRequestEvent pull = WebhookPayload.pullRequestEventFromPayload(payload);
        if (pull != null && hookEvent != null) {
            SCMEvent.Type eventType;
            switch (hookEvent) {
                case PULL_REQUEST_OPENED,
                     PULL_REQUEST_REOPENED:
                    eventType = SCMEvent.Type.CREATED;
                    break;
                case PULL_REQUEST_DECLINED,
                     PULL_REQUEST_MERGED:
                    eventType = SCMEvent.Type.REMOVED;
                    break;
                default:
                    eventType = SCMEvent.Type.UPDATED;
                    break;
            }
            // assume updated as a catch-all type
            notifyEvent(new PostWebhooksPREvent(eventType, pull, getOrigin(context), hookEvent), BitbucketSCMSource.getEventDelaySeconds());
        }
    }

}
