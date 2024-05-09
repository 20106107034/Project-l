package org.mitmuzaffarpur.project_ll

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import org.mitmuzaffarpur.project_ll.ui.theme.ProjectllTheme
import java.io.ByteArrayInputStream

class RvActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
//            val bundle = intent.extras
//            if (bundle != null) {
//                val studentData = bundle.getSerializable("StudentData") as ImageResponse
//                // Now you can use studentData object
//            } else {
//                // Handle the case when bundle is null
//            }
            val bundle = intent.extras
            val receivedArrayList =
                bundle?.getSerializable("arrayList") as? ArrayList<Triple<String, String, String>>

            Column {

                Text(text = "CAM",
                    textAlign = TextAlign.Center,
                    modifier= Modifier
                        .padding(0.dp, 10.dp)
                        .fillMaxWidth(1f)
                        .heightIn(min = 40.dp, max = 60.dp),
                    style = MaterialTheme.typography.headlineMedium
                )

                if (receivedArrayList != null) {
                    LazyColumn(content = {
                        items(receivedArrayList) { item ->
                            ListOfStudent(name = item.first, csScore = item.second,encodedImage=item.third)
                        }
                    })
                }
                else{
                    Text(text = "Not found any known image")
                }
            }
        }
    }

    

    @Composable
    fun ListOfStudent(name: String, csScore: String,encodedImage:String) {

        var falg:Boolean=false


//        try {
            val byteArray = Base64.decode(encodedImage, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
//        }
//        catch (e:Exception){
//            Text(text = "You have a bad base64")
//        }


        Card(modifier = Modifier.padding(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxSize()
            ) {
//                image by bitmap
                Image(bitmap = bitmap.asImageBitmap(),
                    contentDescription =null
                    )
//                Image(painter = painterResource(id = R.drawable.ic_launcher_foreground),
//                    contentDescription = null)
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Confidence Score : $csScore",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(text = "Present",
                        style = MaterialTheme.typography.bodyMedium)
                }

            }

        }

    }


}



