package ke.co.suncha.simba.aqua.reports.scheduled;

import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Created by maitha.manyala on 8/1/16.
 */
public class ReportHeaderRequest implements Serializable {
    private DateTime toDate;
    private Long schemeId;
}
