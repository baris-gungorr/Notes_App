package com.barisgungorr.view.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.barisgungorr.data.NoteModel
import com.barisgungorr.notesapp.R
import com.barisgungorr.notesapp.databinding.FragmentSaveBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.thebluealliance.spectrum.SpectrumPalette
import java.text.SimpleDateFormat
import java.util.Date


class SaveFragment : Fragment() {

    private lateinit var binding: FragmentSaveBinding
    private var data = NoteModel()
    private lateinit var navController: NavController
    private var color: Int = -1315861
    private lateinit var colorPickerDialog: BottomSheetDialog
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSaveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        navController = Navigation.findNavController(view)

        val colorPickerView = layoutInflater.inflate(R.layout.bottom_sheet_layout, null)
        val colorPicker = colorPickerView.findViewById<SpectrumPalette>(R.id.colorPicker)
        colorPicker.setSelectedColor(color)
        colorPicker.setOnColorSelectedListener { value ->
            color = value
            updateColors(color)
        }
        colorPickerDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        colorPickerDialog.setContentView(colorPickerView)

        binding.fabColorPick.setOnClickListener { colorPickerDialog.show() }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (shouldNavigateBack()) {
                        saveNote()
                    } else {
                        navController.navigate(R.id.action_saveFragment_to_noteFragment)
                    }
                }
            })

        // Bu fonksiyonu ekledim
        setUpNote()

        binding.etNoteContent.setStylesBar(binding.styleBar)

        binding.saveNote.setOnClickListener { saveNote() }
    }

    private fun shouldNavigateBack(): Boolean {
        return (binding.etNoteContent.text?.isEmpty() ?:(
                arguments?.getString("noteId") ?: ""
                )) == ""
    }

    private fun saveNote() {
        if (binding.etNoteContent.text?.isEmpty() == true) {
            Toast.makeText(activity, R.string.save_fragment_write_something, Toast.LENGTH_SHORT).show()
        } else {
            // Bu değişkeni tanımladım
            val noteId = arguments?.getString("noteId") ?: FirebaseFirestore.getInstance().collection("notes")
            .document(FirebaseAuth.getInstance().uid.toString())
                .collection("myNotes").document().id
            data = data.copy(
                // Bu satırı değiştirdim
                id = noteId,
            title = binding.etTitle.text.toString(),
            content = binding.etNoteContent.getMD(),
            date = Date().time,
            color = color
            )

            if (arguments?.getString("noteId") == null) {
                FirebaseFirestore.getInstance().collection("notes")
                    .document(FirebaseAuth.getInstance().uid.toString())
                    .collection("myNotes").document(noteId).set(data.toHashMap())
                .addOnSuccessListener {
                    handler.postDelayed({
                        navController.navigate(R.id.action_saveFragment_to_noteFragment)
                    }, 1000)
                }
                    .addOnFailureListener { e ->
                        Toast.makeText(activity, "Kayıt sırasında bir hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                updateNote()
            }
        }
    }

    private fun updateNote() {
        data = data.copy(id = arguments?.getString("noteId")!!)
        FirebaseFirestore.getInstance().collection("notes")
            .document(FirebaseAuth.getInstance().uid.toString())
            .collection("myNotes").document(arguments?.getString("noteId")!!)
        .set(data)
            .addOnSuccessListener {
                navController.navigate(R.id.action_saveFragment_to_noteFragment)
            }
    }

    private fun setUpNote() {
        val userId = FirebaseAuth.getInstance().uid
        if (userId == null) {
            Toast.makeText(activity, "Kullanıcı kimliği doğrulanmadı", Toast.LENGTH_SHORT).show()
            return
        }

        val noteId = arguments?.getString("noteId") ?: FirebaseFirestore.getInstance().collection("notes")
            .document(userId)
            .collection("myNotes").document().id
        if (arguments?.getString("noteId") == null) {
            binding.lastEdited.text = getString(
                R.string.save_fragment_edited_on,
                SimpleDateFormat.getInstance().format(Date())
            )
        } else {
            FirebaseFirestore.getInstance().collection("notes")
                .document(userId)
                .collection("myNotes").document(noteId)
                .addSnapshotListener { value, _ ->
                    val note = value?.toObject(NoteModel::class.java)

                    if (note != null) {
                        data = note
                        binding.etTitle.setText(note.title)
                        binding.etNoteContent.renderMD(note.content)
                        binding.lastEdited.text = getString(
                            R.string.save_fragment_edited_on,
                            SimpleDateFormat.getInstance().format(Date(note.date))
                        )
                        updateColors(note.color)
                    }
                }
        }
    }

    private fun updateColors(color: Int) {
        binding.noteContentFragmentParent.setBackgroundColor(color)
        binding.styleBar.setBackgroundColor(color)
        activity?.window?.statusBarColor = color
        activity?.window?.navigationBarColor = color
        (colorPickerDialog.findViewById<CardView>(R.id.bottomSheetParent))?.setCardBackgroundColor(color)
    }
}
