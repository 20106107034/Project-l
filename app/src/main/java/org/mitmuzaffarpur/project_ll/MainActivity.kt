package org.mitmuzaffarpur.project_ll

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import org.mitmuzaffarpur.project_ll.ui.theme.ProjectllTheme
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.TimeUnit


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
        }
        setContent {
            ProjectllTheme {
                // A surface container using the 'background' color from the theme
                Column {
                    Text(text = "Face Recognition App",
                        textAlign = TextAlign.Center,
                        modifier= Modifier
                            .padding(0.dp, 10.dp)
                            .fillMaxWidth(1f)
                            .heightIn(min = 40.dp, max = 60.dp),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    UI()
                }
            }
        }
    }
}




@Composable
fun UI() {
    val UploadBtn = createMutableState("Upload")
    val context = LocalContext.current
    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                imageUri = it
            } })



    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(color = Color.White)
            .fillMaxSize()
    ){

        if (imageUri != null) {
            val painter = rememberImagePainter(imageUri)
            Image(
                painter = painter,
                contentDescription = "Image from URI",
                modifier = Modifier
                    .size(300.dp) // Set the size as per your requirement
                    .padding(8.dp), // Add padding as needed
                contentScale = ContentScale.Fit
            )
        }
        CustomSpacer( height = 3.dp)
        Button(
            onClick = { galleryLauncher.launch("image/*") },
        ) {
            Text(text = "Select")
        }
        CustomSpacer( height = 3.dp)
        Button(onClick = {
            uploadImg(context,imageUri)
            UploadBtn.value="Uploading..."
        }) {
            Text(text =UploadBtn.value)
        }
        CustomSpacer( height = 3.dp)
    }

}



@Composable
fun createMutableState(initialValue: String): MutableState<String> {
    return remember { mutableStateOf(initialValue) }
}

@Composable
fun CustomSpacer(height: Dp = 0.dp) {
    Spacer(
        modifier = Modifier
            .fillMaxWidth(.5f)
            .height(height)
            .background(Color.Black)
    )
}

//fun selectImg(context: Context){
//    Toast.makeText(context,"Select_img clicked",Toast.LENGTH_SHORT).show()
//}


@OptIn(DelicateCoroutinesApi::class)
fun uploadImg(context: Context, imageUri: Uri?) {

    if (imageUri == null) {
        Log.d("image","NOt supported")
        Toast.makeText(context,"Image not selected",Toast.LENGTH_SHORT).show()
        restartApp(context)
        // when you directly click on upload then app restart 
    }
    else {

        imageUri?.let { uri ->
            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r", null)
            parcelFileDescriptor?.let { pfd ->
                val fileDescriptor = pfd.fileDescriptor
                val inputStream = FileInputStream(fileDescriptor)
                val file = File(context.cacheDir, "image.png") // Change the file path as needed
                inputStream.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("image", file.name, requestBody)

                val httpClient = OkHttpClient.Builder()
                httpClient.connectTimeout(50000, TimeUnit.MILLISECONDS)
                    .readTimeout(50000, TimeUnit.MILLISECONDS)

                val retrofit = Retrofit.Builder()
                    .baseUrl("http://192.168.201.26:5000/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build()
                    .create(UploadService::class.java)

                try {
                    GlobalScope.launch {
                        var st = ""
                        val response = retrofit.uploadImage(part)
                        for (i in response) {
                            st = st + i.name + " " + i.Confidence_Score + ", "
                        }
                        if (response.isNotEmpty()) {
                            Log.d("CONNECTED", "${st} this ")

                            val arrayList = ArrayList<Triple<String, String, String>>()
                            for (i in response) {
                                arrayList.add(
                                    Triple(
                                        i.name,
                                        i.Confidence_Score,
                                        i.Cropped_image_encoded
                                    )
                                )
                            }
                            // Put the ArrayList in the Intent using Bundle
                            val bundle = Bundle().apply {
                                putSerializable("arrayList", arrayList)
                            }
                            val intent = Intent(context, RvActivity::class.java)
                            intent.putExtras(bundle)
                            context.startActivity(intent)


                        } else {
                            Log.d("NoT Connected", "${st} this")

                        }

                    }
                } catch (e: Exception) {
                    Log.d("Error ", "Not connected")


                }
                parcelFileDescriptor.close()


            }
        }
    }
}

fun restartApp(context:Context) {
    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}










