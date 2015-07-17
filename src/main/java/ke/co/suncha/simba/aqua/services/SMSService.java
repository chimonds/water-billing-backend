package ke.co.suncha.simba.aqua.services;

import ke.co.suncha.simba.admin.helpers.AuditOperation;
import ke.co.suncha.simba.admin.models.AuditRecord;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.AuditService;
import ke.co.suncha.simba.admin.service.SimbaOptionService;
import ke.co.suncha.simba.admin.utils.Config;
import ke.co.suncha.simba.aqua.models.*;
import ke.co.suncha.simba.aqua.repository.*;
import ke.co.suncha.simba.aqua.utils.SMSNotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.json.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by manyala on 5/26/15.
 */
@Service
public class SMSService {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ConsumerRepository consumerRepository;

    @Autowired
    private SMSRepository smsRepository;

    @Autowired
    private SimbaOptionService optionService;

    @Autowired
    private SMSGroupRepository smsGroupRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private SMSTemplateRepository smsTemplateRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BillingMonthRepository billingMonthRepository;

    @Autowired
    private AuthManager authManager;

    @Autowired
    CounterService counterService;

    @Autowired
    GaugeService gaugeService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private ZoneRepository zoneRepository;

    private Boolean processingSMS = false;

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    final Integer INITIAL_DELAY_SUMMARY_REPORTS = 0;

    public SMSService() {
        //ge
    }

    public String getMonthYear() {
        SimpleDateFormat format1 = new SimpleDateFormat("MMM_yyyy_");
        String bmonth = format1.format(Calendar.getInstance().getTime());
        return bmonth.toUpperCase();
    }

    //send payment summary reports @8 oclock & 4 oclock
    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void sendSummaryReports() {

    }

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void process() {
        sendSMS();
        //populateSMSGroupSMSs();
    }

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void explode() {

        try {
            List<SMSGroup> smsGroups = smsGroupRepository.findAllByApprovedAndExplodedAndFromSystem(true, false, false);
            if (smsGroups.isEmpty()) {
                return;
            }
            for (SMSGroup smsGroup : smsGroups) {
                log.info("Exploding:" + smsGroup.getName());
                Boolean exploded = false;

                List<SMS> smsList = new ArrayList<>();

                //add all in zones
                List<Zone> zones = smsGroup.getZones();
                if (!zones.isEmpty()) {
                    for (Zone zone : zones) {
                        List<Account> accounts = accountRepository.findAllByZone(zone);
                        if (!accounts.isEmpty()) {
                            for (Account account : accounts) {

                                if (account.getConsumer() != null) {
                                    if (account.getConsumer().getPhoneNumber() != null) {
                                        //set
                                        if (account.getConsumer().getPhoneNumber().length() > 0) {
                                            SMS sms = new SMS();

                                            //set message
                                            String message = this.parseSMS(smsGroup.getSmsTemplate().getMessage(), account.getAccountId(), 0L, 0L);
                                            sms.setMessage(message);
                                            sms.setMobileNumber(account.getConsumer().getPhoneNumber());
                                            sms.setSmsGroup(smsGroup);
                                            smsList.add(sms);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                //add all in contacts
                if (!smsGroup.getContacts().isEmpty()) {
                    for (Contact contact : smsGroup.getContacts()) {
                        SMS sms = new SMS();
                        sms.setMessage(smsGroup.getSmsTemplate().getMessage());
                        sms.setMobileNumber(contact.getText());
                        sms.setSmsGroup(smsGroup);
                        smsList.add(sms);
                    }
                }

                //add all


                if (!smsList.isEmpty()) {
                    smsRepository.save(smsList);
                    smsGroup.setExploded(true);
                    smsGroup.setMessages(smsList.size());
                    smsGroupRepository.save(smsGroup);
                } else {
                    if (smsGroup.getApproved()) {
                        smsGroup.setExploded(true);
                        smsGroupRepository.save(smsGroup);
                    }
                }
            }
        } catch (Exception ex) {
            //ex.printStackTrace();
            log.error(ex.getMessage());
        }
    }

    public String formatNumber(Double value) {
        NumberFormat formatter = new DecimalFormat("###,###.###");
        return formatter.format(value);
    }

    //TODO;
    //TO REMOVE
    //@Scheduled(fixedDelay = 300)
    //@PostConstruct
    //@Transactional
    public void updateMissingSMSForJuneBilling() {
        log.info("Getting messages for june billing");
        try {

            BillingMonth billingMonth = billingMonthRepository.findOne(173L);
            if (billingMonth == null) {
                return;
            }

            List<Bill> bills = billRepository.findAllByBillingMonth_BillingMonthId(billingMonth.getBillingMonthId());


            if (bills.isEmpty()) {
                return;
            }

            log.info(bills.size() + " bills found for june.");

            for (Bill bill : bills) {

                try {
                    //check if notification already exists
                    Long accountId = billRepository.findAccountIdByBillId(bill.getBillId());

                    Account account = accountRepository.findOne(accountId);

                    String notification = "";
                    if (account.getOutstandingBalance() == 0) {
                        notification = Config.SMS_NOTIFICATION_BILL_ACCOUNT_WITH_ZERO_BALANCE;
                    } else if (account.getOutstandingBalance() > 0) {
                        notification = Config.SMS_NOTIFICATION_BILL_ACCOUNT_WITH_BALANCE;
                    } else if (account.getOutstandingBalance() < 0) {
                        notification = Config.SMS_NOTIFICATION_BILL_ACCOUNT_CREDIT_BALANCE;
                    }

                    String monthYear = this.getMonthYear() + account.getZone().getName() + "_";
                    notification = monthYear + notification;

                    //set sms group
                    SMSGroup smsGroup = smsGroupRepository.findByName(notification);
                    if (smsGroup != null) {

                        Long consumerId = accountRepository.findConsumerIdByAccountId(accountId);
                        Consumer consumer = consumerRepository.findOne(consumerId);

                        if (!consumer.getPhoneNumber().isEmpty()) {
                            SMS sms = smsRepository.findBySmsGroupAndMobileNumber(smsGroup, consumer.getPhoneNumber());
                            if (sms == null) {
                                this.saveNotification(account.getAccountId(), 0L, bill.getBillId(), SMSNotificationType.BILL);
                            }
                        }
                    }
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }


    @Transactional
    private String parseSMS(String message, Long accountId, Long paymentId, Long billId) {
        //$account_no
        //$balance
        //$receiptno
        //$receipt_amount
        //$billing_month
        //$bill_amount
        //$firstname

        Account account = accountRepository.findOne(accountId);
        if (account == null) {
            return message;
        }

        //$account
        if (message.contains("$account_no")) {
            message = message.replace("$account_no", account.getAccNo());
        }

        //$balance
        if (message.contains("$balance")) {
            //calculate balance
            message = message.replace("$balance", this.formatNumber(account.getOutstandingBalance()));
        }

        //Payment placeholders

        //$receiptno
        Payment payment = paymentRepository.findOne(paymentId);
        if (payment != null) {
            if (message.contains("$receipt_no")) {
                message = message.replace("$receipt_no", payment.getReceiptNo().toUpperCase());
            }
            if (message.contains("$receipt_amount")) {
                message = message.replace("$receipt_amount", this.formatNumber(payment.getAmount()));
            }
        }

        //$billing_month
        BillingMonth billingMonth = billingMonthRepository.findByCurrent(1);
        if (billingMonth != null) {
            SimpleDateFormat format1 = new SimpleDateFormat("MMM, yyyy");
            String bmonth = format1.format(billingMonth.getMonth().getTime());
            message = message.replace("$billing_month", bmonth);
        }

        //$bill_amount
        Bill bill = billRepository.findOne(billId);

        if (bill != null) {
            if (message.contains("$bill_amount")) {
                message = message.replace("$bill_amount", this.formatNumber(bill.getTotalBilled()));
            }
        }

        //$firstname
        if (message.contains("$firstname")) {
            message = message.replace("$firstname", account.getConsumer().getFirstName());
        }

        return message;
    }

    @Transactional
    public void saveNotification(Long accountId, Long paymentId, Long billId, SMSNotificationType smsNotificationType) {


        SMS sms = new SMS();
        Account account = accountRepository.findOne(accountId);
        if (account.getConsumer() == null) {
            return;
        }

        Long consumerId = accountRepository.findConsumerIdByAccountId(accountId);
        Consumer consumer = consumerRepository.findOne(consumerId);

        if (consumer == null) {
            return;
        }

        if (consumer.getPhoneNumber() == null) {
            return;
        }

        if (consumer.getPhoneNumber().length() != 10) {
            log.error("Invalid account notification number:" + consumer.getPhoneNumber());
        }

        //set mobile number
        sms.setMobileNumber(consumer.getPhoneNumber());


        String notification = "";
        if (smsNotificationType == SMSNotificationType.BILL) {
            if (account.getOutstandingBalance() == 0) {
                //
                notification = Config.SMS_NOTIFICATION_BILL_ACCOUNT_WITH_ZERO_BALANCE;
            } else if (account.getOutstandingBalance() > 0) {
                notification = Config.SMS_NOTIFICATION_BILL_ACCOUNT_WITH_BALANCE;
            } else if (account.getOutstandingBalance() < 0) {
                notification = Config.SMS_NOTIFICATION_BILL_ACCOUNT_CREDIT_BALANCE;
            }
        } else if (smsNotificationType == SMSNotificationType.PAYMENT) {
            if (account.getOutstandingBalance() == 0) {
                notification = Config.SMS_NOTIFICATION_PAYMENT_ACCOUNT_WITH_ZERO_BALANCE;
            } else if (account.getOutstandingBalance() > 0) {
                notification = Config.SMS_NOTIFICATION_PAYMENT_ACCOUNT_WITH_BALANCE;
            } else if (account.getOutstandingBalance() < 0) {
                notification = Config.SMS_NOTIFICATION_PAYMENT_ACCOUNT_CREDIT_BALANCE;
            }
        }


        if (smsNotificationType == SMSNotificationType.BILL) {
            String monthYear = this.getMonthYear() + account.getZone().getName() + "_";
            notification = monthYear + notification;
        } else if (smsNotificationType == SMSNotificationType.PAYMENT) {
            notification = this.getMonthYear() + notification;
        }


        //set sms group
        SMSGroup smsGroup = smsGroupRepository.findByName(notification);
        if (smsGroup == null) {
            return;
        }

        String message = this.parseSMS(smsGroup.getSmsTemplate().getMessage(), accountId, paymentId, billId);

        //set message
        sms.setMessage(message);

        //save sms
        sms = smsRepository.save(sms);
        sms.setSmsGroup(smsGroup);
        sms = smsRepository.save(sms);
    }

    /**
     * Send SMSs
     */
    private void sendSMS() {
        try {
            Boolean sendMSgs = false;
            try {
                sendMSgs = Boolean.parseBoolean(optionService.getOption("SMS_ENABLE").getValue());
            } catch (Exception ex) {

            }
            if (!sendMSgs) {
                return;
            }

            List<SMS> smsList = smsRepository.findAllBySend(false);
            if (smsList.isEmpty()) {
                return;
            }

            log.info("Processing:" + smsList.size() + " SMSs");
            String sms_username = optionService.getOption("SMS_USERNAME").getValue();
            String sms_api_key = optionService.getOption("SMS_API_KEY").getValue();
            String sms_sender_id = optionService.getOption("SMS_SENDER_ID").getValue();
            AfricasTalkingGateway gateway = new AfricasTalkingGateway(sms_username, sms_api_key);
            Integer smsSent = 0;
            for (SMS sms : smsList) {
                if (sms.getSmsGroup().getApproved()) {
                    try {
                        if (sms.getMobileNumber().length() != 10) {
                            log.error("SMS not sent. Invalid mobile number:" + sms.getMobileNumber());
                        } else {
                            log.info("Sending SMS to:" + sms.getMobileNumber());

                            JSONArray results = gateway.sendMessage(sms.getMobileNumber(), sms.getMessage(), sms_sender_id, 1);
                            JSONObject result = results.getJSONObject(0);
                            String status = result.getString("status");
                            if (status.compareTo("Success") == 0) {
                                sms.setDateSend(Calendar.getInstance());
                                sms.setSend(true);
                                smsRepository.save(sms);
                                log.info("SMS sent:" + sms.getMessage());
                                smsSent++;
                            } else {
                                log.error("Unable to send SMS to:" + sms.getMobileNumber());
                                log.error(status);
                            }
                        }
                    } catch (Exception ex) {
                        log.error(ex.getMessage());
                    }

                    log.info("ALL SMS's:" + smsList.size());
                    log.info("Sent SMS's:" + smsSent);
                    log.info("Not Sent SMS's:" + (smsList.size() - smsSent));
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    @Transactional
    public RestResponse create(RestRequestObject<SMSGroup> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {

                response = authManager.grant(requestObject.getToken(), "sms_create");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                SMSGroup smsGroup = requestObject.getObject();

                //check if sms group already exists
                SMSGroup smsGroup1 = smsGroupRepository.findByName(smsGroup.getName());

                if (smsGroup1 != null) {
                    responseObject.setMessage("SMS already exists");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                //check if message name is empty
                if (smsGroup.getName().isEmpty()) {
                    responseObject.setMessage("Message name can not be empty");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                //check if message template is empty
                if (smsGroup.getSmsTemplate() == null) {
                    responseObject.setMessage("Message template can not be empty");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                //check if zones is empty
                if (smsGroup.getZones().isEmpty() && smsGroup.getContacts().isEmpty()) {
                    responseObject.setMessage("Please select at least one recipient");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                String errorMessage = "";
                Boolean validZones = true;
                for (Zone zone : smsGroup.getZones()) {
                    if (zoneRepository.findByName(zone.getName()) == null) {
                        validZones = false;
                        errorMessage += zone.getName() + " zone invalid ";
                    }
                }

                Boolean validMobileNumbers = true;

                if (!smsGroup.getContacts().isEmpty()) {
                    for (Contact contact : smsGroup.getContacts()) {

                        if (contact.getText().matches("\\d{10}")) {
                            if (!contact.getText().startsWith("07")) {
                                validMobileNumbers = false;
                                errorMessage += contact.getText() + " invalid. ";
                            }
                        } else {
                            validMobileNumbers = false;
                            errorMessage += contact.getText() + " invalid. ";
                        }
                    }
                }

                if (!validMobileNumbers || !validZones) {
                    responseObject.setMessage(errorMessage);
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                // create resource
                SMSGroup created = new SMSGroup();
                created.setName(smsGroup.getName());
                created = smsGroupRepository.save(created);

                if (!smsGroup.getContacts().isEmpty()) {
                    List<Contact> contacts = new ArrayList<>();
                    for (Contact contact : smsGroup.getContacts()) {
                        contact.setSmsGroup(created);
                        contacts.add(contact);
                    }
                    created.setContacts(contacts);
                }

                created.setSmsTemplate(smsGroup.getSmsTemplate());
                created.setZones(smsGroup.getZones());
                //save other stuff
                created = smsGroupRepository.save(created);


                // package response
                responseObject.setMessage("SMS created successfully. ");
                responseObject.setPayload(created);
                response = new RestResponse(responseObject, HttpStatus.CREATED);

                //Start - audit trail
                AuditRecord auditRecord = new AuditRecord();
                auditRecord.setParentID(String.valueOf(created.getSmsGroupId()));
                auditRecord.setCurrentData(created.toString());
                auditRecord.setParentObject("SMSGroup");
                auditRecord.setNotes("CREATED SMS");
                auditService.log(AuditOperation.CREATED, auditRecord);
                //End - audit trail
                return response;

            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    @Transactional
    public RestResponse approve(RestRequestObject<SMSGroup> requestObject, Long smsGroupId) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {

                response = authManager.grant(requestObject.getToken(), "sms_approve");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }


                //check if sms group already exists
                SMSGroup smsGroup = smsGroupRepository.findOne(smsGroupId);

                if (smsGroup == null) {
                    responseObject.setMessage("SMS does not exists");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                if (smsGroup.getApproved()) {
                    responseObject.setMessage("SMS has already been approved/rejected");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                SMSGroup smsGroup1 = requestObject.getObject();

                //check if message name is empty
                if (smsGroup1.getStatus().isEmpty()) {
                    responseObject.setMessage("Message status can not be empty");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                if (smsGroup1.getNotes().isEmpty()) {
                    responseObject.setMessage("Message notes can not be empty");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                String status = smsGroup1.getStatus();
                //log.info("Approve status:" + status);
                if (status.compareToIgnoreCase("Approve") == 0) {
                    smsGroup.setApprovalLevel(1);
                    smsGroup.setStatus("Approved");
                } else if (status.compareToIgnoreCase("Reject") == 0) {
                    smsGroup.setApprovalLevel(2);
                    smsGroup.setStatus("Rejected");
                } else {
                    responseObject.setMessage("Invalid message approval/rejection status");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }
                smsGroup.setNotes(smsGroup1.getNotes());
                //set approved
                smsGroup.setApproved(true);
                smsGroup = smsGroupRepository.save(smsGroup);

                // package response
                responseObject.setMessage("SMS created successfully. ");
                responseObject.setPayload(smsGroup);
                response = new RestResponse(responseObject, HttpStatus.CREATED);

                //Start - audit trail
                AuditRecord auditRecord = new AuditRecord();
                auditRecord.setParentID(String.valueOf(smsGroup.getSmsGroupId()));
                auditRecord.setCurrentData(smsGroup.toString());
                auditRecord.setParentObject("SMSGroup Approve/Reject");
                auditRecord.setNotes("Approve/Reject SMSGroup");
                auditService.log(AuditOperation.UPDATED, auditRecord);
                //End - audit trail
                return response;

            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    private Sort sortByDateAddedDesc() {
        return new Sort(Sort.Direction.DESC, "createdOn");
    }

    public RestResponse getAllSMSGroupsByFilter(RestRequestObject<RestPageRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "sms_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                RestPageRequest p = requestObject.getObject();
                Page<SMSGroup> page;
                if (p.getFilter().isEmpty()) {
                    page = smsGroupRepository.findAll(new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));
                } else {
                    page = smsGroupRepository.findAllByNameContains(p.getFilter(), new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));
                }

                if (page.hasContent()) {
                    responseObject.setMessage("Fetched data successfully");
                    responseObject.setPayload(page);
                    response = new RestResponse(responseObject, HttpStatus.OK);
                } else {
                    responseObject.setMessage("Your search did not match any records");
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

    public RestResponse getAllSMSByFilter(RestRequestObject<RestPageRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "sms_view");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                RestPageRequest p = requestObject.getObject();
                Page<SMS> page;
                if (p.getFilter().isEmpty()) {
                    page = smsRepository.findAll(new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));
                } else {
                    page = smsRepository.findAllByMessageContains(p.getFilter(), new PageRequest(p.getPage(), p.getSize(), sortByDateAddedDesc()));
                }

                if (page.hasContent()) {
                    responseObject.setMessage("Fetched data successfully");
                    responseObject.setPayload(page);
                    response = new RestResponse(responseObject, HttpStatus.OK);
                } else {
                    responseObject.setMessage("Your search did not match any records");
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


    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void populateDefaultPaymentNotifications() {
        try {
            //SMS notification account with balances
            try {
                SMSGroup smsGroup = smsGroupRepository.findByName(getMonthYear() + Config.SMS_NOTIFICATION_PAYMENT_ACCOUNT_WITH_BALANCE);
                if (smsGroup == null) {
                    smsGroup = new SMSGroup();
                    smsGroup.setName(getMonthYear() + Config.SMS_NOTIFICATION_PAYMENT_ACCOUNT_WITH_BALANCE);
                    smsGroup.setApproved(true);
                    smsGroup.setStatus("Approved");
                    smsGroup.setFromSystem(true);
                    smsGroup.setExploded(true);
                    smsGroup = smsGroupRepository.save(smsGroup);


                    //create template
                    SMSTemplate smsTemplate = smsTemplateRepository.findByName(Config.SMS_TEMPLATE_PAYMENT_ACCOUNT_WITH_BALANCE);
                    if (smsTemplate == null) {
                        smsTemplate = new SMSTemplate();
                        smsTemplate.setName(Config.SMS_TEMPLATE_PAYMENT_ACCOUNT_WITH_BALANCE);
                        smsTemplate.setMessage(Config.SMS_NOTIFICATION_PAYMENT_DEFAULT);
                        smsTemplate = smsTemplateRepository.save(smsTemplate);
                    }
                    smsGroup.setSmsTemplate(smsTemplate);
                    smsGroup = smsGroupRepository.save(smsGroup);
                }
            } catch (Exception ex) {

            }

            //payment notification account with zero balance
            try {
                SMSGroup smsGroup = smsGroupRepository.findByName(getMonthYear() + Config.SMS_NOTIFICATION_PAYMENT_ACCOUNT_WITH_ZERO_BALANCE);
                if (smsGroup == null) {
                    smsGroup = new SMSGroup();
                    smsGroup.setName(getMonthYear() + Config.SMS_NOTIFICATION_PAYMENT_ACCOUNT_WITH_ZERO_BALANCE);
                    smsGroup.setApproved(true);
                    smsGroup.setStatus("Approved");
                    smsGroup.setFromSystem(true);
                    smsGroup.setExploded(true);
                    smsGroup = smsGroupRepository.save(smsGroup);


                    //create template
                    SMSTemplate smsTemplate = smsTemplateRepository.findByName(Config.SMS_TEMPLATE_PAYMENT_ACCOUNT_WITH_ZERO_BALANCE);
                    if (smsTemplate == null) {
                        smsTemplate = new SMSTemplate();
                        smsTemplate.setName(Config.SMS_TEMPLATE_PAYMENT_ACCOUNT_WITH_ZERO_BALANCE);
                        smsTemplate.setMessage(Config.SMS_NOTIFICATION_PAYMENT_DEFAULT);
                        smsTemplate = smsTemplateRepository.save(smsTemplate);
                    }
                    smsGroup.setSmsTemplate(smsTemplate);
                    smsGroup = smsGroupRepository.save(smsGroup);

                }
            } catch (Exception ex) {

            }

            //payment notification account with credit balance
            try {
                SMSGroup smsGroup = smsGroupRepository.findByName(getMonthYear() + Config.SMS_NOTIFICATION_PAYMENT_ACCOUNT_CREDIT_BALANCE);
                if (smsGroup == null) {
                    smsGroup = new SMSGroup();
                    smsGroup.setName(getMonthYear() + Config.SMS_NOTIFICATION_PAYMENT_ACCOUNT_CREDIT_BALANCE);
                    smsGroup.setStatus("Approved");
                    smsGroup.setFromSystem(true);
                    smsGroup.setExploded(true);
                    smsGroup.setApproved(true);
                    smsGroup = smsGroupRepository.save(smsGroup);


                    //create template
                    SMSTemplate smsTemplate = smsTemplateRepository.findByName(Config.SMS_TEMPLATE_PAYMENT_ACCOUNT_WITH_CREDIT_BALANCE);
                    if (smsTemplate == null) {
                        smsTemplate = new SMSTemplate();
                        smsTemplate.setName(Config.SMS_TEMPLATE_PAYMENT_ACCOUNT_WITH_CREDIT_BALANCE);
                        smsTemplate.setMessage(Config.SMS_NOTIFICATION_PAYMENT_DEFAULT);
                        smsTemplate = smsTemplateRepository.save(smsTemplate);
                    }
                    smsGroup.setSmsTemplate(smsTemplate);
                    smsGroup = smsGroupRepository.save(smsGroup);
                }
            } catch (Exception ex) {

            }

        } catch (Exception ex) {
        }
    }

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void populateDefaultBillingNotifications() {
        try {
            List<Zone> zones = zoneRepository.findAll();
            if (!zones.isEmpty()) {
                for (Zone zone : zones) {
                    String monthYear = this.getMonthYear() + zone.getName() + "_";
                    try {
                        SMSGroup smsGroup = smsGroupRepository.findByName(monthYear + Config.SMS_NOTIFICATION_BILL_ACCOUNT_WITH_BALANCE);
                        if (smsGroup == null) {
                            smsGroup = new SMSGroup();
                            smsGroup.setName(monthYear + Config.SMS_NOTIFICATION_BILL_ACCOUNT_WITH_BALANCE);
                            smsGroup.setApproved(false);
                            //smsGroup.setStatus("Approved");
                            //smsGroup.setFromSystem(true);
                            //smsGroup.setExploded(true);
                            smsGroup = smsGroupRepository.save(smsGroup);

                            //create template
                            SMSTemplate smsTemplate = smsTemplateRepository.findByName(Config.SMS_TEMPLATE_BILL_ACCOUNT_WITH_BALANCE);
                            if (smsTemplate == null) {
                                smsTemplate = new SMSTemplate();
                                smsTemplate.setName(Config.SMS_TEMPLATE_BILL_ACCOUNT_WITH_BALANCE);
                                smsTemplate.setMessage(Config.SMS_NOTIFICATION_BILL_DEFAULT);
                                smsTemplate = smsTemplateRepository.save(smsTemplate);
                            }
                            smsGroup.setSmsTemplate(smsTemplate);
                            smsGroup = smsGroupRepository.save(smsGroup);
                        }
                    } catch (Exception ex) {

                    }


                    try {
                        SMSGroup smsGroup = smsGroupRepository.findByName(monthYear + Config.SMS_NOTIFICATION_BILL_ACCOUNT_WITH_ZERO_BALANCE);
                        if (smsGroup == null) {
                            smsGroup = new SMSGroup();
                            smsGroup.setName(monthYear + Config.SMS_NOTIFICATION_BILL_ACCOUNT_WITH_ZERO_BALANCE);
//                    smsGroup.setApproved(false);
//                    smsGroup.setStatus("Approved");
//                    smsGroup.setFromSystem(true);
//                    smsGroup.setExploded(true);
                            smsGroup = smsGroupRepository.save(smsGroup);

                            //smsGroup.setApproved(true);


                            //create template
                            SMSTemplate smsTemplate = smsTemplateRepository.findByName(Config.SMS_TEMPLATE_BILL_ACCOUNT_WITH_ZERO_BALANCE);
                            if (smsTemplate == null) {
                                smsTemplate = new SMSTemplate();
                                smsTemplate.setName(Config.SMS_TEMPLATE_BILL_ACCOUNT_WITH_ZERO_BALANCE);
                                smsTemplate.setMessage(Config.SMS_NOTIFICATION_BILL_DEFAULT);
                                smsTemplate = smsTemplateRepository.save(smsTemplate);
                            }
                            smsGroup.setSmsTemplate(smsTemplate);
                            smsGroup = smsGroupRepository.save(smsGroup);
                        }
                    } catch (Exception ex) {

                    }

                    try {
                        SMSGroup smsGroup = smsGroupRepository.findByName(monthYear + Config.SMS_NOTIFICATION_BILL_ACCOUNT_CREDIT_BALANCE);
                        if (smsGroup == null) {
                            smsGroup = new SMSGroup();
                            smsGroup.setName(monthYear + Config.SMS_NOTIFICATION_BILL_ACCOUNT_CREDIT_BALANCE);
//                    smsGroup.setApproved(false);
//                    smsGroup.setStatus("Approved");
//                    smsGroup.setFromSystem(true);
//                    smsGroup.setExploded(true);
                            smsGroup = smsGroupRepository.save(smsGroup);

                            //smsGroup.setApproved(true);

                            //create template
                            SMSTemplate smsTemplate = smsTemplateRepository.findByName(Config.SMS_TEMPLATE_BILL_ACCOUNT_WITH_CREDIT_BALANCE);
                            if (smsTemplate == null) {
                                smsTemplate = new SMSTemplate();
                                smsTemplate.setName(Config.SMS_TEMPLATE_BILL_ACCOUNT_WITH_CREDIT_BALANCE);
                                smsTemplate.setMessage(Config.SMS_NOTIFICATION_BILL_DEFAULT);
                                smsTemplate = smsTemplateRepository.save(smsTemplate);
                            }
                            smsGroup.setSmsTemplate(smsTemplate);
                            smsGroup = smsGroupRepository.save(smsGroup);
                        }
                    } catch (Exception ex) {

                    }
                }
            }
        } catch (Exception ex) {
        }
    }

}
