package com.mikepenz.materialdrawer.model;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;
import com.mikepenz.materialdrawer.R;
import com.mikepenz.materialdrawer.model.interfaces.Badgeable;
import com.mikepenz.materialdrawer.model.interfaces.Checkable;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Iconable;
import com.mikepenz.materialdrawer.model.interfaces.Identifyable;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;
import com.mikepenz.materialdrawer.model.interfaces.Tagable;
import com.mikepenz.materialdrawer.util.PressedEffectStateListDrawable;
import com.mikepenz.materialdrawer.util.UIUtils;

/**
 * Created by mikepenz on 03.02.15.
 */
public class SecondaryDrawerItem implements IDrawerItem, Nameable<SecondaryDrawerItem>, Iconable<SecondaryDrawerItem>, Badgeable<SecondaryDrawerItem>, Checkable<SecondaryDrawerItem>, Tagable<SecondaryDrawerItem>, Identifyable<SecondaryDrawerItem> {

    private int identifier = -1;
    private Drawable icon;
    private int iconRes = -1;
    private IIcon iicon;
    private Drawable selectedIcon;
    private int selectedIconRes = -1;
    private String name;
    private int nameRes = -1;
    private String badge;
    private boolean enabled = true;
    private boolean checkable = true;
    private Object tag;

    private int selectedColor = 0;
    private int selectedColorRes = -1;
    private boolean selectedIconTinted = false;

    private int textColor = 0;
    private int textColorRes = -1;

    private int selectedTextColor = 0;
    private int selectedTextColorRes = -1;

    private int disabledColor = 0;
    private int disabledColorRes = -1;

    public SecondaryDrawerItem withIdentifier(int identifier) {
        this.identifier = identifier;
        return this;
    }

    public SecondaryDrawerItem withIcon(Drawable icon) {
        this.icon = icon;
        return this;
    }

    public SecondaryDrawerItem withIcon(int iconRes) {
        this.iconRes = iconRes;
        return this;
    }

    public SecondaryDrawerItem withIcon(IIcon iicon) {
        this.iicon = iicon;
        return this;
    }

    public SecondaryDrawerItem withSelectedIcon(Drawable selectedIcon) {
        this.selectedIcon = selectedIcon;
        return this;
    }

    public SecondaryDrawerItem withName(String name) {
        this.name = name;
        return this;
    }

    public SecondaryDrawerItem withName(int nameRes) {
        this.nameRes = nameRes;
        return this;
    }

    public SecondaryDrawerItem withBadge(String badge) {
        this.badge = badge;
        return this;
    }

    public SecondaryDrawerItem withCheckable(boolean checkable) {
        this.checkable = checkable;
        return this;
    }

    public SecondaryDrawerItem withTag(Object object) {
        this.tag = object;
        return this;
    }

    public SecondaryDrawerItem setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public SecondaryDrawerItem withSelectedColor(int selectedColor) {
        this.selectedColor = selectedColor;
        return this;
    }

    public SecondaryDrawerItem withSelectedColorRes(int selectedColorRes) {
        this.selectedColorRes = selectedColorRes;
        return this;
    }

    public SecondaryDrawerItem withTextColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    public SecondaryDrawerItem withTextColorRes(int textColorRes) {
        this.textColorRes = textColorRes;
        return this;
    }

    public SecondaryDrawerItem withSelectedTextColor(int selectedTextColor) {
        this.selectedTextColor = selectedTextColor;
        return this;
    }

    public SecondaryDrawerItem withSelectedTextColorRes(int selectedColorRes) {
        this.selectedTextColorRes = selectedColorRes;
        return this;
    }

    public SecondaryDrawerItem withDisabledColor(int disabledColor) {
        this.disabledColor = disabledColor;
        return this;
    }

    public SecondaryDrawerItem withDisabledColorRes(int disabledColorRes) {
        this.disabledColorRes = disabledColorRes;
        return this;
    }

    public SecondaryDrawerItem withTintSelectedIcon(boolean tintSelectedIcon) {
        this.selectedIconTinted = tintSelectedIcon;
        return this;
    }

    public int getSelectedColor() {
        return selectedColor;
    }

    public void setSelectedColor(int selectedColor) {
        this.selectedColor = selectedColor;
    }

    public int getSelectedColorRes() {
        return selectedColorRes;
    }

    public void setSelectedColorRes(int selectedColorRes) {
        this.selectedColorRes = selectedColorRes;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getTextColorRes() {
        return textColorRes;
    }

    public void setTextColorRes(int textColorRes) {
        this.textColorRes = textColorRes;
    }

    public int getSelectedTextColor() {
        return selectedTextColor;
    }

    public void setSelectedTextColor(int selectedTextColor) {
        this.selectedTextColor = selectedTextColor;
    }

    public int getSelectedTextColorRes() {
        return selectedTextColorRes;
    }

    public void setSelectedTextColorRes(int selectedTextColorRes) {
        this.selectedTextColorRes = selectedTextColorRes;
    }

    public int getDisabledColor() {
        return disabledColor;
    }

    public void setDisabledColor(int disabledColor) {
        this.disabledColor = disabledColor;
    }

    public int getDisabledColorRes() {
        return disabledColorRes;
    }

    public void setDisabledColorRes(int disabledColorRes) {
        this.disabledColorRes = disabledColorRes;
    }

    public boolean isSelectedIconTinted() {
        return selectedIconTinted;
    }

    public void setSelectedIconTinted(boolean selectedIconTinted) {
        this.selectedIconTinted = selectedIconTinted;
    }

    @Override
    public Object getTag() {
        return tag;
    }

    @Override
    public void setTag(Object tag) {
        this.tag = tag;
    }

    public Drawable getIcon() {
        return icon;
    }

    @Override
    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public IIcon getIIcon() {
        return iicon;
    }

    @Override
    public void setIIcon(IIcon iicon) {
        this.iicon = iicon;
    }

    public int getIconRes() {
        return iconRes;
    }

    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
    }

    public int getSelectedIconRes() {
        return selectedIconRes;
    }

    public void setSelectedIconRes(int selectedIconRes) {
        this.selectedIconRes = selectedIconRes;
    }

    public Drawable getSelectedIcon() {
        return selectedIcon;
    }

    public void setSelectedIcon(Drawable selectedIcon) {
        this.selectedIcon = selectedIcon;
    }

    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public int getNameRes() {
        return nameRes;
    }

    @Override
    public void setNameRes(int nameRes) {
        this.nameRes = nameRes;
    }

    public String getBadge() {
        return badge;
    }

    @Override
    public void setBadge(String badge) {
        this.badge = badge;
    }

    @Override
    public int getIdentifier() {
        return identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isCheckable() {
        return checkable;
    }

    @Override
    public void setCheckable(boolean checkable) {
        this.checkable = checkable;
    }

    @Override
    public String getType() {
        return "SECONDARY_ITEM";
    }

    @Override
    public int getLayoutRes() {
        return R.layout.material_drawer_item_secondary;
    }


    @Override
    public View convertView(LayoutInflater inflater, View convertView, ViewGroup parent) {
        Context ctx = parent.getContext();

        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(getLayoutRes(), parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        int selected_color = selectedColor;
        if (selected_color == 0 && selectedColorRes != -1) {
            selected_color = ctx.getResources().getColor(selectedColorRes);
        } else if (selected_color == 0) {
            selected_color = UIUtils.getThemeColorFromAttrOrRes(ctx, R.attr.material_drawer_selected, R.color.material_drawer_selected);
        }
        UIUtils.setBackground(viewHolder.view, UIUtils.getDrawerItemBackground(selected_color));

        if (this.getNameRes() != -1) {
            viewHolder.name.setText(this.getNameRes());
        } else {
            viewHolder.name.setText(this.getName());
        }

        if (badge != null) {
            viewHolder.badge.setText(badge);
            viewHolder.badge.setVisibility(View.VISIBLE);
        } else {
            viewHolder.badge.setVisibility(View.GONE);
        }

        int selected_text = selectedTextColor;
        if (selected_text == 0 && selectedTextColorRes != -1) {
            selected_text = ctx.getResources().getColor(selectedTextColorRes);
        } else if (selected_text == 0) {
            selected_text = UIUtils.getThemeColorFromAttrOrRes(ctx, R.attr.material_drawer_selected_text, R.color.material_drawer_selected_text);
        }

        int color;

        if (this.isEnabled()) {
            color = textColor;
            if (color == 0 && textColorRes != -1) {
                color = ctx.getResources().getColor(textColorRes);
            } else if (color == 0) {
                color = UIUtils.getThemeColorFromAttrOrRes(ctx, R.attr.material_drawer_secondary_text, R.color.material_drawer_secondary_text);
            }
            viewHolder.name.setTextColor(UIUtils.getTextColor(color, selected_text));
            viewHolder.badge.setTextColor(UIUtils.getTextColor(color, selected_text));
        } else {
            color = disabledColor;
            if (color == 0 && disabledColorRes != -1) {
                color = ctx.getResources().getColor(disabledColorRes);
            } else if (color == 0) {
                color = UIUtils.getThemeColorFromAttrOrRes(ctx, R.attr.material_drawer_hint_text, R.color.material_drawer_hint_text);
            }
            viewHolder.name.setTextColor(color);
            viewHolder.badge.setTextColor(color);
        }

        Drawable icon = null;
        Drawable selectedIcon = null;
        if (this.getIcon() != null) {
            icon = this.getIcon();

            if (this.getSelectedIcon() != null) {
                selectedIcon = this.getSelectedIcon();
            } else if (this.isSelectedIconTinted()) {
                icon = new PressedEffectStateListDrawable(icon, selected_text);
            }
        } else if (this.getIIcon() != null) {
            icon = new IconicsDrawable(ctx, this.getIIcon()).color(color).actionBarSize().paddingDp(1);
            selectedIcon = new IconicsDrawable(ctx, this.getIIcon()).color(selected_text).actionBarSize().paddingDp(1);
        } else if (this.getIconRes() > -1) {
            icon = ctx.getResources().getDrawable(iconRes);

            if (this.getSelectedIconRes() > -1) {
                selectedIcon = ctx.getResources().getDrawable(selectedIconRes);
            } else if (this.isSelectedIconTinted()) {
                icon = new PressedEffectStateListDrawable(icon, selected_text);
            }
        }

        if (icon != null) {
            if (selectedIcon != null) {
                viewHolder.icon.setImageDrawable(UIUtils.getIconColor(icon, selectedIcon));
            } else {
                viewHolder.icon.setImageDrawable(icon);
            }

            viewHolder.icon.setVisibility(View.VISIBLE);
        } else {
            viewHolder.icon.setVisibility(View.GONE);
        }

        return convertView;
    }

    private static class ViewHolder {
        private View view;
        private ImageView icon;
        private TextView name;
        private TextView badge;

        private ViewHolder(View view) {
            this.view = view;
            this.icon = (ImageView) view.findViewById(R.id.icon);
            this.name = (TextView) view.findViewById(R.id.name);
            this.badge = (TextView) view.findViewById(R.id.badge);
        }
    }
}
