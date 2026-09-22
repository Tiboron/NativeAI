package com.tiboron.childrensmath;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Color;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.content.Context;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(new MathView(this));
    }

    static class MathView extends View {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Random random = new Random();
        final Handler handler = new Handler(Looper.getMainLooper());
        final RectF answerHit = new RectF();
        final RectF nextHit = new RectF();
        final int BLACK = Color.rgb(28,28,28);
        final int GRAY = Color.rgb(105,105,105);
        final int LINE = Color.rgb(35,35,35);
        Problem problem;
        boolean controlsVisible = false;
        boolean answerVisible = false;
        final Runnable revealControls = () -> { controlsVisible = true; invalidate(); };

        MathView(Context c) {
            super(c);
            setBackgroundColor(Color.WHITE);
            setFocusable(true);
            generateProblem();
        }

        void generateProblem() {
            handler.removeCallbacks(revealControls);
            controlsVisible = false;
            answerVisible = false;
            if (random.nextBoolean()) {
                int count = random.nextBoolean() ? 2 : 3;
                int[] nums = new int[count];
                for (int i = 0; i < count; i++) nums[i] = 1 + random.nextInt(999999);
                problem = new Problem(true, nums);
            } else {
                int minuend = 2 + random.nextInt(999998);
                int subtrahend = 1 + random.nextInt(minuend - 1);
                problem = new Problem(false, new int[]{minuend, subtrahend});
            }
            invalidate();
            handler.postDelayed(revealControls, 5000);
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth(), h = getHeight();
            paint.setStyle(Paint.Style.FILL);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            paint.setColor(GRAY);
            paint.setTextSize(dp(13));
            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(problem.addition ? "ADDITION" : "SUBTRACTION", dp(20), dp(31), paint);

            float digitSize = Math.min(dp(43), w * 0.105f);
            paint.setTextSize(digitSize);
            paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
            paint.setColor(BLACK);
            paint.setTextAlign(Paint.Align.RIGHT);

            String[] formatted = new String[problem.numbers.length];
            for (int i = 0; i < problem.numbers.length; i++)
                formatted[i] = String.format(Locale.US, "%,d", problem.numbers[i]);

            float right = w * 0.79f;
            float rowGap = digitSize * 1.18f;
            float startY = h * 0.34f;

            for (int i = 0; i < formatted.length; i++) {
                String prefix = (i == formatted.length - 1)
                        ? (problem.addition ? "+ " : "− ")
                        : "  ";
                canvas.drawText(prefix + formatted[i], right, startY + i * rowGap, paint);
            }

            float lineY = startY + (formatted.length - 0.42f) * rowGap;
            paint.setStrokeWidth(dp(2));
            paint.setColor(LINE);
            canvas.drawLine(w * 0.29f, lineY, right + dp(6), lineY, paint);

            if (answerVisible) {
                paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
                paint.setTextSize(digitSize);
                paint.setColor(BLACK);
                paint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(String.format(Locale.US, "%,d", problem.answer()),
                        right, lineY + rowGap, paint);
            }

            if (controlsVisible) {
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                paint.setTextSize(dp(13));
                paint.setTextAlign(Paint.Align.LEFT);
                paint.setColor(BLACK);

                float nextW = dp(110), nextH = dp(48);
                float nextX = w - nextW - dp(20), nextY = h - nextH - dp(16);
                float answerX = w - dp(20);
                float answerY = nextY - dp(18);
                canvas.drawText("ANSWER", answerX, answerY, paint);
                answerHit.set(answerX - dp(105), answerY - dp(30),
                        answerX + dp(10), answerY + dp(10));
                paint.setColor(BLACK);
                canvas.drawRoundRect(new RectF(nextX, nextY, nextX + nextW, nextY + nextH),
                        dp(8), dp(8), paint);
                paint.setColor(Color.WHITE);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTextSize(dp(16));
                canvas.drawText("NEXT", nextX + nextW / 2, nextY + dp(30), paint);
                nextHit.set(nextX, nextY, nextX + nextW, nextY + nextH);
            }
        }

        @Override public boolean onTouchEvent(MotionEvent e) {
            if (e.getAction() != MotionEvent.ACTION_UP || !controlsVisible) return true;
            if (answerHit.contains(e.getX(), e.getY())) {
                answerVisible = true;
                invalidate();
                return true;
            }
            if (nextHit.contains(e.getX(), e.getY())) {
                generateProblem();
                return true;
            }
            return true;
        }

        float dp(float v) { return v * getResources().getDisplayMetrics().density; }

        static class Problem {
            final boolean addition;
            final int[] numbers;
            Problem(boolean addition, int[] numbers) {
                this.addition = addition;
                this.numbers = numbers;
            }
            int answer() {
                if (addition) {
                    int r = 0;
                    for (int n : numbers) r += n;
                    return r;
                }
                return numbers[0] - numbers[1];
            }
        }
    }
}
