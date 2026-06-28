package org.dhentech.infrastructure.repository;

import org.dhentech.domain.TransferStatus;
import org.dhentech.infrastructure.entity.TransferEntity;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransferSpecification {

    private TransferSpecification() {
    }

    public static Specification<TransferEntity> withFilters(
            TransferStatus status, LocalDate from, LocalDate to,
            String sourceAccount, String destinationAccount) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                switch (status) {
                    case PENDING:
                        predicates.add(cb.equal(root.get("status"), TransferStatus.PENDING));
                        predicates.add(cb.greaterThan(root.get("transferDate"), LocalDate.now()));
                        break;
                    case EXECUTED:
                        predicates.add(cb.equal(root.get("status"), TransferStatus.PENDING));
                        predicates.add(cb.lessThanOrEqualTo(root.get("transferDate"), LocalDate.now()));
                        break;
                    case CANCELLED:
                        predicates.add(cb.equal(root.get("status"), TransferStatus.CANCELLED));
                        break;
                }
            }

            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transferDate"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transferDate"), to));
            }
            if (sourceAccount != null && !sourceAccount.isBlank()) {
                predicates.add(cb.equal(root.get("sourceAccount"), sourceAccount));
            }
            if (destinationAccount != null && !destinationAccount.isBlank()) {
                predicates.add(cb.equal(root.get("destinationAccount"), destinationAccount));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
