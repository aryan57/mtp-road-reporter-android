package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.example.myapplication.ml.Model
import com.example.myapplication.utils.ApiRequestHandler
import com.example.myapplication.utils.Constants
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import org.json.JSONArray
import org.json.JSONObject
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


class HomeFragment : Fragment() {


    private lateinit var uploadButton: Button
    private lateinit var apiRequestHandler: ApiRequestHandler

    private lateinit var imageView: ImageView
    private lateinit var bitmap: Bitmap
    private lateinit var imageUri: Uri

    private lateinit var selectImgBtn: Button

    private lateinit var errorText: TextView
    private lateinit var description: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var dropdown: AutoCompleteTextView
    private lateinit var dropdownAdapter: ArrayAdapter<String>


    private lateinit var view: View
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        apiRequestHandler = ApiRequestHandler(view.context)

        uploadButton = view.findViewById(R.id.upload)
        imageView = view.findViewById(R.id.imageView)
        selectImgBtn = view.findViewById(R.id.select_image)
        errorText = view.findViewById(R.id.error)
        progressBar = view.findViewById(R.id.loading)
        description = view.findViewById(R.id.description)
        dropdown = view.findViewById(R.id.category_dropdown)
        dropdownAdapter = ArrayAdapter(
            view.context, android.R.layout.simple_dropdown_item_1line, getCategoriesName()
        )

        dropdown.setAdapter(dropdownAdapter)

        // Check for location permission and request if needed
        checkLocationPermission()


        uploadButton.setOnClickListener {
            upload()
        }

        selectImgBtn.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 100)

        }

        return view
    }

    private fun getCategoriesJSONFromAsset(): JSONArray {
        val str = view.context.assets.open("categories.json").bufferedReader().readText()
        return try {
            val x = JSONArray(str)
            x
        } catch (_: Exception) {
            JSONArray("[]")
        }

    }

    private fun getCategoriesName(): List<String> {
        val categories = mutableListOf<String>()
        val jsonArray = getCategoriesJSONFromAsset()
        for (i in 0 until jsonArray.length()) {
            categories.add(jsonArray.getJSONObject(i).getString("categoryName"))
        }
        return categories
    }

    private fun getCategoryIdFromName(categoryName: String): Int {
        val jsonArray = getCategoriesJSONFromAsset()
        for (i in 0 until jsonArray.length()) {
            if (jsonArray.getJSONObject(i).getString("categoryName") == categoryName) {
                return jsonArray.getJSONObject(i).getInt("id")
            }
        }
        return -1
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            imageUri = data?.data!!
            bitmap = MediaStore.Images.Media.getBitmap(view.context.contentResolver, imageUri)
            imageView.setImageURI(imageUri)
            doInferFunction()
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1
            )
        }
    }

    private fun getCurrentLocationAndMakeApiCall(jsonObjWithImgID: JSONObject) {
        if (ContextCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val x = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token
            )
            x.addOnSuccessListener { location ->
                if (location != null) {

                    jsonObjWithImgID.put("description", description.text)
                    jsonObjWithImgID.put(
                        "categoryId", getCategoryIdFromName(dropdown.text.toString())
                    )
                    jsonObjWithImgID.put(
                        "unixTime", System.currentTimeMillis() / 1000
                    ) // current unix time in seconds
                    jsonObjWithImgID.put("latitude", location.latitude)
                    jsonObjWithImgID.put("longitude", location.longitude)
                    // not putting imgID, as it is already there

                    // image upload done, now make another api call to create the post from imageID
                    apiRequestHandler.makeApiRequest(Request.Method.POST,
                        Constants.API_PATH_CREATE_POST,
                        jsonObjWithImgID,
                        {
                            Toast.makeText(
                                view.context, "Post created successfully", Toast.LENGTH_SHORT
                            ).show()
                        },
                        { errorMessage ->
                            errorText.text = errorMessage
                            errorText.visibility = View.VISIBLE
                        },
                        {
                            uploadButton.isEnabled = true
                            progressBar.visibility = View.GONE
                        })
                } else {
                    // Handle the case where location is null
                    errorText.text = "Cannot get location"
                    errorText.visibility = View.VISIBLE
                }
            }
            x.addOnFailureListener { exception ->
                // Handle the exception, e.g., no location available or permission denied
                errorText.text = exception.message
                errorText.visibility = View.VISIBLE
            }
        }
    }


    private fun upload() {

        val inputStream: InputStream?
        try {
            inputStream = view.context.contentResolver.openInputStream(imageUri)
        } catch (_: Exception) {
            Toast.makeText(
                view.context, "Select an image first", Toast.LENGTH_SHORT
            ).show()

            return
        }
        if (inputStream == null) {
            Toast.makeText(
                view.context, "inputStream is null", Toast.LENGTH_SHORT
            ).show()

            return
        }

        val filesDir = view.context.filesDir
        val file = File(filesDir, "image.png")

        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        inputStream.close()

        progressBar.visibility = View.VISIBLE
        errorText.visibility = View.GONE
        uploadButton.isEnabled = false

        apiRequestHandler.uploadImage(file, { imageUploadResponse ->
            Log.i(Constants.LOG_TAG_NAME, "Image upload successful. $imageUploadResponse")
            getCurrentLocationAndMakeApiCall(imageUploadResponse)

        }, { errorMessage ->
            Log.e(Constants.LOG_TAG_NAME, "Image upload NOT successful. $errorMessage")
            errorText.text = errorMessage
            errorText.visibility = View.VISIBLE
            uploadButton.isEnabled = true
            progressBar.visibility = View.GONE
        }, {

        })

    }


    private fun doInferFunction() {

        val imageProcessor =
            ImageProcessor.Builder().add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
//            .add(NormalizeOp(0.0f,255.0f))
//            .add(TransformToGrayscaleOp())
                .build()

        val labels = view.context.assets.open("labels.txt").bufferedReader().readLines()


        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        val model = Model.newInstance(view.context)

        // Creates inputs for reference.
        val inputFeature0 =
            TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(tensorImage.buffer)

        // Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray

        var maxIdx = 0
        outputFeature0.forEachIndexed { idx, fl ->
            if (outputFeature0[maxIdx] < fl) {
                maxIdx = idx
            }
        }

        val mlCategoryText = labels[maxIdx]

        val position = dropdownAdapter.getPosition(mlCategoryText)
        if (position != -1) {
            dropdown.setText(dropdownAdapter.getItem(position), false)
        } else {
            Toast.makeText(
                view.context,
                "$mlCategoryText does not match with any of the categories in the category dropdown",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Releases model resources if no longer used.
        model.close()
    }

}