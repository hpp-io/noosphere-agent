package io.hpp.noosphere.agent.config;

import java.util.Arrays;
import java.util.List;

/**
 * Application constants.
 */
public final class Constants {

    // Regex for acceptable logins
    public static final String LOGIN_REGEX = "^(?>[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*)|(?>[_.@A-Za-z0-9-]+)$";

    public static final String SYSTEM = "system";
    public static final String DEFAULT_LANGUAGE = "ko";

    public static final String ZERO_ADDRESS = "0x0000000000000000000000000000000000000000";

    public static final String FULL_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
    public static final String KAKAO_PAY_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String TOSS_PAY_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
    public static final String SMART_FACTORY_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String SIMPLE_DATE_FORMAT = "yyyy/MM/dd";
    public static final String SIMPLE_DATE_FORMAT_2 = "yyyy-MM-dd";
    public static final String DATE_FORMAT_MONTH = "MM";
    public static final String DATE_FORMAT_YEAR = "yyyy";
    public static final String DATE_FORMAT_DAY = "dd";
    public static final String DATE_FORMAT_SEPARATOR = "/";
    public static final String DATE_FORMAT_SEPARATOR_2 = "-";
    public static final String EXCEL_DATE_FORMAT = "yyyy/MM/dd";
    public static final String FILENAME_DATE_FORMAT = "yyyy_MM_dd";
    public static final String DEFAULT_ENCODING = "UTF-8";

    public static final String PROFILE_DEV = "dev";
    public static final String PROFILE_TEST = "test";
    public static final String PROFILE_PROD = "prod";
    public static final String PROFILE_DEMO = "demo";

    public static final String PHONE_NUMBER_REGEX = "^01(?:0|1|[6-9])[.-]?(\\d{3}|\\d{4})[.-]?(\\d{4})$";
    public static final String EMAIL_REGEX =
        "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

    public static final String ERROR_KEY_REQUIRED = "required";
    public static final String ERROR_KEY_INVALID = "invalid";
    public static final String ERROR_KEY_ALREADY_EXISTS = "alreadyExists";
    public static final String ERROR_KEY_INSUFFICIENT = "insufficient";
    public static final String ERROR_KEY_NOT_MODIFIABLE_COMPLETED = "notModifiableCompleted";
    public static final String ERROR_KEY_FILE_TOO_LARGE = "fileTooLarge";
    public static final String ERROR_KEY_NOT_COMPLETED = "notCompleted";
    public static final String ERROR_KEY_NOT_MODIFIABLE_ERROR = "notModifiableError";
    public static final String ERROR_KEY_NOT_FOUND = "notFound";
    public static final String ERROR_KEY_MESSAGE_FAILED = "messageFailed";

    public static final String PROPERTY_NAME_ID = "id";
    public static final String PROPERTY_NAME_ROLE = "role";
    public static final String PROPERTY_NAME_NAME = "name";
    public static final String PROPERTY_NAME_BOOKMARK = "bookmark";
    public static final String PROPERTY_NAME_ACCOUNT_BALANCE = "accountBalance";
    public static final String PROPERTY_NAME_FILE = "file";
    public static final String PROPERTY_NAME_FOLDER = "folder";
    public static final String PROPERTY_NAME_USER = "user";
    public static final String PROPERTY_NAME_GROUP = "group";

    public static final String PROPERTY_NAME_EMAIL = "email";
    public static final String PROPERTY_NAME_EXPIRED_AT = "expiredAt";
    public static final String PROPERTY_NAME_MOBILE_PHONE_NUMBER = "mobilePhoneNumber";
    public static final String PROPERTY_NAME_PHONE_NUMBER = "phoneNumber";
    public static final String PROPERTY_NAME_PROFILE_IMAGE = "profileImage";
    public static final String PROPERTY_NAME_PASSWORD = "password";
    public static final String PROPERTY_NAME_CURRENT_PASSWORD = "currentPassword";
    public static final String PROPERTY_NAME_LOCALE = "locale";
    public static final String PROPERTY_NAME_MARKETING_AGREEMENT = "marketingAgreement";
    public static final String PROPERTY_NAME_SIGNATURE = "signature";
    public static final String PROPERTY_NAME_TYPE = "type";
    public static final String PROPERTY_NAME_ERROR_KEY = "errorKey";
    public static final String PROPERTY_NAME_NEW_ERROR_KEY = "newErrorKey";
    public static final String PROPERTY_NAME_VALUE = "value";
    public static final String PROPERTY_NAME_PASSWORD_STRENGTH = "passwordStrength";
    public static final String PROPERTY_NAME_USER_NAME = "userName";
    public static final String PROPERTY_NAME_USER_ID = "userId";
    public static final String PROPERTY_NAME_USER_EMAIL = "userEmail";
    public static final String PROPERTY_NAME_COMPANY_CODE = "corpCode";
    public static final String PROPERTY_NAME_DATA_TYPE = "dataType";
    public static final String PROPERTY_NAME_CORP_CODE = "corpCode";
    public static final String PROPERTY_NAME_SEARCH_TEXT = "searchText";
    public static final String PROPERTY_NAME_BUSINESS_REGISTRATION_NUMBER = "bizrNo";
    public static final String PROPERTY_NAME_BUSINESS_REGISTRATION_NUMBER_NE = "notBizrNo";
    public static final String PROPERTY_NAME_CORP_NAME = "corpName";
    public static final String PROPERTY_NAME_STOCK_CODE = "stockCode";
    public static final String PROPERTY_NAME_ADDRESS = "address";
    public static final String PROPERTY_NAME_ROLES = "roles";
    public static final String PROPERTY_NAME_REALM_ACCESS = "realm_access";
    public static final String PROPERTY_NAME_PAYMENT = "payment";
    public static final String PROPERTY_NAME_STATUS = "status";
    public static final String PROPERTY_NAME_HTTP_STATUS = "httpStatus";
    public static final String PROPERTY_NAME_CODE_LIST = "codeList";
    public static final String PROPERTY_NAME_DEPTH = "depth";

    public static final String COLUMN_NAME_CREATED_AT = "createdAt";
    public static final String COLUMN_NAME_UPDATED_AT = "updatedAt";
    public static final String COLUMN_NAME_NAME = "name";
    public static final String COLUMN_NAME_QUESTION_ORDER = "questionOrder";

    public static final String URL_FILE = "file";
    public static final String URL_READY = "ready";
    public static final String URL_APPROVE = "approve";
    public static final String URL_CONFIRM = "confirm";
    public static final String URL_CANCEL = "cancel";
    public static final String URL_API = "api";
    public static final String URL_CHANNELS = "channels";
    public static final String URL_ANSWER = "answer";
    public static final String NULL_STRING = "null";
    public static final String PREFIX_ROLE = "ROLE_";

    public static final long DEFAULT_QUEUE_DELAY = 20L;
    public static final Integer HTTP_STATUS_CODE_OK = 200;
    public static final Integer HTTP_STATUS_CODE_CREATED = 201;
    public static final Integer HTTP_STATUS_CODE_NO_CONTENT = 204;

    public static final String HTTP_HEADER_API_KEY = "X-API-Key";

    public static final Integer RESULT_CODE_SUCCESSFUL = 0;

    public static final String BOOLEAN_VALUE_STRING_TRUE = "true";
    public static final String BOOLEAN_VALUE_STRING_FALSE = "false";

    public static final String HEADER_X_TOTAL_COUNT = "X-Total-Count";
    public static final String HTTP_CONTENT_TYPE_EXCEL = "application/vnd.ms-excel";

    public static final String SYSTEM_EMAIL = "system@planesg.ai";

    public static final List<String> EXTENSIONS_IMAGE = Arrays.asList("bmp", "gif", "jpg", "png", "jpeg");
    public static final List<String> EXTENSIONS_DOCUMENT = Arrays.asList("pdf", "txt", "doc", "docx");
    public static final List<String> EXTENSIONS_EXCEL = Arrays.asList("xls", "xlsx");
    public static final List<String> EXTENSIONS_VIDEO = Arrays.asList("mp4", "avi", "mov", "mpg", "wmv", "mpeg");
    public static final String FILE_EXTENSION_PDF = "pdf";

    public static final int QUERY_MAX_DECIMAL_LENGTH = 10;
    public static final String SHORT_ANSWER_YES = "Y";
    public static final String SHORT_ANSWER_NO = "N";

    private Constants() {}
}
