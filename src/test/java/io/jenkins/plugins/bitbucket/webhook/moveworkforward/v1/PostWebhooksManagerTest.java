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

import com.cloudbees.jenkins.plugins.bitbucket.api.BitbucketAuthenticatedClient;
import com.cloudbees.jenkins.plugins.bitbucket.api.BitbucketWebHook;
import io.jenkins.plugins.bitbucket.webhook.moveworkforward.v1.trait.PostWebhooksConfigurationTrait;
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

class PostWebhooksManagerTest {

    private PostWebhooksManager sut;

    @BeforeEach
    void setup() {
        sut = new PostWebhooksManager();
    }

    @Test
    void test_register_new_webhook() throws Exception {
        BitbucketAuthenticatedClient client = mock(BitbucketAuthenticatedClient.class);
        when(client.getRepositoryOwner()).thenReturn("owner");
        when(client.getRepositoryName()).thenReturn("test_repos");
        when(client.get(anyString())).thenReturn("[]");

        PostWebhooksConfiguration configuration = new PostWebhooksConfiguration(false, null);
        configuration.setEndpointJenkinsRootURL("http://local-jenkins.com/");

        sut.apply(configuration);
        sut.register(client);

        verify(client).post(eq("/rest/webhook/1.0/projects/owner/repos/test_repos/configurations"), any(PostWebhookPayload.class));
    }

    @Test
    void test_register_update_existing_webhook() throws Exception {
        BitbucketAuthenticatedClient client = mock(BitbucketAuthenticatedClient.class);
        when(client.getRepositoryOwner()).thenReturn("owner");
        when(client.getRepositoryName()).thenReturn("test_repos");
        when(client.get(anyString())).thenReturn(loadResource("read_webhooks.json"));

        PostWebhooksConfiguration configuration = new PostWebhooksConfiguration(false, null);
        configuration.setEndpointJenkinsRootURL("http://local-jenkins.com/");

        PostWebhooksConfigurationTrait trait = new PostWebhooksConfigurationTrait("jhon@acme.com", "tmp/*", true);
        sut.apply(trait);
        sut.apply(configuration);
        sut.register(client);

        ArgumentCaptor<PostWebhookPayload> payloadCaptor = ArgumentCaptor.forClass(PostWebhookPayload.class);
        verify(client).put(eq("/rest/webhook/1.0/projects/owner/repos/test_repos/configurations/21"), payloadCaptor.capture());
        assertThat(payloadCaptor.getValue()).satisfies(payload -> {
            assertThat(payload.getBranchesToIgnore()).isEqualTo(trait.getBranchesToIgnore());
            assertThat(payload.getCommittersToIgnore()).isEqualTo(trait.getCommittersToIgnore());
            assertThat(payload.isSkipCI()).isEqualTo(trait.isSkipCI());
        });
    }

    @Test
    void test_read() throws Exception {
        BitbucketAuthenticatedClient client = mock(BitbucketAuthenticatedClient.class);
        when(client.getRepositoryOwner()).thenReturn("owner");
        when(client.getRepositoryName()).thenReturn("test_repos");
        when(client.get(anyString())).thenReturn(loadResource("read_webhooks.json"));

        PostWebhooksConfiguration configuration = new PostWebhooksConfiguration(false, null);
        configuration.setEndpointJenkinsRootURL("http://local-jenkins.com/");

        sut.apply(configuration);
        Collection<BitbucketWebHook> webhooks = sut.read(client);

        assertThat(webhooks).hasSize(1);
        verify(client).get("/rest/webhook/1.0/projects/owner/repos/test_repos/configurations");
    }

    private String loadResource(String resource) throws IOException {
        return IOUtils.toString(this.getClass().getResourceAsStream(resource), StandardCharsets.UTF_8);
    }
}
