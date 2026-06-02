package com.factory.alert.infrastructure.repository;

import com.factory.alert.dto.response.AlertResponse;
import com.factory.alert.dto.response.CountResponse;
import com.factory.alert.infrastructure.entity.QAlert;
import com.factory.alert.infrastructure.enums.AlertSeverity;
import com.factory.alert.infrastructure.enums.AlertStatus;
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

@Repository
@RequiredArgsConstructor
public class AlertRepositorySupportImpl implements AlertRepositorySupport {

    private final JPAQueryFactory queryFactory;
    private final QAlert alert = QAlert.alert;

    public long bulkUpdateAll(String status, String severity) {
        JPAUpdateClause clause = queryFactory.update(alert);

        if(status != null) {
            clause.set(alert.status, AlertStatus.fromCode(status));
        }
        if(severity != null) {
            clause.set(alert.severity, AlertSeverity.fromCode(severity));
        }
        return clause.execute();
    }

    public Page<AlertResponse> findWithCondition(String status, String severity,
        Pageable pageable) {

        JPAQuery<AlertResponse> query = queryFactory
            .select(Projections.constructor(
                AlertResponse.class,
                alert.id,
                alert.anomalyId,
                alert.equipmentId,
                alert.title,
                alert.message,
                alert.status.stringValue(),
                alert.severity.stringValue()
            ))
            .from(alert)
            .where(
                eqStatus(status),
                eqSeverity(severity)
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
                eqStatus(status),
                eqSeverity(severity)
            );

        return PageableExecutionUtils.getPage(
            content,
            pageable,
            countQuery::fetchOne
        );
    }

    public CountResponse getCount(List<String> status) {

        List<AlertStatus> statuses = toStatuses(status);

        return queryFactory
            .select(Projections.constructor(
                CountResponse.class,
                alert.count(),
                warningCountExpr(statuses),
                criticalCountExpr(statuses)
            ))
            .from(alert)
            .where(inStatus(statuses))
            .fetchOne();
    }

    private BooleanExpression eqStatus(String status) {
        return status != null ? alert.status.eq(AlertStatus.fromCode(status)) : null;
    }

    private BooleanExpression eqSeverity(String severity) {
        return severity != null ? alert.severity.eq(AlertSeverity.fromCode(severity)) : null;
    }

    private BooleanExpression inStatus(List<AlertStatus> statuses) {
        return (statuses == null || statuses.isEmpty())
            ? null
            : alert.status.in(statuses);
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
            .when(
                alert.status.in(statuses == null ? List.of(AlertStatus.values()) : statuses)
                    .and(alert.severity.eq(AlertSeverity.WARNING))
            )
            .then(1L)
            .otherwise(0L)
            .sumLong();
    }

    private NumberExpression<Long> criticalCountExpr(List<AlertStatus> statuses) {
        return new CaseBuilder()
            .when(
                alert.status.in(statuses == null ? List.of(AlertStatus.values()) : statuses)
                    .and(alert.severity.eq(AlertSeverity.CRITICAL))
            )
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
