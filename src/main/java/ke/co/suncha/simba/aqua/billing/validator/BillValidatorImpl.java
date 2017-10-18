package ke.co.suncha.simba.aqua.billing.validator;

import ke.co.suncha.simba.admin.service.SimbaOptionService;
import ke.co.suncha.simba.aqua.account.Account;
import ke.co.suncha.simba.aqua.account.AccountService;
import ke.co.suncha.simba.aqua.account.BillingFrequency;
import ke.co.suncha.simba.aqua.account.OnStatus;
import ke.co.suncha.simba.aqua.billing.Bill;
import ke.co.suncha.simba.aqua.billing.BillingService;
import ke.co.suncha.simba.aqua.services.BillingMonthService;
import ke.co.suncha.simba.aqua.utils.BillRequest;
import ke.co.suncha.simba.aqua.utils.Response;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by maitha.manyala on 10/18/17.
 */
@Service
public class BillValidatorImpl implements BillValidator {
    @Autowired
    AccountService accountService;

    @Autowired
    BillingMonthService billingMonthService;

    @Autowired
    SimbaOptionService optionService;

    @Autowired
    BillingService billingService;

    @Override
    public Response create(BillRequest billRequest, Long accountId) {
        if (accountId == null) {
            return Response.ErrorOccurred("Invalid account resource");
        }

        if (!billingMonthService.canTransact(new DateTime())) {
            return Response.ErrorOccurred("Sorry we can not complete your request, invalid billing month/transaction date");
        }

        Account account = accountService.getById(accountId);

        if (account == null) {
            return Response.ErrorOccurred("Invalid account");
        }

        if (!account.isActive()) {
            return Response.ErrorOccurred("Sorry we can not complete your request, the account is inactive.");
        }

        if (account.getOnStatus() != OnStatus.TURNED_ON) {
            return Response.ErrorOccurred("Sorry we can not complete your request, you can only bill account which has been turned on.");
        }

        //Check if meter validation is enabled
        Boolean billOnlyMeteredAccounts = Boolean.parseBoolean(optionService.getOption("BILL_ONLY_METERED_ACCOUNTS").getValue());
        if (billOnlyMeteredAccounts) {
            if (account.getMeter() == null) {
                return Response.ErrorOccurred("Sorry we can not complete your request, the account is not metered.");
            }
        }

        Bill lastBill = billingService.getLastBill(accountId);
        if (account.getBillingFrequency() == BillingFrequency.MONTHLY) {
            if (lastBill.isBilled()) {
                return Response.ErrorOccurred("The account has already being billed this month.");
            }
        }

        if (account.getBillingFrequency() == BillingFrequency.ANY_TIME) {
            if (billRequest.getTransactionDate() == null) {
                return Response.ErrorOccurred("The account billing frequency requires you provide a transaction date.");
            }
            if (!billingMonthService.canTransact(billRequest.getTransactionDate())) {
                return Response.ErrorOccurred("Sorry we can not complete your request, transaction date does not fall within the current billing month");
            }
        }

        if (!billRequest.getBillWaterSale()) {
            if (Double.compare(billRequest.getCurrentReading(), billRequest.getPreviousReading()) != 0) {
                return Response.ErrorOccurred("Previous reading and current reading miss match");
            }
        }

        if (billRequest.getPreviousReading() == null) {
            return Response.ErrorOccurred("Previous reading can not be empty");
        }

        if (billRequest.getCurrentReading() == null) {
            return Response.ErrorOccurred("Current reading can not be empty");
        }

        if (billingMonthService.getActiveMonth() == null) {
            return Response.ErrorOccurred("Invalid billing month, we could complete your request.");
        }

        return Response.NoError();
    }
}
