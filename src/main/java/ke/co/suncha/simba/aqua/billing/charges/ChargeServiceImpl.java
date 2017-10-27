package ke.co.suncha.simba.aqua.billing.charges;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.impl.JPAQuery;
import ke.co.suncha.simba.admin.service.UserService;
import ke.co.suncha.simba.aqua.account.AccountService;
import ke.co.suncha.simba.aqua.models.BillItemType;
import ke.co.suncha.simba.aqua.models.BillingMonth;
import ke.co.suncha.simba.aqua.services.BillingMonthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * Created by maitha.manyala on 10/12/17.
 */
@Service
public class ChargeServiceImpl implements ChargeService {
    @Autowired
    AccountService accountService;

    @Autowired
    BillingMonthService billingMonthService;

    @Autowired
    ChargeRepository chargeRepository;
    @Autowired
    ChargeItemRepository chargeItemRepository;

    @Autowired
    UserService userService;

    @Autowired
    EntityManager entityManager;

    @Override
    public Charge create(Long userId, Long accountId, List<BillItemType> billItemTypeList) {
        Charge dbCharge = new Charge();
        dbCharge.setAccount(accountService.getById(accountId));
        dbCharge.setBillingMonth(billingMonthService.getActiveMonth());
        dbCharge.setCreatedBy(userService.getById(userId));
        dbCharge = chargeRepository.save(dbCharge);
        if (!billItemTypeList.isEmpty()) {
            for (BillItemType billItemType : billItemTypeList) {
                ChargeItem ci = new ChargeItem();
                ci.setCharge(dbCharge);
                ci.setAmount(billItemType.getAmount());
                ci.setBillItemType(billItemType);
                chargeItemRepository.save(ci);
            }
        }
        return dbCharge;
    }

    @Override
    public Boolean accountHasCharge(Long accountId) {
        BillingMonth billingMonth = billingMonthService.getActiveMonth();
        if (billingMonth == null) {
            return Boolean.TRUE;
        }
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QCharge.charge.account.accountId.eq(accountId));
        builder.and(QCharge.charge.billingMonth.billingMonthId.eq(billingMonth.getBillingMonthId()));
        JPAQuery query = new JPAQuery(entityManager);
        Charge charge = query.from(QCharge.charge).where(builder).singleResult(QCharge.charge);
        if (charge == null) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public void delete(Long chargeId) {
        chargeRepository.delete(chargeId);
    }

    @Override
    public Boolean exists(Long chargeId) {
        Charge charge = chargeRepository.findOne(chargeId);
        if (charge == null) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean canDelete(Long chargeId) {
        Charge charge = getById(chargeId);
        if (charge == null) {
            return Boolean.FALSE;
        }
        if (charge.getBilled()) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Charge getById(Long chargeId) {
        return chargeRepository.findOne(chargeId);
    }

    @Override
    public Charge get(Long accountId, Long billingMonthId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QCharge.charge.account.accountId.eq(accountId));
        builder.and(QCharge.charge.billingMonth.billingMonthId.eq(billingMonthId));
        JPAQuery query = new JPAQuery(entityManager);
        return query.from(QCharge.charge).where(builder).singleResult(QCharge.charge);
    }

    @Override
    public Page<Charge> getPage(BooleanBuilder builder, PageRequest pageRequest) {
        return chargeRepository.findAll(builder, pageRequest);
    }
}