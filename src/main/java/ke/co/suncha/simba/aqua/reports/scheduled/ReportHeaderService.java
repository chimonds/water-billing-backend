package ke.co.suncha.simba.aqua.reports.scheduled;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import ke.co.suncha.simba.admin.helpers.AuditOperation;
import ke.co.suncha.simba.admin.models.AuditRecord;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.service.AuditService;
import ke.co.suncha.simba.admin.service.SimbaOptionService;
import ke.co.suncha.simba.aqua.account.scheme.Scheme;
import ke.co.suncha.simba.aqua.account.scheme.SchemeRepository;
import ke.co.suncha.simba.aqua.models.*;
import ke.co.suncha.simba.aqua.reports.ReportObject;
import ke.co.suncha.simba.aqua.repository.AccountRepository;
import ke.co.suncha.simba.aqua.repository.AgeingDataRepository;
import ke.co.suncha.simba.aqua.repository.ZoneRepository;
import ke.co.suncha.simba.aqua.services.AccountService;
import ke.co.suncha.simba.aqua.services.BillService;
import ke.co.suncha.simba.aqua.services.PaymentService;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by maitha.manyala on 8/1/16.
 */
@Service
@Transactional
public class ReportHeaderService {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    SchemeRepository schemeRepository;

    @Autowired
    ZoneRepository zoneRepository;

    @Autowired
    ReportHeaderRepository reportHeaderRepository;

    @Autowired
    AuthManager authManager;

    @Autowired
    AuditService auditService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountBalanceRecordRepository accountBalanceRecordRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    EntityManager entityManager;

    @Autowired
    PaymentService paymentService;

    @Autowired
    BillService billService;

    @Autowired
    SimbaOptionService optionService;

    @Autowired
    AgeingDataRepository ageingDataRepository;

    public BlockingQueue<ReportHeader> reportHeaderBlockingQueue = new ArrayBlockingQueue<ReportHeader>(100);

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public ReportHeaderService() {
    }

    @PostConstruct
    public void initReportHeaderQueue() {
        try {
            BooleanBuilder builder = new BooleanBuilder();
            builder.and(QReportHeader.reportHeader.status.eq(ReportStatus.PENDING));
            Iterable<ReportHeader> reportHeaders = reportHeaderRepository.findAll(builder, QReportHeader.reportHeader.createdOn.asc());
            for (ReportHeader reportHeader : reportHeaders) {
                if (!reportHeaderBlockingQueue.contains(reportHeader)) {
                    reportHeaderBlockingQueue.put(reportHeader);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 1000)
    public void pollQueue() {
        try {
            if (!reportHeaderBlockingQueue.isEmpty()) {
                processReportHeader(reportHeaderBlockingQueue.take());
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Transactional
    public void processReportHeader(ReportHeader reportHeader) {
        try {
            if (reportHeader != null) {
                if (reportHeader.getReportType() == ReportType.ACCOUNT_BALANCES) {
                    processAccountBalanceRequest(reportHeader);
                } else if (reportHeader.getReportType() == ReportType.AGEING) {
                    processAgeingBalanceRequest(reportHeader);
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    private String getTimeTaken(DateTime start, DateTime end) {
        Period period = new Period(start, end);
        PeriodFormatter formatter = new PeriodFormatterBuilder().appendMinutes().appendSuffix(" min ").appendSeconds().appendSuffix(" sec").appendHours().appendSuffix(" hrs").appendDays().appendSuffix(" days").appendWeeks().appendSuffix(" weeks").appendMonths().appendSuffix(" months").appendYears().appendSuffix(" years").printZeroNever().toFormatter();
        String elapsed = formatter.print(period);
        return elapsed;
    }

    @Transactional
    private void processAgeingBalanceRequest(ReportHeader reportHeader) {
        try {
            DateTime startTime = new DateTime();
            reportHeader.setStatus(ReportStatus.PROCESSING);
            reportHeader = reportHeaderRepository.save(reportHeader);

            BooleanBuilder builder = new BooleanBuilder();
            if (reportHeader.getOnStatus() != null) {
                builder.and(QAccount.account.onStatus.eq(reportHeader.getOnStatus()));
            }

            if (reportHeader.getCutOff() != null) {
                if (reportHeader.getCutOff() == 1) {
                    builder.and(QAccount.account.active.eq(Boolean.TRUE));
                } else if (reportHeader.getCutOff() == 2) {
                    builder.and(QAccount.account.active.eq(Boolean.FALSE));
                }
            }

            if (reportHeader.getZone() != null) {
                builder.and(QAccount.account.zone.zoneId.eq(reportHeader.getZone().getZoneId()));
            } else if (reportHeader.getScheme() != null) {
                builder.and(QAccount.account.zone.scheme.schemeId.eq(reportHeader.getScheme().getSchemeId()));
            }

            Calendar createdOn = Calendar.getInstance();
            //DateTime created = reportHeader.getToDate().plusDays(1);
            DateTime created = reportHeader.getToDate().withZone(DateTimeZone.forID("Africa/Nairobi")).hourOfDay().withMaximumValue();


            createdOn.setTimeInMillis(created.getMillis());
            builder.and(QAccount.account.createdOn.loe(createdOn));

            JPAQuery query = new JPAQuery(entityManager);
            QAccount qAccount = QAccount.account;
            List<Long> accountIds = query.from(qAccount).where(builder).list(qAccount.accountId);
            if (!accountIds.isEmpty()) {
                log.info("Start************************************************************************");
                log.info("Generating scheduled ageing balances report, size:" + accountIds.size());
                log.info("Report as at: " + reportHeader.getToDate());
                for (Long accountId : accountIds) {
                    try {
                        accountService.updateAccountAgeingCustom(accountId, reportHeader.getToDate(), reportHeader);
                    } catch (Exception ex) {
                        log.error(ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }

            DateTime endTime = new DateTime();
            reportHeader.setTimeTaken(getTimeTaken(startTime, endTime));
            reportHeader.setStatus(ReportStatus.PROCESSED);
            reportHeaderRepository.save(reportHeader);
            log.info("Done processing ageing balances scheduled report....");
            log.info("End************************************************************************");
        } catch (Exception ex) {
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Transactional
    private void processAccountBalanceRequest(ReportHeader reportHeader) {
        try {
            DateTime startTime = new DateTime();
            reportHeader.setStatus(ReportStatus.PROCESSING);
            reportHeader = reportHeaderRepository.save(reportHeader);

            BooleanBuilder builder = new BooleanBuilder();
            if (reportHeader.getOnStatus() != null) {
                builder.and(QAccount.account.onStatus.eq(reportHeader.getOnStatus()));
            }

            if (reportHeader.getCutOff() != null) {
                if (reportHeader.getCutOff() == 1) {
                    builder.and(QAccount.account.active.eq(Boolean.TRUE));
                } else if (reportHeader.getCutOff() == 2) {
                    builder.and(QAccount.account.active.eq(Boolean.FALSE));
                }
            }

            if (reportHeader.getZone() != null) {
                builder.and(QAccount.account.zone.zoneId.eq(reportHeader.getZone().getZoneId()));
            } else if (reportHeader.getScheme() != null) {
                builder.and(QAccount.account.zone.scheme.schemeId.eq(reportHeader.getScheme().getSchemeId()));
            }

            Calendar createdOn = Calendar.getInstance();
            DateTime created = reportHeader.getToDate().withZone(DateTimeZone.forID("Africa/Nairobi")).hourOfDay().withMaximumValue();


            //DateTime created = reportHeader.getToDate().plusDays(1);

            createdOn.setTimeInMillis(created.getMillis());
            builder.and(QAccount.account.createdOn.loe(createdOn));

            JPAQuery query = new JPAQuery(entityManager);
            QAccount qAccount = QAccount.account;
            List<Long> accountIds = query.from(qAccount).where(builder).list(qAccount.accountId);
            if (!accountIds.isEmpty()) {
                log.info("Start************************************************************************");
                log.info("Generating scheduled account balances report, size:" + accountIds.size());
                log.info("Report as at" + reportHeader.getToDate());
                for (Long accountId : accountIds) {
                    try {
                        Double balanceBroughtForward = 0d;
                        String accNo = "";
                        String accountStatus = "Active";
                        String zone = "Not Available";
                        String consumerName = "";

                        query = new JPAQuery(entityManager);
                        BooleanBuilder accountBuilder = new BooleanBuilder();
                        accountBuilder.and(QAccount.account.accountId.eq(accountId));
                        List<Tuple> tuples = query.from(QAccount.account).where(accountBuilder).list(QAccount.account.balanceBroughtForward, QAccount.account.accNo, QAccount.account.active, QAccount.account.zone.name, QAccount.account.consumer.firstName, QAccount.account.consumer.middleName, QAccount.account.consumer.lastName);
                        if (!tuples.isEmpty()) {
                            Tuple tuple = tuples.get(0);
                            //account number
                            accNo = tuple.get(QAccount.account.accNo);
                            Double dbBalanceBroughtForward = tuple.get(QAccount.account.balanceBroughtForward);
                            if (dbBalanceBroughtForward != null) {
                                balanceBroughtForward += dbBalanceBroughtForward;
                            }

                            //Account status
                            Boolean active = tuple.get(QAccount.account.active);
                            if (!active) {
                                accountStatus = "Inactive";
                            }

                            //Zone
                            zone = tuple.get(QAccount.account.zone.name);

                            //consumer name
                            consumerName = tuple.get(QAccount.account.consumer.firstName) + " " + tuple.get(QAccount.account.consumer.middleName) + " " + tuple.get(QAccount.account.consumer.lastName);
                        }

                        AccountBalanceRecord accountBalanceRecord = new AccountBalanceRecord();
                        accountBalanceRecord.setAccountId(accountId);
                        accountBalanceRecord.setName(consumerName.replace("null", "").toUpperCase());
                        accountBalanceRecord.setAccNo(accNo);
                        accountBalanceRecord.setZone(zone);
                        accountBalanceRecord.setCutOff(accountStatus);
                        accountBalanceRecord.setReportHeader(reportHeader);

                        query = new JPAQuery(entityManager);
                        BooleanBuilder paymentBuilder = new BooleanBuilder();
                        paymentBuilder.and(QPayment.payment.account.accountId.eq(accountId));
                        paymentBuilder.and(QPayment.payment.transactionDate.loe(createdOn));
                        Double totalReceipts = query.from(QPayment.payment).where(paymentBuilder).singleResult(QPayment.payment.amount.sum());
                        if (totalReceipts == null) {
                            totalReceipts = 0d;
                        }

                        query = new JPAQuery(entityManager);
                        BooleanBuilder billsBuilder = new BooleanBuilder();
                        billsBuilder.and(QBill.bill.account.accountId.eq(accountId));
                        billsBuilder.and(QBill.bill.transactionDate.loe(createdOn));
                        Double totalBills = query.from(QBill.bill).where(billsBuilder).singleResult(QBill.bill.totalBilled.sum());
                        if (totalBills == null) {
                            totalBills = 0d;
                        }
                        totalBills += balanceBroughtForward;

                        Double balance = totalBills - totalReceipts;
                        accountBalanceRecord.setBalance(balance);
                        accountBalanceRecordRepository.save(accountBalanceRecord);
                    } catch (Exception ex) {
                        log.error(ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }

            DateTime endTime = new DateTime();
            reportHeader.setTimeTaken(getTimeTaken(startTime, endTime));
            reportHeader.setStatus(ReportStatus.PROCESSED);
            reportHeaderRepository.save(reportHeader);
            log.info("Done processing account balances scheduled report....");
            log.info("End************************************************************************");
        } catch (Exception ex) {
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Transactional
    public RestResponse createAccountBalancesRequest(RestRequestObject<ReportHeader> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_account_balances");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                String emailAddress = authManager.getEmailFromToken(requestObject.getToken());
                String[] emailData = emailAddress.split("@");
                if (emailData.length > 0) {
                    emailAddress = emailData[0];
                }
                ReportHeader reportHeader = requestObject.getObject();
                ReportHeader toSave = new ReportHeader();

                if (reportHeader.getToDate() == null) {
                    responseObject.setMessage("To date can not be empty");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                toSave.setToDate(reportHeader.getToDate().withZone(DateTimeZone.forID("Africa/Nairobi")));

                if (reportHeader.getOnStatus() == null) {
                    responseObject.setMessage("On status can not be empty");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }
                toSave.setOnStatus(reportHeader.getOnStatus());

                //Check if user has existing unprocessed requests
                Integer pendingRequests = reportHeaderRepository.countPendingByRequestor(emailAddress, ReportStatus.PENDING);
                if (pendingRequests > 0) {
                    responseObject.setMessage("Sorry you already have a  " + pendingRequests + " pending request(s). You can only submit another request when all your pending requests are processed. ");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                if (reportHeader.getCutOff() != null) {
                    toSave.setCutOff(reportHeader.getCutOff());
                }

                // create resource
                if (reportHeader.getScheme() != null) {
                    if (reportHeader.getScheme().getSchemeId() != null) {
                        Scheme scheme = schemeRepository.findOne(reportHeader.getScheme().getSchemeId());
                        if (scheme != null) {
                            reportHeader.setScheme(scheme);
                            toSave.setScheme(scheme);
                        }
                    }
                }

                if (reportHeader.getZone() != null) {
                    if (reportHeader.getZone().getZoneId() != null) {
                        Zone zone = zoneRepository.findOne(reportHeader.getZone().getZoneId());
                        if (zone != null) {
                            if (zone.getScheme().equals(schemeRepository.findOne(reportHeader.getScheme().getSchemeId()))) {
                                toSave.setZone(zone);
                            }
                        }
                    }
                }

                toSave.setReportType(ReportType.ACCOUNT_BALANCES);
                toSave.setRequestedBy(emailAddress);
                toSave.setCreatedOn(new DateTime());
                toSave = reportHeaderRepository.save(toSave);

                //add to queue
                if (!reportHeaderBlockingQueue.contains(toSave)) {
                    reportHeaderBlockingQueue.put(toSave);
                }

                // package response
                responseObject.setMessage("Scheduled report created successfully. ");
                responseObject.setPayload(toSave);
                response = new RestResponse(responseObject, HttpStatus.CREATED);

                //Start - audit trail
                AuditRecord auditRecord = new AuditRecord();
                auditRecord.setParentID(String.valueOf(toSave.getReportHeaderId()));
                auditRecord.setCurrentData(toSave.toString());
                auditRecord.setParentObject("Report Header");
                auditRecord.setNotes("CREATED REPORT HEADER - BALANCES");
                auditService.log(AuditOperation.CREATED, auditRecord);
                //End - audit trail
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            ex.printStackTrace();
        }
        return response;
    }

    @Transactional
    public RestResponse createAgeingBalancesRequest(RestRequestObject<ReportHeader> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_ageing");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                String emailAddress = authManager.getEmailFromToken(requestObject.getToken());
                String[] emailData = emailAddress.split("@");
                if (emailData.length > 0) {
                    emailAddress = emailData[0];
                }
                ReportHeader reportHeader = requestObject.getObject();
                ReportHeader toSave = new ReportHeader();

                if (reportHeader.getToDate() == null) {
                    responseObject.setMessage("To date can not be empty");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                toSave.setToDate(reportHeader.getToDate().withZone(DateTimeZone.forID("Africa/Nairobi")));

                if (reportHeader.getOnStatus() == null) {
                    responseObject.setMessage("On status can not be empty");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }
                toSave.setOnStatus(reportHeader.getOnStatus());

                //Check if user has existing unprocessed requests
                Integer pendingRequests = reportHeaderRepository.countPendingByRequestor(emailAddress, ReportStatus.PENDING);
                if (pendingRequests > 0) {
                    responseObject.setMessage("Sorry you already have a  " + pendingRequests + " pending request(s). You can only submit another request when all your pending requests are processed. ");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                if (reportHeader.getCutOff() != null) {
                    toSave.setCutOff(reportHeader.getCutOff());
                }

                if (reportHeader.getScheme() == null) {
                    responseObject.setMessage("Scheme can not be empty");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                Scheme scheme = schemeRepository.findOne(reportHeader.getScheme().getSchemeId());
                if (scheme == null) {
                    responseObject.setMessage("Invalid scheme resource");
                    response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
                    return response;
                }

                // create resource
                if (reportHeader.getScheme() != null) {
                    if (reportHeader.getScheme().getSchemeId() != null) {
                        //Scheme scheme = schemeRepository.findOne(reportHeader.getScheme().getSchemeId());
                        if (scheme != null) {
                            reportHeader.setScheme(scheme);
                            toSave.setScheme(scheme);
                        }
                    }
                }

                if (reportHeader.getZone() != null) {
                    if (reportHeader.getZone().getZoneId() != null) {
                        Zone zone = zoneRepository.findOne(reportHeader.getZone().getZoneId());
                        if (zone != null) {
                            if (zone.getScheme().equals(schemeRepository.findOne(reportHeader.getScheme().getSchemeId()))) {
                                toSave.setZone(zone);
                            }
                        }
                    }
                }

                toSave.setReportType(ReportType.AGEING);
                toSave.setRequestedBy(emailAddress);
                toSave.setCreatedOn(new DateTime());
                toSave = reportHeaderRepository.save(toSave);

                //add to queue
                if (!reportHeaderBlockingQueue.contains(toSave)) {
                    reportHeaderBlockingQueue.put(toSave);
                }

                // package response
                responseObject.setMessage("Scheduled report created successfully. ");
                responseObject.setPayload(toSave);
                response = new RestResponse(responseObject, HttpStatus.CREATED);

                //Start - audit trail
                AuditRecord auditRecord = new AuditRecord();
                auditRecord.setParentID(String.valueOf(toSave.getReportHeaderId()));
                auditRecord.setCurrentData(toSave.toString());
                auditRecord.setParentObject("Report Header");
                auditRecord.setNotes("CREATED REPORT HEADER - AGEING");
                auditService.log(AuditOperation.CREATED, auditRecord);
                //End - audit trail
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            ex.printStackTrace();
        }
        return response;
    }

    public RestResponse getAccountBalancesHeaderList(RestRequestObject<RestPageRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_account_balances");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                RestPageRequest restPageRequest = requestObject.getObject();
                BooleanBuilder builder = new BooleanBuilder();
                builder.and(QReportHeader.reportHeader.reportType.eq(ReportType.ACCOUNT_BALANCES));

                RestPageRequest p = requestObject.getObject();
                Page<ReportHeader> page = reportHeaderRepository.findAll(builder, new PageRequest(p.getPage(), p.getSize(), new Sort(Sort.Direction.DESC, "createdOn")));

                responseObject.setMessage("Fetched data successfully");
                responseObject.setPayload(page);
                response = new RestResponse(responseObject, HttpStatus.OK);
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    public RestResponse getAgeingBalancesHeaderList(RestRequestObject<RestPageRequest> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_ageing");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                RestPageRequest restPageRequest = requestObject.getObject();
                BooleanBuilder builder = new BooleanBuilder();
                builder.and(QReportHeader.reportHeader.reportType.eq(ReportType.AGEING));

                RestPageRequest p = requestObject.getObject();
                Page<ReportHeader> page = reportHeaderRepository.findAll(builder, new PageRequest(p.getPage(), p.getSize(), new Sort(Sort.Direction.DESC, "createdOn")));

                responseObject.setMessage("Fetched data successfully");
                responseObject.setPayload(page);
                response = new RestResponse(responseObject, HttpStatus.OK);
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    public RestResponse getAccountBalancesReport(RestRequestObject<ReportHeader> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_account_balances");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                ReportHeader reportHeader = requestObject.getObject();
                BooleanBuilder builder = new BooleanBuilder();
                builder.and(QAccountBalanceRecord.accountBalanceRecord.reportHeader.reportHeaderId.eq(reportHeader.getReportHeaderId()));

                Iterable<AccountBalanceRecord> records = accountBalanceRecordRepository.findAll(builder, QAccountBalanceRecord.accountBalanceRecord.balance.desc());

                log.info("Packaged report data...");
                ReportObject report = new ReportObject();
                report.setDate(Calendar.getInstance());

                report.setCompany(this.optionService.getOption("COMPANY_NAME").getValue()); //TODO;
                report.setTitle(this.optionService.getOption("REPORT:ACCOUNTS_BALANCE").getValue());
                report.setContent(records);

                //Get total balance
                JPAQuery query = new JPAQuery(entityManager);
                BooleanBuilder balanceBuilder = new BooleanBuilder();
                balanceBuilder.and(QAccountBalanceRecord.accountBalanceRecord.reportHeader.reportHeaderId.eq(reportHeader.getReportHeaderId()));
                balanceBuilder.and(QAccountBalanceRecord.accountBalanceRecord.balance.gt(0));
                Double balance = query.from(QAccountBalanceRecord.accountBalanceRecord).where(balanceBuilder).singleResult(QAccountBalanceRecord.accountBalanceRecord.balance.sum());
                if (balance == null) {
                    balance = 0d;
                }
                report.setAmount(balance);

                //Credit balances
                query = new JPAQuery(entityManager);
                BooleanBuilder creditBalanceBuilder = new BooleanBuilder();
                creditBalanceBuilder.and(QAccountBalanceRecord.accountBalanceRecord.reportHeader.reportHeaderId.eq(reportHeader.getReportHeaderId()));
                creditBalanceBuilder.and(QAccountBalanceRecord.accountBalanceRecord.balance.lt(0));
                Double creditBalances = query.from(QAccountBalanceRecord.accountBalanceRecord).where(creditBalanceBuilder).singleResult(QAccountBalanceRecord.accountBalanceRecord.balance.sum());
                if (creditBalances == null) {
                    creditBalances = 0d;
                }
                report.setAmount1(creditBalances);


                log.info("Sending Payload send to client...");
                responseObject.setMessage("Fetched data successfully");
                responseObject.setPayload(report);
                response = new RestResponse(responseObject, HttpStatus.OK);
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }

    public RestResponse getAgeingBalancesReport(RestRequestObject<ReportHeader> requestObject) {
        try {
            response = authManager.tokenValid(requestObject.getToken());
            if (response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                response = authManager.grant(requestObject.getToken(), "report_account_balances");
                if (response.getStatusCode() != HttpStatus.OK) {
                    return response;
                }

                ReportHeader reportHeader = requestObject.getObject();
                BooleanBuilder builder = new BooleanBuilder();
                builder.and(QAgeingData.ageingData.reportHeader.reportHeaderId.eq(reportHeader.getReportHeaderId()));

                Iterable<AgeingData> records = ageingDataRepository.findAll(builder, QAgeingData.ageingData.balance.desc());

                log.info("Packaged report data...");
                ReportObject report = new ReportObject();
                report.setDate(Calendar.getInstance());

                report.setCompany(this.optionService.getOption("COMPANY_NAME").getValue()); //TODO;
                report.setTitle(this.optionService.getOption("REPORT:AGEING").getValue());
                report.setContent(records);

                log.info("Sending Payload send to client...");
                responseObject.setMessage("Fetched data successfully");
                responseObject.setPayload(report);
                response = new RestResponse(responseObject, HttpStatus.OK);
            }
        } catch (Exception ex) {
            responseObject.setMessage(ex.getLocalizedMessage());
            response = new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
            log.error(ex.getLocalizedMessage());
        }
        return response;
    }
}
