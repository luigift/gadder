package co.gadder.gadder;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;

public class Constants {

    // Database indexation
    public final static String VERSION = "v0";
    public final static String USERS = "users";
    public final static String USER_TOKEN = "user_token";
    public final static String USER_PHONE = "user_phone";
    public final static String USER_FRIENDS = "user_friends";
    public final static String USER_ACTIVITIES = "user_activities";
    public final static String USER_NOTIFICATIONS = "user_notifications";

    // Weather conditions
    public final static String CLEAR = "clear";
    public final static String FOGGY = "foggy";
    public final static String SNOWY = "snowy";
    public final static String CLOUDY = "cloudy";
    public final static String HAZY = "hazy";
    public final static String ICY = "icy";
    public final static String RAINY = "rainy";
    public final static String STORMY = "stormy";
    public final static String WINDY = "windy";
    public final static String UNKNOWN = "unknown";

    final static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    public static Bitmap textAsBitmap(String text, float textSize, int textColor) {
        Paint paint = new Paint(ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.0f); // round
        int height = (int) (baseline + paint.descent() + 0.0f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }
}
