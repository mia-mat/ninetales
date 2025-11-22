package ws.mia.ninetales.util;

public class HttpUtil {
	public static boolean isSuccess(int statusCode) {
		return statusCode >= 200 && statusCode < 300;
	}


}
