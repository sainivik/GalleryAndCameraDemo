package com.sainivik.galleryandcamerademo

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.sainivik.galleryandcamerademo.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    var imgPath = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setListener()
    }

    private fun setListener() {
        binding.btnPhoto.setOnClickListener {
            if (checkAndRequestPermissions()) {
                selectImage()
            } else {
            }
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        val ExtstorePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val cameraPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )
        val listPermissionsNeeded =
            ArrayList<String>()
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (ExtstorePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded
                .add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(
                this, listPermissionsNeeded.toTypedArray(), 1234
            )
            return false
        }
        return true
    }

    /*dialog to give option to choose photo*/
    private fun selectImage() {
        val options =
            arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder =
            AlertDialog.Builder(this@MainActivity)
        builder.setTitle("Add Photo!")
        builder.setItems(options) { dialog, item ->
            if (options[item] == "Take Photo") {
                try {
                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    val photoFile: File = createImageFile()
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        val photoURI = FileProvider.getUriForFile(
                            this@MainActivity,
                            "$packageName.file_provider",
                            photoFile
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(takePictureIntent, 1)
                    }
                } catch (ex: Exception) {
                    // Error occurred while creating the File
                    Toast.makeText(this@MainActivity, ex.message, Toast.LENGTH_SHORT).show()
                }
            } else if (options[item] == "Choose from Gallery") {
                val intent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                startActivityForResult(intent, 2)
            } else if (options[item] == "Cancel") {
                dialog.dismiss()
            }
        }
        builder.show()
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val file_path = getExternalFilesDir(null)!!.absolutePath +
                "/TestImages"
        val dir = File(file_path)
        if (!dir.exists()) dir.mkdirs()
        val outFile: OutputStream? = null
        val file =
            File(dir, System.currentTimeMillis().toString() + ".jpg")
        imgPath = file.absolutePath
        return file
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                try {
                    val bitmap: Bitmap
                    val bitmapOptions = BitmapFactory.Options()
                    try {
                        bitmap = BitmapFactory.decodeFile(imgPath, bitmapOptions)
                        binding.ivSelectedImage.setImageBitmap(bitmap)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            } else if (requestCode == 2) {
                val selectedImage = data!!.data
                val filePath =
                    arrayOf(MediaStore.Images.Media.DATA)
                val c =
                    contentResolver.query(selectedImage!!, filePath, null, null, null)
                c!!.moveToFirst()
                val columnIndex = c.getColumnIndex(filePath[0])
                val picturePath = c.getString(columnIndex)
                c.close()
                val thumbnail = BitmapFactory.decodeFile(picturePath)
                Log.w("path of", picturePath + "")
                binding.ivSelectedImage.setImageBitmap(thumbnail)
            }
        }
    }
}