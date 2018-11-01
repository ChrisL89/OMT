package com.offer.repository;

import com.offer.model.ActivationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface ActivationHistoryRepository extends JpaRepository<ActivationHistory, Long> {

    ArrayList<ActivationHistory> findByOfferId(Long offerId);

}
