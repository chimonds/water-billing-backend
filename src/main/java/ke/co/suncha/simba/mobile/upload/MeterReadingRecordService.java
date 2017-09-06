package ke.co.suncha.simba.mobile.upload;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.impl.JPAQuery;
import ke.co.suncha.simba.aqua.models.MeterReading;
import ke.co.suncha.simba.aqua.models.QMeterReading;
import ke.co.suncha.simba.aqua.services.AccountManagerService;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.io.*;

/**
 * Created by maitha.manyala on 9/3/17.
 */
@Service
public class MeterReadingRecordService {
    @Autowired
    AccountManagerService accountService;

    @Autowired
    EntityManager entityManager;

    @Autowired
    MeterReadingRepository meterReadingRepository;

    public Boolean hasMeterReading(Long accountId, Long billingMonthId) {
        if (get(accountId, billingMonthId) != null) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public MeterReading addRecord(MeterReading meterReading) {
        return meterReadingRepository.save(meterReading);
    }

    public void removeRecord(Long accountId, Long billingMonthId) {
        MeterReading meterReading = get(accountId, billingMonthId);
        if (meterReading != null) {
            meterReadingRepository.delete(meterReading);
        }
    }

    public Boolean isBilled(Long accountId, Long billingMonthId) {
        MeterReading meterReading = get(accountId, billingMonthId);
        if (meterReading.getBilled()) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

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

    private String getImagesDirectory() {
        String path = System.getProperty("user.home") + File.separator + "meter_readings";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
        return path;
    }

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

    public Boolean saveImage(MeterReading meterReading, String imageString) {
        String path = getImagePath(meterReading);
        try {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
                file = new File(path);
            }

            byte[] imageByte= Base64.decodeBase64(imageString);
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
