package reihandio.dev.androidsmartkantin.Remote;


import reihandio.dev.androidsmartkantin.Model.MyResponse;
import reihandio.dev.androidsmartkantin.Model.Sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAU54ELr0:APA91bGFv8_0TS68cE0zF5WPkGfIzM-o8nE90Z5OhGI29xuQF1GKStSEDo6MDmttfu8ZwRNpKpt2XF-numl2QTntRZLJOgFGXQ6_L6nXiB53sXVzXa4B9kYJys_30qPikdsfHkV4Ha_Z"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification (@Body Sender body);
}
