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

import java.util.Calendar;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ke.co.suncha.simba.admin.models.SimbaOption;
import ke.co.suncha.simba.admin.models.SystemAction;
import ke.co.suncha.simba.admin.models.User;
import ke.co.suncha.simba.admin.models.UserAuth;
import ke.co.suncha.simba.admin.models.UserRole;
import ke.co.suncha.simba.admin.repositories.SimbaOptionRepository;
import ke.co.suncha.simba.admin.repositories.SystemActionRepository;
import ke.co.suncha.simba.admin.repositories.UserRepository;
import ke.co.suncha.simba.admin.repositories.UserRoleRepository;
import ke.co.suncha.simba.admin.security.AuthManager;
import ke.co.suncha.simba.aqua.models.BillItemType;
import ke.co.suncha.simba.aqua.models.BillingMonth;
import ke.co.suncha.simba.aqua.models.MeterOwner;
import ke.co.suncha.simba.aqua.models.MeterSize;
import ke.co.suncha.simba.aqua.models.PaymentSource;
import ke.co.suncha.simba.aqua.models.PaymentType;
import ke.co.suncha.simba.aqua.repository.BillItemTypeRepository;
import ke.co.suncha.simba.aqua.repository.BillingMonthRepository;
import ke.co.suncha.simba.aqua.repository.MeterOwnerRepository;
import ke.co.suncha.simba.aqua.repository.MeterSizeRepository;
import ke.co.suncha.simba.aqua.repository.PaymentSourceRepository;
import ke.co.suncha.simba.aqua.repository.PaymentTypeRepository;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 *
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
	 * @param number
	 *            the number to set
	 */
	public void setNumber(int number) {
		this.number = number;
	}

	/**
	 * Release One (2015-03-16)
	 * 
	 * @return
	 */

	@PostConstruct
	private void release_1() {
		try {
			System.out.println("Running updates release_1.....");

			ReleaseManager release = new ReleaseManager();
			release.setNumber(1);
			if (release.getNumber() <= APP_VERSION) {
				try {
					// set default company name
					SimbaOption so = new SimbaOption();
					so.setName("COMPANY_NAME");
					so.setDescription("The name of the company");
					so.setValue("Demo Company");
					optionService.save(so);

					// set application version

					so = new SimbaOption();
					so.setName("APP_VERSION");
					so.setDescription("The current application version");
					so.setValue("1");
					optionService.save(so);
				} catch (Exception ex) {

				}

				try {
					// setup default system actions
					SystemAction systemAction = new SystemAction();
					systemAction.setName("Users:add");
					systemAction.setDescription("Add users to the system");
					systemActionRepository.save(systemAction);

					systemAction = new SystemAction();
					systemAction.setName("Users:update");
					systemAction.setDescription("Update users info");
					systemActionRepository.save(systemAction);

					systemAction = new SystemAction();
					systemAction.setName("Roles:add");
					systemAction.setDescription("Add user roles to the system");
					systemActionRepository.save(systemAction);

					systemAction = new SystemAction();
					systemAction.setName("Roles:update");
					systemAction.setDescription("Update user roles to the system");
					systemActionRepository.save(systemAction);

					systemAction = new SystemAction();
					systemAction.setName("Menu:settings");
					systemAction.setDescription("View top application level settings menu");
					systemActionRepository.save(systemAction);
				} catch (Exception ex) {

				}

				try {
					// Setup default user role
					UserRole userRole = new UserRole();
					userRole.setName("Administrator");
					userRole.setDescription("Administrators have permission to do anything.");
					UserRole createdUserRole = userRoleRepository.save(userRole);

					// get all system actions and assign to the default role
					List<SystemAction> availableSystemActions = systemActionRepository.findAll();
					createdUserRole.setSystemActions(availableSystemActions);

					createdUserRole = userRoleRepository.save(createdUserRole);

					// Setup default user
					User user = new User();
					user.setEmailAddress("maitha.manyala@gmail.com");
					user.setFirstName("Maitha");
					user.setLastName("Manyala");
					user.setActive(true);

					// create auth with default pass 123456
					UserAuth auth = new UserAuth();
					auth.setAuthPassword(AuthManager.encodePassword(user.getEmailAddress().toLowerCase(), "123456"));
					user.setUserAuth(auth);
					user.setUserRole(createdUserRole);
					userRepository.save(user);
				} catch (Exception ex) {

				}

				try {
					//
					Integer current = 2001;

					Integer last = 2030;

					System.out.println("Last:" + last);
					for (int x = current; x <= last; x++) {

						for (int y = 1; y <= 12; y++) {

							
							
							try {
								BillingMonth bm = new BillingMonth();
								//bm.setCode(Integer.valueOf(billingCode));
								Calendar c = Calendar.getInstance();
								c.set(current, y, 24);
								bm.setMonth(c);
								
								String month = y + "";
								if (month.length() == 1) {
									month = "0" + month;
								}
								String billingCode = current + "" + month;
								
								
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

				// Payment sources
				try {
					PaymentSource ps = new PaymentSource();
					ps.setName("CASH");
					paymentSourceRepository.save(ps);

					ps = new PaymentSource();
					ps.setName("M-PESA");
					paymentSourceRepository.save(ps);

					ps = new PaymentSource();
					ps.setName("Post Bank");
					paymentSourceRepository.save(ps);
				} catch (Exception ex) {

				}

				// Payment types
				try {
					PaymentType pt = new PaymentType();
					pt.setIsPrimary(true);
					pt.setName("Water Sale");
					pt.setUnique(true);
					pt.setDescription("Water sale payment");
					paymentTypeRepository.save(pt);

					pt = new PaymentType();
					pt.setName("Credit");
					pt.setDescription("Credit");
					paymentTypeRepository.save(pt);

					pt = new PaymentType();
					pt.setName("Debit");
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

				// Bill item types
				try {
					BillItemType bit = new BillItemType();
					bit.setName("Reconnection Fee");
					bit.setAmount((double) 500);
					bit.setActive(false);
					billItemTypeRepository.save(bit);

					bit = new BillItemType();
					bit.setName("At Owners Request Fee");
					bit.setAmount((double) 1000);
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
					bit.setAmount((double) 100);
					billItemTypeRepository.save(bit);

				} catch (Exception ex) {

				}

				// Meter sizes
				try {
					MeterSize ms = new MeterSize();
					ms.setRentAmount((double) 50);
					ms.setSize("Half INCH");
					meterSizeRepository.save(ms);

					ms = new MeterSize();
					ms.setRentAmount((double) 250);
					ms.setSize("Two & Half INCH");
					meterSizeRepository.save(ms);
				} catch (Exception ex) {

				}

				// Meter Owners
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
			System.out.println(ex.getMessage());
		}
	}
}
