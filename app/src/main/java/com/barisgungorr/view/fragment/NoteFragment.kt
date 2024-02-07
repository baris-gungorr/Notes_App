package com.barisgungorr.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.barisgungorr.data.GridModel
import com.barisgungorr.data.InformationModel
import com.barisgungorr.data.NoteModel
import com.barisgungorr.notesapp.R
import com.barisgungorr.notesapp.databinding.FragmentNoteBinding
import com.barisgungorr.view.activity.MainActivity
import com.barisgungorr.view.adapter.NoteAdapter
import com.barisgungorr.view.utils.SwipeGesture
import com.barisgungorr.view.utils.hideKeyboard
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class NoteFragment : Fragment() {
    private lateinit var binding: FragmentNoteBinding
    private lateinit var addNoteFab: LinearLayout
    private lateinit var noteUser: ImageView
    lateinit var search: EditText
    private lateinit var chatFabText: TextView
    private var backPressedTime: Long = 0
    private var backToast: Toast? = null
    private lateinit var rvNote: RecyclerView
    lateinit var noData: ImageView
    lateinit var options: FirestoreRecyclerOptions<NoteModel>
    lateinit var firebaseAdapter: NoteAdapter
    private lateinit var noteGrid: ImageView
    private var isGrid = false


    private var layoutManager =
        StaggeredGridLayoutManager(
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

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    backToast?.cancel()
                    activity?.moveTaskToBack(true)
                    activity?.finish()
                    return
                } else {
                    val backToast = Toast.makeText(
                        context,
                        "Çıkış yapmak için iki kez tıklayın",
                        Toast.LENGTH_LONG
                    )
                    backToast.show()
                }
                backPressedTime = System.currentTimeMillis()

            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        val view: View = inflater.inflate(R.layout.fragment_note, container, false)

        rvNote = view.findViewById(R.id.rv_note)

        setUpFirebaseAdapter()

        binding.root
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

        addNoteFab = view.findViewById(R.id.add_note_fab)
        rvNote = view.findViewById(R.id.rv_note)
        noData = view.findViewById(R.id.no_data)
        chatFabText = view.findViewById(R.id.chatFabText)
        noteUser = view.findViewById(R.id.note_user)
        search = view.findViewById(R.id.search)
        noteGrid = view.findViewById(R.id.note_grid)




        FirebaseFirestore.getInstance().collection("notes")
            .document(FirebaseAuth.getInstance().uid.toString()).addSnapshotListener { value, _ ->

                val data = value?.toObject(GridModel::class.java)

                if (data?.isGrid != null) {

                    isGrid = data.isGrid
                }

                if (isGrid) {

                    layoutManager =
                        StaggeredGridLayoutManager(
                            2, StaggeredGridLayoutManager.VERTICAL
                        )

                    rvNote.layoutManager = layoutManager

                } else {

                    layoutManager =
                        StaggeredGridLayoutManager(
                            1, StaggeredGridLayoutManager.VERTICAL
                        )

                    rvNote.layoutManager = layoutManager

                }

                noteGrid.setOnClickListener {

                    if (!isGrid) {

                        layoutManager =
                            StaggeredGridLayoutManager(
                                2, StaggeredGridLayoutManager.VERTICAL
                            )

                        rvNote.layoutManager = layoutManager

                        val gridModel = GridModel()

                        isGrid = true



                        FirebaseFirestore.getInstance().collection("notes")
                            .document(FirebaseAuth.getInstance().uid.toString()).set(gridModel)

                    } else {

                        layoutManager =
                            StaggeredGridLayoutManager(
                                1, StaggeredGridLayoutManager.VERTICAL
                            )

                        rvNote.layoutManager = layoutManager

                        val gridModel = GridModel()

                        isGrid = false



                        FirebaseFirestore.getInstance().collection("notes")
                            .document(FirebaseAuth.getInstance().uid.toString()).set(gridModel)

                    }
                }
            }

        noteUser.setOnClickListener {

            val dialog = Dialog(requireContext(), R.style.BottomSheetDialogTheme)

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

            dialog.setContentView(R.layout.accont_dialog)

            dialog.show()

            val userProfile: ImageView? = dialog.findViewById(R.id.user_profile)
            val userName: TextView? = dialog.findViewById(R.id.user_name)
            val userMail: TextView? = dialog.findViewById(R.id.user_mail)
            val userLogout: Button? = dialog.findViewById(R.id.user_logout)
            // val deleteAccount: Button? = dialog.findViewById(R.id.delete_account)


            FirebaseDatabase.getInstance().reference.child("Users")
                .child(FirebaseAuth.getInstance().uid.toString())
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        val data = snapshot.getValue(InformationModel::class.java)

                        if (data?.userProfilePhoto != null) {
                            Picasso.get().load(data.userProfilePhoto)
                                .placeholder(R.drawable.dp_holder) // Yedek resim drawable dosyası
                                .error(R.drawable.dp_holder) // Hata durumunda yine yedek resim
                                .into(userProfile!!)
                        } else {
                            userProfile?.setImageResource(R.drawable.dp_holder)
                        }


                        Picasso.get().load(data?.userProfilePhoto).placeholder(R.drawable.dp_holder)
                            .error(
                                R.drawable.dp_holder
                            ).into(userProfile!!)

                        userName?.text = data?.username

                        userMail?.text = data?.userEmail

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })

            userLogout?.setOnClickListener {

                FirebaseAuth.getInstance().signOut()

                try {
                    val googleSignInOptions =
                        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken("166513008091-8r3be9dd0dvbpr5hkoaa5d0afmtm82fs.apps.googleusercontent.com")
                            .requestEmail()
                            .build()

                    val googleSignInClient: GoogleSignInClient =
                        GoogleSignIn.getClient(requireActivity(), googleSignInOptions)

                    Auth.GoogleSignInApi.signOut(googleSignInClient.asGoogleApiClient())

                } catch (_: Exception) {

                }

                startActivity(Intent(requireActivity(), MainActivity::class.java))

                requireActivity().finish()

            }

                deleteAccount?.setOnClickListener {
                            val openUrlIntent =
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://forms.gle/46DcNhLTbKsvEaWB8"))
                            startActivity(openUrlIntent)

                        }



        }


        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                noData.visibility = View.GONE

                setUpFirebaseAdapter()

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (s?.isEmpty()!!) {

                    setUpFirebaseAdapter()

                }

                val query =
                    FirebaseFirestore.getInstance().collection("notes")
                        .document(FirebaseAuth.getInstance().uid.toString())
                        .collection("myNotes").orderBy("title").startAt(s.toString())
                        .endAt(s.toString() + "\uF8FF")

                options = FirestoreRecyclerOptions.Builder<NoteModel>()
                    .setQuery(query, NoteModel::class.java).setLifecycleOwner(viewLifecycleOwner)
                    .build()

                firebaseAdapter = NoteAdapter(options, requireActivity())

                recyclerViewSetUp()

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

            return@setOnEditorActionListener true

        }

        rvNote.setOnScrollChangeListener { _, scrollX, scrollY, _, oldScrollY ->
            when {
                scrollX > oldScrollY -> {
                    chatFabText.visibility = View.GONE
                }

                scrollX == scrollY -> {
                    chatFabText.visibility = View.VISIBLE
                }

                else -> {
                    chatFabText.visibility = View.VISIBLE
                }
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
                    if (value.isEmpty) {

                        noData.visibility = View.VISIBLE

                    } else {

                        noData.visibility = View.GONE

                    }
                }
            }
    }

    private fun swipeToGesture(rvNote: RecyclerView?) {

        val swipeGesture = object : SwipeGesture(requireContext()) {

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val position = viewHolder.absoluteAdapterPosition

                firebaseAdapter.snapshots.getSnapshot(position).id

                val note = firebaseAdapter.getItem(position)

                try {

                    when (direction) {

                        ItemTouchHelper.LEFT -> {

                            FirebaseFirestore.getInstance().collection("notes")
                                .document(FirebaseAuth.getInstance().uid.toString())
                                .collection("myNotes")
                                .document(firebaseAdapter.snapshots.getSnapshot(position).id)
                                .delete()

                            search.apply {
                                hideKeyboard()
                                clearFocus()
                            }

                            val snackBar = Snackbar.make(
                                requireView(), "Not silindi", Snackbar.LENGTH_LONG
                            ).addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                override fun onDismissed(
                                    transientBottomBar: Snackbar?,
                                    event: Int
                                ) {
                                    super.onDismissed(transientBottomBar, event)
                                }

                                override fun onShown(transientBottomBar: Snackbar?) {

                                    transientBottomBar?.setAction("Geri al") {


                                        FirebaseFirestore.getInstance().collection("notes")
                                            .document(FirebaseAuth.getInstance().uid.toString())
                                            .collection("myNotes").document().set(note)

                                    }
                                    super.onShown(transientBottomBar)

                                }
                            }).apply {
                                animationMode = Snackbar.ANIMATION_MODE_FADE
                                setAnchorView(R.id.add_note_fab)
                            }
                            snackBar.setActionTextColor(
                                ContextCompat.getColor
                                    (requireContext(), R.color.orangeRed)
                            )
                            snackBar.setActionTextColor(Color.BLUE)
                            snackBar.setBackgroundTint(Color.YELLOW)

                            snackBar.show()


                        }

                        ItemTouchHelper.RIGHT -> {

                            FirebaseFirestore.getInstance().collection("notes")
                                .document(FirebaseAuth.getInstance().uid.toString())
                                .collection("myNotes")
                                .document(firebaseAdapter.snapshots.getSnapshot(position).id)
                                .delete()

                            search.apply {
                                hideKeyboard()
                                clearFocus()
                            }

                            val snackBar = Snackbar.make(
                                requireView(), "Not silindi", Snackbar.LENGTH_LONG
                            ).addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                override fun onDismissed(
                                    transientBottomBar: Snackbar?,
                                    event: Int
                                ) {
                                    super.onDismissed(transientBottomBar, event)
                                }

                                override fun onShown(transientBottomBar: Snackbar?) {

                                    transientBottomBar?.setAction("GERİ AL") {

                                        FirebaseFirestore.getInstance().collection("notes")
                                            .document(FirebaseAuth.getInstance().uid.toString())
                                            .collection("myNotes").document().set(note)

                                    }

                                    super.onShown(transientBottomBar)
                                }
                            }).apply {
                                animationMode = Snackbar.ANIMATION_MODE_FADE
                                setAnchorView(R.id.add_note_fab)
                            }
                            snackBar.setActionTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.orangeRed
                                )
                            )
                            snackBar.show()
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

    private fun setUpFirebaseAdapter() {

        val query: Query = FirebaseFirestore.getInstance().collection("notes")
            .document(FirebaseAuth.getInstance().uid.toString())
            .collection("myNotes").orderBy("date", Query.Direction.DESCENDING)

        options =
            FirestoreRecyclerOptions.Builder<NoteModel>().setQuery(query, NoteModel::class.java)
                .setLifecycleOwner(viewLifecycleOwner).build()

        firebaseAdapter = NoteAdapter(options, requireActivity())

        recyclerViewSetUp()

    }

    private fun recyclerViewSetUp() {

        rvNote.setHasFixedSize(true)

        rvNote.adapter = firebaseAdapter

        rvNote.layoutManager = layoutManager

        rvNote.viewTreeObserver.addOnPreDrawListener {
            startPostponedEnterTransition()
            true
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onStart() {
        super.onStart()

        rvNote.recycledViewPool.clear()
        firebaseAdapter.notifyDataSetChanged()
        firebaseAdapter.startListening()

    }

    override fun onStop() {
        super.onStop()

        firebaseAdapter.stopListening()

    }

    private var mContext: Context? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        mContext = context
    }

    override fun onDetach() {
        super.onDetach()

        mContext = null
    }
}

