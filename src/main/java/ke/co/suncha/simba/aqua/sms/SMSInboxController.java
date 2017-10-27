package ke.co.suncha.simba.aqua.sms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by maitha.manyala on 7/27/17.
 */
@RestController
@RequestMapping(value = "/api/v1/inbox")
public class SMSInboxController {
    @Autowired
    SMSInboxService smsInboxService;

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void receive(HttpServletRequest request, HttpServletResponse response) {
        String from = request.getParameter("from");
        String to = request.getParameter("to");
        String text = request.getParameter("text");
        String date = request.getParameter("date");
        String id = request.getParameter("id");
        SMSInbox inbox = new SMSInbox();
        inbox.setFrom(from);
        inbox.setTo(to);
        inbox.setText(text);
        inbox.setDate(date);
        inbox.setId(id);
        smsInboxService.create(inbox);
    }
}
