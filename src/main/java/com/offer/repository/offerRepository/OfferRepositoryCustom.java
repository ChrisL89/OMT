package com.offer.repository.offerRepository;

import com.offer.model.BonusDto;
import com.offer.model.Offer;

import java.util.List;

public interface OfferRepositoryCustom {

    List<BonusDto> findOffersByCustomerId(Long customerId, OfferRepository offerRepository);

    List<BonusDto> findOffersByCustomerId(Long customerId, String status, int fromNumber, int toNumber, OfferRepository offerRepository);

    Offer findDepositOfferByCustomerId(Long customerId, Float depositAmount, String currency);
    Offer findDepositOfferByCustomerId(Long customerId, String bonusStatus, String offerTriggerType, Float depositAmount, String currency, String promotionCode);

    Offer findLoginOfferByCustomerId(Long customerId, String bonusStatus, String offerTriggerType);

    Offer findRegisterOfferForCustomer(String promoCode, String offerTriggerType, String offerStatus);
}
