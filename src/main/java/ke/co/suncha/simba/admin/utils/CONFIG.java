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
    public static final String SMS_NOTIFICATION_BILL_DEFAULT = "Dear Customer, your $billing_month bill for a/c $account_no is KES $bill_amount. New Water balance is KES $balance. Pay via MPESA paybill no 888780 to avoid disconnection.";
    public static final String SMS_NOTIFICATION_PAYMENT_DEFAULT = "Thank you for your payment of KES $receipt_amount to a/c $account_no (txn id $receipt_no). Your new water balance is KES $balance.";

}
