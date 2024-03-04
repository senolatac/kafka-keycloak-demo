package com.nar.kafkademo;

import com.nar.kafkademo.model.DemoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DemoRepository extends JpaRepository<DemoEntity, UUID> {
}
