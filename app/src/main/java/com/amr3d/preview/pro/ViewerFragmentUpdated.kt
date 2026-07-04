package com.amr3d.preview.pro

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycleScope // 1. ده كان ناقص
import kotlinx.coroutines.* // 2. وده كمان
import android.provider.MediaStore

class STLViewerFragment : Fragment() {

    private lateinit var glViewerView: GLViewerView
    private lateinit var emptyStateText: TextView
    private lateinit var welcomeText: TextView
    private var currentModel: STLModel? = null
    private var pendingUri: Uri? = null

    private val openDocumentLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                requireContext().contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {}
            loadFile(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_viewer, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        glViewerView = view.findViewById(R.id.glViewerView)
        emptyStateText = view.findViewById(R.id.emptyStateText)
        welcomeText = view.findViewById(R.id.welcomeText)

        view.findViewById<Button>(R.id.btnOpenFile).setOnClickListener {
            openDocumentLauncher.launch(arrayOf("*/*"))
        }

        pendingUri?.let {
            pendingUri = null
            loadFile(it)
        }
    }

    fun loadFile(uri: Uri) {
        if (!isAdded || view == null) {
            pendingUri = uri
            return
        }

        val fileName = requireContext().contentResolver.query(
            uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null
        )?.use { cursor ->
            cursor.moveToFirst()
            cursor.getString(0)
        } ?: "ملف"

        val isSTL = fileName.endsWith(".stl", ignoreCase = true)
        val isDXF = fileName.endsWith(".dxf", ignoreCase = true)

        when {
            isSTL -> loadSTLFile(uri)
            isDXF -> loadDXFFile(uri)
            else -> Toast.makeText(context, "نوع ملف غير مدعوم", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSTLFile(uri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val model = withContext(Dispatchers.IO) {
                    STLParser.parse(requireContext(), uri)
                }
                setModelToViewer(model)
            } catch (e: Exception) {
                Toast.makeText(context, "خطأ STL: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadDXFFile(uri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val model = withContext(Dispatchers.IO) {
                    DXFParser.parse(requireContext(), uri) // بنستخدم parse
                }
                setModelToViewer(model)
                Toast.makeText(context, "✅  DXF: ${model.triangleCount} مثلث", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "خطأ DXF: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun setModelToViewer(model: STLModel) {
        currentModel = model
        glViewerView.queueEvent {
            glViewerView.stlRenderer.setModel(model)
        }
        emptyStateText.visibility = View.GONE
        welcomeText.visibility = View.GONE
    }
}
