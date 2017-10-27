package ke.co.suncha.simba.mobile.upload;

import ke.co.suncha.simba.aqua.models.MeterReading;

import java.util.List;

/**
 * Created by maitha.manyala on 10/20/17.
 */
public interface MeterReadingRecordService {
    MeterReading save(MeterReading meterReading);

    MeterReading updateCurrentReading(Long meterReadingId, MeterReading meterReading);

    Long readByBillingMonth(Long billingMonthId);

    List<Long> getPendingReadings();

    Long accountsWithMeters();

    Boolean hasMeterReading(Long accountId, Long billingMonthId);

    MeterReading addRecord(MeterReading meterReading);

    void removeRecord(Long accountId, Long billingMonthId);

    Boolean isBilled(Long accountId, Long billingMonthId);

    MeterReading get(Long accountId, Long billingMonthId);

    MeterReading getByMeterReadingId(Long meterReadingId);

    String getImagesDirectory();

    String getImagePath(MeterReading meterReading);

    Boolean saveImage(MeterReading meterReading, String imageString);
}