package com.factory.alert.infrastructure.repository;

import com.factory.alert.dto.response.AlertResponse;
import com.factory.alert.dto.response.CountResponse;
import com.factory.alert.infrastructure.enums.AlertSeverity;
import com.factory.alert.infrastructure.enums.AlertStatus;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import static com.factory.alert.infrastructure.entity.QAlert.alert;

@Repository
@RequiredArgsConstructor
public class AlertRepositorySupportImpl implements AlertRepositorySupport {

    private final JPAQueryFactory queryFactory;

    @Override
    public long bulkUpdateAll(String status, String severity) {
        JPAUpdateClause clause = queryFactory.update(alert);

        if (status != null) {
            clause.set(alert.status, AlertStatus.fromCode(status));
        }
        if (severity != null) {
            clause.set(alert.severity, AlertSeverity.fromCode(severity));
        }
        return clause.execute();
    }

    @Override
    public Page<AlertResponse> fetchAlertsWithCondition(String status, String severity,
        Pageable pageable) {

        JPAQuery<AlertResponse> query = queryFactory
            .select(getAlertResponseProjection())
            .from(alert)
            .where(
                statusEq(status),
                severityEq(severity)
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

        pageable.getSort().forEach(order -> {
            query.orderBy(toOrderSpecifier(order));
        });
        List<AlertResponse> content = query.fetch();

        JPAQuery<Long> countQuery = queryFactory
            .select(alert.count())
            .from(alert)
            .where(
                statusEq(status),
                severityEq(severity)
            );

        return PageableExecutionUtils.getPage(
            content,
            pageable,
            countQuery::fetchOne
        );
    }

    @Override
    public AlertResponse fetchAlert(Long id) {
        return queryFactory
            .select(getAlertResponseProjection())
            .from(alert)
            .where(alert.id.eq(id))
            .fetchOne();
    }

    @Override
    public CountResponse fetchCountWithStatus(List<String> status) {

        List<AlertStatus> statuses = toStatuses(status);

        return queryFactory
            .select(Projections.constructor(
                CountResponse.class,
                alert.count(),
                warningCountExpr(statuses),
                criticalCountExpr(statuses)
            ))
            .from(alert)
            .where(statusIn(statuses))
            .fetchOne();
    }

    private BooleanExpression statusEq(String status) {
        return status != null ? alert.status.eq(AlertStatus.fromCode(status)) : null;
    }

    private BooleanExpression severityEq(String severity) {
        return severity != null ? alert.severity.eq(AlertSeverity.fromCode(severity)) : null;
    }

    private BooleanExpression statusIn(List<AlertStatus> statuses) {
        return (statuses == null || statuses.isEmpty())
            ? null
            : alert.status.in(statuses);
    }

    private ConstructorExpression<AlertResponse> getAlertResponseProjection() {
        return Projections.constructor(
            AlertResponse.class,
            alert.id,
            alert.anomalyId,
            alert.equipmentId,
            alert.title,
            alert.message,
            alert.status.stringValue(),
            alert.severity.stringValue(),
            alert.createdAt,
            alert.updatedAt
        );
    }

    private List<AlertStatus> toStatuses(List<String> status) {
        if (status == null || status.isEmpty()) {
            return null;
        }

        return status.stream()
            .map(AlertStatus::fromCode)
            .toList();
    }

    private NumberExpression<Long> warningCountExpr(List<AlertStatus> statuses) {
        return new CaseBuilder()
            .when(alert.severity.eq(AlertSeverity.WARNING))
            .then(1L)
            .otherwise(0L)
            .sumLong();
    }

    private NumberExpression<Long> criticalCountExpr(List<AlertStatus> statuses) {
        return new CaseBuilder()
            .when(alert.severity.eq(AlertSeverity.CRITICAL))
            .then(1L)
            .otherwise(0L)
            .sumLong();
    }

    private OrderSpecifier<?> toOrderSpecifier(Sort.Order order) {
        PathBuilder<Object> entityPath = new PathBuilder<>(alert.getType(), alert.getMetadata());
        ComparableExpression<?> sortPath = entityPath.getComparable(order.getProperty(),
            Comparable.class);
        return order.isAscending() ? sortPath.asc() : sortPath.desc();
    }
}
