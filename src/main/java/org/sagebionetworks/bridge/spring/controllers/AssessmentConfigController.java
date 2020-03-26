package org.sagebionetworks.bridge.spring.controllers;

import static org.sagebionetworks.bridge.BridgeConstants.UPDATES_TYPEREF;
import static org.sagebionetworks.bridge.Roles.DEVELOPER;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import org.sagebionetworks.bridge.models.accounts.UserSession;
import org.sagebionetworks.bridge.models.assessments.AssessmentConfig;
import org.sagebionetworks.bridge.services.AssessmentConfigService;

@CrossOrigin
@RestController
public class AssessmentConfigController extends BaseController {

    private AssessmentConfigService service;
    
    final void setAssessmentConfigService(AssessmentConfigService service) {
        this.service = service;
    }
    
    @GetMapping("/v1/assessments/{guid}/config")
    public AssessmentConfig getAssessmentConfig(@PathVariable String guid) {
        UserSession session = getAuthenticatedSession();
        String appId = session.getStudyIdentifier().getIdentifier();
        
        return service.getAssessmentConfig(appId, guid);
    }
    
    @PostMapping("/v1/assessments/{guid}/config")
    public AssessmentConfig updateAssessmentConfig(@PathVariable String guid) {
        UserSession session = getAuthenticatedSession(DEVELOPER);
        String appId = session.getStudyIdentifier().getIdentifier();
        
        AssessmentConfig config = parseJson(AssessmentConfig.class);
        
        return service.updateAssessmentConfig(appId, guid, config);
    }
    
    @PostMapping("/v1/assessments/{guid}/config/customize")
    public AssessmentConfig customizeAssessmentConfig(@PathVariable String guid) {
        UserSession session = getAuthenticatedSession(DEVELOPER);
        String appId = session.getStudyIdentifier().getIdentifier();
        
        Map<String, Map<String, JsonNode>> updates = parseJson(UPDATES_TYPEREF);
        
        return service.customizeAssessmentConfig(appId, guid, updates);
    }
    
}
