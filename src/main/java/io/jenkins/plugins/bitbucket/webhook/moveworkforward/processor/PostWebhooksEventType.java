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

import com.google.common.base.Objects;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public enum PostWebhooksEventType {
    TAG_CREATED(null/*"repo:push"*/),
    BRANCH_DELETED(null/*"repo:push"*/),
    BRANCH_CREATED(null/*"repo:push"*/),
    REPOSITORY_MIRROR_SYNCHRONIZED(null/*"repo:push"*/),
    ABSTRACT_REPOSITORY_REFS_CHANGED("repo:push"), // any push event
    PULL_REQUEST_DECLINED("pullrequest:rejected"),
    PULL_REQUEST_MERGED("pullrequest:fulfilled"),
    PULL_REQUEST_UPDATED("pullrequest:updated"),
    PULL_REQUEST_RESCOPED("pullrequest:rescoped"),
    PULL_REQUEST_REOPENED(null/*"pullrequest:updated"*/),
    PULL_REQUEST_OPENED("pullrequest:created"),
    PULL_REQUEST_COMMENT("pullrequest:comment"),
    PULL_REQUEST_CANCELABLE_COMMENT(null/*"pullrequest:comment"*/),
    PULL_REQUEST_COMMENT_ACTIVITY(null/*"pullrequest:comment"*/),
    PULL_REQUEST_DELETED("pullrequest:deleted"),
    BUILD_STATUS_SET("build:status");

    private final String headerName;

    private PostWebhooksEventType(String headerName) {
        this.headerName = headerName;
    }

    public String getHeaderName() {
        return headerName;
    }

    @CheckForNull
    public static PostWebhooksEventType fromHeader(@Nullable String headerValue) {
        for (PostWebhooksEventType value : PostWebhooksEventType.values()) {
            if (Objects.equal(value.getHeaderName(), headerValue)) {
                return value;
            }
        }
        return null;
    }

}
