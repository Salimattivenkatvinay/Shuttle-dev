package com.simplecity.amp_library.ui.activities;

import com.simplecity.amp_library.R;
import com.simplecity.amp_library.ui.widgets.WidgetProviderExtraLarge;

public class WidgetConfigureExtraLarge extends BaseWidgetConfigure {

    private static final String TAG = "WidgetConfigureExtraLar";

    @Override
    int[] getWidgetLayouts() {
        return new int[]{R.layout.widget_layout_extra_large};
    }

    @Override
    String getLayoutIdString() {
        return WidgetProviderExtraLarge.ARG_EXTRA_LARGE_LAYOUT_ID;
    }

    @Override
    String getUpdateCommandString() {
        return WidgetProviderExtraLarge.CMDAPPWIDGETUPDATE;
    }

    @Override
    int getRootViewId() {
        return R.id.widget_layout_extra_large;
    }

    @Override
    protected String screenName() {
        return TAG;
    }
}
