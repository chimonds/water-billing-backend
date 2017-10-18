package ke.co.suncha.simba.aqua.billing.charges;

import com.mysema.query.BooleanBuilder;
import ke.co.suncha.simba.admin.helpers.AuditOperation;
import ke.co.suncha.simba.admin.models.AuditRecord;
import ke.co.suncha.simba.admin.models.User;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.AuditService;
import ke.co.suncha.simba.admin.service.UserService;
import ke.co.suncha.simba.admin.version.ReleaseManager;
import ke.co.suncha.simba.aqua.account.Account;
import ke.co.suncha.simba.aqua.account.AccountService;
import ke.co.suncha.simba.aqua.account.BillingFrequency;
import ke.co.suncha.simba.aqua.account.OnStatus;
import ke.co.suncha.simba.aqua.billing.Bill;
import ke.co.suncha.simba.aqua.billing.BillingServiceImpl;
import ke.co.suncha.simba.aqua.makerChecker.tasks.TaskService;
import ke.co.suncha.simba.aqua.services.BillingMonthService;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

/**
 * Created by maitha.manyala on 10/13/17.
 */
@Service
public class ChargeManager {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AuthManager authManager;

    @Autowired
    AuditService auditService;

    @Autowired
    BillingMonthService billingMonthService;

    @Autowired
    private TaskService taskService;

    @Autowired
    EntityManager entityManager;

    @Autowired
    AccountService accountService;

    @Autowired
    BillingServiceImpl billingService;

    @Autowired
    ReleaseManager releaseManager;

    @Autowired
    ChargeService chargeService;

    @Autowired
    UserService userService;

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    @Transactional
    public RestResponse create(RestRequestObject<ChargeRequest> requestObject, Long accountId) {
        response = authManager.tokenValid(requestObject.getToken());
        if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
            response = authManager.grant(requestObject.getToken(), "bill_account");

            if (response.getStatusCode() != HttpStatus.OK) {
                return response;
            }

            Account account = accountService.getById(accountId);

            if (account == null) {
                responseObject.setMessage("Invalid account.");
                responseObject.setPayload("");
                response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                return response;
            }

            if (chargeService.accountHasCharge(accountId)) {
                responseObject.setMessage("Account charges already exist for this billing month");
                responseObject.setPayload("");
                response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                return response;
            }

            if (!billingMonthService.canTransact(new DateTime())) {
                responseObject.setMessage("Sorry we can not complete your request, invalid billing month/transaction date");
                responseObject.setPayload("");
                response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                return response;
            }

            if (!account.isActive()) {
                responseObject.setMessage("Sorry we can not complete your request, the account is inactive.");
                responseObject.setPayload("");
                response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                return response;
            }

            if (account.getOnStatus() != OnStatus.TURNED_ON) {
                responseObject.setMessage("Sorry we can not complete your request, you can only bill account which has been turned on.");
                responseObject.setPayload("");
                response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                return response;
            }

            Bill lastBill = billingService.getLastBill(accountId);


            if (account.getBillingFrequency() != BillingFrequency.MONTHLY) {
                responseObject.setMessage("Invalid account billing frequency.");
                responseObject.setPayload("");
                response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                return response;
            }

            if (account.getBillingFrequency() == BillingFrequency.MONTHLY) {
                if (lastBill.isBilled()) {
                    responseObject.setMessage("The account has already being billed this month.");
                    responseObject.setPayload("");
                    response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                    return response;
                }
            }

            ChargeRequest chargeRequest = requestObject.getObject();

            if (chargeRequest == null) {
                responseObject.setMessage("Invalid charge request object");
                responseObject.setPayload("");
                response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                return response;
            }

            if (chargeRequest.getBillItemTypes() == null) {
                responseObject.setMessage("Bill items can not be empty");
                responseObject.setPayload("");
                response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                return response;
            }

            if (chargeRequest.getBillItemTypes().isEmpty()) {
                responseObject.setMessage("Bill items can not be empty");
                responseObject.setPayload("");
                response = new RestResponse(responseObject, HttpStatus.CONFLICT);
                return response;
            }

            User user = userService.getByEmailAddress(authManager.getEmailFromToken(requestObject.getToken()));
            Charge charge = chargeService.create(user.getUserId(), accountId, chargeRequest.getBillItemTypes());

            responseObject.setMessage("Account charge added successfully");
            responseObject.setPayload(charge);
            response = new RestResponse(responseObject, HttpStatus.OK);


            //region audit trail
            AuditRecord auditRecord = new AuditRecord();
            auditRecord.setParentID(String.valueOf(charge.getChargeId()));
            auditRecord.setParentObject("CHARGES");
            auditRecord.setCurrentData(charge.toString());
            auditRecord.setNotes("CREATED CHARGE FOR:" + charge.getAccount().getAccNo());
            auditService.log(AuditOperation.CREATED, auditRecord);
            //endregion - audit trail
        }

        return response;
    }

    @Transactional
    public RestResponse delete(RestRequestObject<Charge> requestObject) {
        response = authManager.tokenValid(requestObject.getToken());
        if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
            response = authManager.grant(requestObject.getToken(), "bill_account");
            if (response.getStatusCode() != HttpStatus.OK) {
                return response;
            }
        }

        Charge charge = requestObject.getObject();

        if (charge == null) {
            responseObject.setMessage("Charge resource can not be empty");
            responseObject.setPayload("");
            response = new RestResponse(responseObject, HttpStatus.CONFLICT);
            return response;
        }

        if (!chargeService.exists(charge.getChargeId())) {
            responseObject.setMessage("Invalid charge resource");
            responseObject.setPayload("");
            response = new RestResponse(responseObject, HttpStatus.CONFLICT);
            return response;
        }

        if (!chargeService.canDelete(charge.getChargeId())) {
            responseObject.setMessage("Sorry we can not complete your request. Please contact your admin");
            responseObject.setPayload("");
            response = new RestResponse(responseObject, HttpStatus.CONFLICT);
            return response;
        }
        chargeService.delete(charge.getChargeId());

        responseObject.setMessage("Charge deleted successfully");
        responseObject.setPayload(charge);
        response = new RestResponse(responseObject, HttpStatus.OK);
        return response;
    }

    public RestResponse getPage(RestRequestObject<RestPageRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "accounts_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                RestPageRequest p = requestObject.getObject();

                BooleanBuilder builder = new BooleanBuilder();
                if (p != null) {
                    if (p.getBillingMonthId() != null) {
                        builder.and(QCharge.charge.billingMonth.billingMonthId.eq(p.getBillingMonthId()));
                    }

                    if (StringUtils.isNotEmpty(p.getFilter())) {
                        BooleanBuilder filterBuilder = new BooleanBuilder();
                        filterBuilder.or(QCharge.charge.account.accNo.containsIgnoreCase(p.getFilter()));
                        filterBuilder.or(QCharge.charge.account.consumer.firstName.containsIgnoreCase(p.getFilter()));
                        filterBuilder.or(QCharge.charge.account.consumer.middleName.containsIgnoreCase(p.getFilter()));
                        filterBuilder.or(QCharge.charge.account.consumer.lastName.containsIgnoreCase(p.getFilter()));
                        builder.and(filterBuilder);
                    }
                }

                PageRequest pageRequest = new PageRequest(p.getPage(), p.getSize(), new Sort(Sort.Direction.DESC, "createdOn"));
                Page<Charge> page = chargeService.getPage(builder, pageRequest);

                if (page.hasContent()) {
                    responseObject.setMessage("Fetched data successfully");
                    responseObject.setPayload(page);
                    response = new RestResponse(responseObject, HttpStatus.OK);
                } else {
                    responseObject.setMessage("No records found");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                }
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

}