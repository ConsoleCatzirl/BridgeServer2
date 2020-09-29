package org.sagebionetworks.bridge.hibernate;

import static java.util.stream.Collectors.toList;

import java.util.List;

import javax.annotation.Resource;

import com.google.common.collect.ImmutableMap;

import org.springframework.stereotype.Component;

import org.sagebionetworks.bridge.dao.EnrollmentDao;
import org.sagebionetworks.bridge.models.PagedResourceList;
import org.sagebionetworks.bridge.models.accounts.AccountRef;
import org.sagebionetworks.bridge.models.studies.EnrollmentDetail;
import org.sagebionetworks.bridge.models.studies.EnrollmentFilter;

@Component
public class HibernateEnrollmentDao implements EnrollmentDao {
    
    static final String REF_QUERY = "SELECT new org.sagebionetworks.bridge.hibernate.HibernateAccount("
            + "a.firstName, a.lastName, a.email, a.phone, a.synapseUserId, a.orgMembership, a.id) FROM "
            + "org.sagebionetworks.bridge.hibernate.HibernateAccount a WHERE a.appId = :appId AND a.id = :id";

    private HibernateHelper hibernateHelper;
    
    @Resource(name = "basicHibernateHelper")
    final void setHibernateHelper(HibernateHelper hibernateHelper) {
        this.hibernateHelper = hibernateHelper;
    }
    
    @Override
    public PagedResourceList<EnrollmentDetail> getEnrollmentsForStudy(String appId, String studyId, 
            EnrollmentFilter filter, Integer offsetBy, Integer pageSize) {
        QueryBuilder builder = new QueryBuilder();
        builder.append("FROM HibernateEnrollment WHERE");
        builder.append("appId = :appId AND studyId = :studyId", "appId", appId, "studyId", studyId);
        builder.enrollment(filter);
        
        int total = hibernateHelper.queryCount("SELECT COUNT(*) " + builder.getQuery(), builder.getParameters());
        
        List<HibernateEnrollment> enrollments = hibernateHelper.queryGet(builder.getQuery(),
                builder.getParameters(), offsetBy, pageSize, HibernateEnrollment.class);
        
        List<EnrollmentDetail> dtos = enrollments.stream().map(enrollment -> {
            AccountRef participantRef = nullSafeAccountRef(appId, enrollment.getAccountId());
            AccountRef enrolledByRef = nullSafeAccountRef(appId, enrollment.getEnrolledBy());
            AccountRef withdrawnByRef = nullSafeAccountRef(appId, enrollment.getWithdrawnBy());
            return new EnrollmentDetail(enrollment, participantRef, enrolledByRef, withdrawnByRef);
        }).collect(toList());
        return new PagedResourceList<>(dtos, total, true);
    }
    
    @Override
    public List<EnrollmentDetail> getEnrollmentsForUser(String appId, String userId) {
        QueryBuilder builder = new QueryBuilder();
        builder.append("FROM HibernateEnrollment WHERE");
        builder.append("appId = :appId AND accountId = :userId", "appId", appId, "userId", userId);
        
        List<HibernateEnrollment> enrollments = hibernateHelper.queryGet(builder.getQuery(),
                builder.getParameters(), null, null, HibernateEnrollment.class);
        return enrollments.stream().map(enrollment -> {
            AccountRef participantRef = nullSafeAccountRef(appId, enrollment.getAccountId());
            AccountRef enrolledByRef = nullSafeAccountRef(appId, enrollment.getEnrolledBy());
            AccountRef withdrawnByRef = nullSafeAccountRef(appId, enrollment.getWithdrawnBy());
            return new EnrollmentDetail(enrollment, participantRef, enrolledByRef, withdrawnByRef);
        }).collect(toList());
    }
    
    private AccountRef nullSafeAccountRef(String appId, String id) {
        if (id == null) {
            return null;
        }
        List<HibernateAccount> accounts = hibernateHelper.queryGet(REF_QUERY, 
                ImmutableMap.of("appId", appId, "id", id), null, 1, HibernateAccount.class);
        if (accounts.isEmpty()) {
            return null;
        }
        return new AccountRef(accounts.get(0));
    }
}
