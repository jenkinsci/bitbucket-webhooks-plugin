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

import com.cloudbees.jenkins.plugins.bitbucket.api.BitbucketWebHook;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;

// See https://help.moveworkforward.com/BPW/how-to-update-settings-using-rest-apis#HowtoupdatesettingsusingRESTAPIs-Schema
public class PostWebhook2Payload implements BitbucketWebHook {
    public static class Source {
        private String projectKey;
        private String repositorySlug;
        private String branchName;

        public String getProjectKey() {
            return projectKey;
        }

        public void setProjectKey(String projectKey) {
            this.projectKey = projectKey;
        }

        public String getRepositorySlug() {
            return repositorySlug;
        }

        public void setRepositorySlug(String repositorySlug) {
            this.repositorySlug = repositorySlug;
        }

        public String getBranchName() {
            return branchName;
        }

        public void setBranchName(String branchName) {
            this.branchName = branchName;
        }

        @Override
        public int hashCode() {
            return Objects.hash(branchName, projectKey, repositorySlug);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Source other = (Source) obj;
            return Objects.equals(branchName, other.branchName)
                    && Objects.equals(projectKey, other.projectKey)
                    && Objects.equals(repositorySlug, other.repositorySlug);
        }
    }

    public static class Destination {
        private boolean useUrl;
        private String url;

        public Destination() {
        }

        public Destination(String url) {
            this.useUrl = true;
            this.url = url;
        }

        public boolean isUseUrl() {
            return useUrl;
        }

        public void setUseUrl(boolean useUrl) {
            this.useUrl = useUrl;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @Override
        public int hashCode() {
            return Objects.hash(url, useUrl);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Destination other = (Destination) obj;
            return Objects.equals(url, other.url) && useUrl == other.useUrl;
        }
    }

    private Integer id;
    @JsonProperty("enabled")
    private boolean active;
    @JsonProperty("name")
    private String description;
    private String projectKey;
    private String repositorySlug;
    private Destination[] destinations;
    private Source[] sources;
    private Source[] ignoredSources;
    private String[] users;
    private String[] ignoredUsers;
    private String[] groups;
    private String[] ignoredGroups;
    @JsonProperty("eventTypes")
    private List<String> events;
    private boolean ignoreCerts;
    private boolean ignoreURLValidation;
    private boolean skipCI;
    private String httpMethod = "POST";
    private String repositoryPattern;
    private String[] filePathPatterns;

    @Override
    public String getUuid() {
        return String.valueOf(id);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public String getRepositorySlug() {
        return repositorySlug;
    }

    public void setRepositorySlug(String repositorySlug) {
        this.repositorySlug = repositorySlug;
    }

    public Destination[] getDestinations() {
        return destinations;
    }

    public void setDestinations(Destination[] destinations) {
        this.destinations = destinations;
    }

    public Source[] getIgnoredSources() {
        return ignoredSources;
    }

    public void setIgnoredSources(Source[] ignoredSources) {
        this.ignoredSources = ignoredSources;
    }

    public String[] getUsers() {
        return users;
    }

    public void setUsers(String[] users) {
        this.users = users;
    }

    public String[] getIgnoredUsers() {
        return ignoredUsers;
    }

    public void setIgnoredUsers(String[] ignoredUsers) {
        this.ignoredUsers = ignoredUsers;
    }

    public String[] getGroups() {
        return groups;
    }

    public void setGroups(String[] groups) {
        this.groups = groups;
    }

    public String[] getIgnoredGroups() {
        return ignoredGroups;
    }

    public void setIgnoredGroups(String[] ignoredGroups) {
        this.ignoredGroups = ignoredGroups;
    }

    @Override
    public List<String> getEvents() {
        return events;
    }

    public void setEvents(List<String> events) {
        this.events = events;
    }

    public boolean isIgnoreCerts() {
        return ignoreCerts;
    }

    public void setIgnoreCerts(boolean ignoreCerts) {
        this.ignoreCerts = ignoreCerts;
    }

    public boolean isIgnoreURLValidation() {
        return ignoreURLValidation;
    }

    public void setIgnoreURLValidation(boolean ignoreURLValidation) {
        this.ignoreURLValidation = ignoreURLValidation;
    }

    public boolean isSkipCI() {
        return skipCI;
    }

    public void setSkipCI(boolean skipCI) {
        this.skipCI = skipCI;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getRepositoryPattern() {
        return repositoryPattern;
    }

    public void setRepositoryPattern(String repositoryPattern) {
        this.repositoryPattern = repositoryPattern;
    }

    public String[] getFilePathPatterns() {
        return filePathPatterns;
    }

    public void setFilePathPatterns(String[] filePathPatterns) {
        this.filePathPatterns = filePathPatterns;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getUrl() {
        return CollectionUtils.sizeIsEmpty(destinations) ? null : destinations[0].getUrl();
    }

    public Source[] getSources() {
        return sources;
    }

    public void setSources(Source[] sources) {
        this.sources = sources;
    }
}
