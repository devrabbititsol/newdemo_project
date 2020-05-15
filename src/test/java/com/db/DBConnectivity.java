package com.db;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.ServerAddress;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

public class DBConnectivity {
	
	private static String mongoDBHostName = "dev-app-dcos-public-node.dp.nio.io";
	private static int mongoDBPortNumber = 20001;
	private static String mongoDBUserName = "minion";
	private static String mongoDBPassword = "holes-fmxCv=T";
	private static String mongoDBAuthenticationName = "minion";
	

	public static String validateDBValue(String query, String apendValue) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection con = DriverManager.getConnection("jdbc:mysql://10.118.208.67:11001/lasertag", "lasertag", "test");
			PreparedStatement preparedStmt = con.prepareStatement(query);
			ResultSet rs = preparedStmt.executeQuery(query + "'" + apendValue + "'");
			while (rs.next()) {
				System.out.println(rs.getString(1));
				String value = rs.getString(1);
				con.close();
				return value;
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return "";
	}

	public static boolean deleteDBValue(String query, String apendValue) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection con = DriverManager.getConnection("jdbc:mysql://10.118.208.67:11001/lasertag", "lasertag", "test");
			// String query = "delete from users where id = ?";
			PreparedStatement preparedStmt = con.prepareStatement(query);
			preparedStmt.setString(1, apendValue);
			preparedStmt.execute();
			con.close();
			return true;
		} catch (Exception e) {
			System.out.println(e);
		}
		return false;
	}
	
	
	public static MongoClient authenticateMongoDB(ExtentTest test, Logger logger) {
		MongoCredential mongoCredential = null;
		MongoClient mongoClient = null;
		try {
			mongoCredential = MongoCredential.createCredential(mongoDBUserName, mongoDBAuthenticationName, mongoDBPassword.toCharArray());
			return mongoClient = new MongoClient(new ServerAddress(mongoDBHostName, mongoDBPortNumber), Arrays.asList(mongoCredential));
		} catch (Exception e) {
			test.log(LogStatus.FAIL, "Database is not connected.");
			logger.info("Database is not connected.");
		}
		return mongoClient;
	}
	
	/* @DBName
	 * @tableName
	 * @queryKey
	 * @queryValue
	 * @returnValue 
	 */
	// Here Validating the response from both POST request and DB.
	
	@SuppressWarnings("deprecation")
	public static void mongoDBPostResponseValidation(String dbName, String collectionName, String searchKey, String searchValue, String[] postKeyParameters, JSONObject responseObj, ExtentTest test, Logger logger) {
		MongoClient mongoClient = null;
		try {
			mongoClient = authenticateMongoDB(test, logger);
			if (mongoClient != null) {
				DB db = mongoClient.getDB(dbName);
				
				DBCollection collection = db.getCollection(collectionName);
				BasicDBObject searchQuery = new BasicDBObject();
				if (searchKey.equals("_id")) {
					searchQuery.put(searchKey, new ObjectId(searchValue));
				} else {
					searchQuery.put(searchKey, searchValue);
				}
			JSONArray jsonArray = new JSONArray(collection.find(searchQuery).toArray());
			if (jsonArray.length() > 0) {
				test.log(LogStatus.PASS, "Database is connected.");
				logger.info("Database is connected.");
				JSONObject dbJsonObj = jsonArray.getJSONObject(0);
				for (int i = 0; i < postKeyParameters.length; i++) {
					String key = postKeyParameters[i];
					String reponseValue = responseObj.optString(postKeyParameters[i]);
					String dbValue = dbJsonObj.optString(postKeyParameters[i]);
					if (reponseValue.equals(dbValue)) {
						test.log(LogStatus.PASS, "Successfully Validated the Key : " + key + " , Actual Value(From Web Service Response) : " + reponseValue + "  With Expected Response (From Data Base) : " + dbValue);
						logger.info("Successfully Validated the Key : " + key + " , Actual Value(From Web Service Response) : " + reponseValue + "  With Expected Response (From Data Base) : " + dbValue);
					} else {
						test.log(LogStatus.FAIL, "Assertion-error: Key : " + key + " , Actual Value(From Web Service Response) : " + reponseValue + "  With Expected Response (From Data Base) : " + dbValue);
						logger.info("Assertion-error: Key : " + key + " , Actual Value(From Web Service Response) : " + reponseValue + "  With Expected Response (From Data Base) : " + dbValue);
					}
				}
			} else {
					logger.info("Invalid Response");
				}
			}
		} catch (MongoException me) {
			exceptionHandling(me,test,logger);
		} catch (Exception e) {
			test.log(LogStatus.FAIL, e.getLocalizedMessage());
			logger.info(e.getMessage());
		} finally {
			if (mongoClient != null) {
				mongoClient.close();
			}
		}

	}
	
	
	public static String exceptionHandling(MongoException me, ExtentTest test, Logger logger) {
		String msg = "";
		if (me.getMessage().contains("error 18")) { // Authentication failure code is 18
			System.out.println(msg = "Database authentication is failed.");
		} else if (me instanceof MongoTimeoutException) {
			System.out.println(msg = "Database connection is timed out.");
		}  else {
			System.out.println(msg = "Unexpected exception first " + me.getLocalizedMessage());
		}
		test.log(LogStatus.FAIL, msg);
		logger.info(msg);
		return msg;
	}
	
	// Here Validating the response from both PUT request and DB.
	
	public static void mongoDBPutResponseValidation(String dbName, String collectionName, String searchKey, String searchValue, String []putKeyParameters, JSONObject responseObj, ExtentTest test, Logger logger, String updateValue) {
		MongoClient mongoClient = null;
		try {
			mongoClient = authenticateMongoDB(test, logger);
			if (mongoClient != null) {
				@SuppressWarnings("deprecation")
				DB database = mongoClient.getDB(dbName);
				
				DBCollection collection = database.getCollection(collectionName);
				BasicDBObject searchQuery = new BasicDBObject();
				if (searchKey.equals("_id")) {
					searchQuery.put(searchKey, new ObjectId(searchValue));
				} else {
					searchQuery.put(searchKey, searchValue);
				}
			JSONArray jsonArray = new JSONArray(collection.find(searchQuery).toArray());
			if (jsonArray.length() > 0) {
				test.log(LogStatus.PASS, "Database is connected.");
				logger.info("Database is connected.");
				JSONObject dbJsonObj = jsonArray.getJSONObject(0);
				for (int i = 0; i < putKeyParameters.length; i++) {
					String key = putKeyParameters[i];
					String reponseValue = responseObj.optString(putKeyParameters[i]);
					String dbValue = dbJsonObj.optString(putKeyParameters[i]);
					if (reponseValue.equals(dbValue)) {
						if(dbValue.equals(updateValue)) {
							test.log(LogStatus.PASS, "Successfully Updated the Key : " + key + " , Actual Value(From Web Service Response) : " + reponseValue + "  With Expected Response (From Data Base) : " + dbValue);
							logger.info("Successfulluy Updated the Key : " + key + " , Actual Value(From Web Service Response) : " + reponseValue + "  With Expected Response (From Data Base) : " + dbValue);	
						}
						test.log(LogStatus.PASS, "Successfully Validated the Key : " + key + " , Actual Value(From Web Service Response) : " + reponseValue + "  With Expected Response (From Data Base) : " + dbValue);
						logger.info("Successfully Validated the Key : " + key + " , Actual Value(From Web Service Response) : " + reponseValue + "  With Expected Response (From Data Base) : " + dbValue);
					} else {
						test.log(LogStatus.FAIL, "Assertion-error: Key : " + key + " , Actual Value(From Web Service Response) : "+ reponseValue + "  With Expected Response (From Data Base) : " + dbValue);
						logger.info("Assertion-error: Key : " + key + " , Actual Value(From Web Service Response) : "+ reponseValue + "  With Expected Response (From Data Base) : " + dbValue);
					}
				}
			} else {
				logger.info("Invalid Response");
			}
		 }
		} catch (MongoException me) {
			exceptionHandling(me,test,logger);
		} catch (Exception e) {
			test.log(LogStatus.FAIL, e.getLocalizedMessage());
			logger.info(e.getMessage());
		} finally {
			if (mongoClient != null) {
				mongoClient.close();
			}
		}
	}

	
	@SuppressWarnings({ "deprecation", "unused" })
	public static boolean retrieve(String dbName, String collectionName, String searchKey, String searchValue ,String returnValue, ExtentTest test, Logger logger) {
		MongoClient mongoClient = null;
		try {
			mongoClient = authenticateMongoDB(test, logger);
			if (mongoClient != null) {
			DB database = mongoClient.getDB(dbName);
			
			DBCollection collection = database.getCollection(collectionName);
			BasicDBObject searchQuery = new BasicDBObject();
			if(searchKey.equals("_id")) {
				searchQuery.put(searchKey, new ObjectId(searchValue));
			} else {
			searchQuery.put(searchKey, searchValue);
			}
			//System.out.println(collection.find(searchQuery).toArray());

			JSONArray jsonArray = new JSONArray(collection.find(searchQuery).toArray());
			if (jsonArray.length() > 0) {
				test.log(LogStatus.PASS, "Database is connected.");
				logger.info("Database is connected.");
				JSONObject jsonObj = jsonArray.getJSONObject(0);
				/*
				String returnVal = jsonObj.getString(returnValue);
				System.out.println(returnVal);*/
				return true;
			} else {
				return false;
			}
		  }
		} catch (MongoException me) {
			exceptionHandling(me,test,logger);
			return true;
		} catch (Exception e) {
			test.log(LogStatus.FAIL, e.getLocalizedMessage());
			logger.info(e.getMessage());
		}  finally {
			if (mongoClient != null) {
				mongoClient.close();
			}
		}
		
		return true;
	}
	
	@SuppressWarnings({ "deprecation", "unused" })
	public static String doDBDeleteValidate(String dbName, String collectionName, String searchKey, String searchValue ,String returnValue, ExtentTest test, Logger logger) {
		MongoClient mongoClient = null;
		try {
			mongoClient = authenticateMongoDB(test, logger);
			if (mongoClient != null) {
			DB database = mongoClient.getDB(dbName);
			
			DBCollection collection = database.getCollection(collectionName);
			BasicDBObject searchQuery = new BasicDBObject();
			if(searchKey.equals("_id")) {
				searchQuery.put(searchKey, new ObjectId(searchValue));
			} else {
			searchQuery.put(searchKey, searchValue);
			}
			
			JSONArray jsonArray = new JSONArray(collection.find(searchQuery).toArray());
			if (jsonArray.length() > 0) {
				test.log(LogStatus.PASS, "Database is connected.");
				logger.info("Database is connected.");
				JSONObject jsonObj = jsonArray.getJSONObject(0);
			
				return "true";
			} else {
				return "false";
			}
		  }
		} catch (MongoException me) {
			return exceptionHandling(me,test,logger);
			
		} catch (Exception e) {
			test.log(LogStatus.FAIL, e.getLocalizedMessage());
			logger.info(e.getMessage());
			return e.getMessage();
		}  finally {
			if (mongoClient != null) {
				mongoClient.close();
			}
		}
		return "Something went wrong";
		
		
	}
	
	

	@SuppressWarnings("deprecation")
	public static boolean tagRetrieve(String dbName, String collectionName, String searchKey, String searchValue, ExtentTest test, Logger logger) {
		MongoClient mongoClient = null;
		try {
			mongoClient = authenticateMongoDB(test, logger);
			if (mongoClient != null) {
				DB database = mongoClient.getDB(dbName);
				DBCollection collection = database.getCollection(collectionName);
				BasicDBObject searchQuery = new BasicDBObject();
				if (searchKey.equals("_id")) {
					searchQuery.put(searchKey, new ObjectId(searchValue));
				} else {
					searchQuery.put(searchKey, searchValue);
				}
				JSONArray jsonArray = new JSONArray(collection.find(searchQuery).toArray());
				if (jsonArray.length() > 0) {
					test.log(LogStatus.PASS, "Database is connected.");
					logger.info("Database is connected.");
					return true;
				} else {
					return false;
				}
			}
		} catch (MongoException me) {
			exceptionHandling(me,test,logger);
		} catch (Exception e) {
			test.log(LogStatus.FAIL, e.getLocalizedMessage());
			logger.info(e.getMessage());
		}  finally {
			if (mongoClient != null) {
				mongoClient.close();
			}
		}
		return false;
	}


}
