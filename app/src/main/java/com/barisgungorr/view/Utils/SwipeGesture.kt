package com.barisgungorr.view.Utils

import android.content.Context
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.barisgungorr.notesapp.R
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import org.checkerframework.checker.units.qual.C

abstract class SwipeGesture(context: Context): ItemTouchHelper.SimpleCallback(
    0,
    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
) {
val deleteColor = ContextCompat.getColor(context, R.color.deleteColor)
    val deleIcon = R.drawable.ic_round_delete_24


    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        RecyclerViewSwipeDecorator.Builder(
            c,
            recyclerView,
            viewHolder,
            dX,
            dY,
            actionState,
            isCurrentlyActive
        )
            .addSwipeLeftBackgroundColor(deleteColor)
            .addSwipeLeftActionIcon(deleIcon)
            .addSwipeRightBackgroundColor(deleteColor)
            .addSwipeRightActionIcon(deleIcon)
            .create()
            .decorate()


        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }


}