package com.barisgungorr.notesapp.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.barisgungorr.Models.NoteModel
import com.barisgungorr.notesapp.Adapters.NoteAdapter
import com.barisgungorr.notesapp.R
import com.google.android.material.transition.MaterialElevationScale


class NoteFragment : Fragment() {
    lateinit var addNoteFab: LinearLayout

    lateinit var noteUser: ImageView

    lateinit var search: EditText

    lateinit var chatFabText: TextView

    private var backPressedTime: Long = 0

    private var backToast: Toast? = null

    lateinit var rvNote: RecyclerView

    lateinit var noData: ImageView

    lateinit var options: FirestoreRecyclerOptions<NoteModel>

    lateinit var firebaseAdapter: NoteAdapter

    lateinit var noteGrid: ImageView

    private var isGrid=false



    var layoutManager =
        StaggeredGridLayoutManager(1
            , StaggeredGridLayoutManager.VERTICAL)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition = MaterialElevationScale(false).apply {
            duration = 350
        }
        enterTransition = MaterialElevationScale(true).apply {
            duration = 350
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_note, container, false)
    }

}