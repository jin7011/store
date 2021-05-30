package com.example.sns_project.fcm;

import android.app.Activity;
import android.util.Log;
import androidx.annotation.NonNull;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import org.json.JSONException;
import org.json.JSONObject;

public class SendServer {

    public SendServer(){}

    public void Send(String title, String body,Activity activity){
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                String token = task.getResult();

                //JSON형식으로 데이터 통신
                String url = "https://noti0711.herokuapp.com/";
                JSONObject testjson = new JSONObject();

                try {
                    //입력해둔 edittext의 id와 pw값을 받아와 put해줍니다 : 데이터를 json형식으로 바꿔 넣어주었습니다.
                    testjson.put("title",title);
                    testjson.put("body",body);
                    testjson.put("token",token);
                    Log.d("zxcaqw123",token);
                    String jsonString = testjson.toString(); //완성된 json 포맷

                    //이제 전송
                    final RequestQueue requestQueue = Volley.newRequestQueue(activity);
                    final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                            Request.Method.POST,
                            url,
                            testjson,
                            new Response.Listener<JSONObject>() {

                        //데이터 전달을 끝내고 이제 그 응답을 받을 차례입니다.
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                //받은 json형식의 응답을 받아
                                JSONObject jsonObject = new JSONObject(response.toString());
                                String res = jsonObject.getString("token");

                                if(res != null){
                                    Log.d("zxcaqw123",res);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        //서버로 데이터 전달 및 응답 받기에 실패한 경우 아래 코드가 실행됩니다.
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            //Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    requestQueue.add(jsonObjectRequest);
                    //
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
