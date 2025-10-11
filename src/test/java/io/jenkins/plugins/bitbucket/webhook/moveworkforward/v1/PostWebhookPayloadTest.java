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

import com.cloudbees.jenkins.plugins.bitbucket.impl.util.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

class PostWebhookPayloadTest {

    @Test
    void test_serialization() throws Exception {
        PostWebhookPayload payload = new PostWebhookPayload();
        payload.setActive(true);
        payload.setDescription("Jenkins hook");
        payload.setUrl("http://local-jenkins.com/bitbucket-scmsource-hook/notify");
        payload.setCommittersToIgnore("jdoe");
        payload.setBranchesToIgnore("release/*");
        payload.setSkipCI(true);
        payload.setTagCreated(true);
        payload.setBranchCreated(true);
        payload.setBranchDeleted(true);
        payload.setRepoPush(true);
        payload.setPrRescoped(false);
        payload.setPrMerged(true);
        payload.setPrReopened(true);
        payload.setPrUpdated(true);
        payload.setPrCreated(true);
        payload.setPrCommented(false);
        payload.setPrDeleted(true);
        payload.setPrDeclined(true);
        payload.setBuildStatus(false);
        payload.setRepoMirrorSynced(true);
        payload.setId(21);

        assertThatJson(JsonParser.toString(payload)).isEqualTo(loadResource("payload.json"));
    }

    private String loadResource(String resource) throws IOException {
        return IOUtils.toString(this.getClass().getResourceAsStream(resource), StandardCharsets.UTF_8);
    }
}
