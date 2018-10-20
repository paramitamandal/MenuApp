package com.sip.menuapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class SimpleDividerItemDecoration extends RecyclerView.ItemDecoration {
    private Drawable mDivider;

    @SuppressLint("ResourceType")
    public SimpleDividerItemDecoration(Context context) {
        mDivider = context.getResources().getDrawable(R.drawable.recycler_horizontal_divider);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDivider.getIntrinsicHeight();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }


        // 1. Get the parent (RecyclerView) padding
//        int left = parent.getPaddingLeft();
//        int right = parent.getWidth() - parent.getPaddingRight();
//
//        // 2. Iterate items of the RecyclerView
//        for (int childIdx = 0; childIdx < parent.getChildCount(); childIdx++) {
//
//            // 3. Get the item
//            View item = parent.getChildAt(childIdx);
//
//            // 4. Determine the item's top and bottom with the divider
//            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) item.getLayoutParams();
//            int top = item.getBottom() + params.bottomMargin;
//            int bottom = top + mDivider.getIntrinsicHeight();
//
//            // 5. Set the divider's bounds
//            this.mDivider.setBounds(left, top, right, bottom);
//
//            // 6. Draw the divider
//            this.mDivider.draw(c);
//        }

    }

}
