/**
 * File created on 15/05/15 at 01:03
 * Copyright Vyacheslav Krylov, 2014
 */
package org.thunderdog.challegram.util;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;
import android.view.View;

import androidx.annotation.Nullable;

import org.drinkless.td.libcore.telegram.TdApi;
import org.thunderdog.challegram.component.chat.WallpaperView;
import org.thunderdog.challegram.theme.Theme;
import org.thunderdog.challegram.theme.ThemeColorId;
import org.thunderdog.challegram.theme.ThemeDelegate;
import org.thunderdog.challegram.theme.ThemeId;
import org.thunderdog.challegram.theme.ThemeSet;
import org.thunderdog.challegram.tool.Screen;

import me.vkryl.core.ColorUtils;
import me.vkryl.core.unit.BitwiseUtils;

public class CustomTypefaceSpan extends MetricAffectingSpan {
  private static final int FLAG_FAKE_BOLD = 1;
  private static final int FLAG_REMOVE_UNDERLINE = 1 << 1;
  private static final int FLAG_NO_BACKGROUND_TRANSPARENCY = 1 << 2;
  private static final int FLAG_NEED_UNDERLINE = 1 << 3;
  private static final int FLAG_NEED_STRIKETHROUGH = 1 << 4;

  private @Nullable Typeface typeface;
  private @ThemeColorId int colorId;
  @Nullable
  private ThemeDelegate forcedTheme;
  private TdApi.TextEntityType type;
  private int flags;
  private float textSizeDp;

  private @ThemeColorId int transparentColorId;
  private WallpaperView boundTransparencyView;

  private @ThemeColorId int backgroundColorId;

  private Object tag;

  public interface OnClickListener {
    boolean onClick (View view, CustomTypefaceSpan span);
  }

  public CustomTypefaceSpan (final @Nullable Typeface typeface, final int colorId) {
    this.typeface = typeface;
    this.colorId = colorId;
  }

  public CustomTypefaceSpan setBackgroundColorId (@ThemeColorId int colorId) {
    return setBackgroundColorId(colorId, false);
  }

  public CustomTypefaceSpan setBackgroundColorId (@ThemeColorId int colorId, boolean noTransparency) {
    this.backgroundColorId = colorId;
    this.flags = BitwiseUtils.setFlag(flags, FLAG_NO_BACKGROUND_TRANSPARENCY, noTransparency);
    return this;
  }

  public CustomTypefaceSpan setTextSizeDp (float textSizeDp) {
    this.textSizeDp = textSizeDp;
    return this;
  }

  public CustomTypefaceSpan setRemoveUnderline (boolean removeUnderline) {
    this.flags = BitwiseUtils.setFlag(flags, FLAG_REMOVE_UNDERLINE, removeUnderline);
    return this;
  }

  public CustomTypefaceSpan setNeedUnderline (boolean needUnderline) {
    this.flags = BitwiseUtils.setFlag(flags, FLAG_NEED_UNDERLINE, needUnderline);
    return this;
  }

  public CustomTypefaceSpan setNeedStrikethrough (boolean needStrikethrough) {
    this.flags = BitwiseUtils.setFlag(flags, FLAG_NEED_STRIKETHROUGH, needStrikethrough);
    return this;
  }

  public CustomTypefaceSpan setFakeBold (boolean fakeBold) {
    this.flags = BitwiseUtils.setFlag(flags, FLAG_FAKE_BOLD, fakeBold);
    return this;
  }

  public CustomTypefaceSpan setTypeface (@Nullable Typeface typeface) {
    this.typeface = typeface;
    return this;
  }

  public CustomTypefaceSpan setColorId (int colorId) {
    this.colorId = colorId;
    return this;
  }

  public CustomTypefaceSpan setForceThemeId (int forceThemeId) {
    this.forcedTheme = forceThemeId != ThemeId.NONE ? ThemeSet.getOrLoadTheme(forceThemeId, true) : null;
    return this;
  }

  public CustomTypefaceSpan setForcedTheme (@Nullable ThemeDelegate theme) {
    this.forcedTheme = theme;
    return this;
  }

  public CustomTypefaceSpan setEntityType (TdApi.TextEntityType type) {
    this.type = type;
    setNeedUnderline(type != null && type.getConstructor() == TdApi.TextEntityTypeUnderline.CONSTRUCTOR);
    setNeedStrikethrough(type != null && type.getConstructor() == TdApi.TextEntityTypeStrikethrough.CONSTRUCTOR);
    return this;
  }

  public CustomTypefaceSpan setTransparencyColorId (@ThemeColorId int transparentColorId, WallpaperView wallpaperView) {
    this.transparentColorId = transparentColorId;
    this.boundTransparencyView = wallpaperView;
    return this;
  }

  public CustomTypefaceSpan setTag (Object tag) {
    this.tag = tag;
    return this;
  }

  public Object getTag () {
    return tag;
  }

  public TdApi.TextEntityType getEntityType () {
    return type;
  }

  @Nullable
  public Typeface getTypeface () {
    return typeface;
  }

  public boolean useFakeBold () {
    return (flags & FLAG_FAKE_BOLD) != 0;
  }

  @Override
  public void updateDrawState (final TextPaint drawState) {
    apply(drawState);
  }

  @Override
  public void updateMeasureState (final TextPaint paint) {
    apply(paint);
  }

  public void onClick (View view) {
    if (onClickListener != null) {
      onClickListener.onClick(view, this);
    }
  }

  public boolean isClickable () {
    // TODO default click behavior for known entities
    return onClickListener != null;
  }

  private OnClickListener onClickListener;

  public CustomTypefaceSpan setOnClickListener (OnClickListener onClickListener) {
    this.onClickListener = onClickListener;
    return this;
  }

  public OnClickListener getOnClickListener () {
    return onClickListener;
  }

  private void apply (final TextPaint paint) {
    paint.setFakeBoldText((flags & FLAG_FAKE_BOLD) != 0);
    if (backgroundColorId != 0) {
      int color = forcedTheme != null ? forcedTheme.getColor(backgroundColorId) : Theme.getColor(backgroundColorId);
      if ((flags & FLAG_NO_BACKGROUND_TRANSPARENCY) != 0 && Color.alpha(color) < 255) {
        color = ColorUtils.compositeColor(0xff000000, color);
      }
      paint.bgColor = color;
    }
    if ((flags & FLAG_REMOVE_UNDERLINE) != 0) {
      paint.setUnderlineText(false);
    }
    if ((flags & FLAG_NEED_UNDERLINE) != 0) {
      paint.setUnderlineText(true);
    }
    if ((flags & FLAG_NEED_STRIKETHROUGH) != 0) {
      paint.setStrikeThruText(true);
    }
    if (typeface != null) {
      final Typeface oldTypeface = paint.getTypeface();
      final int oldStyle = oldTypeface != null ? oldTypeface.getStyle() : 0;
      final int fakeStyle = oldStyle & ~typeface.getStyle();
      if ((fakeStyle & Typeface.BOLD) != 0) {
        paint.setFakeBoldText(true);
      }
      if ((fakeStyle & Typeface.ITALIC) != 0) {
        paint.setTextSkewX(-0.25f);
      }
      paint.setTypeface(typeface);
    }
    if (textSizeDp != 0) {
      paint.setTextSize(Screen.dp(textSizeDp));
    }
    if (colorId != 0) {
      int color;
      if (forcedTheme != null) {
        color = forcedTheme.getColor(colorId);
      } else if (transparentColorId != 0 && transparentColorId != colorId && boundTransparencyView != null) {
        float factor = boundTransparencyView.getBackgroundTransparency();
        if (factor == 0f) {
          color = Theme.getColor(colorId);
        } else if (factor == 1f) {
          color = Theme.getColor(transparentColorId);
        } else {
          color = ColorUtils.fromToArgb(Theme.getColor(colorId), Theme.getColor(transparentColorId), factor);
        }
      } else {
        color = Theme.getColor(colorId);
      }
      paint.setColor(color);
    } else if (transparentColorId != 0 && boundTransparencyView != null) {
      int color = paint.getColor();
      float factor = boundTransparencyView.getBackgroundTransparency();
      paint.setColor(ColorUtils.fromToArgb(color, Theme.getColor(transparentColorId), factor));
    }
  }
}