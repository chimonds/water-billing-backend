package ke.co.suncha.simba.aqua.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import ke.co.suncha.simba.admin.models.User;
import ke.co.suncha.simba.admin.repositories.UserRepository;
import ke.co.suncha.simba.admin.request.RestResponse;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.admin.security.Credential;
import ke.co.suncha.simba.admin.service.AuditService;
import ke.co.suncha.simba.admin.service.SimbaOptionService;
import ke.co.suncha.simba.aqua.account.Account;
import ke.co.suncha.simba.aqua.models.*;
import ke.co.suncha.simba.aqua.repository.*;
import ke.co.suncha.simba.aqua.scheme.zone.Zone;
import ke.co.suncha.simba.aqua.scheme.zone.ZoneRepository;
import ke.co.suncha.simba.aqua.utils.MobileClientRequest;
import ke.co.suncha.simba.aqua.utils.MobileClientResponse;
import ke.co.suncha.simba.aqua.utils.RemoteUser;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by maitha.manyala on 7/9/15.
 */
@Service
public class AccountSummaryService {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountSummaryRepository accountSummaryRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private ConsumerRepository consumerRepository;

    @Autowired
    private SimbaOptionService optionService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AuthManager authManager;

    @Autowired
    CounterService counterService;

    @Autowired
    GaugeService gaugeService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private AccountStatusHistoryRepository accountStatusHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    AccountManagerService accountService;

    @Autowired
    AccountsUpdateRepository accountsUpdateRepository;

    private RestResponse response;
    private RestResponseObject responseObject = new RestResponseObject();

    public AccountSummaryService() {

    }

    //@Scheduled(fixedDelay = 10000)
    public void notifyAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            if (!users.isEmpty()) {
                for (User user : users) {
                    RestTemplate restTemplate = new RestTemplate();

                    MobileClientRequest request = new MobileClientRequest();
                    Credential credential = new Credential();

                    credential.setUsername(optionService.getOption("MOBILE_CLIENT_USERNAME").getValue());
                    credential.setPassword(optionService.getOption("MOBILE_CLIENT_KEY").getValue());
                    request.setLogin(credential);

                    RemoteUser remoteUser = new RemoteUser();
                    remoteUser.setUsername(user.getEmailAddress());
                    remoteUser.setFullname(user.getFirstName());

                    request.setPayload(remoteUser);

                    String url = optionService.getOption("MOBILE_CLIENT_ENDPOINT").getValue() + "/upload_users";
                    String jsonResponse = restTemplate.postForObject(url, request, String.class);
                    //log.info("Response:" + jsonResponse);

                    //2. Convert JSON to Java object
                    //ObjectMapper mapper = new ObjectMapper();
                    //MobileClientResponse clientResponse = mapper.readValue(jsonResponse, MobileClientResponse.class);
//                    if (clientResponse.getStatus() != 200) {
//                        log.error(clientResponse.getMessage());
//                    } else {
//                        accountSummary.setNotifyClient(false);
//                        accountSummaryRepository.save(accountSummary);
//                        log.info("Mobile client success:" + accountSummary.getAccNo());
//                    }
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    //@Scheduled(fixedDelay = 1000)
    public void notifyRemoteClient() {
        try {
            Boolean notifyClient = false;
            try {
                notifyClient = Boolean.parseBoolean(optionService.getOption("MOBILE_CLIENT_ENABLE").getValue());
            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
            if (!notifyClient) {
                return;
            }

            Page<AccountSummary> accountSummaries;
            accountSummaries = accountSummaryRepository.findAllByNotifyClient(true, new PageRequest(0, 300));
            if (accountSummaries.hasContent()) {
                for (AccountSummary accountSummary : accountSummaries) {
                    log.info("Notifying mobile client records for:" + accountSummary.getAccNo());
                    RestTemplate restTemplate = new RestTemplate();

                    MobileClientRequest request = new MobileClientRequest();
                    Credential credential = new Credential();

                    credential.setUsername(optionService.getOption("MOBILE_CLIENT_USERNAME").getValue());
                    credential.setPassword(optionService.getOption("MOBILE_CLIENT_KEY").getValue());
                    request.setLogin(credential);
                    request.setPayload(accountSummary);

                    String url = optionService.getOption("MOBILE_CLIENT_ENDPOINT").getValue() + "/account_summary";
                    String jsonResponse = restTemplate.postForObject(url, request, String.class);
                    //log.info("Response:"+jsonResponse);

                    //2. Convert JSON to Java object
                    ObjectMapper mapper = new ObjectMapper();
                    MobileClientResponse clientResponse = mapper.readValue(jsonResponse, MobileClientResponse.class);
                    if (clientResponse.getStatus() != 200) {
                        log.error(clientResponse.getMessage());
                    } else {
                        accountSummary.setNotifyClient(false);
                        accountSummaryRepository.save(accountSummary);
                        log.info("Mobile client success:" + accountSummary.getAccNo());
                    }
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    //@Scheduled(fixedDelay = 300000)
    public void populateAccountSummaryTable() {
        try {
            List<String> accountList = accountRepository.findAllAccountNumbers();
            if (!accountList.isEmpty()) {
                for (String accNo : accountList) {
                    Account account = accountRepository.findByaccNo(accNo);

                    if (account != null) {

                        Boolean updateRecord = false;
                        AccountSummary accountSummary = accountSummaryRepository.findByaccNo(accNo);
                        if (accountSummary == null) {
                            accountSummary = new AccountSummary();
                            updateRecord = true;
                        } else {
                            if (accountSummary.getBalance().compareTo(account.getOutstandingBalance()) != 0) {
                                //log.info(accountSummary.getBalance()+"");
                                //log.info(account.getOutstandingBalance()+"");
                                updateRecord = true;
                            }
                        }
                        if (updateRecord) {
                            accountSummary.setBalance(account.getOutstandingBalance());
                            accountSummary.setStatus(account.getAccountStatus());
                            accountSummary.setAccNo(account.getAccNo());
                            accountSummary.setNotifyClient(true);

                            //get account name
                            Long consumerId = accountRepository.findConsumerIdByAccountId(account.getAccountId());
                            if (consumerId != null) {
                                Consumer consumer = consumerRepository.findOne(consumerId);
                                if (consumer != null) {
                                    String fullName = "";
                                    if (consumer.getFirstName() != null) {
                                        fullName += consumer.getFirstName() + " ";
                                    }
                                    if (consumer.getMiddleName() != null) {
                                        fullName += consumer.getMiddleName() + " ";
                                    }

                                    if (consumer.getLastName() != null) {
                                        fullName += consumer.getLastName();
                                    }

                                    accountSummary.setAccName(fullName);
                                }
                            }

                            //get zone
                            Long zoneId = accountRepository.findZoneIdByAccountId(account.getAccountId());
                            if (zoneId != null) {
                                Zone zone = zoneRepository.findOne(zoneId);
                                if (zone != null) {
                                    accountSummary.setZone(zone.getName());
                                }
                            }

                            accountSummary = accountSummaryRepository.save(accountSummary);
                        }
                    }
                }
            }
            //upload data

        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    @Scheduled(initialDelay = 1000, fixedDelay = Integer.MAX_VALUE)
    public void populateOutstandingAccountBalances() {
        try {
            log.info("Start updating balances:"+ new DateTime());
            List<BigInteger> accountList = accountRepository.findAllAccountIds();
            if (!accountList.isEmpty()) {
                for (BigInteger accountId : accountList) {
                    accountService.updateBalance(accountId.longValue());
                }
            }
            log.info("End updating balances:"+ new DateTime());
            log.info("*****************************************************");
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 3000)
    public void updateAccounts() {
        try {
            List<BigInteger> accountUpdateList = accountsUpdateRepository.findAllByPending();
            if (!accountUpdateList.isEmpty()) {
                for (BigInteger recordId : accountUpdateList) {
                    try {
                        AccountUpdate accountUpdate = accountsUpdateRepository.findOne(recordId.longValue());
                        Long accountId = accountUpdate.getAccountId();
                        Account account = accountRepository.findOne(accountUpdate.getAccountId());
                        if (account != null) {
                            account.setUpdateBalance(Boolean.TRUE);
                            accountRepository.save(account);
                            accountService.updateBalance(accountId);
                        }
                        accountUpdate.setStatus(1);
                        accountUpdate.setUpdatedOn(new DateTime());
                        accountsUpdateRepository.save(accountUpdate);
                    } catch (Exception ex) {
                        log.error(ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }
}
