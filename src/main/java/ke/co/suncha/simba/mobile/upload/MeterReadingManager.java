package ke.co.suncha.simba.mobile.upload;

import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.SimbaOptionService;
import ke.co.suncha.simba.admin.service.UserService;
import ke.co.suncha.simba.aqua.account.Account;
import ke.co.suncha.simba.aqua.billing.Bill;
import ke.co.suncha.simba.aqua.billing.BillService;
import ke.co.suncha.simba.aqua.billing.BillingService;
import ke.co.suncha.simba.aqua.billing.charges.Charge;
import ke.co.suncha.simba.aqua.billing.charges.ChargeItem;
import ke.co.suncha.simba.aqua.billing.charges.ChargeService;
import ke.co.suncha.simba.aqua.billing.validator.BillValidator;
import ke.co.suncha.simba.aqua.models.BillItemType;
import ke.co.suncha.simba.aqua.models.BillingMonth;
import ke.co.suncha.simba.aqua.models.MeterReading;
import ke.co.suncha.simba.aqua.services.AccountManagerService;
import ke.co.suncha.simba.aqua.services.BillingMonthService;
import ke.co.suncha.simba.aqua.services.MeterReadingService;
import ke.co.suncha.simba.aqua.services.TariffService;
import ke.co.suncha.simba.aqua.utils.BillMeta;
import ke.co.suncha.simba.aqua.utils.BillRequest;
import ke.co.suncha.simba.aqua.utils.Response;
import ke.co.suncha.simba.mobile.request.RequestResponse;
import ke.co.suncha.simba.mobile.user.MobileUserAuthService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maitha.manyala on 9/3/17.
 */
@Service
@Transactional
public class MeterReadingManager {
    @Autowired
    MobileUserAuthService mobileUserAuthService;

    @Autowired
    BillingMonthService billingMonthService;

    @Autowired
    MeterReadingRecordServiceImpl meterReadingRecordService;

    @Autowired
    AccountManagerService accountService;

    @Autowired
    UserService userService;

    @Autowired
    BillService billService;

    @Autowired
    TariffService tariffService;

    @Autowired
    private SimbaOptionService optionService;

    @Autowired
    BillingService billingService;

    @Autowired
    ChargeService chargeService;

    @Autowired
    BillValidator billValidator;

    @Autowired
    MeterReadingService meterReadingService;

    @Autowired
    AuthManager authManager;

    public RequestResponse<MeterReadingRequest> addMeterReadingRequest(UploadRequest request) {

        RequestResponse<MeterReadingRequest> response = new RequestResponse<>();
        response.setObject(request.getReadingRequest());
        response.setError(Boolean.TRUE);
        MeterReadingRequest meterReadingRequest = request.getReadingRequest();
        if (!mobileUserAuthService.canLogin(request.getUser())) {
            response.setMessage("Access denied to this resource");
            return response;
        } else {
            BillingMonth billingMonth = billingMonthService.getActiveMonth();
            if (billingMonth == null) {
                response.setMessage("No open billing month, please contact your admin");
                return response;
            }
            if (billingMonth.getBillingMonthId() != meterReadingRequest.getBillingMonthId()) {
                response.setMessage("Billing month miss match, please contact your admin");
                return response;
            }

            if (!billingMonth.getMeterReading()) {
                response.setMessage("Meter reading is not open, please contact your admin");
                return response;
            }

            Account account = accountService.getByAccountId(meterReadingRequest.getAccountId());
            if (account == null) {
                response.setMessage("Account resource does not exist");
                return response;
            }

            if (meterReadingRecordService.hasMeterReading(account.getAccountId(), billingMonth.getBillingMonthId())) {
                if (meterReadingRecordService.isBilled(account.getAccountId(), billingMonth.getBillingMonthId())) {
                    response.setMessage("Account already being billed");
                    return response;
                } else {
                    meterReadingRecordService.removeRecord(account.getAccountId(), billingMonth.getBillingMonthId());
                }
            }

            MeterReading meterReading = new MeterReading();
            meterReading.setAccount(account);
            meterReading.setBillingMonth(billingMonth);
            meterReading.setReadBy(userService.getByEmailAddress(request.getUser().getEmail()));
            meterReading.setBilled(Boolean.FALSE);
            meterReading.setReadOn(new DateTime(meterReadingRequest.getReadOn()));
            meterReading.setCurrentReading(meterReadingRequest.getReading());
            meterReading.setImagePath(meterReadingRecordService.getImagePath(meterReading));

            Double previousReading = 0.0;
            Double unitsConsumed = 0.0;
            Double unitsBilled = 0.0;
            Double amountBilled = 0.0;
            String consumptionType = "Actual";

            Bill lastBill = billService.getAccountLastBill(account.getAccountId());
            if (lastBill != null) {
                previousReading = lastBill.getCurrentReading();
            }

            meterReading.setPreviousReading(previousReading);
            unitsConsumed = meterReading.getCurrentReading() - meterReading.getPreviousReading();
            Integer billOnAverageUnits = billService.getMinimumAverageUnitsToBill();
            if (unitsConsumed > billOnAverageUnits) {
                meterReading.setConsumptionType("Actual");
                unitsBilled = unitsConsumed;
            } else {
                meterReading.setConsumptionType("Average");
                unitsBilled = account.getAverageConsumption();
            }

            BillMeta billMeta = new BillMeta();
            billMeta.setUnits(unitsBilled);
            billMeta = tariffService.calculate(billMeta, account.getAccountId());

            meterReading.setAmountBilled(billMeta.getAmount());
            meterReading.setUnitsBilled(unitsBilled);
            meterReading.setUnitsConsumed(unitsConsumed);

            if (!meterReadingRecordService.saveImage(meterReading, request.getImagePayload())) {
                response.setMessage("We could not save meter reading data. Please contact your admin");
                return response;
            } else {
                meterReading = meterReadingRecordService.addRecord(meterReading);
                response.setMessage("We could not save meter reading data. Please contact your admin");
                response.setError(Boolean.FALSE);
                meterReadingRequest.setUploaded(1);
                response.setObject(meterReadingRequest);
                return response;
            }
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void processReadings() {
        Boolean autoBill = Boolean.valueOf(optionService.getOption("AUTO_BILL").getValue());
        if (autoBill) {
            List<Long> meterReadingIds = meterReadingRecordService.getPendingReadings();
            if (!meterReadingIds.isEmpty()) {
                for (Long meterReadingId : meterReadingIds) {
                    billAccount(meterReadingId);
                }
            }
        }
    }

    @Transactional
    private void billAccount(Long meterReadingId) {
        MeterReading meterReading = meterReadingRecordService.getByMeterReadingId(meterReadingId);
        if (meterReading != null) {
            BillRequest billRequest = new BillRequest();
            billRequest.setCurrentReading(meterReading.getCurrentReading());

            Double previousReading = billingService.getLastMeterReading(meterReading.getAccount().getAccountId());
            billRequest.setPreviousReading(previousReading);
            List<BillItemType> billItemTypeList = new ArrayList<>();

            Charge charge = chargeService.get(meterReading.getAccount().getAccountId(), meterReading.getBillingMonth().getBillingMonthId());
            if (charge != null) {
                for (ChargeItem chargeItem : charge.getChargeItems()) {
                    billItemTypeList.add(chargeItem.getBillItemType());
                }
            }

            billRequest.setBillItemTypes(billItemTypeList);

            //set previous reading
            Response response = billValidator.create(billRequest, meterReading.getAccount().getAccountId());
            if (response.hasError()) {
                meterReading.setBilled(Boolean.FALSE);
                meterReading.setResponse(response.getMessage());
                meterReading.setBilledBy(meterReading.getReadBy());
                meterReading.setProcessed(Boolean.TRUE);
                meterReading.setBilledOn(new DateTime());
                meterReadingRecordService.save(meterReading);
            } else {
                Bill bill = billingService.create(billRequest, meterReading.getAccount().getAccountId());
                if (bill != null) {
                    meterReading.setBilled(Boolean.TRUE);
                    meterReading.setResponse("Billed");
                    meterReading.setBilledBy(meterReading.getReadBy());
                    meterReading.setProcessed(Boolean.TRUE);
                    meterReading.setBilledOn(new DateTime());
                    meterReading.setBill(bill);
                    meterReadingRecordService.save(meterReading);
                }
            }
        }
    }
}
