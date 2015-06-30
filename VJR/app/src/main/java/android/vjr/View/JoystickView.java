package android.vjr.View;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.vjr.R;


public class JoystickView extends View implements Runnable {
    // Constants
    private final double RAD = 57.2957795;
    public final static long DEFAULT_LOOP_INTERVAL = 50; // 10 ms
    public final static boolean DEFAULT_BACK_MIDDLE = true;
    public final static boolean DEFAULT_STAY_POS = false;
    public boolean MOD_DEFAULT = DEFAULT_STAY_POS;
    public final static int FRONT = 3;
    public final static int FRONT_RIGHT = 2 ;
    public final static int RIGHT = 1;
    public final static int RIGHT_BOTTOM =8 ;
    public final static int BOTTOM = 7;
    public final static int BOTTOM_LEFT =6 ;
    public final static int LEFT = 5;
    public final static int LEFT_FRONT = 4;
    // Variables
    private OnJoystickMoveListener onJoystickMoveListener; // Listener
    private Thread thread = new Thread(this);
    private long loopInterval = DEFAULT_LOOP_INTERVAL;
    private int xPosition = 0; // Touch x position
    private int yPosition = 0; // Touch y position
    private double centerX = 0; // Center view x position
    private double centerY = 0; // Center view y position
    private Paint mainCircle;
    private Paint secondaryCircle;
    private Paint button;
    private Paint horizontalLine;
    private Paint verticalLine;
    private int joystickRadius;
    private int buttonRadius;
    private int lastAngle = 0;
    private int lastPower = 0;
    private Bitmap _joystick = (Bitmap) BitmapFactory.decodeResource(getContext().getResources(), R.drawable.joystick);
    public  int joystickMode = 0; // 0 = angle 1 = puissance
    private Paint backgroundPaint;
    // end drawing tools
    private int yReelPosition= 0,xReelPosition=0;
    private boolean movedButton = true;
    Canvas canvas;
    public JoystickView(Context context) {
        super(context);
    }

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initJoystickView();
    }
    public void setModDefault(boolean defaultMod){
        MOD_DEFAULT = defaultMod;
    }
    public JoystickView(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);
        initJoystickView();
    }

    protected void initJoystickView() {
        mainCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        mainCircle.setColor(Color.BLACK);
        mainCircle.setStyle(Paint.Style.STROKE);
        secondaryCircle = new Paint();
        secondaryCircle.setColor(Color.BLACK);
        secondaryCircle.setStrokeWidth(2);
        secondaryCircle.setStyle(Paint.Style.STROKE);
        verticalLine = new Paint();
        verticalLine.setStrokeWidth(2);
        verticalLine.setColor(Color.BLACK);
        horizontalLine = new Paint();
        horizontalLine.setStrokeWidth(2);
        horizontalLine.setColor(Color.BLACK);
        button = new Paint(Paint.ANTI_ALIAS_FLAG);
        button.setColor(Color.RED);
        button.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onFinishInflate() {}

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        // before measure, get the center of view
        xPosition = (int) getWidth() / 2;
        yPosition = (int) getWidth() / 2;
        int d = Math.min(xNew, yNew);
        buttonRadius = (int) (d / 2 * 0.25);
        joystickRadius = (int) (d / 2 * 0.75);

    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // setting the measured values to resize the view to a certain width and
        // height
        int d = Math.min(measure(widthMeasureSpec), measure(heightMeasureSpec));
        setMeasuredDimension(d, d);
    }
    private int measure(int measureSpec) {
        int result = 0;
        // Decode the measurement specifications.
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.UNSPECIFIED) {
            // Return a default size of 200 if no bounds are specified.
            result = 200;
        } else {
            // As you want to fill the available space
            // always return the full available bounds.
            result = specSize;
        }
        return result;
    }
    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        this.canvas = canvas;
        centerX = (getWidth()) / 2;
        centerY = (getHeight()) / 2;
        // painting the main circle
        Log.d("repaint", "repaint");
        backgroundPaint = new Paint();
        backgroundPaint.setFilterBitmap(true);
        canvas.drawCircle((int) centerX, (int) centerY, joystickRadius, mainCircle);
        // painting the secondary circle
        canvas.drawCircle((int) centerX, (int) centerY, joystickRadius / 2,
                secondaryCircle);
        // paint lines
        canvas.drawLine((float) centerX, (float) centerY, (float) centerX, (float) (centerY - joystickRadius), verticalLine);
        canvas.drawLine((float) (centerX - joystickRadius), (float) centerY,(float) (centerX + joystickRadius), (float) centerY,horizontalLine);
        canvas.drawLine((float) centerX, (float) (centerY + joystickRadius),(float) centerX, (float) centerY, horizontalLine);
        // painting the move button
        //public void drawBitmap (int[] colors, int offset, int stride, int x, int y, int width, int height, boolean hasAlpha, Paint paint)

        if (joystickMode == 1)
            canvas.drawBitmap(_joystick, (int) centerX - _joystick.getWidth() / 2, yPosition - _joystick.getHeight() / 2, null);
        else
            canvas.drawBitmap(_joystick, xPosition - _joystick.getWidth() / 2, (int) centerY - _joystick.getHeight() / 2, null);
        Log.d("Moved : " , Boolean.toString(MOD_DEFAULT));

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP && MOD_DEFAULT == DEFAULT_BACK_MIDDLE){
            Log.d("Evt ", event.toString());
            xPosition = (int) centerX;
            yPosition = (int) centerY;
            xReelPosition = (int) centerX;
            yReelPosition = (int) centerY;

            return true;
        }

        xPosition = (int) event.getX();
        yPosition = (int) event.getY();
        xReelPosition = (int) event.getX();
        yReelPosition = (int) event.getY();
        movedButton = true;
        double abs = Math.sqrt((xPosition - centerX) * (xPosition - centerX)
                + (yPosition - centerY) * (yPosition - centerY));
        if (abs > joystickRadius) {
            xPosition = (int) ((xPosition - centerX) * joystickRadius / abs + centerX);
            yPosition = (int) ((yPosition -centerY ) * joystickRadius / abs + centerY);
        }
        //get the coord
        Log.d("X coord : ",Integer.toString(xPosition));

        //get the coord
        Log.d("Y coord : ",Integer.toString(yPosition));
        invalidate();
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
        thread = new Thread(this);
        thread.start();
        if (onJoystickMoveListener != null)
            onJoystickMoveListener.onValueChanged(getAngle(), getPower(),getDirection());
        return true;
    }
    private int getAngle() {
        Log.d("wdth  ", Integer.toString(getWidth()));
        return mapRange(xReelPosition,(int) centerX - joystickRadius, (int) centerX + joystickRadius,-90,90);

    }
    public int mapRange(long x, long in_min, long in_max, long out_min, long out_max)
    {
        long target =  (int) ((x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min);
        if(target < out_min)
            target = out_min;
        else if (target > out_max)
            target = out_max;
        return (int) target;
    }

    private int getPower() {
        return mapRange(yReelPosition,(int) centerY - joystickRadius, (int) centerY + joystickRadius,-100,100) * -1;

    }
    private int getDirection() {
        if (lastPower == 0 && lastAngle == 0) {
            return 0;
        }
        int a = 0;
        if (lastAngle <= 0) {
            a = (lastAngle * -1) + 90;
        } else if (lastAngle > 0) {
            if (lastAngle <= 90) {
                a = 90 - lastAngle;
            } else {
                a = 360 - (lastAngle - 90);
            }
        }
        int direction = (int) (((a + 22) / 45) + 1);
        if (direction > 8) {
            direction = 1;
        }
        return direction;
    }

    public void setOnJoystickMoveListener(OnJoystickMoveListener listener,
                                          long repeatInterval) {
        this.onJoystickMoveListener = listener;
        this.loopInterval = repeatInterval;
    }
    public static interface OnJoystickMoveListener {
        public void onValueChanged(int angle, int power, int direction);
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            post(new Runnable() {
                public void run() {
                    if (onJoystickMoveListener != null)
                        onJoystickMoveListener.onValueChanged(getAngle(),getPower(), getDirection());
                }
            });
            try {

                Thread.sleep(loopInterval);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}