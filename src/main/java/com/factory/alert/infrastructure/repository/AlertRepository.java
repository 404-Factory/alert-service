package com.factory.alert.infrastructure.repository;

import com.factory.alert.infrastructure.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long>, AlertRepositorySupport {

}
