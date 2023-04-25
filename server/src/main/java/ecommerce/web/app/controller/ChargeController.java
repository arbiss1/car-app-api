package ecommerce.web.app.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import ecommerce.web.app.entities.ChargeRequest;
import ecommerce.web.app.entities.User;
import ecommerce.web.app.service.StripeService;
import ecommerce.web.app.service.UserService;
import ecommerce.web.app.repository.CardRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import javax.validation.Valid;

@Controller
public class ChargeController {

    private final StripeService paymentsService;
    private final CardRepository cardRepository;
    private final UserService userService;

    public ChargeController(StripeService paymentsService,
                            CardRepository cardRepository,
                            UserService userService){
        this.paymentsService = paymentsService;
        this.userService = userService;
        this.cardRepository = cardRepository;
    }

    @PostMapping("/charge")
    public ResponseEntity charge(@Valid @RequestBody ChargeRequest chargeRequest, BindingResult result)
            throws StripeException {
        if(result.hasErrors()){
            return new ResponseEntity(result,HttpStatus.CONFLICT);
        }
        User getAuthenticatedUser = userService.getAuthenticatedUser().get();
        int getTotalAmountOfItemsFoundInCard = cardRepository.getTotalPrice(getAuthenticatedUser.getId());
        chargeRequest.setDescription("Example charge");
        chargeRequest.setAmount(getTotalAmountOfItemsFoundInCard);
        Charge charge = paymentsService.charge(chargeRequest);
        return new ResponseEntity(charge, HttpStatus.ACCEPTED);
    }
}
