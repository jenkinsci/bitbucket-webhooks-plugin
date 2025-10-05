/*
 * The MIT License
 *
 * Copyright (c) 2017, CloudBees, Inc.
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
package io.jenkins.plugins.bitbucket.webhook.moveworkforward.trait;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketGitSCMBuilder;
import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSource;
import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSourceContext;
import com.cloudbees.jenkins.plugins.bitbucket.api.webhook.BitbucketWebhookManager;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Util;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;
import io.jenkins.plugins.bitbucket.webhook.moveworkforward.Messages;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.trait.SCMBuilder;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A {@link SCMSourceTrait} for {@link BitbucketSCMSource} to configure extra information in a {@link BitbucketWebhookManager}.
 *
 * @since 1.0.0
 */
public class PostWebhooksConfigurationTrait extends SCMSourceTrait {

    /**
     * The committers that should be ignored in the webhook. A comma separated string.
     */
    private final String committersToIgnore;

    /**
     * Comma separated list of branch masks to ignore notifications from.
     */
    private final String branchesToIgnore;

    /**
     * Do not post webhooks if the title or description of a PR, or the last commit message contain "[ci skip]" or "[skip ci]".
     */
    private final boolean skipCI;

    /**
     * Constructor.
     *
     * @param committersToIgnore a string of comma separated Bitbucket usernames to ignore
     */
    @DataBoundConstructor
    public PostWebhooksConfigurationTrait(@NonNull String committersToIgnore, @NonNull String branchesToIgnore, boolean skipCI) {
        this.committersToIgnore = Util.fixEmptyAndTrim(committersToIgnore);
        this.branchesToIgnore = Util.fixEmptyAndTrim(branchesToIgnore);
        this.skipCI = skipCI;
    }

    public String getCommittersToIgnore() {
        return this.committersToIgnore;
    }

    public String getBranchesToIgnore() {
        return this.branchesToIgnore;
    }

    public boolean isSkipCI() {
        return this.skipCI;
    }

    /**
     * Our constructor.
     */
    @Symbol("bitbucketWebhookConfiguration")
    @Extension
    public static class DescriptorImpl extends SCMSourceTraitDescriptor {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return Messages.WebhookConfigurationTrait_displayName();
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("rawtypes")
        @Override
        public Class<? extends SCMSourceContext> getContextClass() {
            return BitbucketSCMSourceContext.class;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Class<? extends SCMSource> getSourceClass() {
            return BitbucketSCMSource.class;
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("rawtypes")
        @Override
        public Class<? extends SCMBuilder> getBuilderClass() {
            return BitbucketGitSCMBuilder.class;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Class<? extends SCM> getScmClass() {
            return GitSCM.class;
        }
    }
}
