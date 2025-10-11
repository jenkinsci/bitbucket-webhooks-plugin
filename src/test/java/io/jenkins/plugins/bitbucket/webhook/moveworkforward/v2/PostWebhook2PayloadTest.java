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

import com.cloudbees.jenkins.plugins.bitbucket.impl.util.JsonParser;
import io.jenkins.plugins.bitbucket.webhook.moveworkforward.processor.PostWebhooksEventType;
import io.jenkins.plugins.bitbucket.webhook.moveworkforward.v2.PostWebhook2Payload.Destination;
import io.jenkins.plugins.bitbucket.webhook.moveworkforward.v2.PostWebhook2Payload.Source;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

class PostWebhook2PayloadTest {

    @Test
    void test_serialization() throws Exception {
        String projectKey = "EVENT_1";
        String repositorySlug = "eve_1";

        PostWebhook2Payload payload = new PostWebhook2Payload();
        payload.setId(5);
        payload.setActive(true);
        payload.setDescription("Repository1 configuration 1");
        payload.setDestinations(new Destination[] { new Destination("http://example.com/webhook") });
        payload.setSources(new Source[] { new Source(projectKey, repositorySlug) });
        payload.setIgnoredSources(new Source[] { new Source(projectKey, repositorySlug, "tttt") });
        payload.setUsers(new String[] { "user1" });
        payload.setIgnoredUsers(new String[] { "user2" });
        payload.setProjectKey(projectKey);
        payload.setRepositorySlug(repositorySlug);
        payload.setEventTypes(List.of(PostWebhooksEventType.BRANCH_DELETED, PostWebhooksEventType.PULL_REQUEST_DELETED));

        assertThatJson(JsonParser.toString(payload))
            .whenIgnoringPaths("lastModified", "override", "authenticationType", "skipPersonalProjects")
            .isEqualTo(loadResource("payload.json"));
    }

    private String loadResource(String resource) throws IOException {
        return IOUtils.toString(this.getClass().getResourceAsStream(resource), StandardCharsets.UTF_8);
    }
}
