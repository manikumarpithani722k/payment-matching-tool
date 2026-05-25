package com.paymentmatchingtool.repository;

import com.paymentmatchingtool.entity.MatchResult;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {

    List<MatchResult> findByResolvedOrderByOrderIdAscCurrencyAsc(boolean resolved);

    List<MatchResult> findAllByOrderByOrderIdAscCurrencyAsc();
}
