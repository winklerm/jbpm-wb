/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.console.ng.cm.backend.server;

import java.util.Comparator;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.jbpm.console.ng.cm.model.CaseCommentSummary;
import org.jbpm.console.ng.cm.model.CaseDefinitionSummary;
import org.jbpm.console.ng.cm.model.CaseInstanceSummary;
import org.jbpm.console.ng.cm.service.CaseManagementService;
import org.jbpm.console.ng.cm.util.CaseInstanceSearchRequest;
import org.jbpm.console.ng.cm.util.CaseInstanceSortBy;
import org.kie.server.api.model.cases.CaseComment;
import org.kie.server.api.model.cases.CaseDefinition;
import org.kie.server.api.model.cases.CaseInstance;
import org.kie.server.client.CaseServicesClient;

import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Service
@ApplicationScoped
public class RemoteCaseManagementServiceImpl implements CaseManagementService {

    @Inject
    private CaseServicesClient client;

    @Override
    public List<CaseDefinitionSummary> getCaseDefinitions() {
        final List<CaseDefinition> caseDefinitions = client.getCaseDefinitions(0, Integer.MAX_VALUE);
        return caseDefinitions.stream().map(new CaseDefinitionMapper()).collect(toList());
    }

    @Override
    public CaseDefinitionSummary getCaseDefinition(final String serverTemplateId, final String containerId, final String caseDefinitionId) {
        return ofNullable(client.getCaseDefinition(containerId, caseDefinitionId)).map(new CaseDefinitionMapper()).orElse(null);
    }

    @Override
    public List<CaseInstanceSummary> getCaseInstances(final CaseInstanceSearchRequest request) {
        final List<CaseInstance> caseInstances = client.getCaseInstances(singletonList(request.getStatus()), 0, Integer.MAX_VALUE);
        return caseInstances.stream().map(new CaseInstanceMapper()).sorted(getCaseInstanceSummaryComparator(request.getSortBy())).collect(toList());
    }

    protected Comparator<CaseInstanceSummary> getCaseInstanceSummaryComparator(final CaseInstanceSortBy sortBy) {
        switch (ofNullable(sortBy).orElse(CaseInstanceSortBy.CASE_ID)) {
            case STARTED:
                return (CaseInstanceSummary cis1, CaseInstanceSummary cis2) -> cis1.getStartedAt().compareTo(cis2.getStartedAt());
            case CASE_ID:
            default:
                return (CaseInstanceSummary cis1, CaseInstanceSummary cis2) -> cis1.getCaseId().compareTo(cis2.getCaseId());
        }
    }

    @Override
    public String startCaseInstance(final String serverTemplateId, final String containerId, final String caseDefinitionId) {
        return client.startCase(containerId, caseDefinitionId);
    }

    @Override
    public void cancelCaseInstance(final String serverTemplateId, final String containerId, final String caseId) {
        client.cancelCaseInstance(containerId, caseId);
    }

    @Override
    public void destroyCaseInstance(final String serverTemplateId, final String containerId, final String caseId) {
        client.destroyCaseInstance(containerId, caseId);
    }

    @Override
    public CaseInstanceSummary getCaseInstance(final String serverTemplateId, final String containerId, final String caseId) {
        return ofNullable(client.getCaseInstance(containerId, caseId, true, true, true, true))
                .map(new CaseInstanceMapper())
                .orElse(null);
    }

    @Override
    public void assignUserToRole(final String serverTemplateId, final String containerId, final String caseId, final String roleName, final String user) {
        client.assignUserToRole(containerId, caseId, roleName, user);
    }

    @Override
    public void assignGroupToRole(final String serverTemplateId, final String containerId, final String caseId, final String roleName, final String group) {
        client.assignGroupToRole(containerId, caseId, roleName, group);
    }

    @Override
    public void removeUserFromRole(final String serverTemplateId, final String containerId, final String caseId, final String roleName, final String user) {
        client.removeUserFromRole(containerId, caseId, roleName, user);
    }

    @Override
    public void removeGroupFromRole(final String serverTemplateId, final String containerId, final String caseId, final String roleName, final String group) {
        client.removeGroupFromRole(containerId, caseId, roleName, group);
    }

    @Override
    public List<CaseCommentSummary> getComments(final String serverTemplateId, final String containerId,
                                                final String caseId, final Integer page, final Integer pageSize) {
        final List<CaseComment> caseComments = client.getComments(containerId, caseId, page, pageSize);
        return caseComments.stream().map(new CaseCommentsMapper()).collect(toList());
    }

    @Override
    public void addComment(final String serverTemplateId, final String containerId, final String caseId,
                           final String author, final String text) {
        client.addComment(containerId, caseId, author, text);
    }

    @Override
    public void updateComment(final String serverTemplateId, final String containerId, final String caseId,
                              final String commentId, final String author, final String text) {
        client.updateComment(containerId, caseId, commentId, author, text);
    }

    @Override
    public void removeComment(final String serverTemplateId, final String containerId, final String caseId,
                              final String commentId) {
        client.removeComment(containerId, caseId, commentId);
    }
}