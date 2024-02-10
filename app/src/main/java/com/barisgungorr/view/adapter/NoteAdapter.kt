package com.barisgungorr.view.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.barisgungorr.data.NoteModel
import com.barisgungorr.notesapp.R
import com.barisgungorr.notesapp.databinding.NoteLayoutBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import org.commonmark.node.SoftLineBreak
import java.text.SimpleDateFormat

class NoteAdapter(options: FirestoreRecyclerOptions<NoteModel>, context: Context) :
    FirestoreRecyclerAdapter<NoteModel, NoteAdapter.NoteViewHolder>(options) {

    private val mContext: Context = context

    class NoteViewHolder(val binding: NoteLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val markWon = Markwon.builder(binding.root.context)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TaskListPlugin.create(binding.root.context))
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureVisitor(builder: MarkwonVisitor.Builder) {
                    super.configureVisitor(builder)
                    builder.on(
                        SoftLineBreak::class.java
                    ) { visitor, _ -> visitor.forceNewLine() }
                }
            }).build()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = NoteLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int, model: NoteModel) {
        holder.binding.apply {
            holder.markWon.setMarkdown(noteContent, model.content)
            noteTitle.text = model.title
            titleCard.setCardBackgroundColor(model.color)
            contentCard.setCardBackgroundColor(model.color)
            noteDate.text = SimpleDateFormat.getInstance().format(model.date)

            if (model.content == "" || noteContent.text.isEmpty()) {
                contentCard.visibility = View.GONE
            }
            setOnClickListenerForNavigation(titleCard, position)
            setOnClickListenerForNavigation(contentCard, position)
            setOnClickListenerForNavigation(noteTitle, position)
            setOnClickListenerForNavigation(noteContent, position)
        }
    }

    private fun setOnClickListenerForNavigation(view: View, position: Int) {
        view.setOnClickListener {
            val bundle = Bundle().apply {
                putString("noteId", snapshots.getSnapshot(position).id)
            }
            Navigation.findNavController(it)
                .navigate(R.id.action_noteFragment_to_saveFragment, bundle)
        }
    }
}
