package reggie.Util;

/**
 * 自定义业务类异常
 */
public class CustomException extends RuntimeException{
    public CustomException(String message) {
        super(message);
    }
}
