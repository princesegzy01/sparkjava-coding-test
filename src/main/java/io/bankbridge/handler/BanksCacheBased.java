package io.bankbridge.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.bankbridge.model.BankModel;
import io.bankbridge.model.BankModelList;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.h2.util.json.JSONObject;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class BanksCacheBased {

	public static CacheManager cacheManager;
	public static List BankList = new ArrayList();

	public static void init() throws Exception {


		cacheManager = CacheManagerBuilder
				.newCacheManagerBuilder().withCache("banks", CacheConfigurationBuilder
						.newCacheConfigurationBuilder(String.class, BankModel.class, ResourcePoolsBuilder.heap(20)))
				.build();
		cacheManager.init();
//		Cache<String, Object> cache = cacheManager.getCache("banks", String.class, BankModel.class);
		try {
			BankModelList models = new ObjectMapper().readValue(
					Thread.currentThread().getContextClassLoader().getResource("banks-v1.json"), BankModelList.class);

			// loop through each banks in model
			// and store them in a bank hashmap
			// and inturns put them in a List
			for (BankModel model : models.banks) {

//				cache.put(model.bic, model);

				// Declare and put each bank in Hashmap
				Map<String, Object> bank = new HashMap<>();
				bank.put("id", model.bic);
				bank.put("name",model.name);
				bank.put("countryCode", model.countryCode);
				bank.put("products", model.products);

				// Add each bank to BankList list
				BankList.add(bank);
			}
		} catch (Exception e) {
			throw e;
		}
	}

	// Handle function handles the request and response object
	// from the v1/all/banks endpoints
	public static Object handle(Request request, Response response) {

		// set the response type to json
		response.type("application/json");

		// create a list to store the banks
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
			for(int i =0; i <BankList.size(); i++){

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
			String resultAsString = new ObjectMapper().writeValueAsString(_bankList.subList(offset > _bankList.size() ? _bankList.size() : offset, _bankList.size() < 5 ? _bankList.size() : 5	));
			return  resultAsString;
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Error while processing request");
		}


	}

}
