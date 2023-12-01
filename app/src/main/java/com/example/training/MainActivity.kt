package com.example.training

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.training.ui.theme.TrainingTheme
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class Country(
    val name: Map<String, Any>,
    val flags: Map<String, Any>,
    val cca2: String
    // Add other properties as needed
)

interface CountryApi {
    @GET("all")
    fun getAllCountries(): Call<List<Country>>
}


object RetrofitClient {
    private const val BASE_URL = "https://restcountries.com/v3.1/"

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

fun fetchAllCountries(callback: (List<Country>?) -> Unit) {
    val countryApi = RetrofitClient.instance.create(CountryApi::class.java)
    val call = countryApi.getAllCountries()

    call.enqueue(object : Callback<List<Country>> {
        override fun onResponse(call: Call<List<Country>>, response: Response<List<Country>>) {
            if (response.isSuccessful) {
                val countries = response.body()
                callback(countries)
            } else {
                callback(null)
            }
        }

        override fun onFailure(call: Call<List<Country>>, t: Throwable) {
            println("Failed to fetch countries: ${t.message}")
            callback(null)
        }
    })
}

val interFamily = FontFamily(
    Font(R.font.inter, FontWeight.Normal),
    Font(R.font.inter_extrabold, FontWeight.ExtraBold)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrainingTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Main()
                }
            }
        }
    }
}



@Composable
fun Main() {
    val (currentScreen, setCurrentScreen) = remember { mutableStateOf("welcome") }
    Column {
        AnimatedVisibility(visible = (currentScreen == "welcome")) {
            WelcomeScreen(setCurrentScreen = setCurrentScreen)
        }
        AnimatedVisibility(visible = (currentScreen == "countries")) {
            CountryList()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryList() {
    var searchInput by remember {
        mutableStateOf("")
    }
    println("Started")
    var countriesMutable by remember { mutableStateOf<List<Country>>(emptyList()) }
    var countries by remember { mutableStateOf<List<Country>>(emptyList()) }

    // Fetch countries and update the list
    LaunchedEffect(true) {
        GlobalScope.launch(Dispatchers.Main) {
            fetchAllCountries { fetchedCountries ->
                if (fetchedCountries != null) {
                    println("loaded")
                    countriesMutable = fetchedCountries
                    countries = fetchedCountries
                } else {
                    println("error")
                    // Handle error
                    // You might want to show an error message here
                }
            }
        }
    }
    Box {
        Image(
            painter = painterResource(R.drawable.background2),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box {
                OutlinedTextField(
                    value = searchInput,
                    placeholder = {
                        Text(
                            text = "Search",
                            color = Color(0xFFFFFB73),
                            fontWeight = FontWeight.Normal,
                            fontSize = 24.sp
                        )
                    },
                    onValueChange = {
                        var temp = it.lowercase()
                        println(countries)
                        searchInput = it
                        countriesMutable = countries.filter { country ->  country.name["common"].toString().lowercase().contains(temp) || country.cca2.lowercase().contains(temp)}
                    },
                    textStyle = TextStyle(
                        color = Color(0xFFFFFB73),
                        fontWeight = FontWeight.Normal,
                        fontSize = 24.sp
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .padding(vertical = 48.dp)
                        .background(Color(0x1AFFFB73))
                        .border(
                            width = 2.dp, color = Color(0xFFFFFB73),
                            shape = MaterialTheme.shapes.medium.copy(
                                topStart = CornerSize(12.dp),
                                topEnd = CornerSize(12.dp),
                                bottomStart = CornerSize(12.dp),
                                bottomEnd = CornerSize(12.dp)
                            )
                        )
                        .fillMaxWidth(0.8f)

                )
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                items(countriesMutable) { country ->
                    AnimatedVisibility(visible = (country != null)) {
                        CountryItem(country = country)
                    }
                }
            }
        }
    }
}

@Composable
fun CountryItem(country: Country) {
    if(country.flags["png"] !== null) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(100.dp),
        ) {
            AsyncImage(
                model = country.flags["png"],
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(150.dp)
                    .clip(
                        shape = MaterialTheme.shapes.medium.copy(
                            topStart = CornerSize(12.dp),
                            topEnd = CornerSize(12.dp),
                            bottomStart = CornerSize(0.dp),
                            bottomEnd = CornerSize(0.dp)
                        )
                    )
                    .shadow(8.dp)
            )
            Box {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .background(
                            Color(0xff167044),
                            shape = MaterialTheme.shapes.medium.copy(
                                topStart = CornerSize(0.dp),
                                topEnd = CornerSize(0.dp),
                                bottomStart = CornerSize(12.dp),
                                bottomEnd = CornerSize(12.dp)
                            )
                        )
                        .padding(vertical = 10.dp)
                        .width(150.dp)
                        .height(25.dp)

                )
                Text(
                    text = "${country.cca2}",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .shadow(16.dp)
                        .background(
                            Color(0xff21BF73),
                            shape = MaterialTheme.shapes.medium.copy(
                                topStart = CornerSize(0.dp),
                                topEnd = CornerSize(0.dp),
                                bottomStart = CornerSize(12.dp),
                                bottomEnd = CornerSize(12.dp)
                            )
                        )
                        .padding(vertical = 10.dp)
                        .width(150.dp)
                        .height(25.dp)
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(modifier: Modifier = Modifier, setCurrentScreen: (String) -> Unit) {
    var loginScreen by remember {
        mutableStateOf(true)
    }
    Box {
        Image(
            painter = painterResource(R.drawable.background1),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.fillMaxSize()
        )
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Column(
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(color = Color(0xFFFFFB73))
                )
                Text(
                    text = "COUNTRIES\nEXPLORER",
                    fontSize = 40.sp,
                    lineHeight = 1.2.em,
                    textAlign = TextAlign.Center,
                    color = Color(0xFFFFFB73),
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = interFamily,
                    modifier = modifier
                        .background(Color(0xB31F1750))
                        .padding(vertical = 28.dp)
                        .fillMaxWidth(2f)
                )
                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(color = Color(0xFFFFFB73))
                )
            }
            Column(
                modifier = modifier
                    .padding(top = 100.dp)
                    .background(Color(0xB31F1750))
                    .padding(bottom = 30.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                var username by remember {
                    mutableStateOf("")
                }
                var password by remember {
                    mutableStateOf("")
                }
                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(color = Color(0xFFFFFB73))
                )
                OutlinedTextField(
                    value = username,
                    placeholder = {
                        Text(
                            text = "Username",
                            color = Color(0xFFFFFB73),
                            fontWeight = FontWeight.Normal,
                            fontSize = 24.sp
                        )
                    },
                    onValueChange = { username = it },
                    textStyle = TextStyle(
                        color = Color(0xFFFFFB73),
                        fontWeight = FontWeight.Normal,
                        fontSize = 24.sp
                    ),
                    singleLine = true,
                    modifier = modifier
                        .background(Color(0xB31F1750))
                        .padding(top = 30.dp)
                        .border(
                            width = 2.dp, color = Color(0xFFFFFB73),
                            shape = MaterialTheme.shapes.medium.copy(
                                topStart = CornerSize(12.dp),
                                topEnd = CornerSize(12.dp),
                                bottomStart = CornerSize(12.dp),
                                bottomEnd = CornerSize(12.dp)
                            )
                        )
                        .fillMaxWidth(0.8f)

                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = {
                        Text(
                            text = "Password",
                            color = Color(0xFFFFFB73),
                            fontWeight = FontWeight.Normal,
                            fontSize = 24.sp
                        )
                    },
                    textStyle = TextStyle(
                        color = Color(0xFFFFFB73),
                        fontWeight = FontWeight.Normal,
                        fontSize = 24.sp
                    ),
                    singleLine = true,
                    modifier = modifier
                        .padding(top = 15.dp)
                        .background(Color(0xB31F1750))
                        .border(
                            width = 2.dp, color = Color(0xFFFFFB73),
                            shape = MaterialTheme.shapes.medium.copy(
                                topStart = CornerSize(12.dp),
                                topEnd = CornerSize(12.dp),
                                bottomStart = CornerSize(12.dp),
                                bottomEnd = CornerSize(12.dp)
                            )
                        )
                        .fillMaxWidth(0.8f)

                )
                AnimatedVisibility(visible = !loginScreen) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = {
                            Text(
                                text = "Confirm Password",
                                color = Color(0xFFFFFB73),
                                fontWeight = FontWeight.Normal,
                                fontSize = 24.sp
                            )
                        },
                        textStyle = TextStyle(
                            color = Color(0xFFFFFB73),
                            fontWeight = FontWeight.Normal,
                            fontSize = 24.sp
                        ),
                        singleLine = true,
                        modifier = modifier
                            .padding(top = 15.dp)
                            .background(Color(0xB31F1750))
                            .border(
                                width = 2.dp, color = Color(0xFFFFFB73),
                                shape = MaterialTheme.shapes.medium.copy(
                                    topStart = CornerSize(12.dp),
                                    topEnd = CornerSize(12.dp),
                                    bottomStart = CornerSize(12.dp),
                                    bottomEnd = CornerSize(12.dp)
                                )
                            )
                            .fillMaxWidth(0.8f)
                    )
                }
                Button(
                    onClick = { setCurrentScreen("countries") },
                    modifier = modifier
                        .padding(top = 15.dp)
                        .fillMaxWidth(0.8f)
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFFB73)
                    ),
                    shape = MaterialTheme.shapes.medium.copy(
                        topStart = CornerSize(12.dp),
                        topEnd = CornerSize(12.dp),
                        bottomStart = CornerSize(12.dp),
                        bottomEnd = CornerSize(12.dp)
                    ),
                    content = {
                        Text(
                            text = if (loginScreen) "Log in"
                            else "Sign up",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1F1750),
                        )
                    }
                )
                Surface(
                    onClick = { loginScreen = !loginScreen },
                    color = Color.Transparent,
                    modifier = modifier
                        .padding(top = 20.dp)
                ) {
                    Text(
                        text = if (loginScreen) buildAnnotatedString {
                            append("No account yet? ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                                append("Sign up")
                            }
                        }
                        else buildAnnotatedString {
                            append("Have an account? ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                                append("Log in")
                            }
                        },
                        fontSize = 24.sp,
                        color = Color(0xFFFFFB73),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    TrainingTheme {
        Main()
    }
}