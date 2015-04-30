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
    private String company;
    private Object content;

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
}
