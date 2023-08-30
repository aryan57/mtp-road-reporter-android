package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.example.myapplication.ml.Model
import com.example.myapplication.utils.ApiRequestHandler
import com.example.myapplication.utils.Constants
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class MainActivity : AppCompatActivity() {

    private lateinit var logoutButton: Button
    private lateinit var fetchButton: Button
    private lateinit var apiRequestHandler: ApiRequestHandler

    private lateinit var imageView: ImageView
    private lateinit var selectImgBtn: Button
    private lateinit var doInferenceBtn: Button
    private lateinit var inferenceResultView: TextView
    private lateinit var bitmap: Bitmap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        apiRequestHandler = ApiRequestHandler(this)
        logoutButton = findViewById(R.id.logout)
        fetchButton = findViewById(R.id.fetch_profile)
        imageView = findViewById(R.id.imageView)
        selectImgBtn = findViewById(R.id.select_image)
        doInferenceBtn = findViewById(R.id.do_inference)
        inferenceResultView = findViewById(R.id.inference_result)

        logoutButton.setOnClickListener {
            apiRequestHandler.logoutUser()
        }

        fetchButton.setOnClickListener {
            fetchButton.isEnabled = false
            apiRequestHandler.makeApiRequest(Request.Method.GET,
                Constants.API_PATH_WHOAMI,
                null,
                { response ->
                    Toast.makeText(this, response.toString(), Toast.LENGTH_SHORT).show()
                },
                {},
                {
                    fetchButton.isEnabled = true
                })
        }

        val imageProcessor =
            ImageProcessor.Builder().add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
//            .add(NormalizeOp(0.0f,255.0f))
//            .add(TransformToGrayscaleOp())
                .build()

        val labels = application.assets.open("labels.txt").bufferedReader().readLines()

        selectImgBtn.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 100)

        }

        doInferenceBtn.setOnClickListener {


            var tensorImage = TensorImage(DataType.FLOAT32)
            tensorImage.load(bitmap)
            tensorImage = imageProcessor.process(tensorImage)

            val model = Model.newInstance(this)

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

            inferenceResultView.text = labels[maxIdx]

            // Releases model resources if no longer used.
            model.close()
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            val uri = data?.data
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            imageView.setImageBitmap(bitmap)
        }
    }
}