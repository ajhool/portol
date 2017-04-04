package com.portol.webserver.repository;

import javax.servlet.http.Cookie;

import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.portol.common.model.PortolToken;
import com.portol.common.model.user.User;
import com.portol.webserver.manager.MongoManaged;

public class UserRepository {

	// the DB we are using
	private JacksonDBCollection<User, String> userMongo;

	public UserRepository(MongoManaged userMongoManaged) {
		this.userMongo = JacksonDBCollection.wrap(userMongoManaged.getDB().getCollection("user"), User.class,
				String.class);
	}

	public WriteResult<User, String> save(User incoming) {
		return userMongo.save(incoming);
	}

	public User findOneById(String id) throws Exception {
		BasicDBObject query = new BasicDBObject();
		query.put("_id", id);
		DBCursor<User> users = userMongo.find(query);

		if (users.count() != 1) {
			return null;
		}

		return users.toArray().get(0);

	}

	public User findOneByEmail(User newUser) throws Exception {
		BasicDBObject query = new BasicDBObject();
		query.put("email", newUser.getEmail());
		DBCursor<User> users = userMongo.find(query);

		if (users.count() < 1) {
			return null;
		}

		return users.toArray().get(0);

	}

	public User findExistingUser(User loggingIn) throws Exception {

		BasicDBObject query = new BasicDBObject("userName", loggingIn.getUserName());
		DBCursor<User> users = userMongo.find(query);

		if (users == null || users.count() == 0) {
			return null;
		}

		if (users.count() > 1) {
			throw new Exception("wrong number of users found... multiple fields matching now....");
		}

		User tentative = users.toArray().get(0);

		return tentative;

	}

	public User findOneByToken(PortolToken validToken) throws Exception {
		BasicDBObject query = new BasicDBObject("currentToken.value", validToken.getValue());

		DBCursor<User> users = userMongo.find(query);

		if (users == null || users.count() == 0) {
			return null;
		}

		if (users.count() > 1) {
			throw new Exception("wrong number of users found... should vae unique token per user");
		}

		return users.toArray().get(0);
	}

	private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

	public User findUserLoggedOnPlatform(String platformToFind) throws Exception {

		if (platformToFind == null || platformToFind.length() < 2) {
			return null;
		}
		// just go down the array checking for
		DBCursor<User> users = null;

		BasicDBObject query = new BasicDBObject("platforms._id", platformToFind);
		BasicDBObject fields = new BasicDBObject("platforms.$", 1);

		users = userMongo.find(query, fields);
		if (users != null) {

			if (users.count() > 1) {
				logger.error("too many users with the same platform");
			}

		} else
			return null;

		if (users == null || users.count() == 0)
			return null;

		User tentative = users.toArray().get(0);

		User full = this.findOneById(tentative.getUserId());

		return full;

	}

	public User findUserWithCookie(Cookie[] cookies) throws Exception {
		if (cookies == null) {
			return null;
		}
		// just go down the array checking for
		DBCursor<User> users = null;
		for (int i = 0; i < cookies.length; i++) {

			Cookie toFind = cookies[i];
			if (toFind == null)
				continue;

			BasicDBObject query = new BasicDBObject("platforms._id", toFind.getValue());
			BasicDBObject fields = new BasicDBObject("platforms.$", 1);

			users = userMongo.find(query, fields);
			if (users != null) {
				if (users.count() == 1) {
					break;
				}

				if (users.count() > 1) {
					logger.error("too many users with the same cookie");
				}

			}
		}

		if (users == null || users.count() == 0)
			return null;

		User tentative = users.toArray().get(0);
		User full = this.findOneById(tentative.getUserId());

		return full;
	}

}
