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
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.scm.api.SCMEvent;

@Extension
public class PostWebhooksProcessor extends AbstractPostWebhookProcessor {

    private static final Logger logger = Logger.getLogger(PostWebhooksProcessor.class.getName());

    @Override
    protected List<PostWebhooksEventType> getSupportedEvents() {
        return List.of(PostWebhooksEventType.ABSTRACT_REPOSITORY_REFS_CHANGED);
    }

    @Override
    public void process(@NonNull String eventType, @NonNull String payload, @NonNull Map<String, Object> context, @NonNull BitbucketEndpoint endpoint) {
        logger.finer(() -> "Incoming webhook payload: " + payload);

        BitbucketPushEvent push = WebhookPayload.pushEventFromPayload(payload);
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

}
