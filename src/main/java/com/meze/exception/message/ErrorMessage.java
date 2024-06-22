package com.meze.exception.message;

public class ErrorMessage {
    public final static String RESOURCE_NOT_FOUND_MESSAGE = "Resource with id %s not found";
    public final static String ROLE_NOT_FOUND_MESSAGE = "Role : %s not found";
    public final static String USER_NOT_FOUND_MESSAGE = "Email or password doesn't matched.";
    public final static String PRINCIPAL_FOUND_MESSAGE= "User not found";
    public final static String EMAIL_ALREADY_CONFIRMED_MESSAGE = "Email already confirmed";
    public final static String RESET_TOKEN_ALREADY_USED_MESSAGE = "Reset token which you have sent already used.";
    public final static String TOKEN_EXPIRED_MESSAGE = "Confirmation Token Expired";
    public final static String JWTTOKEN_ERROR_MESSAGE = "JWT Token Validation Error: %s";
    public final static String EMAIL_ALREADY_EXIST_MESSAGE = "Email: %s already exist";
    public final static String EMAIL_NOT_CONFIRMED_MESSAGE = "Email %s need to be confirmed";
    public final static String FAILED_TO_SEND_EMAIL_MESSAGE = "Failed to send email";
    public final static String NOT_PERMITTED_METHOD_MESSAGE = "You don't have any permission to change this data";
    public final static String PASSWORD_NOT_MATCHED = "Your passwords are not matched";
    public final static String IMAGE_NOT_FOUND_MESSAGE = "ImageFile with id %s not found";
    public final static String IMAGE_USED_MESSAGE = "Image already used";
    public final static String EXCEL_REPORT_ERROR_MESSAGE = "Error occurred while generating excel report";
    public final static String BRAND_NOT_FOUND_MESSAGE = "Brand id: %d not found";
    public final static String BRAND_CONFLICT_EXCEPTION = "Brand '%s' is already exist";
    public final static String BRAND_CAN_NOT_UPDATE_EXCEPTION = "Brand can not be updated";
    public final static String CATEGORY_USED_EXCEPTION = "Category '%s' is already exist";
    public final static String PRODUCT_USED_BY_ORDER_MESSAGE = "Product couldn't be deleted while it have order";
    public final static String PRODUCT_NOT_FOUND_MESSAGE = "Product with id %d not found";
    public final static String PRODUCT_OUT_OF_STOCK_MESSAGE = "The product with id %d is out of stock";
    public final static String USER_ADDRESS_USED_MESSAGE = "User address is used by order";
    public final static String BRAND_CAN_NOT_DELETE_EXCEPTION = "Brand can not be deleted";
    public static final String UUID_NOT_FOUND_MESSAGE = "Uuid could not found with given id";
    public final static String COUPON_NOT_FOUND_MESSAGE = "Coupon with code '%s' not found";
    public final static String CATEGORY_NOT_FOUND_MESSAGE = "Category not found";
    public final static String CATEGORY_CAN_NOT_DELETE_EXCEPTION = "Category can not be deleted";
    public final static String IMAGE_ALREADY_EXIST_MESSAGE = "Image: %s already exist";
    public final static String COUPON_NOT_VALID_MESSAGE = "Coupon with code '%s' is not valid.";
    public final static String COUPON_ALREADY_USED_MESSAGE = "Coupon with code '%s' is already used.";
    public final static String COUPON_CAN_NOT_BE_USED_MESSAGE = "This coupon can only be used for purchases with a total amount of '%.2f' or higher.";
    public final static String REVIEW_IS_NOT_POSSIBLE_MESSAGE = "To be able to review this product, you must have purchased it";
    public final static String ORDER_NOT_FOUND_MESSAGE = "Order with id: %d not found";
    public final static String ORDER_CAN_NOT_BE_UPDATED_MESSAGE = "Order can not be updated while status \"%s\"";
    public final static String ORDER_CAN_BE_UPDATED_ONLY_ONCE_MESSAGE = "You can only update an order once.";

}
