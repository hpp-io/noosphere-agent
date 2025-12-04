package io.hpp.noosphere.agent.service.util;

import static io.hpp.noosphere.agent.config.Constants.*;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

import ch.qos.logback.core.util.FileSize;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.google.common.net.InternetDomainName;
import com.google.gson.*;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber;
import io.hpp.noosphere.agent.config.Constants;
import io.hpp.noosphere.agent.web.rest.errors.ErrorConstants;
import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.*;
import java.text.Normalizer.Form;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections4.comparators.ComparatorChain;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public class CommonUtil {

    private static final Logger log = LoggerFactory.getLogger(CommonUtil.class);

    public static boolean isValid(String value) {
        return value != null && !value.isEmpty() && !NULL_STRING.equals(value.trim().toLowerCase());
    }

    public static boolean isUUID(String value) {
        if (CommonUtil.isValid(value)) {
            try {
                UUID uuid = UUID.fromString(value);
                return true;
            } catch (IllegalArgumentException exception) {
                // do nothing
            }
        }
        return false;
    }

    public static boolean isValid(UUID value) {
        return value != null && !value.toString().isEmpty() && !NULL_STRING.equals(value.toString().trim().toLowerCase());
    }

    public static String removeInvalidValues(String value) {
        if ("null".equals(value)) {
            return null;
        } else {
            return value;
        }
    }

    public static boolean isValid(Boolean value) {
        return value != null;
    }

    public static boolean isValid(Long value) {
        return value != null;
    }

    public static boolean isValid(Instant value) {
        return value != null;
    }

    public static boolean isValid(LocalDate value) {
        return value != null;
    }

    public static boolean isValid(Date value) {
        return value != null;
    }

    public static boolean isValid(Integer value) {
        return value != null;
    }

    public static boolean isValid(Double value) {
        return value != null;
    }

    public static boolean isValid(BigDecimal value) {
        return value != null;
    }

    public static boolean isValid(List<?> value) {
        return value != null && !value.isEmpty();
    }

    public static boolean isValid(Set<?> value) {
        return value != null && !value.isEmpty();
    }

    public static boolean isValid(Map<?, ?> value) {
        return value != null && !value.isEmpty();
    }

    public static String stripPhoneNumber(String value) {
        if (isValid(value)) {
            value = value.trim();
            value = value.replaceAll("-", "");
            value = value.replaceAll(" ", "");
            value = value.replaceAll("\\)", "");
            value = value.replaceAll("\\(", "");
        }
        return value;
    }

    private static Gson _gson = null;

    public static GsonBuilder addIntegerWithQuotesTypeAdapter(GsonBuilder gsonBuilder) {
        gsonBuilder.registerTypeAdapter(
            Integer.class,
            new JsonSerializer<Integer>() {
                @Override
                public JsonElement serialize(Integer src, Type typeOfSrc, JsonSerializationContext context) {
                    return new JsonPrimitive(Objects.requireNonNull(src != null ? src.toString() : null));
                }
            }
        );
        return gsonBuilder;
    }

    public static GsonBuilder createGsonBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(
            Instant.class,
            new JsonDeserializer<Instant>() {
                @Override
                public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    return Objects.requireNonNull(
                        (json != null && isValid(json.getAsString()))
                            ? Instant.from(ISO_OFFSET_DATE_TIME.withZone(ZoneId.of("UTC")).parse(json.getAsString()))
                            : null
                    );
                }
            }
        );
        gsonBuilder.registerTypeAdapter(
            Instant.class,
            new JsonSerializer<Instant>() {
                @Override
                public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
                    return new JsonPrimitive(
                        Objects.requireNonNull(src != null ? ISO_OFFSET_DATE_TIME.withZone(ZoneId.of("UTC")).format(src) : null)
                    );
                }
            }
        );
        return gsonBuilder;
    }

    public static Gson createGson() {
        if (_gson != null) {
            return _gson;
        }
        GsonBuilder gsonBuilder = createGsonBuilder();
        _gson = gsonBuilder.create();
        return _gson;
    }

    public static GsonBuilder createGsonBuilderWithDateTime(String dateFormat) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(
            Instant.class,
            new JsonDeserializer<Instant>() {
                @Override
                public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    return Objects.requireNonNull(
                        (json != null && isValid(json.getAsString())) ? parseInstant(json.getAsString(), dateFormat) : null
                    );
                }
            }
        );
        gsonBuilder.registerTypeAdapter(
            Instant.class,
            new JsonSerializer<Instant>() {
                @Override
                public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
                    return new JsonPrimitive(Objects.requireNonNull(src != null ? formatDate(src, dateFormat) : null));
                }
            }
        );
        return gsonBuilder;
    }

    public static Gson createGsonWithDateTime(String dateFormat) {
        if (_gson != null) {
            return _gson;
        }
        GsonBuilder gsonBuilder = createGsonBuilderWithDateTime(dateFormat);
        _gson = gsonBuilder.create();
        return _gson;
    }

    public static String toJson(final Object object) {
        return toJson(object, false);
    }

    public static String toJson(final Object object, boolean pretty) {
        Gson gson = null;
        if (pretty) {
            GsonBuilder gsonBuilder = createGsonBuilder();
            gsonBuilder.setPrettyPrinting();
            gson = gsonBuilder.create();
        } else {
            gson = createGson();
        }
        return gson.toJson(object);
    }

    public static <T> T fromJson(final String value, Class<T> valueType) {
        Gson gson = createGson();
        return gson.fromJson(value, valueType);
    }

    public static <T> T fromJson(final String value, Type valueType) {
        Gson gson = createGson();
        return gson.fromJson(value, valueType);
    }

    public static String toJsonWithDateTime(final Object object, String dateFormat) {
        return toJsonWithDateTime(object, false, dateFormat);
    }

    public static String toJsonWithIntegerWithQuotes(final Object object, boolean pretty) {
        Gson gson = null;
        GsonBuilder gsonBuilder = createGsonBuilder();
        gsonBuilder = addIntegerWithQuotesTypeAdapter(gsonBuilder);
        if (pretty) {
            gsonBuilder.setPrettyPrinting();
        }
        gson = gsonBuilder.create();
        return gson.toJson(object);
    }

    public static <T> T fromJsonWithDateTime(final String value, Class<T> valueType, String dateFormat) {
        Gson gson = createGsonWithDateTime(dateFormat);
        return gson.fromJson(value, valueType);
    }

    public static <T> T fromJsonWithDateTime(final String value, Type valueType, String dateFormat) {
        Gson gson = createGsonWithDateTime(dateFormat);
        return gson.fromJson(value, valueType);
    }

    public static String toJsonWithDateTime(final Object object, boolean pretty, String dateFormat) {
        Gson gson = null;
        if (pretty) {
            GsonBuilder gsonBuilder = createGsonBuilderWithDateTime(dateFormat);
            gsonBuilder.setPrettyPrinting();
            gson = gsonBuilder.create();
        } else {
            gson = createGson();
        }
        return gson.toJson(object);
    }

    public static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // StdDateFormat is ISO8601 since jackson 2.9
        objectMapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }

    public static String toJsonWithJackson(final Object object) throws JsonProcessingException {
        ObjectMapper objectMapper = createObjectMapper();
        return objectMapper.writeValueAsString(object);
    }

    public static <T> T fromJsonWithJackson(final String value, Class<T> clazz) throws JsonProcessingException {
        ObjectMapper objectMapper = createObjectMapper();
        return objectMapper.readValue(value, clazz);
    }

    public static String base64Encode(final String input) throws UnsupportedEncodingException {
        return Base64.getEncoder().encodeToString(input.getBytes(Constants.DEFAULT_ENCODING));
    }

    public static String getUserIp(HttpServletRequest request, String httpXForwardedFor) {
        String historyUserIp = request.getRemoteAddr();
        if (httpXForwardedFor != null && !httpXForwardedFor.isEmpty()) {
            historyUserIp = httpXForwardedFor;
        }
        return historyUserIp;
    }

    public static String formatDate(Instant input, String format) {
        if (isValid(input) && isValid(format)) {
            DateTimeFormatter simpleDateFormat = DateTimeFormatter.ofPattern(format).withZone(
                TimeZone.getTimeZone("Asia/Seoul").toZoneId()
            );
            return simpleDateFormat.format(input);
        } else {
            return null;
        }
    }

    public static String formatLocalIso8601(Instant input) {
        if (isValid(input)) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            return formatter.format(input);
        } else {
            return null;
        }
    }

    public static String formatUTCIso8601(Instant input) {
        if (isValid(input)) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
            return formatter.format(input);
        } else {
            return null;
        }
    }

    public static String formatDate(LocalDate input, String format) {
        if (isValid(input) && isValid(format)) {
            DateTimeFormatter simpleDateFormat = DateTimeFormatter.ofPattern(format).withZone(
                TimeZone.getTimeZone("Asia/Seoul").toZoneId()
            );
            return simpleDateFormat.format(input);
        } else {
            return null;
        }
    }

    public static String formatDate(Date input, String format) {
        if (isValid(input) && isValid(format)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.KOREAN);
            return simpleDateFormat.format(input);
        } else {
            return null;
        }
    }

    public static LocalDate parseLocalDate(String input, String format) {
        if (isValid(input) && isValid(format)) {
            DateTimeFormatter simpleDateFormat = DateTimeFormatter.ofPattern(format).withZone(
                TimeZone.getTimeZone("Asia/Seoul").toZoneId()
            );
            return LocalDate.from(simpleDateFormat.parse(input));
        } else {
            return null;
        }
    }

    public static Instant parseInstant(String input, String format) {
        if (isValid(input) && isValid(format)) {
            DateTimeFormatter simpleDateFormat = DateTimeFormatter.ofPattern(format).withZone(
                TimeZone.getTimeZone("Asia/Seoul").toZoneId()
            );
            return Instant.from(simpleDateFormat.parse(input));
        } else {
            return null;
        }
    }

    public static Instant parseInstantFromDateString(String input, String format) {
        if (isValid(input) && isValid(format)) {
            final DateTimeFormatter simpleDateFormat = new DateTimeFormatterBuilder()
                .appendPattern(format)
                .parseDefaulting(ChronoField.NANO_OF_DAY, 0)
                .toFormatter()
                .withZone(TimeZone.getTimeZone("Asia/Seoul").toZoneId());
            return Instant.from(simpleDateFormat.parse(input));
        } else {
            return null;
        }
    }

    public static Instant parseInstantFromDateTimeString(String input, String format) {
        if (isValid(input) && isValid(format)) {
            final DateTimeFormatter simpleDateFormat = new DateTimeFormatterBuilder()
                .appendPattern(format)
                .toFormatter()
                .withZone(TimeZone.getTimeZone("Asia/Seoul").toZoneId());
            return Instant.from(simpleDateFormat.parse(input));
        } else {
            return null;
        }
    }

    public static Date parseDate(String input, String format) {
        if (isValid(input) && isValid(format)) {
            DateTimeFormatter simpleDateFormat = DateTimeFormatter.ofPattern(format).withZone(
                TimeZone.getTimeZone("Asia/Seoul").toZoneId()
            );
            return Date.from(Instant.from(simpleDateFormat.parse(input)));
        } else {
            return null;
        }
    }

    public static Number parseNumberStringToNumber(String input) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.KOREAN);
        try {
            return numberFormat.parse(input);
        } catch (ParseException | NullPointerException e) {
            log.warn("parseNumberStringToNumber error: {}", e.getMessage());
            //      e.printStackTrace();
        }
        return null;
    }

    public static Boolean isNumberString(String input) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.KOREAN);
        try {
            numberFormat.parse(input);
            return true;
        } catch (ParseException | NullPointerException e) {
            return false;
        }
    }

    public static Integer parseNumberStringToInteger(String input) {
        Number number = parseNumberStringToNumber(input);
        return number != null ? number.intValue() : null;
    }

    public static Long parseNumberStringToLong(String input) {
        Number number = parseNumberStringToNumber(input);
        return number != null ? number.longValue() : null;
    }

    public static Double parseNumberStringToDouble(String input) {
        Number number = parseNumberStringToNumber(input);
        return number != null ? number.doubleValue() : null;
    }

    public static Float parseNumberStringToFloat(String input) {
        Number number = parseNumberStringToNumber(input);
        return number != null ? number.floatValue() : null;
    }

    public static BigDecimal parseNumberStringToBigDecimal(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        try {
            BigDecimal value = new BigDecimal(input.trim());
            return value.stripTrailingZeros();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid input for BigDecimal: " + input, e);
        }
    }

    public static Instant createInstantFromYear(Integer year) {
        ZonedDateTime now = ZonedDateTime.now()
            .withYear(year)
            .withMonth(1)
            .withDayOfMonth(1)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);
        return now.toInstant();
    }

    public static ZonedDateTime getKoreanDateTimeTruncatedByDay(Instant timestamp) {
        ZonedDateTime zonedDateTime = timestamp.atZone(TimeZone.getTimeZone("Asia/Seoul").toZoneId());
        return zonedDateTime.truncatedTo(ChronoUnit.DAYS);
    }

    public static ZonedDateTime getDefaultZonedDateTime(Instant timestamp) {
        return getZonedDateTime(timestamp, TimeZone.getDefault());
    }

    public static ZonedDateTime getZonedDateTime(Instant timestamp, TimeZone timeZone) {
        if (timestamp != null && timeZone != null) {
            return timestamp.atZone(timeZone.toZoneId());
        } else {
            return null;
        }
    }

    public static Integer getMonthOfYearFromDefaultZonedDateTime(Instant timestamp) {
        ZonedDateTime zonedDateTime = getDefaultZonedDateTime(timestamp);
        if (zonedDateTime != null) {
            return zonedDateTime.getMonthValue();
        } else {
            return null;
        }
    }

    public static Integer getYearFromDefaultZonedDateTime(Instant timestamp) {
        ZonedDateTime zonedDateTime = getDefaultZonedDateTime(timestamp);
        if (zonedDateTime != null) {
            return zonedDateTime.getYear();
        } else {
            return null;
        }
    }

    public static Integer getDayOfMonthFromDefaultZonedDateTime(Instant timestamp) {
        ZonedDateTime zonedDateTime = getDefaultZonedDateTime(timestamp);
        if (zonedDateTime != null) {
            return zonedDateTime.getDayOfMonth();
        } else {
            return null;
        }
    }

    public static int getWeekOfMonth(ZonedDateTime timestamp, int weekNumber) {
        ZonedDateTime temp = timestamp.with(ChronoField.ALIGNED_WEEK_OF_YEAR, weekNumber);
        Calendar now = Calendar.getInstance();
        now.set(Calendar.YEAR, temp.getYear());
        now.set(Calendar.MONTH, temp.getMonthValue());
        now.set(Calendar.DAY_OF_MONTH, temp.getDayOfMonth());
        return now.get(Calendar.WEEK_OF_MONTH);
    }

    public static int getMonthOfYear(ZonedDateTime timestamp, int weekNumber) {
        ZonedDateTime temp = timestamp.with(ChronoField.ALIGNED_WEEK_OF_YEAR, weekNumber);
        Calendar now = Calendar.getInstance();
        now.set(Calendar.YEAR, temp.getYear());
        now.set(Calendar.MONTH, temp.getMonthValue());
        now.set(Calendar.DAY_OF_MONTH, temp.getDayOfMonth());
        return now.get(Calendar.MONTH);
    }

    public static String camelToUnderscore(String input) {
        String regex = "([a-z])([A-Z]+)";
        String replacement = "$1_$2";
        if (input == null) {
            return null;
        }
        return input.replaceAll(regex, replacement).toLowerCase();
    }

    public static String snakeToCamel(String snakeCase) {
        StringBuilder camelCase = new StringBuilder();
        String[] words = snakeCase.split("_");

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (i == 0) {
                camelCase.append(word);
            } else {
                camelCase.append(capitalizeFirstLetter(word));
            }
        }

        return camelCase.toString();
    }

    private static String capitalizeFirstLetter(String word) {
        return Character.toUpperCase(word.charAt(0)) + word.substring(1);
    }

    public static <T> T convertValue(final Object object, Class<T> valueType) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(object, valueType);
    }

    public static String getEmptyStringIfNull(String value) {
        if (value == null) {
            return "";
        } else {
            return value;
        }
    }

    public static List<File> findFiles(String prefix, File directory) {
        List<File> foundFiles = new ArrayList<>();
        File[] list = directory.listFiles();
        if (list != null) {
            for (File file : list) {
                if (file.isDirectory()) {
                    foundFiles.addAll(findFiles(prefix, file));
                } else if (file.getAbsolutePath().contains(prefix)) {
                    foundFiles.add(file);
                }
            }
        }
        return foundFiles;
    }

    public static String generateNewUuidString() {
        return UUID.randomUUID().toString();
    }

    public static UUID generateNewUuid() {
        return UUID.randomUUID();
    }

    public static long convertFileSizeStringToLong(String fileSizeString) {
        long maxFileSize = 10 * 1024 * 1024;
        if (fileSizeString != null && !fileSizeString.isEmpty()) {
            FileSize fileSize = FileSize.valueOf(fileSizeString);
            maxFileSize = fileSize.getSize();
        }
        return maxFileSize;
    }

    public static String normalizeFilename(String inputFilename) {
        inputFilename = inputFilename.replaceAll("[\\\\/:*?\"<>|]", "_");
        return Normalizer.isNormalized(inputFilename, Form.NFD) ? Normalizer.normalize(inputFilename, Form.NFC) : inputFilename;
    }

    public static String formatCurrency(Locale locale, String value) {
        return formatCurrency(locale, value, true);
    }

    public static String formatCurrency(Locale locale, String value, boolean includeSymbol) {
        return formatCurrency(locale, value, includeSymbol, 0);
    }

    public static String formatCurrency(Locale locale, String value, boolean includeSymbol, int numberOfFractionDigits) {
        return formatCurrency(locale, Double.parseDouble(value), includeSymbol, numberOfFractionDigits);
    }

    public static String formatCurrency(Locale locale, double value) {
        return formatCurrency(locale, value, true);
    }

    public static String formatCurrency(Locale locale, double value, boolean includeSymbol) {
        return formatCurrency(locale, value, includeSymbol, 0);
    }

    public static String formatCurrency(Locale locale, double value, boolean includeSymbol, int numberOfFractionDigits) {
        String returnValue = String.valueOf(value);
        try {
            NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);
            if (!includeSymbol) {
                DecimalFormatSymbols decimalFormatSymbols = ((DecimalFormat) numberFormat).getDecimalFormatSymbols();
                decimalFormatSymbols.setCurrencySymbol("");
                numberFormat.setMaximumFractionDigits(numberOfFractionDigits);
                ((DecimalFormat) numberFormat).setDecimalFormatSymbols(decimalFormatSymbols);
            }
            returnValue = numberFormat.format(value);
        } catch (Exception e) {
            log.error("failed to format currency " + value, e);
        }
        return returnValue;
    }

    public static boolean isTemporaryFile(String objectKey) {
        return objectKey.contains("/tmp/");
    }

    public static String stripUUID(String objectKey) {
        String returnValue = objectKey;
        if (isTemporaryFile(objectKey)) {
            String temp = objectKey.substring(36);
            if ("-".equals(temp.substring(0, 1))) {
                returnValue = temp.substring(1);
            }
        }
        return returnValue;
    }

    public static String getFileName(String objectKey) {
        String returnValue = FilenameUtils.getName(objectKey);
        if (isTemporaryFile(objectKey)) {
            String temp = returnValue.substring(36);
            if ("-".equals(temp.substring(0, 1))) {
                returnValue = temp.substring(1);
            }
        }
        return returnValue;
    }

    public static Locale convertLocaleForMessages(Locale locale) {
        Locale newLocale = locale;
        if (Locale.ENGLISH.getLanguage().equals(locale.getLanguage())) {
            newLocale = Locale.ENGLISH;
        } else if (Locale.SIMPLIFIED_CHINESE.getLanguage().equals(locale.getLanguage())) {
            newLocale = Locale.SIMPLIFIED_CHINESE;
        } else if (Locale.JAPANESE.getLanguage().equals(locale.getLanguage())) {
            newLocale = Locale.JAPANESE;
        } else {
            newLocale = Locale.KOREAN;
        }
        return newLocale;
    }

    public static String getFirstString(List<String> valueList) {
        String returnValue = null;
        if (valueList != null && valueList.size() > 0 && CommonUtil.isValid(valueList.get(0))) {
            returnValue = valueList.get(0);
        }
        return returnValue;
    }

    public static String escapeSpecialCharacters(String data) {
        if (isValid(data)) {
            //      String escapedData = data.replaceAll("\\R", "\n");
            String escapedData = data.replaceAll("\"", "\"\"");
            return escapedData;
        } else {
            return "";
        }
    }

    public static String writeCsvCharacters(String inputValue) {
        return CommonUtil.writeCsvCharacters(inputValue, true);
    }

    public static String writeCsvCharacters(String inputValue, boolean escapeSpecialCharacters) {
        if (isValid(inputValue)) {
            return "\"" + (escapeSpecialCharacters ? CommonUtil.escapeSpecialCharacters(inputValue) : inputValue) + "\"";
        } else {
            return "\"\"";
        }
    }

    public static String encodeBase64(String message) {
        byte[] utf8JsonString = message.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(utf8JsonString);
    }

    public static String decodeBase64(String message) throws UnsupportedEncodingException {
        byte[] utf8JsonString = Base64.getDecoder().decode(message);
        return new String(utf8JsonString, Constants.DEFAULT_ENCODING);
    }

    static String EMPTY_SPACE_CHARACTER = String.valueOf((char) 160);
    static String NEXT_LINE_CHARACTER = "\n";

    public static String trimEmptyString(String inputValue) {
        String returnValue = inputValue;
        if (isValid(returnValue)) {
            returnValue = returnValue.trim();
            if (returnValue.substring(0, 1).equals(EMPTY_SPACE_CHARACTER) || returnValue.substring(0, 1).equals(NEXT_LINE_CHARACTER)) {
                returnValue = returnValue.substring(1);
            }
            if (
                returnValue.substring(returnValue.length() - 1).equals(EMPTY_SPACE_CHARACTER) ||
                returnValue.substring(returnValue.length() - 1).equals(NEXT_LINE_CHARACTER)
            ) {
                returnValue = returnValue.substring(0, returnValue.length() - 1);
            }
        }
        return returnValue;
    }

    public static String removeAllEmptySpaces(String inputValue) {
        String returnValue = inputValue;
        if (isValid(returnValue)) {
            returnValue = returnValue.trim();
            returnValue = returnValue.replaceAll(" ", "");
            returnValue = returnValue.replaceAll(NEXT_LINE_CHARACTER, "");
            returnValue = returnValue.replaceAll(EMPTY_SPACE_CHARACTER, "");
        }
        return returnValue;
    }

    static PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    public static String formatPhoneNumber(String input) {
        try {
            input = removeAllEmptySpaces(input);
            Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(input, Locale.KOREA.getCountry());
            return phoneUtil.format(phoneNumber, PhoneNumberFormat.NATIONAL);
        } catch (NumberParseException e) {
            return input;
        }
    }

    public static String formatPhoneNumberAsInternational(String input) {
        try {
            input = removeAllEmptySpaces(input);
            Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(input, Locale.KOREA.getCountry());
            return phoneUtil.format(phoneNumber, PhoneNumberFormat.INTERNATIONAL);
        } catch (NumberParseException e) {
            return input;
        }
    }

    public static boolean isPhoneNumberValid(String input) {
        if (isValid(input)) {
            try {
                input = removeAllEmptySpaces(input);
                return phoneUtil.isValidNumberForRegion(phoneUtil.parse(input, Locale.KOREA.getCountry()), Locale.KOREA.getCountry());
            } catch (NumberParseException e) {
                return false;
            }
        }
        return false;
    }

    public static boolean isEmailValid(String input) {
        if (isValid(input)) {
            return Pattern.compile(EMAIL_REGEX).matcher(input).matches();
        }
        return false;
    }

    public static boolean getBoolean(String input) {
        boolean returnValue = false;
        if (isValid(input)) {
            if (BOOLEAN_VALUE_STRING_TRUE.equalsIgnoreCase(input.trim())) {
                returnValue = true;
            }
        }
        return returnValue;
    }

    public static Boolean getBooleanObject(String input) {
        Boolean returnValue = null;
        if (isValid(input)) {
            if (BOOLEAN_VALUE_STRING_TRUE.equalsIgnoreCase(input.trim())) {
                returnValue = true;
            } else if (BOOLEAN_VALUE_STRING_FALSE.equalsIgnoreCase(input.trim())) {
                returnValue = false;
            }
        }
        return returnValue;
    }

    public static String convertNextLineToBrTag(String input) {
        String output = input.replaceAll("\n", "<br/>");
        return output;
    }

    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }

        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    public static String[] getListPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            if (pd.getPropertyType().getSimpleName().equals("Set") || pd.getPropertyType().getSimpleName().equals("List")) {
                emptyNames.add(pd.getName());
            }
        }

        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    public static String[] getNullAndListPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            if (pd.getPropertyType().getSimpleName().equals("Set") || pd.getPropertyType().getSimpleName().equals("List")) {
                emptyNames.add(pd.getName());
            }
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }

        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    // then use Spring BeanUtils to copy and ignore null using our function
    public static void copyPropertiesIgnoreNull(Object src, Object target) {
        BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
    }

    public static void copyPropertiesIgnoreNullAndList(Object src, Object target) {
        BeanUtils.copyProperties(src, target, getNullAndListPropertyNames(src));
    }

    public static void copyPropertiesIgnoreList(Object src, Object target) {
        BeanUtils.copyProperties(src, target, getListPropertyNames(src));
    }

    public static String extractDomainFromHomepageUrl(String url) {
        InternetDomainName internetDomainName = InternetDomainName.from(url).topPrivateDomain();
        return internetDomainName.toString();
    }

    public static String extractDomainFromEmail(String email) {
        return email.substring(email.indexOf("@") + 1);
    }

    public static Locale getLocaleFromLanguage(String language) {
        Locale locale = Locale.KOREAN;
        if (Locale.ENGLISH.getLanguage().equals(language)) {
            locale = Locale.ENGLISH;
        } else if (Locale.CHINESE.getLanguage().equals(language)) {
            locale = Locale.CHINESE;
        } else if (Locale.JAPANESE.getLanguage().equals(language)) {
            locale = Locale.JAPANESE;
        }
        return locale;
    }

    public static String stripBizrNo(String inputValue) {
        if (isValid(inputValue)) {
            inputValue = removeAllEmptySpaces(inputValue);
            inputValue = inputValue.replaceAll("-", "");
        }
        return inputValue;
    }

    public static Map<String, Object> getAlertParameters(URI type, String errorKey, String propertyName, String value) {
        Map<String, Object> parameters = new HashMap<>();
        String newErrorKey = "";
        if (ErrorConstants.CONSTRAINT_VIOLATION_TYPE.equals(type)) {
            newErrorKey = "error.validation." + errorKey + "." + propertyName;
        } else if (ErrorConstants.ENTITY_NOT_FOUND_TYPE.equals(type)) {
            newErrorKey = "error.validation." + errorKey + "." + propertyName;
        } else {
            newErrorKey = "error." + errorKey + "." + propertyName;
        }
        parameters.put(Constants.PROPERTY_NAME_NEW_ERROR_KEY, newErrorKey);
        //    parameters.put(Constants.PROPERTY_NAME_VALUE, value);
        return parameters;
    }

    public static Double parseDouble(String value) {
        Double returnValue = null;
        if (CommonUtil.isValid(value)) {
            returnValue = Double.valueOf(value);
        }
        return returnValue;
    }

    public static Integer parseInt(String value) {
        Integer returnValue = null;
        if (CommonUtil.isValid(value)) {
            returnValue = Integer.valueOf(value);
        }
        return returnValue;
    }

    public static boolean areListsEqual(List<Object> list1, List<Object> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }
        for (int i = 0; i < list1.size(); i++) {
            if (!list1.get(i).equals(list2.get(i))) {
                return false;
            }
        }
        return true;
    }

    public static void sort(List list, List<String> sortFieldList) {
        List<BeanComparator> comparatorList = new ArrayList<>();
        for (String sortField : sortFieldList) {
            comparatorList.add(new BeanComparator<>(sortField));
        }
        ComparatorChain chain = new ComparatorChain(comparatorList);
        list.sort(chain);
    }

    public static String trim(String value) {
        String returnValue = value;
        if (returnValue != null) {
            returnValue = returnValue.trim();
            if (returnValue.isEmpty() || returnValue.isBlank()) {
                returnValue = null;
            }
        }
        return returnValue;
    }

    public static String formatNumber(Number inputValue) {
        DecimalFormat df = new DecimalFormat("###,###,###,###,###,###.#####################################");
        df.setMaximumFractionDigits(20);
        return df.format(inputValue);
    }

    public static Integer calculateNumberOfDays(Instant startDate, Instant endDate) {
        if (startDate != null && endDate != null) {
            long days = ChronoUnit.DAYS.between(startDate, endDate);
            return (int) days;
        } else {
            return 0;
        }
    }

    public static Instant addMonthsAndDays(Instant inputDate, int months, int days) {
        Instant newDate = inputDate;
        if (newDate != null) {
            String formatDate = CommonUtil.formatDate(newDate, Constants.SIMPLE_DATE_FORMAT_2);
            LocalDate localDate = CommonUtil.parseLocalDate(formatDate, Constants.SIMPLE_DATE_FORMAT_2);
            LocalDateTime localDateTime = localDate.atTime(0, 0, 0);
            if (months > 0) {
                localDateTime = localDateTime.plusMonths(months);
            }
            if (days > 0) {
                localDateTime = localDateTime.plusDays(days);
            }
            newDate = localDateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant();
        }
        return newDate;
    }

    public static Instant truncateInstantToDay(Instant inputDate) {
        if (inputDate != null) {
            String formatDate = CommonUtil.formatDate(inputDate, Constants.SIMPLE_DATE_FORMAT_2);
            LocalDate localDate = CommonUtil.parseLocalDate(formatDate, Constants.SIMPLE_DATE_FORMAT_2);
            LocalDateTime localDateTime = localDate.atTime(0, 0, 0);
            return localDateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant();
        }
        return null;
    }

    public static int moveItemInArray(List list, int moveItemIndex, int moveAfterIndex) {
        int moveItemIndexAfterMoved = -1;
        if (moveItemIndex > -1 && moveAfterIndex > -1 && list != null && !list.isEmpty()) {
            Object moveItem = list.remove(moveItemIndex);
            if (moveItemIndex < moveAfterIndex) {
                moveAfterIndex--;
            }
            if (moveAfterIndex == (list.size() - 1)) {
                list.add(moveItem);
            } else {
                list.add(moveAfterIndex + 1, moveItem);
            }
            moveItemIndexAfterMoved = moveAfterIndex + 1;
        }
        return moveItemIndexAfterMoved;
    }

    public static void moveItemInArray(List list, int moveItemIndex, int moveAfterIndex, int moveItemSecondIndex) {
        int newMovedItemIndex = moveItemInArray(list, moveItemIndex, moveAfterIndex);
        if (newMovedItemIndex > -1 && moveItemSecondIndex > -1 && list != null && !list.isEmpty()) {
            moveItemInArray(list, moveItemSecondIndex, newMovedItemIndex);
        }
    }

    public static BigDecimal addBigDecimal(BigDecimal one, BigDecimal two) {
        BigDecimal result = new BigDecimal(0);
        if (one != null) {
            result = result.add(one);
        }
        if (two != null) {
            result = result.add(two);
        }
        return result;
    }

    /**
     * Extracts the 'hex_data' byte array from the input map and decodes it into a UTF-8 string.
     */
    public static String decodeInputDataToString(Map<String, Object> data) {
        if (data == null) {
            return "";
        }
        Object hexData = data.get("hex_data");
        if (hexData instanceof byte[]) {
            return new String((byte[]) hexData, StandardCharsets.UTF_8);
        }
        return "";
    }
}
