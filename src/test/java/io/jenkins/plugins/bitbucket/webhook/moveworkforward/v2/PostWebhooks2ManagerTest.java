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
import io.jenkins.plugins.bitbucket.webhook.moveworkforward.v2.trait.PostWebhooks2ConfigurationTrait;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PostWebhooks2ManagerTest {

    private PostWebhooks2Manager sut;

    @BeforeEach
    void setup() {
        sut = new PostWebhooks2Manager();
    }

    @Test
    void test_register_new_webhook() throws Exception {
        BitbucketAuthenticatedClient client = mock(BitbucketAuthenticatedClient.class);
        when(client.getRepositoryOwner()).thenReturn("owner");
        when(client.getRepositoryName()).thenReturn("test_repos");
        when(client.get(anyString())).thenReturn("[]");

        PostWebhooks2Configuration configuration = new PostWebhooks2Configuration(false, null);
        configuration.setEndpointJenkinsRootURL("http://example.com");

        sut.apply(configuration);
        sut.register(client);

        verify(client).post(eq("/rest/webhook/2.0/configurations"), any(PostWebhook2Payload.class));
    }

    @Test
    void test_register_update_existing_webhook() throws Exception {
        BitbucketAuthenticatedClient client = mock(BitbucketAuthenticatedClient.class);
        when(client.getRepositoryOwner()).thenReturn("owner");
        when(client.getRepositoryName()).thenReturn("test_repos");
        when(client.get(anyString())).thenReturn(loadResource("read_webhooks.json"));

        PostWebhooks2Configuration configuration = new PostWebhooks2Configuration(false, null);
        configuration.setEndpointJenkinsRootURL("http://example.com");

        PostWebhooks2ConfigurationTrait trait = new PostWebhooks2ConfigurationTrait(null, "jdoe", "anonym", true, true, true);
        sut.apply(trait);
        sut.apply(configuration);
        sut.register(client);

        ArgumentCaptor<PostWebhook2Payload> payloadCaptor = ArgumentCaptor.forClass(PostWebhook2Payload.class);
        verify(client).put(eq("/rest/webhook/2.0/configurations/5"), payloadCaptor.capture());
        assertThat(payloadCaptor.getValue()).satisfies(payload -> {
            assertThat(payload.getIgnoredUsers()).containsExactly("jdoe");
            assertThat(payload.getIgnoredGroups()).containsExactly("anonym");
            assertThat(payload.isIgnoreCerts()).isEqualTo(trait.isIgnoreCerts());
            assertThat(payload.isIgnoreURLValidation()).isEqualTo(trait.isIgnoreURLValidation());
            assertThat(payload.isSkipCI()).isEqualTo(trait.isSkipCI());
            assertThat(payload.getEvents()).contains(
                    "ABSTRACT_REPOSITORY_REFS_CHANGED",
                    "BRANCH_CREATED",
                    "BRANCH_DELETED",
                    "PULL_REQUEST_DECLINED",
                    "PULL_REQUEST_DELETED",
                    "PULL_REQUEST_MERGED",
                    "PULL_REQUEST_OPENED",
                    "PULL_REQUEST_REOPENED",
                    "PULL_REQUEST_UPDATED",
                    "REPOSITORY_MIRROR_SYNCHRONIZED",
                    "TAG_CREATED");
        });
    }

    @Test
    void test_read() throws Exception {
        BitbucketAuthenticatedClient client = mock(BitbucketAuthenticatedClient.class);
        when(client.getRepositoryOwner()).thenReturn("owner");
        when(client.getRepositoryName()).thenReturn("test_repos");
        when(client.get(anyString())).thenReturn(loadResource("read_webhooks.json"));

        PostWebhooks2Configuration configuration = new PostWebhooks2Configuration(false, null);
        configuration.setEndpointJenkinsRootURL("http://example.com");

        sut.apply(configuration);
        Collection<BitbucketWebHook> webhooks = sut.read(client);

        assertThat(webhooks).hasSize(1);
        verify(client).get("/rest/webhook/2.0/configurations?projectKey=owner&repositorySlug=test_repos");
    }

    private String loadResource(String resource) throws IOException {
        return IOUtils.toString(this.getClass().getResourceAsStream(resource), StandardCharsets.UTF_8);
    }
}
