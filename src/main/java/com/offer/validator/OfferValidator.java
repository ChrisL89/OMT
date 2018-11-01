package com.offer.validator;


import com.offer.apierror.ApiErrorCode;
import com.offer.repository.offerRepository.OfferRepository;
import com.offer.model.Offer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class OfferValidator implements Validator {

    private OfferRepository offerRepository;

    @Autowired
    public OfferValidator(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Offer.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Offer offer = (Offer) target;

        if(offerRepository.findByOfferCode(offer.getOfferCode()) != null) {
            errors.reject(ApiErrorCode.ALREADY_EXISTS.getCode());
        }
    }

}
