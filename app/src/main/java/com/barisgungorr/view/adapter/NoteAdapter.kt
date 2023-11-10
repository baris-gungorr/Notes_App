package com.barisgungorr.view.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.barisgungorr.data.NoteModel
import com.barisgungorr.notesapp.R
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import org.commonmark.node.SoftLineBreak
import java.text.SimpleDateFormat

class NoteAdapter(private var options: FirestoreRecyclerOptions<NoteModel>, var context: Context) :
    FirestoreRecyclerAdapter<NoteModel, NoteAdapter.NoteViewHolder>(options) {


    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val contextCard: CardView = itemView.findViewById(R.id.content_card)
        val titleCard: CardView = itemView.findViewById(R.id.title_card)
        val noteContent: TextView = itemView.findViewById(R.id.note_content)
        val noteTitle: TextView = itemView.findViewById(R.id.note_title)
        val noteDate: TextView = itemView.findViewById(R.id.note_date)


        val markWon = Markwon.builder(itemView.context)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TaskListPlugin.create(itemView.context))
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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_layout, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int, model: NoteModel) {


        holder.apply {

            markWon.setMarkdown(noteContent, model.content)
            noteTitle.text = model.title
            titleCard.setCardBackgroundColor(model.color)
            contextCard.setCardBackgroundColor(model.color)
            noteDate.text = SimpleDateFormat.getInstance().format(model.date)


            if (model.content == "") {

                contextCard.visibility = View.GONE

            } else if (noteContent.text.isEmpty()) {

                contextCard.visibility = View.GONE

            }




            holder.titleCard.setOnClickListener {

                val bundle = Bundle()
                bundle.putString(
                    "noteId",
                    NoteAdapter(options, context).snapshots.getSnapshot(position).id
                )
                Navigation.findNavController(it)
                    .navigate(R.id.action_noteFragment_to_saveFragment, bundle)

            }

            holder.contextCard.setOnClickListener {

                val bundle = Bundle()
                bundle.putString(
                    "noteId",
                    NoteAdapter(options, context).snapshots.getSnapshot(position).id
                )
                Navigation.findNavController(it)
                    .navigate(R.id.action_noteFragment_to_saveFragment, bundle)

            }

            holder.noteTitle.setOnClickListener {

                val bundle = Bundle()
                bundle.putString(
                    "noteId",
                    NoteAdapter(options, context).snapshots.getSnapshot(position).id
                )
                Navigation.findNavController(it)
                    .navigate(R.id.action_noteFragment_to_saveFragment, bundle)

            }

            holder.noteContent.setOnClickListener {

                val bundle = Bundle()
                bundle.putString(
                    "noteId",
                    NoteAdapter(options, context).snapshots.getSnapshot(position).id
                )
                Navigation.findNavController(it)
                    .navigate(R.id.action_noteFragment_to_saveFragment, bundle)

            }

        }


    }

}

