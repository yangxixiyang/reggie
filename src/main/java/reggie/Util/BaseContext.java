package reggie.Util;

/**
 * 基于ThreadLocal封装的工具类，用于保存和获取当前用户的id
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal=new ThreadLocal<>();

    public static void setCurrentId(long id){
        threadLocal.set(id);
    }
    public static long getCurrentId(){
        return threadLocal.get();
    }
}
