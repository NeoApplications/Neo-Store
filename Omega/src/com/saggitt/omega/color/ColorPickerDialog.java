/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Saul Henriquez
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.color;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.ColorUtils;

import com.android.launcher3.R;
import com.jaredrummler.android.colorpicker.ColorPanelView;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.jaredrummler.android.colorpicker.ColorPickerView;
import com.jaredrummler.android.colorpicker.ColorShape;

import java.util.Arrays;
import java.util.Locale;

public class ColorPickerDialog extends DialogFragment implements ColorPickerView.OnColorChangedListener, TextWatcher {

    private static final String TAG = "ColorPickerDialog";

    public static final int TYPE_CUSTOM = 0;
    public static final int TYPE_PRESETS = 1;

    /**
     * Material design colors used as the default color presets
     */
    public static final int[] MATERIAL_COLORS = {
            0xFFF44336, // RED 500
            0xFFE91E63, // PINK 500
            0xFFFF2C93, // LIGHT PINK 500
            0xFF9C27B0, // PURPLE 500
            0xFF673AB7, // DEEP PURPLE 500
            0xFF3F51B5, // INDIGO 500
            0xFF2196F3, // BLUE 500
            0xFF03A9F4, // LIGHT BLUE 500
            0xFF00BCD4, // CYAN 500
            0xFF009688, // TEAL 500
            0xFF4CAF50, // GREEN 500
            0xFF8BC34A, // LIGHT GREEN 500
            0xFFCDDC39, // LIME 500
            0xFFFFEB3B, // YELLOW 500
            0xFFFFC107, // AMBER 500
            0xFFFF9800, // ORANGE 500
            0xFF795548, // BROWN 500
            0xFF607D8B, // BLUE GREY 500
            0xFF9E9E9E, // GREY 500
    };

    static final int ALPHA_THRESHOLD = 165;

    private static final String ARG_ID = "id";
    private static final String ARG_TYPE = "dialogType";
    private static final String ARG_COLOR = "color";
    private static final String ARG_ALPHA = "alpha";
    private static final String ARG_PRESETS = "presets";
    private static final String ARG_ALLOW_PRESETS = "allowPresets";
    private static final String ARG_ALLOW_CUSTOM = "allowCustom";
    private static final String ARG_DIALOG_TITLE = "dialogTitle";
    private static final String ARG_SHOW_COLOR_SHADES = "showColorShades";
    private static final String ARG_COLOR_SHAPE = "colorShape";
    private static final String ARG_PRESETS_BUTTON_TEXT = "presetsButtonText";
    private static final String ARG_CUSTOM_BUTTON_TEXT = "customButtonText";
    private static final String ARG_SELECTED_BUTTON_TEXT = "selectedButtonText";

    ColorPickerDialogListener colorPickerDialogListener;
    FrameLayout rootView;
    int[] presets;
    @ColorInt
    int color;
    int dialogType;
    int dialogId;
    boolean showColorShades;
    int colorShape;

    // -- PRESETS --------------------------
    ColorPaletteAdapter adapter;
    LinearLayout shadesLayout;
    SeekBar transparencySeekBar;
    TextView transparencyPercText;

    // -- CUSTOM ---------------------------
    ColorPickerView colorPicker;
    ColorPanelView newColorPanel;
    EditText hexEditText;
    boolean showAlphaSlider;
    private int presetsButtonStringRes;
    private boolean fromEditText;
    private int customButtonStringRes;

    private final View.OnTouchListener onPickerTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v != hexEditText && hexEditText.hasFocus()) {
                hexEditText.clearFocus();
                InputMethodManager imm = (InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(hexEditText.getWindowToken(), 0);
                hexEditText.clearFocus();
                return true;
            }
            return false;
        }
    };

    /**
     * Create a new Builder for creating a {@link ColorPickerDialog} instance
     *
     * @return The {@link ColorPickerDialog.Builder builder} to create the {@link ColorPickerDialog}.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialogId = getArguments().getInt(ARG_ID);
        showAlphaSlider = getArguments().getBoolean(ARG_ALPHA);
        showColorShades = getArguments().getBoolean(ARG_SHOW_COLOR_SHADES);
        colorShape = getArguments().getInt(ARG_COLOR_SHAPE);
        if (savedInstanceState == null) {
            color = getArguments().getInt(ARG_COLOR);
            dialogType = getArguments().getInt(ARG_TYPE);
        } else {
            color = savedInstanceState.getInt(ARG_COLOR);
            dialogType = savedInstanceState.getInt(ARG_TYPE);
        }

        rootView = new FrameLayout(getContext());
        if (dialogType == TYPE_CUSTOM) {
            rootView.addView(createPickerView());
        } else if (dialogType == TYPE_PRESETS) {
            rootView.addView(createPresetsView());
        }

        int selectedButtonStringRes = getArguments().getInt(ARG_SELECTED_BUTTON_TEXT);
        if (selectedButtonStringRes == 0) {
            selectedButtonStringRes = R.string.cpv_select;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setView(rootView)
                .setPositiveButton(selectedButtonStringRes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onColorSelected(color);
                    }
                });

        int dialogTitleStringRes = getArguments().getInt(ARG_DIALOG_TITLE);
        if (dialogTitleStringRes != 0) {
            builder.setTitle(dialogTitleStringRes);
        }

        presetsButtonStringRes = getArguments().getInt(ARG_PRESETS_BUTTON_TEXT);
        customButtonStringRes = getArguments().getInt(ARG_CUSTOM_BUTTON_TEXT);

        int neutralButtonStringRes;
        if (dialogType == TYPE_CUSTOM && getArguments().getBoolean(ARG_ALLOW_PRESETS)) {
            neutralButtonStringRes = (presetsButtonStringRes != 0 ? presetsButtonStringRes : R.string.cpv_presets);
        } else if (dialogType == TYPE_PRESETS && getArguments().getBoolean(ARG_ALLOW_CUSTOM)) {
            neutralButtonStringRes = (customButtonStringRes != 0 ? customButtonStringRes : R.string.cpv_custom);
        } else {
            neutralButtonStringRes = 0;
        }

        if (neutralButtonStringRes != 0) {
            builder.setNeutralButton(neutralButtonStringRes, null);
        }

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();

        // http://stackoverflow.com/a/16972670/1048340
        //noinspection ConstantConditions
        dialog.getWindow()
                .clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        // Do not dismiss the dialog when clicking the neutral button.
        Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        if (neutralButton != null) {
            neutralButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rootView.removeAllViews();
                    switch (dialogType) {
                        case TYPE_CUSTOM:
                            dialogType = TYPE_PRESETS;
                            ((Button) v).setText(customButtonStringRes != 0 ? customButtonStringRes : R.string.cpv_custom);
                            rootView.addView(createPresetsView());
                            break;
                        case TYPE_PRESETS:
                            dialogType = TYPE_CUSTOM;
                            ((Button) v).setText(presetsButtonStringRes != 0 ? presetsButtonStringRes : R.string.cpv_presets);
                            rootView.addView(createPickerView());
                    }
                }
            });
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        onDialogDismissed();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(ARG_COLOR, color);
        outState.putInt(ARG_TYPE, dialogType);
        super.onSaveInstanceState(outState);
    }

    /**
     * Set the callback.
     * <p/>
     * Note: The preferred way to handle the callback is to have the calling Activity implement
     * {@link ColorPickerDialogListener} as this will not survive an orientation change.
     *
     * @param colorPickerDialogListener The callback invoked when a color is selected or the dialog is dismissed.
     */
    public void setColorPickerDialogListener(ColorPickerDialogListener colorPickerDialogListener) {
        this.colorPickerDialogListener = colorPickerDialogListener;
    }

    // region Custom Picker

    View createPickerView() {
        View contentView = View.inflate(getActivity(), R.layout.cpv_dialog_color_picker, null);
        colorPicker = (ColorPickerView) contentView.findViewById(R.id.cpv_color_picker_view);
        ColorPanelView oldColorPanel = (ColorPanelView) contentView.findViewById(R.id.cpv_color_panel_old);
        newColorPanel = (ColorPanelView) contentView.findViewById(R.id.cpv_color_panel_new);
        ImageView arrowRight = (ImageView) contentView.findViewById(R.id.cpv_arrow_right);
        hexEditText = (EditText) contentView.findViewById(R.id.cpv_hex);

        try {
            final TypedValue value = new TypedValue();
            TypedArray typedArray =
                    getActivity().obtainStyledAttributes(value.data, new int[]{android.R.attr.textColorPrimary});
            int arrowColor = typedArray.getColor(0, Color.BLACK);
            typedArray.recycle();
            arrowRight.setColorFilter(arrowColor);
        } catch (Exception ignored) {
        }

        colorPicker.setAlphaSliderVisible(showAlphaSlider);
        oldColorPanel.setColor(getArguments().getInt(ARG_COLOR));
        colorPicker.setColor(color, true);
        newColorPanel.setColor(color);
        setHex(color);

        if (!showAlphaSlider) {
            hexEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        }

        newColorPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newColorPanel.getColor() == color) {
                    onColorSelected(color);
                    dismiss();
                }
            }
        });

        contentView.setOnTouchListener(onPickerTouchListener);
        colorPicker.setOnColorChangedListener(this);
        hexEditText.addTextChangedListener(this);

        hexEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(hexEditText, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });

        return contentView;
    }

    @Override
    public void onColorChanged(int newColor) {
        color = newColor;
        if (newColorPanel != null) {
            newColorPanel.setColor(newColor);
        }
        if (!fromEditText && hexEditText != null) {
            setHex(newColor);
            if (hexEditText.hasFocus()) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(hexEditText.getWindowToken(), 0);
                hexEditText.clearFocus();
            }
        }
        fromEditText = false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (hexEditText.isFocused()) {
            int color = parseColorString(s.toString());
            if (color != colorPicker.getColor()) {
                fromEditText = true;
                colorPicker.setColor(color, true);
            }
        }
    }

    private void setHex(int color) {
        if (showAlphaSlider) {
            hexEditText.setText(String.format("%08X", (color)));
        } else {
            hexEditText.setText(String.format("%06X", (0xFFFFFF & color)));
        }
    }

    private int parseColorString(String colorString) throws NumberFormatException {
        int a, r, g, b = 0;
        if (colorString.startsWith("#")) {
            colorString = colorString.substring(1);
        }
        if (colorString.length() == 0) {
            r = 0;
            a = 255;
            g = 0;
        } else if (colorString.length() <= 2) {
            a = 255;
            r = 0;
            b = Integer.parseInt(colorString, 16);
            g = 0;
        } else if (colorString.length() == 3) {
            a = 255;
            r = Integer.parseInt(colorString.substring(0, 1), 16);
            g = Integer.parseInt(colorString.substring(1, 2), 16);
            b = Integer.parseInt(colorString.substring(2, 3), 16);
        } else if (colorString.length() == 4) {
            a = 255;
            r = Integer.parseInt(colorString.substring(0, 2), 16);
            g = r;
            r = 0;
            b = Integer.parseInt(colorString.substring(2, 4), 16);
        } else if (colorString.length() == 5) {
            a = 255;
            r = Integer.parseInt(colorString.substring(0, 1), 16);
            g = Integer.parseInt(colorString.substring(1, 3), 16);
            b = Integer.parseInt(colorString.substring(3, 5), 16);
        } else if (colorString.length() == 6) {
            a = 255;
            r = Integer.parseInt(colorString.substring(0, 2), 16);
            g = Integer.parseInt(colorString.substring(2, 4), 16);
            b = Integer.parseInt(colorString.substring(4, 6), 16);
        } else if (colorString.length() == 7) {
            a = Integer.parseInt(colorString.substring(0, 1), 16);
            r = Integer.parseInt(colorString.substring(1, 3), 16);
            g = Integer.parseInt(colorString.substring(3, 5), 16);
            b = Integer.parseInt(colorString.substring(5, 7), 16);
        } else if (colorString.length() == 8) {
            a = Integer.parseInt(colorString.substring(0, 2), 16);
            r = Integer.parseInt(colorString.substring(2, 4), 16);
            g = Integer.parseInt(colorString.substring(4, 6), 16);
            b = Integer.parseInt(colorString.substring(6, 8), 16);
        } else {
            b = -1;
            g = -1;
            r = -1;
            a = -1;
        }
        return Color.argb(a, r, g, b);
    }

    // -- endregion --

    // region Presets Picker

    View createPresetsView() {
        View contentView = View.inflate(getActivity(), R.layout.cpv_dialog_presets, null);
        shadesLayout = (LinearLayout) contentView.findViewById(R.id.shades_layout);
        transparencySeekBar = (SeekBar) contentView.findViewById(R.id.transparency_seekbar);
        transparencyPercText = (TextView) contentView.findViewById(R.id.transparency_text);
        GridView gridView = (GridView) contentView.findViewById(R.id.gridView);

        loadPresets();

        if (showColorShades) {
            createColorShades(color);
        } else {
            shadesLayout.setVisibility(View.GONE);
            contentView.findViewById(R.id.shades_divider).setVisibility(View.GONE);
        }

        adapter = new ColorPaletteAdapter(new ColorPaletteAdapter.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int newColor) {
                if (color == newColor) {
                    // Double tab selects the color
                    ColorPickerDialog.this.onColorSelected(color);
                    dismiss();
                    return;
                }
                color = newColor;
                if (showColorShades) {
                    createColorShades(color);
                }
            }
        }, presets, getSelectedItemPosition(), colorShape);

        gridView.setAdapter(adapter);

        if (showAlphaSlider) {
            setupTransparency();
        } else {
            contentView.findViewById(R.id.transparency_layout).setVisibility(View.GONE);
            contentView.findViewById(R.id.transparency_title).setVisibility(View.GONE);
        }

        return contentView;
    }

    private void loadPresets() {
        int alpha = Color.alpha(color);
        presets = getArguments().getIntArray(ARG_PRESETS);
        if (presets == null) presets = MATERIAL_COLORS;
        boolean isMaterialColors = presets == MATERIAL_COLORS;
        presets = Arrays.copyOf(presets, presets.length); // don't update the original array when modifying alpha
        if (alpha != 255) {
            // add alpha to the presets
            for (int i = 0; i < presets.length; i++) {
                int color = presets[i];
                int red = Color.red(color);
                int green = Color.green(color);
                int blue = Color.blue(color);
                presets[i] = Color.argb(alpha, red, green, blue);
            }
        }
        presets = unshiftIfNotExists(presets, color);
        int initialColor = getArguments().getInt(ARG_COLOR);
        if (initialColor != color) {
            // The user clicked a color and a configuration change occurred. Make sure the initial color is in the presets
            presets = unshiftIfNotExists(presets, initialColor);
        }
        if (isMaterialColors && presets.length == 19) {
            // Add black to have a total of 20 colors if the current color is in the material color palette
            presets = pushIfNotExists(presets, Color.argb(alpha, 0, 0, 0));
        }
    }

    void createColorShades(@ColorInt final int color) {
        final int[] colorShades = getColorShades(color);

        if (shadesLayout.getChildCount() != 0) {
            for (int i = 0; i < shadesLayout.getChildCount(); i++) {
                FrameLayout layout = (FrameLayout) shadesLayout.getChildAt(i);
                final ColorPanelView cpv = (ColorPanelView) layout.findViewById(R.id.cpv_color_panel_view);
                ImageView iv = (ImageView) layout.findViewById(R.id.cpv_color_image_view);
                cpv.setColor(colorShades[i]);
                cpv.setTag(false);
                iv.setImageDrawable(null);
            }
            return;
        }

        final int horizontalPadding = getResources().getDimensionPixelSize(R.dimen.cpv_item_horizontal_padding);

        for (final int colorShade : colorShades) {
            int layoutResId;
            if (colorShape == ColorShape.SQUARE) {
                layoutResId = R.layout.cpv_color_item_square;
            } else {
                layoutResId = R.layout.cpv_color_item_circle;
            }

            final View view = View.inflate(getActivity(), layoutResId, null);
            final ColorPanelView colorPanelView = (ColorPanelView) view.findViewById(R.id.cpv_color_panel_view);

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) colorPanelView.getLayoutParams();
            params.leftMargin = params.rightMargin = horizontalPadding;
            colorPanelView.setLayoutParams(params);
            colorPanelView.setColor(colorShade);
            shadesLayout.addView(view);

            colorPanelView.post(new Runnable() {
                @Override
                public void run() {
                    // The color is black when rotating the dialog. This is a dirty fix. WTF!?
                    colorPanelView.setColor(colorShade);
                }
            });

            colorPanelView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.getTag() instanceof Boolean && (Boolean) v.getTag()) {
                        onColorSelected(ColorPickerDialog.this.color);
                        dismiss();
                        return; // already selected
                    }
                    ColorPickerDialog.this.color = colorPanelView.getColor();
                    adapter.selectNone();
                    for (int i = 0; i < shadesLayout.getChildCount(); i++) {
                        FrameLayout layout = (FrameLayout) shadesLayout.getChildAt(i);
                        ColorPanelView cpv = (ColorPanelView) layout.findViewById(R.id.cpv_color_panel_view);
                        ImageView iv = (ImageView) layout.findViewById(R.id.cpv_color_image_view);
                        iv.setImageResource(cpv == v ? R.drawable.cpv_preset_checked : 0);
                        if (cpv == v && ColorUtils.calculateLuminance(cpv.getColor()) >= 0.65
                                || Color.alpha(cpv.getColor()) <= ALPHA_THRESHOLD) {
                            iv.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
                        } else {
                            iv.setColorFilter(null);
                        }
                        cpv.setTag(cpv == v);
                    }
                }
            });
            colorPanelView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    colorPanelView.showHint();
                    return true;
                }
            });
        }
    }

    private void onColorSelected(int color) {
        if (colorPickerDialogListener != null) {
            Log.w(TAG, "Using deprecated listener which may be remove in future releases");
            colorPickerDialogListener.onColorSelected(dialogId, color);
            return;
        }
        Activity activity = getActivity();
        if (activity instanceof ColorPickerDialogListener) {
            ((ColorPickerDialogListener) activity).onColorSelected(dialogId, color);
        } else {
            throw new IllegalStateException("The activity must implement ColorPickerDialogListener");
        }
    }

    private void onDialogDismissed() {
        if (colorPickerDialogListener != null) {
            Log.w(TAG, "Using deprecated listener which may be remove in future releases");
            colorPickerDialogListener.onDialogDismissed(dialogId);
            return;
        }
        Activity activity = getActivity();
        if (activity instanceof ColorPickerDialogListener) {
            ((ColorPickerDialogListener) activity).onDialogDismissed(dialogId);
        }
    }

    private int shadeColor(@ColorInt int color, double percent) {
        String hex = String.format("#%06X", (0xFFFFFF & color));
        long f = Long.parseLong(hex.substring(1), 16);
        double t = percent < 0 ? 0 : 255;
        double p = percent < 0 ? percent * -1 : percent;
        long R = f >> 16;
        long G = f >> 8 & 0x00FF;
        long B = f & 0x0000FF;
        int alpha = Color.alpha(color);
        int red = (int) (Math.round((t - R) * p) + R);
        int green = (int) (Math.round((t - G) * p) + G);
        int blue = (int) (Math.round((t - B) * p) + B);
        return Color.argb(alpha, red, green, blue);
    }

    private int[] getColorShades(@ColorInt int color) {
        return new int[]{
                shadeColor(color, 0.9), shadeColor(color, 0.7), shadeColor(color, 0.5), shadeColor(color, 0.333),
                shadeColor(color, 0.166), shadeColor(color, -0.125), shadeColor(color, -0.25), shadeColor(color, -0.375),
                shadeColor(color, -0.5), shadeColor(color, -0.675), shadeColor(color, -0.7), shadeColor(color, -0.775),
        };
    }

    private void setupTransparency() {
        int progress = 255 - Color.alpha(color);
        transparencySeekBar.setMax(255);
        transparencySeekBar.setProgress(progress);
        int percentage = (int) ((double) progress * 100 / 255);
        transparencyPercText.setText(String.format(Locale.ENGLISH, "%d%%", percentage));
        transparencySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int percentage = (int) ((double) progress * 100 / 255);
                transparencyPercText.setText(String.format(Locale.ENGLISH, "%d%%", percentage));
                int alpha = 255 - progress;
                // update items in GridView:
                for (int i = 0; i < adapter.colors.length; i++) {
                    int color = adapter.colors[i];
                    int red = Color.red(color);
                    int green = Color.green(color);
                    int blue = Color.blue(color);
                    adapter.colors[i] = Color.argb(alpha, red, green, blue);
                }
                adapter.notifyDataSetChanged();
                // update shades:
                for (int i = 0; i < shadesLayout.getChildCount(); i++) {
                    FrameLayout layout = (FrameLayout) shadesLayout.getChildAt(i);
                    ColorPanelView cpv = (ColorPanelView) layout.findViewById(R.id.cpv_color_panel_view);
                    ImageView iv = (ImageView) layout.findViewById(R.id.cpv_color_image_view);
                    if (layout.getTag() == null) {
                        // save the original border color
                        layout.setTag(cpv.getBorderColor());
                    }
                    int color = cpv.getColor();
                    color = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
                    if (alpha <= ALPHA_THRESHOLD) {
                        cpv.setBorderColor(color | 0xFF000000);
                    } else {
                        cpv.setBorderColor((int) layout.getTag());
                    }
                    if (cpv.getTag() != null && (Boolean) cpv.getTag()) {
                        // The alpha changed on the selected shaded color. Update the checkmark color filter.
                        if (alpha <= ALPHA_THRESHOLD) {
                            iv.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
                        } else {
                            if (ColorUtils.calculateLuminance(color) >= 0.65) {
                                iv.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
                            } else {
                                iv.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                            }
                        }
                    }
                    cpv.setColor(color);
                }
                // update color:
                int red = Color.red(color);
                int green = Color.green(color);
                int blue = Color.blue(color);
                color = Color.argb(alpha, red, green, blue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private int[] unshiftIfNotExists(int[] array, int value) {
        boolean present = false;
        for (int i : array) {
            if (i == value) {
                present = true;
                break;
            }
        }
        if (!present) {
            int[] newArray = new int[array.length + 1];
            newArray[0] = value;
            System.arraycopy(array, 0, newArray, 1, newArray.length - 1);
            return newArray;
        }
        return array;
    }

    private int[] pushIfNotExists(int[] array, int value) {
        boolean present = false;
        for (int i : array) {
            if (i == value) {
                present = true;
                break;
            }
        }
        if (!present) {
            int[] newArray = new int[array.length + 1];
            newArray[newArray.length - 1] = value;
            System.arraycopy(array, 0, newArray, 0, newArray.length - 1);
            return newArray;
        }
        return array;
    }

    private int getSelectedItemPosition() {
        for (int i = 0; i < presets.length; i++) {
            if (presets[i] == color) {
                return i;
            }
        }
        return -1;
    }

    // endregion

    // region Builder

    @IntDef({TYPE_CUSTOM, TYPE_PRESETS})
    public @interface DialogType {

    }

    public static final class Builder {

        ColorPickerDialogListener colorPickerDialogListener;
        @StringRes
        int dialogTitle = R.string.cpv_default_title;
        @StringRes
        int presetsButtonText = R.string.cpv_presets;
        @StringRes
        int customButtonText = R.string.cpv_custom;
        @StringRes
        int selectedButtonText = R.string.cpv_select;
        @ColorPickerDialog.DialogType
        int dialogType = TYPE_PRESETS;
        int[] presets = MATERIAL_COLORS;
        @ColorInt
        int color = Color.BLACK;
        int dialogId = 0;
        boolean showAlphaSlider = false;
        boolean allowPresets = true;
        boolean allowCustom = true;
        boolean showColorShades = true;
        @ColorShape
        int colorShape = ColorShape.CIRCLE;

        /*package*/ Builder() {

        }

        /**
         * Set the dialog title string resource id
         *
         * @param dialogTitle The string resource used for the dialog title
         * @return This builder object for chaining method calls
         */
        public ColorPickerDialog.Builder setDialogTitle(@StringRes int dialogTitle) {
            this.dialogTitle = dialogTitle;
            return this;
        }

        /**
         * Set the selected button text string resource id
         *
         * @param selectedButtonText The string resource used for the selected button text
         * @return This builder object for chaining method calls
         */
        public ColorPickerDialog.Builder setSelectedButtonText(@StringRes int selectedButtonText) {
            this.selectedButtonText = selectedButtonText;
            return this;
        }

        /**
         * Set the presets button text string resource id
         *
         * @param presetsButtonText The string resource used for the presets button text
         * @return This builder object for chaining method calls
         */
        public ColorPickerDialog.Builder setPresetsButtonText(@StringRes int presetsButtonText) {
            this.presetsButtonText = presetsButtonText;
            return this;
        }

        /**
         * Set the custom button text string resource id
         *
         * @param customButtonText The string resource used for the custom button text
         * @return This builder object for chaining method calls
         */
        public ColorPickerDialog.Builder setCustomButtonText(@StringRes int customButtonText) {
            this.customButtonText = customButtonText;
            return this;
        }

        /**
         * Set which dialog view to show.
         *
         * @param dialogType Either {@link com.jaredrummler.android.colorpicker.ColorPickerDialog#TYPE_CUSTOM} or {@link com.jaredrummler.android.colorpicker.ColorPickerDialog#TYPE_PRESETS}.
         * @return This builder object for chaining method calls
         */
        public ColorPickerDialog.Builder setDialogType(@ColorPickerDialog.DialogType int dialogType) {
            this.dialogType = dialogType;
            return this;
        }

        /**
         * Set the colors used for the presets
         *
         * @param presets An array of color ints.
         * @return This builder object for chaining method calls
         */
        public ColorPickerDialog.Builder setPresets(@NonNull int[] presets) {
            this.presets = presets;
            return this;
        }

        /**
         * Set the original color
         *
         * @param color The default color for the color picker
         * @return This builder object for chaining method calls
         */
        public ColorPickerDialog.Builder setColor(int color) {
            this.color = color;
            return this;
        }

        /**
         * Set the dialog id used for callbacks
         *
         * @param dialogId The id that is sent back to the {@link ColorPickerDialogListener}.
         * @return This builder object for chaining method calls
         */
        public ColorPickerDialog.Builder setDialogId(int dialogId) {
            this.dialogId = dialogId;
            return this;
        }

        /**
         * Show the alpha slider
         *
         * @param showAlphaSlider {@code true} to show the alpha slider. Currently only supported with the {@link
         *                        ColorPickerView}.
         * @return This builder object for chaining method calls
         */
        public ColorPickerDialog.Builder setShowAlphaSlider(boolean showAlphaSlider) {
            this.showAlphaSlider = showAlphaSlider;
            return this;
        }

        /**
         * Show/Hide a neutral button to select preset colors.
         *
         * @param allowPresets {@code false} to disable showing the presets button.
         * @return This builder object for chaining method calls
         */
        public ColorPickerDialog.Builder setAllowPresets(boolean allowPresets) {
            this.allowPresets = allowPresets;
            return this;
        }

        /**
         * Show/Hide the neutral button to select a custom color.
         *
         * @param allowCustom {@code false} to disable showing the custom button.
         * @return This builder object for chaining method calls
         */
        public ColorPickerDialog.Builder setAllowCustom(boolean allowCustom) {
            this.allowCustom = allowCustom;
            return this;
        }

        /**
         * Show/Hide the color shades in the presets picker
         *
         * @param showColorShades {@code false} to hide the color shades.
         * @return This builder object for chaining method calls
         */
        public ColorPickerDialog.Builder setShowColorShades(boolean showColorShades) {
            this.showColorShades = showColorShades;
            return this;
        }

        /**
         * Set the shape of the color panel view.
         *
         * @param colorShape Either {@link ColorShape#CIRCLE} or {@link ColorShape#SQUARE}.
         * @return This builder object for chaining method calls
         */
        public ColorPickerDialog.Builder setColorShape(int colorShape) {
            this.colorShape = colorShape;
            return this;
        }

        /**
         * Create the {@link ColorPickerDialog} instance.
         *
         * @return A new {@link ColorPickerDialog}.
         * @see
         */
        public ColorPickerDialog create() {
            ColorPickerDialog dialog = new ColorPickerDialog();
            Bundle args = new Bundle();
            args.putInt(ARG_ID, dialogId);
            args.putInt(ARG_TYPE, dialogType);
            args.putInt(ARG_COLOR, color);
            args.putIntArray(ARG_PRESETS, presets);
            args.putBoolean(ARG_ALPHA, showAlphaSlider);
            args.putBoolean(ARG_ALLOW_CUSTOM, allowCustom);
            args.putBoolean(ARG_ALLOW_PRESETS, allowPresets);
            args.putInt(ARG_DIALOG_TITLE, dialogTitle);
            args.putBoolean(ARG_SHOW_COLOR_SHADES, showColorShades);
            args.putInt(ARG_COLOR_SHAPE, colorShape);
            args.putInt(ARG_PRESETS_BUTTON_TEXT, presetsButtonText);
            args.putInt(ARG_CUSTOM_BUTTON_TEXT, customButtonText);
            args.putInt(ARG_SELECTED_BUTTON_TEXT, selectedButtonText);
            dialog.setArguments(args);
            return dialog;
        }

        /**
         * Create and show the {@link ColorPickerDialog} created with this builder.
         *
         * @param activity The current activity.
         */
        public void show(FragmentManager activity) {
            create().show(activity, "color-picker-dialog");
        }
    }

    // endregion
}