package com.example.happyplaces.ui.add

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.location.LocationManagerCompat.isLocationEnabled
import com.example.happyplaces.R
import com.example.happyplaces.data.DatabaseHandler
import com.example.happyplaces.data.HappyPlaceModel
import com.example.happyplaces.databinding.ActivityAddHappyPlaceBinding
import com.example.happyplaces.ui.MainActivity
import com.example.happyplaces.utils.GetAddressFromLatLng
import com.example.happyplaces.utils.getParcelableExtraProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityAddHappyPlaceBinding

    private var cal = Calendar.getInstance()
    private var saveImageToInternalStorage: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0
    private lateinit var locationLauncher: ActivityResultLauncher<Intent>
    private lateinit var choosePhotoLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private lateinit var mHappyPlaceDetails: HappyPlaceModel
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)

        binding.apply {
            setContentView(root)
            setSupportActionBar(toolbarAddPlace)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            toolbarAddPlace.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            etDate.setOnClickListener(this@AddHappyPlaceActivity)
            tvAddImage.setOnClickListener(this@AddHappyPlaceActivity)
            btnSave.setOnClickListener(this@AddHappyPlaceActivity)
            etLocation.setOnClickListener(this@AddHappyPlaceActivity)
            tvSelectCurrentLocation.setOnClickListener(this@AddHappyPlaceActivity)
        }

        if (!Places.isInitialized()) {
            Places.initialize(
                this@AddHappyPlaceActivity,
                resources.getString(R.string.google_maps_api_key)
            )
        }

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            mHappyPlaceDetails =
                intent.getParcelableExtraProvider<HappyPlaceModel>(MainActivity.EXTRA_PLACE_DETAILS)!!
            if (mHappyPlaceDetails != null) {
                supportActionBar.apply {
                    title = "Edit Happy Place"

                }
                binding.apply {
                    etTitle.setText(mHappyPlaceDetails.title)
                    etDescription.setText(mHappyPlaceDetails.description)
                    etDate.setText(mHappyPlaceDetails.date)
                    etLocation.setText(mHappyPlaceDetails.location)
                    mLatitude = mHappyPlaceDetails.latitude
                    mLongitude = mHappyPlaceDetails.longitude
                    saveImageToInternalStorage = Uri.parse(mHappyPlaceDetails.image)
                    ivPlaceImage.setImageURI(saveImageToInternalStorage)
                    btnSave.text = "UPDATE"
                }
            }
        }

        dateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
            }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let { data ->
                    val place = Autocomplete.getPlaceFromIntent(data)
                    place.let { place ->
                        binding.etLocation.setText(place.address)
                        place.latLng?.let { location ->
                            mLatitude = location.latitude
                            mLongitude = location.longitude
                        }
                    }
                }
            }
        }
        choosePhotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val contentUri = result.data?.data
                try {
                    val selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                        contentResolver,
                        contentUri
                    )
                    saveImageToInternalStorage =
                        saveImageToInternalStorage(selectedImageBitmap)
                    Log.i("image", "Path: $saveImageToInternalStorage")
                    binding.ivPlaceImage.setImageBitmap(selectedImageBitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(
                        this@AddHappyPlaceActivity,
                        "Fail to Load the image from gallery",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let {
                    val thumbnail = result.data!!.extras?.get("data") as Bitmap
                    saveImageToInternalStorage =
                        saveImageToInternalStorage(thumbnail)
                    Log.i("image", "Path: $saveImageToInternalStorage")
                    binding.ivPlaceImage.setImageBitmap(thumbnail)
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.et_date -> {
                DatePickerDialog(
                    this@AddHappyPlaceActivity,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            R.id.tv_add_image -> {
                val pictureDialogItems =
                    arrayOf("Select photo from gallery", "Capture photo from camera")
                val pictureDialog = AlertDialog.Builder(this)
                    .setTitle("Select Action")
                    .setItems(pictureDialogItems) { _, which ->
                        when (which) {
                            0 -> choosePhotoFromGallery()

                            1 -> takePhotoFromCamera()
                        }
                    }
                pictureDialog.show()
            }

            R.id.btn_save -> {
                binding.apply {
                    when {
                        etTitle.text.isNullOrEmpty() -> {

                        }

                        etDescription.text.isNullOrEmpty() -> {

                        }

                        etLocation.text.isNullOrEmpty() -> {

                        }

                        saveImageToInternalStorage == null -> {

                        }

                        else -> {
                            val happyPlaceModel = HappyPlaceModel(
                                id = if (mHappyPlaceDetails == null) 0 else mHappyPlaceDetails.id,
                                title = etTitle.text.toString(),
                                image = saveImageToInternalStorage.toString(),
                                description = etDescription.text.toString(),
                                date = etDate.text.toString(),
                                location = etLocation.text.toString(),
                                latitude = mLatitude,
                                longitude = mLongitude
                            )
                            val dbHandler = DatabaseHandler(this@AddHappyPlaceActivity)

                            if (mHappyPlaceDetails == null) {
                                val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModel)

                                if (addHappyPlace > 0) {
                                    Toast.makeText(
                                        this@AddHappyPlaceActivity,
                                        "SUCCESS",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    setResult(Activity.RESULT_OK)
                                    finish()
                                }
                            } else {
                                val updateHappyPlace = dbHandler.updateHappyPlace(happyPlaceModel)

                                if (updateHappyPlace > 0) {
                                    Toast.makeText(
                                        this@AddHappyPlaceActivity,
                                        "SUCCESS",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    setResult(Activity.RESULT_OK)
                                    finish()
                                }
                            }

                        }
                    }
                }

            }

            R.id.et_location -> {
                try {
                    val fields = listOf(
                        Place.Field.ID,
                        Place.Field.NAME,
                        Place.Field.LAT_LNG,
                        Place.Field.ADDRESS
                    )
                    val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(this@AddHappyPlaceActivity)

                    locationLauncher.launch(intent)
                } catch (e : Exception) {
                    e.printStackTrace()
                }
            }

            R.id.tv_select_current_location -> {
                if (!isLocationEnabled()) {
                    Toast.makeText(this, "", Toast.LENGTH_SHORT).show()
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                } else {
                    Dexter.withActivity(this).withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ).withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            if (report!!.areAllPermissionsGranted()) {
                                requestNewLocationData()
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permissions: MutableList<PermissionRequest>?,
                            token: PermissionToken?
                        ) {
                            showRationalDialogForPermissions()
                        }

                    })
                }
            }
        }

    }

    private fun takePhotoFromCamera() {
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report?.areAllPermissionsGranted() == true) {
                        val galleryIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        cameraLauncher.launch(galleryIntent)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }
            })
            .onSameThread()
            .check()
    }

    private fun choosePhotoFromGallery() {
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report?.areAllPermissionsGranted() == true) {
                        val galleryIntent =
                            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        choosePhotoLauncher.launch(galleryIntent)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }
            })
            .onSameThread()
            .check()
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("Require")
            .setPositiveButton("SETTINGS") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("packages", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNeutralButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .show()

    }

    private fun updateDateInView() {
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())

        binding.apply {
            etDate.setText(sdf.format(cal.time).toString())
        }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")
        try {
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return Uri.parse(file.absolutePath)
    }

    private fun isLocationEnabled() : Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_LOW_POWER
        mLocationRequest.interval = 1000
        mLocationRequest.numUpdates = 1

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallBack, Looper.getMainLooper())

    }

    private val mLocationCallBack = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation = locationResult.lastLocation
            mLastLocation?.let {
                mLatitude = it.latitude
                mLongitude = it.longitude
            }

            val addressTask = GetAddressFromLatLng(this@AddHappyPlaceActivity, mLatitude, mLongitude)
            addressTask.setAddressListener(object : GetAddressFromLatLng.AddressListener {
                override fun onAddressFound(address: String?) {
                    binding.etLocation.setText(address)
                }

                override fun onError() {
                }
            })
            addressTask.getAddress()
        }
    }

    companion object {
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
    }
}