package com.example.restapijsonapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)

interface ApiService {
    @GET("posts")
    suspend fun getPosts(): List<Post>
}

object RetrofitClient {
    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PostScreen()
            }
        }
    }
}

@Composable
fun PostScreen() {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun fetchPosts() {
        scope.launch {
            loading = true
            error = null
            try {
                posts = RetrofitClient.api.getPosts()
            } catch (e: Exception) {
                error = "Unable to fetch data. Check your internet connection."
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { fetchPosts() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("REST API - JSON Posts", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { fetchPosts() }, enabled = !loading) {
            Text("Refresh Data")
        }
        Spacer(modifier = Modifier.height(12.dp))

        when {
            loading -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Text(text = error!!, color = MaterialTheme.colorScheme.error)
                Button(onClick = { fetchPosts() }) { Text("Retry") }
            }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(posts, key = { it.id }) { post ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(post.title, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(post.body, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("User ID: ${post.userId} | Post ID: ${post.id}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
