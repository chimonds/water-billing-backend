package ke.co.suncha.simba.mobile.account;

import ke.co.suncha.simba.aqua.reports.StatementRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maitha.manyala on 8/20/17.
 */
public class MobileStatement {
    private MobileAccount account;
    private List<StatementRecord> records = new ArrayList<>();

    public MobileAccount getAccount() {
        return account;
    }

    public void setAccount(MobileAccount account) {
        this.account = account;
    }

    public List<StatementRecord> getRecords() {
        return records;
    }

    public void setRecords(List<StatementRecord> records) {
        this.records = records;
    }
}
