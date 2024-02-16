package com.barisgungorr.view.fragment

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.barisgungorr.data.NoteModel
import com.barisgungorr.notesapp.R
import com.barisgungorr.notesapp.databinding.FragmentNoteBinding
import com.barisgungorr.view.adapter.NoteAdapter
import com.barisgungorr.view.utils.SwipeGesture
import com.barisgungorr.view.utils.hideKeyboard
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class NoteFragment : Fragment() {
    private lateinit var binding: FragmentNoteBinding
    private lateinit var addNoteFab: LinearLayout
    private lateinit var noteUser: ImageView
    private lateinit var search: EditText
    private lateinit var chatFabText: TextView
    private lateinit var rvNote: RecyclerView
    private lateinit var noData: ImageView
    private lateinit var noteGrid: ImageView
    private var isGrid = false
    private var backPressedTime: Long = 0
    private var backToast: Toast? = null
    private lateinit var options: FirestoreRecyclerOptions<NoteModel>
    private lateinit var firebaseAdapter: NoteAdapter
    private val layoutManager = StaggeredGridLayoutManager(
        1, StaggeredGridLayoutManager.VERTICAL
    )

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
        binding = FragmentNoteBinding.inflate(inflater, container, false)
        val view = binding.root

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    backToast?.cancel()
                    activity?.moveTaskToBack(true)
                    activity?.finish()
                    return
                } else {
                    backToast = Toast.makeText(
                        context,
                        "Çıkış yapmak için iki kez tıklayın",
                        Toast.LENGTH_LONG
                    )
                    backToast?.show()
                }
                backPressedTime = System.currentTimeMillis()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        setUpFirebaseAdapter()

        return view
    }

    private fun setUpFirebaseAdapter() {
        rvNote = binding.rvNote
        val query: Query = FirebaseFirestore.getInstance().collection("notes")
            .document(FirebaseAuth.getInstance().uid.toString())
            .collection("myNotes").orderBy("date", Query.Direction.DESCENDING)

        options =
            FirestoreRecyclerOptions.Builder<NoteModel>().setQuery(query, NoteModel::class.java)
                .setLifecycleOwner(viewLifecycleOwner).build()

        firebaseAdapter = NoteAdapter(options, requireActivity())
        rvNote.setHasFixedSize(true)
        rvNote.adapter = firebaseAdapter
        rvNote.layoutManager = layoutManager
        rvNote.viewTreeObserver.addOnPreDrawListener {
            startPostponedEnterTransition()
            true
        }


    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = Navigation.findNavController(view)

        requireView().hideKeyboard()

        CoroutineScope(Dispatchers.Main).launch {
            delay(10)

            activity?.window?.statusBarColor =
                ContextCompat.getColor(requireContext(), R.color.black)

            activity?.window?.navigationBarColor =
                ContextCompat.getColor(requireContext(), android.R.color.transparent)

            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            activity?.window?.statusBarColor = Color.BLACK
        }

        addNoteFab = binding.addNoteFab
        rvNote = binding.rvNote
        noData = binding.noData
        chatFabText = binding.chatFabText
        noteUser = binding.noteUser
        search = binding.search
        noteGrid = binding.noteGrid

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                noData.visibility = View.GONE
                setUpFirebaseAdapter()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.isEmpty()!!) {
                    setUpFirebaseAdapter()
                }

                val query = FirebaseFirestore.getInstance().collection("notes")
                    .document(FirebaseAuth.getInstance().uid.toString())
                    .collection("myNotes").orderBy("title").startAt(s.toString())
                    .endAt(s.toString() + "\uF8FF")

                options = FirestoreRecyclerOptions.Builder<NoteModel>()
                    .setQuery(query, NoteModel::class.java).setLifecycleOwner(viewLifecycleOwner)
                    .build()

                firebaseAdapter.updateOptions(options)
            }

            override fun afterTextChanged(s: Editable?) {
                if (s?.isEmpty()!!) {
                    setUpFirebaseAdapter()
                }
            }
        })

        search.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                v.clearFocus()
                requireView().hideKeyboard()
            }
            true
        }

        rvNote.setOnScrollChangeListener { _, scrollX, scrollY, _, oldScrollY ->
            chatFabText.visibility = when {
                scrollX > oldScrollY -> View.GONE
                scrollX == scrollY -> View.VISIBLE
                else -> View.VISIBLE
            }
        }

        addNoteFab.setOnClickListener {
            navController.navigate(R.id.action_noteFragment_to_saveFragment)
        }

        swipeToGesture(rvNote)

        FirebaseFirestore.getInstance().collection("notes")
            .document(FirebaseAuth.getInstance().uid.toString())
            .collection("myNotes").addSnapshotListener { value, _ ->

                if (value != null) {
                    noData.visibility = if (value.isEmpty) View.VISIBLE else View.GONE
                }
            }
    }

    private fun swipeToGesture(rvNote: RecyclerView?) {

        val swipeGesture = object : SwipeGesture(requireContext()) {

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val position = viewHolder.absoluteAdapterPosition

                val note = firebaseAdapter.getItem(position)

                try {

                    when (direction) {

                        ItemTouchHelper.LEFT -> {

                            val deletedNote = note?.toHashMap()
                            deletedNote?.let {
                                FirebaseFirestore.getInstance().collection("notes")
                                    .document(FirebaseAuth.getInstance().uid.toString())
                                    .collection("myNotes")
                                    .document(firebaseAdapter.snapshots.getSnapshot(position).id)
                                    .delete()
                                    .addOnSuccessListener {
                                        val snackBar = Snackbar.make(
                                            requireView(), "Not silindi", Snackbar.LENGTH_LONG
                                        ).addCallback(object :
                                            BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                            override fun onShown(transientBottomBar: Snackbar?) {
                                                transientBottomBar?.setAction("Geri al") {
                                                    FirebaseFirestore.getInstance().collection("notes")
                                                        .document(FirebaseAuth.getInstance().uid.toString())
                                                        .collection("myNotes").add(deletedNote)
                                                }
                                                super.onShown(transientBottomBar)
                                            }
                                        }).apply {
                                            animationMode = Snackbar.ANIMATION_MODE_FADE
                                            setAnchorView(R.id.add_note_fab)
                                            setBackgroundTint(Color.YELLOW)
                                        }
                                        snackBar.setActionTextColor(Color.BLUE)
                                        snackBar.show()
                                    }
                            }
                        }

                        ItemTouchHelper.RIGHT -> {

                            val deletedNote = note?.toHashMap()
                            deletedNote?.let {
                                FirebaseFirestore.getInstance().collection("notes")
                                    .document(FirebaseAuth.getInstance().uid.toString())
                                    .collection("myNotes")
                                    .document(firebaseAdapter.snapshots.getSnapshot(position).id)
                                    .delete()
                                    .addOnSuccessListener {
                                        val snackBar = Snackbar.make(
                                            requireView(), "Not silindi", Snackbar.LENGTH_LONG
                                        ).addCallback(object :
                                            BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                            override fun onShown(transientBottomBar: Snackbar?) {
                                                transientBottomBar?.setAction("Geri al") {
                                                    FirebaseFirestore.getInstance().collection("notes")
                                                        .document(FirebaseAuth.getInstance().uid.toString())
                                                        .collection("myNotes").add(deletedNote)
                                                }
                                                super.onShown(transientBottomBar)
                                            }
                                        }).apply {
                                            animationMode = Snackbar.ANIMATION_MODE_FADE
                                            setAnchorView(R.id.add_note_fab)
                                            setBackgroundTint(Color.YELLOW)
                                        }
                                        snackBar.setActionTextColor(Color.BLUE)
                                        snackBar.show()
                                    }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        val touchHelper = ItemTouchHelper(swipeGesture)

        touchHelper.attachToRecyclerView(rvNote)
    }

    override fun onStart() {
        super.onStart()
        firebaseAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        firebaseAdapter.stopListening()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }
}
