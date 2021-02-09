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
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BanksCacheBased {


	public static CacheManager cacheManager;
//	private static boolean BankList;
	//	public List BankList;
	public static List BankList = new ArrayList();

	public static void init() throws Exception {


		cacheManager = CacheManagerBuilder
				.newCacheManagerBuilder().withCache("banks", CacheConfigurationBuilder
						.newCacheConfigurationBuilder(String.class, BankModel.class, ResourcePoolsBuilder.heap(20)))
				.build();
		cacheManager.init();
		Cache cache = cacheManager.getCache("banks", String.class, BankModel.class);
		try {
			BankModelList models = new ObjectMapper().readValue(
					Thread.currentThread().getContextClassLoader().getResource("banks-v1.json"), BankModelList.class);
			for (BankModel model : models.banks) {

				cache.put(model.bic, model);

				HashMap<String, Object> bank = new HashMap<>();
				bank.put("id", model.bic);
				bank.put("name",model.name);
				bank.put("countryCode", model.countryCode);
				bank.put("products", model.products);

				BankList.add(bank);
			}
//			System.out.println(BankList);
		} catch (Exception e) {
			throw e;
		}
	}

	public static String handle(Request request, Response response) {
//		List<BankModel> result = new ArrayList<>();
//		cacheManager.getCache("banks", String.class, BankModel.class).forEach(entry -> {
//			result.add(entry.getValue());
//		});
		try {
			String resultAsString = new ObjectMapper().writeValueAsString(BankList);
			return resultAsString;
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Error while processing request");
		}

	}

}
