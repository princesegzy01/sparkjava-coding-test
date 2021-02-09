package io.bankbridge.handler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URI;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.bankbridge.model.BankModel;
import spark.Request;
import spark.Response;

public class BanksRemoteCalls {

	private static Map config;
	public static List BankList = new ArrayList();


	public static void init() throws Exception {
		config = new ObjectMapper()
				.readValue(Thread.currentThread().getContextClassLoader().getResource("banks-v2.json"), Map.class);
	}

	public static String handle(Request request, Response response) {
		response.type("application/json");

		//Get the Banks record from the GetData function
		BankList = GetData();

		// create reusable _bankList variable
		List _bankList = BankList;

		// default country code to empty
		String countryCode = "";

		// set default offset to 0
		int offset = 0;

		// set default limit to 0
		int limit = 5;

		// check if user pass countryCode as part of the query string
		// if yes, filter BankList and return record with only  country code provided
		if(request.queryParams("countryCode") != null &&  !request.queryParams("countryCode").isEmpty()){

			// retrieve country code from request
			countryCode = request.queryParams("countryCode");

			// create a temporary list
			// to store return bank record
			List _newBankList = new ArrayList();

			// loop through record to get
			// match banks with same country code
			for(int i =0; i < BankList.size(); i++){

				// get the current Map at a certain index
				Map<String, Object> _d = (Map<String, Object>) BankList.get(i);

				// do comaprison with the provided country codes
				if(_d.get("countryCode").equals(countryCode)){

					// if found,
					// add the record to the temporary bank list
					_newBankList.add(_d);
				}
			}

			// if temporary bank list is empty
			// return the empty result
			if(_newBankList.isEmpty()){

				// run json conversion in a try/catch
				try {

					// convert the empty result to a jackson josn variable
					String resultAsString = new ObjectMapper().writeValueAsString(_newBankList);

					// return the empty jackson json value
					return  resultAsString;
				} catch (JsonProcessingException e) {
					throw new RuntimeException("Error while processing request");
				}
			}

			// if temporary banklist is not empty,
			// set the _bankList variable to the temp list
			_bankList = _newBankList;

		}

		// check if user pass offset as part of the query string
		if(request.queryParams("offset") != null && Integer.parseInt(request.queryParams("offset")) >= 0){

			// if yes, set the offset as the request query string
			offset = Integer.parseInt(request.queryParams("offset"));

			// if the limit querystring is not supplied
			// return a 400 BadRequest code
			if(request.queryParams("limit") == null){
				response.status(400);
				return "{\"error\":\"Limit must be provided \"}";
			}

			// if the supplied limit is less than 0
			// return a 400 BadRequest error and a message
			if(request.queryParams("limit") != null && Integer.parseInt(request.queryParams("limit")) < 0){
				response.status(400);
				return "{\"error\":\"Limit must be greater than 0\"}";
			}

			// if limit queryString meets requirements
			if(!request.queryParams("limit").isEmpty() && Integer.parseInt(request.queryParams("limit")) > 0){

				// set limit variable as limit queryString
				limit = Integer.parseInt(request.queryParams("limit"));

				// if addition of offset and limit is
				// greater than the bank list
				if ((offset + limit ) >  _bankList.size()){

					// set the limit to the total size
					limit = _bankList.size();
				}else {

					// set limit as offset + limit
					limit = offset + limit;
				}

				try {
					// return data from the offset and limit
					String resultAsString = new ObjectMapper().writeValueAsString(_bankList.subList(offset > _bankList.size() ? _bankList.size() : offset, limit));
					return  resultAsString;
				} catch (JsonProcessingException e) {
					throw new RuntimeException("Error while processing request");
				}
			}
		}

		// return data for default limit and offset
		try {
			String resultAsString = new ObjectMapper().writeValueAsString(_bankList.subList(offset > _bankList.size() ? _bankList.size() : offset, _bankList.size() < 5 ? _bankList.size() : 5));
			return  resultAsString;
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Error while processing request");
		}
	}

	// GetData function Calls the Httpclient
	// to get information about the individual
	// information for each bank's endpoints
	public  static  List<Map<String, Object>> GetData(){

		List _BankList = new ArrayList();
		ObjectMapper objectMapper = new ObjectMapper();

		// loop through each banks in config
		// and store them in a bank hashmap
		// and inturns put them in a List
		for (Object url : config.values()) {

			// make http connection to the bank url
			// return the String response and store in a variable
			String json = MakeRequest(url.toString());

			try {

				// convert String response and convert
				// to a Json representation
				JsonNode jsonNode = objectMapper.readTree(json);

				// Declare and put each bank in Hashmap
				Map<String, Object> bank = new HashMap<>();
				bank.put("id", jsonNode.get("bic").asText());
				bank.put("name",jsonNode.get("name").asText());
				bank.put("countryCode", jsonNode.get("countryCode").asText());
				bank.put("auth", jsonNode.get("auth").asText());

				// Add each bank to BankList list
				_BankList.add(bank);

			}catch (IOException e) {
				e.printStackTrace();
			}
		}

		// return the Array of BankList
		return  _BankList;
	}

	// MakeRequest function handles http client to make
	// Request to external API endpoint and return response
	public static String MakeRequest(String endpoint){
		try {

			// creatr the httpclient variable
			HttpClient client = HttpClient.newHttpClient();

			// use the client to make a Get request
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(endpoint)).build();

			// Get the response object
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			// return the response body
			return response.body();

		} catch (MalformedURLException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return "";
	}
}
