/*
 * The MIT License
 *
 * Copyright (c) 2025, Nikolas Falco
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

import com.cloudbees.jenkins.plugins.bitbucket.api.endpoint.BitbucketEndpoint;
import com.cloudbees.jenkins.plugins.bitbucket.api.webhook.BitbucketWebhookProcessor;
import com.cloudbees.jenkins.plugins.bitbucket.api.webhook.BitbucketWebhookProcessorException;
import edu.umd.cs.findbugs.annotations.NonNull;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.lang3.StringUtils;

abstract class AbstractPostWebhookProcessor implements BitbucketWebhookProcessor {

    private static final String SERVER_URL_PARAMETER = "server_url";
    private static final String EVENT_TYPE_HEADER = "X-Event-Key";

    @Override
    public boolean canHandle(@NonNull Map<String, String> headers, @NonNull MultiValuedMap<String, String> parameters) {
        if (headers.containsKey(EVENT_TYPE_HEADER)
                && headers.containsKey("X-Bitbucket-Type")
                && parameters.containsKey(SERVER_URL_PARAMETER)) {
            PostWebhooksEventType eventType = PostWebhooksEventType.fromHeader(headers.get(EVENT_TYPE_HEADER));
            return eventType != null && getSupportedEvents().contains(eventType);
        }
        return false;
    }

    protected abstract List<PostWebhooksEventType> getSupportedEvents();

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
    public void verifyPayload(Map<String, String> headers, String payload, BitbucketEndpoint endpoint) throws BitbucketWebhookProcessorException {
    }

    @NonNull
    protected String getOrigin(Map<String, Object> context) {
        return StringUtils.firstNonBlank((String) context.get("origin"), "unknow");
    }

}
