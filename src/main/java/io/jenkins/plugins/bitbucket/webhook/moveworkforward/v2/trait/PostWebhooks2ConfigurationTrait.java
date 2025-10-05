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
package io.jenkins.plugins.bitbucket.webhook.moveworkforward.v2.trait;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketGitSCMBuilder;
import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSource;
import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSourceContext;
import com.cloudbees.jenkins.plugins.bitbucket.api.webhook.BitbucketWebhookManager;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Util;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;
import io.jenkins.plugins.bitbucket.webhook.Messages;
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
public class PostWebhooks2ConfigurationTrait extends SCMSourceTrait {

    private String ignoredUsers;
    private String ignoredGroups;
    private boolean ignoreCerts;
    private boolean ignoreURLValidation;
    /**
     * Do not post webhooks if the title or description of a PR, or the last commit message contain "[ci skip]" or "[skip ci]".
     */
    private final boolean skipCI;

    /**
     * Constructor.
     *
     * @param ignoredUsers a string of comma separated Bitbucket usernames to ignore
     * @param ignoredGroups a string of comma separated Bitbucket groups to ignore
     * @param ignoreCerts accept self-signed certificates
     * @param ignoreURLValidation skip callback URL validation
     * @param skipCI commits that contains specific messages
     */
    @DataBoundConstructor
    public PostWebhooks2ConfigurationTrait(@NonNull String ignoredUsers,
                                           @NonNull String ignoredGroups,
                                           boolean ignoreCerts,
                                           boolean ignoreURLValidation,
                                           boolean skipCI) {
        this.ignoredUsers = Util.fixEmptyAndTrim(ignoredUsers);
        this.ignoredGroups = Util.fixEmptyAndTrim(ignoredGroups);
        this.ignoreCerts = ignoreCerts;
        this.ignoreURLValidation = ignoreURLValidation;
        this.skipCI = skipCI;
    }

    public String getIgnoredUsers() {
        return ignoredUsers;
    }

    public String getIgnoredGroups() {
        return ignoredGroups;
    }

    public boolean isIgnoreCerts() {
        return ignoreCerts;
    }

    public boolean isIgnoreURLValidation() {
        return ignoreURLValidation;
    }

    public boolean isSkipCI() {
        return this.skipCI;
    }

    /**
     * Our constructor.
     */
    @Symbol("postWebhook2Configuration")
    @Extension
    public static class DescriptorImpl extends SCMSourceTraitDescriptor {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return Messages.PostWebhooks2ConfigurationTrait_displayName();
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
