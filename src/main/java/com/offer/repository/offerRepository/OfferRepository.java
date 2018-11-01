package com.offer.repository.offerRepository;

import com.offer.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface OfferRepository extends JpaRepository<Offer, Long>, OfferRepositoryCustom {

    Offer findByOfferCode(String offerCode);

}
