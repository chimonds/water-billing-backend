/*
 * The MIT License
 *
 * Copyright 2015 Maitha Manyala.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ke.co.suncha.simba.admin.version;

import ke.co.suncha.simba.admin.models.SystemAction;
import ke.co.suncha.simba.admin.models.User;
import ke.co.suncha.simba.admin.models.UserAuth;
import ke.co.suncha.simba.admin.models.UserRole;
import ke.co.suncha.simba.admin.repositories.SimbaOptionRepository;
import ke.co.suncha.simba.admin.repositories.SystemActionRepository;
import ke.co.suncha.simba.admin.repositories.UserRepository;
import ke.co.suncha.simba.admin.repositories.UserRoleRepository;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.aqua.models.*;
import ke.co.suncha.simba.aqua.repository.*;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 */
@Service
public class ReleaseManager {
    @Autowired
    private SimbaOptionRepository optionService;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SystemActionRepository systemActionRepository;

    @Autowired
    private BillingMonthRepository billingMonthRepository;

    @Autowired
    private MeterSizeRepository meterSizeRepository;

    @Autowired
    private MeterOwnerRepository meterOwnerRepository;

    @Autowired
    private PaymentSourceRepository paymentSourceRepository;

    @Autowired
    private PaymentTypeRepository paymentTypeRepository;

    @Autowired
    private BillItemTypeRepository billItemTypeRepository;

    @Autowired
    private SMSTemplateRepository smsTemplateRepository;


    public static int APP_VERSION = 1;
    public static int PROGRESS = 0;
    private int number;

    /**
     * @return the number
     */
    public int getNumber() {
        return number;
    }

    /**
     * @param number the number to set
     */
    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * Release One (2015-03-16)
     *
     * @return
     */

    //@PostConstruct
    private void release_1() {
        try {
            System.out.println("Running updates release_1.....");

            ReleaseManager release = new ReleaseManager();
            release.setNumber(1);
            if (release.getNumber() <= APP_VERSION) {
                try {
                    // set default company name
//                    SimbaOption so = new SimbaOption();
//                    so.setName("COMPANY_NAME");
//                    so.setDescription("The name of the company");
//                    so.setValue("Kibwezi -  Makindu Water and Sanitation Company Ltd");
//                    optionService.save(so);

                    // set application version

//                    so = new SimbaOption();
//                    so.setName("APP_VERSION");
//                    so.setDescription("The current application version");
//                    so.setValue("1");
//                    optionService.save(so);
                } catch (Exception ex) {

                }

                try {

                    String content = "account_create,\n" +
                            "accounts_view,\n" +
                            "account_update,\n" +
                            "account_transfer,\n" +
                            "report_account_receivable,\n" +
                            "report_credit_balances,\n" +
                            "report_field_report,\n" +
                            "report_negative_readings,\n" +
                            "report_meter_stops,\n" +
                            "report_meter_readings,\n" +
                            "report_billed_amount,\n" +
                            "report_billing_checklist,\n" +
                            "billing_month_view,\n" +
                            "billing_month_update,\n" +
                            "consumers_create,\n" +
                            "consumers_view,\n" +
                            "consumers_update,\n" +
                            "location_create,\n" +
                            "location_view,\n" +
                            "location_update,\n" +
                            "meter_create,\n" +
                            "meter_view,\n" +
                            "meter_update,\n" +
                            "meter_allocate,\n" +
                            "meter_deallocate,\n" +
                            "payment_create,\n" +
                            "payments_view,\n" +
                            "report_payments,\n" +
                            "account_statement,\n" +
                            "report_billing_summary,\n" +
                            "report_potential_cut_off,\n" +
                            "report_monthly_bills,\n" +
                            "report_waris,\n" +
                            "zones_create,\n" +
                            "zones_view,\n" +
                            "zones_update,\n" +
                            "roles_create,\n" +
                            "roles_view,\n" +
                            "roles_update,\n" +
                            "users_view,\n" +
                            "users_create,\n" +
                            "users_update,\n" +
                            "permissions_update,\n" +
                            "settings_update,\n" +
                            "dashboard_view,\n" +
                            "users_change_own_password,\n" +

                            "stats_consumer_count,\n" +
                            "stats_accounts_count,\n" +
                            "stats_accounts_active_count,\n" +
                            "stats_accounts_inactive_count,\n" +
                            "stats_billed_this_month,\n" +
                            "stats_paid_this_month,\n" +
                            "stats_paid_today,\n" +
                            "stats_payments_not_allocated,\n" +
                            "stats_bills_payments_linegraph,\n" +
                            "stats_zones_bargraph,\n" +
                            "stats_zones_account_balances,\n" +

                            "account_view,\n" +
                            "bill_account,\n" +
                            "bill_delete,\n" +
                            "payment_transfer,\n" +
                            "mpesa_view,\n" +
                            "postbank_view,\n" +
                            "sms_template_update,\n" +
                            "sms_view,\n" +
                            "accountCategory_view,\n" +
                            "accountCategory_update,\n" +
                            "accountCategory_create,\n" +
                            "report_accounts_not_billed,\n" +
                            "location_view,\n" +
                            "report_ageing,\n" +
                            "sms_create,\n" +
                            "sms_approve,\n" +
                            "report_consumers_without_phone_numbers,\n" +
                            "stats_sms_balance,\n" +
                            "payment_void,\n" +
                            "bill_edit_previous_reading,\n" +
                            "report_accounts,\n" +
                            "settings_view";
                    String[] permissions = content.split(",");
                    if (permissions.length > 0) {
                        for (String permission : permissions) {
                            try {
                                SystemAction systemAction = new SystemAction();
                                systemAction.setName(permission.trim());
                                systemAction.setDescription("Auto generated");
                                systemActionRepository.save(systemAction);
                            } catch (Exception ex) {

                            }
                        }
                    }
                } catch (Exception ex) {
                }


                try {
                    UserRole userRole = new UserRole();
                    userRole.setName("Administrator");
                    userRole.setDescription("Administrators have permission to do anything.");
                    UserRole createdUserRole = userRoleRepository.save(userRole);
                } catch (Exception ex) {

                }

                try {
                    List<SystemAction> availableSystemActions = systemActionRepository.findAll();
                    UserRole userRole = userRoleRepository.findOne(1L);
                    userRole.setSystemActions(availableSystemActions);
                    userRole = userRoleRepository.save(userRole);
                } catch (Exception ex) {

                }

                try {
                    //  Setup default user
                    User user = new User();
                    user.setEmailAddress("maitha.manyala@gmail.com");
                    user.setFirstName("Maitha");
                    user.setLastName("Manyala");
                    user.setActive(true);

                    // create auth with default pass 123456

                    UserAuth auth = new UserAuth();
                    auth.setAuthPassword(AuthManager.encodePassword(user.getEmailAddress().toLowerCase(), "123456"));
                    user.setUserAuth(auth);
                    userRepository.save(user);
                } catch (Exception ex) {

                }

                try {
                    User user = userRepository.findOne(1L);
                    UserRole userRole = userRoleRepository.findOne(1L);
                    user.setUserRole(userRole);
                    userRepository.save(user);

                } catch (Exception ex) {

                }


                try {
                    //
                    Integer current = 2001;
                    Integer last = 2030;
                    for (int x = current; x <= last; x++) {
                        for (int y = 1; y <= 12; y++) {
                            try {
                                BillingMonth bm = new BillingMonth();
                                //bm.setCode(Integer.valueOf(billingCode));
                                Calendar c = Calendar.getInstance();
                                c.set(current, y, 24);
                                bm.setMonth(new DateTime().withMillis(c.getTimeInMillis()));


                                SimpleDateFormat format1 = new SimpleDateFormat("yyyyMM");
                                String billingCode = format1.format(c.getTime());

                                bm.setCode(Integer.valueOf(billingCode));
                                billingMonthRepository.save(bm);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                System.out.println(ex.getLocalizedMessage());
                            }
                        }
                        current++;
                    }

                } catch (Exception ex) {

                }
                //Payment sources
                try {
                    PaymentSource ps = new PaymentSource();
                    ps.setName("CASH");
                    paymentSourceRepository.save(ps);

                    ps = new PaymentSource();
                    ps.setName("M-PESA");
                    paymentSourceRepository.save(ps);

                    ps = new PaymentSource();
                    ps.setName("PostBank");
                    paymentSourceRepository.save(ps);
                } catch (Exception ex) {

                }

                PaymentType pt = new PaymentType();
                //Payment types
                try {
                    pt.setIsPrimary(true);
                    pt.setName("Water Sale");
                    pt.setUnique(true);
                    pt.setDescription("Water sale payment");
                    paymentTypeRepository.save(pt);

                    pt = new PaymentType();
                    pt.setName("Smart Receipt");
                    pt.setUnique(true);
                    pt.setDescription("Smart Receipt");
                    paymentTypeRepository.save(pt);

                    pt = new PaymentType();
                    pt.setName("Fine");
                    pt.setUnique(true);
                    pt.setDescription("Fine");
                    paymentTypeRepository.save(pt);

                    pt = new PaymentType();
                    pt.setName("Credit");
                    pt.setComment(true);
                    pt.setDescription("Credit");
                    paymentTypeRepository.save(pt);

                    pt = new PaymentType();
                    pt.setName("Debit");
                    pt.setComment(true);
                    pt.setNegative(true);
                    pt.setDescription("Debit");
                    paymentTypeRepository.save(pt);

                    pt = new PaymentType();
                    pt.setName("Other");
                    pt.setUnique(true);
                    pt.setDescription("Other");
                    paymentTypeRepository.save(pt);


                } catch (Exception ex) {

                }

                try {
                    pt = new PaymentType();
                    pt.setName("Meter Rent");
                    pt.setUnique(true);
                    pt.setDescription("Meter Rent");
                    paymentTypeRepository.save(pt);

                    pt = new PaymentType();
                    pt.setName("Fine");
                    pt.setUnique(true);
                    pt.setDescription("Fine");
                    paymentTypeRepository.save(pt);
                } catch (Exception ex) {
                }

                //Bill item types
                try {
                    BillItemType bit = new BillItemType();
                    bit.setName("Reconnection Fee");
                    bit.setAmount((double) 500);
                    bit.setActive(false);
                    billItemTypeRepository.save(bit);

                    bit = new BillItemType();
                    bit.setName("At Owners Request Fee");
                    bit.setAmount((double) 500);
                    billItemTypeRepository.save(bit);

                    bit = new BillItemType();
                    bit.setName("Change Of Account Name");
                    bit.setAmount((double) 500);
                    billItemTypeRepository.save(bit);

                    bit = new BillItemType();
                    bit.setName("By Pass Fee");
                    bit.setActive(false);
                    bit.setAmount((double) 5000);
                    billItemTypeRepository.save(bit);

                    bit = new BillItemType();
                    bit.setName("Bounced Cheque Fee");
                    bit.setAmount((double) 400);
                    billItemTypeRepository.save(bit);

                    bit = new BillItemType();
                    bit.setName("Surcharge Irrigation");
                    bit.setAmount((double) 5000);
                    billItemTypeRepository.save(bit);

                    bit = new BillItemType();
                    bit.setName("Surcharge Missuse");
                    bit.setAmount((double) 5000);
                    billItemTypeRepository.save(bit);

                    bit = new BillItemType();
                    bit.setName("Meter Servicing");
                    bit.setAmount((double) 500);
                    billItemTypeRepository.save(bit);
                } catch (Exception ex) {

                }

                //Meter sizes
                MeterSize ms = new MeterSize();
                try {
                    ms = new MeterSize();
                    ms.setRentAmount((double) 50);
                    ms.setSize("0.5 INCH");
                    meterSizeRepository.save(ms);
                } catch (Exception ex) {
                }

                try {
                    ms = new MeterSize();
                    ms.setRentAmount((double) 50);
                    ms.setSize("0.75 INCH");
                    meterSizeRepository.save(ms);
                } catch (Exception ex) {
                }

                try {
                    ms = new MeterSize();
                    ms.setRentAmount((double) 250);
                    ms.setSize("1 INCH");
                    meterSizeRepository.save(ms);
                } catch (Exception ex) {
                }

                try {
                    ms = new MeterSize();
                    ms.setRentAmount((double) 250);
                    ms.setSize("1.5 INCH");
                    meterSizeRepository.save(ms);
                } catch (Exception ex) {
                }

                try {
                    ms = new MeterSize();
                    ms.setRentAmount((double) 250);
                    ms.setSize("2.5 INCH");
                    meterSizeRepository.save(ms);
                } catch (Exception ex) {
                }

                try {
                    ms = new MeterSize();
                    ms.setRentAmount((double) 450);
                    ms.setSize("3 INCH");
                    meterSizeRepository.save(ms);
                } catch (Exception ex) {
                }

                try {
                    ms = new MeterSize();
                    ms.setRentAmount((double) 450);
                    ms.setSize("3.5 INCH");
                    meterSizeRepository.save(ms);
                } catch (Exception ex) {
                }

                try {
                    ms = new MeterSize();
                    ms.setRentAmount((double) 800);
                    ms.setSize("4 INCH");
                    meterSizeRepository.save(ms);
                } catch (Exception ex) {

                }


                //Meter Owners
                try {
                    MeterOwner mo = new MeterOwner();
                    mo.setName("Personal");
                    mo.setCharge(false);
                    meterOwnerRepository.save(mo);

                    mo = new MeterOwner();
                    mo.setName("Organization");
                    mo.setCharge(true);
                    meterOwnerRepository.save(mo);
                } catch (Exception ex) {
                }

            }
        } catch (Exception ex) {
            // ex.printStackTrace();
            //System.out.println(ex.getMessage());
        }
    }

    public void addPermission(String name) {
        try {
            SystemAction systemAction = new SystemAction();
            systemAction.setName(name);
            systemAction.setDescription("Auto generated");
            systemActionRepository.save(systemAction);
        } catch (Exception ex) {

        }
    }
}
