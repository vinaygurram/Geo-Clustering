package somthgi;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipart;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;

/**
 * Created by gurramvinay on 7/15/15.
 */
public class DataCrawler {


    public void getData(HttpClient httpClient, int id, FileWriter fileWriter){

        try {
            HttpPost postRequest = new HttpPost("https://m.gymer.in/api/gym_details.php");
            postRequest.setHeader("Content-Type","multipart/form-data");
            MultipartEntityBuilder multipartEntityBuilder =  MultipartEntityBuilder.create();
            multipartEntityBuilder.addTextBody("id",String.valueOf(id), ContentType.MULTIPART_FORM_DATA);
            multipartEntityBuilder.addTextBody("token","5caabf406424595346aee1a24d277fee:62cc7e27f3fba97709f568533236eade\"", ContentType.MULTIPART_FORM_DATA);
            postRequest.setEntity(multipartEntityBuilder.build());
            HttpResponse response = httpClient.execute(postRequest);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer result = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            JSONObject jsonObject = new JSONObject(result.toString());

            jsonObject = jsonObject.getJSONObject("Response");
            JSONArray gym_details = jsonObject.getJSONArray("gym_details");
            JSONObject gym = gym_details.getJSONObject(0);

            fileWriter.write(gym.getString("id")+gym.getString("gym_name")+gym.getString("gym_address")+gym.getString("latitude_longitude")+
                    gym.getString("area")+gym.getString("facilities")+gym.getString("mobile")+gym.getString("landline")+
                    gym.getString("email")+gym.getString("about")+gym.getString("operation_hrs")+gym.getString("price")+gym.getString("logo"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public static void main(String[] args){
        DataCrawler dataCrawler = new DataCrawler();
        HttpClient httpClient = HttpClientBuilder.create().build();
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter("src/main/resources/GYMDATA.csv");
            fileWriter.write("id,gym_name,gym_address,latitude_longitude,area,facilities,mobile,landline,email,about,operation_hrs,price,logo");
            for(int i=81;i<207;i++){
                dataCrawler.getData(httpClient,i,fileWriter);
            }
            fileWriter.close();

        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
