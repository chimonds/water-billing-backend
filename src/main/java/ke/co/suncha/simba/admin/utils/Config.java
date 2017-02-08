package ke.co.suncha.simba.admin.utils;

/**
 * Created by manyala on 6/23/15.
 */
public class Config {
    //sms default templates
    public static final String SMS_TEMPLATE_PAYMENT_ACCOUNT_WITH_BALANCE = "SMS_TEMPLATE_PAYMENT_ACCOUNT_WITH_BALANCE";
    public static final String SMS_TEMPLATE_PAYMENT_ACCOUNT_WITH_ZERO_BALANCE = "SMS_TEMPLATE_PAYMENT_ACCOUNT_WITH_ZERO_BALANCE";
    public static final String SMS_TEMPLATE_PAYMENT_ACCOUNT_WITH_CREDIT_BALANCE = "SMS_TEMPLATE_PAYMENT_ACCOUNT_WITH_CREDIT_BALANCE";

    //sms default payment notifications
    public static final String SMS_NOTIFICATION_PAYMENT_ACCOUNT_WITH_BALANCE = "SMS_NOTIFICATION_PAYMENT_ACCOUNT_WITH_BALANCE";
    public static final String SMS_NOTIFICATION_PAYMENT_ACCOUNT_WITH_ZERO_BALANCE = "SMS_NOTIFICATION_PAYMENT_ACCOUNT_WITH_ZERO_BALANCE";
    public static final String SMS_NOTIFICATION_PAYMENT_ACCOUNT_CREDIT_BALANCE = "SMS_NOTIFICATION_PAYMENT_ACCOUNT_CREDIT_BALANCE";

    //bills sms default templates
    public static final String SMS_TEMPLATE_BILL_ACCOUNT_WITH_BALANCE = "SMS_TEMPLATE_BILL_ACCOUNT_WITH_BALANCE";
    public static final String SMS_TEMPLATE_BILL_ACCOUNT_WITH_ZERO_BALANCE = "SMS_TEMPLATE_BILL_ACCOUNT_WITH_ZERO_BALANCE";
    public static final String SMS_TEMPLATE_BILL_ACCOUNT_WITH_CREDIT_BALANCE = "SMS_TEMPLATE_BILL_ACCOUNT_WITH_CREDIT_BALANCE";

    //sms default payment notifications
    public static final String SMS_NOTIFICATION_BILL_ACCOUNT_WITH_BALANCE = "SMS_NOTIFICATION_BILL_ACCOUNT_WITH_BALANCE";
    public static final String SMS_NOTIFICATION_BILL_ACCOUNT_WITH_ZERO_BALANCE = "SMS_NOTIFICATION_BILL_ACCOUNT_WITH_ZERO_BALANCE";
    public static final String SMS_NOTIFICATION_BILL_ACCOUNT_CREDIT_BALANCE = "SMS_NOTIFICATION_BILL_ACCOUNT_CREDIT_BALANCE";
    public static final String SMS_NOTIFICATION_BILL_DEFAULT = "Dear Customer, your $billing_month bill for a/c $account_no is KES $bill_amount. New Water balance is KES $balance. Pay via MPESA paybill no 913110 to avoid disconnection.";
    public static final String SMS_NOTIFICATION_PAYMENT_DEFAULT = "Thank you for your payment of KES $receipt_amount to a/c $account_no (txn id $receipt_no). Your new water balance is KES $balance.";


    public static final String SMS_TEMPLATE_REMOTE_BALANCE_REQUEST_ACCOUNT_VALID = "SMS_TEMPLATE_REMOTE_BALANCE_REQUEST_ACCOUNT_VALID";
    public static final String SMS_TEMPLATE_REMOTE_BALANCE_REQUEST_ACCOUNT_NOT_VALID = "SMS_TEMPLATE_REMOTE_BALANCE_REQUEST_ACCOUNT_NOT_VALID";


    public static final String SMS_TEMPLATE_REMOTE_BALANCE_REQUEST_ACCOUNT_VALID_DEFAULT = "Dear Customer, your Water Balance for a/c $account_no is KES $balance. Tuhifadhi Maji.";
    public static final String SMS_TEMPLATE_REMOTE_BALANCE_REQUEST_ACCOUNT_NOT_VALID_DEFAULT = "Dear Customer, account number does not exist. Tuhifadhi Maji.";

    public static final String SMS_NOTIFICATION_REMOTE_BALANCE_REQUEST_ACCOUNT_VALID = "SMS_NOTIFICATION_REMOTE_BALANCE_REQUEST_ACCOUNT_VALID";
    public static final String SMS_NOTIFICATION_REMOTE_BALANCE_REQUEST_ACCOUNT_NOT_VALID = "SMS_NOTIFICATION_REMOTE_BALANCE_REQUEST_ACCOUNT_NOT_VALID";

    public static final String SMS_NOTIFICATION_APPROVAL_TASK = "SMS_NOTIFICATION_APPROVAL_TASK";
    public static final String SMS_TEMPLATE_APPROVAL_TASK = "SMS_TEMPLATE_APPROVAL_TASK";
    public static final String SMS_TEMPLATE_APPROVAL_TASK_DEFAULT = "Dear $firstname, a $task_name approval task #$sno for account $acc_no has been assigned to you. Kindly visit https://nolturesh.opentembo.io to approve/reject the request.";

    public static final String SMS_NOTIFICATION_APPROVAL_TASK_REMINDER = "SMS_NOTIFICATION_APPROVAL_TASK_REMINDER";
    public static final String SMS_TEMPLATE_APPROVAL_TASK_REMINDER = "SMS_TEMPLATE_APPROVAL_TASK_REMINDER";
    public static final String SMS_TEMPLATE_APPROVAL_TASK_DEFAULT_REMINDER = "Dear $firstname, you have $count pending task(s) assigned to you. Kindly visit https://nolturesh.opentembo.io to approve/reject the request(s).";

    public static final String SMS_NOTIFICATION_STATS_ALERT = "SMS_NOTIFICATION_STATS_ALERT";
    public static final String SMS_TEMPLATE_STATS_ALERT = "SMS_TEMPLATE_APPROVAL_STATS_ALERT";
    public static final String SMS_TEMPLATE_STATS_ALERT_DEFAULT = "Paid Yesterday: KES $paidYesterday, Paid this Month: KES $paidThisMonth, Billed last Month: KES $billedLastMonth, Collection Efficiency: $collectionEfficiency%";
}