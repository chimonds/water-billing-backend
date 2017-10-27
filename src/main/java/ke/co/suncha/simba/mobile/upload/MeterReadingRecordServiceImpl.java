package ke.co.suncha.simba.mobile.upload;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.impl.JPAQuery;
import ke.co.suncha.simba.aqua.account.QAccount;
import ke.co.suncha.simba.aqua.models.BillingMonth;
import ke.co.suncha.simba.aqua.models.MeterReading;
import ke.co.suncha.simba.aqua.models.QMeterReading;
import ke.co.suncha.simba.aqua.services.AccountManagerService;
import ke.co.suncha.simba.aqua.services.BillingMonthService;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by maitha.manyala on 9/3/17.
 */
@Service
public class MeterReadingRecordServiceImpl implements MeterReadingRecordService {
    @Autowired
    AccountManagerService accountService;

    @Autowired
    EntityManager entityManager;

    @Autowired
    MeterReadingRepository meterReadingRepository;

    @Autowired
    BillingMonthService billingMonthService;

    @Override
    public MeterReading updateCurrentReading(Long meterReadingId, MeterReading meterReading) {
        MeterReading dbMeterReading = getByMeterReadingId(meterReadingId);
        dbMeterReading.setCurrentReading(meterReading.getCurrentReading());
        return save(dbMeterReading);
    }

    @Override
    public MeterReading save(MeterReading meterReading) {
        return meterReadingRepository.save(meterReading);
    }

    @Override
    public Long readByBillingMonth(Long billingMonthId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QMeterReading.meterReading.billingMonth.billingMonthId.eq(billingMonthId));
        JPAQuery query = new JPAQuery(entityManager);
        Long count = query.from(QMeterReading.meterReading).where(builder).count();
        if (count == null) {
            count = 0l;
        }
        return count;
    }

    @Override
    public List<Long> getPendingReadings() {
        List<Long> meterReadings = new ArrayList<>();
        BillingMonth billingMonth = billingMonthService.getActiveMonth();
        if (billingMonth != null) {
            BooleanBuilder builder = new BooleanBuilder();
            builder.and(QMeterReading.meterReading.billingMonth.billingMonthId.eq(billingMonth.getBillingMonthId()));
            builder.and(QMeterReading.meterReading.billed.eq(Boolean.FALSE));
            JPAQuery query = new JPAQuery(entityManager);
            meterReadings = query.from(QMeterReading.meterReading).where(builder).list(QMeterReading.meterReading.meterReadingId);
            if (meterReadings == null) {
                meterReadings = new ArrayList<>();
            }
        }
        return meterReadings;
    }

    @Override
    public Long accountsWithMeters() {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QAccount.account.meter.isNotNull());
        builder.and(QAccount.account.active.eq(Boolean.TRUE));
        JPAQuery query = new JPAQuery(entityManager);
        Long count = query.from(QAccount.account).where(builder).count();
        if (count == null) {
            count = 0l;
        }
        return count;

    }

    @Override
    public Boolean hasMeterReading(Long accountId, Long billingMonthId) {
        if (get(accountId, billingMonthId) != null) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public MeterReading addRecord(MeterReading meterReading) {
        return meterReadingRepository.save(meterReading);
    }

    @Override
    public void removeRecord(Long accountId, Long billingMonthId) {
        MeterReading meterReading = get(accountId, billingMonthId);
        if (meterReading != null) {
            meterReadingRepository.delete(meterReading);
        }
    }

    @Override
    public Boolean isBilled(Long accountId, Long billingMonthId) {
        MeterReading meterReading = get(accountId, billingMonthId);
        if (meterReading.getBilled()) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public MeterReading get(Long accountId, Long billingMonthId) {
        JPAQuery query = new JPAQuery(entityManager);
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QMeterReading.meterReading.account.accountId.eq(accountId));
        builder.and(QMeterReading.meterReading.billingMonth.billingMonthId.eq(billingMonthId));
        MeterReading meterReading = query.from(QMeterReading.meterReading).where(builder).singleResult(QMeterReading.meterReading);
        if (meterReading != null) {
            return meterReading;
        }
        return null;
    }

    @Override
    public MeterReading getByMeterReadingId(Long meterReadingId) {
        return meterReadingRepository.findOne(meterReadingId);
    }

    @Override
    public String getImagesDirectory() {
        String path = System.getProperty("user.home") + File.separator + "meter_readings";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
        return path;
    }

    @Override
    public String getImagePath(MeterReading meterReading) {
        String imageDirectory = getImagesDirectory();
        String billingMonthImageDir = imageDirectory + File.separator + meterReading.getBillingMonth().getMonth().toString("yyyy_MM");
        File file = new File(billingMonthImageDir);
        if (!file.exists()) {
            file.mkdir();
        }

        String path = billingMonthImageDir + File.separator + meterReading.getAccount().getAccNo() + ".jpg";
        return path;
    }

    @Override
    public Boolean saveImage(MeterReading meterReading, String imageString) {
        String path = getImagePath(meterReading);
        try {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
                file = new File(path);
            }

            byte[] imageByte = Base64.decodeBase64(imageString);
            InputStream input = new ByteArrayInputStream(imageByte);
            OutputStream output = new FileOutputStream(path);
            IOUtils.copy(input, output);

        } catch (Exception ex) {
            ex.printStackTrace();
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @PostConstruct
    private void setupImagesDirectory() {
        getImagesDirectory();
    }
}
