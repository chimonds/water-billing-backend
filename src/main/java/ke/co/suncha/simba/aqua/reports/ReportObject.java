package ke.co.suncha.simba.aqua.reports;

import org.springframework.stereotype.Service;

import java.util.Calendar;

/**
 * Created by manyala on 4/26/15.
 */
@Service
public class ReportObject {
    private Calendar date;
    private String title;
    private Double amount;
    private Double meterRent;
    private Double charges;
    private Integer units;
    private String company;
    private Object content;
    private String heading1;
    private String heading2;
    private String heading3;
    private String heading4;

    public String getHeading1() {
        return heading1;
    }

    public void setHeading1(String heading1) {
        this.heading1 = heading1;
    }

    public String getHeading2() {
        return heading2;
    }

    public void setHeading2(String heading2) {
        this.heading2 = heading2;
    }

    public String getHeading3() {
        return heading3;
    }

    public void setHeading3(String heading3) {
        this.heading3 = heading3;
    }

    public String getHeading4() {
        return heading4;
    }

    public void setHeading4(String heading4) {
        this.heading4 = heading4;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }


    public Object getContent() {
        return content;
    }


    public void setContent(Object content) {
        this.content = content;
    }

    public Double getMeterRent() {
        return meterRent;
    }

    public void setMeterRent(Double meterRent) {
        this.meterRent = meterRent;
    }

    public Double getCharges() {
        return charges;
    }

    public void setCharges(Double charges) {
        this.charges = charges;
    }

    public Integer getUnits() {
        return units;
    }

    public void setUnits(Integer units) {
        this.units = units;
    }
}
